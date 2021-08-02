package org.openstreetmap.atlas.checks.database.wikidata;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.hadoop.classification.InterfaceStability.Unstable;

/**
 * A WikiData bean for encoding/decoding WikiData table information. Any item with an @Unstable
 * annotation is not yet finalized. Please avoid using them, or expect breakage if you do.
 *
 * @author Taylor Smock
 */
public final class WikiData
{
    // This should probably be converted to Java 14 Records at some point.
    private static final Map<String, WikiData> createdWikiData = Collections
            .synchronizedMap(new TreeMap<>());
    private static final String EMPTY_STRING = "";
    private static final Pattern QUOTE_PATTERN = Pattern.compile("(^\")|(\"$)");
    private static final Pattern BRACKET_PATTERN = Pattern.compile("(^\\[)|(\\]$)");
    private final String identifier;
    private final Collection<String> instanceOfP2;
    private final String subclassOfP3;
    private final WikiDataItem statusP6;
    private final String wikidataEquivalentP7;
    private final Collection<String> formatterUrlP8;
    private final WikiDataItem keyTypeP9;
    private final String keyForThisTagP10;
    private final String proposalDiscussionP11;
    private final String wikiDataConceptP12;
    private final Pattern valueValidationRegexP13;
    private final String permanentKeyIdP16;
    private final Collection<String> redirectToP17;
    private final Collection<String> differentFromP18;
    private final String permanentTagIdP19;
    private final String propertyDifferentFromP20;
    private final String relationRoleIdP21;
    private final Collection<String> requiredKeyOrTagP22;
    private final String groupP25;
    private final Collection<String> limitedToLanguageP26;
    private final String imageP28;
    private final Collection<String> mustOnlyBeUsedInRegionP29;
    private final Collection<String> notToBeUsedInRegionP30;
    private final Collection<String> documentationWikiPagesP31;
    private final String languageCodeP32;
    private final boolean useOnNodesP33;
    private final boolean useOnWaysP34;
    private final boolean useOnAreasP35;
    private final boolean useOnRelationsP36;
    private final boolean useOnChangesetsP37;
    // Technically should be OSMCartoImageP39 to match OSM Wiki Data.
    private final String oSMCartoImageP39;
    private final String tagForThisRelationTypeP40;
    private final String permanentRelationTypeIdP41;
    private final Collection<String> belongsToRelationTypeP43;
    private final Collection<String> incompatibleWithP44;
    private final Collection<String> impliesP45;
    private final Collection<String> combinationP46;
    private final String imageCaptionP47;
    private final String limitedToRegionP48;
    private final Collection<String> geographicCodeP49;
    private final String redirectsToWikiPageP50;
    private final Collection<String> identicalToP51;

    /**
     * Clear wiki data items after a database update
     */
    public static void clearWikiData()
    {
        createdWikiData.clear();
    }

    /**
     * Get or create a WikiData item from a map
     *
     * @param wikiMap
     *            The map to convert to an item
     * @return A WikiData item or {@code null}, if the map does not contain an "id" key.
     */
    @Nullable
    public static WikiData getWikiData(final Map<String, Object> wikiMap)
    {
        Objects.requireNonNull(wikiMap, "Wiki Data Item map cannot be null");
        final var identifier = wikiMap.getOrDefault("id", "").toString();
        if (identifier.isBlank())
        {
            return null;
        }
        // First, do unsynchronized call (faster to not sync everything)
        if (createdWikiData.containsKey(identifier))
        {
            return createdWikiData.get(identifier);
        }
        // Then synchronize on the static instance so that multiple wikidata's are not
        // created
        synchronized (createdWikiData)
        {
            return createdWikiData.computeIfAbsent(identifier, tid -> new WikiData(wikiMap));
        }
    }

    /**
     * Get a wikidata that goes with an identifier
     *
     * @param identifier
     *            The identifier to get
     * @return A WikiData item or {@code null}, if one is not found.
     */
    @Nullable
    public static WikiData getWikiData(final String identifier)
    {
        Objects.requireNonNull(identifier, "id cannot be null");
        return createdWikiData.getOrDefault(identifier, null);
    }

