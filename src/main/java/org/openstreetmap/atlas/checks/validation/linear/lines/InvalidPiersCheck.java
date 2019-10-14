package org.openstreetmap.atlas.checks.validation.linear.lines;

import java.util.Arrays;
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
 * This check identifies piers (OSM ways with man_made = Pier tag) that have either a
 * linear geometry and no area = Yes tag, or a polygonal geometry and no area=Yes tag.
 * A pier must also have (i) a highway Tag or (ii) a highway overlapping the pier or (iii) be connected
 * to a ferry route/amenity = Ferry_Terminal or (iv) be connected to a building.
 *
 * @author sayas01
 */
public class InvalidPiersCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 6L;
    private static final String LINEAR_WITH_PIER_TAG = "This way {0,number,#} has a \"man_made=pier\" tag but has a linear geometry. "
            + "Please make necessary changes to convert its geometry to a polygon and add tag, \"area=yes\".";
    private static final String POLYGON_WITH_NO_AREA_TAG = "This way {0,number,#} has a \"man_made=pier\" and a polygon geometry, "
            + "but is missing \"area=yes\" tag.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(LINEAR_WITH_PIER_TAG, POLYGON_WITH_NO_AREA_TAG);
    private static final Predicate<AtlasObject> HAS_NO_AREA_TAG =
            atlasObject -> !Validators.isOfType(atlasObject, AreaTag.class, AreaTag.YES);
    private static final Predicate<AtlasObject> IS_FERRY_TERMINAL = atlasObject ->
            Validators.isOfType(atlasObject, AmenityTag.class, AmenityTag.FERRY_TERMINAL);

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
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check. Valid object
     * for the check is a master edge with man_made=pier tag and does not have an area=yes tag.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge
               && ((Edge) object).isMasterEdge()
               && ManMadeTag.isPier(object)
               && HAS_NO_AREA_TAG.test(object)
               && !this.isFlagged(object.getOsmIdentifier());
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
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        // Collect all master edges that form the OSM way
        final Set<Edge> edgesFormingOSMWay = new OsmWayWalker(edge).collectEdges().stream().filter(Edge::isMasterEdge).collect(
                Collectors.toSet());
        // Build a polygon using the locations in the edges
        final Set<Location> locationsInOsmWay = new HashSet<>();
        edgesFormingOSMWay.forEach(osmWayEdge -> locationsInOsmWay.addAll(osmWayEdge.asPolyLine()));
        final Polygon osmWayAsPolygon = new Polygon(locationsInOsmWay);
        // Check if the OSM way has linear geometry or polygonal geometry
        final boolean isPolygonal =
                this.hasPolygonalGeometry( edgesFormingOSMWay, edge);
        final Atlas atlas = edge.getAtlas();
        // Check if the pier overlaps a highway or not
        final boolean overlapsHighway = this.pierOverlapsHighway(edge, edgesFormingOSMWay, osmWayAsPolygon, atlas, isPolygonal, locationsInOsmWay);
        // Check if the pier has connections to ferry route or buildings
         final boolean isConnectedToFerryOrBuilding = this.isConnectedToFerryOrBuilding(edge, atlas, edgesFormingOSMWay, isPolygonal, osmWayAsPolygon);
        // A pier is valid if it either has a highway tag or it overlaps a highway or is connected to a building or ferry route
        final boolean isValidPier = HighwayTag.highwayTag(edge).isPresent() || overlapsHighway
                || isConnectedToFerryOrBuilding;
        if(!isValidPier)
        {
            return Optional.empty();
        }
        final int instructionIndex = isPolygonal ? 1 :0;
        this.markAsFlagged(edge.getOsmIdentifier());
        return Optional.of(this.createFlag(edgesFormingOSMWay,
                this.getLocalizedInstruction(instructionIndex, object.getOsmIdentifier())));
    }

    private boolean pierOverlapsHighway(final Edge originalEdge, final Set<Edge> edgesFormingOSMWay, final Polygon osmWayAsPolygon, final Atlas atlas, final boolean isPolygonal, final Set<Location> locationsInOsmWay)
    {
        return Iterables.stream(atlas.edgesIntersecting(osmWayAsPolygon)).anyMatch(intersectingEdge ->
                            intersectingEdge.isMasterEdge()&&
                            HighwayTag.highwayTag(intersectingEdge).isPresent()
                            && this.areOnSameLayer(originalEdge, intersectingEdge)
                            && this.areOnSameLevel(originalEdge, intersectingEdge)
                            // should not just be intersecting
                            && (isPolygonal ? this.polygonalPierOverlapsHighway(intersectingEdge, osmWayAsPolygon,originalEdge):
                            this.linearPierOverlapsHighway(intersectingEdge, locationsInOsmWay, edgesFormingOSMWay )));
    }

    private boolean polygonalPierOverlapsHighway(final Edge intersectingEdge, final Polygon osmWayAsPolygon, final Edge originalEdge)
    {
        return intersectingEdge.asPolyLine().overlapsShapeOf(osmWayAsPolygon) ||
                intersectingEdge.asPolyLine().segments().stream().anyMatch(
                        osmWayAsPolygon::fullyGeometricallyEncloses);
    }

    private boolean linearPierOverlapsHighway(final Edge intersectingEdge, final Set<Location> locationsInOSMWay, final Set<Edge> edgesFormingOSMWay )
    {
       return edgesFormingOSMWay.stream().anyMatch(connectedEdge -> connectedEdge.asPolyLine().overlapsShapeOf(intersectingEdge.asPolyLine()));

    }

    /**
     * Checks if two atlas objects are on the same level. Since as per https://wiki.openstreetmap.org/wiki/Key:level,
     * level=0 is not always street level, if level tag is absent, it is not considered as level=0.
     * @param taggableOne first object to compare
     * @param taggableTwo second object to compare
     * @return true if object one and object two are on the same level
     */
    private boolean areOnSameLevel(final AtlasObject taggableOne, final AtlasObject taggableTwo)
    {
        final Optional<String> levelTagEdgeOne = LevelTag.getTaggedValue(taggableOne);
        final Optional<String> levelTagEdgeTwo = LevelTag.getTaggedValue(taggableTwo);
        if (levelTagEdgeOne.isPresent() && levelTagEdgeTwo.isPresent())
        {
            return levelTagEdgeOne.get().equals(levelTagEdgeTwo.get());
        }
        return !levelTagEdgeOne.isPresent() && !levelTagEdgeTwo.isPresent();
    }

    private boolean areOnSameLayer(final AtlasObject taggableOne, final AtlasObject taggableTwo)
    {
        return LayerTag.getTaggedOrImpliedValue(taggableOne, 0L)
                .equals(LayerTag.getTaggedOrImpliedValue(taggableTwo, 0L));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks if the input edges form a polygon or not.
     * @param edgesFormingOSMWay set of edges
     * @param originalEdge
     *
     * @return true if the input edges form a polygon geometry,
     */
    private boolean hasPolygonalGeometry(final Set<Edge> edgesFormingOSMWay,
            final Edge originalEdge)
    {
        final HashSet<Long> wayIds = new HashSet<>();

        // Captures cases where the single edge has polygonal geometry
        if ((edgesFormingOSMWay.size() == 1 && originalEdge.start().equals(originalEdge.end())
                && originalEdge
                .length().isGreaterThan(
                        Distance.ZERO)))
        {
            return true;
        }
        Edge nextEdge = originalEdge;
        // Loop through out going edges with the same OSM id
        while (nextEdge != null)
        {
            wayIds.add(nextEdge.getIdentifier());
            final List<Edge> nextEdgeList = Iterables.stream(nextEdge.outEdges())
                    .filter(outEdge -> outEdge.isMasterEdge()
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

    private boolean isConnectedToFerryOrBuilding(final Edge originalEdge, final Atlas atlas, final Set<Edge> edgesOfOSMWay, final boolean isPolygonal, final Polygon polygon)
    {
        boolean intersectsFerryRoute;
        boolean intersectsBuilding;
        intersectsFerryRoute = edgesOfOSMWay.stream().anyMatch(eachEdge ->
            eachEdge.connectedEdges().stream().filter(connectedEdge -> this.areOnSameLayer(connectedEdge, originalEdge)
                    && this.areOnSameLevel(connectedEdge, originalEdge)).anyMatch(RouteTag::isFerry));

        intersectsBuilding = edgesOfOSMWay.stream().anyMatch(eachEdge -> Iterables.stream(atlas.areasIntersecting(eachEdge.bounds(),
                intersectingArea ->    intersectingArea.asPolygon().intersects(eachEdge.asPolyLine()) && this.areOnSameLevel(intersectingArea, originalEdge)
                    && this.areOnSameLayer(intersectingArea, originalEdge) &&
                    (BuildingTag.isBuilding(intersectingArea) || IS_FERRY_TERMINAL.test(intersectingArea)))).iterator().hasNext());
        if(isPolygonal)
        {
            intersectsBuilding =edgesOfOSMWay.stream().anyMatch(eachEdge -> Iterables.stream(atlas.areasIntersecting(eachEdge.bounds(),
                    intersectingArea ->  (polygon.fullyGeometricallyEncloses(intersectingArea.asPolygon()) ||
                            intersectingArea.asPolygon().intersects(eachEdge.asPolyLine())) && this.areOnSameLevel(intersectingArea, originalEdge)
                            && this.areOnSameLayer(intersectingArea, originalEdge) &&
                            (BuildingTag.isBuilding(intersectingArea) || IS_FERRY_TERMINAL.test(intersectingArea)))).iterator().hasNext());
        }

        return intersectsFerryRoute || intersectsBuilding;
    }
}
