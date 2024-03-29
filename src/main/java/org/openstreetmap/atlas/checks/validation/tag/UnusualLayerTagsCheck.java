package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.KeyFullyChecked;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.CoveredTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LandUseTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.LocationTag;
import org.openstreetmap.atlas.tags.ManMadeTag;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.ServiceTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.TunnelTag;
import org.openstreetmap.atlas.tags.WaterwayTag;
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
    private static final String LANDUSE_INSTRUCTION = "Landuse feature is not on the ground";
    private static final String NATURAL_INSTRUCTION = "Natural feature is not on the ground";
    private static final String HIGHWAY_NOT_ON_GROUND_INSTRUCTION = "Highway not on ground and no tunnel, bridge or covered tags";
    private static final String WATERWAY_NOT_ON_GROUND_INSTRUCTION = "Waterway not on ground and no tunnel, bridge or covered tags";
    private static final String LAYER_LEVEL_IS_ZERO = "Layer level should not be 0";
    @KeyFullyChecked(KeyFullyChecked.Type.TAGGABLE_FILTER)
    static final Predicate<Taggable> ALLOWED_TAGS;
    private static final long BRIDGE_LAYER_TAG_MAX_VALUE = LayerTag.getMaxValue();
    // Constants for bridge checks
    private static final long BRIDGE_LAYER_TAG_MIN_VALUE = 1;
    private static final String BRIDGE_INSTRUCTION = String.format(
            "Bridges must have a layer tag set to a value in [%d, %d].", BRIDGE_LAYER_TAG_MIN_VALUE,
            BRIDGE_LAYER_TAG_MAX_VALUE);
    private static final long TUNNEL_LAYER_TAG_MAX_VALUE = -1;
    // Constants for tunnel checks
    private static final long TUNNEL_LAYER_TAG_MIN_VALUE = LayerTag.getMinValue();
    private static final String TUNNEL_INSTRUCTION = String.format(
            "Tunnels must have layer tags set to a value in [%d, %d].", TUNNEL_LAYER_TAG_MIN_VALUE,
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
        return (object instanceof Node || object instanceof Area || object instanceof Line
                || (object instanceof Edge && ((Edge) object).isMainEdge()))
                && ALLOWED_TAGS.test(object)
                // remove way sectioned duplicates
                && !this.isFlagged(object.getOsmIdentifier());
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

        // mark osm id as flagged
        this.markAsFlagged(object.getOsmIdentifier());

        if (object.tag(LayerTag.KEY) != null && object.tag(LayerTag.KEY).equals("0"))
        {
            return Optional.of(this.createFlag(object, LAYER_LEVEL_IS_ZERO));
        }

        else if (this.landUseNotOnGround(object, isTagValueValid))
        {
            return Optional.of(this.createFlag(object, LANDUSE_INSTRUCTION));
        }
        else if (this.naturalNotOnGround(object, isTagValueValid))
        {
            return Optional.of(this.createFlag(object, NATURAL_INSTRUCTION));
        }
        else if (this.highwayNotOnGround(object, isTagValueValid))
        {
            return Optional.of(this.createFlag(object, HIGHWAY_NOT_ON_GROUND_INSTRUCTION));
        }
        else if (this.waterNotOnGround(object, isTagValueValid))
        {
            return Optional.of(this.createFlag(object, WATERWAY_NOT_ON_GROUND_INSTRUCTION));
        }
        else if (!this.checkLayerValue(object, layerTagValue, isTagValueValid).isEmpty())
        {
            return Optional.of(this.createFlag(object,
                    this.checkLayerValue(object, layerTagValue, isTagValueValid)));
        }

        return Optional.empty();
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
    private boolean checkBridgeLayerValue(final AtlasObject object,
            final Optional<Long> layerTagValue, final boolean isTagValueValid)
    {
        return this.objectIsBridge(object) && (!isTagValueValid
                || (layerTagValue.isPresent() && (layerTagValue.get() > BRIDGE_LAYER_TAG_MAX_VALUE
                        || layerTagValue.get() < BRIDGE_LAYER_TAG_MIN_VALUE)));
    }

    /**
     * checks if the object with a tunnel or bridge tag has the appropriate value in the layer tag.
     *
     * @param object
     * @param layerTagValue
     * @param isTagValueValid
     * @return
     */
    private String checkLayerValue(final AtlasObject object, final Optional<Long> layerTagValue,
            final boolean isTagValueValid)
    {
        if (this.checkTunnelLayerValue(object, layerTagValue, isTagValueValid)
                && this.properWithoutLayer(object))
        {
            return TUNNEL_INSTRUCTION;
        }
        else if (this.checkBridgeLayerValue(object, layerTagValue, isTagValueValid)
                && this.properWithoutLayer(object))
        {
            return BRIDGE_INSTRUCTION;
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
    private boolean checkTunnelLayerValue(final AtlasObject object,
            final Optional<Long> layerTagValue, final boolean isTagValueValid)
    {
        return this.objectIsTunnel(object) && (!isTagValueValid
                || (layerTagValue.isPresent() && layerTagValue.get() > TUNNEL_LAYER_TAG_MAX_VALUE)
                || (layerTagValue.isPresent() && layerTagValue.get() < TUNNEL_LAYER_TAG_MIN_VALUE));
    }

    /**
     * Checks if the object has a Highway Tag (excluding steps) and a layer tag. If the layer tag is
     * less than 0m the method checks for tunnel tag; if the layer tag is greater than 0 - for
     * bridge tag./
     *
     * @param object
     * @return instructions for flagging
     */
    private boolean highwayNotOnGround(final AtlasObject object, final boolean isTagValueValid)
    {
        return isTagValueValid && HighwayTag.highwayTag(object).isPresent()
                && this.properWithoutLayer(object) && (!this.objectIsTunnel(object)
                        && !this.objectIsBridge(object) && !this.objectIsCovered(object));
    }

    /**
     * Checks if the object has a LandUse tag and a layer tag that is not 0
     *
     * @param object
     * @return instructions for flagging
     */
    private boolean landUseNotOnGround(final AtlasObject object, final boolean isTagValueValid)
    {
        return isTagValueValid && object.tag(LandUseTag.KEY) != null
                && (!this.objectIsTunnel(object) && !this.objectIsBridge(object)
                        && !this.objectIsCovered(object));
    }

    /**
     * Checks if the object has a Natural Tag (excluding "water") and a layer tag that is not 0
     *
     * @param object
     * @return instructions for flagging
     */
    private boolean naturalNotOnGround(final AtlasObject object, final boolean isTagValueValid)
    {
        final Optional<NaturalTag> natural = NaturalTag.get(object);
        return isTagValueValid && natural.isPresent() && !natural.get().equals(NaturalTag.WATER)
                && (!this.objectIsTunnel(object) && !this.objectIsBridge(object)
                        && !this.objectIsCovered(object));
    }

    /**
     * Helper function that determines if the object is a bridge. It checks for both the bridge and
     * the man_made=bridge tags.
     * 
     * @param object
     * @return true if the object is a bridge, otherwise - false
     */
    private boolean objectIsBridge(final AtlasObject object)
    {
        return BridgeTag.isBridge(object) || (object.tag(ManMadeTag.KEY) != null
                && object.tag(ManMadeTag.KEY).equalsIgnoreCase("bridge"));
    }

    /**
     * Helper function that determines if the object has a covered tag that is not "no"
     *
     * @param object
     * @return true if there is covered tag
     */
    private boolean objectIsCovered(final AtlasObject object)
    {
        return object.tag(CoveredTag.KEY) != null
                && !object.tag(CoveredTag.KEY).equalsIgnoreCase("no");
    }

    /**
     * Helper function that determines if the object is a tunnel. It checks for both the tunnel and
     * the man_made=tunnel tags.
     *
     * @param object
     * @return true if the object is a tunnel, otherwise - false
     */
    private boolean objectIsTunnel(final AtlasObject object)
    {
        return TunnelTag.isTunnel(object) || (object.tag(ManMadeTag.KEY) != null
                && object.tag(ManMadeTag.KEY).equalsIgnoreCase("tunnel"));
    }

    /**
     * conditions upon which items can have a bridge or tunnel and layer tags can be independent.
     * 
     * @param object
     * @return
     */
    private boolean properWithoutLayer(final AtlasObject object)
    {
        final Optional<HighwayTag> highway = HighwayTag.highwayTag(object);
        return !((highway.isPresent() && highway.get().equals(HighwayTag.STEPS))
                || (object.tag(ServiceTag.KEY) != null && object.tag(ServiceTag.KEY)
                        .equalsIgnoreCase(ServiceTag.PARKING_AISLE.toString())));
    }

    /**
     * Checks if a water object is not on the ground (Natural tag = "water" or a Waterway tag)
     *
     * @param object
     * @return instructions for flagging
     */
    private boolean waterNotOnGround(final AtlasObject object, final boolean isTagValueValid)
    {
        final Optional<NaturalTag> natural = NaturalTag.get(object);
        return isTagValueValid
                && (WaterwayTag.get(object).isPresent()
                        || (natural.isPresent() && natural.get().equals(NaturalTag.WATER)))
                && (!this.objectIsTunnel(object) && !this.objectIsBridge(object)
                        && !this.objectIsCovered(object) && (object.tag(LocationTag.KEY) == null
                                || !object.tag(LocationTag.KEY).equalsIgnoreCase("underground")));
    }

}
