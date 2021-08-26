package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.KeyFullyChecked;
import org.openstreetmap.atlas.geography.atlas.items.*;
import org.openstreetmap.atlas.geography.atlas.multi.MultiArea;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.*;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Checks {@link Edge}'s {@link LayerTag} and flags it if the value is unusual. Also see
 * http://wiki.openstreetmap.org/wiki/Key:layer
 *
 * @author mkalender, bbreithaupt
 */
public class UnusualLayerTagsCheck extends BaseCheck<Long>
{
    // Instructions
    public static final String INVALID_LAYER_INSTRUCTION = String.format(
            "Case 4 A layer tag must have a value in [%d, %d] and 0 should not be used explicitly.",
            LayerTag.getMinValue(), LayerTag.getMaxValue());
    public static final String JUNCTION_INSTRUCTION = "Case 2 Junctions with valid layer values "
            + "must include bridge or tunnel tags";
    public static final String FIRST_INSTRUCTION = "Case 5 Landuse feature is not on the ground";
    public static final String SECOND_INSTRUCTION = "Case 6 Natural feature underground";
    public static final String THIRD_INSTRUCTION = "Case 7 Highway underground and no tunnel";
    public static final String FOURTH_INSTRUCTION = "Case 8 Highway above ground and no bridge";
    public static final String FIFTH_INSTRUCTION = "Case 9 Waterway underground and no tunnel";
    public static final String SIXTH_INSTRUCTION = "Case 10 Waterway above ground and no bridge";
    @KeyFullyChecked(KeyFullyChecked.Type.TAGGABLE_FILTER)
    static final Predicate<Taggable> ALLOWED_TAGS;
    private static final long BRIDGE_LAYER_TAG_MAX_VALUE = LayerTag.getMaxValue();
    // Constants for bridge checks
    private static final long BRIDGE_LAYER_TAG_MIN_VALUE = 1;
    public static final String BRIDGE_INSTRUCTION = String.format(
            "Case 3 Bridges must have a layer tag set to a value in [%d, %d].", BRIDGE_LAYER_TAG_MIN_VALUE,
            BRIDGE_LAYER_TAG_MAX_VALUE);
    private static final int THREE = 3;
    private static final long TUNNEL_LAYER_TAG_MAX_VALUE = -1;
    // Constants for tunnel checks
    private static final long TUNNEL_LAYER_TAG_MIN_VALUE = LayerTag.getMinValue();
    public static final String TUNNEL_INSTRUCTION = String.format(
            "Case 1 Tunnels must have layer tags set to a value in [%d, %d].", TUNNEL_LAYER_TAG_MIN_VALUE,
            TUNNEL_LAYER_TAG_MAX_VALUE);
    public static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(TUNNEL_INSTRUCTION,
            JUNCTION_INSTRUCTION, BRIDGE_INSTRUCTION, INVALID_LAYER_INSTRUCTION, FIRST_INSTRUCTION,
            SECOND_INSTRUCTION, THIRD_INSTRUCTION, FOURTH_INSTRUCTION,FIFTH_INSTRUCTION,SIXTH_INSTRUCTION);
    // tunnel=building_passage should be excluded from eligible candidates
    private static final Predicate<Taggable> ELIGIBLE_TUNNEL_TAGS = object -> Validators
            .hasValuesFor(object, TunnelTag.class)
            && !Validators.isOfType(object, TunnelTag.class, TunnelTag.BUILDING_PASSAGE);
    private static final long serialVersionUID = 7040472721500502360L;

    /**
     * Initializes a predicate to limit check for tunnels, bridges and edges with layer tag values
     */
    static
    {
        ALLOWED_TAGS = Validators.hasValuesFor(BridgeTag.class)
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
     * one of the following tags: tunnel, bridge, layer
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return
        (
                object instanceof Node || object instanceof Area || object instanceof Line || (object instanceof Edge && ((Edge) object).isMainEdge()))
                // remove way sectioned duplicates
                && !this.isFlagged(object.getOsmIdentifier());
    }

