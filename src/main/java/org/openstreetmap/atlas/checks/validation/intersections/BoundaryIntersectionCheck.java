package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.openstreetmap.atlas.geography.atlas.Atlas;
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
 * @author srachanski This check analyses whether there are some boundaries with the same tag that
 *         are intersecting.
 */
public class BoundaryIntersectionCheck extends BaseCheck<Long>
{

    private static final String DELIMITER = ", ";
    private static final int INDEX = 0;
    private static final String BOUNDARY = "boundary";
    private static final String INVALID_BOUNDARY_FORMAT = "Boundary {0} with way {1} is crossing invalidly with boundary {2} with way {3} at coordinates {4}.";
    private static final String INSTRUCTION_FORMAT = INVALID_BOUNDARY_FORMAT
            + " Two boundaries should not intersect each other.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INSTRUCTION_FORMAT,
            INVALID_BOUNDARY_FORMAT);

    private static boolean isRelationTypeBoundaryWithBoundaryTag(final AtlasObject object)
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
        return object instanceof Relation && isRelationTypeBoundaryWithBoundaryTag(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Map<String, Relation> tagToRelation = this.getRelationMap(object);
        final RelationBoundary relationBoundary = new RelationBoundary(tagToRelation,
                this.getBoundaryAtlasEntities((Relation) object));
        final Set<String> instructions = new HashSet<>();
        final Set<AtlasObject> objectsToFlag = new HashSet<>();
        final Set<String> matchedTags = new HashSet<>();
        final Atlas atlas = object.getAtlas();
        for (final AtlasEntity atlasEntity : relationBoundary.getAtlasEntities())
        {
            final Iterable<LineItem> lineItemsIntersecting = atlas.lineItemsIntersecting(
                    atlasEntity.bounds(), this.getPredicateForLineItemSelection(atlasEntity,
                            relationBoundary.getTagToRelation().keySet()));
            final Iterable<Area> areasIntersecting = atlas.areasIntersecting(atlasEntity.bounds(),
                    this.getPredicateForAreaSelection(atlasEntity,
                            relationBoundary.getTagToRelation().keySet()));
            final Set<String> currentMatchedTags = new HashSet<>();
            matchedTags.addAll(this.processLineItems(relationBoundary, instructions, objectsToFlag,
                    atlasEntity, lineItemsIntersecting, currentMatchedTags));
            matchedTags.addAll(this.processAreas(relationBoundary, instructions, objectsToFlag,
                    atlasEntity, areasIntersecting, currentMatchedTags));

            objectsToFlag.addAll(relationBoundary.getRelationsByBoundaryTags(matchedTags));
            objectsToFlag.add(atlasEntity);
        }
        if (instructions.isEmpty())
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

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private boolean checkAtlasEntityAsBoundary(final AtlasEntity atlasEntity,
            final Set<String> boundaryTags)
    {
        return atlasEntity.relations().stream()
                .anyMatch(relationToCheck -> BoundaryIntersectionCheck
                        .isRelationTypeBoundaryWithBoundaryTag(relationToCheck)
                        && boundaryTags.contains(relationToCheck.getTag(BOUNDARY).orElse("")))
                || boundaryTags.contains(atlasEntity.getTag(BOUNDARY).orElse(""));
    }

    private String coordinatesToList(final Coordinate[] locations)
    {
        return Stream.of(locations)
                .map(coordinate -> String.format("(%s, %s)", coordinate.getX(), coordinate.getY()))
                .collect(Collectors.joining(DELIMITER));
    }

    private String createInstruction(final AtlasEntity lineItem, final long osmIdentifier,
            final Coordinate[] intersectingPoints, final String firstBoundaries,
            final String secondBoundaries)
    {
        return this.getLocalizedInstruction(INDEX, firstBoundaries,
                Long.toString(lineItem.getOsmIdentifier()), secondBoundaries,
                Long.toString(osmIdentifier), this.coordinatesToList(intersectingPoints));
    }

    private Set<Relation> getBoundaries(final AtlasEntity atlasEntity)
    {
        final Set<Relation> relations = atlasEntity.relations().stream()
                .filter(MultiRelation.class::isInstance).map(AtlasEntity::relations)
                .flatMap(Collection::stream).collect(Collectors.toSet());
        relations.addAll(atlasEntity.relations());
        return relations.stream()
                .filter(BoundaryIntersectionCheck::isRelationTypeBoundaryWithBoundaryTag)
                .collect(Collectors.toSet());
    }

    private Set<AtlasEntity> getBoundaryAtlasEntities(final Relation relation)
    {
        final RelationMemberList relationMemberLineItems = relation.membersOfType(ItemType.EDGE,
                ItemType.LINE, ItemType.AREA);
        return relationMemberLineItems.stream().map(RelationMember::getEntity)
                .collect(Collectors.toSet());
    }

    private Geometry getGeometryForIntersection(final String wktFirst) throws ParseException
    {

        final WKTReader wktReader = new WKTReader();
        Geometry geometry1 = wktReader.read(wktFirst);
        if (geometry1.getGeometryType().equals(Geometry.TYPENAME_POLYGON))
        {
            geometry1 = geometry1.getBoundary();
        }
        return geometry1;
    }

    private Coordinate[] getIntersectionPoints(final String wktFirst, final String wktSecond)
    {
        try
        {
            final Geometry geometry1 = this.getGeometryForIntersection(wktFirst);
            final Geometry geometry2 = this.getGeometryForIntersection(wktSecond);
            return geometry1.intersection(geometry2).getCoordinates();
        }
        catch (final ParseException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private List<LineItem> getLineItems(final LineItem lineItem)
    {
        if (lineItem instanceof MultiLine)
        {
            final List<LineItem> lines = new ArrayList<>();
            ((MultiLine) lineItem).getSubLines().forEach(lines::add);
            return lines;
        }
        return Collections.singletonList(lineItem);
    }

    private Set<Relation> getMatchingBoundaries(final RelationBoundary relationBoundary,
            final AtlasEntity atlasEntity)
    {
        return this.getBoundaries(atlasEntity).stream()
                .filter(boundary -> relationBoundary.getTagToRelation()
                        .containsKey(boundary.getTag(BOUNDARY).orElse(StringUtils.EMPTY)))
                .filter(boundary -> !relationBoundary
                        .containsRelationId(boundary.getOsmIdentifier()))
                .collect(Collectors.toSet());
    }

    private Predicate<Area> getPredicateForAreaSelection(final AtlasEntity atlasEntity,
            final Set<String> boundaryTags)
    {
        return areaToCheck ->
        {
            if (this.checkAtlasEntityAsBoundary(areaToCheck, boundaryTags))
            {
                return this.isCrossingNotTouching(atlasEntity.toWkt(), areaToCheck.toWkt());
            }
            return false;
        };
    }

    private Predicate<LineItem> getPredicateForLineItemSelection(final AtlasEntity atlasEntity,
            final Set<String> boundaryTags)
    {
        return areaToCheck ->
        {
            if (this.checkAtlasEntityAsBoundary(areaToCheck, boundaryTags))
            {
                return this.isCrossingNotTouching(atlasEntity.toWkt(), areaToCheck.toWkt());
            }
            return false;
        };
    }

    private Map<String, Relation> getRelationMap(final AtlasObject object)
    {
        final Map<String, Relation> tagToRelation = new HashMap<>();
        if (object instanceof MultiRelation)
        {
            ((MultiRelation) object).relations().stream()
                    .filter(BoundaryIntersectionCheck::isRelationTypeBoundaryWithBoundaryTag)
                    .forEach(relation -> object.getTag(BOUNDARY)
                            .ifPresent(boundary -> tagToRelation.put(boundary, (Relation) object)));
        }
        object.getTag(BOUNDARY)
                .ifPresent(boundary -> tagToRelation.put(boundary, (Relation) object));
        return tagToRelation;
    }

    private void handleIntersections(final RelationBoundary relationBoundary,
            final Set<String> instructions, final AtlasEntity currentEntity,
            final Set<String> currentMatchedTags, final Set<Relation> matchingBoundaries,
            final String areaWkt, final long osmIdentifier)
    {
        final Coordinate[] intersectingPoints = this.getIntersectionPoints(currentEntity.toWkt(),
                areaWkt);
        final String firstBoundaries = this
                .objectsToString(relationBoundary.getRelationsByBoundaryTags(currentMatchedTags));
        final String secondBoundaries = this.objectsToString(matchingBoundaries);
        if (intersectingPoints.length != 0
                && firstBoundaries.hashCode() < secondBoundaries.hashCode())
        {
            instructions.add(this.createInstruction(currentEntity, osmIdentifier,
                    intersectingPoints, firstBoundaries, secondBoundaries));
        }
    }

    private boolean isAnyGeometryInvalid(final Geometry geometry1, final Geometry geometry2)
    {
        return !geometry1.isValid() || !geometry1.isSimple() || !geometry2.isValid()
                || !geometry2.isSimple();
    }

    /**
     * Checks whether two geometries are crossing and not touching
     *
     * @param wktFirst
     *            first geometry
     * @param wktSecond
     *            second geometry
     * @return boolean
     */
    private boolean isCrossingNotTouching(final String wktFirst, final String wktSecond)
    {
        final WKTReader wktReader = new WKTReader();
        try
        {
            final Geometry geometry1 = wktReader.read(wktFirst);
            final Geometry geometry2 = wktReader.read(wktSecond);
            if (geometry1.equals(geometry2))
            {
                return false;
            }
            if (this.isAnyGeometryInvalid(geometry1, geometry2))
            {
                return false;
            }
            return this.isIntersectingNotTouching(geometry1, geometry2);
        }
        catch (final ParseException e)
        {
            return false;
        }
    }

    private boolean isGeometryPairOfLineType(final Geometry lineString, final Geometry lineString2)
    {
        return lineString.getGeometryType().equals(Geometry.TYPENAME_LINESTRING)
                && lineString2.getGeometryType().equals(Geometry.TYPENAME_LINESTRING);
    }

    private boolean isIntersectingNotTouching(final Geometry geometry1, final Geometry geometry2)
    {
        return geometry1.intersects(geometry2)
                && (geometry1.crosses(geometry2) || (geometry1.overlaps(geometry2)
                        && !this.isGeometryPairOfLineType(geometry1, geometry2)));
    }

    private String objectsToString(final Set<? extends AtlasObject> objects)
    {
        return objects.stream().map(object -> Long.toString(object.getOsmIdentifier()))
                .collect(Collectors.joining(DELIMITER));
    }

    /**
     * Processes areas and finds tags matched with the origin feature tags that can indicate
     * possible intersection of boundaries with the same tag.
     *
     * @param relationBoundary
     *            main boundary wrapper
     * @param instructions
     *            flag instructions
     * @param objectsToFlag
     *            objects that should be flagged
     * @param atlasEntity
     *            entity for which intersections were found
     * @param areasIntersecting
     *            currently processed intersections
     * @param currentMatchedTags
     *            matched tags
     * @return Set of tags
     */
    private Set<String> processAreas(final RelationBoundary relationBoundary,
            final Set<String> instructions, final Set<AtlasObject> objectsToFlag,
            final AtlasEntity atlasEntity, final Iterable<Area> areasIntersecting,
            final Set<String> currentMatchedTags)
    {
        final Set<String> matchedTags = new HashSet<>();
        areasIntersecting.forEach(area ->
        {
            final Set<Relation> matchingBoundaries = this.getMatchingBoundaries(relationBoundary,
                    area);
            if (!matchingBoundaries.isEmpty())
            {
                currentMatchedTags.addAll(matchingBoundaries.stream()
                        .map(relation -> relation.getTag(BOUNDARY)).filter(Optional::isPresent)
                        .map(Optional::get).collect(Collectors.toSet()));
                objectsToFlag.addAll(matchingBoundaries);
                this.handleIntersections(relationBoundary, instructions, atlasEntity,
                        currentMatchedTags, matchingBoundaries, area.toWkt(),
                        area.getOsmIdentifier());
            }
            matchedTags.addAll(currentMatchedTags);
        });
        return matchedTags;
    }

    /**
     * Processes lines and their relations to find tags matched with the origin feature tags that
     * can indicate possible intersection of boundaries with the same tag
     *
     * @param relationBoundary
     *            main boundary wrapper
     * @param instructions
     *            flag instructions
     * @param objectsToFlag
     *            objects that should be flagged
     * @param atlasEntity
     *            entity for which intersections were found
     * @param lineItemsIntersecting
     *            currently processed intersections
     * @param currentMatchedTags
     *            matched tags
     * @return Set of tags
     */
    private Set<String> processLineItems(final RelationBoundary relationBoundary,
            final Set<String> instructions, final Set<AtlasObject> objectsToFlag,
            final AtlasEntity atlasEntity, final Iterable<LineItem> lineItemsIntersecting,
            final Set<String> currentMatchedTags)
    {
        final Set<String> matchedTags = new HashSet<>();
        lineItemsIntersecting.forEach(lineItem ->
        {
            final Set<Relation> matchingBoundaries = this.getMatchingBoundaries(relationBoundary,
                    lineItem);
            if (!matchingBoundaries.isEmpty())
            {
                currentMatchedTags.addAll(matchingBoundaries.stream()
                        .map(relation -> relation.getTag(BOUNDARY)).filter(Optional::isPresent)
                        .map(Optional::get).collect(Collectors.toSet()));
                objectsToFlag.addAll(matchingBoundaries);

                final List<LineItem> lineItems = this.getLineItems(lineItem);
                lineItems.forEach(line ->
                {
                    objectsToFlag.add(line);
                    this.handleIntersections(relationBoundary, instructions, atlasEntity,
                            currentMatchedTags, matchingBoundaries, line.toWkt(),
                            line.getOsmIdentifier());
                });
            }
            matchedTags.addAll(currentMatchedTags);
        });
        return matchedTags;
    }

}
