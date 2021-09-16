package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.KeyFullyChecked;
import org.openstreetmap.atlas.geography.atlas.items.*;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.*;
import org.openstreetmap.atlas.tags.LocationTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Checks {@link Edge}'s {@link LayerTag} and flags it if the value is unusual. Also see
 * http://wiki.openstreetmap.org/wiki/Key:layer
 *
 * @author mkalender, bbreithaupt, v-naydinyan
 */
public class UnusualLayerTagsCheck extends BaseCheck<Long>
{
    // Instructions
    // public static final String INVALID_LAYER_INSTRUCTION = String.format(

    public static final String LANDUSE_INSTRUCTION = "Landuse feature is not on the ground";
    public static final String NATURAL_INSTRUCTION = "Natural feature is not on the ground";
    public static final String HIGHWAY_UNDER_GROUND_INSTRUCTION = "Highway underground and no tunnel";
    public static final String HIGHWAY_ABOVE_GROUND_INSTRUCTION = "Highway above ground and no bridge";
    public static final String WATERWAY_UNDER_GROUND_INSTRUCTION = "Waterway underground and no tunnel";
    public static final String WATERWAY_ABOVE_GROUND_INSTRUCTION = "Waterway above ground and no bridge";
    public static final String LAYER_LEVEL_IS_ZERO = "Layer level should not be 0";
    @KeyFullyChecked(KeyFullyChecked.Type.TAGGABLE_FILTER)
    static final Predicate<Taggable> ALLOWED_TAGS;
    private static final long BRIDGE_LAYER_TAG_MAX_VALUE = LayerTag.getMaxValue();
    // Constants for bridge checks
    private static final long BRIDGE_LAYER_TAG_MIN_VALUE = 1;
    public static final String BRIDGE_INSTRUCTION = String.format(
            "Bridges must have a layer tag set to a value in [%d, %d].",
            BRIDGE_LAYER_TAG_MIN_VALUE, BRIDGE_LAYER_TAG_MAX_VALUE);
    private static final long TUNNEL_LAYER_TAG_MAX_VALUE = -1;
    // Constants for tunnel checks
    private static final long TUNNEL_LAYER_TAG_MIN_VALUE = LayerTag.getMinValue();
    public static final String TUNNEL_INSTRUCTION = String.format(
            "Tunnels must have layer tags set to a value in [%d, %d].",
            TUNNEL_LAYER_TAG_MIN_VALUE, TUNNEL_LAYER_TAG_MAX_VALUE);
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
        return ((object instanceof Node || object instanceof Area || object instanceof Line
                || (object instanceof Edge && ((Edge) object).isMainEdge()))
                && ALLOWED_TAGS.test(object)
                // remove way sectioned duplicates
                && !this.isFlagged(object.getOsmIdentifier()));
    }

    /**
     * A function that collected all Edges for an Edge object and flags it as a Way.
     * 
     * @param object
     * @param instruction
     * @return
     */
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

        final ManMadeTag bridgeIsManMade = ManMadeTag.BRIDGE;

        // mark osm id as flagged
        this.markAsFlagged(object.getOsmIdentifier());

        if (object.tag(LayerTag.KEY) != null && object.tag(LayerTag.KEY).equals("0"))
        {
            return Optional.of(this.createFlag(object, this.LAYER_LEVEL_IS_ZERO));
        }

        else if (!landUseNotOnGround(object, isTagValueValid).isEmpty())
        {
            return Optional
                    .of(this.createFlag(object, landUseNotOnGround(object, isTagValueValid)));
        }
        else if (!naturalNotOnGround(object, isTagValueValid).isEmpty())
        {
            return Optional
                    .of(this.createFlag(object, naturalNotOnGround(object, isTagValueValid)));
        }
        else if (!highwayNotOnGround(object, layerTagValue, isTagValueValid).isEmpty())
        {
            return Optional.of(this.createFlag(object,
                    highwayNotOnGround(object, layerTagValue, isTagValueValid)));
        }
        else if (!waterNotOnGround(object, layerTagValue, isTagValueValid).isEmpty())
        {
            return Optional.of(this.createFlag(object,
                    waterNotOnGround(object, layerTagValue, isTagValueValid)));
        }

        return Optional.empty();
    }

    /**
     * Checks if the object has a LandUse tag and a layer tag that is not 0
     * 
     * @param object
     * @return instructions for flagging
     */
    private String landUseNotOnGround(AtlasObject object, final boolean isTagValueValid)
    {
        if (isTagValueValid
                && object.tag(LandUseTag.KEY) != null
                && (!objectIsTunnel(object) || !objectIsBridge(object)))
        {
            return this.LANDUSE_INSTRUCTION;
        }
        return "";
    }

    /**
     * Checks if the object has a Natural Tag (excluding "water") and a layer tag that is not 0
     * 
     * @param object
     * @return instructions for flagging
     */
    private String naturalNotOnGround(AtlasObject object, final boolean isTagValueValid)
    {
        if (isTagValueValid
                && NaturalTag.get(object).isPresent()
                && !NaturalTag.get(object).equals("water")
                && (!objectIsTunnel(object) || !objectIsBridge(object)))
        {
            return this.NATURAL_INSTRUCTION;
        }
        return "";
    }

    /**
     * Checks if the object has a Highway Tag (excluding steps) and a layer tag. If the layer tag is
     * less than 0m the method checks for tunnel tag; if the layer tag is greater than 0 - for
     * bridge tag./
     * 
     * @param object
     * @param layerTagValue
     * @return instructions for flagging
     */
    private String highwayNotOnGround(AtlasObject object, final Optional<Long> layerTagValue,
            final boolean isTagValueValid)
    {
        if (HighwayTag.highwayTag(object).isPresent()
                && !HighwayTag.highwayTag(object).equals("steps"))
        {
            if (isTagValueValid && layerTagValue.get() < 0L && !objectIsTunnel(object))
            {
                return this.HIGHWAY_UNDER_GROUND_INSTRUCTION;
            }
            else if (isTagValueValid && layerTagValue.get() > 0L && !objectIsBridge(object)
                    && !ManMadeTag.isPier(object))
            {
                return this.HIGHWAY_ABOVE_GROUND_INSTRUCTION;
            }
            else if (!checkLayerValue(object, layerTagValue, isTagValueValid).isEmpty())
            {
                return checkLayerValue(object, layerTagValue, isTagValueValid);
            }
        }

        return "";
    }

    /**
     * Checks if a water object is not on the ground (Natural tag = "water" or a Waterway tag)
     * 
     * @param object
     * @param layerTagValue
     * @return instructions for flagging
     */
    private String waterNotOnGround(AtlasObject object, Optional<Long> layerTagValue,
            final boolean isTagValueValid)
    {
        if (object.tag(WaterwayTag.KEY) != null || (NaturalTag.get(object).isPresent()
                && object.tag(NaturalTag.KEY).equalsIgnoreCase("water")))
        {
            if (isTagValueValid && layerTagValue.get() < 0L && (!objectIsTunnel(object))
                    && (object.tag(LocationTag.KEY) == null
                            || !object.tag(LocationTag.KEY).equalsIgnoreCase("underground")))
            {
                return this.WATERWAY_UNDER_GROUND_INSTRUCTION;
            }
            else if (isTagValueValid && layerTagValue.get() > 0L && (!objectIsBridge(object)))
            {
                return this.WATERWAY_ABOVE_GROUND_INSTRUCTION;
            }
            else if (!checkLayerValue(object, layerTagValue, isTagValueValid).isEmpty())
            {
                return checkLayerValue(object, layerTagValue, isTagValueValid);
            }
        }
        return "";
    }

    /**
     * Checks if the tunnel object has the proper layer value (that it exists and is within the
     * range specified above).
     * 
     * @param object
     * @param layerTagValue
     * @param isTagValueValid
     * @return instructions for flagging
     */
    private boolean checkTunnelLayerValue(AtlasObject object, Optional<Long> layerTagValue,
            final boolean isTagValueValid)
    {
        if (objectIsTunnel(object)
                && (!isTagValueValid || layerTagValue.get() > TUNNEL_LAYER_TAG_MAX_VALUE
                        || layerTagValue.get() < TUNNEL_LAYER_TAG_MIN_VALUE))
        {
            return true;
        }
        return false;
    }

    /**
     * Checks if the bridge object has the proper layer value (that it exists and is within the
     * range specified above)
     * 
     * @param object
     * @param layerTagValue
     * @param isTagValueValid
     * @return instructions for flagging
     */
    private boolean checkBridgeLayerValue(AtlasObject object, final Optional<Long> layerTagValue,
            final boolean isTagValueValid)
    {
        if (objectIsBridge(object) && (!isTagValueValid || layerTagValue.get() != 0)
                && (!isTagValueValid || layerTagValue.get() > BRIDGE_LAYER_TAG_MAX_VALUE
                        || layerTagValue.get() < BRIDGE_LAYER_TAG_MIN_VALUE))
        {
            return true;
        }
        return false;
    }

    /**
     * Helper function that determines if the object is a bridge. It checks for both the bridge and
     * the man_made=bridge tags.
     * 
     * @param object
     * @return true if the object is a bridge, otherwise - false
     */
    private boolean objectIsBridge(AtlasObject object)
    {
        if (BridgeTag.isBridge(object) || (object.tag(ManMadeTag.KEY) != null
                && object.tag(ManMadeTag.KEY).equalsIgnoreCase("bridge")))
        {
            return true;
        }
        return false;
    }

    /**
     * Helper function that determines if the object is a tunnel. It checks for both the tunnel and
     * the man_made=tunnel tags.
     * 
     * @param object
     * @return true if the object is a tunnel, otherwise - false
     */
    private boolean objectIsTunnel(AtlasObject object)
    {
        if (TunnelTag.isTunnel(object) || (object.tag(ManMadeTag.KEY) != null
                && object.tag(ManMadeTag.KEY).equalsIgnoreCase("tunnel")))
        {
            return true;
        }
        return false;
    }

    private String checkLayerValue(AtlasObject object, final Optional<Long> layerTagValue,
            final boolean isTagValueValid)
    {
        if (checkTunnelLayerValue(object, layerTagValue, isTagValueValid))
        {
            return this.TUNNEL_INSTRUCTION;
        }
        else if (checkBridgeLayerValue(object, layerTagValue, isTagValueValid))
        {
            return this.BRIDGE_INSTRUCTION;
        }
        return "";
    }
}
