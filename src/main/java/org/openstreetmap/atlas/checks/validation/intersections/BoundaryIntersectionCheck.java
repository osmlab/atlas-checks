package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
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
import org.openstreetmap.atlas.tags.BoundaryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check analyses whether there are some boundaries with the same tag that are intersecting.
 *
 * @author srachanski
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

    /**
     * Checks if a relation is of a boundary type and has boundary tag
     *
     * @param object
     *            AtlasObject
     * @return returns true if a relation is of a boundary type and has boundary tag
     */
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

    /**
     * Creates String containing coordinate information
     *
     * @param locations
     *            array with coordinates
     * @return String containing coordinate information
     */
    private String coordinatesToList(final Coordinate[] locations)
    {
        return Stream.of(locations)
                .map(coordinate -> String.format("(%s, %s)", coordinate.getX(), coordinate.getY()))
                .collect(Collectors.joining(DELIMITER));
    }

    /**
     * Creates instruction with information about check violation
     *
     * @param atlasEntity
     *            base entity for instruction
     * @param osmIdentifier
     *            id of other boundary
     * @param intersectingPoints
     *            points of intersection
     * @param firstBoundaries
     *            first way ids
     * @param secondBoundaries
     *            second way ids
     * @return returns instruction with information about check violation
     */
    private String createInstruction(final AtlasEntity atlasEntity, final long osmIdentifier,
            final Coordinate[] intersectingPoints, final String firstBoundaries,
            final String secondBoundaries)
    {
        return this.getLocalizedInstruction(INDEX, firstBoundaries,
                Long.toString(atlasEntity.getOsmIdentifier()), secondBoundaries,
                Long.toString(osmIdentifier), this.coordinatesToList(intersectingPoints));
    }

    /**
     * Extracts all entity ids to a String
     *
     * @param entities
     *            atlas entities
     * @return String with ids
     */
    private String entityIdsToString(final Set<? extends AtlasEntity> entities)
    {
        return entities.stream().map(entity -> Long.toString(entity.getOsmIdentifier()))
                .collect(Collectors.joining(DELIMITER));
    }

    /**
     * Extracts all boundary entities (edges, lines and areas)
     *
     * @param relation
     *            parent relation to extract entities from
     * @return boundary entities (edges, lines and areas)
     */
    private Set<AtlasEntity> getBoundaryAtlasEntities(final Relation relation)
    {
        final RelationMemberList relationMemberLineItems = relation.membersOfType(ItemType.EDGE,
                ItemType.LINE, ItemType.AREA);
        return relationMemberLineItems.stream().map(RelationMember::getEntity)
                .collect(Collectors.toSet());
    }

    /**
     * Transforms wkt geometry to Geometry object, for polygons it takes their boundaries
     *
     * @param wkt
     *            geometry in form of wkt
     * @return geometry object
     * @throws ParseException
     *             when parsing failes
     */
    private Geometry getGeometryForIntersection(final String wkt) throws ParseException
    {

        final WKTReader wktReader = new WKTReader();
        Geometry geometry1 = wktReader.read(wkt);
        if (geometry1.getGeometryType().equals(Geometry.TYPENAME_POLYGON))
        {
            geometry1 = geometry1.getBoundary();
        }
        return geometry1;
    }

    /**
     * Finds intersection points of two geometries given in a form of wkt
     *
     * @param wktFirst
     *            first wkt geometry
     * @param wktSecond
     *            second wkt geometry
     * @return array of intersection coordinates
     */
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

    /**
     * Find relations matching relationBoundary boundary tags and not present in it.
     *
     * @param relationBoundary
     *            origin object to check against
     * @param atlasEntity
     *            entity which relations should be checked
     * @return set of relations
     */
    private Set<Relation> getMatchingBoundaries(final RelationBoundary relationBoundary,
            final AtlasEntity atlasEntity)
    {
        return atlasEntity.relations().stream()
                .filter(boundary -> relationBoundary.getTagToRelation()
                        .containsKey(boundary.getTag(BOUNDARY).orElse(StringUtils.EMPTY)))
                .filter(boundary -> !relationBoundary
                        .containsRelationId(boundary.getOsmIdentifier()))
                .collect(Collectors.toSet());
    }

    /**
     * Predicate for checking if Area is a proper boundary and if is crossing not touching given
     * AtlasEntity
     *
     * @param atlasEntity
     *            entity to check against
     * @param boundaryTags
     *            expected boundary tags
     * @return true if Area is a proper boundary and if is crossing not touching given entity
     */
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

    /**
     * Predicate for checking if LineItem is proper boundary and if is crossing not touching given
     * AtlasEntity
     *
     * @param atlasEntity
     *            entity to check against
     * @param boundaryTags
     *            expected boundary tags
     * @return true if LineItem is a proper boundary and if is crossing not touching given entity
     */
    private Predicate<LineItem> getPredicateForLineItemSelection(final AtlasEntity atlasEntity,
            final Set<String> boundaryTags)
    {
        return lineToCheck ->
        {
            if (this.checkAtlasEntityAsBoundary(lineToCheck, boundaryTags))
            {
                return this.isCrossingNotTouching(atlasEntity.toWkt(), lineToCheck.toWkt());
            }
            return false;
        };
    }

    /**
     * Creates map of boundary tag name to a relation
     *
     * @param atlasObject
     *            boundary object
     * @return map of boundary tag name to a relation
     */
    private Map<String, Relation> getRelationMap(final AtlasObject atlasObject)
    {
        final Map<String, Relation> tagToRelation = new HashMap<>();
        atlasObject.getTag(BOUNDARY)
                .ifPresent(boundary -> tagToRelation.put(boundary, (Relation) atlasObject));
        return tagToRelation;
    }

    /**
     * Checks if there are intersections to be add to instructions
     *
     * @param relationBoundary
     *            relationBoundary that is processed
     * @param instructions
     *            instruction for flag
     * @param currentEntity
     *            entity to check intersections against
     * @param currentMatchedTags
     *            boundary tags that are both in entity and relationBoundary
     * @param matchingBoundaries
     *            relations that are intersecting
     * @param areaWkt
     *            wkt geometry of area
     * @param osmIdentifier
     *            osm id of area
     */
    private void handleIntersections(final RelationBoundary relationBoundary,
            final Set<String> instructions, final AtlasEntity currentEntity,
            final Set<String> currentMatchedTags, final Set<Relation> matchingBoundaries,
            final String areaWkt, final long osmIdentifier)
    {
        final Coordinate[] intersectingPoints = this.getIntersectionPoints(currentEntity.toWkt(),
                areaWkt);
        final String firstBoundaries = this
                .entityIdsToString(relationBoundary.getRelationsByBoundaryTags(currentMatchedTags));
        final String secondBoundaries = this.entityIdsToString(matchingBoundaries);
        if (intersectingPoints.length != 0
                && firstBoundaries.hashCode() < secondBoundaries.hashCode())
        {
            instructions.add(this.createInstruction(currentEntity, osmIdentifier,
                    intersectingPoints, firstBoundaries, secondBoundaries));
        }
    }

    /**
     * Validate geometries (check if they are both valid and simple)
     *
     * @param geometry1
     *            first geometry
     * @param geometry2
     *            second geometry
     * @return boolean stating if both geometries are valid and not simple
     */
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
     * @return boolean if geometries are crossing not touching
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

    /**
     * Checks whether both geometries are LineStrings.
     *
     * @param geometry1
     *            first geometry
     * @param geometry2
     *            second geometry
     * @return boolean if geometries are both of type LineString
     */
    private boolean isGeometryPairOfLineType(final Geometry geometry1, final Geometry geometry2)
    {
        return geometry1.getGeometryType().equals(Geometry.TYPENAME_LINESTRING)
                && geometry2.getGeometryType().equals(Geometry.TYPENAME_LINESTRING);
    }

    /**
     * Checks if two geometries are intersecting one other but are not touching
     *
     * @param geometry1
     *            first geometry
     * @param geometry2
     *            second geometry
     * @return boolean if geometries are intersecting not touching
     */
    private boolean isIntersectingNotTouching(final Geometry geometry1, final Geometry geometry2)
    {
        return geometry1.intersects(geometry2)
                && (geometry1.crosses(geometry2) || (geometry1.overlaps(geometry2)
                        && !this.isGeometryPairOfLineType(geometry1, geometry2)));
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
                objectsToFlag.add(lineItem);
                this.handleIntersections(relationBoundary, instructions, atlasEntity,
                        currentMatchedTags, matchingBoundaries, lineItem.toWkt(),
                        lineItem.getOsmIdentifier());
            }
            matchedTags.addAll(currentMatchedTags);
        });
        return matchedTags;
    }

}