    @Override
    protected CheckFlag createFlag(final AtlasObject object, final String instruction)
    {
        if (object instanceof Edge)
        {
            return super.createFlag(new OsmWayWalker((Edge) object).collectEdges(), instruction);
        }
        return super.createFlag(object, instruction);
    }

    /**
     * Flag an {@link Edge} if it's {@link LayerTag} value is unusual
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Retrieve layer tag value and evaluate it
        final Optional<Long> layerTagValue = LayerTag.getTaggedValue(object);
        final boolean isTagValueValid = layerTagValue.isPresent();


        // mark osm id as flagged
        this.markAsFlagged(object.getOsmIdentifier());

        if(isTagValueValid && !layerTagValue.equals(0L))
        {
            if(object.tag(LandUseTag.KEY) != null)
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(4)));
            }
            if(layerTagValue.get() < 0L
                    && object.tag(NaturalTag.KEY) != null
                    && !object.tag(NaturalTag.KEY).equalsIgnoreCase("water"))
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(5)));
            }
            if(layerTagValue.get() < 0L
                    && (object.tag(HighwayTag.KEY) != null && !object.tag(HighwayTag.KEY).equalsIgnoreCase("steps"))
                    && (object.tag(TunnelTag.KEY) == null || object.tag(TunnelTag.KEY).equalsIgnoreCase("no")))
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(6)));
            }

            if(layerTagValue.get() > 0L
                    && (object.tag(HighwayTag.KEY) != null && !object.tag(HighwayTag.KEY).equalsIgnoreCase("steps"))
                    && (object.tag(BridgeTag.KEY) == null || object.tag(BridgeTag.KEY).equalsIgnoreCase("no"))
                    && (object.tag(ManMadeTag.KEY) == null || !object.tag(ManMadeTag.KEY).equalsIgnoreCase("pier")))
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(7)));
            }

            if(object.tag(WaterwayTag.KEY) != null
                    || (object.tag(NaturalTag.KEY) != null && object.tag(NaturalTag.KEY).equalsIgnoreCase("water")))
            {
                if(layerTagValue.get() < 0L
                        && (object.tag(TunnelTag.KEY) == null ||  object.tag(TunnelTag.KEY).equalsIgnoreCase("no"))
                        &&( object.tag(LocationTag.KEY) == null || !object.tag(LocationTag.KEY).equalsIgnoreCase("underground")))
                {
                    return Optional.of(this.createFlag(object, this.getLocalizedInstruction(8)));
                }
                if(layerTagValue.get() > 0L
                        && (object.tag(BridgeTag.KEY) == null || object.tag(BridgeTag.KEY).equalsIgnoreCase("no")))
                {
                    return Optional.of(this.createFlag(object, this.getLocalizedInstruction(9)));
                }
            }
        }
        if(object instanceof Edge
                && HighwayTag.isCarNavigableHighway(object)
                && ALLOWED_TAGS.test(object))
        {
            // Rule: tunnel edges must have a layer tag value in [-5, -1]
            if (TunnelTag.isTunnel(object)
                    && (!isTagValueValid || layerTagValue.get() > TUNNEL_LAYER_TAG_MAX_VALUE
                    || layerTagValue.get() < TUNNEL_LAYER_TAG_MIN_VALUE))
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0)));
            }

            // Rule: bridge edges must have no layer tag or a layer tag value in [1, 5]
            if (BridgeTag.isBridge(object) && (!isTagValueValid || layerTagValue.get() != 0)
                    && (!isTagValueValid || layerTagValue.get() > BRIDGE_LAYER_TAG_MAX_VALUE
                    || layerTagValue.get() < BRIDGE_LAYER_TAG_MIN_VALUE))
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(2)));
            }

            // Rule: Junction edges with valid layer must include bridge or tunnel tag
            if (JunctionTag.isRoundabout(object) && isTagValueValid && layerTagValue.get() != 0L
                    && !(TunnelTag.isTunnel(object) || BridgeTag.isBridge(object)))
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(1)));
            }

            // Verify that if layer tag is present it should have a valid long value
            // We are doing this verification here (after other more specific checks) to let above
            // checks create a more specific flag
            if (!isTagValueValid)
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(THREE)));
            }
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
