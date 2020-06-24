package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.roundabout.ComplexRoundabout;
import org.openstreetmap.atlas.geography.atlas.walker.SimpleEdgeWalker;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.TunnelTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags roundabouts where the directionality is opposite to what it should be, the
 * roundabout is multi-directional, the roundabout is incomplete, part of the roundabout is not car
 * navigable, or the roundabout has car navigable edges inside it.
 *
 * @author savannahostrowski
 * @author bbreithaupt
 */

public class MalformedRoundaboutCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -3018101860747289836L;
    private static final String BASIC_INSTRUCTION = "This roundabout is malformed.";
    private static final String ENCLOSED_ROADS_INSTRUCTIONS = "This roundabout has car navigable ways inside it.";
    private static final List<String> LEFT_DRIVING_COUNTRIES_DEFAULT = Arrays.asList("AIA", "ATG",
            "AUS", "BGD", "BHS", "BMU", "BRB", "BRN", "BTN", "BWA", "CCK", "COK", "CXR", "CYM",
            "CYP", "DMA", "FJI", "FLK", "GBR", "GGY", "GRD", "GUY", "HKG", "IDN", "IMN", "IND",
            "IRL", "JAM", "JEY", "JPN", "KEN", "KIR", "KNA", "LCA", "LKA", "LSO", "MAC", "MDV",
            "MLT", "MOZ", "MSR", "MUS", "MWI", "MYS", "NAM", "NFK", "NIU", "NPL", "NRU", "NZL",
            "PAK", "PCN", "PNG", "SGP", "SGS", "SHN", "SLB", "SUR", "SWZ", "SYC", "TCA", "THA",
            "TKL", "TLS", "TON", "TTO", "TUV", "TZA", "UGA", "VCT", "VGB", "VIR", "WSM", "ZAF",
            "ZMB", "ZWE");
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(ENCLOSED_ROADS_INSTRUCTIONS, BASIC_INSTRUCTION);
    private final List<String> leftDrivingCountries;

    public MalformedRoundaboutCheck(final Configuration configuration)
    {
        super(configuration);
        this.leftDrivingCountries = configurationValue(configuration, "traffic.countries.left",
                LEFT_DRIVING_COUNTRIES_DEFAULT);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // We check that the object is an instance of Edge
        return object instanceof Edge
                // Make sure that the object has an iso_country_code
                && object.getTag(ISOCountryTag.KEY).isPresent()
                // Make sure that the edges are instances of roundabout
                && JunctionTag.isRoundabout(object)
                // And that the Edge has not already been marked as flagged
                && !this.isFlagged(object.getIdentifier())
                // Make sure that we are only looking at master edges
                && ((Edge) object).isMasterEdge()
                // Check for excluded highway types
                && !this.isExcludedHighway(object)
                // Check if object doesn't contain synthetic node.
                // https://github.com/osmlab/atlas-checks/issues/316
                && !this.isEdgeWithSyntheticBoundaryNode(object);
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
        final Set<String> instructions = new HashSet<>();

        // Create a ComplexRoundabout based on object
        final ComplexRoundabout complexRoundabout = new ComplexRoundabout((Edge) object,
                this.leftDrivingCountries);
        // If the ComplexRoundbaout is invalid add the reasons why to the instructions, so that it
        // will be flagged
        if (!complexRoundabout.isValid())
        {
            instructions.addAll(complexRoundabout.getAllInvalidations().stream()
                    .map(ComplexEntity.ComplexEntityError::getReason).collect(Collectors.toSet()));
        }
        // Get the roundabout Edges
        final Set<Edge> roundaboutEdgeSet = complexRoundabout.getRoundaboutEdgeSet();
        // Get the roundabout Route
        final Route roundaboutEdges = complexRoundabout.getRoundaboutRoute();
        // Mark that the Edges have been processed
        roundaboutEdgeSet
                .forEach(roundaboutEdge -> this.markAsFlagged(roundaboutEdge.getIdentifier()));

        // If there are car navigable edges inside the roundabout flag it, as it is probably
        // malformed or a throughabout
        if (!roundaboutEdgeSet.isEmpty() && roundaboutEdges != null
                && roundaboutEdgeSet.stream().noneMatch(this::ignoreBridgeTunnelCrossings)
                && this.roundaboutEnclosesRoads(roundaboutEdges))
        {
            instructions.add(this.getLocalizedInstruction(0));
        }

        if (!instructions.isEmpty())
        {
            final CheckFlag flag = this.createFlag(roundaboutEdgeSet,
                    this.getLocalizedInstruction(1));
            instructions.forEach(flag::addInstruction);
            return Optional.of(flag);
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks if an {@link Edge} has values that indicate it is crossable by another edge.
     *
     * @param edge
     *            {@link Edge} to be checked
     * @return true if it is a bridge or tunnel, or has a layer tag
     */
    private boolean ignoreBridgeTunnelCrossings(final Edge edge)
    {
        return Validators.hasValuesFor(edge, LayerTag.class) || BridgeTag.isBridge(edge)
                || TunnelTag.isTunnel(edge);
    }

    /**
     * Checks if an {@link Edge} intersects a {@link Polygon} and is either fully inside it or
     * intersecting at a point other than one of its {@link Node}s.
     *
     * @param polygon
     *            {@link Polygon} to check against
     * @param edge
     *            the {@link Edge} to check
     * @return true if some part of the {@link Edge} is inside the {@link Polygon} and doesn't just
     *         have touching {@link Node}s.
     */
    private boolean intersectsWithEnclosedGeometry(final Polygon polygon, final Edge edge)
    {
        final PolyLine polyline = edge.asPolyLine();
        return polygon.intersections(polyline).stream()
                .anyMatch(intersection -> !(edge.start().getLocation().equals(intersection)
                        || edge.end().getLocation().equals(intersection))
                        || polygon.fullyGeometricallyEncloses(polyline));
    }

    /**
     * Checks if {@link AtlasObject} contains synthetic boundary Node
     * 
     * @param object
     *            the {@link AtlasObject} to check
     * @return true if roundabout contains synthetic boundary Node.
     */
    private boolean isEdgeWithSyntheticBoundaryNode(final AtlasObject object)
    {
        return new SimpleEdgeWalker((Edge) object, this.isRoundaboutEdge()).collectEdges().stream()
                .anyMatch(roundaboutEdge -> roundaboutEdge.connectedNodes().stream()
                        .anyMatch(SyntheticBoundaryNodeTag::isSyntheticBoundaryNode));
    }

    /**
     * Checks if an {@link AtlasObject} has a highway value that excludes it from this check. These
     * have been excluded because they commonly act differently from car navigable roundabouts.
     *
     * @param object
     * @return
     */
    private boolean isExcludedHighway(final AtlasObject object)
    {
        return Validators.isOfType(object, HighwayTag.class, HighwayTag.CYCLEWAY,
                HighwayTag.PEDESTRIAN, HighwayTag.FOOTWAY);
    }

    /**
     * Function for {@link SimpleEdgeWalker} that gathers connected edges that are part of a
     * roundabout.
     *
     * @return {@link Function} for {@link SimpleEdgeWalker}
     */
    private Function<Edge, Stream<Edge>> isRoundaboutEdge()
    {
        return edge -> edge.connectedEdges().stream()
                .filter(connected -> JunctionTag.isRoundabout(connected)
                        && !this.isExcludedHighway(connected));
    }

    /**
     * Checks for roads that should not be inside a roundabout. Such roads are car navigable, not a
     * roundabout, bridge, or tunnel, don't have a layer tag, and have geometry inside the
     * roundabout.
     *
     * @param roundabout
     *            A roundabout as a {@link Route}
     * @return true if there is a road that is crossing and has geometry enclosed by the roundabout
     */
    private boolean roundaboutEnclosesRoads(final Route roundabout)
    {
        final Polygon roundaboutPoly = new Polygon(roundabout.asPolyLine());
        return roundabout.start().getAtlas().edgesIntersecting(roundaboutPoly,
                edge -> HighwayTag.isCarNavigableHighway(edge) && !JunctionTag.isRoundabout(edge)
                        && !this.ignoreBridgeTunnelCrossings(edge)
                        && this.intersectsWithEnclosedGeometry(roundaboutPoly, edge))
                .iterator().hasNext();
    }
}
