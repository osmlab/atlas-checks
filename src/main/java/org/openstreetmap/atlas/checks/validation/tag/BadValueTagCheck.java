package org.openstreetmap.atlas.checks.validation.tag;

import static org.openstreetmap.atlas.checks.constants.CommonConstants.COLON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * TODO
 *
 * @author mm-ciub
 */
public class BadValueTagCheck extends BaseCheck<String>
{

    private static final long serialVersionUID = -7807908139598415492L;
    private static final Logger logger = LoggerFactory.getLogger(BadValueTagCheck.class);

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "OSM feature {0,number,#} has unacceptable tag values.",
            "Check the following source tag for illegal values: {0}");
    private static final String DEFAULT_FILTER_RESOURCE = "badTagValue.txt";
    private final List<Tuple<? extends Class<AtlasEntity>, List<RegexTaggableFilter>>> classTagFilters;

    public BadValueTagCheck(final Configuration configuration)
    {
        super(configuration);
        classTagFilters = getFilters();
    }

    private static List<Tuple<? extends Class<AtlasEntity>, List<RegexTaggableFilter>>> getFilters()
    {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                InvalidTagsCheck.class.getResourceAsStream(DEFAULT_FILTER_RESOURCE))))
        {
            final List<Tuple<? extends Class<AtlasEntity>, List<RegexTaggableFilter>>> listOfClassToFilters = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null)
            {
                final String[] split = line.split(COLON);
                if (split.length == 2)
                {
                    listOfClassToFilters.add(
                            new Tuple<>(ItemType.valueOf(split[0].toUpperCase()).getMemberClass(),
                                    getFiltersFromResource(split[1])));
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

    private static List<RegexTaggableFilter> getFiltersFromResource(final String filterResourcePath)
    {
        try (InputStreamReader reader = new InputStreamReader(
                InvalidTagsCheck.class.getResourceAsStream(filterResourcePath)))
        {
            final JsonElement element = new JsonParser().parse(reader);
            final JsonArray filters = element.getAsJsonObject().get("filters").getAsJsonArray();
            return StreamSupport.stream(filters.spliterator(), false)
                    .map(JsonElement::getAsJsonObject).map(jsonObject ->
                    {
                        final String tagName = jsonObject.getAsJsonPrimitive("tagName")
                                .getAsString();
                        final JsonArray regexArray = jsonObject.getAsJsonArray("regex");
                        final List<String> regex = StreamSupport
                                .stream(regexArray.spliterator(), false)
                                .map(JsonElement::getAsString).collect(Collectors.toList());
                        return new RegexTaggableFilter(tagName, regex);
                    }).collect(Collectors.toList());
        }
        catch (final Exception exception)
        {
            logger.error("There was a problem parsing bad-value-tag-check-filter.json. "
                    + "Check if the JSON file has valid structure.", exception);
            return Collections.emptyList();
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
        //TODO check that object has tags from filters
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
                .filter(filter -> filter.test(object))
                // Map the filters that were passed to instructions
                .map(filter -> this.getLocalizedInstruction(1, filter.getTagName()))
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

    private static class RegexTaggableFilter implements Predicate<AtlasObject>, Serializable
    {

        private final List<Pattern> regexPatterns;
        private final String tagName;

        RegexTaggableFilter(final String tagName, final List<String> definition)
        {
            this.tagName = tagName;
            this.regexPatterns = definition.stream().map(Pattern::compile)
                    .collect(Collectors.toList());
        }

        @Override
        public boolean test(final AtlasObject atlasObject)
        {
            final Optional<String> tagValue = atlasObject.getTag(tagName);
            if (tagValue.isPresent())
            {
                Optional<Matcher> match = regexPatterns.stream()
                        .map(pattern -> pattern.matcher(tagValue.get())).filter(Matcher::matches)
                        .findAny();
                return match.isPresent();

            }
            return false;
        }

        public String getTagName()
        {
            return tagName;
        }
    }
}
