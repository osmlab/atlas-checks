package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
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

/**
 * @author srachanski
 */
public class BoundaryIntersectionCheck extends BaseCheck<Long>
{

    public static final String DELIMITER = ", ";
    public static final int INDEX = 0;
    public static final String TYPE = "type";
    public static final String BOUNDARY = "boundary";
    private static final String INVALID_BOUNDARY_FORMAT = "Boundaries {0} with way {1} is crossing invalidly with boundaries {2} with way {3} at coordinates {4}.";
    private static final String INSTRUCTION_FORMAT = INVALID_BOUNDARY_FORMAT
            + " Two boundaries should not intersect each other.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INSTRUCTION_FORMAT,
            INVALID_BOUNDARY_FORMAT);


    public BoundaryIntersectionCheck(final Configuration configuration)
    {
        super(configuration);
    }

    private static boolean isObjectOfBoundaryTypeWithBoundaryTag(final AtlasObject object)
    {
        return Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.BOUNDARY)
                && Validators.hasValuesFor(object, BoundaryTag.class);
    }

    private static boolean isRelationTypeBoundaryWithBoundaryTag(final AtlasObject object)
    {
        return Validators.isOfType(object, RelationTypeTag.class, RelationTypeTag.BOUNDARY)
                && Validators.hasValuesFor(object, BoundaryTag.class);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Relation && isObjectOfBoundaryTypeWithBoundaryTag(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        return this.processRelation(object);
    }

    private Optional<CheckFlag> processRelation(final AtlasObject object)
    {
        final Map<String, Relation> tagToRelation = this.getRelationMap(object);
        final RelationBoundary relationBoundary = new RelationBoundary(tagToRelation, this.getBoundaryParts((Relation) object));
        final Set<String> instructions = new HashSet<>();
        final Set<AtlasObject> objectsToFlag = new HashSet<>();
        final Set<String> matchedTags = new HashSet<>();
        for(final BoundaryPart currentBoundaryPart : relationBoundary.getBoundaryParts())
        {
            final Iterable<LineItem> lineItemsIntersecting = object.getAtlas().lineItemsIntersecting(currentBoundaryPart.getBounds(),
                    this.getPredicateForLineItemsSelection(currentBoundaryPart, relationBoundary.getTagToRelation().keySet()));
            final Iterable<Area> areasIntersecting = object.getAtlas().areasIntersecting(currentBoundaryPart.getBounds(),
                    this.getPredicateForAreaSelection(currentBoundaryPart, relationBoundary.getTagToRelation().keySet()));
            final Set<String> currentMatchedTags = new HashSet<>();
            this.processLineItems(relationBoundary, instructions, objectsToFlag, matchedTags, currentBoundaryPart, lineItemsIntersecting, currentMatchedTags);
            this.processAreas(relationBoundary, instructions, objectsToFlag, matchedTags, currentBoundaryPart, areasIntersecting, currentMatchedTags);

            objectsToFlag.addAll(relationBoundary.getRelationsByBoundaryTags(matchedTags));
        }
        if(instructions.isEmpty())
        {
            return Optional.empty();
        }
        else
            {
            final CheckFlag checkFlag = new CheckFlag(this.getTaskIdentifier(object));
            instructions.forEach(checkFlag::addInstruction);
            checkFlag.addObjects(objectsToFlag);
            return Optional.of(checkFlag);
        }
    }

    private void processAreas(final RelationBoundary relationBoundary,
                              final Set<String> instructions,
                              final Set<AtlasObject> objectsToFlag,
                              final Set<String> matchedTags,
                              final BoundaryPart currentBoundaryPart,
                              final Iterable<Area> areasIntersecting,
                              final Set<String> currentMatchedTags)
    {
        areasIntersecting.forEach(area ->
        {
            final Set<Relation> matchingBoundaries = this.getBoundaries(area)
                    .stream()
                    .filter(boundary -> relationBoundary.getTagToRelation().containsKey(boundary.getTag(BOUNDARY).orElse(StringUtils.EMPTY)))
                    .collect(Collectors.toSet());
            if(!matchingBoundaries.isEmpty())
            {
                currentMatchedTags.addAll(matchingBoundaries
                        .stream()
                        .map(relation -> relation.getTag(BOUNDARY))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet()));
                objectsToFlag.addAll(matchingBoundaries);
                    final Coordinate[] intersectingPoints = this.getIntersectionPoints(currentBoundaryPart.getWktGeometry(),
                            area.toWkt());
                    final String firstBoundaries = this.objectsToString(relationBoundary.getRelationsByBoundaryTags(currentMatchedTags));
                    final String secondBoundaries = this.objectsToString(matchingBoundaries);
                    if(firstBoundaries.hashCode() < secondBoundaries.hashCode())
                    {
                        this.addInstruction(instructions, currentBoundaryPart, area, intersectingPoints, firstBoundaries, secondBoundaries);
                    }
            }
            matchedTags.addAll(currentMatchedTags);
        });
    }

    private void processLineItems(final RelationBoundary relationBoundary,
                                  final Set<String> instructions,
                                  final Set<AtlasObject> objectsToFlag,
                                  final Set<String> matchedTags,
                                  final BoundaryPart currentBoundaryPart,
                                  final Iterable<LineItem> lineItemsIntersecting,
                                  final Set<String> currentMatchedTags)
    {
        lineItemsIntersecting.forEach(lineItem ->
        {
            final Set<Relation> matchingBoundaries = this.getBoundaries(lineItem)
                    .stream()
                    .filter(boundary -> relationBoundary.getTagToRelation().containsKey(boundary.getTag(BOUNDARY).get()))
                    .collect(Collectors.toSet());
            if(!matchingBoundaries.isEmpty())
            {
                currentMatchedTags.addAll(matchingBoundaries
                        .stream()
                        .map(relation -> relation.getTag(BOUNDARY))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet()));
                objectsToFlag.addAll(matchingBoundaries);

                final List<LineItem> lineItems = this.getLineItems(lineItem);
                lineItems.forEach(line ->
                {
                    objectsToFlag.add(line);
                    final Coordinate[] intersectingPoints = this.getIntersectionPoints(currentBoundaryPart.getWktGeometry(),
                            line.toWkt());
                    final String firstBoundaries = this.objectsToString(relationBoundary.getRelationsByBoundaryTags(currentMatchedTags));
                    final String secondBoundaries = this.objectsToString(matchingBoundaries);
                    if(firstBoundaries.hashCode() < secondBoundaries.hashCode())
                    {
                        this.addInstruction(instructions, currentBoundaryPart, line, intersectingPoints, firstBoundaries, secondBoundaries);
                    }
                });
            }
            matchedTags.addAll(currentMatchedTags);
        });
    }

    private void addInstruction(final Set<String> instructions,
                                final BoundaryPart lineItem,
                                final LineItem line,
                                final Coordinate[] intersectingPoints,
                                final String firstBoundaries,
                                final String secondBoundaries)
    {
        final String instruction = this.getLocalizedInstruction(INDEX,
                firstBoundaries,
                Long.toString(lineItem.getOsmIdentifier()),
                secondBoundaries,
                Long.toString(line.getOsmIdentifier()),
                this.coordinatesToList(intersectingPoints));
        instructions.add(instruction);
    }

    private void addInstruction(final Set<String> instructions,
                                final BoundaryPart lineItem,
                                final Area area,
                                final Coordinate[] intersectingPoints,
                                final String firstBoundaries,
                                final String secondBoundaries)
    {
        final String instruction = this.getLocalizedInstruction(INDEX,
                firstBoundaries,
                Long.toString(lineItem.getOsmIdentifier()),
                secondBoundaries,
                Long.toString(area.getOsmIdentifier()),
                this.coordinatesToList(intersectingPoints));
        instructions.add(instruction);
    }

    private Map<String, Relation> getRelationMap(final AtlasObject object)
    {
        final Map<String, Relation> tagToRelation = new HashMap<>();
        if(object instanceof MultiRelation)
        {
            ((MultiRelation) object).relations()
                    .stream()
                    .filter(BoundaryIntersectionCheck::isObjectOfBoundaryTypeWithBoundaryTag)
                    .forEach(relation -> tagToRelation.put(relation.getTag(BOUNDARY).get(), relation));
        }
        tagToRelation.put(object.getTag(BOUNDARY).get(), (Relation) object);
        return tagToRelation;
    }

    private List<LineItem> getLineItems(final LineItem lineItem)
    {
        if(lineItem instanceof MultiLine)
        {
            final List<LineItem> lines = new ArrayList<>();
            ((MultiLine) lineItem).getSubLines()
                    .forEach(lines::add);
            return lines;
        }
        return Collections.singletonList(lineItem);
    }

    private Set<BoundaryPart> getBoundaryParts(final Relation relation)
    {
        final RelationMemberList relationMemberLineItems = relation.membersOfType(ItemType.EDGE, ItemType.LINE, ItemType.AREA);
        return relationMemberLineItems
                .stream().map(RelationMember::getEntity)
                .map(entity ->
                {
                    final String tag = entity.getTags().get(BOUNDARY);
                    final Set<String> boundaryTags = entity.relations()
                            .stream()
                            .filter(currentRelation -> BOUNDARY.equals(currentRelation.getTag(TYPE).get()))
                            .map(currentRelation -> currentRelation.getTag(BOUNDARY).orElse(StringUtils.EMPTY))
                            .collect(Collectors.toSet());
                    boundaryTags.add(tag);
                    boundaryTags.remove(StringUtils.EMPTY);
                    return new BoundaryPart(entity.getOsmIdentifier(),
                            entity.bounds(), entity.toWkt(), boundaryTags);
                })
                .collect(Collectors.toSet());
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private Set<Relation> getBoundaries(final LineItem currentLineItem)
    {
        final Set<Relation> relations = currentLineItem.relations().stream()
                .filter(relation -> relation instanceof MultiRelation)
                .map(AtlasEntity::relations)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        relations.addAll(currentLineItem.relations());
        return relations.stream().filter(BoundaryIntersectionCheck::isRelationTypeBoundaryWithBoundaryTag)
                .collect(Collectors.toSet());
    }

    private Set<Relation> getBoundaries(final Area area)
    {
        final Set<Relation> relations = area.relations().stream()
                .filter(relation -> relation instanceof MultiRelation)
                .map(AtlasEntity::relations)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        relations.addAll(area.relations());
        return relations.stream().filter(BoundaryIntersectionCheck::isRelationTypeBoundaryWithBoundaryTag)
                .collect(Collectors.toSet());
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
            throw new IllegalStateException(e);
        }
    }

    private Predicate<LineItem> getPredicateForLineItemsSelection(final BoundaryPart boundaryPart, final Set<String> boundaryTags)
    {
        return lineItemToCheck ->
        {
            if (this.checkLineItemAsBoundary(lineItemToCheck, boundaryTags))
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
            if (this.checkAreaAsBoundary(areaToCheck, boundaryTags))
            {
                return this.isCrossingNotTouching(boundaryPart.getWktGeometry(), areaToCheck.toWkt());
            }
            return false;
        };
    }

    private boolean checkLineItemAsBoundary(final LineItem lineItem, final Set<String> boundaryTags)
    {
        return lineItem
                .relations().stream()
                .anyMatch(relationToCheck -> BoundaryIntersectionCheck
                        .isRelationTypeBoundaryWithBoundaryTag(relationToCheck)
                        && boundaryTags.contains(relationToCheck.getTag(BOUNDARY).get()))
                || boundaryTags.contains(lineItem.getTag(BOUNDARY).orElse(""));
    }

    private boolean checkAreaAsBoundary(final Area area, final Set<String> boundaryTags)
    {
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
            final Geometry geometry1 = wktReader.read(wktFirst);
            final Geometry geometry2 = wktReader.read(wktSecond);
            return geometry1.intersects(geometry2) && !(geometry1.within(geometry2) || geometry1.contains(geometry2));
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
