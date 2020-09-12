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
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags objects with name tags that improperly use mixed cases.
 *
 * @author bbreithaupt
 */
public class MixedCaseNameCheck extends BaseCheck<String>
{

    private static final long serialVersionUID = 7109483897229499466L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "{0} {1,number,#} has (an) invalid mixed-case value(s) for the following tag(s): {2}.");
    private static final List<String> CHECK_NAME_COUNTRIES_DEFAULT = Arrays.asList("AIA", "ATG",
            "AUS", "BHS", "BRB", "BLZ", "BMU", "BWA", "VGB", "CMR", "CAN", "CYM", "DMA", "FJI",
            "GMB", "GHA", "GIB", "GRD", "GUY", "IRL", "JAM", "KEN", "LSO", "MWI", "MLT", "MUS",
            "MSR", "NAM", "NZL", "NGA", "PNG", "SYC", "SLE", "SGP", "SLB", "ZAF", "SWZ", "TZA",
            "TON", "TTO", "TCA", "UGA", "GBR", "USA", "VUT", "ZMB", "ZWE");
    private static final List<String> LANGUAGE_NAME_TAGS_DEFAULT = Arrays.asList("name:en");
    private static final List<String> LOWER_CASE_PREPOSITIONS_DEFAULT = Arrays.asList("and", "from",
            "to", "of", "by", "upon", "on", "off", "at", "as", "into", "like", "near", "onto",
            "per", "till", "up", "via", "with", "for", "in");
    private static final List<String> LOWER_CASE_ARTICLES_DEFAULT = Arrays.asList("a", "an", "the");
    private static final String SPLIT_CHARACTERS_DEFAULT = " -/&@–";
    private static final List<String> NAME_AFFIXES_DEFAULT = Arrays.asList("Mc", "Mac", "Mck",
            "Mhic", "Mic");
    private static final List<String> MIXED_CASE_UNITS_DEFAULT = Arrays.asList("kV");

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
    // Know intentionally mixed case words
    private final String mixedCaseUnits;

    // Regex Patterns
    private final Pattern upperCasePattern;
    private final Pattern anyLetterPattern;
    private final Pattern lowerCasePattern;
    private final Pattern mixedCaseUnitsPattern;
    private final Pattern mixedCaseApostrophePattern;
    private final Pattern nonFirstCapitalPattern;

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
                "name.prepositions", LOWER_CASE_PREPOSITIONS_DEFAULT);
        this.lowerCaseArticles = (List<String>) configurationValue(configuration, "name.articles",
                LOWER_CASE_ARTICLES_DEFAULT);
        this.splitCharacters = (String) configurationValue(configuration, "regex.split",
                SPLIT_CHARACTERS_DEFAULT);
        this.nameAffixes = (String) configurationValue(configuration, "name.affixes",
                NAME_AFFIXES_DEFAULT, value -> String.join("|", (List<String>) value));
        this.mixedCaseUnits = (String) configurationValue(configuration, "name.units",
                MIXED_CASE_UNITS_DEFAULT, value -> String.join("|", (List<String>) value));

        // Compile regex
        this.upperCasePattern = Pattern.compile("\\p{Lu}");
        this.anyLetterPattern = Pattern.compile("\\p{L}");
        this.lowerCasePattern = Pattern.compile("\\p{Ll}");
        this.mixedCaseUnitsPattern = Pattern.compile(
                String.format("[^\\p{L}]*\\p{Digit}[%1$s][^\\p{L}]*", this.mixedCaseUnits));
        this.mixedCaseApostrophePattern = Pattern
                .compile("([^\\p{Ll}]+'\\p{Ll})|([^\\p{Ll}]+\\p{Ll}')");
        this.nonFirstCapitalPattern = Pattern.compile(String.format(
                "(\\p{L}.*(?<!'|%1$s)(\\p{Lu}))|(\\p{L}.*(?<=')\\p{Lu}(?!.))", this.nameAffixes));
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
        // Valid objects are items that were OSM nodes or ways (Equivalent to Atlas nodes, points,
        // edges, lines and areas)
        return !(object instanceof Relation) && !this.isFlagged(this.getUniqueOSMIdentifier(object))
                && (object.getTags().containsKey(ISOCountryTag.KEY)
                        // Must have an ISO code that is in checkNameCountries...
                        && this.checkNameCountries
                                .contains(object.tag(ISOCountryTag.KEY).toUpperCase())
                        // And have a name tag
                        && Validators.hasValuesFor(object, NameTag.class)
                        // And if an Edge, is a main edge
                        && (!(object instanceof Edge) || ((Edge) object).isMainEdge())
                        // Or it must have a specific language name tag from languageNameTags
                        || this.languageNameTags.stream()
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
        final String country = object.tag(ISOCountryTag.KEY);
        if (country != null && this.checkNameCountries.contains(country.toUpperCase())
                && Validators.hasValuesFor(object, NameTag.class)
                && isMixedCase(osmTags.get(NameTag.KEY)))
        {
            mixedCaseNameTags.add(NameTag.KEY);
        }
        // Check all language name tags
        for (final String key : this.languageNameTags)
        {
            if (osmTags.containsKey(key) && isMixedCase(osmTags.get(key)))
            {
                mixedCaseNameTags.add(key);
            }
        }

        // If mix case id detected, flag
        if (!mixedCaseNameTags.isEmpty())
        {
            this.markAsFlagged(this.getUniqueOSMIdentifier(object));

            // Instruction includes type of OSM object and list of flagged tags
            final String instruction = this.getLocalizedInstruction(0,
                    object instanceof LocationItem ? "Node" : "Way", object.getOsmIdentifier(),
                    String.join(", ", mixedCaseNameTags));

            // Generate the flag
            if (object instanceof Edge)
            {
                return Optional.of(this.createFlag(new OsmWayWalker((Edge) object).collectEdges(),
                        instruction));
            }
            return Optional.of(this.createFlag(object, instruction));
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
        // Check if it is all lower case
        if (this.upperCasePattern.matcher(value).find())
        {
            // Split into words based on configurable characters
            final String[] wordArray = value.split("[\\Q" + this.splitCharacters + "\\E]");
            boolean firstWord = true;
            // Check each word
            for (final String word : wordArray)
            {
                // Check if the word is intentionally mixed case
                if (!isMixedCaseUnit(word))
                {
                    final Matcher firstLetterMatcher = this.anyLetterPattern.matcher(word);
                    // If the word is not in the list of prepositions, and the
                    // word is not both in the article list and not the first word: check that
                    // the first letter is a capital
                    if (!this.lowerCasePrepositions.contains(word)
                            && !(!firstWord && this.lowerCaseArticles.contains(word))
                            // If the first letter is lower case: return true if it is not preceded
                            // by a number
                            && firstLetterMatcher.find()
                            && Character.isLowerCase(firstLetterMatcher.group().charAt(0))
                            && !(firstLetterMatcher.start() != 0 && Character
                                    .isDigit(word.charAt(firstLetterMatcher.start() - 1)))
                            // If the word is not all upper case: check if all the letters not
                            // following apostrophes, unless at the end of the word, are lower case
                            || this.lowerCasePattern.matcher(word).find()
                                    && !isMixedCaseApostrophe(word)
                                    && isProperNonFirstCapital(word))
                    {
                        return true;
                    }
                }
                firstWord = false;
            }
        }
        return false;
    }

    /**
     * Tests a {@link String} for being all upper case, except the last letter which is adjacent to
     * an apostrophe (ex. MAX's).
     *
     * @param word
     *            {@link String} to test
     * @return true if a lower case letter is found preceding or following an apostrophe that is the
     *         last or second to last character in the string, and all other letters are upper case
     */
    private boolean isMixedCaseApostrophe(final String word)
    {
        // This returns true if the last 2 characters are an apostrophe and a lower case letter, and
        // all other letters are upper case.
        return this.mixedCaseApostrophePattern.matcher(word).matches();
    }

    /**
     * Tests a {@link String} against a configurable list of unit abbreviations.
     *
     * @param word
     *            {@link String} to test
     * @return true if {@code word} contains a mixed case unit abbreviation preceded by a number,
     *         and it does not contain any other alphabetic characters.
     */
    private boolean isMixedCaseUnit(final String word)
    {
        // This returns true if one of the items in this.mixedCaseUnits is preceded by a number -
        // `\p{Digit}`
        // There may be 0 or more non-alphabetic characters proceeding or following the
        // digit+mixedCaseUnits - `[^\p{L}]*`
        return this.mixedCaseUnitsPattern.matcher(word).find();
    }

    /**
     * Tests a {@link String} for incorrect capitalization, excluding the first letter.
     *
     * @param word
     *            {@link String} to test
     * @return true if a capital letter is incorrectly used
     */
    private boolean isProperNonFirstCapital(final String word)
    {
        // This checks each capital letter for incorrect usage
        // It does not check the first letter - `(\p{L}.*`
        // To be incorrect usage a capital letter:
        // Must not be preceded by an apostrophe or name affix - `(?<!'|%1$s)(\p{Lu})`
        // Must not be the last character if it follows an apostrophe - `(?<=')\p{Lu}(?!.)`
        return this.nonFirstCapitalPattern.matcher(word).find();
    }
}
