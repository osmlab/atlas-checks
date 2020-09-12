package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LandUseTag;
import org.openstreetmap.atlas.tags.ManMadeTag;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags Area Objects with conflicting tag combinations.
 *
 * @author danielbaah
 */
public class ConflictingAreaTagCombination extends BaseCheck<Long>
{

    private static final String INVALID_COMBINATION_INSTRUCTION = "OSM feature {0,number,#} has invalid tag combinations.";
    private static final String INVALID_BUILDING_NATURAL_INSTRUCTION = "Building tag should not exist with natural tag";
    private static final String INVALID_BUILDING_HIGHWAY_INSTRUCTION = "Building tag should not exist with highway tag";
    private static final String INVALID_NATURAL_HIGHWAY_INSTRUCTION = "Natural tag should not exist with highway tag";
    private static final String INVALID_WATER_LANDUSE_INSTRUCTION = "natural=WATER tag should not exist with any landuse tag other than RESERVOIR, BASIN, or AQUACULTURE";
    private static final String INVALID_WATER_MANMADE_INSTRUCTION = "natural=WATER tag should not exist with any MAN_MADE tag other than RESERVOIR_COVERED or WASTEWATER_PLANT";
    private static final String INVALID_LANDUSE_HIGHWAY = "Land use tag should not exist with highway tag";

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            INVALID_COMBINATION_INSTRUCTION, INVALID_BUILDING_NATURAL_INSTRUCTION,
            INVALID_BUILDING_HIGHWAY_INSTRUCTION, INVALID_NATURAL_HIGHWAY_INSTRUCTION,
            INVALID_WATER_MANMADE_INSTRUCTION, INVALID_WATER_LANDUSE_INSTRUCTION,
            INVALID_LANDUSE_HIGHWAY);
    // Building tag should not exist with any natural tags. The only exception is if building=NO.
    private static final Predicate<Taggable> BUILDING_NATURAL = object -> Validators
            .isNotOfType(object, BuildingTag.class, BuildingTag.NO)
            && Validators.hasValuesFor(object, NaturalTag.class);
    // Building tag should not exist with any highway tags. The only exception is highway=SERVICES.
    private static final Predicate<Taggable> BUILDING_HIGHWAY = object -> Validators
            .isNotOfType(object, BuildingTag.class, BuildingTag.NO)
            && Validators.isNotOfType(object, HighwayTag.class, HighwayTag.SERVICES);
    // Natural tag should not exist with any highway tags.
    private static final Predicate<Taggable> NATURAL_HIGHWAY = object -> Validators.hasValuesFor(
            object, NaturalTag.class) && Validators.hasValuesFor(object, HighwayTag.class);
    // The natural=WATER tag should not exist with any man_made tags. The exceptions are
    // man_made=RESERVOIR_COVERED and man_made=WASTEWATER_PLANT.
    private static final Predicate<Taggable> NATURAL_WATER_MANMANDE = object -> Validators
            .isOfType(object, NaturalTag.class, NaturalTag.WATER)
            && Validators.isNotOfType(object, ManMadeTag.class, ManMadeTag.RESERVOIR_COVERED,
                    ManMadeTag.WASTEWATER_PLANT);
    // The natural=WATER tag should not exist with any landuse tags. The exceptions are
    // landuse=BASIN,
    // landuse=RESERVOIR, and landuse=AQUACULTURE.
    private static final Predicate<Taggable> WATER_LANDUSE = object -> Validators.isOfType(object,
            NaturalTag.class, NaturalTag.WATER)
            && Validators.isNotOfType(object, LandUseTag.class, LandUseTag.RESERVOIR,
                    LandUseTag.BASIN, LandUseTag.AQUACULTURE);
    // Landuse should not exist with any highway tags.
    private static final Predicate<Taggable> LANDUSE_HIGHWAY = object -> Validators.hasValuesFor(
            object, LandUseTag.class) && Validators.hasValuesFor(object, HighwayTag.class);
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final long serialVersionUID = 9167816371258788999L;

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
        return object instanceof Area && !Validators.isOfType(object, AreaTag.class, AreaTag.NO);
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

        if (BUILDING_NATURAL.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(1));
            hasConflictingCombinations = true;
        }

        if (BUILDING_HIGHWAY.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(2));
            hasConflictingCombinations = true;
        }

        if (NATURAL_HIGHWAY.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(THREE));
            hasConflictingCombinations = true;
        }

        if (NATURAL_WATER_MANMANDE.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(FOUR));
            hasConflictingCombinations = true;
        }

        if (WATER_LANDUSE.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(FIVE));
            hasConflictingCombinations = true;
        }

        if (LANDUSE_HIGHWAY.test(object))
        {
            flag.addInstruction(this.getLocalizedInstruction(SIX));
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
