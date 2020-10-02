package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.tags.BoundaryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import io.netty.util.internal.StringUtil;

/**
 * @author srachanski
 */
public class BoundaryIntersectionCheck extends BaseCheck<Long> {
    
    private static final String INVALID_BOUNDARY_FORMAT = "Boundary {0} with way {1} is crossing invalidly with boundary(ies) {3} with way {2} at coordinates {4}.";
    private static final String INSTRUCTION_FORMAT = INVALID_BOUNDARY_FORMAT + " Two boundaries should not intersect each other.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INSTRUCTION_FORMAT, INVALID_BOUNDARY_FORMAT);
    
    private static final Predicate<LineItem> LINE_ITEM_AS_BOUNDARY = lineItem -> lineItem.relations()
            .stream()
            .anyMatch(BoundaryIntersectionCheck::isTypeBoundaryWithBoundaryTag);
    public static final String DELIMITER = ", ";
    public static final int INDEX = 0;
    
    public BoundaryIntersectionCheck(final Configuration configuration) {
        super(configuration);
    }
    
    private static boolean isTypeBoundaryWithBoundaryTag(final AtlasObject object) {
        return Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.BOUNDARY) &&
                Validators.hasValuesFor(object, BoundaryTag.class);
    }
    
    @Override
    public boolean validCheckForObject(final AtlasObject object) {
        return object instanceof Relation &&
                isTypeBoundaryWithBoundaryTag(object);
    }
    
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object) {
        final Relation relation = (Relation) object;
        final RelationMemberList relationMemberLineItems = relation.membersOfType(ItemType.EDGE);
        relationMemberLineItems.addAll(relation.membersOfType(ItemType.LINE));
        final List<LineItem> lineItems = this.getLineItems(relationMemberLineItems);
        final CheckFlag checkFlag = this.prepareCheckFlag(object, relation, lineItems);
        if (checkFlag.getFlaggedObjects().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(checkFlag);
    }
    
    private List<LineItem> getLineItems(final RelationMemberList relationMembersEdges) {
        return relationMembersEdges
                .stream()
                .map(this::castToLineItem)
                .collect(Collectors.toList());
    }
    
    private LineItem castToLineItem(final RelationMember relationMember) {
        return (LineItem) relationMember.getEntity();
    }
    
    private CheckFlag prepareCheckFlag(final AtlasObject object, final Relation relation, final List<LineItem> lineItems) {
        final CheckFlag checkFlag = new CheckFlag(this.getTaskIdentifier(object));
        lineItems.forEach(lineItem -> this.analyzeIntersections(object.getAtlas(), relation, checkFlag, lineItems, lineItem));
        return checkFlag;
    }
    
    private void analyzeIntersections(final Atlas atlas, final Relation relation, final CheckFlag checkFlag, final List<LineItem> lineItems, final LineItem lineItem) {
        final Set<LineItem> knownIntersections = new HashSet<>();
        final Set<LineItem> intersections = lineItems
                .stream()
                .map(currentLineItem -> atlas.lineItemsIntersecting(currentLineItem.bounds(), this.getPredicateForLineItemsSelection(lineItems, lineItem)))
                .flatMap(iterable -> StreamSupport.stream(iterable.spliterator(), false))
                .collect(Collectors.toSet());
        for (final LineItem currentLineItem : intersections) {
            if (!knownIntersections.contains(currentLineItem)) {
                final Set<Relation> intersectingBoundaries = this.getBoundary(currentLineItem)
                        .stream()
                        .filter(boundary -> boundary.getOsmIdentifier() < relation.getOsmIdentifier())
                        .collect(Collectors.toSet());
                if (!intersectingBoundaries.isEmpty()) {
                    this.addInformationToFlag(relation, checkFlag, lineItem, currentLineItem, intersectingBoundaries);
                    knownIntersections.add(currentLineItem);
                }
            }
        }
    }
    
    private void addInformationToFlag(final Relation relation, final CheckFlag checkFlag, final LineItem lineItem, final LineItem currentLineItem, final Set<Relation> intersectingBoundaries) {
        final Set<Location> intersectingPoints = this.getIntersectingPoints(lineItem, currentLineItem);
        checkFlag.addPoints(intersectingPoints);
        checkFlag.addObject(currentLineItem);
        checkFlag.addObjects(intersectingBoundaries);
        final String instruction = this.getLocalizedInstruction(INDEX,
                Long.toString(relation.getOsmIdentifier()),
                Long.toString(lineItem.getOsmIdentifier()),
                Long.toString(currentLineItem.getOsmIdentifier()),
                this.relationsToList(intersectingBoundaries),
                this.locationsToList(intersectingPoints));
        if (StringUtil.isNullOrEmpty(checkFlag.getInstructions()) || !checkFlag.getInstructions().contains(instruction)) {
            checkFlag.addObject(lineItem);
            checkFlag.addObject(relation);
            checkFlag.addInstruction(instruction);
        }
    }
    
    private Predicate<LineItem> getPredicateForLineItemsSelection(final List<LineItem> lineItems, final LineItem currentLineItem) {
        return lineItemToCheck ->
        {
            WKTReader wktReader = new WKTReader();
            if (LINE_ITEM_AS_BOUNDARY.test(lineItemToCheck) && !lineItems.contains(lineItemToCheck)) {
                try {
                    Geometry line1 = wktReader.read(lineItemToCheck.asPolyLine().toWkt());
                    Geometry line2 = wktReader.read(currentLineItem.asPolyLine().toWkt());
                    return line1.crosses(line2) &&
                            !line1.touches(line2);
                } catch (ParseException e) {
                    return false;
                }
            }
            return false;
        };
    }
    
    private String relationsToList(final Set<Relation> relations) {
        return relations
                .stream()
                .map(relation -> Long.toString(relation.getOsmIdentifier()))
                .collect(Collectors.joining(DELIMITER));
    }
    
    private String locationsToList(final Set<Location> locations) {
        return locations
                .stream()
                .map(location -> String.format("(%s, %s)", location.getLatitude(), location.getLongitude()))
                .collect(Collectors.joining(DELIMITER));
    }
    
    private Set<Relation> getBoundary(final LineItem currentLineItem) {
        return currentLineItem.relations()
                .stream()
                .filter(BoundaryIntersectionCheck::isTypeBoundaryWithBoundaryTag)
                .collect(Collectors.toSet());
    }
    
    private Set<Location> getIntersectingPoints(final LineItem lineItem, final LineItem currentLineItem) {
        return currentLineItem.asPolyLine().intersections(lineItem.asPolyLine());
    }
    
    @Override
    protected List<String> getFallbackInstructions() {
        return FALLBACK_INSTRUCTIONS;
    }
    
}
