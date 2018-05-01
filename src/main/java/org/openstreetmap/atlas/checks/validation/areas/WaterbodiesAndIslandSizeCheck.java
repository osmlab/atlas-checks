package org.openstreetmap.atlas.checks.validation.areas;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.atlas.predicates.TagPredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.PlaceTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 *
 * @author danielbaah
 */
public class WaterbodiesAndIslandSizeCheck extends BaseCheck
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;
    private static String WATERBODY_MIN_INSTRUCTION = "Waterbody {0,number,#} has an area {1,number,#} which is too small.";
    private static String WATERBODY_MAX_INSTRUCTION = "Waterbody {0,number,#} has an area {1,number,#} which is too large.";
    private static String ISLAND_MIN_INSTRUCTION = "Island {0,number,#} has an area {1,number,#} which is too small.";
    private static String ISLAND_MAX_INSTRUCTION = "Island {0,number,#} has an area {1,number,#} which is too large.";
    private static String ISLET_MAX_INSTRUCTION = "Islet {0,number,#} has an area {1,number,#} and should likely be tagged with place=ISLAND.";

    private static final Predicate<AtlasObject> IS_ARCHIPELAGO = object -> Validators
            .isOfType(object, PlaceTag.class, PlaceTag.ARCHIPELAGO);
    private static final Predicate<AtlasObject> IS_ISLET = object -> Validators.isOfType(object,
            PlaceTag.class, PlaceTag.ISLET);
    private static final Predicate<AtlasObject> IS_ISLAND = object -> Validators.isOfType(object,
            PlaceTag.class, PlaceTag.ISLAND);

    private static final double WATERBODY_MIN_AREA_DEFAULT = 10.0;
    private static final double WATERBODY_MAX_AREA_DEFAULT = 337000.0;
    private static final double ISLAND_MIN_AREA_DEFAULT = 10.0;
    private static final double ISLAND_MAX_AREA_DEFAULT = 2130800.0;
    private static final double ISLET_MAX_AREA_DEFAULT = 1.0;
    public static final int THREE = 3;
    public static final int FOUR = 3;

    private final double waterbodyMinimumArea;
    private final double waterbodyMaximumArea;
    private final double islandMinimumArea;
    private final double islandMaximumArea;
    private final double isletMaximumArea;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            WATERBODY_MIN_INSTRUCTION, WATERBODY_MAX_INSTRUCTION, ISLAND_MIN_INSTRUCTION,
            ISLAND_MAX_INSTRUCTION, ISLET_MAX_INSTRUCTION);

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public WaterbodiesAndIslandSizeCheck(final Configuration configuration)
    {
        super(configuration);
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH,
        // Distance::meters);
        this.waterbodyMinimumArea = (double) configurationValue(configuration,
                "area.waterbody.minimum.meters", WATERBODY_MIN_AREA_DEFAULT);
        this.waterbodyMaximumArea = (double) configurationValue(configuration,
                "area.waterbody.maximum.kilometers", WATERBODY_MAX_AREA_DEFAULT);
        this.islandMinimumArea = (double) configurationValue(configuration,
                "area.island.minimum.kilometers", ISLAND_MIN_AREA_DEFAULT);
        this.islandMaximumArea = (double) configurationValue(configuration,
                "area.island.maximum.kilometers", ISLAND_MAX_AREA_DEFAULT);
        this.isletMaximumArea = (double) configurationValue(configuration,
                "area.island.maximum.kilometers", ISLET_MAX_AREA_DEFAULT);

    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // by default we will assume all objects as valid
        return object instanceof Area && (TagPredicates.IS_WATER_BODY.test(object)
                || IS_ARCHIPELAGO.test(object) || IS_ISLET.test(object) || IS_ISLAND.test(object));
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // insert algorithmic check to see whether object needs to be flagged.
        // Example of flagging an object
        // return Optional.of(this.createFlag(object, "Instruction how to fix issue or reason behind
        // flagging the object");

        final Area area = (Area) object;
        final double surfaceAreaMeters = area.asPolygon().surface().asMeterSquared();
        final double surfaceAreaKilometers = area.asPolygon().surface().asKilometerSquared();

        if (TagPredicates.IS_WATER_BODY.test(object))
        {
            if (surfaceAreaMeters < this.waterbodyMinimumArea)
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0,
                        object.getOsmIdentifier(), surfaceAreaMeters)));
            }
            else if (surfaceAreaKilometers > this.waterbodyMaximumArea)
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(1,
                        object.getOsmIdentifier(), surfaceAreaKilometers)));
            }
        }

        if (IS_ISLAND.test(object))
        {
            if (surfaceAreaMeters < this.islandMinimumArea)
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(2,
                        object.getOsmIdentifier(), surfaceAreaMeters)));
            }
            else if (surfaceAreaKilometers > this.islandMaximumArea)
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(THREE,
                        object.getOsmIdentifier(), surfaceAreaKilometers)));
            }
        }

        if (IS_ISLET.test(object))
        {
            if (surfaceAreaMeters > this.isletMaximumArea)
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(FOUR,
                        object.getOsmIdentifier(), surfaceAreaKilometers)));
            }
        }

        return Optional.empty();
    }
}
