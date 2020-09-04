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
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags any Nodes tagged as highway=MINI_ROUNDABOUT that do not have very many incoming/outgoing
 * Edges as potentially tagged incorrectly.
 *
 * @author nachtm
 */
public class InvalidMiniRoundaboutCheck extends BaseCheck<Long>
{
    private static final long DEFAULT_MINIMUM_VALENCE = 6;
    private static final String MINIMUM_VALENCE_KEY = "valence.minimum";
    private static final String OTHER_EDGES_INSTRUCTION = "This Mini-Roundabout Node ({0,number,#})"
            + " has {1, number,#} connecting car-navigable Ways. Please verify that this is the most accurate tag."
            + " Possible alternate tags include, but are not limited to, traffic_calming=ISLAND, junction=CIRCULAR,"
            + " or removing this tag entirely.";
    private static final String TWO_EDGES_INSTRUCTION = "This Mini-Roundabout Node ({0,number,#}) "
            + "appears to be a turnaround. Consider changing this to highway=TURNING_LOOP or "
            + "highway=TURNING_CIRCLE.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(TWO_EDGES_INSTRUCTION,
            OTHER_EDGES_INSTRUCTION);
    private static final DirectionTag[] VALID_DIRECTIONS = { DirectionTag.CLOCKWISE,
            DirectionTag.ANTICLOCKWISE };
    private static final long serialVersionUID = 2590918466698565671L;
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
                DEFAULT_MINIMUM_VALENCE);
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
        final Collection<Edge> carNavigableEdges = this.getCarNavigableEdges(node);
        final long valence = carNavigableEdges.size();
        Optional<CheckFlag> result = Optional.empty();

        // If this Node is a turnaround, we always want to flag it.
        if (this.isTurnaround(carNavigableEdges))
        {
            result = Optional.of(this.flagNode(node, carNavigableEdges,
                    this.getLocalizedInstruction(0, node.getOsmIdentifier())));
        }
        // Otherwise, if there is not a direction tag, and the valence is less than valenceMinimum,
        // we want to flag it.
        else if (!Validators.isOfType(node, DirectionTag.class, VALID_DIRECTIONS)
                && valence < this.minimumValence && valence > 0)
        {
            result = Optional
                    .of(this.flagNode(node, carNavigableEdges, this.getLocalizedInstruction(1,
                            node.getOsmIdentifier(), getMainEdgeCount(carNavigableEdges))));
        }
        return result;
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Helper function to flag Nodes and a collection of related Edges with a particular
     * instruction.
     *
     * @param node
     *            The Node to flag.
     * @param edges
     *            The Edges (usually a set of connected Edges) to flag.
     * @param instruction
     *            The instruction to include in the flag.
     * @return The properly flagged CheckNode.
     */
    private CheckFlag flagNode(final Node node, final Collection<Edge> edges,
            final String instruction)
    {
        final CheckFlag flag = this.createFlag(node, instruction);
        edges.forEach(flag::addObject);
        return flag;
    }

    /**
     * Get all of the Edges which are connected to this Node and are car-navigable, as per
     * {@link HighwayTag#isCarNavigableHighway(Taggable)}.
     *
     * @param node
     *            The Node from which to gather connected Edges.
     * @return The Edges that are connected to this Node and are car-navigable.
     */
    private Collection<Edge> getCarNavigableEdges(final Node node)
    {
        return node.connectedEdges().stream().filter(HighwayTag::isCarNavigableHighway)
                .collect(Collectors.toSet());
    }

    /**
     * Given a list of carNavigableEdges, get the number of those edges which are main edges.
     *
     * @param carNavigableEdges
     *            The carNavigable edges we would like to filter.
     * @return The number of edges in carNavigableEdges which are main edges.
     */
    private long getMainEdgeCount(final Collection<Edge> carNavigableEdges)
    {
        return carNavigableEdges.stream().filter(Edge::isMainEdge).count();
    }

    /**
     * Determines whether or not a set of Edges is a turnaround or not, where a turnaround is
     * defined as a collection containing a main Edge and its reverse Edge. This function is only
     * guaranteed to return sensible results when carNavigableEdges is a collection of the connected
     * car-navigable Edges for a single Node.
     *
     * @param carNavigableEdges
     *            A collection of Edges. Must be a collection of the connected car-navigable Edges
     *            from a single Node, or else the results are not guaranteed to be logical.
     * @return True if the collection represents a turnaround, false otherwise.
     */
    private boolean isTurnaround(final Collection<Edge> carNavigableEdges)
    {
        return getMainEdgeCount(carNavigableEdges) == 1 && carNavigableEdges.size() == 2;
    }
}
