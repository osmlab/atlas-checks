package org.openstreetmap.atlas.checks.validation.tag;

import java.util.*;
import java.util.Arrays;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.AddressStreetTag;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags different values (ie "ss", "\u00df", or deprecated tags) for each country specified. Config file contains
 * all the tags required to tag or not to tag specified for each country. The check looks at the object's ISO code and
 * then pulls all the needed tags for that country from configuration.json .
 *
 * @author v-naydinyan
 */
public class StreetNameCheck extends BaseCheck<Long>
{
    private static final List<String> ALL_COUNTRIES_ISO_DEFAULT = List.of("AUT", "CHE", "DEU", "LIE");
    private static final List<List<String>> ALL_VALUES_TO_FLAG_DEFAULT = List.of(List.of("strasse"),
            List.of("stra\u00dfe"), List.of("strasse"), List.of("stra\u00dfe"));
    private static final List<List<String>> ALL_VALUES_NOT_FLAG_DEFAULT = List.of(List.of("strasser"),
            List.of(), List.of("strasser"), List.of());
    private static final List<List<String>> ALL_DEPRECATED_VALUES_TO_FLAG_DEFAULT = List.of(List.of(),
            List.of(), List.of("associatedstreet"), List.of());
    private static final List<List<String>> ALL_ITEMS_TO_FLAG_DEFAULT = List.of(List.of("ss"),
            List.of("\u00df"), List.of("ss"), List.of("\u00df"));

