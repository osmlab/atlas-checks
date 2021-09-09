package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.Opt;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.KeyFullyChecked;
import org.openstreetmap.atlas.geography.atlas.items.*;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.*;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import javax.swing.text.html.Option;

/**
 * Checks {@link Edge}'s {@link LayerTag} and flags it if the value is unusual. Also see
 * http://wiki.openstreetmap.org/wiki/Key:layer
 *
 * @author mkalender, bbreithaupt, v-naydinyan
 */
public class UnusualLayerTagsCheck extends BaseCheck<Long>
{
    // Instructions
    public static final String INVALID_LAYER_INSTRUCTION = String.format(
            "Case 4 A layer tag must have a value in [%d, %d] and 0 should not be used explicitly.",
            LayerTag.getMinValue(), LayerTag.getMaxValue());
    public static final String JUNCTION_INSTRUCTION = "Case 2 Junctions with valid layer values "
            + "must include bridge or tunnel tags";
    public static final String LANDUSE_INSTRUCTION = "Case 5 Landuse feature is not on the ground";
    public static final String NATURAL_UNDER_GROUND_INSTRUCTION = "Case 6 Natural feature underground";
    public static final String HIGHWAY_UNDER_GROUND_INSTRUCTION = "Case 7 Highway underground and no tunnel";
    public static final String HIGHWAY_ABOVE_GROUND_INSTRUCTION = "Case 8 Highway above ground and no bridge";
    public static final String WATERWAY_UNDER_GROUND_INSTRUCTION = "Case 9 Waterway underground and no tunnel";
    public static final String WATERWAY_ABOVE_GROUND_INSTRUCTION = "Case 10 Waterway above ground and no bridge";
    @KeyFullyChecked(KeyFullyChecked.Type.TAGGABLE_FILTER)
    static final Predicate<Taggable> ALLOWED_TAGS;
    private static final long BRIDGE_LAYER_TAG_MAX_VALUE = LayerTag.getMaxValue();
    // Constants for bridge checks
    private static final long BRIDGE_LAYER_TAG_MIN_VALUE = 1;
    public static final String BRIDGE_INSTRUCTION = String.format(
            "Case 3 Bridges must have a layer tag set to a value in [%d, %d].", BRIDGE_LAYER_TAG_MIN_VALUE,
            BRIDGE_LAYER_TAG_MAX_VALUE);
    private static final long TUNNEL_LAYER_TAG_MAX_VALUE = -1;
    // Constants for tunnel checks
    private static final long TUNNEL_LAYER_TAG_MIN_VALUE = LayerTag.getMinValue();
    public static final String TUNNEL_INSTRUCTION = String.format(
            "Case 1 Tunnels must have layer tags set to a value in [%d, %d].", TUNNEL_LAYER_TAG_MIN_VALUE,
            TUNNEL_LAYER_TAG_MAX_VALUE);
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
                (object instanceof Node || object instanceof Area || object instanceof Line || (object instanceof Edge && ((Edge) object).isMainEdge()))
                && ALLOWED_TAGS.test(object)
                // remove way sectioned duplicates
                && !this.isFlagged(object.getOsmIdentifier()));
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

        if(!checkTunnelLayerValue(object, layerTagValue, isTagValueValid).isEmpty()) {
            return Optional.of(this.createFlag(object, checkTunnelLayerValue(object, layerTagValue, isTagValueValid)));
        }
        else if(!checkBridgeLayerValue(object, layerTagValue, isTagValueValid).isEmpty())
        {
            return Optional.of(this.createFlag(object, checkBridgeLayerValue(object, layerTagValue, isTagValueValid)));
        }

        if(isTagValueValid && !layerTagValue.equals(0L)) {
            if(!landUseNotOnGround(object).isEmpty())
            {
                return Optional.of(this.createFlag(object, landUseNotOnGround(object)));
            }

            else if(!naturalNotOnGround(object).isEmpty())
            {
                return Optional.of(this.createFlag(object, naturalNotOnGround(object)));
            }
            else if(!highwayNotOnGround(object, layerTagValue).isEmpty())
            {
                return Optional.of(this.createFlag(object, highwayNotOnGround(object, layerTagValue)));
            }
            else if(!waterNotOnGround(object, layerTagValue).isEmpty())
            {
                return Optional.of(this.createFlag(object, waterNotOnGround(object, layerTagValue)));
            }

        }
        else if(!isTagValueValid)
        {
            return Optional.of(this.createFlag(object, this.INVALID_LAYER_INSTRUCTION));
        }

        return Optional.empty();
    }

    /**
     * Checks if the object has a LandUse tag and a layer tag that is not 0
     * @param object
     * @return
     */
    private String landUseNotOnGround(AtlasObject object)
    {
        if (object.tag(LandUseTag.KEY) != null)
        {
            return this.LANDUSE_INSTRUCTION;
        }
        return "";
    }

    /**
     * Checks if the object has a Natural Tag (excluding "water") and a layer tag that is not 0
     * @param object
     * @return
     */
    private String naturalNotOnGround(AtlasObject object)
    {
        if (object.tag(NaturalTag.KEY) != null
                && !object.tag(NaturalTag.KEY).equalsIgnoreCase("water")) {
            return this.NATURAL_UNDER_GROUND_INSTRUCTION;
        }
        return "";
    }

    /**
     * Checks if the object has a Highway Tag (excluding steps) and a layer tag.
     * If the layer tag is less than 0m the method checks for tunnel tag; if the layer tag is greater than 0 - for bridge tag./
     * @param object
     * @param layerTagValue
     * @return
     */
    private String highwayNotOnGround(AtlasObject object, final Optional<Long> layerTagValue)
    {
        if (object.tag(HighwayTag.KEY) != null && !object.tag(HighwayTag.KEY).equalsIgnoreCase("steps"))
        {
            if (layerTagValue.get() < 0L
                    && !objectIsTunnel(object))
            {
                return this.HIGHWAY_UNDER_GROUND_INSTRUCTION;
            }
            else if (layerTagValue.get() > 0L
                    && !objectIsBridge(object)
                    && !ManMadeTag.isPier(object))
            {
                return this.HIGHWAY_ABOVE_GROUND_INSTRUCTION;
            }
        }

        return "";
    }

    /**
     *
     * @param object
     * @param layerTagValue
     * @return
     */
    private String waterNotOnGround(AtlasObject object, Optional<Long> layerTagValue)
    {
        if (object.tag(WaterwayTag.KEY) != null
                || (NaturalTag.get(object).isPresent() && object.tag(NaturalTag.KEY).equalsIgnoreCase("water"))) {
            if (layerTagValue.get() < 0L
                    && (!TunnelTag.isTunnel(object))
                    && (object.tag(LocationTag.KEY) == null || !object.tag(LocationTag.KEY).equalsIgnoreCase("underground"))) {
                return this.WATERWAY_UNDER_GROUND_INSTRUCTION;
            } else if (layerTagValue.get() > 0L
                    && (!BridgeTag.isBridge(object))) {
                return this.WATERWAY_ABOVE_GROUND_INSTRUCTION;
            }
        }
        return "";
    }

    /**
     *
     * @param object
     * @param layerTagValue
     * @param isTagValueValid
     * @return
     */
    private String checkTunnelLayerValue(AtlasObject object, Optional<Long>layerTagValue, final boolean isTagValueValid)
    {
        if (objectIsTunnel(object)
                && (!isTagValueValid || layerTagValue.get() > TUNNEL_LAYER_TAG_MAX_VALUE
                || layerTagValue.get() < TUNNEL_LAYER_TAG_MIN_VALUE)) {
            return this.TUNNEL_INSTRUCTION;
        }
        return "";
    }

    /**
     *
     * @param object
     * @param layerTagValue
     * @param isTagValueValid
     * @return
     */
    private String checkBridgeLayerValue(AtlasObject object, final Optional<Long> layerTagValue, final boolean isTagValueValid)
    {
        if (objectIsBridge(object)
                && (!isTagValueValid || layerTagValue.get() != 0)
                && (!isTagValueValid || layerTagValue.get() > BRIDGE_LAYER_TAG_MAX_VALUE
                || layerTagValue.get() < BRIDGE_LAYER_TAG_MIN_VALUE)) {
            return this.BRIDGE_INSTRUCTION;
        }
        return "";
    }

    /**
     *
     * @param object
     * @return
     */
    private boolean objectIsBridge(AtlasObject object)
    {
        if(BridgeTag.isBridge(object)
                || (object.tag(ManMadeTag.KEY) != null && object.tag(ManMadeTag.KEY).equalsIgnoreCase("bridge")))
        {
            return true;
        }
        return false;
    }

    /**
     *
     * @param object
     * @return
     */
    private boolean objectIsTunnel (AtlasObject object)
    {
        if(TunnelTag.isTunnel(object)
                || (object.tag(ManMadeTag.KEY) != null && object.tag(ManMadeTag.KEY).equalsIgnoreCase("tunnel")))
        {
            return true;
        }
        return false;
    }
}

