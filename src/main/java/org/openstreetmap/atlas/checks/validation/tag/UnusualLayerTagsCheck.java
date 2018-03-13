package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.TunnelTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Checks {@link Edge}'s {@link LayerTag} and flags it if the value is unusual. Also see
 * http://wiki.openstreetmap.org/wiki/Key:layer
 *
 * @author mkalender
 */
public class UnusualLayerTagsCheck extends BaseCheck<Long>
{
    // Instructions
    public static final String INVALID_LAYER_INSTRUCTION = String.format(
            "A layer tag must have a value in [%d, %d] and 0 should not be used explicitly.",
            LayerTag.getMinValue(), LayerTag.getMaxValue());
    public static final String JUNCTION_INSTRUCTION = "Junction edges with valid layer value "
            + "must include bridge or tunnel tags";
    private static final Predicate<Taggable> ALLOWED_TAGS;
    private static final long BRIDGE_LAYER_TAG_MAX_VALUE = LayerTag.getMaxValue();
    // Constants for bridge checks
    private static final long BRIDGE_LAYER_TAG_MIN_VALUE = 1;
    public static final String BRIDGE_INSTRUCTION = String.format(
            "Bridge edges must have no layer tag or a layer tag set to a value in [%d, %d].",
            BRIDGE_LAYER_TAG_MIN_VALUE, BRIDGE_LAYER_TAG_MAX_VALUE);
    private static final int THREE = 3;
    private static final long TUNNEL_LAYER_TAG_MAX_VALUE = -1;
    // Constants for tunnel checks
    private static final long TUNNEL_LAYER_TAG_MIN_VALUE = LayerTag.getMinValue();
    public static final String TUNNEL_INSTRUCTION = String.format(
            "Tunnel edges must have layer tag set to a value in [%d, %d].",
            TUNNEL_LAYER_TAG_MIN_VALUE, TUNNEL_LAYER_TAG_MAX_VALUE);
    public static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(TUNNEL_INSTRUCTION,
            JUNCTION_INSTRUCTION, BRIDGE_INSTRUCTION, INVALID_LAYER_INSTRUCTION);
    // tunnel=building_passage should be excluded from eligible candidates
    private static final Predicate<Taggable> ELIGIBLE_TUNNEL_TAGS = object -> Validators
            .hasValuesFor(object, TunnelTag.class)
            && !(Validators.isOfType(object, TunnelTag.class, TunnelTag.BUILDING_PASSAGE));
    private static final long serialVersionUID = 7040472721500502360L;

    /**
     * Initializes a predicate to limit check for tunnels, junctions, bridges and edges with layer
     * tag values
     */
    static
    {
        ALLOWED_TAGS = Validators.hasValuesFor(JunctionTag.class)
                .or(Validators.hasValuesFor(BridgeTag.class))
                .or(Validators.hasValuesFor(LayerTag.class)).or(ELIGIBLE_TUNNEL_TAGS);
    }

    /**
     * Configuration required to construct any Check
     *
     * @param configuration
     *            {@link Configuration}
     */
    public UnusualLayerTagsCheck(final Configuration configuration)
    {
        super(configuration);
    }

    /**
     * Validate if given {@link AtlasObject} is actually an {@link Edge} and make sure the edge has
     * one of the following tags: tunnel, junction, bridge, layer
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge
                // must be highway navigable
                && HighwayTag.isCarNavigableHighway(object)
                // and one of the following
                && ALLOWED_TAGS.test(object)
                // removes one of two bi-directional edge candidates
                && ((Edge) object).isMasterEdge()
                // remove way sectioned duplicates
                && !this.isFlagged(object.getOsmIdentifier());
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Flag an {@link Edge} if it's {@link LayerTag} value is unusual
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Retrieve layer tag value and evaluate it
        final Optional<Long> layerTagValue = LayerTag.getTaggedOrImpliedValue(object, 0L);
        final boolean isTagValueValid = layerTagValue.isPresent();

        // mark osm id as flagged
        this.markAsFlagged(object.getOsmIdentifier());

        // Rule: tunnel edges must have a layer tag value in [-5, -1]
        if (TunnelTag.isTunnel(object)
                && (!isTagValueValid || layerTagValue.get() > TUNNEL_LAYER_TAG_MAX_VALUE
                        || layerTagValue.get() < TUNNEL_LAYER_TAG_MIN_VALUE))
        {
            return Optional.of(createFlag(object, this.getLocalizedInstruction(0)));
        }

        // Rule: bridge edges must have no layer tag or a layer tag value in [1, 5]
        if (BridgeTag.isBridge(object) && (!isTagValueValid || layerTagValue.get() != 0)
                && (!isTagValueValid || layerTagValue.get() > BRIDGE_LAYER_TAG_MAX_VALUE
                        || layerTagValue.get() < BRIDGE_LAYER_TAG_MIN_VALUE))
        {
            return Optional.of(createFlag(object, this.getLocalizedInstruction(2)));
        }

        // Rule: Junction edges with valid layer must include bridge or tunnel tag
        if (JunctionTag.isRoundabout(object) && (isTagValueValid && layerTagValue.get() != 0L)
                && !(TunnelTag.isTunnel(object) || BridgeTag.isBridge(object)))
        {
            return Optional.of(createFlag(object, this.getLocalizedInstruction(1)));
        }

        // Verify that if layer tag is present it should have a valid long value
        // We are doing this verification here (after other more specific checks) to let above
        // checks create a more specific flag
        if (!isTagValueValid)
        {
            return Optional.of(createFlag(object, this.getLocalizedInstruction(THREE)));
        }

        return Optional.empty();
    }
}
