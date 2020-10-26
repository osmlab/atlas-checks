package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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
import org.openstreetmap.atlas.checks.utility.RelationIntersections;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.multi.MultiLine;
import org.openstreetmap.atlas.geography.atlas.multi.MultiRelation;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.BoundaryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * @author srachanski
 */
public class BoundaryIntersectionCheck_old extends BaseCheck<Long>
{

    private static final String INVALID_BOUNDARY_FORMAT = "Boundary {0} with way {1} is crossing invalidly with boundary {3} with way {2} at coordinates {4}.";
    private static final String INSTRUCTION_FORMAT = INVALID_BOUNDARY_FORMAT
            + " Two boundaries should not intersect each other.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INSTRUCTION_FORMAT,
            INVALID_BOUNDARY_FORMAT);
    public static final String DELIMITER = ", ";
    public static final int INDEX = 0;
    public static final String TYPE = "type";
    public static final String BOUNDARY = "boundary";
    public static final String EMPTY = "";

    private static final Predicate<LineItem> LINE_ITEM_WITH_BOUNDARY_TAGS = lineItem -> BOUNDARY
            .equals(lineItem.getTag(TYPE).orElse(EMPTY)) && lineItem.getTag(BOUNDARY).isPresent();

    private static final Predicate<LineItem> LINE_ITEM_AS_BOUNDARY = lineItem -> lineItem
            .relations().stream()
            .anyMatch(relationToCheck -> BoundaryIntersectionCheck_old
                    .isRelationTypeBoundaryWithBoundaryTag(relationToCheck)
                    || LINE_ITEM_WITH_BOUNDARY_TAGS.test(lineItem));

    private static LineItem castToLineItem(final RelationMember relationMember)
    {
        return (LineItem) relationMember.getEntity();
    }

    private static Set<LineItem> getLineItems(final RelationMemberList relationMembers)
    {
        return relationMembers.stream().map(BoundaryIntersectionCheck_old::castToLineItem)
                .collect(Collectors.toSet());
    }

    private static boolean isRelationTypeBoundaryWithBoundaryTag(final AtlasObject object)
    {
        return Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.BOUNDARY)
                && Validators.hasValuesFor(object, BoundaryTag.class);
    }

    private static boolean isRelationWithAnyLineItemWithBoundaryTags(final AtlasObject object)
    {
        final Relation relation = (Relation) object;
        return BoundaryIntersectionCheck_old.getLineItems(relation.membersOfType(ItemType.EDGE))
                .stream().anyMatch(LINE_ITEM_WITH_BOUNDARY_TAGS)
                || BoundaryIntersectionCheck_old.getLineItems(relation.membersOfType(ItemType.LINE))
                        .stream().anyMatch(LINE_ITEM_WITH_BOUNDARY_TAGS);
    }

    public BoundaryIntersectionCheck_old(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        boolean qualifying =  object instanceof Relation && (isRelationTypeBoundaryWithBoundaryTag(object)
                || isRelationWithAnyLineItemWithBoundaryTags(object));
        if(qualifying){
            System.out.println("OSM Identifier passed to Boundary Check: " + object.getOsmIdentifier());
        }
        if(object.getOsmIdentifier() == 2332402){
            System.out.println("ID 2332402 found it is of type " + object.getClass());
        }
        if(object.getOsmIdentifier() == 2212273){
            System.out.println("ID 2212273 found it is of type " + object.getClass());
        }
        return qualifying;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Relation relation = (Relation) object;
        final MultiRelation multiRelation = (MultiRelation) object;
        final RelationMemberList relationMemberLineItems = relation.membersOfType(ItemType.EDGE);
        relationMemberLineItems.addAll(relation.membersOfType(ItemType.LINE));
        final Set<LineItem> lineItems = BoundaryIntersectionCheck_old
                .getLineItems(relationMemberLineItems);
        //TODO rename and clean up
        Set<LineItem> lineItemsExpanded = new HashSet<>();
        //                   if(lineItem instanceof MultiEdge){
        //                        return ((MultiEdge) lineItem)getSubEdgeList().iterator();
        //                   }
        Set<Iterator<? extends LineItem>> iterators = lineItems
                .stream()
                .map(lineItem -> {
                    if (lineItem instanceof MultiLine) {
                        return ((MultiLine) lineItem).getSubLines().iterator();
                    }
//                   if(lineItem instanceof MultiEdge){
//                        return ((MultiEdge) lineItem)getSubEdgeList().iterator();
//                   }
                    return Arrays.asList(lineItem).iterator();
                })
                .collect(Collectors.toSet());
        iterators.forEach(iterator -> iterator.forEachRemaining(record -> lineItemsExpanded.add(record)));
        final CheckFlag checkFlag = this.prepareCheckFlag(object, relation, lineItemsExpanded);
        if (checkFlag.getFlaggedObjects().isEmpty())
        {
            return Optional.empty();
        }
        return Optional.of(checkFlag);
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private void addInformationToFlag(final CheckFlag checkFlag, final Relation relation,
            final LineItem lineItem, final Relation intersectingBoundary,
            final Map<Long, Set<LineItem>> lineItems)
    {
        lineItems.keySet().forEach(osmId ->
        {
            final Set<LineItem> currentLineItems = lineItems.get(osmId);
            if (this.isSameBoundaryType(relation, intersectingBoundary)
                    || this.isSameBoundaryType(lineItem, currentLineItems))
            {
                final Set<Location> intersectingPoints = this.getIntersectingPoints(lineItem,
                        currentLineItems);
                checkFlag.addPoints(intersectingPoints);
                this.addLineItems(checkFlag, currentLineItems);
                checkFlag.addObject(intersectingBoundary);
                final String instruction = this.getLocalizedInstruction(INDEX,
                        Long.toString(relation.getOsmIdentifier()),
                        Long.toString(lineItem.getOsmIdentifier()), Long.toString(osmId),
                        Long.toString(intersectingBoundary.getOsmIdentifier()),
                        this.locationsToList(intersectingPoints));
                checkFlag.addInstruction(instruction);
            }
        });
        if (!checkFlag.getInstructions().isEmpty())
        {
            this.addLineItem(checkFlag, lineItem);
            checkFlag.addObject(relation);
        }
    }

    private void addLineItem(final CheckFlag checkFlag, final LineItem lineItem)
    {
        if (lineItem instanceof Edge)
        {
            checkFlag.addObjects(new OsmWayWalker((Edge) lineItem).collectEdges());
        }
        else
        {
            checkFlag.addObject(lineItem);
        }
    }

    private void addLineItems(final CheckFlag checkFlag, final Set<LineItem> lineItems)
    {
        lineItems.forEach(lineItem -> this.addLineItem(checkFlag, lineItem));
    }

    private void analyzeIntersections(final Atlas atlas, final Relation relation,
            final CheckFlag checkFlag, final Set<LineItem> lineItems, final LineItem lineItem)
    {
        final RelationIntersections relationIntersections = new RelationIntersections();
        lineItems.stream()
                .map(currentLineItem -> atlas.lineItemsIntersecting(currentLineItem.bounds(),
                        this.getPredicateForLineItemsSelection(lineItems, lineItem)))
                .flatMap(iterable -> StreamSupport.stream(iterable.spliterator(), false))
                .forEach(currentLineItem ->
                {
                    final Set<Relation> boundaries = this.getBoundary(currentLineItem);
                    boundaries.forEach(boundary -> relationIntersections.addIntersection(boundary,
                            currentLineItem));
                });
        relationIntersections.getRelations().forEach(currentRelation ->
        {
            if (currentRelation.getOsmIdentifier() > relation.getOsmIdentifier())
            {
                this.addInformationToFlag(checkFlag, relation, lineItem, currentRelation,
                        relationIntersections.getLineItemMap(currentRelation));
            }
        });
    }

    private Set<Relation> getBoundary(final LineItem currentLineItem)
    {
        return currentLineItem.relations().stream()
                .filter(relation -> BoundaryIntersectionCheck_old.isRelationTypeBoundaryWithBoundaryTag(
                        relation) || LINE_ITEM_WITH_BOUNDARY_TAGS.test(currentLineItem))
                .collect(Collectors.toSet());
    }

    private Set<Location> getIntersectingPoints(final LineItem lineItem,
            final Set<LineItem> currentLineItems)
    {
        return currentLineItems.stream()
                .map(currentLineItem -> currentLineItem.asPolyLine()
                        .intersections(lineItem.asPolyLine()))
                .flatMap(Collection::stream).collect(Collectors.toSet());
    }

    private Predicate<LineItem> getPredicateForLineItemsSelection(final Set<LineItem> lineItems,
            final LineItem currentLineItem)
    {
        return lineItemToCheck ->
        {
            if (LINE_ITEM_AS_BOUNDARY.test(lineItemToCheck) && !lineItems.contains(lineItemToCheck))
            {
                return this.isCrossingNotTouching(currentLineItem, lineItemToCheck);
            }
            return false;
        };
    }

    private boolean isCrossingNotTouching(final LineItem currentLineItem,
            final LineItem lineItemToCheck)
    {
        final WKTReader wktReader = new WKTReader();
        try
        {
            final Geometry line1 = wktReader.read(lineItemToCheck.asPolyLine().toWkt());
            final Geometry line2 = wktReader.read(currentLineItem.asPolyLine().toWkt());
            return line1.crosses(line2) && !line1.touches(line2);
        }
        catch (final ParseException e)
        {
            return false;
        }
    }

    private boolean isSameBoundaryType(final Relation relation, final Relation intersectingBoundary)
    {
        return intersectingBoundary.getTag(BOUNDARY).equals(relation.getTag(BOUNDARY));
    }

    private boolean isSameBoundaryType(final LineItem lineItem,
            final Set<LineItem> currentLineItems)
    {
        final LineItem intersectingLineItem = currentLineItems.iterator().next();
        return !currentLineItems.isEmpty() && LINE_ITEM_WITH_BOUNDARY_TAGS.test(lineItem)
                && LINE_ITEM_WITH_BOUNDARY_TAGS.test(intersectingLineItem)
                && lineItem.getTag(BOUNDARY).equals(intersectingLineItem.getTag(BOUNDARY));
    }

    private String locationsToList(final Set<Location> locations)
    {
        return locations.stream().map(location -> String.format("(%s, %s)", location.getLatitude(),
                location.getLongitude())).collect(Collectors.joining(DELIMITER));
    }

    private CheckFlag prepareCheckFlag(final AtlasObject object, final Relation relation,
            final Set<LineItem> lineItems)
    {
        final CheckFlag checkFlag = new CheckFlag(this.getTaskIdentifier(object));
        lineItems.forEach(lineItem -> this.analyzeIntersections(object.getAtlas(), relation,
                checkFlag, lineItems, lineItem));
        return checkFlag;
    }
}
