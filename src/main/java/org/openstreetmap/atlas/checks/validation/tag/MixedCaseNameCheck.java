package org.openstreetmap.atlas.checks.validation.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags objects with name tags that improperly use mixed cases.
 *
 * @author bbreithaupt
 */
public class MixedCaseNameCheck extends BaseCheck
{

    private static final long serialVersionUID = 7109483897229499466L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "{0} {1,number,#} has (an) invalid mixed-case value(s) for the following tag(s): {2}.");

    // Non Bicameral Language Countries
    // "AFG", "DZA", "BHR", "BGD", "BLR", "BTN", "BRN", "KHM", "TCD", "CHN", "COM",
    // "DJI", "EGY", "ERI", "ETH", "GEO", "IND", "IRN", "IRQ", "ISR", "JPN", "JOR",
    // "KAZ", "KWT", "KGZ", "LAO", "LBN", "LBY", "MKD", "MYS", "MDV", "MRT", "MAR",
    // "MMR", "NPL", "PRK", "OMN", "PAK", "PSE", "QAT", "SAU", "SGP", "SOM", "KOR",
    // "LKA", "SDN", "SYR", "TWN", "TZA", "THA", "TUN", "UKR", "ARE", "ESH", "YEM"

    private static final List<String> CHECK_NAME_COUNTRIES_DEFAULT = Arrays.asList("AIA", "ATG",
            "AUS", "BHS", "BRB", "BLZ", "BMU", "BWA", "VGB", "CMR", "CAN", "CYM", "DMA", "FJI",
            "GMB", "GHA", "GIB", "GRD", "GUY", "IRL", "JAM", "KEN", "LSO", "MWI", "MLT", "MUS",
            "MSR", "NAM", "NZL", "NGA", "PNG", "SYC", "SLE", "SGP", "SLB", "ZAF", "SWZ", "TZA",
            "TON", "TTO", "TCA", "UGA", "GBR", "USA", "VUT", "ZMB", "ZWE");
    private static final List<String> LANGUAGE_NAME_TAGS_DEFAULT = Arrays.asList("name:en");
    private static final List<String> LOWER_CASE_PREPOSITIONS_DEFAULT = Arrays.asList("and", "from",
            "to", "of", "by", "upon", "on", "off", "at", "as", "into", "like", "near", "onto",
            "per", "till", "up", "via", "with", "for");
    private static final List<String> LOWER_CASE_ARTICLES_DEFAULT = Arrays.asList("a", "an", "the");
    private static final String SPLIT_CHARACTERS_DEFAULT = " -/()&@–";
    private static final List<String> NAME_AFFIXES_DEFAULT = Arrays.asList("Mc", "Mac", "Mck",
            "Mhic", "Mic");

    // A list of countries where the name tag should be checked
    private final List<String> checkNameCountries;
    // A list of language specific name tags to check
    private final List<String> languageNameTags;
    // A list of prepositions that are normally lower case in names
    private final List<String> lowerCasePrepositions;
    // A list of articles that are normally lower case in names, unless at the start
    private final List<String> lowerCaseArticles;
    // A string of characters that can proceed a capital letter
    private final String splitCharacters;
    // A list of name affixes that can proceed a capital letter
    private final String nameAffixes;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public MixedCaseNameCheck(final Configuration configuration)
    {
        super(configuration);
        this.checkNameCountries = (List<String>) configurationValue(configuration,
                "check_name.countries", CHECK_NAME_COUNTRIES_DEFAULT);
        this.languageNameTags = (List<String>) configurationValue(configuration,
                "name.language.keys", LANGUAGE_NAME_TAGS_DEFAULT);
        this.lowerCasePrepositions = (List<String>) configurationValue(configuration,
                "lower_case.prepositions", LOWER_CASE_PREPOSITIONS_DEFAULT);
        this.lowerCaseArticles = (List<String>) configurationValue(configuration,
                "lower_case.articles", LOWER_CASE_ARTICLES_DEFAULT);
        this.splitCharacters = (String) configurationValue(configuration, "words.split.characters",
                SPLIT_CHARACTERS_DEFAULT);
        this.nameAffixes = (String) configurationValue(configuration, "name_affixes",
                NAME_AFFIXES_DEFAULT, value -> String.join("|", (List<String>) value));
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
        return !(object instanceof Relation) && !this.isFlagged(object.getOsmIdentifier())
                && ((object.getTags().containsKey(ISOCountryTag.KEY)
                        && checkNameCountries.contains(object.tag(ISOCountryTag.KEY).toUpperCase())
                        && Validators.hasValuesFor(object, NameTag.class))
                        || languageNameTags.stream()
                                .anyMatch(key -> object.getOsmTags().containsKey(key)));
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
        final List<String> mixedCaseNameTags = new ArrayList<>();
        final Map<String, String> osmTags = object.getOsmTags();

        // Check ISO against list of countries for testing name tag
        if (checkNameCountries.contains(object.tag(ISOCountryTag.KEY).toUpperCase())
                && Validators.hasValuesFor(object, NameTag.class)
                && isMixedCase(osmTags.get(NameTag.KEY)))
        {
            mixedCaseNameTags.add("name");
        }
        // Check all language name tags
        for (final String key : languageNameTags)
        {
            if (osmTags.containsKey(key) && isMixedCase(osmTags.get(key)))
            {
                mixedCaseNameTags.add(key);
            }

        }

        // If mix case id detected, flag
        if (!mixedCaseNameTags.isEmpty())
        {
            this.markAsFlagged(object.getOsmIdentifier());
            final String osmType;
            // Get OSM type for object
            if (object instanceof LocationItem)
            {
                osmType = "Node";
            }
            else
            {
                osmType = "Way";
            }
            // Instruction includes type of OSM object and list of flagged tags
            return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0, osmType,
                    object.getOsmIdentifier(), String.join(", ", mixedCaseNameTags))));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Tests each word in a string for proper use of case in a name.
     *
     * @param value
     *            String to check
     * @return true when there is improper case in any of the words
     */
    private boolean isMixedCase(final String value)
    {
        // Split into words based on configurable characters
        final String[] wordArray = value.split("[\\Q" + this.splitCharacters + "\\E]");
        boolean firstWord = true;
        // Check each word
        for (final String word : wordArray)
        {
            // If there is more than 1 word, the word is not in the list of prepositions, and the
            // word is not both in the article list and not the first word: check that
            // the first letter is a capital
            if (wordArray.length > 1 && !lowerCasePrepositions.contains(word)
                    && !(!firstWord && lowerCaseArticles.contains(word)))
            {
                final Matcher firstLetterMatcher = Pattern.compile("\\p{L}").matcher(word);
                // If the first letter is lower case: return true if it is not preceded by a number
                if (firstLetterMatcher.find()
                        && Character.isLowerCase(firstLetterMatcher.group().charAt(0))
                        && !(firstLetterMatcher.start() != 0
                                && Character.isDigit(word.charAt(firstLetterMatcher.start() - 1))))
                {
                    return true;
                }
            }
            // If the word is not all upper case: check if all the letters not following
            // apostrophes, unless at the end of the word, are lower case
            if (Pattern.compile("[\\p{L}&&[^\\p{Lu}]]").matcher(word).find() && Pattern.compile(
                    String.format("(\\p{L}.*(?<!'|%1$s)(\\p{Lu}))|(\\p{L}.*(?<=')\\p{Lu}(?!.))",
                            this.nameAffixes))
                    .matcher(word).find())
            {
                return true;
            }
            firstWord = false;
        }
        return false;
    }
}