    /**
     * Convert a string to a set collection ([0, 1, 2, 3, 3] -> [0, 1, 2, 3])
     *
     * @param string
     *            The string to convert
     * @return A collection (a "set") of items
     */
    private static Collection<String> stringToCollection(final String string)
    {
        if (string != null && !string.isBlank())
        {
            return Stream.of(BRACKET_PATTERN.matcher(string).replaceAll(EMPTY_STRING).split(",", 0))
                    .map(tString -> QUOTE_PATTERN.matcher(tString).replaceAll(EMPTY_STRING))
                    .map(String::trim)
                    .map(tString -> QUOTE_PATTERN.matcher(tString).replaceAll(EMPTY_STRING))
                    .collect(Collectors.toUnmodifiableSet());
        }
        return Collections.emptySet();
    }

    /**
     * Convert an object to a string
     *
     * @param object
     *            The object to convert to a string
     * @return The string for the object, or an empty string
     */
    @Nonnull
    private static String stringify(@Nullable final Object object)
    {
        if (object != null)
        {
            return object.toString();
        }
        return EMPTY_STRING;
    }

    /**
     * Create a WikiData item
     *
     * @param wikiMap
     *            The map to use to create the WikiData item
     */
    private WikiData(final Map<String, Object> wikiMap)
    {
        this.identifier = wikiMap.get("id").toString();
        this.instanceOfP2 = stringToCollection(stringify(WikiProperty.INSTANCE_OF_P2.get(wikiMap)));
        this.subclassOfP3 = stringify(WikiProperty.SUBCLASS_OF_P3.get(wikiMap));
        this.statusP6 = WikiDataItem.fromValue(stringify(WikiProperty.STATUS_P6.get(wikiMap)));
        this.wikidataEquivalentP7 = stringify(WikiProperty.WIKIDATA_EQUIVALENT_P7.get(wikiMap));
        this.formatterUrlP8 = stringToCollection(
                stringify(WikiProperty.FORMATTER_URL_P8.get(wikiMap)));
        this.keyTypeP9 = WikiDataItem.fromValue(stringify(WikiProperty.KEY_TYPE_P9.get(wikiMap)));
        this.keyForThisTagP10 = stringify(WikiProperty.KEY_FOR_THIS_TAG_P10.get(wikiMap));
        this.proposalDiscussionP11 = stringify(WikiProperty.PROPOSAL_DISCUSSION_P11.get(wikiMap));
        this.wikiDataConceptP12 = stringify(WikiProperty.WIKIDATA_CONCEPT_P12.get(wikiMap));
        if (WikiProperty.VALUE_VALIDATION_REGEX_P13.get(wikiMap) != null)
        {
            this.valueValidationRegexP13 = Pattern.compile(
                    "^(" + stringify(WikiProperty.VALUE_VALIDATION_REGEX_P13.get(wikiMap)) + ")$");
        }
        else
        {
            this.valueValidationRegexP13 = null;
        }
        this.permanentKeyIdP16 = stringify(WikiProperty.PERMANENT_KEY_ID_P16.get(wikiMap));
        this.redirectToP17 = stringToCollection(
                stringify(WikiProperty.REDIRECT_TO_P17.get(wikiMap)));
        this.differentFromP18 = stringToCollection(
                stringify(WikiProperty.DIFFERENT_FROM_P18.get(wikiMap)));
        this.permanentTagIdP19 = stringify(WikiProperty.PERMANENT_TAG_ID_P19.get(wikiMap));
        this.propertyDifferentFromP20 = stringify(
                WikiProperty.PROPERTY_DIFFERENT_FROM_P20.get(wikiMap));
        this.relationRoleIdP21 = stringify(WikiProperty.RELATION_ROLE_ID_P21.get(wikiMap));
        this.requiredKeyOrTagP22 = stringToCollection(
                stringify(WikiProperty.REQUIRED_KEY_OR_TAG_P22.get(wikiMap)));
        this.groupP25 = stringify(WikiProperty.GROUP_P25.get(wikiMap));
        this.limitedToLanguageP26 = stringToCollection(
                stringify(WikiProperty.LIMITED_TO_LANGUAGE_P26.get(wikiMap)));
        this.imageP28 = stringify(WikiProperty.IMAGE_P28.get(wikiMap));
        this.mustOnlyBeUsedInRegionP29 = stringToCollection(
                stringify(WikiProperty.MUST_ONLY_BE_USED_IN_REGION_P29.get(wikiMap)));
        this.notToBeUsedInRegionP30 = stringToCollection(
                stringify(WikiProperty.NOT_TO_BE_USED_IN_REGION_P30.get(wikiMap)));
        this.documentationWikiPagesP31 = stringToCollection(
                stringify(WikiProperty.DOCUMENTATION_WIKI_PAGES_P31.get(wikiMap)));
        this.languageCodeP32 = stringify(WikiProperty.LANGUAGE_CODE_P32.get(wikiMap));
        // Use !WikiDataItem.IS_PROHIBITED_Q8001 so that the default is "true"
        this.useOnNodesP33 = !WikiDataItem.IS_PROHIBITED_Q8001
                .matches(stringify(WikiProperty.USE_ON_NODES_P33.get(wikiMap)));
        this.useOnWaysP34 = !WikiDataItem.IS_PROHIBITED_Q8001
                .matches(stringify(WikiProperty.USE_ON_WAYS_P34.get(wikiMap)));
        this.useOnAreasP35 = !WikiDataItem.IS_PROHIBITED_Q8001
                .matches(stringify(WikiProperty.USE_ON_AREAS_P35.get(wikiMap)));
        this.useOnRelationsP36 = !WikiDataItem.IS_PROHIBITED_Q8001
                .matches(stringify(WikiProperty.USE_ON_RELATIONS_P36.get(wikiMap)));
        this.useOnChangesetsP37 = !WikiDataItem.IS_PROHIBITED_Q8001
                .matches(stringify(WikiProperty.USE_ON_CHANGESETS_P37.get(wikiMap)));
        this.oSMCartoImageP39 = stringify(WikiProperty.OSM_CARTO_IMAGE_P39.get(wikiMap));
        this.tagForThisRelationTypeP40 = stringify(
                WikiProperty.TAG_FOR_THIS_RELATION_TYPE_P40.get(wikiMap));
        this.permanentRelationTypeIdP41 = stringify(
                WikiProperty.PERMANENT_RELATION_TYPE_ID_P41.get(wikiMap));
        this.belongsToRelationTypeP43 = stringToCollection(
                stringify(WikiProperty.BELONGS_TO_RELATION_TYPE_P43.get(wikiMap)));
        this.incompatibleWithP44 = stringToCollection(
                stringify(WikiProperty.INCOMPATIBLE_WITH_P44.get(wikiMap)));
        this.impliesP45 = stringToCollection(stringify(WikiProperty.IMPLIES_P45.get(wikiMap)));
        this.combinationP46 = stringToCollection(
                stringify(WikiProperty.COMBINATION_P46.get(wikiMap)));
        this.imageCaptionP47 = stringify(WikiProperty.IMAGE_CAPTION_P47.get(wikiMap));
        this.limitedToRegionP48 = stringify(WikiProperty.LIMITED_TO_REGION_P48.get(wikiMap));
        this.geographicCodeP49 = stringToCollection(
                stringify(WikiProperty.GEOGRAPHIC_CODE_P49.get(wikiMap)));
        this.redirectsToWikiPageP50 = stringify(
                WikiProperty.REDIRECTS_TO_WIKI_PAGE_P50.get(wikiMap));
        this.identicalToP51 = stringToCollection(
                stringify(WikiProperty.IDENTICAL_TO_P51.get(wikiMap)));
    }

