package org.openstreetmap.atlas.checks.validation.intersections;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.multi.MultiLine;
import org.openstreetmap.atlas.geography.atlas.multi.MultiRelation;
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

//    private static final Predicate<LineItem> LINE_ITEM_WITH_BOUNDARY_TAGS = lineItem -> BOUNDARY
//            .equals(lineItem.getTag(TYPE).orElse(EMPTY)) && lineItem.getTag(BOUNDARY).isPresent();

    private static final Predicate<LineItem> LINE_ITEM_AS_BOUNDARY = lineItem -> lineItem
            .relations().stream()
            .anyMatch(relationToCheck -> BoundaryIntersectionCheck
                    .isRelationTypeBoundaryWithBoundaryTag(relationToCheck));
//                    || LINE_ITEM_WITH_BOUNDARY_TAGS.test(lineItem));

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

    public BoundaryIntersectionCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        System.out.println("Checking id " + object.getOsmIdentifier() + " " + object.getClass());
        return (object instanceof Relation || object instanceof LineItem) &&
                isObjectOfBoundaryTypeWithBoundaryTag(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        System.out.println("Checking for flag id " + object.getOsmIdentifier() + " " + object.getClass());
        if(object instanceof Relation){
            System.out.println("Processing id " + object.getOsmIdentifier() + " " + object.getClass());
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
        Map<String, Relation> tagToRelation = getRelationMap(object);
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
                    
                    List<LineItem> lineItems = getLineItems(lineItem);
                    lineItems.forEach(line -> {
                        objectsToFlag.add(line);
                        final Set<Location> intersectingPoints = this.getIntersectingPoints(currentLineItem,
                                line);
                        String firstBoundaries = objectsToString(relationBoundary.getRelationsByBoundaryTags(currentMatchedTags));
                        String secondBoundaries = objectsToString(matchingBoundaries);
                        if(firstBoundaries.hashCode() < secondBoundaries.hashCode()) {
                            addInstruction(instructions, currentLineItem, line, intersectingPoints, firstBoundaries, secondBoundaries);
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
    
    private void addInstruction(Set<String> instructions, LineItem lineItem, LineItem line, Set<Location> intersectingPoints, String firstBoundaries, String secondBoundaries) {
        final String instruction = this.getLocalizedInstruction(INDEX,
                firstBoundaries,
                Long.toString(lineItem.getOsmIdentifier()),
                secondBoundaries,
                Long.toString(line.getOsmIdentifier()),
                this.locationsToList(intersectingPoints));
        instructions.add(instruction);
    }
    
    private Map<String, Relation> getRelationMap(AtlasObject object) {
        Map<String, Relation> tagToRelation = new HashMap<>();
        if(object instanceof MultiRelation){
            ((MultiRelation) object).relations()
                    .stream()
                    .filter(BoundaryIntersectionCheck::isObjectOfBoundaryTypeWithBoundaryTag)
                    .forEach(relation -> tagToRelation.put(relation.getTag(BOUNDARY).get(), relation));
        }
        tagToRelation.put(object.getTag(BOUNDARY).get(), (Relation) object);
        return tagToRelation;
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

    private Set<Relation> getBoundaries(final LineItem currentLineItem)
    {
        Set<Relation> relations = currentLineItem.relations().stream()
                .filter(relation -> relation instanceof MultiRelation)
                .map(relation -> ((MultiRelation) relation).relations())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        relations.addAll(currentLineItem.relations());
        return relations.stream().filter(BoundaryIntersectionCheck::isRelationTypeBoundaryWithBoundaryTag)
                .collect(Collectors.toSet());
    }
    
    private Set<Location> getIntersectingPoints(final LineItem lineItem,
                                                final LineItem secondLineItem)
    {
        return lineItem.asPolyLine()
                        .intersections(secondLineItem.asPolyLine());
    }

    private Predicate<LineItem> getPredicateForLineItemsSelection(final Set<LineItem> lineItems,
                                                                  final LineItem currentLineItem, Set<String> boundaryTags)
    {
        return lineItemToCheck ->
        {
            if (checkLineItemAsBoundary(lineItemToCheck, boundaryTags) && !lineItems.contains(lineItemToCheck))
            {
                return this.isCrossingNotTouching(currentLineItem, lineItemToCheck);
            }
            return false;
        };
    }
    
    private boolean checkLineItemAsBoundary(LineItem lineItem, Set<String> boundaryTags){
        return lineItem
                .relations().stream()
                .anyMatch(relationToCheck -> BoundaryIntersectionCheck
                        .isRelationTypeBoundaryWithBoundaryTag(relationToCheck)
                        && boundaryTags.contains(relationToCheck.getTag(BOUNDARY).get()));
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
