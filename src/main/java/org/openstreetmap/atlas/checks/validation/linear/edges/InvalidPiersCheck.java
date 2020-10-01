package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.AmenityTag;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.LevelTag;
import org.openstreetmap.atlas.tags.ManMadeTag;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check identifies piers (OSM ways with man_made = Pier tag) that have either a linear
 * geometry and no area = Yes tag, or a polygonal geometry and no area=Yes tag. A pier must also
 * have (i) a highway Tag or (ii) a highway overlapping the pier at the same level/layer as the pier
 * or (iii) be connected to a ferry route/amenity = Ferry_Terminal or (iv) be connected to a
 * building at the same level/layer as the pier. A polygonal pier with a building tag or
 * amenity=ferry_terminal is also valid for the check.
 *
 * @author sayas01
 */
public class InvalidPiersCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 6011101860745289836L;
    private static final String LINEAR_GEOMETRY_WITH_HIGHWAY_TAG = "This way {0,number,#} is a linear pier with \"man_made=pier\"tag and a highway tag. "
            + "Please make necessary changes to convert its geometry to a polygon and add tag, \"area=yes\".";
    private static final String POLYGONAL_GEOMETRY_WITH_BUILDING_AND_HIGHWAY_TAG = "This way {0,number,#} is a ferry terminal or a building or a highway with \"man_made=pier\" tag and has a polygonal geometry, "
            + "but is missing \"area=yes\" tag. Please add tag \"area=yes\" to the pier.";
    private static final String LINEAR_GEOMETRY_WITH_OVERLAPPING_HIGHWAY_AND_BUILDING = "This way {0,number,#} is a linear pier with \"man_made=pier\" tag and has either overlapping highways or buildings or is connected to buildings or ferry routes or both. "
            + "Please make necessary changes to convert its geometry to a polygon and add tag, \"area=yes\".";
    private static final String POLYGONAL_GEOMETRY_WITH_OVERLAPPING_HIGHWAY_AND_BUILDING = "This way {0,number,#} is a polygonal pier with \"man_made=pier\" tag and has either overlapping highways or buildings or is connected to buildings or ferry routes or both, "
            + "but is missing \"area=yes\" tag. Please add tag \"area=yes\" to the pier.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            LINEAR_GEOMETRY_WITH_HIGHWAY_TAG, POLYGONAL_GEOMETRY_WITH_BUILDING_AND_HIGHWAY_TAG,
            LINEAR_GEOMETRY_WITH_OVERLAPPING_HIGHWAY_AND_BUILDING,
            POLYGONAL_GEOMETRY_WITH_OVERLAPPING_HIGHWAY_AND_BUILDING);
    private static final Predicate<AtlasObject> HAS_NO_AREA_TAG = atlasObject -> !Validators
            .isOfType(atlasObject, AreaTag.class, AreaTag.YES);
    private static final Predicate<AtlasObject> IS_FERRY_TERMINAL = atlasObject -> Validators
            .isOfType(atlasObject, AmenityTag.class, AmenityTag.FERRY_TERMINAL);
    private static final Predicate<AtlasObject> IS_BUILDING = atlasObject -> Validators
            .hasValuesFor(atlasObject, BuildingTag.class);
    private static final String MINIMUM_HIGHWAY_TYPE_OVERLAPPING_EDGE_DEFAULT = HighwayTag.TOLL_GANTRY
            .toString();
    private static final String MINIMUM_HIGHWAY_TYPE_PIER_DEFAULT = HighwayTag.TOLL_GANTRY
            .toString();
    private static final int INSTRUCTION_INDEX_0 = 0;
    private static final int INSTRUCTION_INDEX_1 = 1;
    private static final int INSTRUCTION_INDEX_2 = 2;
    private static final int INSTRUCTION_INDEX_3 = 3;
    private final HighwayTag minimumHighwayTypeOverlappingEdge;
    private final HighwayTag minimumHighwayTypePier;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public InvalidPiersCheck(final Configuration configuration)
    {
        super(configuration);
        this.minimumHighwayTypeOverlappingEdge = this.configurationValue(configuration,
                "highway.type.minimum.overlapping", MINIMUM_HIGHWAY_TYPE_OVERLAPPING_EDGE_DEFAULT,
                configValue -> HighwayTag.valueOf(configValue.toUpperCase()));
        this.minimumHighwayTypePier = this.configurationValue(configuration,
                "highway.type.minimum.pier", MINIMUM_HIGHWAY_TYPE_PIER_DEFAULT,
                configValue -> HighwayTag.valueOf(configValue.toUpperCase()));
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check. Valid object
     * for the check is a main edge with man_made=pier tag and does not have an area=yes tag.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && ((Edge) object).isMainEdge() && ManMadeTag.isPier(object)
                && HAS_NO_AREA_TAG.test(object) && !this.isFlagged(object.getOsmIdentifier());
    }

    /**
     * Overriding this method to not skip piers.
     *
     * @return true since we need the check to accept piers
     */
    @Override
    protected boolean acceptPier()
    {
        return true;
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @SuppressWarnings("squid:S3655")
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        // We will mark the edge as flagged as we will only be looking at all way sectioned edges of
        // a way once
        this.markAsFlagged(edge.getOsmIdentifier());
        // Collect all main edges that form the OSM way
        final Set<Edge> edgesFormingOSMWay = new OsmWayWalker(edge).collectEdges().stream()
                .filter(Edge::isMainEdge).collect(Collectors.toSet());
        final List<Edge> listOfEdgesFormingOSMWay = new ArrayList<>(edgesFormingOSMWay);
        // We need to sort the edges to build the geometry of the OSM way in a deterministic manner
        listOfEdgesFormingOSMWay.sort(Comparator.comparingLong(AtlasObject::getIdentifier));
        // Build a polygon using the locations in the edges
        final Set<Location> locationsInOsmWay = new HashSet<>();
        listOfEdgesFormingOSMWay
                .forEach(osmWayEdge -> locationsInOsmWay.addAll(osmWayEdge.asPolyLine()));
        final Polygon osmWayAsPolygon = new Polygon(locationsInOsmWay);
        // Check if the OSM way has linear geometry or polygonal geometry
        final boolean isPolygonal = this.hasPolygonalGeometry(listOfEdgesFormingOSMWay, edge);
        final int instructionIndex;
        // We can flag the edge if it has a highway tag with the right priority or is a polygonal
        // pier with building tag or is a polygonal pier with amenity=ferry_terminal
        if ((HighwayTag.highwayTag(edge).isPresent() && HighwayTag.highwayTag(edge).get()
                .isMoreImportantThanOrEqualTo(this.minimumHighwayTypePier))
                || (isPolygonal && IS_BUILDING.test(edge))
                || (isPolygonal && IS_FERRY_TERMINAL.test(edge)))
        {
            instructionIndex = isPolygonal ? INSTRUCTION_INDEX_1 : INSTRUCTION_INDEX_0;
            return Optional.of(this.createFlag(edgesFormingOSMWay,
                    this.getLocalizedInstruction(instructionIndex, object.getOsmIdentifier())));

        }
        // Check if the pier has connections to ferry route or buildings
        final boolean isConnectedToFerryOrBuilding = this.isConnectedToFerryOrBuilding(edge,
                listOfEdgesFormingOSMWay, isPolygonal, osmWayAsPolygon);
        // Check if the pier overlaps a highway or not
        final boolean overlapsHighway = this.pierOverlapsHighway(edge, listOfEdgesFormingOSMWay,
                osmWayAsPolygon, isPolygonal);
        // Flag the pier if it overlaps a highway or is connected
        // to a building or ferry route or overlaps a building
        instructionIndex = isPolygonal ? INSTRUCTION_INDEX_3 : INSTRUCTION_INDEX_2;
        return overlapsHighway || isConnectedToFerryOrBuilding
                ? Optional.of(this.createFlag(edgesFormingOSMWay,
                        this.getLocalizedInstruction(instructionIndex, object.getOsmIdentifier())))
                : Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks if two {@link AtlasObject} are on the same level and layer as the other.
     *
     * @param objectOne
     *            first object to compare
     * @param objectTwo
     *            second object to compare
     * @return true if the level and layer of edgeOne and edgeTwo are the same
     */
    private boolean areOnSameLevelOrLayer(final AtlasObject objectOne, final Edge objectTwo)
    {
        return LayerTag.areOnSameLayer(objectOne, objectTwo)
                && LevelTag.areOnSameLevel(objectOne, objectTwo);
    }

    /**
     * Checks if the input edges form a polygon or not. We need to traverse the out edges and see if
     * the edges form a closed loop to verify that the edges form a polygonal geometry.
     * 
     * @param edgesFormingOSMWay
     *            set of edges
     * @param originalEdge
     *            originalEdge
     * @return true if the input edges form a polygon geometry,
     */
    private boolean hasPolygonalGeometry(final List<Edge> edgesFormingOSMWay,
            final Edge originalEdge)
    {
        // Captures cases where the single edge has polygonal geometry
        if (edgesFormingOSMWay.size() == 1 && originalEdge.start().equals(originalEdge.end())
                && originalEdge.length().isGreaterThan(Distance.ZERO))
        {
            return true;
        }
        final HashSet<Long> wayIds = new HashSet<>();
        Edge nextEdge = originalEdge;
        // Loop through out going edges with the same OSM id
        while (nextEdge != null)
        {
            wayIds.add(nextEdge.getIdentifier());
            final List<Edge> nextEdgeList = Iterables.stream(nextEdge.outEdges())
                    .filter(outEdge -> outEdge.isMainEdge()
                            && outEdge.getOsmIdentifier() == originalEdge.getOsmIdentifier())
                    .collectToList();
            nextEdge = nextEdgeList.isEmpty() ? null : nextEdgeList.get(0);
            // If original edge is found, the way is closed
            if (nextEdge != null && wayIds.contains(nextEdge.getIdentifier()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if given edge is connected to a ferry/building or overlaps a building. In case of
     * polygonal piers, buildings that are within the polygon and connected to it are considered
     * overlapping buildings. In case of linear piers, we only check if the building intersects any
     * of the edges of the linear pier.
     *
     * @param originalEdge
     *            any edge
     * @param edgesOfOSMWay
     *            list of edges forming OSM way
     * @param isPolygonal
     *            if true, the OSM way has polygonal geometry else a linear geometry
     * @param polygon
     *            polygon formed from the OSM way edges
     * @return true if the edge is connected to a building/ferry or overlaps a building
     */
    private boolean isConnectedToFerryOrBuilding(final Edge originalEdge,
            final List<Edge> edgesOfOSMWay, final boolean isPolygonal, final Polygon polygon)
    {
        final boolean intersectsFerryRoute;
        final boolean intersectsBuilding;
        final Atlas atlas = originalEdge.getAtlas();
        // Checks if the pier is connected to a ferry route
        intersectsFerryRoute = edgesOfOSMWay.stream()
                .anyMatch(eachEdge -> eachEdge.connectedEdges().stream().filter(
                        connectedEdge -> this.areOnSameLevelOrLayer(connectedEdge, originalEdge))
                        .anyMatch(RouteTag::isFerry));
        if (isPolygonal)
        {
            intersectsBuilding = edgesOfOSMWay.stream().anyMatch(eachEdge -> Iterables
                    .stream(atlas.areasIntersecting(eachEdge.bounds(), intersectingArea -> (polygon
                            .fullyGeometricallyEncloses(intersectingArea.asPolygon())
                            || intersectingArea.asPolygon().intersects(eachEdge.asPolyLine()))
                            && this.areOnSameLevelOrLayer(intersectingArea, originalEdge)
                            && (IS_BUILDING.test(intersectingArea)
                                    || IS_FERRY_TERMINAL.test(intersectingArea))))
                    .iterator().hasNext());
        }
        else
        {
            intersectsBuilding = edgesOfOSMWay.stream().anyMatch(eachEdge -> Iterables
                    .stream(atlas.areasIntersecting(eachEdge.bounds(),
                            intersectingArea -> intersectingArea.asPolygon()
                                    .intersects(eachEdge.asPolyLine())
                                    && this.areOnSameLevelOrLayer(intersectingArea, originalEdge)
                                    && (IS_BUILDING.test(intersectingArea)
                                            || IS_FERRY_TERMINAL.test(intersectingArea))))
                    .iterator().hasNext());
        }
        return intersectsFerryRoute || intersectsBuilding;
    }

    /**
     * Checks if the input edge overlaps any of the edges in the input list of edges. We check if
     * the intersecting edge overlaps any of the given edges and not just intersects the edges.
     *
     * @param intersectingEdge
     *            any edge
     * @param edgesFormingOSMWay
     *            all edges that are part of an OSM way
     * @return true if the edge overlaps any of the edges in the given list of edges
     */
    private boolean linearPierOverlapsHighway(final Edge intersectingEdge,
            final List<Edge> edgesFormingOSMWay)
    {
        return edgesFormingOSMWay.stream().anyMatch(connectedEdge -> connectedEdge.asPolyLine()
                .overlapsShapeOf(intersectingEdge.asPolyLine()));

    }

    /**
     * Checks if the way formed from the input edges overlap a highway or not. We only consider
     * intersecting edges that have highway tags, are on the same level/layer as the pier and are
     * main edges.
     *
     * @param originalEdge
     *            input edge
     * @param edgesFormingOSMWay
     *            all the edges forming OSM way
     * @param osmWayAsPolygon
     *            polygon built from the OSM way edges
     * @param isPolygonal
     *            if true, the OSM way has polygonal geometry else a linear geometry
     * @return true if a highway at the same level/layer overlaps the edges forming the OSM way
     */
    private boolean pierOverlapsHighway(final AtlasObject originalEdge,
            final List<Edge> edgesFormingOSMWay, final Polygon osmWayAsPolygon,
            final boolean isPolygonal)
    {
        return Iterables.stream(originalEdge.getAtlas().edgesIntersecting(osmWayAsPolygon))
                .filter(intersectingEdge -> intersectingEdge.getOsmIdentifier() != originalEdge
                        .getOsmIdentifier())
                .anyMatch(intersectingEdge -> intersectingEdge.isMainEdge()
                        && HighwayTag.highwayTag(intersectingEdge).isPresent()
                        && HighwayTag.highwayTag(intersectingEdge).get()
                                .isMoreImportantThanOrEqualTo(
                                        this.minimumHighwayTypeOverlappingEdge)
                        && this.areOnSameLevelOrLayer(originalEdge, intersectingEdge)
                        // Check if the intersectingEdge is not just intersecting but is overlapping
                        // the pier
                        && (isPolygonal
                                ? this.polygonalPierOverlapsHighway(intersectingEdge,
                                        osmWayAsPolygon)
                                : this.linearPierOverlapsHighway(intersectingEdge,
                                        edgesFormingOSMWay)));
    }

    /**
     * Checks if the given edge overlaps the given polygon.
     *
     * @param intersectingEdge
     *            any edge
     * @param osmWayAsPolygon
     *            any polygon
     * @return true if the edge overlaps the polygon
     */
    private boolean polygonalPierOverlapsHighway(final Edge intersectingEdge,
            final Polygon osmWayAsPolygon)
    {
        return intersectingEdge.asPolyLine().overlapsShapeOf(osmWayAsPolygon)
                // Overlaps a polygon if any of the segments of the intersecting edge lies within
                // the polygon
                || intersectingEdge.asPolyLine().segments().stream()
                        .anyMatch(osmWayAsPolygon::fullyGeometricallyEncloses);
    }
}
