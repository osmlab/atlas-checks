package org.openstreetmap.atlas.checks.validation.tag;

import static org.openstreetmap.atlas.checks.constants.CommonConstants.COLON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.filters.RegexTaggableFilter;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * This flags features based on configurable filters. Each filter passed contains the
 * {@link AtlasEntity} classes to check and a {@link TaggableFilter} or a
 * {@link RegexTaggableFilter} to test objects against. If a feature is of one of the given classes
 * and passes the associated filter then it is flagged. In addition to the taggable filters, there
 * are two configurable boolean values, "filters.resource.append.override" and
 * "filters.resource.append". If the "filters.resource.append.override" key is set to true, only the
 * filters passed through config are used to flag the atlas features. If the
 * "filters.resource.append" is set to true, the filters passed through the config are appended to
 * the default filters that are in the "invalidTags.txt" resource file.
 *
 * @author bbreithaupt
 * @author sayas01
 */
public class InvalidTagsCheck extends BaseCheck<String>
{

    private static final long serialVersionUID = 5150282147895785829L;
    private static final String KEY_VALUE_SEPARATOR = "->";
    private static final String DEFAULT_FILTER_RESOURCE = "invalidTags.txt";
    private static final List<String> FALLBACK_INSTRUCTIONS = new ArrayList<>();
    private static final String DEFAULT_NR_TAGS_INSTRUCTION = "OSM feature {0,number,#} has invalid tags.";
    private static final String DEFAULT_INSTRUCTION = "Check the following tags for missing, conflicting, or incorrect values: {0}";
    private static final Logger logger = LoggerFactory.getLogger(InvalidTagsCheck.class);
    public static final int INLINE_REGEX_FILTER_SIZE = 3;
    private static final String REGEX = "regex";
    private static final int REGEX_INSTRUCTION_INDEX = 3;

    private final List<Tuple<? extends Class<AtlasEntity>, List<Tuple<? extends Predicate<Taggable>, Integer>>>> classTagFilters;

    private static int addInstruction(final String instruction)
    {
        if (Objects.nonNull(instruction) && !instruction.isEmpty())
        {
            FALLBACK_INSTRUCTIONS.add(instruction);
            return FALLBACK_INSTRUCTIONS.size() - 1;
        }
        return 1;
    }

