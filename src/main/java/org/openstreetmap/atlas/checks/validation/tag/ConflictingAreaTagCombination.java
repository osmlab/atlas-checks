package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LandUseTag;
import org.openstreetmap.atlas.tags.LeisureTag;
import org.openstreetmap.atlas.tags.ManMadeTag;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.WaterTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags Area Objects with conflicting tag combinations.
 *
 * @author danielbaah
 */
public class ConflictingAreaTagCombination extends BaseCheck
{

    private static final String INVALID_COMBINATION_INSTRUCTION = "OSM feature {0,number,#} has invalid tag combinations.";
    private static final String INVALID_BUILDING_LANDUSE_INSTRUCTION = "Building should not exist with landuse=*.";
    private static final String INVALID_BUILDING_NATURAL_INSTRUCTION = "Build should not exist with natural=*";
    private static final String INVALID_BUILDING_HIGHWAY_INSTRUCTION = "Building should not exist with highway=*";
    private static final String INVALID_NATURAL_MANMADE_INSTRUCTION = "Natural should not exist with manmande=*.";
    private static final String INVALID_NATURAL_HIGHWAY_INSTRUCTION = "Natural should not exist with highway=*";
    private static final String INVALID_NATURAL_LESISURE_INSTRUCTION = "Natural should not exist with leisure=*";
    private static final String INVALID_WATER_LANDUSE_INSTRUCTION = "Water should not exist with landuse=*";

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            INVALID_COMBINATION_INSTRUCTION, INVALID_BUILDING_LANDUSE_INSTRUCTION,
            INVALID_BUILDING_NATURAL_INSTRUCTION, INVALID_BUILDING_HIGHWAY_INSTRUCTION,
            INVALID_NATURAL_MANMADE_INSTRUCTION, INVALID_NATURAL_HIGHWAY_INSTRUCTION,
            INVALID_NATURAL_LESISURE_INSTRUCTION, INVALID_WATER_LANDUSE_INSTRUCTION);
    private static final Predicate<Taggable> BUILDING_LANDUSE = object -> Validators.hasValuesFor(
            object, BuildingTag.class) && Validators.hasValuesFor(object, LandUseTag.class);
    private static final Predicate<Taggable> BUILDING_NATURAL = object -> Validators.hasValuesFor(
            object, BuildingTag.class) && Validators.hasValuesFor(object, NaturalTag.class);
    private static final Predicate<Taggable> BUILDING_HIGHWAY = object -> Validators.hasValuesFor(
            object, BuildingTag.class) && Validators.hasValuesFor(object, HighwayTag.class);
    private static final Predicate<Taggable> NATURAL_MANMADE = object -> Validators.hasValuesFor(
            object, NaturalTag.class) && Validators.hasValuesFor(object, ManMadeTag.class);
    private static final Predicate<Taggable> NATURAL_HIGHWAY = object -> Validators.hasValuesFor(
            object, NaturalTag.class) && Validators.hasValuesFor(object, HighwayTag.class);
    private static final Predicate<Taggable> NATURAL_LESISURE = object -> Validators.hasValuesFor(
            object, NaturalTag.class) && Validators.hasValuesFor(object, LeisureTag.class);
    // TODO add LandUseTag.AQUACULTURE
    private static final Predicate<Taggable> WATER_LANDUSE = object -> Validators
            .hasValuesFor(object, WaterTag.class)
            && Validators.hasValuesFor(object, LandUseTag.class) && !Validators.isOfType(object,
                    LandUseTag.class, LandUseTag.RESERVOIR, LandUseTag.BASIN);
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int SEVEN = 7;

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public ConflictingAreaTagCombination(final Configuration configuration)
    {
        super(configuration);
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
        return object instanceof Area;
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

        final CheckFlag flag = createFlag(object,
                this.getLocalizedInstruction(0, object.getOsmIdentifier()));
        boolean hasConflictingCombinations = false;

        if (BUILDING_LANDUSE.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(1));
            hasConflictingCombinations = true;
        }

        if (BUILDING_NATURAL.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(2));
            hasConflictingCombinations = true;
        }

        if (BUILDING_HIGHWAY.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(THREE));
            hasConflictingCombinations = true;
        }

        if (NATURAL_MANMADE.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(FOUR));
            hasConflictingCombinations = true;
        }

        if (NATURAL_HIGHWAY.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(FIVE));
            hasConflictingCombinations = true;
        }

        if (NATURAL_LESISURE.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(SIX));
            hasConflictingCombinations = true;
        }

        if (WATER_LANDUSE.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(SEVEN));
            hasConflictingCombinations = true;
        }

        if (hasConflictingCombinations)
        {
            return Optional.of(flag);
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
