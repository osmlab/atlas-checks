package org.openstreetmap.atlas.checks.validation.intersections;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Area;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return relationMembers.stream()
                .map(BoundaryIntersectionCheck::castToLineItem)
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
        return (object instanceof Relation || object instanceof LineItem) &&
                isObjectOfBoundaryTypeWithBoundaryTag(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        if(object instanceof Relation){
            return processRelation(object);
        }
        //process way -> line, edge, area
        return processWay(object);
    }
    
    private static boolean isRelationTypeBoundaryWithBoundaryTag(final AtlasObject object)
    {
        return Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.BOUNDARY)
                && Validators.hasValuesFor(object, BoundaryTag.class);
    }
    
    private Optional<CheckFlag> processRelation(AtlasObject object) {
        if(2021031 == object.getOsmIdentifier()){
            System.out.println(2021031 + " found");
        }
        if(2332348  == object.getOsmIdentifier()){
            System.out.println(2332348  + " found");
        }
        if(2022068  == object.getOsmIdentifier()){
            System.out.println(2022068  + " found");
        }
        if(2075811  == object.getOsmIdentifier()){
            System.out.println(2075811  + " found");
        }
        if(2212273  == object.getOsmIdentifier()){
            System.out.println(2212273 + " found");
        }
        if(2332402  == object.getOsmIdentifier()){
            System.out.println(2332402 + " found");
        }
        Map<String, Relation> tagToRelation = getRelationMap(object);
        RelationBoundary relationBoundary = new RelationBoundary(tagToRelation, getBoundaryParts((Relation) object));
        Set<String> instructions = new HashSet<>();
        Set<AtlasObject> objectsToFlag = new HashSet<>();
        Set<String> matchedTags = new HashSet<>();
        for(BoundaryPart currentBoundaryPart : relationBoundary.getBoundaryParts()){
            //getIntersections
                //getLineItems
                //getAreas
            Iterable<LineItem> lineItemsIntersecting = object.getAtlas().lineItemsIntersecting(currentBoundaryPart.getBounds(),
                    this.getPredicateForLineItemsSelection(currentBoundaryPart, relationBoundary.getTagToRelation().keySet()));
            Iterable<Area> areasIntersecting = object.getAtlas().areasIntersecting(currentBoundaryPart.getBounds(),
                    this.getPredicateForAreaSelection(currentBoundaryPart, relationBoundary.getTagToRelation().keySet()));
            Set<String> currentMatchedTags = new HashSet<>();
            //get boundaries for relation of each intersecting line
            processLineItems(relationBoundary, instructions, objectsToFlag, matchedTags, currentBoundaryPart, lineItemsIntersecting, currentMatchedTags);
            processAreas(relationBoundary, instructions, objectsToFlag, matchedTags, currentBoundaryPart, areasIntersecting, currentMatchedTags);
                    
                    //TODO ?
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
            System.out.println(checkFlag.getInstructions());
            return Optional.of(checkFlag);
        }
    }
    
    private void processAreas(RelationBoundary relationBoundary, Set<String> instructions, Set<AtlasObject> objectsToFlag, Set<String> matchedTags, BoundaryPart currentBoundaryPart, Iterable<Area> areasIntersecting, Set<String> currentMatchedTags) {
        areasIntersecting.forEach(area -> {
            //TODO
            Set<Relation> matchingBoundaries = getBoundaries(area)
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
//                    objectsToFlag.add(currentLineItem);

//                    objectsToFlag.add(area);
                    final Coordinate[] intersectingPoints = this.getIntersectionPoints(currentBoundaryPart.getWktGeometry(),
                            area.toWkt());
                    String firstBoundaries = objectsToString(relationBoundary.getRelationsByBoundaryTags(currentMatchedTags));
                    String secondBoundaries = objectsToString(matchingBoundaries);
                    if(firstBoundaries.hashCode() < secondBoundaries.hashCode()) {
                        addInstruction(instructions, currentBoundaryPart, area, intersectingPoints, firstBoundaries, secondBoundaries);
                    }
        
            }
            matchedTags.addAll(currentMatchedTags);
        });
    }
    
    private void processLineItems(RelationBoundary relationBoundary, Set<String> instructions, Set<AtlasObject> objectsToFlag, Set<String> matchedTags, BoundaryPart currentBoundaryPart, Iterable<LineItem> lineItemsIntersecting, Set<String> currentMatchedTags) {
        lineItemsIntersecting.forEach(lineItem -> {
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
//                    objectsToFlag.add(currentLineItem);
                
                List<LineItem> lineItems = getLineItems(lineItem);
                lineItems.forEach(line -> {
                    objectsToFlag.add(line);
                    final Coordinate[] intersectingPoints = this.getIntersectionPoints(currentBoundaryPart.getWktGeometry(),
                            line.toWkt());
                    String firstBoundaries = objectsToString(relationBoundary.getRelationsByBoundaryTags(currentMatchedTags));
                    String secondBoundaries = objectsToString(matchingBoundaries);
                    if(firstBoundaries.hashCode() < secondBoundaries.hashCode()) {
                        addInstruction(instructions, currentBoundaryPart, line, intersectingPoints, firstBoundaries, secondBoundaries);
                    }
                });
            }
            matchedTags.addAll(currentMatchedTags);
        });
    }
    
    private void addInstruction(Set<String> instructions, BoundaryPart lineItem, LineItem line, Coordinate[] intersectingPoints, String firstBoundaries, String secondBoundaries) {
        final String instruction = this.getLocalizedInstruction(INDEX,
                firstBoundaries,
                Long.toString(lineItem.getOsmIdentifier()),
                secondBoundaries,
                Long.toString(line.getOsmIdentifier()),
                this.coordinatesToList(intersectingPoints));
        instructions.add(instruction);
    }
    
    private void addInstruction(Set<String> instructions, BoundaryPart lineItem, Area area, Coordinate[] intersectingPoints, String firstBoundaries, String secondBoundaries) {
        final String instruction = this.getLocalizedInstruction(INDEX,
                firstBoundaries,
                Long.toString(lineItem.getOsmIdentifier()),
                secondBoundaries,
                Long.toString(area.getOsmIdentifier()),
                this.coordinatesToList(intersectingPoints));
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
    
    private Set<BoundaryPart> getBoundaryParts(Relation relation){
        //TODO??!!
        //        relation.toWkt()
        final RelationMemberList relationMemberLineItems = relation.membersOfType(ItemType.EDGE, ItemType.LINE, ItemType.AREA);
        return relationMemberLineItems
                .stream().map(RelationMember::getEntity)
                .map(entity -> {
                    String tag = entity.getTags().get(BOUNDARY);
                    Set<String> boundaryTags = entity.relations()
                            .stream()
                            .filter(currentRelation -> BOUNDARY.equals(currentRelation.getTag(TYPE)))
                            .map(currentRelation -> currentRelation.getTag(BOUNDARY).orElse(""))
                            .collect(Collectors.toSet());
                    boundaryTags.add(tag);
                    boundaryTags.remove("");
                    return new BoundaryPart(entity.getOsmIdentifier(),
                            entity.bounds(), entity.toWkt(), boundaryTags);
                })
                .collect(Collectors.toSet());
        //        final Set<LineItem> lineItems = BoundaryIntersectionCheck
//                .getLineItems(relationMemberLineItems);
        
//        if(relation instanceof MultiRelation && lineItems.isEmpty()){
//            lineItems.addAll(getLineItemsFromMultiRelation((MultiRelation) relation));
//        }
        
//        Set<LineItem> lineItemsExpanded = new HashSet<>();
//        Set<Iterator<? extends LineItem>> iterators = lineItems
//                .stream()
//                .map(lineItem -> {
//                    if (lineItem instanceof MultiLine) {
//                        return ((MultiLine) lineItem).getSubLines().iterator();
//                    }
//                    return Arrays.asList(lineItem).iterator();
//                })
//                .collect(Collectors.toSet());
//        iterators.forEach(iterator -> iterator.forEachRemaining(lineItemsExpanded::add));
//        return lineItemsExpanded;
//        return lineItems;
    }
    
    private Collection<? extends LineItem> getLineItemsFromMultiRelation(MultiRelation relation) {
        return relation.relations().stream()
                .flatMap(subRelation -> BoundaryIntersectionCheck.getLineItems(subRelation.membersOfType(ItemType.LINE, ItemType.EDGE)).stream())
                .collect(Collectors.toSet());
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
    
    private Set<Relation> getBoundaries(final Area area)
    {
        Set<Relation> relations = area.relations().stream()
                .filter(relation -> relation instanceof MultiRelation)
                .map(relation -> ((MultiRelation) relation).relations())
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        relations.addAll(area.relations());
        return relations.stream().filter(BoundaryIntersectionCheck::isRelationTypeBoundaryWithBoundaryTag)
                .collect(Collectors.toSet());
    }
    
    private Set<Location> getIntersectingPoints(final LineItem lineItem,
                                                final LineItem secondLineItem)
    {
        return lineItem.asPolyLine()
                        .intersections(secondLineItem.asPolyLine());
    }
    
    private Coordinate[] getIntersectionPoints(final String wktFirst,
                                               final String wktSecond)
    {
        final WKTReader wktReader = new WKTReader();
        try
        {
            final Geometry line1 = wktReader.read(wktFirst);
            final Geometry line2 = wktReader.read(wktSecond);
            return line1.intersection(line2).getCoordinates();
        }
        catch (final ParseException e)
        {
            //TODO ?
//            return false;
            throw new IllegalStateException(e);
        }
    }

    //TODO old
//    private Predicate<LineItem> getPredicateForLineItemsSelection_old(final Set<LineItem> lineItems,
//                                                                  final LineItem currentLineItem, Set<String> boundaryTags)
//    {
//        return lineItemToCheck ->
//        {
//            if (checkLineItemAsBoundary(lineItemToCheck, boundaryTags) && !lineItems.contains(lineItemToCheck))
//            {
//                return this.isCrossingNotTouching(currentLineItem, lineItemToCheck);
//            }
//            return false;
//        };
//    }
    
    private Predicate<LineItem> getPredicateForLineItemsSelection(final BoundaryPart boundaryPart, final Set<String> boundaryTags)
    {
        return lineItemToCheck ->
        {
            if (checkLineItemAsBoundary(lineItemToCheck, boundaryTags))
            {
                return this.isCrossingNotTouching(boundaryPart.getWktGeometry(), lineItemToCheck.toWkt());
            }
            return false;
        };
    }
    
    private Predicate<Area> getPredicateForAreaSelection(final BoundaryPart boundaryPart, final Set<String> boundaryTags)
    {
        return areaToCheck ->
        {
            if (checkAreaAsBoundary(areaToCheck, boundaryTags))
            {
                return this.isCrossingNotTouching(boundaryPart.getWktGeometry(), areaToCheck.toWkt());
            }
            return false;
        };
    }
    
    private boolean checkLineItemAsBoundary(LineItem lineItem, Set<String> boundaryTags){
        return lineItem
                .relations().stream()
                .anyMatch(relationToCheck -> BoundaryIntersectionCheck
                        .isRelationTypeBoundaryWithBoundaryTag(relationToCheck)
                        && boundaryTags.contains(relationToCheck.getTag(BOUNDARY).get()))
                || boundaryTags.contains(lineItem.getTag(BOUNDARY).orElse(""));
    }
    
    private boolean checkAreaAsBoundary(Area area, Set<String> boundaryTags){
        return area
                .relations().stream()
                .anyMatch(relationToCheck -> BoundaryIntersectionCheck
                        .isRelationTypeBoundaryWithBoundaryTag(relationToCheck)
                        && boundaryTags.contains(relationToCheck.getTag(BOUNDARY).get()))
                || boundaryTags.contains(area.getTag(BOUNDARY).orElse(""));
    }

    private boolean isCrossingNotTouching(final String wktFirst,
            final String wktSecond)
    {
        final WKTReader wktReader = new WKTReader();
        try
        {
            final Geometry line1 = wktReader.read(wktFirst);
            final Geometry line2 = wktReader.read(wktSecond);
            return line1.intersects(line2) && !line1.touches(line2);
        }
        catch (final ParseException e)
        {
            return false;
        }
    }

    private String coordinatesToList(final Coordinate[] locations)
    {
        return Stream.of(locations)
                .map(coordinate -> String.format("(%s, %s)", coordinate.getX(), coordinate.getY()))
                .collect(Collectors.joining(DELIMITER));
    }
    
    private String objectsToString(final Set<? extends AtlasObject> objects)
    {
        return objects
                .stream()
                .map(object -> Long.toString(object.getOsmIdentifier()))
                .collect(Collectors.joining(DELIMITER));
    }

}
