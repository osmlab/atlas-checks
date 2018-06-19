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
 * This check flags objects with {@code name} tags that improperly use mixed cases.
 *
 * @author bbreithaupt
 */
public class MixedCaseNameCheck extends BaseCheck
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "{0} {1,number,#} has (an) invalid mixed-case value(s) for the following tag(s): {2}.");

    // Non Bicameral Language Countries
    // "AFG", "DZA", "BHR", "BGD", "BLR", "BTN", "BRN", "KHM", "TCD", "CHN", "COM",
    // "DJI", "EGY", "ERI", "ETH", "GEO", "IND", "IRN", "IRQ", "ISR", "JPN", "JOR",
    // "KAZ", "KWT", "KGZ", "LAO", "LBN", "LBY", "MKD", "MYS", "MDV", "MRT", "MAR",
    // "MMR", "NPL", "PRK", "OMN", "PAK", "PSE", "QAT", "SAU", "SGP", "SOM", "KOR",
    // "LKA", "SDN", "SYR", "TWN", "TZA", "THA", "TUN", "UKR", "ARE", "ESH", "YEM"

    private static final List<String> CHECK_NAME_COUNTRIES_DEFAULT = Arrays.asList("AIA", "ATG",
            "AUT", "BHS", "BRB", "BLZ", "BMU", "BWA", "VGB", "CMR", "CAN", "CYM", "DMA", "FJI",
            "GMB", "GHA", "GIB", "GRD", "GUY", "IRL", "JAM", "KEN", "LSO", "MWI", "MLT", "MUS",
            "MSR", "NAM", "NZL", "NGA", "PNG", "SYC", "SLE", "SGP", "SLB", "ZAF", "SWZ", "TZA",
            "TON", "TTO", "TCA", "UGA", "GBR", "USA", "VUT", "ZMB", "ZWE");
    private static final List<String> LANGUAGE_NAME_TAGS_DEFAULT = Arrays.asList("name:en");
    private static final List<String> LOWER_CASE_WORDS_DEFAULT = Arrays.asList("and", "to", "of", "the");
    private static final String SPECIAL_CHARACTERS_DEFAULT = "-/(&";
    private static final List<String> NAME_AFFIXES_DEFAULT = Arrays.asList("Mc", "Mac", "Mck",
            "Mhic", "Mic");

    // A list of countries where the name tag should be checked
    private final List<String> checkNameCountries;
    // A list of language specific name tags to check
    private final List<String> languageNameTags;
    // A list of words that are normally lower case in names
    private final List<String> lowerCaseWords;
    // A string of characters that can proceed a capital letter
    private final String specialCharacters;
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
        this.lowerCaseWords = (List<String>) configurationValue(configuration, "lower_case_words",
                LOWER_CASE_WORDS_DEFAULT);
        this.specialCharacters = (String) configurationValue(configuration,
                "characters.capital.prefix", SPECIAL_CHARACTERS_DEFAULT);
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

        if (checkNameCountries.contains(object.tag(ISOCountryTag.KEY).toUpperCase())
                && Validators.hasValuesFor(object, NameTag.class)
                && isMixedCase(osmTags.get(NameTag.KEY)))
        {
            mixedCaseNameTags.add("name");
        }
        for (final String key : languageNameTags)
        {
            if (osmTags.containsKey(key) && isMixedCase(osmTags.get(key)))
            {
                mixedCaseNameTags.add(key);
            }

        }

        if (!mixedCaseNameTags.isEmpty())
        {
            this.markAsFlagged(object.getOsmIdentifier());
            final String osmType;
            if (object instanceof LocationItem)
            {
                osmType = "Node";
            }
            else
            {
                osmType = "Way";
            }
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
     * @param value
     * @return
     */
    private boolean isMixedCase(final String value)
    {
        // Split into words based on spaces
        final String[] wordArray = value.split(" ");
        // Check each word
        for (final String word : wordArray)
        {
            // If there is more than 1 word and the word is not in the lower case list: check that
            // the first letter is a capital
            if (wordArray.length > 1 && !lowerCaseWords.contains(word))
            {
                final Matcher firstLetterMatcher = Pattern.compile("\\p{L}").matcher(word);
                if (firstLetterMatcher.find()
                        && Character.isLowerCase(firstLetterMatcher.group().charAt(0)))
                {
                    return true;
                }
            }
            // If the word is not all upper case: check if all the letters not following config
            // specified characters and strings, and apostrophes, are lower case
            if (Pattern.compile("[\\p{L}&&[^\\p{Lu}]]").matcher(word).find()
                    && Pattern
                            .compile(String.format(
                                    "(\\p{L}.*(?<![\\Q%1$s\\E']|%2$s)(\\p{Lu}))|(\\p{L}.*(?<=')\\p{Lu}(?!.))",
                                    this.specialCharacters, this.nameAffixes))
                            .matcher(word).find())
            {
                return true;
            }
        }
        return false;
    }
}
