package org.openstreetmap.atlas.checks.validation.linear.lines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.validation.GeometryValidator;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.AmenityTag;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.ManMadeTag;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Auto generated Check template
 *
 * @author sayas01
 */
public class InvalidPiersCheck extends BaseCheck
{
    private static final long serialVersionUID = 6L;
    private static final String LINEAR_WITH_PIER_TAG = "This way {0,number,#} has a \"man_made=pier\" tag but has a linear geometry. "
            + "Please make necessary changes to convert its geometry to a polygon and add tag, \"area=yes\".";
    private static final String POLYGON_WITH_NO_AREA_TAG = "This way {0,number,#} has a \"man_made=pier\" and a polygon geometry, "
            + "but is missing \"area=yes\" tag.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(LINEAR_WITH_PIER_TAG, POLYGON_WITH_NO_AREA_TAG);
    private static final Predicate<AtlasObject> HAS_NO_AREA_TAG =
            atlasObject -> !Validators.isOfType(atlasObject, AreaTag.class, AreaTag.YES);
    private static final String FERRY_TERMINAL = "ferry_terminal";

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
     * for the check is a Line or Polygon with a man_made=pier tag
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
        final Set<Edge> edges = new OsmWayWalker(edge).collectEdges().stream().filter(Edge::isMasterEdge).collect(
                Collectors.toSet());
        final List<Location> allLocations = new ArrayList<>();
        edges.forEach(waySectionedEdge -> allLocations.addAll(waySectionedEdge.asPolyLine()));
        final Polygon polygon = new Polygon(allLocations);
        // Check if the OSM way has linear geometry or polygonal geometry
        final boolean isPolygonal = this.hasPolygonalGeometry( edges, edge, polygon);
        final boolean overlapsHighway = this.overlapsHighway(edge, isPolygonal, edges);
        final boolean isConnectedToFerryOrBuilding = this.isConnectedToFerryOrBuilding(polygon, edge);
        final boolean isValidPier = HighwayTag.highwayTag(edge).isPresent() || overlapsHighway
                || isConnectedToFerryOrBuilding;
        if(!isValidPier)
        {
            return Optional.empty();
        }
        // Line with both area=yes and man_made=pier tag or just an_made=pier tag
        // Polygon with man_made=pier tag but no area=yes tag
        final int instructionIndex = isPolygonal ? 1 :0;

        this.markAsFlagged(edge.getOsmIdentifier());
        return Optional.of(this.createFlag(edges,
                this.getLocalizedInstruction(instructionIndex, object.getOsmIdentifier())));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private boolean hasPolygonalGeometry(final Set<Edge> allEdges, final Edge originalEdge, final Polygon polygon)
    {
        if((allEdges.size()==1 && originalEdge.start().equals(originalEdge.end()) && originalEdge.length().isGreaterThan(
                Distance.ZERO) ))
        {
            return true;
        }
        if(GeometryValidator.isValidPolygon(polygon))
        {
            final List<Segment> segments = polygon.segments();
            final List<Segment> edgeSegments = new ArrayList<>();
            for(Edge e: allEdges)
            {

                edgeSegments.addAll(e.asPolyLine().segments());
            }
            for(Segment segment: segments)
            {
                if(segment.start().equals(segment.end()))
                {
                    continue;
                }
                if(!edgeSegments.contains(segment))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean overlapsHighway(final Edge edge, final boolean isPolygonal, final Set<Edge> edges)
    {
        final List<Edge> intersectingHighways = Iterables
                .stream(edge.getAtlas().edgesIntersecting(edge.asPolyLine().bounds()))
                .filter(intersectingEdge ->
                        HighwayTag.highwayTag(intersectingEdge).isPresent()).collectToList();
        if(!isPolygonal)
        {
            final PolyLine edgeAsPolyLine = edge.asPolyLine();
            return intersectingHighways.stream().anyMatch
                    (filteredEdge-> filteredEdge.asPolyLine().overlapsShapeOf(edgeAsPolyLine));
        }
        return intersectingHighways.stream().anyMatch(intersectingHighway -> edges.stream().anyMatch(waySectionedEdge ->
                intersectingHighway.asPolyLine().overlapsShapeOf(waySectionedEdge.asPolyLine())));
    }

    /**
     * Checks if the way is connected to a street, ferry or building
     *
     * @param edge any Edge
     * @return true if the edge is connected to street, ferry or building
     */
    private boolean isConnectedToFerryOrBuilding(final Polygon polygon, final Edge edge)
    {
        final Atlas atlas = edge.getAtlas();
        // Check if the way intersect an area with building tag or amenity=ferry_terminal or
        // if the way is connected to a ferry route or way with amenity=ferry_terminal
        return atlas.areasIntersecting(polygon, taggable -> BuildingTag.isBuilding(taggable) ||
                (Validators.isOfType(taggable, AmenityTag.class) &&
                taggable.getTag("amenity").get().equals(FERRY_TERMINAL))).iterator().hasNext() ||
        // Find if the edge intersects a ferry route or a ferry_terminal
        Iterables.stream(atlas.edgesIntersecting(polygon))
                .anyMatch(intersectingEdge ->
                        //connected to a ferry
                        RouteTag.isFerry(intersectingEdge)
                                || Validators.isOfType(intersectingEdge.start(), AmenityTag.class) &&
                                intersectingEdge.start().getTag("amenity").get().equals(FERRY_TERMINAL)||
                                Validators.isOfType(intersectingEdge.end(), AmenityTag.class) &&
                                intersectingEdge.end().getTag("amenity").get().equals(FERRY_TERMINAL));
    }
}