    /**
     * If this WikiData item is a relation role, this links to the corresponding relation type (for
     * example, "boundary=inner" -&gt; "multipolygon"). For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P43">Property:P43</a>.
     *
     * @return A collection of Q id relations
     */
    @Nonnull
    public Collection<String> getBelongsToRelationTypeP43()
    {
        return this.belongsToRelationTypeP43;
    }

    /**
     * This key/tag works well with the returned Q id key/tags. This can be used for suggestions.
     * For more information, see <a href="https://wiki.osm.org/wiki/Property:P46">Property:P46</a>.
     *
     * @return A list of Q id key/tags that this key/tag works well with
     */
    @Nonnull
    public Collection<String> getCombinationP46()
    {
        return this.combinationP46;
    }

    /**
     * Item that is different from another item, but they are often confused. For more information,
     * see <a href="https://wiki.osm.org/wiki/Property:P18">Property:P18</a>.
     *
     * @return A collection of Q ids that can be confused with this Q id
     */
    @Nonnull
    public Collection<String> getDifferentFromP18()
    {
        return this.differentFromP18;
    }

    /**
     * Wiki pages for this item in different languages. There should be no more than one value per
     * language. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P31">Property:P31</a>.
     *
     * @return Documentation wiki pages.
     */
    @Unstable
    @Nonnull
    public Collection<String> getDocumentationWikiPagesP31()
    {
        return this.documentationWikiPagesP31.stream()
                .map(key -> "https://wiki.osm.org/wiki/" + key).collect(Collectors.toList());
    }

