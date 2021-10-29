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
 * @author elaineleong
 */
public class RoundaboutHighwayTagCheck extends BaseCheck<Long>
{

    private static final long serialVersionUID = 1L;
    private static final String BASIC_INSTRUCTION = "This roundabout has improper highway tag.";
    private static final String ROUNDABOUT_HIGHWAY_LEVEL_INSTRUCTION = "This way, id:{0,number,#} should have highway tag that matches the highest level of connected routes. Current:{1}. Expect:{2}.";

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(ROUNDABOUT_HIGHWAY_LEVEL_INSTRUCTION, BASIC_INSTRUCTION);

    public RoundaboutHighwayTagCheck(final Configuration configuration)
    {
        super(configuration);
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

        final Set<Edge> connectedHighwayEdges = new HashSet<>();
        final HighwayTag roundaboutHighwayTag = edge.highwayTag();

        // Get all external highway edges connected to the roundabout nodes
        for (final Node node : roundaboutNodes)
        {
            connectedHighwayEdges.addAll(node.connectedEdges().stream()
                    .filter(currentEdge -> HighwayTag.isCarNavigableHighway(currentEdge)
                            && currentEdge.isMainEdge() && !JunctionTag.isRoundabout(currentEdge)
                            && !roundaboutEdges.contains(currentEdge))
                    .collect(Collectors.toSet()));
        }

        // Find the max level of Classified HighwayTag among the external highway edges
        HighwayTag maxHighwayTagFromEdges = HighwayTag.NO;
        for (final Edge connectedHighwayEdge : connectedHighwayEdges)
        {
            if (connectedHighwayEdge.highwayTag().isMoreImportantThan(maxHighwayTagFromEdges)
                    && !connectedHighwayEdge.highwayTag()
                            .isOfEqualClassification(HighwayTag.UNCLASSIFIED))
            {
                maxHighwayTagFromEdges = connectedHighwayEdge.highwayTag();
            }
        }

        if (maxHighwayTagFromEdges.isMoreImportantThan(roundaboutHighwayTag))
        {
            instructions.add(this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                    roundaboutHighwayTag, maxHighwayTagFromEdges));
        }

        if (!instructions.isEmpty())
        {
            flaggedObjects.addAll(roundaboutEdges);
            final CheckFlag flag = this.createFlag(flaggedObjects, this.getLocalizedInstruction(1));
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
