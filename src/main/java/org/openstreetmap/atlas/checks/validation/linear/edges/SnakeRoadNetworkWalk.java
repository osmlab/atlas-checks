package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.tags.names.ReferenceTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Angle;

/**
 * Keeps track of the network walk results for the {@link SnakeRoadCheck}
 *
 * @author mgostintsev
 */
public class SnakeRoadNetworkWalk
{
    private boolean isSnakeRoad;
    private final Optional<String> roadName;
    private final Optional<String> refTag;
    private long greatestEncounteredValence;
    private final TreeSet<AtlasObject> visitedEdges;
    private final Angle edgeHeadingDifferenceThreshold;

    // Keeps track of the directly connected edges to process. Call these friends.
    private final Queue<Edge> directConnections;

    // Keeps track of the edges one layer away from the current edge to process. Call these friends
    // of friends.
    private final Set<Edge> oneLayerRemovedConnections;

    protected SnakeRoadNetworkWalk(final Edge edge, final Angle threshold)
    {
        this.isSnakeRoad = false;
        this.roadName = edge.getTag(NameTag.KEY);
        this.refTag = edge.getTag(ReferenceTag.KEY);
        this.greatestEncounteredValence = 0;

        // We use a TreeSet here to order the edges by their Atlas identifier. This helps us easily
        // grab the first and last Edge making up the road and check connections for false
        // positives.
        this.visitedEdges = new TreeSet<>((one, two) ->
        {
            return Long.compare(one.getIdentifier(), two.getIdentifier());
        });
        this.visitedEdges.add(edge);
        this.directConnections = new LinkedList<>();
        this.oneLayerRemovedConnections = new HashSet<>();
        this.edgeHeadingDifferenceThreshold = threshold;
    }

    protected void addDirectConnections(final Set<Edge> edges)
    {
        this.directConnections.addAll(edges);
    }

    /**
     * Checks if the difference in heading between the incoming and outgoing {@link Edge}s exceeds
     * the threshold
     *
     * @param incoming
     *            The incoming {@link Edge}
     * @param outgoing
     *            The outgoing {@link Edge}
     */
    protected void checkIfEdgeHeadingDifferenceExceedsThreshold(final Edge incoming,
            final Edge outgoing)
    {
        if (!isSnakeRoad())
        {
            final Optional<Heading> incomingHeading = incoming.overallHeading();
            final Optional<Heading> outgoingHeading = outgoing.overallHeading();
            if (incomingHeading.isPresent() && outgoingHeading.isPresent()
                    && incomingHeading.get().difference(outgoingHeading.get())
                            .isGreaterThanOrEqualTo(this.edgeHeadingDifferenceThreshold))
            {
                setSnakeRoadStatus(true);
            }
        }
    }

    protected void clearOneLayerRemovedConnections()
    {
        this.oneLayerRemovedConnections.clear();
    }

    /**
     * We filter false positives for two cases. 1) If the network walk has yielded a snake road with
     * a name, we check all of the connected roads to see if that name is shared across any of them.
     * If it is, highly likely that the snake road is a false positive, and we remove the snake road
     * designation. An example is when we flag a portion of highway that behaves as a snake road. 2)
     * We do the same for ref tags to avoid flagging section of highway that exhibit snake road
     * behavior.
     */
    protected void filterFalsePositives()
    {
        if (isSnakeRoad() && (hasRoadName() || hasRefTag()))
        {
            // Gather all connected edges for the first and last edge of this road
            final Set<Edge> connections = new HashSet<>();
            connections.addAll(
                    getMainEdgesForConnectedEdgesOfDifferentWays((Edge) getVisitedEdges().first()));
            connections.addAll(
                    getMainEdgesForConnectedEdgesOfDifferentWays((Edge) getVisitedEdges().last()));

            // Check their connections for connected names and ref tags
            for (final Edge connection : connections)
            {
                final Optional<String> connectionName = connection.getTag(NameTag.KEY);
                final Optional<String> refTag = connection.getTag(ReferenceTag.KEY);
                if (connectionName.equals(getRoadName()) || refTag.equals(getRefTag()))
                {
                    setSnakeRoadStatus(false);
                    break;
                }
            }
        }
    }

    /**
     * Returns all connected main, non-visited {@link Edge}s that are a continuation of the same OSM
     * way
     *
     * @param edge
     *            the {@link Edge} from which we're seeking connections
     * @return a set of {@link Edge}s
     */
    protected Set<Edge> getConnectedMainEdgeOfTheSameWay(final Edge edge)
    {
        return edge.connectedEdges().stream()
                .filter(connection -> connection.isMainEdge()
                        && connection.getOsmIdentifier() == edge.getOsmIdentifier()
                        && !this.visitedEdges.contains(connection))
                .collect(Collectors.toSet());
    }

    protected Queue<Edge> getDirectConnections()
    {
        return this.directConnections;
    }

    protected long getGreatestEncounteredValence()
    {
        return this.greatestEncounteredValence;
    }

    protected Set<Edge> getOneLayerRemovedConnections()
    {
        return this.oneLayerRemovedConnections;
    }

    protected TreeSet<AtlasObject> getVisitedEdges()
    {
        return this.visitedEdges;
    }

    protected boolean isSnakeRoad()
    {
        return this.isSnakeRoad;
    }

    protected void populateOneLayerRemovedConnections(final Set<Edge> edges)
    {
        this.oneLayerRemovedConnections.addAll(edges);
    }

    /**
     * Adds the given {@link Edge} to the visited set and updates the greatest valence value
     *
     * @param comingFrom
     *            the {@link Edge} we are coming from
     * @param comingTo
     *            the {@link Edge} we are coming to
     */
    protected void visitEdge(final Edge comingFrom, final Edge comingTo)
    {
        this.visitedEdges.add(comingTo);
        this.setGreatestValence(comingTo.start());
        this.setGreatestValence(comingTo.end());
    }

    /**
     * Returns all connected main {@link Edge}s that are NOT part of the same way as the given
     * target {@link Edge}
     *
     * @param edge
     *            the target {@link Edge} for which we're seeking connections
     * @return the {@link Set} of {@link Edge}s we found
     */
    private Set<Edge> getMainEdgesForConnectedEdgesOfDifferentWays(final Edge edge)
    {
        return Iterables.stream(edge.connectedEdges()).filter(candidate -> candidate.isMainEdge()
                && candidate.getOsmIdentifier() != edge.getOsmIdentifier()).collectToSet();
    }

    private Optional<String> getRefTag()
    {
        return this.refTag;
    }

    private Optional<String> getRoadName()
    {
        return this.roadName;
    }

    private boolean hasRefTag()
    {
        return this.refTag.isPresent();
    }

    private boolean hasRoadName()
    {
        return this.roadName.isPresent();
    }

    private void setGreatestValence(final Node node)
    {
        final long valence = node.valence();
        if (valence > this.greatestEncounteredValence)
        {
            this.greatestEncounteredValence = valence;
        }
    }

    private void setSnakeRoadStatus(final boolean value)
    {
        this.isSnakeRoad = value;
    }

}
