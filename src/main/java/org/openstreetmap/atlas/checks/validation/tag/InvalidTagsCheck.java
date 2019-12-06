package org.openstreetmap.atlas.checks.validation.tag;

import static org.openstreetmap.atlas.checks.constants.CommonConstants.COMMA;
import static org.openstreetmap.atlas.checks.constants.CommonConstants.EMPTY_STRING;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * This flags features based on configurable filters. Each filter passed contains the
 * {@link AtlasEntity} classes to check and a {@link TaggableFilter} to test objects against. If a
 * feature is of one of the given classes and passes the associated {@link TaggableFilter} then it
 * is flagged. There are no default filters, so this does not flag anything by default.
 *
 * @author bbreithaupt
 */
public class InvalidTagsCheck extends BaseCheck<String>
{
    private static final long serialVersionUID = 5150282147895785829L;
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "OSM feature {0,number,#} has invalid tags.",
            "Check the following tags for missing, conflicting, or incorrect values: {0}");
    private static final String KEY_VALUE_SEPARATOR = "->";

    private final List<Tuple<? extends Set<Class<AtlasEntity>>, TaggableFilter>> classTagFilters;

    /**
     * Gathers the keys from a {@link TaggableFilter} using regex.
     *
     * @param filter
     *            a {@link TaggableFilter}
     * @return the tag keys as a {@link Set} of {@link String}s
     */
    private static Set<String> getFilterKeys(final TaggableFilter filter)
    {
        return Arrays.stream(filter.toString().split("[|&]+"))
                .filter(string -> string.contains(KEY_VALUE_SEPARATOR))
                .map(tag -> tag.split(KEY_VALUE_SEPARATOR)[0]).collect(Collectors.toSet());
    }

    /**
     * Convert a {@link String} of comma delimited
     *
     * @param classString
     * @param tagFilterString
     * @return
     */
    private static Tuple<Set<Class<AtlasEntity>>, TaggableFilter> stringsToClassTagFilter(
            final String classString, final String tagFilterString)
    {
        return new Tuple<>(
                Arrays.stream(classString.split(COMMA))
                        .map(string -> ItemType.valueOf(string.toUpperCase()).getMemberClass())
                        .collect(Collectors.toSet()),
                TaggableFilter.forDefinition(tagFilterString));
    }

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public InvalidTagsCheck(final Configuration configuration)
    {
        super(configuration);
        this.classTagFilters = this.configurationValue(configuration, "filters.classes.tags",
                Collections.emptyList(), configList -> configList.stream().map(classTagValue ->
                {
                    final List<String> classTagList = (List<String>) classTagValue;
                    if (classTagList.size() > 1)
                    {
                        return stringsToClassTagFilter(classTagList.get(0), classTagList.get(1));
                    }
                    return new Tuple<>(new HashSet<Class<AtlasEntity>>(),
                            TaggableFilter.forDefinition(EMPTY_STRING));
                }).collect(Collectors.toList()));
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
        return !this.isFlagged(this.getUniqueOSMIdentifier(object));
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
        // Test against each filter and create an instruction if the object passes
        final List<String> instructions = this.classTagFilters.stream()
                // Test that the object is one of the given AtlasEntity classes
                .filter(classTagFilter -> classTagFilter.getFirst().stream()
                        .anyMatch(entityClass -> entityClass.isInstance(object))
                        // Test the object against the taggable filter
                        && classTagFilter.getSecond().test(object))
                // Map the filters that were passed to instructions
                .map(classTagFilter -> this.getLocalizedInstruction(1,
                        getFilterKeys(classTagFilter.getSecond())))
                .collect(Collectors.toList());

        if (!instructions.isEmpty())
        {
            // Mark objects flagged by their class and id to allow for the same id in different
            // object types
            this.markAsFlagged(this.getUniqueOSMIdentifier(object));

            // Create a flag with generic instructions
            final String instruction = this.getLocalizedInstruction(0, object.getOsmIdentifier());
            // If the object is an edge add the edges with the same OSM id
            final CheckFlag flag = (object instanceof Edge)
                    ? this.createFlag(new OsmWayWalker((Edge) object).collectEdges(), instruction)
                    : this.createFlag(object, instruction);

            // Add the specific instructions
            instructions.forEach(flag::addInstruction);
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
