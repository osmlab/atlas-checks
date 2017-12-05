package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.OneWayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags roundabout edges that are either bidirectional or have a node with less than or equal to
 * {@link RoundaboutClosedLoopCheck#MINIMUM_VALENCE} connections. See
 * http://wiki.openstreetmap.org/wiki/Key:oneway for more information about one-way and roundabouts
 *
 * @author mkalender
 */
public class RoundaboutClosedLoopCheck extends BaseCheck<Long>
{
    // Instructions
    public static final String ONE_WAY_INSTRUCTION = "This roundabout edge is not one-way.";
    // Highway tags referring roundabouts
    private static final EnumSet<HighwayTag> HIGHWAY_TAGS_FOR_ROUNDABOUTS = EnumSet
            .of(HighwayTag.MINI_ROUNDABOUT, HighwayTag.TURNING_CIRCLE, HighwayTag.TURNING_LOOP);
    // Minimum valence for a node to not to flag
    private static final int MINIMUM_VALENCE = 2;
    public static final String MINIMUM_VALENCE_INSTRUCTION = String.format(
            "This roundabout edge has an end node that has less than %d connections.",
            MINIMUM_VALENCE);
    public static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(ONE_WAY_INSTRUCTION,
            MINIMUM_VALENCE_INSTRUCTION);
    private static final long serialVersionUID = -3648610800112828238L;

    private static boolean aConnectedNodeHasValenceLessThan(final Edge edge, final int valence)
    {
        return edge
                // go through each connected node of given edge
                .connectedNodes().stream()
                // check if any of them has less than given valence value
                .anyMatch(node -> node.connectedEdges().stream()
                        // counting only master edge connections
                        .filter(Edge::isMasterEdge).count() < valence);
    }

    /**
     * Checks whether given {@link AtlasObject} is a form of roundabout
     * http://wiki.openstreetmap.org/wiki/Tag:junction%3Droundabout#Possible_misinterpretations
     *
     * @param object
     *            entity to check
     * @return {@code true} if the given {@link AtlasObject} is a roundabout
     */
    private static boolean isAFormOfRoundabout(final AtlasObject object)
    {
        final Optional<HighwayTag> highwayTag = Validators.from(HighwayTag.class, object);
        return JunctionTag.isRoundabout(object) || (highwayTag.isPresent()
                && HIGHWAY_TAGS_FOR_ROUNDABOUTS.contains(highwayTag.get()));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public RoundaboutClosedLoopCheck(final Configuration configuration)
    {
        super(configuration);
    }

    /**
     * Validates if given {@link AtlasObject} is actually an {@link Edge} and is a roundabout and
     * also corresponding OSM identifier shouldn't be flagged before (this is for avoiding duplicate
     * flags)
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && isAFormOfRoundabout(object)
                && !this.isFlagged(object.getOsmIdentifier());
    }

    /**
     * Flags an {@link Edge} if it is not one-way or it is connected to end nodes whose valence is
     * less than {@link RoundaboutClosedLoopCheck#MINIMUM_VALENCE}. See {@link OneWayTag#isTwoWay}
     * for more details on how one-way checks.
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;

        // Rule: a roundabout edge must be one-way
        // Since Atlas creates two edges for a bidirectional in OSM,
        // a one-way road must be the master edge
        // Rule: a roundabout is assumed to be one-way by default
        // If a one-way tag explicitly says otherwise, then flag it
        if (!edge.isMasterEdge() || OneWayTag.isExplicitlyTwoWay(edge))
        {
            this.markAsFlagged(object.getOsmIdentifier());
            return Optional.of(createFlag(object, this.getLocalizedInstruction(0)));
        }

        // Rule: a roundabout edge should never originate/terminate at a valence-1 node
        // Verify this edge has a node with valence less than given minimum value
        if (aConnectedNodeHasValenceLessThan(edge, MINIMUM_VALENCE))
        {
            this.markAsFlagged(object.getOsmIdentifier());
            return Optional.of(createFlag(object, this.getLocalizedInstruction(1)));
        }

        return Optional.empty();
    }
}
