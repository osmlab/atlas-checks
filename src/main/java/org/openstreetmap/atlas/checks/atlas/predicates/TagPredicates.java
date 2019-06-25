package org.openstreetmap.atlas.checks.atlas.predicates;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.AccessTag;
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
public interface TagPredicates
{
    /**
     * Tests if the {@link AtlasObject} has a building tag
     */
    Predicate<AtlasObject> IS_BUILDING = object -> Validators.hasValuesFor(object,
            BuildingTag.class);

    /**
     * Tests if the {@link AtlasObject} has a building tag equal to roof
     */
    Predicate<AtlasObject> IS_ROOF = object -> Validators.isOfType(object, BuildingTag.class,
            BuildingTag.ROOF);

    /**
     * Tests if the {@link AtlasObject} has area tag equal to yes
     */
    Predicate<AtlasObject> IS_AREA = object -> Validators.isOfType(object, AreaTag.class,
            AreaTag.YES);

    /**
     * Tests if the {@link AtlasObject} has a highway tag and an area tag that equals to yes
     */
    Predicate<AtlasObject> IS_HIGHWAY_AREA = object -> Validators.hasValuesFor(object,
            HighwayTag.class) && Validators.isOfType(object, AreaTag.class, AreaTag.YES);

    /**
     * Tests if the {@link AtlasObject} has a minimum or maximum speed
     */
    Predicate<AtlasObject> HAS_SPEED_LIMIT = object -> Validators.hasValuesFor(object,
            MaxSpeedTag.class) || Validators.hasValuesFor(object, MinSpeedTag.class);

    /**
     * Tests if the {@link AtlasObject} is a water body
     */
    Predicate<AtlasObject> IS_WATER_BODY = object -> Validators.isOfType(object, NaturalTag.class,
            NaturalTag.WATER);

    /**
     * Tests if the {@link AtlasObject} is a bridge
     */
    Predicate<AtlasObject> IS_BRIDGE = object -> Validators.isOfType(object, BridgeTag.class,
            BridgeTag.YES);

    /**
     * Tests if the {@link AtlasObject} is an embankment
     */
    Predicate<AtlasObject> IS_EMBANKMENT = object -> Validators.isOfType(object,
            EmbankmentTag.class, EmbankmentTag.YES);

    /**
     * Tests if the {@link AtlasObject} is a pier
     */
    Predicate<AtlasObject> IS_PIER = object -> Validators.isOfType(object, ManMadeTag.class,
            ManMadeTag.PIER);

    /**
     * Tests if the {@link AtlasObject} is a power line
     */
    Predicate<AtlasObject> IS_POWER_LINE = object -> Validators.isOfType(object, PowerTag.class,
            PowerTag.LINE);

    /**
     * Tests if the {@link AtlasObject} goes underwater
     */
    Predicate<AtlasObject> GOES_UNDERWATER = object -> Validators.isOfType(object,
            LocationTag.class, LocationTag.UNDERWATER);

    /**
     * Tests if the {@link AtlasObject} goes underground
     */
    Predicate<AtlasObject> GOES_UNDERGROUND = object -> Validators.isOfType(object,
            LocationTag.class, LocationTag.UNDERGROUND);

    /**
     * Tests if the {@link AtlasObject} is has highway=pedestrian or highway=footway
     */
    Predicate<AtlasObject> IS_HIGHWAY_FOR_PEDESTRIANS = object -> Validators.isOfType(object,
            HighwayTag.class, HighwayTag.PEDESTRIAN, HighwayTag.FOOTWAY);

    /**
     * Tests if the {@link AtlasObject} has highway tag equal to crossing
     */
    Predicate<AtlasObject> IS_CROSSING_HIGHWAY = object -> Validators.isOfType(object,
            HighwayTag.class, HighwayTag.CROSSING);

    /**
     * Tests if the {@link AtlasObject} has car navigable highway type and access tag not in Private
     * enum set
     */
    Predicate<AtlasObject> IS_CAR_NAVIGABLE_NON_PRIVATE_HIGHWAY = object -> HighwayTag
            .isCarNavigableHighway(object) && !AccessTag.isPrivate(object);

    /**
     * Tests if the {@link AtlasObject} has car navigable highway type and access=!no tag
     */
    Predicate<AtlasObject> IS_CAR_NAVIGABLE_HIGHWAY = object -> HighwayTag
            .isCarNavigableHighway(object) && !AccessTag.isNo(object);
}
