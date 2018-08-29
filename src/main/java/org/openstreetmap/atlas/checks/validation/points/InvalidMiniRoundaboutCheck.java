package org.openstreetmap.atlas.checks.validation.points;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.DirectionTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags any nodes tagged as highway=MINI_ROUNDABOUT that do not have very many incoming/outgoing
 * edges as potentially tagged incorrectly.
 *
 * @author nachtm
 */
public class InvalidMiniRoundaboutCheck extends BaseCheck<Long>
{

    private static final long DEFAULT_VALENCE = 6;
    private static final String MINIMUM_VALENCE_KEY = "minimumValence";
    private static final String OTHER_EDGES_INSTRUCTION = "This Mini-Roundabout Node ({0,number,#})"
            + " has {1, number,#} connecting car-navigable edges. Consider changing this.";
    private static final String TWO_EDGES_INSTRUCTION = "This Mini-Roundabout Node ({0,number,#}) "
            + "has 2 connecting car-navigable edges. Consider changing this to highway=TURNING_LOOP or "
            + "highway=TURNING_CIRCLE.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(TWO_EDGES_INSTRUCTION,
            OTHER_EDGES_INSTRUCTION);
    private static final DirectionTag[] VALID_DIRECTIONS = { DirectionTag.CLOCKWISE,
            DirectionTag.ANTICLOCKWISE };
    private final long minimumValence;

    /**
     * Construct an InvalidMiniRoundaboutCheck with the given configuration values.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public InvalidMiniRoundaboutCheck(final Configuration configuration)
    {
        super(configuration);
        this.minimumValence = this.configurationValue(configuration, MINIMUM_VALENCE_KEY,
                DEFAULT_VALENCE);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Node
                && Validators.isOfType(object, HighwayTag.class, HighwayTag.MINI_ROUNDABOUT);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Node node = (Node) object;
        final Collection<Edge> carNavigableEdges = getCarNavigableEdges(node);
        final long valence = carNavigableEdges.size();
        final Optional<CheckFlag> result;

        if (isTurnaround(carNavigableEdges))
        {
            result = Optional.of(flagNode(node, carNavigableEdges,
                    this.getLocalizedInstruction(0, node.getOsmIdentifier())));
        }
        else if (!Validators.isOfType(node, DirectionTag.class, VALID_DIRECTIONS)
                && valence < minimumValence && valence > 0)
        {
            result = Optional.of(flagNode(node, carNavigableEdges,
                    this.getLocalizedInstruction(1, node.getOsmIdentifier(), valence)));
        }
        else
        {
            result = Optional.empty();
        }
        return result;
    }

    /**
     * Helper function to flag nodes and a collection of related edges with a particular
     * instruction.
     *
     * @param node The node to flag.
     * @param edges The edges (usually a set of connected edges) to flag.
     * @param instruction The instruction to include in the flag.
     * @return The properly flagged CheckNode.
     */
    private CheckFlag flagNode(final Node node, final Collection<Edge> edges,
            final String instruction)
    {
        final CheckFlag flag = this.createFlag(node, instruction);
        Iterables.stream(edges).forEach(flag::addObject);
        return flag;
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Determines whether or not a set of edges is a turnaround or not, where a turnaround is defined
     * as a collection containing a master edge and its reverse edge. This function is only guaranteed
     * to return sensible results when carNavigableEdges is a subset of the connected edges to a single
     * node.
     *
     * @param carNavigableEdges A collection of edges. Must be a subset of the connected edges to a
     *                          single node, or else the results are not guaranteed to be logical.
     * @return True if the collection represents a turnaround, false otherwise.
     */
    private boolean isTurnaround(final Collection<Edge> carNavigableEdges)
    {
        final long masterEdgeCount = carNavigableEdges.stream().filter(Edge::isMasterEdge).count();
        return masterEdgeCount == 1 && carNavigableEdges.size() == 2;
    }

    /**
     * Get all of the edges which are connected to this node and are car-navigable, as per
     * {@link HighwayTag#isCarNavigableHighway(Taggable)}.
     *
     * @param node The node from which to gather connected edges.
     * @return The edges that are connected to this node and are car-navigable.
     */
    private Collection<Edge> getCarNavigableEdges(final Node node)
    {
        return node.connectedEdges().stream().filter(HighwayTag::isCarNavigableHighway)
                .collect(Collectors.toSet());
    }
}
