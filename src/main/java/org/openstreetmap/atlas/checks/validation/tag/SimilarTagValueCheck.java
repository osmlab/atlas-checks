package org.openstreetmap.atlas.checks.validation.tag;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import scala.Tuple3;

import java.util.*;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This check looks for tags with multiple values that are duplicates or
 * values are similar that contain a typo.
 * @author brianjor
 */
public class SimilarTagValueCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 6115389966264680867L;
    private static final Predicate<String> HAS_NON_LATIN = Pattern.compile("[^\\d\\s\\p{Punct}\\p{IsLatin}]").asPredicate();
    private static final Predicate<String> HAS_NUMBER = Pattern.compile("\\d").asPredicate();
    private static final String SEMICOLON = ";";
    private static final String DUPLICATE_TAG_VALUE_INSTRUCTION = "The tag {0} contains duplicate values: {0}";
    private static final int DUPLICATE_INSTRUCTION_INDEX = 0;
    private static final String SIMILAR_TAG_VALUE_INSTRUCTION = "The tag {0} contains similar values: {0}";
    private static final int SIMILAR_INSTRUCTION_INDEX = 1;
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            DUPLICATE_TAG_VALUE_INSTRUCTION, SIMILAR_TAG_VALUE_INSTRUCTION);
    private static final Double MIN_VALUE_LENGTH = 4.0;
    private static final Double MIN_SIMILARITY_THRESHOLD_DEFAULT = 0.0;
    private static final Double MAX_SIMILARITY_THRESHOLD_DEFAULT = 1.0;
    private static final Set<String> TAGS_TO_IGNORE = Set.of(
            "asset_ref",
            "collection_times",
            "except",
            "is_in",
            "junction:ref",
            "maxspeed:conditional",
            "old_name", "old_ref", "opening_hours",
            "ref", "restriction_hours", "route_ref",
            "supervised", "source_ref",
            "target", "telescope"
    );
    // Set of tags with many sub-categories that create a lot of False positives
    private static final Set<String> TAGS_TO_IGNORE_WITH_SUB_CATEGORIES = Set.of(
            "addr", "alt_name",
            "destination",
            "name",
            "seamark",
            "turn"
    );
    // Common value sets that are similar but can be on the same tag
    private static final List<Set<String>> COMMON_SIMILAR_VALUES = Arrays.asList(
            // Ethnonyms
            Set.of("american", "mexican"),
            // Food
            Set.of("cafe", "cake"),
            // Gender
            Set.of("male", "female"), Set.of("woman", "man"), Set.of("women", "men"),
            Set.of("male_toilet", "female_toilet"),
            // Medical
            Set.of("radiology", "cardiology"),
            // Sports
            Set.of("baseball", "basketball"), Set.of("bowls", "boules"),
            Set.of("padel", "paddel"),
            // Other
            Set.of("formal", "informal"),
            Set.of("hotel", "hostel"),
            Set.of("hump", "bump"),
            Set.of("seed", "feed")
    );

    private final Double minValueLength;
    private final Double minSimilarityThreshold;
    private final Double maxSimilarityThreshold;

    /**
     * @param configuration
     *            the JSON configuration for this check
     */
    public SimilarTagValueCheck(final Configuration configuration)
    {
        super(configuration);
        this.minValueLength = this.configurationValue(configuration, "value.length.min",
                MIN_VALUE_LENGTH, Double::doubleValue);
        this.minSimilarityThreshold = this.configurationValue(configuration, "similarity.threshold.min",
                MIN_SIMILARITY_THRESHOLD_DEFAULT, Double::doubleValue);
        this.maxSimilarityThreshold = this.configurationValue(configuration, "similarity.threshold.max",
                MAX_SIMILARITY_THRESHOLD_DEFAULT, Double::doubleValue);
    }

    /**
     * Valid objects for this check objects with tags that contain multiple values.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // Only want objects with tags that contain multiple values.
        return object.getOsmTags()
                .values()
                .stream()
                .anyMatch(value -> value.contains(SEMICOLON));
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
        final Map<String, String> tagsWithMultipleValues = object
                .getOsmTags()
                .entrySet()
                .stream()
                .filter(entry -> !TAGS_TO_IGNORE.contains(entry.getKey()))
                .filter(Predicate.not(this::isTagWithSubCategoriesToIgnore))
                .map(this::removeCommonFalsePositiveValues)
                // Only keep tags that still have multiple values
                .filter(entry -> entry.getValue().contains(SEMICOLON))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        // Map of tags with a list of similar values with their similarity score
        final Map<String, List<Tuple3<String, String, Integer>>> tagsWithSimilars = tagsWithMultipleValues
                .entrySet()
                .stream()
                .map(this::findSimilars)
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        if (!tagsWithSimilars.isEmpty())
        {
            final String ogTags = object.getOsmTags()
                    .entrySet()
                    .stream()
                    .filter(t -> tagsWithSimilars.entrySet().stream().anyMatch(tw -> tw.getKey().equals(t.getKey())))
                    .map(t -> "Tag: " + t.getKey() + ", Value: " + t.getValue())
                    .collect(Collectors.joining(", "));
            System.out.println("Osmid: " + object.getOsmIdentifier());
            System.out.println("    OG Tags: " + ogTags);
            String in = tagsWithSimilars
                    .entrySet()
                    .stream()
                    .map(entry -> "    Tag: " + entry.getKey() + ", Similars: " + entry.getValue())
                    .collect(Collectors.joining(",\n"));
            System.out.println(in);
            return Optional.of(this.createFlag(object, in));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Finds the Levenshtein edit distance for the strings.
     * @return edit distance between the two strings, or -1 if either string is null.
     */
    private int findEditDistance(final String left, final String right)
    {
        try
        {
            return LevenshteinDistance.getDefaultInstance().apply(left, right);
        }
        catch (IllegalArgumentException exception)
        {
            return -1;
        }
    }

    /**
     * Map a tag entry to an entry where their value is a list of tuples containing the two
     * similar tags and their edit distance.
     */
    private Entry<String, List<Tuple3<String, String, Integer>>> findSimilars(final Entry<String, String> entry) {
        final List<String> values = Arrays.asList(entry.getValue().split(SEMICOLON));
        List<Tuple3<String, String, Integer>> similars = new ArrayList<>();
        for (int i = 0; i < values.size() - 1; i++)
        {
            for (int j = i + 1; j < values.size(); j++)
            {
                final String left = values.get(i);
                final String right = values.get(j);
                boolean isCommonSimilars = this.isCommonSimilars(left, right);
                boolean duplicates = left.equals(right);
                // Keep duplicates even if they are common similars
                if (!isCommonSimilars || duplicates)
                {
                    final int editDistance = this.findEditDistance(left, right);
                    if (minSimilarityThreshold <= editDistance && editDistance <= maxSimilarityThreshold)
                    {
                        similars.add(new Tuple3<>(left, right, editDistance));
                    }
                }
            }
        }
        return new SimpleEntry<>(entry.getKey(), similars);
    }

    /**
     * Checks if the strings are contained in the same set of common similars.
     */
    private boolean isCommonSimilars(final String left, final String right)
    {
        return COMMON_SIMILAR_VALUES.stream().anyMatch(setOfSimilars ->
                setOfSimilars.contains(left) && setOfSimilars.contains(right));
    }

    /**
     * Checks if the tag for the entry starts with any of the tags with sub-categories to ignore.
     */
    private boolean isTagWithSubCategoriesToIgnore(final Map.Entry<String, String> entry)
    {
        return TAGS_TO_IGNORE_WITH_SUB_CATEGORIES
                .stream()
                .anyMatch(entry.getKey()::startsWith);
    }

    /**
     * Remove values that are susceptible to being false positives.
     * <br>
     * Removes:
     * <ul>
     *     <li>Values whose length < {@value MIN_VALUE_LENGTH}</li>
     *     <li>Numbers</li>
     *     <li>Common similar values</li>
     * </ul>
     */
    private Entry<String, String> removeCommonFalsePositiveValues(final Entry<String, String> tag)
    {
        final Entry<String, String> newTag = new SimpleEntry<>(tag);
        List<String> splitValues = Arrays.stream(newTag.getValue().split(SEMICOLON))
                // Values must be greater than or equal to the specified minimum length.
                .filter(value -> value.length() >= this.minValueLength)
                // Remove values that contain numbers.
                .filter(Predicate.not(HAS_NUMBER))
                // Remove values that contain non-Latin characters, and others (see regex).
                .filter(Predicate.not(HAS_NON_LATIN))
                .collect(Collectors.toList());
        newTag.setValue(String.join(SEMICOLON, splitValues));
        return newTag;
    }
}
