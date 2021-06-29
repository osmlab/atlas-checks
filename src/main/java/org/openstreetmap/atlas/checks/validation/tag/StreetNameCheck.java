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
 * Auto generated Check template
 *
 * @author v-naydinyan
 */
public class StreetNameCheck extends BaseCheck
{
    private static final List<String> COUNTRIES_DEFAULT = Arrays.asList("AUT", "CHE", "DEU", "LIE");
    private static final List<List<String>> CONTAINS_TAGS_DEFAULT = Arrays.asList(Arrays.asList("strasse"),
            Arrays.asList("stra\u00dfe"), Arrays.asList("strasse"), Arrays.asList("stra\u00dfe"));
    private static final List<List<String>> NOT_CONTAINS_TAGS_DEFAULT = Arrays.asList(Arrays.asList("strasser"),
            Arrays.asList(), Arrays.asList("strasser"), Arrays.asList());
    private static final List<List<String>> DEPRECATED_TAGS_DEFAULT = Arrays.asList(Arrays.asList(),
            Arrays.asList(), Arrays.asList("associatedstreet"), Arrays.asList());
    private static final List<List<String>> TAGS_DEFAULT = Arrays.asList(Arrays.asList("ss"),
            Arrays.asList("\u00df"), Arrays.asList("ss"), Arrays.asList("\u00df"));