    private static final String CONTAINS_VALUE_INSTRUCTION = "The object contains flagged tags: {0}";
    private static final String CONTAINS_DEPRECATED_VALUE_INSTRUCTION = "The type tag {0} is deprecated.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(CONTAINS_VALUE_INSTRUCTION,
            CONTAINS_DEPRECATED_VALUE_INSTRUCTION);


    private final List<String> allCountriesIsoConfig;
    private final List<List<String>> allValuesToFlagConfig;
    private final List<List<String>> allValuesNotFlagConfig;
    private final List<List<String>> allDeprecatedValuesToFlagConfig;
    private final List<List<String>> allItemsToFlagConfig;


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

        this.allCountriesIsoConfig = this.configurationValue(configuration, "check.countries", ALL_COUNTRIES_ISO_DEFAULT);
        this.allValuesToFlagConfig = this.configurationValue(configuration,
                "check.containsValues", ALL_VALUES_TO_FLAG_DEFAULT);
        this.allValuesNotFlagConfig = this.configurationValue(configuration,
                "check.notContainsValues", ALL_VALUES_NOT_FLAG_DEFAULT);
        this.allDeprecatedValuesToFlagConfig = this.configurationValue(configuration,
                "check.deprecatedValues", ALL_DEPRECATED_VALUES_TO_FLAG_DEFAULT);
        this.allItemsToFlagConfig = this.configurationValue(configuration, "check.tags", ALL_ITEMS_TO_FLAG_DEFAULT);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     * The function checks that the object is of type Node, Relation or Way (only include MainEdge)
     *      and that is has not been flagged already.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // Checks that the object is in the ISO list and is Node, Edge or Relation
        return (!this.isFlagged(object.getOsmIdentifier()) && (object instanceof
                Node || (object instanceof Edge && ((Edge) object).isMainEdge()) || object instanceof Relation));
    }


    /**
     * The function makes sure that the edges are flagged as ways.
     * credit: Brian Jorgenson
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
     * @return an optional {@link CheckFlag} object that contains that values in the tag that need to be flagged
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // a list of <ObjectType> that contains all important tags for the object's ISO.
        var countryInfo = createCountryInfo(object);

        if (countryInfo != null) {
            final ArrayList<ArrayList<String>> allValuesFoundInObject = objectContainsValues(object, countryInfo);
            this.markAsFlagged(object.getOsmIdentifier());

            // the item contains a flagged tag but does not contain a tag that isn't to be flagged
            if (!allValuesFoundInObject.get(0).isEmpty() && allValuesFoundInObject.get(1).isEmpty()) {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0, allValuesFoundInObject.get(0))));
            }

            // the item contains a deprecated tag
            if ((allValuesFoundInObject.get(2) != null) && !allValuesFoundInObject.get(2).isEmpty()) {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(1, allValuesFoundInObject.get(2))));
            }
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions() { return FALLBACK_INSTRUCTIONS; }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @param countryInfo
     *            the countryInfo object that contains all the values needed for flagging
     * @return an array that contains tags that have been found at their appropriate indeces: 0 = contains tag,
     *         1 = not contains tag, 2 = tag is deprecated
     */
    private ArrayList<ArrayList<String>> objectContainsValues(final AtlasObject object, final CountryInfo countryInfo) {
        final Map<String, String> tags = object.getTags();
        String objectISO = tags.get(ISOCountryTag.KEY);
        String deprecatedValueCountry = "DEU";
        String streetTag = tags.get(AddressStreetTag.KEY);
        String nameTag = tags.get(NameTag.KEY);
        String typeTag = tags.get(RelationTypeTag.KEY);

        List<String> valuesToFlag = countryInfo.getTagContains();
        List<String> valuesToNotFlag = countryInfo.getTagNotContains();
        List<String> deprecatedValuesToFlag = countryInfo.getDeprecatedTags();

        final ArrayList<ArrayList<String>> contains = new ArrayList<>(3);
        contains.add(0, findValuesToFlag(streetTag, nameTag, valuesToFlag));
        contains.add(1, findValuesToNotFlag(streetTag, nameTag, valuesToNotFlag));

        if (objectISO.equalsIgnoreCase(deprecatedValueCountry)) {
            contains.add(2, findDeprecatedValuesToFlag(typeTag, deprecatedValuesToFlag));
        }
        else {
            contains.add(2, null);
        }

        return contains;
    }

    /**
     *
     * @param streetTag
     *              the street tag of the object, null if doesn't exist
     * @param nameTag
     *              the name tag of the object, null if doesn't exist
     * @param containTags
     *              the list of values that need to be flagged
     * @return an ArrayList of Strings, tags that need to be flagged found in object, otherwise returns null
     */
    private ArrayList<String> findValuesToFlag(String streetTag, String nameTag, List<String> containTags) {
        ArrayList<String> valuesToFlagInObject = new ArrayList<>();
        if (!containTags.isEmpty() && (streetTag != null || nameTag != null)) {
            containTags.forEach(tag -> {
                if ((streetTag != null && streetTag.toLowerCase().contains(String.valueOf(tag)))
                        || (nameTag != null && nameTag.toLowerCase().contains(String.valueOf(tag)))) {
                    valuesToFlagInObject.add(String.valueOf(tag));
                }
            });
        }

        return valuesToFlagInObject;

    }

    /**
     *
     * @param streetTag
     *              the street tag of the object, null if doesn't exist
     * @param nameTag
     *              the name tag of the object, null if doesn't exist
     * @param notContainTags
     *              list of values that are not to be flagged
     * @return an ArrayList of Strings, tags that don't need to be flagged found in object, otherwise returns null
     */
    private ArrayList<String> findValuesToNotFlag(String streetTag, String nameTag, List<String> notContainTags) {
        ArrayList<String> valuesToNotFlagInObject = new ArrayList<>();
        if (!notContainTags.isEmpty() && (streetTag != null || nameTag != null)) {
            notContainTags.forEach(tag -> {
                if ((streetTag != null && streetTag.toLowerCase().contains(String.valueOf(tag)))
                        || (nameTag != null && nameTag.toLowerCase().contains(String.valueOf(tag)))) {
                    valuesToNotFlagInObject.add(String.valueOf(tag));
                }
            });
        }

        return valuesToNotFlagInObject;

    }

    /**
     *
     * @param typeTag
     *              the type tag of the object, null if it doesn't exist
     * @param deprecatedTags
     *              the deprecated values that need to be flagged
     * @return return the items
     */
    private ArrayList<String> findDeprecatedValuesToFlag(String typeTag, List<String> deprecatedTags) {
        ArrayList<String> deprecatedValuesToFlagInObject = new ArrayList<>();
        if (!deprecatedTags.isEmpty() && (typeTag != null)) {
            deprecatedTags.forEach(tag -> {
                if (typeTag.toLowerCase().contains(String.valueOf(tag.toLowerCase()))) {
                    deprecatedValuesToFlagInObject.add(String.valueOf(tag));
                }
            });
        }

        return deprecatedValuesToFlagInObject;

    }


    /**
     * The function creates an object of class CountryInfo if the ISO code of it is in the config variables.
     * @param object
     *          the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return object of class CountryInfo or null
     */
    private CountryInfo createCountryInfo(final AtlasObject object){
        final int objectIndex = this.allCountriesIsoConfig.indexOf(object.tag(ISOCountryTag.KEY));
        if (objectIndex < 0) {
            return null;
        }
        return new CountryInfo(this.allValuesToFlagConfig.get(objectIndex),
                this.allValuesNotFlagConfig.get(objectIndex), this.allDeprecatedValuesToFlagConfig.get(objectIndex), this.allItemsToFlagConfig.get(objectIndex));

    }

    class CountryInfo {
        List<String> tagContains;
        List<String> tagNotContains;
        List<String> deprecatedTags;
        List<String> values;

        public CountryInfo (
                List<String> contains,
                List<String> notContains,
                List<String> deprecated,
                List<String> tags
        ) {
            this.tagContains = contains;
            this.tagNotContains = notContains;
            this.deprecatedTags = deprecated;
            this.values = tags;
        }

        public List<String> getTagContains() {return this.tagContains;}
        public List<String> getTagNotContains() {return this.tagNotContains;}
        public List<String> getDeprecatedTags() {return this.deprecatedTags;}
        public List<String> getVals() {return this.values;}
    }

}

