package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
            + "has the wrong valence. It has a valence of {1, number, integer}. It has "
            + "{2, number, integer} roundabout edges and {3, number, integer} global edges";
    public static final String VALENCE_OF_ONE_INSTRUCTIONS = "This feature, {0,number,#},"
            + " should not be labelled as a roundabout. "
            + "The junction should be a turning loop or turning circle.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(WRONG_VALENCE_INSTRUCTIONS, VALENCE_OF_ONE_INSTRUCTIONS);

    private static final double LOWER_VALENCE_THRESHOLD = 2.0;
    private static final double UPPER_VALENCE_THRESHOLD = 10.0;
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
                LOWER_VALENCE_THRESHOLD);
        this.maximumValence = (double) configurationValue(configuration, "connections.maximum",
                UPPER_VALENCE_THRESHOLD);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // We check that the object is an instance of Edge
        return object instanceof Edge
                // And that the Edge has not already been marked as flagged
                && !this.isFlagged(object.getIdentifier())
                // Make sure that the edges are instances of roundabout
                && JunctionTag.isRoundabout(object);
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
        final Map<Long, Edge> roundaboutEdges = new HashMap<>();

        // Get all edges in the roundabout
        getAllRoundaboutEdges(edge, roundaboutEdges);
        final int totalRoundaboutValence;
        final int roundaboutEdgeCount = roundaboutEdges.size();
        final Set<Edge> connectedEdges = new HashSet<>();

        final Iterator iterator = roundaboutEdges.entrySet().iterator();

        if (!iterator.hasNext())
        {
            return Optional.empty();
        }

        while (iterator.hasNext())
        {
            final Map.Entry pair = (Map.Entry) iterator.next();
            final Edge roundaboutEdge = (Edge) pair.getValue();

            // Adds only car navigable master edges that are connected to the roundabout toward
            // the connectedEdges set
            connectedEdges.addAll(roundaboutEdge.connectedEdges().stream()
                    .map(Edge::getMasterEdge)
                    .filter(e -> HighwayTag.isCarNavigableHighway(e) || JunctionTag.isRoundabout(e))
                    .collect(Collectors.toSet()));
        }

        totalRoundaboutValence = connectedEdges.size() - roundaboutEdgeCount;

        // If the totalRoundaboutValence is less than 2 or greater than or equal to 10
        if (totalRoundaboutValence < this.minimumValence
                || totalRoundaboutValence >= this.maximumValence)
        {
            this.markAsFlagged(object.getIdentifier());

            // If the roundabout valence is 1, this should be labelled as a turning loop instead
            if (totalRoundaboutValence == 1)
            {
                return Optional.of(this.createFlag(connectedEdges,
                        this.getLocalizedInstruction(1, edge.getOsmIdentifier())));
            }
            // Otherwise, we want to flag and given information about identifier and valence
            return Optional.of(this.createFlag(connectedEdges,
                    this.getLocalizedInstruction(0, edge.getOsmIdentifier(), totalRoundaboutValence,
                            roundaboutEdgeCount, connectedEdges.size())));
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
     * @param roundaboutEdges
     *            A map that contains key, value pairs of Edge identifiers and all associated Edge
     *            data
     */
    private void getAllRoundaboutEdges(final Edge edge, final Map<Long, Edge> roundaboutEdges)
    {
        // Initialize a queue to add yet to be processed connected edges to
        final Queue<Edge> queue = new LinkedList<>();

        // Mark the current node as visited and enqueue it
        this.markAsFlagged(edge.getIdentifier());
        queue.add(edge);

        // As long as the queue is not empty
        while (queue.size() != 0)
        {
            // Dequeue a connected edge and add it to the roundaboutEdges
            final Edge e = queue.poll();
            if (e.getIdentifier() < 0)
            {
                continue;
            }
            roundaboutEdges.put(e.getIdentifier(), e);

            // Get the edges connected to the edge e as an iterator
            final Iterator<Edge> iterator = e.connectedEdges().iterator();
            while (iterator.hasNext())
            {
                final Edge connectedEdge = iterator.next();
                final Long edgeId = connectedEdge.getIdentifier();

                if (JunctionTag.isRoundabout(connectedEdge))
                {
                    if (!roundaboutEdges.containsKey(edgeId))

                    {
                        this.markAsFlagged(edgeId);
                        queue.add(connectedEdge);
                    }
                }
            }
        }
    }
}
