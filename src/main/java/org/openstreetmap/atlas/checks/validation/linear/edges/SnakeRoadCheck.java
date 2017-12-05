package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.atlas.predicates.TagPredicates;
import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;

/**
 * This check flags edges that form Snake Roads. A Snake Road is defined as a road that should be
 * split into two or more roads. An example of this is a residential road that weaves in and out of
 * a neighborhood or multiple neighborhoods, where the correct behavior should have each road in a
 * neighborhood be it's own separate way. The criteria we use to identify a snake road is:
 * <ul>
 * <li>1) At some point, two consecutive {@link Edge}s making up the Snake road must have a
 * {@link Heading} difference of at least 60 degrees.
 * <li>2) The snake road must have at least {@value #MINIMUM_EDGES_TO_QUALIFY_AS_SNAKE_ROAD}
 * {@link Edge}s
 * <li>3) At least one {@link Edge} making up the snake road should have a valence greater than
 * {@value #MINIMUM_VALENCE_TO_QUALIFY_AS_SNAKE_ROAD}
 * <li>4) To be considered a snake road, you should either have no road name or have any connected
 * ways that share the same name that you do. This is done to prevent flagging strange portions of
 * highways that may exhibit the behavior of a snake road.
 * </ul>
 *
 * @author mgostintsev
 */
public class SnakeRoadCheck extends BaseCheck<Long>
{
    private static final Angle EDGE_HEADING_DIFFERENCE_THRESHOLD = Angle.degrees(60);
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "The way with id {0} is a snake road. Consider spliting it into two or more separate ways.");
    private static final long MINIMUM_EDGES_TO_QUALIFY_AS_SNAKE_ROAD = 3;
    private static final long MINIMUM_VALENCE_TO_QUALIFY_AS_SNAKE_ROAD = 4;
    private static final long serialVersionUID = 6040648590412505891L;

    /**
     * Validates whether a given {@link Edge} should be considered as a candidate to be part of a
     * snake road. The criteria used:
     * <ul>
     * <li>1) {@link HighwayTag} is equal to or more significant than Residential
     * <li>2) {@link HighwayTag}} is less important than trunk_link
     * <li>3) {@link Edge} is not a roundabout
     * <li>4) {@link Edge} is not an Open Highway Area
     * <li>5) {@link Edge} is way-sectioned
     * <li>6) {@link Edge} is the master edge
     * </ul>
     *
     * @param candidate
     *            {@link Edge} to test
     * @return {@code true} if given {@link Edge} can be part of a snake road
     */
    private static boolean isValidEdgeToConsider(final Edge candidate)
    {
        return candidate.isMasterEdge()
                && candidate.highwayTag().isMoreImportantThanOrEqualTo(HighwayTag.RESIDENTIAL)
                && candidate.highwayTag().isLessImportantThan(HighwayTag.TRUNK_LINK)
                && !JunctionTag.isRoundabout(candidate)
                && !TagPredicates.IS_HIGHWAY_AREA.test(candidate) && candidate.isWaySectioned();
    }

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public SnakeRoadCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return !this.isFlagged(object.getOsmIdentifier()) && TypePredicates.IS_EDGE.test(object)
                && isValidEdgeToConsider((Edge) object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;

        // Mark edge (and all other edges that we may walk) as visited, irregardless
        // of whether it's a snake road or not
        this.markAsFlagged(object.getOsmIdentifier());

        // Check the base case - edge must have connected edges
        if (!edge.connectedEdges().isEmpty())
        {
            // Instantiate the network walk with the starting edge
            SnakeRoadNetworkWalk walk = initializeNetworkWalk(edge);

            // Walk the road
            walk = walkNetwork(edge, walk);

            // If we've found a snake road, create a flag
            if (networkWalkQualifiesAsSnakeRoad(walk))
            {
                return Optional.of(createFlag(walk.getVisitedEdges(),
                        this.getLocalizedInstruction(0, object.getOsmIdentifier())));
            }
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Instantiates the {@link SnakeRoadNetworkWalk} with the starting edge and {@link Angle}
     * difference threshold to use for {@link Edge} heading comparison
     *
     * @param edge
     *            the starting {@link Edge} of the network walk
     * @return the {@link SnakeRoadNetworkWalk} to walk with
     */
    private SnakeRoadNetworkWalk initializeNetworkWalk(final Edge edge)
    {
        final SnakeRoadNetworkWalk walk = new SnakeRoadNetworkWalk(edge,
                EDGE_HEADING_DIFFERENCE_THRESHOLD);
        walk.addDirectConnections(walk.getConnectedMasterEdgeOfTheSameWay(edge));
        return walk;
    }

    /**
     * Determines if the network walk qualifies as a snake road. Specifically, we check the snake
     * road flag, verify the minimum number of edges and minimum valence requirements.
     *
     * @param walk
     *            the {@link SnakeRoadNetworkWalk} that we're inspecting
     * @return {@code true} if it's a snake road, {@code false} otherwise
     */
    private boolean networkWalkQualifiesAsSnakeRoad(final SnakeRoadNetworkWalk walk)
    {
        return walk.isSnakeRoad()
                && walk.getVisitedEdges().size() >= MINIMUM_EDGES_TO_QUALIFY_AS_SNAKE_ROAD
                && walk.getGreatestEncounteredValence() > MINIMUM_VALENCE_TO_QUALIFY_AS_SNAKE_ROAD;
    }

    /**
     * Recursively walks the network and uses {@link SnakeRoadNetworkWalk} to keep track of results
     * at each step
     *
     * @param current
     *            the {@link Edge} we're currently traversing from
     * @param walk
     *            the {@link SnakeRoadNetworkWalk} that contains the snake road status to this point
     * @return {@link SnakeRoadNetworkWalk} with all Snake Road information
     */
    private SnakeRoadNetworkWalk walkNetwork(final Edge current, final SnakeRoadNetworkWalk walk)
    {
        while (!walk.getDirectConnections().isEmpty())
        {
            // Grab the next available edge
            final Edge connection = walk.getDirectConnections().poll();

            // Process it
            walk.visitEdge(current, connection);

            // Add its neighbors to the next layer
            walk.populateOneLayerRemovedConnections(
                    walk.getConnectedMasterEdgeOfTheSameWay(connection));

            // If we've processed all directly connected edges, check the next layer of connections
            if (walk.getDirectConnections().isEmpty())
            {
                if (walk.getOneLayerRemovedConnections().isEmpty())
                {
                    // We've finished processing all direct connections and there are no connections
                    // in the next layer either, filter false positives and return
                    walk.filterFalsePositives();
                    return walk;
                }
                else
                {
                    // We're done processing this layer, populate the direct connections using the
                    // next layer's edges and reset next layer's edges
                    walk.addDirectConnections(walk.getOneLayerRemovedConnections());
                    walk.clearOneLayerRemovedConnections();
                }
            }
        }

        return walk;
    }

}
