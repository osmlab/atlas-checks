package org.openstreetmap.atlas.checks.validation.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.AddressStreetTag;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags different values "ss" or "ß" for each country specified. Config file contains all the tags
 * required to tag or not to tag specified for each country. The check looks at the object's ISO
 * code and then pulls all the needed tags for that country from configuration.json .
 *
 * @author v-naydinyan
 */
public class StreetNameCheck extends BaseCheck<Long>
{

    /**
     * The object of class CountryInfo contains all the tags and values needed to be flagged for the
     * object.
     */
    class CountryInfo
    {
        private List<String> tagContains;
        private List<String> tagNotContains;
        private List<String> tagValues;
        private List<String> correctValues;

        CountryInfo(final List<String> contains, final List<String> notContains,
                final List<String> tags, final List<String> correctTags)
        {
            this.tagContains = contains;
            this.tagNotContains = notContains;
            this.tagValues = tags;
            this.correctValues = correctTags;
        }

        public List<String> getCorrectValues()
        {
            return this.correctValues;
        }

        public List<String> getTagContains()
        {
            return this.tagContains;
        }

        public List<String> getTagNotContains()
        {
            return this.tagNotContains;
        }

        public List<String> getTagValues()
        {
            return this.tagValues;
        }
    }

    private static final List<String> ALL_COUNTRIES_ISO_DEFAULT = List.of("AUT", "CHE", "DEU",
            "LIE");
    private static final List<List<String>> ALL_VALUES_TO_FLAG_DEFAULT = List.of(List.of("strasse"),
            List.of("straße"), List.of("strasse"), List.of("straße"));
    private static final List<List<String>> ALL_VALUES_NOT_FLAG_DEFAULT = List
            .of(List.of("strasser"), List.of(), List.of("strasser"), List.of());
    private static final List<List<String>> ALL_ITEMS_TO_FLAG_DEFAULT = List.of(List.of("ss"),
            List.of("ß"), List.of("ss"), List.of("ß"));
    private static final List<List<String>> ALL_CORRECT_ITEMS_TO_SUBSTITUTE = List.of(List.of("ß"),
            List.of("ss"), List.of("ß"), List.of("ss"));

    private static final String CONTAINS_VALUE_INSTRUCTION = "The name of the object contains the value {0}. Please substitute it with an appropriate value {1}.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(CONTAINS_VALUE_INSTRUCTION);

    private final List<String> allCountriesIsoConfig;
    private final List<List<String>> allValuesToFlagConfig;
    private final List<List<String>> allValuesNotFlagConfig;
    private final List<List<String>> allItemsToFlagConfig;
    private final List<List<String>> allCorrectValuesToSubstitute;

    private static final long serialVersionUID = 3579562381907303707L;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public StreetNameCheck(final Configuration configuration)
    {
        super(configuration);

        this.allCountriesIsoConfig = this.configurationValue(configuration, "check.countries",
                ALL_COUNTRIES_ISO_DEFAULT);
        this.allValuesToFlagConfig = this.configurationValue(configuration, "check.containsValues",
                ALL_VALUES_TO_FLAG_DEFAULT);
        this.allValuesNotFlagConfig = this.configurationValue(configuration,
                "check.notContainsValues", ALL_VALUES_NOT_FLAG_DEFAULT);
        this.allItemsToFlagConfig = this.configurationValue(configuration, "check.tags",
                ALL_ITEMS_TO_FLAG_DEFAULT);
        this.allCorrectValuesToSubstitute = this.configurationValue(configuration,
                "check.correctTags", ALL_CORRECT_ITEMS_TO_SUBSTITUTE);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check. The function
     * checks that the object is of type Node, Relation or Way (only include MainEdge) and that is
     * has not been flagged already.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // Checks that the object is in the ISO list and is Node, Edge or Relation
        return !this.isFlagged(object.getOsmIdentifier()) && (object instanceof Node
                || (object instanceof Edge && ((Edge) object).isMainEdge())
                || object instanceof Relation);
    }

