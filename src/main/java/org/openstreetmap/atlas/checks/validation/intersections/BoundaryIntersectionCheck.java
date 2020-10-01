package org.openstreetmap.atlas.checks.validation.intersections;

import io.netty.util.internal.StringUtil;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author srachanski
 */
public class BoundaryIntersectionCheck extends BaseCheck<Long> {
    
    private static final String INVALID_BOUNDARY_FORMAT = "Boundary {0,number,#} with way {1} is crossing invalidly with boundary(ies) {3} with way {2}.";
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
    
    private static boolean isTypeBoundaryWithBoundaryTag(AtlasObject object) {
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
        Relation relation = (Relation) object;
        RelationMemberList relationMemberLineItems = relation.membersOfType(ItemType.EDGE);
        relationMemberLineItems.addAll(relation.membersOfType(ItemType.LINE));
        List<LineItem> lineItems = getLineItems(relationMemberLineItems);
        CheckFlag checkFlag = prepareCheckFlag(object, relation, lineItems);
        if (checkFlag.getFlaggedObjects().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(checkFlag);
    }
    
    private List<LineItem> getLineItems(RelationMemberList relationMembersEdges) {
        return relationMembersEdges
                .stream()
                .map(this::castToLineItem)
                .collect(Collectors.toList());
    }
    
    private LineItem castToLineItem(RelationMember relationMember) {
        return (LineItem) relationMember.getEntity();
    }
    
    private CheckFlag prepareCheckFlag(AtlasObject object, Relation relation, List<LineItem> lineItems) {
        CheckFlag checkFlag = new CheckFlag(this.getTaskIdentifier(object));
        lineItems.forEach(lineItem -> analyzeIntersections(object.getAtlas(), relation, checkFlag, lineItems, lineItem));
        return checkFlag;
    }
    
    private void analyzeIntersections(Atlas atlas, Relation relation, CheckFlag checkFlag, List<LineItem> lineItems, LineItem lineItem) {
        Set<LineItem> knownIntersections = new HashSet<>();
        Set<LineItem> intersections = lineItems
                .stream()
                .map(currentLineItem -> atlas.lineItemsIntersecting(currentLineItem.bounds(), getPredicateForLineItemsSelection(lineItems, currentLineItem)))
                .flatMap(iterable -> StreamSupport.stream(iterable.spliterator(), false))
                .collect(Collectors.toSet());
        for (LineItem currentLineItem : intersections) {
            if (!knownIntersections.contains(currentLineItem)) {
                Set<Relation> intersectingBoundaries = getBoundary(currentLineItem);
                addInformationToFlag(relation, checkFlag, lineItem, currentLineItem, intersectingBoundaries);
                knownIntersections.add(currentLineItem);
            }
        }
    }
    
    private void addInformationToFlag(Relation relation, CheckFlag checkFlag, LineItem lineItem, LineItem currentLineItem, Set<Relation> intersectingBoundaries) {
        checkFlag.addPoints(getIntersectingPoints(lineItem, currentLineItem));
        checkFlag.addObject(currentLineItem);
        checkFlag.addObjects(intersectingBoundaries);
        String instruction = this.getLocalizedInstruction(INDEX,
                relation.getOsmIdentifier(),
                lineItem.getOsmIdentifier(),
                currentLineItem.getOsmIdentifier(),
                asList(intersectingBoundaries));
        if(StringUtil.isNullOrEmpty(checkFlag.getInstructions()) || !checkFlag.getInstructions().contains(instruction)) {
            checkFlag.addObject(lineItem);
            checkFlag.addObject(relation);
            checkFlag.addInstruction(instruction);
        }
    }
    
    private Predicate<LineItem> getPredicateForLineItemsSelection(List<LineItem> lineItems, LineItem currentLineItem) {
        return lineItemToCheck -> LINE_ITEM_AS_BOUNDARY.test(lineItemToCheck) &&
                !lineItems.contains(lineItemToCheck) &&
                lineItemToCheck.asPolyLine().intersects(currentLineItem.asPolyLine());
    }
    
    private String asList(Set<Relation> intersectingBoundaries) {
        return intersectingBoundaries
                .stream()
                .map(relation -> Long.toString(relation.getOsmIdentifier()))
                .collect(Collectors.joining(DELIMITER));
    }
    
    private Set<Relation> getBoundary(LineItem currentLineItem) {
        return currentLineItem.relations()
                .stream()
                .filter(BoundaryIntersectionCheck::isTypeBoundaryWithBoundaryTag)
                .collect(Collectors.toSet());
    }
    
    private Set<Location> getIntersectingPoints(LineItem lineItem, LineItem currentLineItem) {
        return currentLineItem.asPolyLine().intersections(lineItem.asPolyLine());
    }
    
    @Override
    protected List<String> getFallbackInstructions() {
        return FALLBACK_INSTRUCTIONS;
    }
    
}
