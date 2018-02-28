package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.*;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
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
    public static final String WRONG_VALENCE_INSTRUCTIONS = "This roundabout, {0}, has the "
            + "wrong valence";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(WRONG_VALENCE_INSTRUCTIONS);

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    public RoundaboutValenceCheck(final Configuration configuration)
    {
        super(configuration);

    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // We check that the object is an instance of Edge
        return object instanceof Edge
                // and that the Edge has not already been marked as flagged
                && !this.isFlagged(object.getIdentifier())
                // make sure that the edges are instances of roundabout
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

        // get all edges in the roundabout
        getAllRoundaboutEdges(edge, roundaboutEdges);
        int totalRoundaboutValence = 0;
        Set<Edge> connectedRoundaboutEdges = new HashSet<>();
        Set<Edge> connectedEdges = new HashSet<>();

        Iterator iterator = roundaboutEdges.entrySet().iterator();

        if (!iterator.hasNext())
        {
            return Optional.empty();
        }

            while (iterator.hasNext())
            {

                Map.Entry pair = (Map.Entry) iterator.next();
                Edge roundaboutEdge = (Edge) pair.getValue();

                connectedRoundaboutEdges.addAll(connectedRoundaboutEdges(roundaboutEdge));
                connectedEdges.addAll(roundaboutEdge.connectedEdges());

            }

        totalRoundaboutValence = connectedEdges.size() - connectedRoundaboutEdges.size();

        if (totalRoundaboutValence < 2 || totalRoundaboutValence >= 10)
        {
            this.markAsFlagged(object.getIdentifier());
            return Optional.of(this.createFlag(connectedRoundaboutEdges,
                    this.getLocalizedInstruction(0, edge.getOsmIdentifier())));
        }
        else
        {
            return Optional.empty();
        }
    }

    private Set<Edge> connectedRoundaboutEdges(Edge edge)
    {
        return edge.connectedEdges().stream().filter(e ->
            JunctionTag.isRoundabout(e)).collect(Collectors.toSet());
    }

    private void getAllRoundaboutEdges(Edge edge, Map<Long, Edge> roundaboutEdges)
    {
        Queue<Edge> queue = new LinkedList<>();

        // Mark the current node as visited and enqueue it
        this.markAsFlagged(edge.getIdentifier());
        queue.add(edge);

        while (queue.size() != 0)
        {
            // Dequeue an edge and add it to the roundaboutEdges
            Edge e = queue.poll();
            roundaboutEdges.put(e.getIdentifier(), e);

            // Get the edges connected to the edge e as an iterator
            final Iterator<Edge> iterator = e.connectedEdges().iterator();
            while (iterator.hasNext())
            {
                Edge connectedEdge = iterator.next();
                final Long id = connectedEdge.getIdentifier();

                if (JunctionTag.isRoundabout(connectedEdge))
                {
                    if (!this.isFlagged(id))
                    {
                        this.markAsFlagged(id);
                        queue.add(connectedEdge);
                    }
                }
            }
        }
    }

    private boolean isSingleFeature(final String osmId, final List<Long> edgeIds)
    {
        final List matches = edgeIds.stream()
                // returns true is roundabout Id contains full osmId value
                .filter(id -> Long.toString(id).contains(osmId)).collect(Collectors.toList());

        // if each value passes our test, this roundabout is a single feature
        return matches.size() == edgeIds.size();
    }
}