    /**
     * The function makes sure that the edges are flagged as ways. credit: Brian Jorgenson
     */
    @Override
    protected CheckFlag createFlag(final AtlasObject object, final String instruction)
    {
        if (object instanceof Edge)
        {
            return super.createFlag(new OsmWayWalker((Edge) object).collectEdges(), instruction);
        }
        return super.createFlag(object, instruction);
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that contains that values in the tag that need
     *         to be flagged
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // a list of <ObjectType> that contains all important tags for the object's ISO.
        final var countryInfo = this.createCountryInfo(object);

        if (countryInfo != null)
        {
            final ArrayList<ArrayList<String>> allValuesFoundInObject = this
                    .objectContainsValues(object, countryInfo);
            this.markAsFlagged(object.getOsmIdentifier());

            // the item contains a flagged tag but does not contain a tag that isn't to be flagged
            if (!allValuesFoundInObject.get(0).isEmpty() && allValuesFoundInObject.get(1).isEmpty())
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0,
                        countryInfo.getTagValues(), countryInfo.getCorrectValues())));
            }
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an object of class CountryInfo if the index of the object is in the config file
     */
    private CountryInfo createCountryInfo(final AtlasObject object)
    {
        final int objectIndex;
        final String isoTag = object.tag(ISOCountryTag.KEY);
        if (isoTag != null)
        {
            objectIndex = this.allCountriesIsoConfig.indexOf(object.tag(ISOCountryTag.KEY));
        }
        else
        {
            objectIndex = -1;
        }
        if (objectIndex < 0)
        {
            return null;
        }
        return new CountryInfo(this.allValuesToFlagConfig.get(objectIndex),
                this.allValuesNotFlagConfig.get(objectIndex),
                this.allItemsToFlagConfig.get(objectIndex),
                this.allCorrectValuesToSubstitute.get(objectIndex));
    }

    /**
     * @param streetTag
     *            the street tag of the object, null if doesn't exist
     * @param nameTag
     *            the name tag of the object, null if doesn't exist
     * @param containTags
     *            the list of values that need to be flagged
     * @return an ArrayList of Strings, tags that need to be flagged found in object, otherwise
     *         returns null
     */
    private ArrayList<String> findValuesToFlag(final String streetTag, final String nameTag,
            final List<String> containTags)
    {
        final ArrayList<String> valuesToFlagInObject = new ArrayList<>();
        if (!containTags.isEmpty() && (streetTag != null || nameTag != null))
        {
            containTags.forEach(tag ->
            {
                if ((streetTag != null && streetTag.toLowerCase().contains(String.valueOf(tag)))
                        || (nameTag != null && nameTag.toLowerCase().contains(String.valueOf(tag))))
                {
                    valuesToFlagInObject.add(String.valueOf(tag));
                }
            });
        }

        return valuesToFlagInObject;

    }

    /**
     * @param streetTag
     *            the street tag of the object, null if doesn't exist
     * @param nameTag
     *            the name tag of the object, null if doesn't exist
     * @param notContainTags
     *            list of values that are not to be flagged
     * @return an ArrayList of Strings, tags that don't need to be flagged found in object,
     *         otherwise returns null
     */
    private ArrayList<String> findValuesToNotFlag(final String streetTag, final String nameTag,
            final List<String> notContainTags)
    {
        final ArrayList<String> valuesToNotFlagInObject = new ArrayList<>();
        if (!notContainTags.isEmpty() && (streetTag != null || nameTag != null))
        {
            notContainTags.forEach(tag ->
            {
                if ((streetTag != null && streetTag.toLowerCase().contains(String.valueOf(tag)))
                        || (nameTag != null && nameTag.toLowerCase().contains(String.valueOf(tag))))
                {
                    valuesToNotFlagInObject.add(String.valueOf(tag));
                }
            });
        }

        return valuesToNotFlagInObject;

    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @param countryInfo
     *            the countryInfo object that contains all the values needed for flagging
     * @return an array that contains tags that have been found at their appropriate indeces: 0 =
     *         contains tag, 1 = not contains tag, 2 = tag is deprecated
     */
    private ArrayList<ArrayList<String>> objectContainsValues(final AtlasObject object,
            final CountryInfo countryInfo)
    {
        final Map<String, String> tags = object.getTags();
        final String streetTag = tags.get(AddressStreetTag.KEY);
        final String nameTag = tags.get(NameTag.KEY);

        final List<String> valuesToFlag = countryInfo.getTagContains();
        final List<String> valuesToNotFlag = countryInfo.getTagNotContains();

        final ArrayList<ArrayList<String>> contains = new ArrayList<>(2);
        contains.add(0, this.findValuesToFlag(streetTag, nameTag, valuesToFlag));
        contains.add(1, this.findValuesToNotFlag(streetTag, nameTag, valuesToNotFlag));

        return contains;
    }

}
