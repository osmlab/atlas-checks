package org.openstreetmap.atlas.checks.validation.tag;

import static org.openstreetmap.atlas.checks.constants.CommonConstants.COLON;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.annotations.validation.ISO2CountryValidator;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check verifies that the source:maxspeed tag follows the official tagging rules.
 * https://wiki.openstreetmap.org/wiki/Key:source:maxspeed
 *
 * @author mm-ciub
 */
public class SourceMaxspeedCheck extends BaseCheck<Long>
{

    private static final long serialVersionUID = -7004341564141771203L;
    private static final String GENERAL_INSTRUCTION = "The element with id {0,number,#} does not follow the source:maxspeed tagging rules";
    private static final String WRONG_VALUE_INSTRUCTION = "The value must be 'sign', 'markings' or follow the country_code:context format.";
    private static final String WRONG_COUNTRY_CODE_INSTRUCTION = "{0} is not a valid country code.";
    private static final String WRONG_CONTEXT_INSTRUCTION = "{0} is not a valid context for the maxspeed source. (valid examples: urban, 30 etc.)";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(GENERAL_INSTRUCTION,
            WRONG_VALUE_INSTRUCTION, WRONG_COUNTRY_CODE_INSTRUCTION, WRONG_CONTEXT_INSTRUCTION);
    private static final String SOURCE_MAXSPEED = "source:maxspeed";
    private static final List<String> POSSIBLE_VALUES = Arrays.asList("sign", "markings");
    private static final Pattern COUNTRY_CONTEXT_PATTERN = Pattern.compile("[a-zA-Z]{2}:.+");
    private static final Set<String> EXPECTED_CONTEXT_VALUES = new HashSet<>(
            Arrays.asList("urban", "rural", "bicycle_road", "trunk", "motorway", "living_street",
                    "school", "pedestrian_zone", "urban_motorway", "urban_trunk", "nsl", "express",
                    "nsl_restricted", "nsl_dual", "nsl_single", "zone"));
    // UK uses a different tag for this use case
    private static final List<String> DEFAULT_EXCEPTIONS = Collections.singletonList("UK");
    // Belgium has these 3 regions that are accepted because they have a different default rural or
    // urban maxspeed
    private static final List<String> COUNTRY_EXCEPTIONS = Arrays.asList("BE-VLG", "BE-WAL",
            "BE-BRU");
    // besides the default possible values, there are some accepted variations of "zone"
    private static final String ZONE = "zone";
    private static final int GENERAL_INSTRUCTION_INDEX = 0;
    private static final int VALUE_INSTRUCTION_INDEX = 1;
    private static final int COUNTRY_INSTRUCTION_INDEX = 2;
    private static final int CONTEXT_INSTRUCTION_INDEX = 3;

    private final List<String> exceptedCountries;

    /**
     * @param configuration
     *            the JSON configuration for this check
     */
    public SourceMaxspeedCheck(final Configuration configuration)
    {
        super(configuration);
        this.exceptedCountries = this.configurationValue(configuration, "excepted_countries",
                DEFAULT_EXCEPTIONS);
    }

    /**
     * Valid objects for this check are Points and Edges with a source:maxspeed tag and are not part
     * of the excepted countries.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return !this.isFlagged(object.getOsmIdentifier())
                && (object instanceof Edge || object instanceof Point)
                && this.hasSourceMaxspeed(object)
                && (object.getTags().containsKey(ISOCountryTag.KEY) && !this.exceptedCountries
                        .contains(object.tag(ISOCountryTag.KEY).toUpperCase()));
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        if (object.getTag(SOURCE_MAXSPEED).isPresent())
        {
            final Set<String> instructions = new HashSet<>();
            final String sourceValue = object.getTag(SOURCE_MAXSPEED).get();
            final Matcher matcher = COUNTRY_CONTEXT_PATTERN.matcher(sourceValue);
            if (matcher.find())
            {
                final String[] parts = sourceValue.split(COLON);
                if (!this.isCountryValid(parts[0]))
                {
                    instructions
                            .add(this.getLocalizedInstruction(COUNTRY_INSTRUCTION_INDEX, parts[0]));
                }
                if (!this.isContextValid(parts[1]))
                {
                    instructions
                            .add(this.getLocalizedInstruction(CONTEXT_INSTRUCTION_INDEX, parts[1]));
                }

            }

            else if (!POSSIBLE_VALUES.contains(sourceValue) && !sourceValue.contains(ZONE))
            {
                instructions.add(this.getLocalizedInstruction(VALUE_INSTRUCTION_INDEX));
            }
            if (!instructions.isEmpty())
            {
                final CheckFlag flag = this.createFlag(object, this.getLocalizedInstruction(
                        GENERAL_INSTRUCTION_INDEX, object.getOsmIdentifier()));
                instructions.forEach(flag::addInstruction);
                return Optional.of(flag);
            }
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private boolean hasSourceMaxspeed(final AtlasObject object)
    {
        return object.getOsmTags().keySet().stream().anyMatch(key -> key.contains(SOURCE_MAXSPEED));
    }

    private boolean isContextValid(final String context)
    {
        final boolean isNumber = context.matches("[0-9]].+");
        final boolean isZone = context.contains(ZONE);
        final boolean isHighwayType = EXPECTED_CONTEXT_VALUES.contains(context);
        return isNumber || isZone || isHighwayType;
    }

    private boolean isCountryValid(final String countryCode)
    {
        final ISO2CountryValidator validator = new ISO2CountryValidator();
        return validator.isValid(countryCode) || COUNTRY_EXCEPTIONS.contains(countryCode);
    }
}
