package org.openstreetmap.atlas.checks.atlas.predicates;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.EmbankmentTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.LocationTag;
import org.openstreetmap.atlas.tags.ManMadeTag;
import org.openstreetmap.atlas.tags.MaxSpeedTag;
import org.openstreetmap.atlas.tags.MinSpeedTag;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.PowerTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.names.NameTag;

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
     * Tests if the {@link AtlasObject} has a highway tag that do not contain TERTIARY_LINK, SECONDARY_LINK,
     * PRIMARY_LINK, TRUNK_LINK, or MOTORWAY_LINK
     */
    Predicate<AtlasObject> IS_HIGHWAY_NOT_LINK_TYPE = object -> Validators.hasValuesFor(object,
            HighwayTag.class) && Validators.isNotOfType(object, HighwayTag.class, HighwayTag.TERTIARY_LINK,
            HighwayTag.SECONDARY_LINK, HighwayTag.PRIMARY_LINK, HighwayTag.TRUNK_LINK, HighwayTag.MOTORWAY_LINK);
    
    /**
     * Tests if the {@link AtlasObject} has a highway tag that do contain TERTIARY, SECONDARY, PRIMARY, TRUNK, or MOTORWAY
     */
    Predicate<AtlasObject> VALID_HIGHWAY_TAG = object -> Validators.hasValuesFor(object,
            HighwayTag.class) && Validators.isOfType(object, HighwayTag.class, HighwayTag.TERTIARY,
            HighwayTag.SECONDARY, HighwayTag.PRIMARY, HighwayTag.TRUNK, HighwayTag.MOTORWAY);
    /**
     * Tests if the {@link AtlasObject} has a junction tag not of type ROUNDABOUT.
     */
    Predicate<AtlasObject> NOT_ROUNDABOUT_JUNCTION = object -> Validators.hasValuesFor(object,
            JunctionTag.class) && Validators.isNotOfType(object, JunctionTag.class, JunctionTag.ROUNDABOUT);
    
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
}