    /**
     * URI template from which "$1" can be automatically replaced with the effective property value
     * on items. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P8">Property:P8</a>.
     *
     * @return A collection of formatting urls, where "$1" should be replaced (e.g.,
     *         {@code String.replace("\\$1", value)})
     */
    @Nonnull
    public Collection<String> getFormatterUrlP8()
    {
        return this.formatterUrlP8;
    }

    /**
     * A code that represents either a country or a country's subdivision code. For more
     * information, see <a href="https://wiki.osm.org/wiki/Property:P49">Property:P49</a>.
     *
     * @return ISO 3166-1 alpha-2 or ISO 3166-2 codes
     */
    @Nonnull
    public Collection<String> getGeographicCodeP49()
    {
        return this.geographicCodeP49;
    }

    /**
     * Indicates which group the given tag or a key belongs to. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P25">Property:P25</a>.
     *
     * @return The group for the wiki item
     */
    @Unstable
    @Nonnull
    public String getGroupP25()
    {
        return this.groupP25;
    }

    /**
     * Get the Q id for this item
     *
     * @return The Q id (Wiki Data ID)
     */
    @Nonnull
    public String getId()
    {
        return this.identifier;
    }

    /**
     * Current key/tag has identical meaning to the target key/tag, and both are actively used by
     * the community. Both key/tags should appear on the object. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P51">Property:P51</a>.
     *
     * @return A collection of Q ids that this wiki item is considered to be equivalent to
     */
    @Nonnull
    public Collection<String> getIdenticalToP51()
    {
        return this.identicalToP51;
    }

    /**
     * A qualifier to add to an image to specify image caption in a specific language. For more
     * information, see <a href="https://wiki.osm.org/wiki/Property:P47">Property:P47</a>.
     *
     * @return A caption to add to an image
     */
    @Unstable
    @Nonnull
    public String getImageCaptionP47()
    {
        return this.imageCaptionP47;
    }

    /**
     * Image of the subject. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P28">Property:P28</a>.
     *
     * @return An image to show for the subject
     */
    @Unstable
    @Nonnull
    public String getImageP28()
    {
        return this.imageP28;
    }

    /**
     * Any feature with the current key/tag also implies this key/tag, even if they are not
     * explicitly set. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P45">Property:P45</a>.
     *
     * @return A collection of Q ids which are implied by this key/tag
     */
    @Nonnull
    public Collection<String> getImpliesP45()
    {
        return this.impliesP45;
    }

    /**
     * List of keys and tags that are incompatible with the current key or tag. For more
     * information, see <a href="https://wiki.osm.org/wiki/Property:P44">Property:P44</a>.
     *
     * @return A list of Q ids that should not appear with this key/tag
     */
    @Nonnull
    public Collection<String> getIncompatibleWithP44()
    {
        return this.incompatibleWithP44;
    }

    /**
     * That class of which this subject is a particular example and member. For more information,
     * see <a href="https://wiki.osm.org/wiki/Property:P2">Property:P2</a>.
     *
     * @return Typically "key" or "tag", but may have multiple entries
     */
    @Nonnull
    public Collection<String> getInstanceOfP2()
    {
        return this.instanceOfP2;
    }

    /**
     * For a given tag item, links to the corresponding key item. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P10">Property:P10</a>.
     *
     * @return The Q id for the key for this tag
     */
    @Nonnull
    public String getKeyForThisTagP10()
    {
        return this.keyForThisTagP10;
    }

    /**
     * Type of the key entity, e.g. enum, external id. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P9">Property:P9</a>.
     *
     * @return Either {@code null} or {@link WikiDataItem#WELL_KNOWN_VALUES_Q8}. If the latter, all
     *         values should be documented.
     */
    @Nullable
    public WikiDataItem getKeyTypeP9()
    {
        return this.keyTypeP9;
    }

    /**
     * A two or three letter ISO-639 language code. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P32">Property:P32</a>.
     *
     * @return The language code for the wiki item
     */
    @Nonnull
    public String getLanguageCodeP32()
    {
        return this.languageCodeP32;
    }