    private static final String CONTAINS_TAG_INSTRUCTION = "The object contains flagged tags: {0}";
    private static final String CONTAINS_DEPRECATED_TAG_INSTRUCTION = "The type tag {0} is deprecated.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(CONTAINS_TAG_INSTRUCTION,
            CONTAINS_DEPRECATED_TAG_INSTRUCTION);


    private final List<String> countriesConfig;
    private final List<List<String>> containsTagsConfig;
    private final List<List<String>> notContainsTagsConfig;
    private final List<List<String>> deprecatedTagsConfig;
    private final List<List<String>> tagsConfig;


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

        this.countriesConfig = (List<String>) this.configurationValue(configuration, "check.countries", COUNTRIES_DEFAULT);
        this.containsTagsConfig = (List<List<String>>) this.configurationValue(configuration,
                "check.containsTags", CONTAINS_TAGS_DEFAULT);
        this.notContainsTagsConfig = (List<List<String>>) this.configurationValue(configuration,
                "check.notContainsTags", NOT_CONTAINS_TAGS_DEFAULT);
        this.deprecatedTagsConfig = (List<List<String>>) this.configurationValue(configuration,
                "check.deprecatedTags", DEPRECATED_TAGS_DEFAULT);
        this.tagsConfig = (List<List<String>>) this.configurationValue(configuration, "check.tags", TAGS_DEFAULT);
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
        // Checks that the object is in the ISO list and is Node, Edge or Relation
        return (!this.isFlagged(object.getOsmIdentifier()) && (object instanceof
                Node || (object instanceof Edge && ((Edge) object).isMainEdge()) || object instanceof Relation));
    }

    @Override
    /**
     * The function that flags an item. If it is an edge, then it flags the entire way, otherwise flags the object.
     * The function written by Brian Jorgenson in ConstructionCheck.java.
     */
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
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // a list of <ObjectType> that contains all important tags for the object's ISO.
        final CountryInfo countryInfo = createCountryInfo(object);

        if (countryInfo != null) {
            final ArrayList<ArrayList<String>> objectAllTags = objectContainsTag(object, countryInfo);
            this.markAsFlagged(object.getOsmIdentifier());

            // the item contains a flagged tag but does not contain a tag that isn't to be flagged
            if (objectAllTags.get(0) != null && objectAllTags.get(1) == null) {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0, objectAllTags.get(0))));
            }

            // the item contains a deprecated tag
            if (objectAllTags.get(2) != null) {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(1, objectAllTags.get(2))));
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
     *            the countryInfo object that contains all the tags needed for flagging
     * @return an array that contains tags that have been found at their appropriate indeces: 0 = contains tag,
     *         1 = not contains tag, 2 = tag is deprecated
     */
    private ArrayList<ArrayList<String>> objectContainsTag(final AtlasObject object, final CountryInfo countryInfo) {
        final Map<String, String> tags = object.getTags();
        String street_tag = tags.get(AddressStreetTag.KEY);
        String name_tag = tags.get(NameTag.KEY);
        String type_tag = tags.get(RelationTypeTag.KEY);

        List<String> tagsToFlag = countryInfo.getTagContains();
        List<String> tagsNotFlag = countryInfo.getTagNotContains();
        List<String> deprecatedTagsFlag = countryInfo.getDeprecatedTags();

        final ArrayList<ArrayList<String>> contains = new ArrayList<ArrayList<String>>(3);

        final ArrayList<String> containedTags = containTags(street_tag, name_tag, tagsToFlag);
        contains.add(0, containedTags);

        final ArrayList<String> notContainedTags = notContainTags(street_tag, name_tag, tagsNotFlag);
        contains.add(1, notContainedTags);

        final ArrayList<String> deprecatedItems = deprecatedTags(type_tag, deprecatedTagsFlag);
        contains.add(2, deprecatedItems);


        return contains;
    }

    private ArrayList<String> containTags(String street_tag, String name_tag, List<String> containTags) {
        ArrayList<String> flaggedTags = new ArrayList<String>();
        if (!containTags.isEmpty() && (street_tag != null || name_tag != null)) {
            containTags.forEach(tag -> {
                if ((street_tag != null && street_tag.toLowerCase().contains(String.valueOf(tag)))
                        || (name_tag != null && name_tag.toLowerCase().contains(String.valueOf(tag)))) {
                    flaggedTags.add(String.valueOf(tag));
                }
            });
        }

        if (!flaggedTags.isEmpty()) {
            return flaggedTags;
        }
        return null;

    }

    private ArrayList<String> notContainTags(String street_tag, String name_tag, List<String> notContainTags) {
        ArrayList<String> nonFlaggedTags = new ArrayList<String>();
        if (!notContainTags.isEmpty() && (street_tag != null || name_tag != null)) {
            notContainTags.forEach(tag -> {
                if ((street_tag != null && street_tag.toLowerCase().contains(String.valueOf(tag)))
                        || (name_tag != null && name_tag.toLowerCase().contains(String.valueOf(tag)))) {
                    nonFlaggedTags.add(String.valueOf(tag));
                }
            });
        }

        if (!nonFlaggedTags.isEmpty()) {
            return nonFlaggedTags;
        }
        return null;

    }

    private ArrayList<String> deprecatedTags(String type_tag, List<String> deprecatedTags) {
        ArrayList<String> deprecatedFlags = new ArrayList<String>();
        if (!deprecatedTags.isEmpty() && (type_tag != null)) {
            deprecatedTags.forEach(tag -> {
                if (type_tag.toLowerCase().contains(String.valueOf(tag))) {
                    deprecatedFlags.add(String.valueOf(tag));
                }
            });
        }

        if (!deprecatedFlags.isEmpty()) {
            return deprecatedFlags;
        }
        return null;

    }

//    private ArrayList<ArrayList<String>> indexNegative (AtlasObject object) {
//        String objectISOs = object.tag(ISOCountryTag.KEY);
//        List<String> allISOs = Arrays.asList(objectISOs.split(","));
//        final ArrayList<ArrayList<String>> allIsoInfo = new ArrayList<>();
//        allISOs.forEach(s -> {
//            final CountryInfo countryInfo = createCountryInfo(s);
//            final ArrayList<String> countryTags = objectContainsTag(object, countryInfo);
//            countryTags.add(s);
//            allIsoInfo.add(countryTags);
//
//        });
//        return allIsoInfo;
//    }


    private CountryInfo createCountryInfo(final AtlasObject object){
        final int objectIndex = this.countriesConfig.indexOf(object.tag(ISOCountryTag.KEY));
        if (objectIndex < 0) {
            return null;
        }
        final CountryInfo countryInfo = new CountryInfo(this.containsTagsConfig.get(objectIndex),
                this.notContainsTagsConfig.get(objectIndex), this.deprecatedTagsConfig.get(objectIndex), this.tagsConfig.get(objectIndex));

        return countryInfo;

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

