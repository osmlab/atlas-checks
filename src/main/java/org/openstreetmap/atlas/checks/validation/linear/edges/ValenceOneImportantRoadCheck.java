package org.openstreetmap.atlas.checks.validation.linear.edges;

import static org.openstreetmap.atlas.checks.constants.CommonConstants.SINGLE_SPACE;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags important roads that either start or end with valance-1 nodes. This has some
 * overlap with the FloatingEdgeCheck, though does not have the same length requirement and targets
 * road segments of greater importance.
 *
 * @author brian_l_davis
 */
public class ValenceOneImportantRoadCheck extends BaseCheck<Long>
{
    public static final String DEAD_END = "This road dead ends at node {0,number,#}.";
    public static final String FLOATING = "Both entry {0,number,#} and exit {1,number,#} nodes are disconnected.";
    public static final String NO_ENTRY = "There is no way to enter this road at node {0,number,#}.";
    public static final String PREFACE = "Road [highway={0}] {1,number,#} is not connected with the surrounding road network.";
    public static final String REVERSE_HINT = "Hint: This portion of the road may be digitally reversed.";
    private static final String CONSTRUCTION_HINT = "Hint: This road may be connected to roads with highway=construction or highway=proposed.";
    private static final String ACCESS_HINT = "Hint: This road may be connected to roads with restrictive access tags.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(PREFACE, FLOATING,
            REVERSE_HINT, NO_ENTRY, DEAD_END, CONSTRUCTION_HINT, ACCESS_HINT);
    private static final EnumSet<HighwayTag> IMPORTANT_ROADS = EnumSet.of(HighwayTag.MOTORWAY,
            HighwayTag.TRUNK);
    private static final boolean OUTWARD = true;
    private static final boolean INWARD = !OUTWARD;
    private static final int ONE = 1;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final long serialVersionUID = 3216643318716503627L;

    public ValenceOneImportantRoadCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && IMPORTANT_ROADS.contains(((Edge) object).highwayTag());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        final boolean beginsWithValence1 = inboundValence(edge) == ONE;
        final boolean endsWithValence1 = outboundValence(edge) == ONE;

        if (beginsWithValence1 || endsWithValence1)
        {
            final StringList instructions = new StringList(this.getLocalizedInstruction(0,
                    edge.highwayTag().toString().toLowerCase(), edge.getIdentifier()));
            final List<Location> locations;
            boolean construction = false;
            boolean accessNo = false;

            if (beginsWithValence1)
            {
                this.markAsFlagged(edge.start().getOsmIdentifier());
                if (endsWithValence1)
                {
                    this.markAsFlagged(edge.end().getOsmIdentifier());
                    locations = Arrays.asList(edge.start().getLocation(), edge.end().getLocation());
                    instructions.add(this.getLocalizedInstruction(ONE, edge.start().getIdentifier(),
                            edge.end().getIdentifier()));

                    // edge-case, check for reversed segments
                    if (reverseOutboundValence(edge) >= ONE && reverseInboundValence(edge) >= ONE)
                    {
                        instructions.add(this.getLocalizedInstruction(2));
                    }
                    // check for Line connections
                    construction = hasConstructionConnection(edge.end());
                    accessNo = hasNoAccessConnection(edge.end());
                }
                else
                {
                    locations = Collections.singletonList(edge.start().getLocation());
                    instructions
                            .add(this.getLocalizedInstruction(THREE, edge.start().getIdentifier()));
                }
                // check for Line connections
                if (!construction)
                {
                    construction = hasConstructionConnection(edge.start());
                }
                if (!accessNo)
                {
                    accessNo = hasNoAccessConnection(edge.start());
                }
            }
            else
            {
                this.markAsFlagged(edge.end().getOsmIdentifier());
                locations = Collections.singletonList(edge.end().getLocation());
                instructions.add(this.getLocalizedInstruction(FOUR, edge.end().getIdentifier()));
                // check for Line connections
                construction = hasConstructionConnection(edge.end());
                accessNo = hasNoAccessConnection(edge.end());
            }

            if (construction)
            {
                instructions.add(this.getLocalizedInstruction(FIVE));
            }
            if (accessNo)
            {
                instructions.add(this.getLocalizedInstruction(SIX));
            }

            return Optional.of(createFlag(edge, instructions.join(SINGLE_SPACE), locations));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Helper to find the number of edges connected to a node based direction. The valence is
     * artificially high if the node already resulted in an edge being flagged.
     *
     * @param node
     *            the node to determine valence for
     * @param outward
     *            direction of navigational flow
     * @return the number connected navigable
     */
    private long directionalValence(final Node node, final Boolean outward)
    {
        if (this.isFlagged(node.getOsmIdentifier())
                || SyntheticBoundaryNodeTag.isBoundaryNode(node))
        {
            // force false negative if this node is connected to an edge already flagged or is a
            // boundary node
            return Long.MAX_VALUE;
        }
        final Set<Node> candidates = Collections.singleton(node);
        return node.connectedEdges().stream()
                // filter by flow direction to flag opposing one-ways
                .filter(otherEdge -> otherEdge.isConnectedAtStartTo(candidates) == outward)
                // count and add one for the current edge, which was just filtered out
                .count() + 1;
    }

    /**
     * Checks if a node is connected to a {@link Line} that has a highway tag with the value
     * {@code construction} or {@code proposed}.
     *
     * @param node
     *            {@link Node} to check the connection of
     * @return true if any {@link Line}s that have a highway tag with the value {@code construction}
     *         or {@code proposed} are found
     */
    private boolean hasConstructionConnection(final Node node)
    {
        return node.getAtlas()
                .linesContaining(node.getLocation(), line -> Validators.isOfType(line,
                        HighwayTag.class, HighwayTag.CONSTRUCTION, HighwayTag.PROPOSED))
                .iterator().hasNext();
    }

    /**
     * Checks if a node is connected to a {@link Line} that has a highway tag and an access tag with
     * value {@code no}.
     *
     * @param node
     *            {@link Node} to check the connection of
     * @return true if and {@link Line}s that have a highway tag and an access tag with value
     *         {@code no} are found
     */
    private boolean hasNoAccessConnection(final Node node)
    {
        return node.getAtlas()
                .linesContaining(node.getLocation(),
                        line -> Validators.hasValuesFor(line, HighwayTag.class)
                                && Validators.isOfType(line, AccessTag.class, AccessTag.NO))
                .iterator().hasNext();
    }

    private long inboundValence(final Edge edge)
    {
        return directionalValence(edge.start(), INWARD);
    }

    private long outboundValence(final Edge edge)
    {
        return directionalValence(edge.end(), OUTWARD);
    }

    private long reverseInboundValence(final Edge edge)
    {
        return directionalValence(edge.end(), INWARD);
    }

    private long reverseOutboundValence(final Edge edge)
    {
        return directionalValence(edge.start(), OUTWARD);
    }
}