    /**
     * A qualifier property to specify when a statement only applies to a given documentation
     * language, and nowhere else. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P26">Property:P26</a>.
     *
     * @return A collection of languages where the statement is limited to a language(s)
     */
    @Unstable
    @Nonnull
    public Collection<String> getLimitedToLanguageP26()
    {
        return this.limitedToLanguageP26;
    }

    /**
     * Indicate that something is limited to a region. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P48">Property:P48</a>.
     *
     * @return The limited regions
     */
    @Unstable
    @Nonnull
    public String getLimitedToRegionP48()
    {
        return this.limitedToRegionP48;
    }

    /**
     * This key or tag is to be used only in the given region(s). This property cannot be used at
     * the same time as P30. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P29">Property:P29</a>.
     *
     * @return A list of Q ids for the regions which the key/tag can be used in
     */
    @Nonnull
    public Collection<String> getMustOnlyBeUsedInRegionP29()
    {
        return this.mustOnlyBeUsedInRegionP29;
    }

    /**
     * This key or tag must not be used in the given region(s). This property cannot be used at the
     * same time as P29. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P30">Property:P30</a>.
     *
     * @return A list of Q ids for the regions which the key/tag must not be used in
     */
    @Nonnull
    public Collection<String> getNotToBeUsedInRegionP30()
    {
        return this.notToBeUsedInRegionP30;
    }

    /**
     * The OSM Carto rendering of the given feature. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P39">Property:P39</a>.
     *
     * @return The image used for rendering (you should prefix it with
     *         {@code "https://wiki.openstreetmap.org/wiki/File:"}).
     */
    @Unstable
    @Nonnull
    public String getOSMCartoImageP39()
    {
        return this.oSMCartoImageP39;
    }

    /**
     * A string representing the key ID. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P16">Property:P16</a>.
     *
     * @return The key for the wiki item
     */
    @Nonnull
    public String getPermanentKeyIdP16()
    {
        return this.permanentKeyIdP16;
    }

    /**
     * A string representing the role ID. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P41">Property:P41</a>.
     *
     * @return The relation type id (should be present as the value for the "type" key)
     */
    @Nonnull
    public String getPermanentRelationTypeIdP41()
    {
        return this.permanentRelationTypeIdP41;
    }

    /**
     * A string in a "key=value" format as used in OSM. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P19">Property:P19</a>.
     *
     * @return The tag ("key=value")
     */
    @Nonnull
    public String getPermanentTagIdP19()
    {
        return this.permanentTagIdP19;
    }

    /**
     * Property that is different from another property, but they are often confused. For more
     * information, see <a href="https://wiki.osm.org/wiki/Property:P20">Property:P20</a>.
     *
     * @return A property that this property is different from (not used for wiki items)
     */
    @Unstable
    @Nonnull
    public String getPropertyDifferentFromP20()
    {
        return this.propertyDifferentFromP20;
    }

    /**
     * A link to the key or tag proposal page For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P11">Property:P11</a>.
     *
     * @return A full link to the proposal for this key/tag.
     */
    @Nonnull
    public String getProposalDiscussionP11()
    {
        return this.proposalDiscussionP11;
    }

    /**
     * Indicates that the current key, tag or relation type should not be used. Use the target item
     * instead. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P17">Property:P17</a>.
     *
     * @return The replacement key/tags
     */
    @Nonnull
    public Collection<String> getRedirectToP17()
    {
        return this.redirectToP17;
    }

    /**
     * A qualifier for P31 (wiki page) indicating that the page is a redirect to another page. For
     * more information, see <a href="https://wiki.osm.org/wiki/Property:P50">Property:P50</a>.
     *
     * @return The wiki page redirects
     */
    @Unstable
    @Nonnull
    public String getRedirectsToWikiPageP50()
    {
        return this.redirectsToWikiPageP50;
    }

    /**
     * A string in a "relationtype=role" format. Should only be set on relation role items. For more
     * information, see <a href="https://wiki.osm.org/wiki/Property:P21">Property:P21</a>.
     *
     * @return The relation role id ({@code relation_type=role})
     */
    @Nonnull
    public String getRelationRoleIdP21()
    {
        return this.relationRoleIdP21;
    }

