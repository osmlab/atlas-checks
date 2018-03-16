package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check ensures that roundabouts with unreasonable valences are flagged. In reference to OSM
 * Wiki, each roundabout should have greater than 1 connection as 1 connection should be tagged as a
 * turning point, and no connections is obviously not a valid way.
 *
 * @author savannahostrowski
 */
public class RoundaboutValenceCheck extends BaseCheck
{

    private static final long serialVersionUID = 1L;
    public static final String WRONG_VALENCE_INSTRUCTIONS = "This roundabout, {0,number,#}, "
            + "has the wrong valence. It has a valence of {1,number,#}.";
    public static final String VALENCE_OF_ONE_INSTRUCTIONS = "This feature, {0,number,#},"
            + " should not be labelled as a roundabout. "
            + "This feature should be a turning loop or turning circle.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(WRONG_VALENCE_INSTRUCTIONS, VALENCE_OF_ONE_INSTRUCTIONS);

    private static final double LOWER_VALENCE_THRESHOLD_DEFAULT = 2.0;
    private static final double UPPER_VALENCE_THRESHOLD_DEFAULT = 10.0;
    private final double minimumValence;
    private final double maximumValence;

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    public RoundaboutValenceCheck(final Configuration configuration)
    {
        super(configuration);

        this.minimumValence = (double) configurationValue(configuration, "connections.minimum",
                LOWER_VALENCE_THRESHOLD_DEFAULT);
        this.maximumValence = (double) configurationValue(configuration, "connections.maximum",
                UPPER_VALENCE_THRESHOLD_DEFAULT);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // We check that the object is an instance of Edge
        return object instanceof Edge
                // Make sure that the edges are instances of roundabout
                && JunctionTag.isRoundabout(object)
                // And that the Edge has not already been marked as flagged
                && !this.isFlagged(object.getIdentifier())
                // Make sure that we are only looking at master edges
                && ((Edge) object).isMasterEdge();
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

        // Get all edges in the roundabout
        final Set<Edge> roundaboutEdges = getAllRoundaboutEdges(edge);

        final Set<Edge> connectedEdges = roundaboutEdges.stream()
                .flatMap(roundaboutEdge -> roundaboutEdge.connectedEdges().stream())
                .filter(HighwayTag::isCarNavigableHighway).filter(Edge::isMasterEdge)
                .filter(currentEdge -> !JunctionTag.isRoundabout(currentEdge))
                .filter(currentEdge -> !roundaboutEdges.contains(currentEdge))
                .collect(Collectors.toSet());
        final int totalRoundaboutValence = connectedEdges.size();

        // If the totalRoundaboutValence is less than the minimum configured number of connections
        // or greater than or equal to the maximum configured number of connections
        if (totalRoundaboutValence < this.minimumValence
                || totalRoundaboutValence > this.maximumValence)
        {
            this.markAsFlagged(object.getIdentifier());

            // If the roundabout valence is 1, this should be labelled as a turning loop instead
            if (totalRoundaboutValence == 1)
            {
                return Optional.of(this.createFlag(roundaboutEdges,
                        this.getLocalizedInstruction(1, edge.getOsmIdentifier())));
            }
            // Otherwise, we want to flag and given information about identifier and valence
            return Optional.of(this.createFlag(roundaboutEdges, this.getLocalizedInstruction(0,
                    edge.getOsmIdentifier(), totalRoundaboutValence)));
        }
        // If the totalRoundaboutValence is not unusual, we don't flag the object
        else
        {
            return Optional.empty();
        }
    }

    /**
     * This method gets all edges in a roundabout given one edge in that roundabout
     * 
     * @param edge
     *            An Edge object known to be a roundabout edge
     * @return A set of edges in the roundabout
     */
    private Set<Edge> getAllRoundaboutEdges(final Edge edge)
    {
        final Set<Edge> roundaboutEdges = new HashSet<>();

        // Initialize a queue to add yet to be processed connected edges to
        final Queue<Edge> queue = new LinkedList<>();

        // Mark the current Edge as visited and enqueue it
        this.markAsFlagged(edge.getIdentifier());
        queue.add(edge);

        // As long as the queue is not empty
        while (!queue.isEmpty())
        {
            // Dequeue a connected edge and add it to the roundaboutEdges
            final Edge currentEdge = queue.poll();

            roundaboutEdges.add(currentEdge);

            // Get the edges connected to the edge e as an iterator
            final Set<Edge> connectedEdges = currentEdge.connectedEdges();

            for (final Edge connectedEdge : connectedEdges)
            {
                final Long edgeId = connectedEdge.getIdentifier();

                if (JunctionTag.isRoundabout(connectedEdge)
                        && !roundaboutEdges.contains(connectedEdge))

                {
                    this.markAsFlagged(edgeId);
                    queue.add(connectedEdge);
                }
            }
        }
        return roundaboutEdges;
    }
}
