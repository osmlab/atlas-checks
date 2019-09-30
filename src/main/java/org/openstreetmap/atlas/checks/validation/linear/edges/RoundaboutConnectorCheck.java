package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.direction.EdgeDirectionComparator;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check looks for roundabout connectors that intersect the roundabout at too sharp an angle.
 * This is generally caused by one way roads drawn in the wrong direction or two way roads that
 * should be one way.
 *
 * @author bbreithaupt
 */
public class RoundaboutConnectorCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -5311314357995383430L;

    private static final Double ONE_WAY_THRESHOLD_DEFAULT = 100.0;
    private static final Double TWO_WAY_THRESHOLD_DEFAULT = 130.0;
    private static final Double POLYLINE_END_DISTANCE_METERS_DEFAULT = 50.0;
    private static final String ONE_WAY_INSTRUCTION = "This way, id:{0,number,#}, is connected to a roundabout at too sharp and angle.";
    private static final String TWO_WAY_INSTRUCTION = "This way, id:{0,number,#}, is connected to a roundabout at too sharp and angle to be a two way road.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(ONE_WAY_INSTRUCTION,
            TWO_WAY_INSTRUCTION);

    private static final EdgeDirectionComparator EDGE_DIRECTION_COMPARATOR = new EdgeDirectionComparator();

    // Maximum angle for a turn in or out of a roundabout from a one way road.
    private final Angle oneWayThreshold;
    // Maximum angle for a turn in or out of a roundabout from a two way road.
    private final Angle twoWayThreshold;
    // Maximum distance for start/end nodes to be separated between double digitized roads.
    private final Distance polylineEndDistanceThreshold;

    private final boolean runParallelCheck;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public RoundaboutConnectorCheck(final Configuration configuration)
    {
        super(configuration);
        this.oneWayThreshold = this.configurationValue(configuration, "threshold.one-way",
                ONE_WAY_THRESHOLD_DEFAULT, Angle::degrees);
        this.twoWayThreshold = this.configurationValue(configuration, "threshold.two-way",
                TWO_WAY_THRESHOLD_DEFAULT, Angle::degrees);
        this.polylineEndDistanceThreshold = this.configurationValue(configuration,
                "end.meters.maximum", POLYLINE_END_DISTANCE_METERS_DEFAULT, Distance::meters);
        this.runParallelCheck = this.configurationValue(configuration, "parallel.check.run", false);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && !this.isFlagged(object.getOsmIdentifier())
                && !JunctionTag.isRoundabout(object)
                && ((Edge) object).highwayTag().isMoreImportantThan(HighwayTag.SERVICE);
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
        // Get the object as an Edge
        final Edge edge = (Edge) object;
        // Look for a connected roundabout Edge
        final Optional<Edge> optionalRoundaboutEdge = this.getRoundaboutEdge(edge);
        // Short circuit if the edge is not connected to a roundabout
        if (!optionalRoundaboutEdge.isPresent())
        {
            return Optional.empty();
        }
        final Edge roundaboutEdge = optionalRoundaboutEdge.get();

        String instruction = null;

        // Get the correct heading depending on if we are entering or exiting the roundabout and
        // record the connecting Node
        final Optional<Heading> edgeHeading;
        final Optional<Heading> roundaboutHeading;
        final Node connectingNode;
        if (edge.inEdges().contains(roundaboutEdge))
        {
            edgeHeading = edge.asPolyLine().initialHeading();
            roundaboutHeading = roundaboutEdge.asPolyLine().finalHeading();
            connectingNode = edge.start();
        }
        else
        {
            edgeHeading = edge.asPolyLine().finalHeading();
            roundaboutHeading = roundaboutEdge.asPolyLine().initialHeading();
            connectingNode = edge.end();
        }

        // Check that we have both headings
        if (edgeHeading.isPresent() && roundaboutHeading.isPresent())
        {
            // If this is a two way road and the angle is greater than the two way threshold...
            if (edge.hasReverseEdge() && edgeHeading.get().difference(roundaboutHeading.get())
                    .isGreaterThanOrEqualTo(this.twoWayThreshold))
            {
                instruction = this.getLocalizedInstruction(1, object.getOsmIdentifier());

            }
            // If this is one way road and the angle is greater than the one way threshold...
            else if (!edge.hasReverseEdge()
                    && edgeHeading.get().difference(roundaboutHeading.get())
                            .isGreaterThanOrEqualTo(this.oneWayThreshold)
                    && !(this.runParallelCheck && this.hasParallelReverse(edge, connectingNode)))
            {
                instruction = this.getLocalizedInstruction(0, object.getOsmIdentifier());
            }
        }

        if (instruction != null)
        {
            this.markAsFlagged(edge.getOsmIdentifier());
            return Optional.of(this.createFlag(new OsmWayWalker(edge).collectEdges(), instruction));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Get the first connected roundabout {@link Edge} from the in and out {@link Edge}s
     *
     * @param edge
     *            the {@link Edge} to look for connected roundabouts from
     * @return an {@link Optional} of a roundabout {@link Edge}
     */
    private Optional<Edge> getRoundaboutEdge(final Edge edge)
    {
        return Stream.concat(edge.inEdges().stream(), edge.outEdges().stream())
                .filter(JunctionTag::isRoundabout).findFirst();
    }

    /**
     * Check if an roundabout connector {@link Edge} is part of a pair of properly formed parallel
     * in and out connector Ways. If it is, it does not need to flag it. A proper parallel pair of
     * Ways will have overall headings that are about opposite, start/end close to their opposites
     * end/start, and the out connector will precede the in connector in its connection to the
     * roundabout (based on the direction of the roundabout).
     *
     * @param connector
     *            {@link Edge} to check the Way of
     * @param connectingNode
     *            {@link Node} connecting the connector to the roundabout
     * @return true if the connector has a parallel reverse.
     */
    private boolean hasParallelReverse(final Edge connector, final Node connectingNode)
    {
        // Get the connector Way polyline
        final PolyLine connectorPolyline = Route
                .fromNonArrangedEdgeSet(new OsmWayWalker(connector).collectEdges(), false)
                .asPolyLine();
        // Get the connector Way heading
        final Optional<Heading> connectorAverageHeading = connectorPolyline.overallHeading();
        // Get the possible roundabout edges
        final Set<Edge> connectedEdges = connector.start().equals(connectingNode)
                ? connectingNode.outEdges()
                : connectingNode.inEdges();
        // Make sure we have a connector heading
        if (connectorAverageHeading.isPresent())
        {
            // Get the polylines of the possible parallel reverse Ways
            final List<PolyLine> oneEdgeAwayConnectorsHeadings = connectedEdges.stream()
                    .filter(JunctionTag::isRoundabout)
                    .flatMap(roundaboutEdge -> roundaboutEdge.connectedEdges().stream()
                            .filter(edge -> !edge.equals(connector) && !edge.hasReverseEdge()
                                    && !JunctionTag.isRoundabout(edge)))
                    .map(edge -> Route
                            .fromNonArrangedEdgeSet(new OsmWayWalker(edge).collectEdges(), false)
                            .asPolyLine())
                    .collect(Collectors.toList());
            // Check the headings and start/end points to detect parallel reverse ways.
            for (final PolyLine polyline : oneEdgeAwayConnectorsHeadings)
            {
                if (connectorPolyline.first().distanceTo(polyline.last())
                        .isLessThanOrEqualTo(this.polylineEndDistanceThreshold)
                        && connectorPolyline.last().distanceTo(polyline.first())
                                .isLessThanOrEqualTo(this.polylineEndDistanceThreshold)
                        && EDGE_DIRECTION_COMPARATOR.isOppositeDirection(
                                polyline.overallHeading().orElse(connectorAverageHeading.get()),
                                connectorAverageHeading.get()))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