    /**
     * One or more tags or keys that are required for this item. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P22">Property:P22</a>.
     *
     * @return A collection of required keys or tags for this key/tag
     */
    @Nonnull
    public Collection<String> getRequiredKeyOrTagP22()
    {
        return this.requiredKeyOrTagP22;
    }

    /**
     * Community acceptance status. Use reference to link to the proposal discussion page (P11). For
     * more information, see <a href="https://wiki.osm.org/wiki/Property:P6">Property:P6</a>.
     *
     * @return The acceptance status
     */
    @Unstable
    @Nullable
    public WikiDataItem getStatusP6()
    {
        return this.statusP6;
    }

    /**
     * For more information, see <a href="https://wiki.osm.org/wiki/Property:P3">Property:P3</a>.
     *
     * @return The parent class of this wiki item
     */
    @Nonnull
    public String getSubclassOfP3()
    {
        return this.subclassOfP3;
    }

    /**
     * For a given relation item, links to the corresponding tag item, e.g. type=multipolygon. For
     * more information, see <a href="https://wiki.osm.org/wiki/Property:P40">Property:P40</a>.
     *
     * @return The relation type that this wiki item is a part of
     */
    @Nonnull
    public String getTagForThisRelationTypeP40()
    {
        return this.tagForThisRelationTypeP40;
    }

    /**
     * Regular expression to test the validity of the tag's value. May also be used for role names.
     * The wrapping ^( and )$ are assumed. Should not be used for well-known keys. For more
     * information, see <a href="https://wiki.osm.org/wiki/Property:P13">Property:P13</a>.
     *
     * @return A precompiled regex pattern with the wrapping ^( and )$ already added, or
     *         {@code null} if there is no regex.
     */
    @Nullable
    public Pattern getValueValidationRegexP13()
    {
        return this.valueValidationRegexP13;
    }

    /**
     * This key or tag represents a concept described by the given Wikidata item .For more
     * information, see <a href="https://wiki.osm.org/wiki/Property:P12">Property:P12</a>.
     *
     * @return A Q id item for an <i>external</i> wiki data item
     */
    @Nonnull
    public String getWikiDataConceptP12()
    {
        return this.wikiDataConceptP12;
    }

    /**
     * This entity has an equivalent Wikidata entity, like "instance of". For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P7">Property:P7</a>.
     *
     * @return The wikidata identifier (<i>external</i>).
     */
    @Nonnull
    public String getWikidataEquivalentP7()
    {
        return this.wikidataEquivalentP7;
    }

    /**
     * Use status to indicate if this key or tag should be used on areas (closed ways). Keep in mind
     * that closed ways can be an area, a way, or both. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P35">Property:P35</a>.
     *
     * @return {@code true} if the key/tag can generally be used on areas (defaults to {@code true})
     */
    @Unstable
    @Nonnull
    public boolean isUseOnAreasP35()
    {
        return this.useOnAreasP35;
    }

    /**
     * Use status to indicate if this key or tag should be used on changesets For more information,
     * see <a href="https://wiki.osm.org/wiki/Property:P37">Property:P37</a>.
     *
     * @return {@code true} if the key/tag can generally be used on changesets (defaults to
     *         {@code true})
     */
    @Unstable
    @Nonnull
    public boolean isUseOnChangesetsP37()
    {
        return this.useOnChangesetsP37;
    }

    /**
     * Use status to indicate if this key or tag should be used on nodes. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P33">Property:P33</a>.
     *
     * @return {@code true} if the key/tag can generally be used on nodes (defaults to {@code true})
     */
    public boolean isUseOnNodesP33()
    {
        return this.useOnNodesP33;
    }

    /**
     * Use status to indicate if this key or tag should be used on relations. For more information,
     * see <a href="https://wiki.osm.org/wiki/Property:P36">Property:P36</a>.
     *
     * @return {@code true} if the key/tag can generall be used on nodes (defaults to {@code true})
     */
    public boolean isUseOnRelationsP36()
    {
        return this.useOnRelationsP36;
    }

    /**
     * Use status to indicate if this key or tag should be used on ways (not areas). Keep in mind
     * that a closed way can be a way, an area, or both. For more information, see
     * <a href="https://wiki.osm.org/wiki/Property:P34">Property:P34</a>.
     *
     * @return {@code true} if the key/tag can generally be used on ways (defaults to {@code true})
     */
    public boolean isUseOnWaysP34()
    {
        return this.useOnWaysP34;
    }
}
