package org.openstreetmap.atlas.checks.atlas.predicates;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.EmbankmentTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LocationTag;
import org.openstreetmap.atlas.tags.ManMadeTag;
import org.openstreetmap.atlas.tags.MaxSpeedTag;
import org.openstreetmap.atlas.tags.MinSpeedTag;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.PowerTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Collection of tag based predicates
 *
 * @author brian_l_davis
 */
public final class TagPredicates
{
    private TagPredicates()
    {
        // default constructor to fix the error "Utility classes do have public or default
        // constructor"
    }

    /**
     * Tests if the {@link AtlasObject} has a building tag
     */
    public static final Predicate<AtlasObject> IS_BUILDING = object -> Validators
            .hasValuesFor(object, BuildingTag.class);

    /**
     * Tests if the {@link AtlasObject} has a building tag equal to roof
     */
    public static final Predicate<AtlasObject> IS_ROOF = object -> Validators.isOfType(object,
            BuildingTag.class, BuildingTag.ROOF);

    /**
     * Tests if the {@link AtlasObject} has area tag equal to yes
     */
    public static final Predicate<AtlasObject> IS_AREA = object -> Validators.isOfType(object,
            AreaTag.class, AreaTag.YES);

    /**
     * Tests if the {@link AtlasObject} has a highway tag and an area tag that equals to yes
     */
    public static final Predicate<AtlasObject> IS_HIGHWAY_AREA = object -> Validators.hasValuesFor(
            object, HighwayTag.class) && Validators.isOfType(object, AreaTag.class, AreaTag.YES);

    /**
     * Tests if the {@link AtlasObject} has a highway tag that do not contain TERTIARY_LINK,
     * SECONDARY_LINK, PRIMARY_LINK, TRUNK_LINK, or MOTORWAY_LINK
     */
    public static final Predicate<AtlasObject> IS_HIGHWAY_NOT_LINK_TYPE = object -> Validators
            .hasValuesFor(object, HighwayTag.class)
            && Validators.isNotOfType(object, HighwayTag.class, HighwayTag.TERTIARY_LINK,
                    HighwayTag.SECONDARY_LINK, HighwayTag.PRIMARY_LINK, HighwayTag.TRUNK_LINK,
                    HighwayTag.MOTORWAY_LINK);

    /**
     * Tests if the {@link AtlasObject} has a minimum or maximum speed
     */
    public static final Predicate<AtlasObject> HAS_SPEED_LIMIT = object -> Validators.hasValuesFor(
            object, MaxSpeedTag.class) || Validators.hasValuesFor(object, MinSpeedTag.class);

    /**
     * Tests if the {@link AtlasObject} is a water body
     */
    public static final Predicate<AtlasObject> IS_WATER_BODY = object -> Validators.isOfType(object,
            NaturalTag.class, NaturalTag.WATER);

    /**
     * Tests if the {@link AtlasObject} is a bridge
     */
    public static final Predicate<AtlasObject> IS_BRIDGE = object -> Validators.isOfType(object,
            BridgeTag.class, BridgeTag.YES);

    /**
     * Tests if the {@link AtlasObject} is an embankment
     */
    public static final Predicate<AtlasObject> IS_EMBANKMENT = object -> Validators.isOfType(object,
            EmbankmentTag.class, EmbankmentTag.YES);

    /**
     * Tests if the {@link AtlasObject} is a pier
     */
    public static final Predicate<AtlasObject> IS_PIER = object -> Validators.isOfType(object,
            ManMadeTag.class, ManMadeTag.PIER);

    /**
     * Tests if the {@link AtlasObject} is a power line
     */
    public static final Predicate<AtlasObject> IS_POWER_LINE = object -> Validators.isOfType(object,
            PowerTag.class, PowerTag.LINE);

    /**
     * Tests if the {@link AtlasObject} goes underwater
     */
    public static final Predicate<AtlasObject> GOES_UNDERWATER = object -> Validators
            .isOfType(object, LocationTag.class, LocationTag.UNDERWATER);

    /**
     * Tests if the {@link AtlasObject} goes underground
     */
    public static final Predicate<AtlasObject> GOES_UNDERGROUND = object -> Validators
            .isOfType(object, LocationTag.class, LocationTag.UNDERGROUND);

    /**
     * Tests if the {@link AtlasObject} is has highway=pedestrian or highway=footway
     */
    public static final Predicate<AtlasObject> IS_HIGHWAY_FOR_PEDESTRIANS = object -> Validators
            .isOfType(object, HighwayTag.class, HighwayTag.PEDESTRIAN, HighwayTag.FOOTWAY);

    /**
     * Tests if the {@link AtlasObject} has highway tag equal to crossing
     */
    public static final Predicate<AtlasObject> IS_CROSSING_HIGHWAY = object -> Validators
            .isOfType(object, HighwayTag.class, HighwayTag.CROSSING);
}
