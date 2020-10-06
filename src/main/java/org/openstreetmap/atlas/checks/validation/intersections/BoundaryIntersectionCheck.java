package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class BoundaryIntersectionCheck extends BaseCheck<Long>
{
    
    private static final String INVALID_BOUNDARY_FORMAT = "Boundary {0} with way {1} is crossing invalidly with boundary {3} with way {2} at coordinates {4}.";
    private static final String INSTRUCTION_FORMAT = INVALID_BOUNDARY_FORMAT + " Two boundaries should not intersect each other.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INSTRUCTION_FORMAT, INVALID_BOUNDARY_FORMAT);
    public static final String DELIMITER = ", ";
    public static final int INDEX = 0;
    public static final String TYPE = "type";
    public static final String BOUNDARY = "boundary";
    
    public static final String EMPTY = "";
    private static final Predicate<LineItem> LINE_ITEM_AS_BOUNDARY = lineItem -> lineItem.relations()
            .stream()
            .anyMatch(lineToCheck -> BoundaryIntersectionCheck.isRelationTypeBoundaryWithBoundaryTag(lineToCheck) ||
                    (BOUNDARY.equals(lineToCheck.getTag(TYPE).orElse(EMPTY)) &&
                            lineToCheck.getTag(BOUNDARY).isPresent()));
    
    public BoundaryIntersectionCheck(final Configuration configuration)
    {
        super(configuration);
    }
    
    private static boolean isRelationTypeBoundaryWithBoundaryTag(final AtlasObject object)
    {
        return Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.BOUNDARY) &&
                Validators.hasValuesFor(object, BoundaryTag.class);
    }
    
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Relation &&
                isRelationTypeBoundaryWithBoundaryTag(object);
    }
    
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Relation relation = (Relation) object;
        final RelationMemberList relationMemberLineItems = relation.membersOfType(ItemType.EDGE);
        relationMemberLineItems.addAll(relation.membersOfType(ItemType.LINE));
        final List<LineItem> lineItems = this.getLineItems(relationMemberLineItems);
        final CheckFlag checkFlag = this.prepareCheckFlag(object, relation, lineItems);
        if (checkFlag.getFlaggedObjects().isEmpty())
        {
            return Optional.empty();
        }
        return Optional.of(checkFlag);
    }
    
    private List<LineItem> getLineItems(final RelationMemberList relationMembersEdges)
    {
        return relationMembersEdges
                .stream()
                .map(this::castToLineItem)
                .collect(Collectors.toList());
    }
    
    private LineItem castToLineItem(final RelationMember relationMember)
    {
        return (LineItem) relationMember.getEntity();
    }
    
    private CheckFlag prepareCheckFlag(final AtlasObject object, final Relation relation, final List<LineItem> lineItems)
    {
        final CheckFlag checkFlag = new CheckFlag(this.getTaskIdentifier(object));
        lineItems.forEach(lineItem -> this.analyzeIntersections(object.getAtlas(), relation, checkFlag, lineItems, lineItem));
        return checkFlag;
    }
    
    private void analyzeIntersections(final Atlas atlas, final Relation relation, final CheckFlag checkFlag, final List<LineItem> lineItems, final LineItem lineItem)
    {
        final Intersections intersections = new Intersections();
    
        lineItems
            .stream()
            .map(currentLineItem -> atlas.lineItemsIntersecting(currentLineItem.bounds(), this.getPredicateForLineItemsSelection(lineItems, lineItem)))
            .flatMap(iterable -> StreamSupport.stream(iterable.spliterator(), false))
            .forEach(currentLineItem ->
            {
                final Set<Relation> boundaries = this.getBoundary(currentLineItem);
                boundaries.forEach(boundary -> intersections.add(boundary, currentLineItem));
            });
        for(final Relation currentRelation : intersections.getRelations())
        {
            if(currentRelation.getOsmIdentifier() < relation.getOsmIdentifier())
            {
                this.addInformationToFlag(checkFlag, relation, lineItem, currentRelation, intersections.getLineItemsMap(currentRelation));
            }
        }
    }
    
    private void addInformationToFlag(final CheckFlag checkFlag, final Relation relation, final LineItem lineItem, final Relation intersectingBoundary, final Map<Long, Set<LineItem>> lineItems)
    {
        for(final Long osmId : lineItems.keySet())
        {
            final Set<LineItem> currentLineItems = lineItems.get(osmId);
            final Set<Location> intersectingPoints = this.getIntersectingPoints(lineItem, currentLineItems);
            checkFlag.addPoints(intersectingPoints);
            checkFlag.addObjects(currentLineItems);
            checkFlag.addObject(intersectingBoundary);
            final String instruction = this.getLocalizedInstruction(INDEX,
                    Long.toString(relation.getOsmIdentifier()),
                    Long.toString(lineItem.getOsmIdentifier()),
                    Long.toString(osmId),
                    Long.toString(intersectingBoundary.getOsmIdentifier()),
                    this.locationsToList(intersectingPoints));
            if (StringUtil.isNullOrEmpty(checkFlag.getInstructions()) || !checkFlag.getInstructions().contains(instruction))
            {
                checkFlag.addObject(lineItem);
                checkFlag.addObject(relation);
                checkFlag.addInstruction(instruction);
            }
        }
    }
    
    private Predicate<LineItem> getPredicateForLineItemsSelection(final List<LineItem> lineItems, final LineItem currentLineItem)
    {
        return lineItemToCheck ->
        {
            final WKTReader wktReader = new WKTReader();
            if (LINE_ITEM_AS_BOUNDARY.test(lineItemToCheck) &&
                    !lineItems.contains(lineItemToCheck))
            {
                try
                {
                    final Geometry line1 = wktReader.read(lineItemToCheck.asPolyLine().toWkt());
                    final Geometry line2 = wktReader.read(currentLineItem.asPolyLine().toWkt());
                    return line1.crosses(line2) &&
                            !line1.touches(line2);
                }
                catch (final ParseException e)
                {
                    return false;
                }
            }
            return false;
        };
    }
 
    private String locationsToList(final Set<Location> locations)
    {
        return locations
                .stream()
                .map(location -> String.format("(%s, %s)", location.getLatitude(), location.getLongitude()))
                .collect(Collectors.joining(DELIMITER));
    }
    
    private Set<Relation> getBoundary(final LineItem currentLineItem)
    {
        return currentLineItem.relations()
                .stream()
                .filter(BoundaryIntersectionCheck::isRelationTypeBoundaryWithBoundaryTag)
                .collect(Collectors.toSet());
    }
    
    private Set<Location> getIntersectingPoints(final LineItem lineItem, final Set<LineItem> currentLineItems)
    {
        return currentLineItems
                .stream()
                .map(currentLineItem -> currentLineItem.asPolyLine().intersections(lineItem.asPolyLine()))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
    
    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
    
}

class Intersections
{
    private final Map<Relation, Map<Long, Set<LineItem>>> relationIntersections = new HashMap<>();
    
    void add(Relation relation, LineItem lineItem)
    {
        relationIntersections.computeIfAbsent(relation, k -> new HashMap<>());
        long osmIdentifier = lineItem.getOsmIdentifier();
        relationIntersections.get(relation).computeIfAbsent(osmIdentifier, k -> new HashSet<>());
        relationIntersections.get(relation).get(osmIdentifier).add(lineItem);
    }
    
    Set<Relation> getRelations()
    {
        return relationIntersections.keySet();
    }
    
    Set<Long> getWayIdsForRelation(Relation relation)
    {
        return  relationIntersections.get(relation).keySet();
    }
    
    Set<LineItem> getLineItems(Relation relation, Long osmId)
    {
        return relationIntersections.get(relation).get(osmId);
    }
    
    Map<Long, Set<LineItem>> getLineItemsMap(Relation relation)
    {
        return relationIntersections.get(relation);
    }
    
}
