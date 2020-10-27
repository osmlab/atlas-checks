package org.openstreetmap.atlas.checks.validation.intersections;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author srachanski
 */
public class BoundaryIntersectionCheck extends BaseCheck<Long>
{

    private static final String INVALID_BOUNDARY_FORMAT = "Boundaries {0} with way {1} is crossing invalidly with boundaries {2} with way {3} at coordinates {4}.";
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
            .anyMatch(relationToCheck -> BoundaryIntersectionCheck
                    .isRelationTypeBoundaryWithBoundaryTag(relationToCheck)
                    || LINE_ITEM_WITH_BOUNDARY_TAGS.test(lineItem));

    private static LineItem castToLineItem(final RelationMember relationMember)
    {
        return (LineItem) relationMember.getEntity();
    }

    private static Set<LineItem> getLineItems(final RelationMemberList relationMembers)
    {
        return relationMembers.stream().map(BoundaryIntersectionCheck::castToLineItem)
                .collect(Collectors.toSet());
    }
    
    private static boolean isObjectOfBoundaryTypeWithBoundaryTag(final AtlasObject object)
    {
        return Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.BOUNDARY)
                && Validators.hasValuesFor(object, BoundaryTag.class);
    }

    private static boolean isRelationWithAnyLineItemWithBoundaryTags(final AtlasObject object)
    {
        final Relation relation = (Relation) object;
        return BoundaryIntersectionCheck.getLineItems(relation.membersOfType(ItemType.EDGE))
                .stream().anyMatch(LINE_ITEM_WITH_BOUNDARY_TAGS)
                || BoundaryIntersectionCheck.getLineItems(relation.membersOfType(ItemType.LINE))
                        .stream().anyMatch(LINE_ITEM_WITH_BOUNDARY_TAGS);
    }

    public BoundaryIntersectionCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return (object instanceof Relation || object instanceof LineItem) &&
                isObjectOfBoundaryTypeWithBoundaryTag(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        if(object instanceof Relation){
            return processRelation(object);
        }
        return processWay(object);
    }
    
    private static boolean isRelationTypeBoundaryWithBoundaryTag(final AtlasObject object)
    {
        return Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.BOUNDARY)
                && Validators.hasValuesFor(object, BoundaryTag.class);
    }
    
    private Optional<CheckFlag> processRelation(AtlasObject object) {
        Map<String, Relation> tagToRelation = new HashMap<>();
        if(object instanceof MultiRelation){
            ((MultiRelation) object).relations()
                    .stream()
                    .filter(BoundaryIntersectionCheck::isObjectOfBoundaryTypeWithBoundaryTag)
                    .forEach(relation -> tagToRelation.put(relation.getTag(BOUNDARY).get(), relation));
        } else {
            tagToRelation.put(object.getTag(BOUNDARY).get(), (Relation) object);
        }
        RelationBoundary relationBoundary = new RelationBoundary(tagToRelation, getLineItems((Relation) object));
        Set<String> instructions = new HashSet<>();
        Set<AtlasObject> objectsToFlag = new HashSet<>();
        Set<String> matchedTags = new HashSet<>();
        for(LineItem currentLineItem : relationBoundary.getLineItems()){
            //getIntersections
            Iterable<LineItem> intersections = object.getAtlas().lineItemsIntersecting(currentLineItem.bounds(),
                    this.getPredicateForLineItemsSelection(relationBoundary.getLineItems(), currentLineItem, relationBoundary.getTagToRelation().keySet()));
            Set<String> currentMatchedTags = new HashSet<>();
            //get boundaries for relation of each intersecting line
            intersections.forEach(lineItem -> {
                Set<Relation> matchingBoundaries = getBoundaries(lineItem)
                        .stream()
                        .filter(boundary -> relationBoundary.getTagToRelation().keySet().contains(boundary.getTag(BOUNDARY).get()))
                        .collect(Collectors.toSet());
                //update globally
                if(!matchingBoundaries.isEmpty()) {
                    currentMatchedTags.addAll(matchingBoundaries
                            .stream()
                            .map(relation -> relation.getTag(BOUNDARY))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toSet()));
                    objectsToFlag.addAll(matchingBoundaries);
                    objectsToFlag.add(currentLineItem);
                    
                    //TODO check for multiline?
                    List<LineItem> lineItems = getLineItems(lineItem);
                    lineItems.forEach(line -> {
                        objectsToFlag.add(line);
                        final Set<Location> intersectingPoints = this.getIntersectingPoints(lineItem,
                                line);
                        String firstBoundaries = objectsToString(relationBoundary.getRelationsByBoundaryTags(currentMatchedTags));
                        String secondBoundaries = objectsToString(matchingBoundaries);
                        if(firstBoundaries.hashCode() < secondBoundaries.hashCode()) {
                            final String instruction = this.getLocalizedInstruction(INDEX,
                                    firstBoundaries,
                                    Long.toString(lineItem.getOsmIdentifier()),
                                    secondBoundaries,
                                    Long.toString(line.getOsmIdentifier()),
                                    this.locationsToList(intersectingPoints));
                            instructions.add(instruction);
                        }
                    });
                }
                matchedTags.addAll(currentMatchedTags);
            });
    
        objectsToFlag.addAll(relationBoundary.getRelationsByBoundaryTags(matchedTags));
            //createFlagRecord
        }
        //update flag with stored results
        if(instructions.isEmpty())
        {
            return Optional.empty();
        } else
            {
            CheckFlag checkFlag = new CheckFlag(this.getTaskIdentifier(object));
            instructions.forEach(checkFlag::addInstruction);
            checkFlag.addObjects(objectsToFlag);
            return Optional.of(checkFlag);
        }
    }
    
    private List<LineItem> getLineItems(LineItem lineItem) {
        if(lineItem instanceof MultiLine)
        {
            List<LineItem> lines = new ArrayList<>();
            ((MultiLine) lineItem).getSubLines()
                    .forEach(lines::add);
            return lines;
        }
        return Arrays.asList(lineItem);
    }
    
    
    private Optional<CheckFlag> processWay(AtlasObject object) {
        //TODO to be implemented
        return Optional.empty();
    }
    
    private Set<LineItem> getLineItems(Relation relation){
        final RelationMemberList relationMemberLineItems = relation.membersOfType(ItemType.EDGE);
        relationMemberLineItems.addAll(relation.membersOfType(ItemType.LINE));
        final Set<LineItem> lineItems = BoundaryIntersectionCheck
                .getLineItems(relationMemberLineItems);
        
        Set<LineItem> lineItemsExpanded = new HashSet<>();
        Set<Iterator<? extends LineItem>> iterators = lineItems
                .stream()
                .map(lineItem -> {
                    if (lineItem instanceof MultiLine) {
                        return ((MultiLine) lineItem).getSubLines().iterator();
                    }
                    return Arrays.asList(lineItem).iterator();
                })
                .collect(Collectors.toSet());
        iterators.forEach(iterator -> iterator.forEachRemaining(lineItemsExpanded::add));
        return lineItemsExpanded;
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

    private Set<Relation> getBoundaries(final LineItem currentLineItem)
    {
        return currentLineItem.relations().stream()
                .filter(relation -> BoundaryIntersectionCheck.isRelationTypeBoundaryWithBoundaryTag(
                        relation) || LINE_ITEM_WITH_BOUNDARY_TAGS.test(currentLineItem))
                .collect(Collectors.toSet());
    }
    
    private Set<Location> getIntersectingPoints(final LineItem lineItem,
                                                final LineItem secondLineItem)
    {
        return lineItem.asPolyLine()
                        .intersections(secondLineItem.asPolyLine());
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
                                                                  final LineItem currentLineItem, Set<String> strings)
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
    
    private String objectsToString(final Set<? extends AtlasObject> objects)
    {
        return objects
                .stream()
                .map(object -> Long.toString(object.getOsmIdentifier()))
                .collect(Collectors.joining(DELIMITER));
    }

}
