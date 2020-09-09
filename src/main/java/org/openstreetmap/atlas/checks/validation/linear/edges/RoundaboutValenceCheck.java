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
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.walker.SimpleEdgeWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check ensures that roundabouts with unreasonable valences are flagged. In reference to OSM
 * Wiki, each roundabout should have greater than 1 connection as 1 connection should be tagged as a
 * turning point, and no connections is obviously not a valid way. In addition, no individual Node
 * within a roundabout should have more than one main Edge connecting from outside the roundabout.
 *
 * @author savannahostrowski
 * @author bbreithaupt
 */
public class RoundaboutValenceCheck extends BaseCheck<Long>
{

    private static final long serialVersionUID = 1L;
    private static final String BASIC_INSTRUCTION = "This roundabout has improper valence.";
    private static final String WRONG_VALENCE_INSTRUCTIONS = "This roundabout has the wrong valence. It has a valence of {0,number,#}.";
    private static final String VALENCE_OF_ONE_INSTRUCTIONS = "This roundabout should be a turning loop or turning circle.";
    private static final String MINIMUM_NODE_VALENCE_INSTRUCTION = "This roundabout has a node, {0,number,#}, that has more than 1 connections outside the roundabout.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            WRONG_VALENCE_INSTRUCTIONS, VALENCE_OF_ONE_INSTRUCTIONS,
            MINIMUM_NODE_VALENCE_INSTRUCTION, BASIC_INSTRUCTION);

    private static final double LOWER_VALENCE_THRESHOLD_DEFAULT = 2.0;
    private final double minimumValence;

    public RoundaboutValenceCheck(final Configuration configuration)
    {
        super(configuration);

        this.minimumValence = (double) configurationValue(configuration, "connections.minimum",
                LOWER_VALENCE_THRESHOLD_DEFAULT);
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
                // Make sure that we are only looking at main edges
                && ((Edge) object).isMainEdge()
                // Check for excluded highway types
                && HighwayTag.isCarNavigableHighway(object);
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
        final Set<String> instructions = new HashSet<>();
        final Set<AtlasObject> flaggedObjects = new HashSet<>();

        // Get all Edges in the roundabout
        final Set<Edge> roundaboutEdges = new SimpleEdgeWalker(edge, this.isRoundaboutEdge())
                .collectEdges();
        roundaboutEdges
                .forEach(roundaboutEdge -> this.markAsFlagged(roundaboutEdge.getIdentifier()));

        // Get all the Nodes in the roundabout
        final Set<Node> roundaboutNodes = roundaboutEdges.stream().map(Edge::start)
                .collect(Collectors.toSet());
        // Check the valence of each node and gather the total valence
        int totalRoundaboutValence = 0;
        for (final Node node : roundaboutNodes)
        {
            final int nodeValence = node.connectedEdges().stream()
                    .filter(currentEdge -> HighwayTag.isCarNavigableHighway(currentEdge)
                            && currentEdge.isMainEdge() && !JunctionTag.isRoundabout(currentEdge)
                            && !roundaboutEdges.contains(currentEdge))
                    .collect(Collectors.toSet()).size();
            // If a Node has a valance of more than 1, flag it and the roundabout
            if (nodeValence > 1)
            {
                instructions.add(this.getLocalizedInstruction(2, node.getOsmIdentifier()));
                flaggedObjects.add(node);
            }
            totalRoundaboutValence += nodeValence;
        }

        // If the totalRoundaboutValence is less than the minimum configured number of connections
        if (totalRoundaboutValence < this.minimumValence)
        {
            // If the roundabout valence is 1, this should be labelled as a turning loop instead
            if (totalRoundaboutValence == 1)
            {
                instructions.add(this.getLocalizedInstruction(1));
            }
            // Otherwise, we want to flag and given information about identifier and valence
            instructions.add(this.getLocalizedInstruction(0, totalRoundaboutValence));
        }

        if (!instructions.isEmpty())
        {
            flaggedObjects.addAll(roundaboutEdges);
            final CheckFlag flag = this.createFlag(flaggedObjects, this.getLocalizedInstruction(3));
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
     * Function for {@link SimpleEdgeWalker} that gathers connected edges that are part of a
     * roundabout.
     *
     * @return {@link Function} for {@link SimpleEdgeWalker}
     */
    private Function<Edge, Stream<Edge>> isRoundaboutEdge()
    {
        return edge -> edge.connectedEdges().stream()
                .filter(connected -> JunctionTag.isRoundabout(connected)
                        && HighwayTag.isCarNavigableHighway(connected));
    }
}
