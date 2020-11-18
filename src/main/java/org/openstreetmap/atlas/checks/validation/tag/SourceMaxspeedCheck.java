package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;


/**
 * This check verifies that the source:maxspeed tag follows the official tagging rules.
 * https://wiki.openstreetmap.org/wiki/Key:source:maxspeed
 *
 * @author mm-ciub
 */
public class SourceMaxspeedCheck extends BaseCheck<Long> {

    private static final long serialVersionUID = -7004341564141771203L;
    private static final String INVALID_ELEMENT = "The element with id {0,number,#} does not follow the source:speed tagging rules";
    private static final String WRONG_VALUE =
            "The value must be 'sign', 'markings' or follow the country_code:context format.";
    private static final String WRONG_COUNTRY_CODE = "{0} is not a valid country code.";
    private static final String WRONG_CONTEXT =
            "{0} is not a valid context for the maxspeed source. (valid examples: urban, rural etc.)";
    private static final List<String> FALLBACK_INSTRUCTIONS =
            Arrays.asList(WRONG_VALUE, WRONG_COUNTRY_CODE, WRONG_CONTEXT);
    private static final String SOURCE_MAXSPEED = "source:maxspeed";
    private static final List<String> POSSIBLE_VALUES = Arrays.asList("sign", "markings");
    private static final String COUNTRY_CONTEXT = "[A-Z]{2}:.+";
    private static final Set<String> EXPECTED_CONTEXT_VALUES = new HashSet<>(
            Arrays.asList("urban", "rural", "bicycle_road", "trunk", "motorway", "living_street", "school",
                    "pedestrian_zone", "urban_motorway", "urban_trunk", "nsl", "express", "nsl_restricted", "nsl_dual",
                    "nsl_single"));
    private static final List<String> DEFAULT_EXCEPTIONS = Collections.singletonList("UK");

    private final List<String> exceptedCountries;

    /**
     * @param configuration the JSON configuration for this check
     */
    public SourceMaxspeedCheck(final Configuration configuration) {
        super(configuration);
        this.exceptedCountries = this.configurationValue(configuration, "excepted_countries", DEFAULT_EXCEPTIONS);
    }

    /**
     * Valid objects for this check are Nodes, Points and Edges with a source:maxspeed tag
     *
     * @param object the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object) {
        return !this.isFlagged(object.getOsmIdentifier()) && (object instanceof Edge || object instanceof Point) && this
                .hasSourceMaxspeed(object) && (object.getTags().containsKey(ISOCountryTag.KEY)
                && !this.exceptedCountries.contains(object.tag(ISOCountryTag.KEY).toUpperCase()));
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object) {
        if (object.getTag(SOURCE_MAXSPEED).isPresent()) {
            final Set<String> instructions = new HashSet<>();
            final String sourceValue = object.getTag(SOURCE_MAXSPEED).get();
            if (sourceValue.matches(COUNTRY_CONTEXT)) {
                final String[] parts = sourceValue.split(":");

            } else if (!POSSIBLE_VALUES.contains(sourceValue)) {
                instructions.add(this.getLocalizedInstruction(0));
            }
            if (!instructions.isEmpty()) {
                final CheckFlag flag =
                        this.createFlag(object, this.getLocalizedInstruction(2, object.getOsmIdentifier()));
                instructions.forEach(flag::addInstruction);
                return Optional.of(flag);
            }
        }
        return Optional.empty();
    }

    private boolean hasSourceMaxspeed(final AtlasObject object) {
        return object.getOsmTags().keySet().stream().anyMatch(key -> key.contains(SOURCE_MAXSPEED));
    }
}