    /**
     * @return a List of Tuple containing AtlasEntity and a list of Tuple containing either a
     *         TaggableFilters or a RegexTaggableFilters with the index of the associated
     *         instruction read from the json files of each AtlasEntity. DEFAULT_FILTER_RESOURCE
     *         file maps each AtlasEntity to its corresponding filter files. The RegexTaggableFilter
     *         file must contain the word regex in it's naming. ex. bad-source-regex-filter.json
     */
    private static List<Tuple<? extends Class<AtlasEntity>, List<Tuple<? extends Predicate<Taggable>, Integer>>>> getDefaultFilters()
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                InvalidTagsCheck.class.getResourceAsStream(DEFAULT_FILTER_RESOURCE))))
        {
            final List<Tuple<? extends Class<AtlasEntity>, List<Tuple<? extends Predicate<Taggable>, Integer>>>> listOfClassToFilters = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null)
            {
                final String[] split = line.split(COLON);
                if (split.length >= 2)
                {
                    final List<Tuple<? extends Predicate<Taggable>, Integer>> filters = split[1]
                            .contains(REGEX) ? getRegexFiltersFromResource(split[1])
                                    : getFiltersFromResource(split[1]);
                    listOfClassToFilters.add(new Tuple<>(
                            ItemType.valueOf(split[0].toUpperCase()).getMemberClass(), filters));
                }
            }
            return listOfClassToFilters;
        }
        catch (final IOException exception)
        {
            logger.error(String.format("Could not read %s", DEFAULT_FILTER_RESOURCE), exception);
            return Collections.emptyList();
        }
    }

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
     * Read the json file and return a list of TaggableFilter associated with instructions for each
     * line.
     *
     * @param filterResourcePath
     *            file path
     * @return list of Tuple containing TaggableFilter and instruction
     */
    private static List<Tuple<? extends Predicate<Taggable>, Integer>> getFiltersFromResource(
            final String filterResourcePath)
    {
        try (InputStreamReader reader = new InputStreamReader(
                InvalidTagsCheck.class.getResourceAsStream(filterResourcePath)))
        {
            final List<Tuple<? extends Predicate<Taggable>, Integer>> result = new ArrayList<>();
            final JsonElement element = new JsonParser().parse(reader);
            final JsonArray filters = element.getAsJsonObject().get("filters").getAsJsonArray();
            final List<TaggableFilter> taggableFilters = StreamSupport
                    .stream(filters.spliterator(), false)
                    .map(jsonElement -> TaggableFilter.forDefinition(jsonElement.getAsString()))
                    .collect(Collectors.toList());
            final JsonArray instructionsArray = element.getAsJsonObject().get("instructions")
                    .getAsJsonArray();
            final List<String> instructions = StreamSupport
                    .stream(instructionsArray.spliterator(), false).map(JsonElement::getAsString)
                    .collect(Collectors.toList());
            for (int i = 0; i < taggableFilters.size(); i++)
            {
                final String instruction = i < instructions.size() ? instructions.get(i) : "";
                final int instructionIndex = addInstruction(instruction);
                result.add(Tuple.createTuple(taggableFilters.get(i), instructionIndex));
            }
            return result;

        }
        catch (final Exception exception)
        {
            logger.error(
                    "There was a problem parsing invalid-tags-check-inconsistent-highway-filter.json. "
                            + "Check if the JSON file has valid structure.",
                    exception);
            return Collections.emptyList();
        }
    }

    private static List<Tuple<? extends Predicate<Taggable>, Integer>> getRegexFiltersFromResource(
            final String filterResourcePath)
    {
        try (InputStreamReader reader = new InputStreamReader(
                InvalidTagsCheck.class.getResourceAsStream(filterResourcePath)))
        {
            final JsonElement element = new JsonParser().parse(reader);
            final JsonArray filters = element.getAsJsonObject().get("filters").getAsJsonArray();
            return StreamSupport.stream(filters.spliterator(), false)
                    .map(JsonElement::getAsJsonObject).map(jsonObject ->
                    {
                        final Set<String> tagNames = getSetFromJsonArray(
                                jsonObject.getAsJsonArray("tagNames"));
                        final Set<String> regex = getSetFromJsonArray(
                                jsonObject.getAsJsonArray(REGEX));
                        final JsonArray exceptionArray = jsonObject.getAsJsonArray("exceptions");
                        final HashMap<String, Set<String>> exceptions = new HashMap<>();
                        StreamSupport.stream(exceptionArray.spliterator(), false)
                                .map(JsonElement::getAsJsonObject).forEach(exception ->
                                {
                                    final String tagName = exception.get("tagName").getAsString();
                                    final Set<String> values = getSetFromJsonArray(
                                            exception.getAsJsonArray("values"));
                                    exceptions.putIfAbsent(tagName, values);
                                });
                        final RegexTaggableFilter filter = new RegexTaggableFilter(tagNames, regex,
                                exceptions);
                        final String instruction = jsonObject.get("instruction").getAsString();
                        final int instructionIndex = addInstruction(instruction);
                        return Tuple.createTuple(filter, instructionIndex);
                    }).collect(Collectors.toList());
        }
        catch (final Exception exception)
        {
            logger.error(
                    "There was a problem parsing {}. Check if the JSON file has valid structure.",
                    filterResourcePath, exception);
            return Collections.emptyList();
        }
    }

    private static Set<String> getSetFromJsonArray(final JsonArray array)
    {
        return StreamSupport.stream(array.spliterator(), false).map(JsonElement::getAsString)
                .collect(Collectors.toSet());
    }

    /**
     * Convert a {@link String} of comma delimited {@link AtlasEntity} class names and a
     * {@link String} {@link TaggableFilter} to a {@link Set} of {@link AtlasEntity} {@link Class}ed
     * and {@link TaggableFilter}.
     *
     * @param classString
     *            A {@link String} of comma delimited {@link AtlasEntity} class names
     * @param tagFilterString
     *            A {@link String} {@link TaggableFilter} definition
     * @param instruction
     *            A {@link String} representing a specific instruction for the filter
     * @return A {@link Tuple} of a {@link Set} of {@link AtlasEntity} {@link Class}es and a
     *         {@link TaggableFilter}
     */
    private static Tuple<? extends Class<AtlasEntity>, List<Tuple<TaggableFilter, Integer>>> stringsToClassTagFilter(
            final String classString, final String tagFilterString, final String instruction)
    {
        final int instructionIndex = addInstruction(instruction);
        final List<Tuple<TaggableFilter, Integer>> filters = new ArrayList<>();
        filters.add(
                Tuple.createTuple(TaggableFilter.forDefinition(tagFilterString), instructionIndex));
        return Tuple.createTuple(ItemType.valueOf(classString.toUpperCase()).getMemberClass(),
                filters);
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
        FALLBACK_INSTRUCTIONS.add(DEFAULT_NR_TAGS_INSTRUCTION);
        FALLBACK_INSTRUCTIONS.add(DEFAULT_INSTRUCTION);
        final boolean overrideResourceFilters = this.configurationValue(configuration,
                "filters.resource.override", false);
        // If the "filters.resource.override" key in the config is set to true, use only the filters
        // passed through the config,
        if (overrideResourceFilters)
        {
            this.classTagFilters = this.getFiltersFromConfiguration(configuration);
        }
        // Append filters from config to the default list of filters if "filters.resource.append"
        // is set to true
        else
        {
            final List<Tuple<? extends Class<AtlasEntity>, List<Tuple<? extends Predicate<Taggable>, Integer>>>> defaultFilters = getDefaultFilters();
            // Add all filters from the config file to the default list of filters
            defaultFilters.addAll(this.getFiltersFromConfiguration(configuration));
            this.classTagFilters = defaultFilters;
        }
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
                .filter(classTagFilter -> classTagFilter.getFirst().isInstance(object))
                .map(Tuple::getSecond).flatMap(Collection::stream)
                .filter(filter -> filter.getFirst().test(object))
                // Map the filters that were passed to instructions
                .map(filter ->
                {
                    if (filter.getFirst() instanceof TaggableFilter)
                    {
                        return this.getLocalizedInstruction(filter.getSecond(),
                                getFilterKeys((TaggableFilter) filter.getFirst()));
                    }
                    else
                    {
                        return this.getLocalizedInstruction(filter.getSecond(),
                                ((RegexTaggableFilter) filter.getFirst()).getMatchedTags(object));
                    }
                }).collect(Collectors.toList());
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

    /**
     * From the config file, create a list of Tuples with atlas entity and corresponding list of
     * taggable and regex filters associated with instructions
     *
     * @param configuration
     *            configuration
     * @return List of Tuples containing AtlasEntity and its corresponding list of TaggableFilter
     *         and RegexTaggableFilter
     */
    private List<Tuple<? extends Class<AtlasEntity>, List<Tuple<? extends Predicate<Taggable>, Integer>>>> getFiltersFromConfiguration(
            final Configuration configuration)
    {
        final List<Tuple<? extends Class<AtlasEntity>, List<Tuple<? extends Predicate<Taggable>, Integer>>>> filters = this
                .readConfigurationFilter(configuration, "filters.classes.tags");
        final List<Tuple<? extends Class<AtlasEntity>, List<Tuple<? extends Predicate<Taggable>, Integer>>>> allFilters = new ArrayList<>(
                filters);
        final List<Tuple<? extends Class<AtlasEntity>, List<Tuple<? extends Predicate<Taggable>, Integer>>>> regexFilters = this
                .readConfigurationFilter(configuration, "filters.classes.regex");
        allFilters.addAll(regexFilters);
        return allFilters;
    }

    @SuppressWarnings("unchecked")
    private List<Tuple<? extends Class<AtlasEntity>, List<Tuple<? extends Predicate<Taggable>, Integer>>>> readConfigurationFilter(
            final Configuration configuration, final String key)
    {
        return this.configurationValue(configuration, key, Collections.emptyList(),
                configList -> configList.stream().map(classTagValue ->
                {

                    if (key.contains(REGEX))
                    {
                        final List<Object> classTagList = (List<Object>) classTagValue;
                        if (classTagList.size() >= INLINE_REGEX_FILTER_SIZE)
                        {
                            final String element = (String) classTagList.get(0);
                            final List<String> tagNames = (List<String>) classTagList.get(1);
                            final List<String> regex = (List<String>) classTagList.get(2);
                            final List<Tuple<? extends Predicate<Taggable>, Integer>> filters = new ArrayList<>();
                            final String instruction = classTagList
                                    .size() > INLINE_REGEX_FILTER_SIZE
                                            ? (String) classTagList.get(REGEX_INSTRUCTION_INDEX)
                                            : "";
                            final RegexTaggableFilter filter = new RegexTaggableFilter(
                                    new HashSet<>(tagNames), new HashSet<>(regex), null);
                            final int instructionIndex = addInstruction(instruction);
                            filters.add(Tuple.createTuple(filter, instructionIndex));
                            return Optional.of(Tuple.createTuple(
                                    ItemType.valueOf(element.toUpperCase()).getMemberClass(),
                                    filters));
                        }
                    }
                    else
                    {
                        final List<String> classTagList = (List<String>) classTagValue;
                        if (classTagList.size() > 1)
                        {
                            final String instruction = classTagList.size() > 2 ? classTagList.get(2)
                                    : "";
                            return Optional.of(stringsToClassTagFilter(classTagList.get(0),
                                    classTagList.get(1), instruction));
                        }
                    }
                    return Optional.empty();
                }).filter(Optional::isPresent).map(
                        tuple -> (Tuple<? extends Class<AtlasEntity>, List<Tuple<? extends Predicate<Taggable>, Integer>>>) tuple
                                .get())
                        .collect(Collectors.toList()));
    }
}
