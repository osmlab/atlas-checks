package org.openstreetmap.atlas.checks.validation.points;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.RailwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check verifies lone {@link Node}s with highway tag that they follow the tagging principles.
 *
 * @author mm-ciub
 */
public class LoneNodeCheck extends BaseCheck<Long>
{

    private static final long serialVersionUID = -1489101405354234053L;

    private static final List<String> VALID_HIGHWAY_TAGS_NODE = List.of("bus_stop", "crossing",
            "elevator", "emergency_bay", "emergency_access_point", "give_way", "phone", "milestone",
            "mini_roundabout", "motorway_junction", "passing_place", "platform", "rest_area",
            "speed_camera", "street_lamp", "services", "stop", "traffic_mirror", "traffic_signals",
            "trailhead", "turning_circle", "turning_loop", "toll_gantry");

    private static final String UNEXPECTED_HIGHWAY_TAG_VALUE_INSTRUCTION = "This node {0,number,#} has a highway tag with an unexpected value: ''{1}''. Please make sure this is a valid highway tag for a node.";
    private static final String LONE_NODE_INSTRUCTION = "This node {0,number,#} has a Highway tag but is not part of any way that has a highway or railway tag. Either add such a tag to the appropriate parent or remove the highway tag from the node.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(UNEXPECTED_HIGHWAY_TAG_VALUE_INSTRUCTION, LONE_NODE_INSTRUCTION);

    /**
     * Default constructor.
     *
     * @param configuration
     *            the JSON configuration for this check
     */

    public LoneNodeCheck(final Configuration configuration)
    {
        super(configuration);
    }

    /**
     * The object is valid if it is of type Node and has a highway tag.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Node && object.getTag(HighwayTag.KEY).isPresent()
                && !this.isFlagged(object.getOsmIdentifier());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Node node = (Node) object;
        this.markAsFlagged(node.getOsmIdentifier());

        final Optional<String> highwayValue = object.getTag(HighwayTag.KEY);
        final List<String> instructions = new ArrayList<>();
        if (highwayValue.isPresent() && !VALID_HIGHWAY_TAGS_NODE.contains(highwayValue.get()))
        {
            instructions.add(
                    this.getLocalizedInstruction(0, object.getOsmIdentifier(), highwayValue.get()));
        }

        if (this.isLoneNode(node))
        {
            instructions.add(this.getLocalizedInstruction(1, object.getOsmIdentifier()));
        }
        return instructions.isEmpty() ? Optional.empty()
                : Optional.of(this.createFlag(object, instructions));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Check if there are no connected edges with highway or railway tag.
     * 
     * @param node
     *            to be checked
     * @return true if no edge is found
     */
    private boolean isLoneNode(final Node node)
    {
        return node.connectedEdges().stream()
                .noneMatch(edge -> edge.getTag(HighwayTag.KEY).isPresent()
                        || edge.getTag(RailwayTag.KEY).isPresent());

    }

}
