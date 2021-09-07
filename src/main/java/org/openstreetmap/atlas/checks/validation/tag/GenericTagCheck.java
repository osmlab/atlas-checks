package org.openstreetmap.atlas.checks.validation.tag;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.base.ExternalDataFetcher;
import org.openstreetmap.atlas.checks.database.taginfo.TagInfoKeyTagCommon;
import org.openstreetmap.atlas.checks.database.taginfo.TagInfoKeys;
import org.openstreetmap.atlas.checks.database.taginfo.TagInfoTags;
import org.openstreetmap.atlas.checks.database.wikidata.WikiData;
import org.openstreetmap.atlas.checks.database.wikidata.WikiDataItem;
import org.openstreetmap.atlas.checks.database.wikidata.WikiProperty;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.AtlasToOsmType;
import org.openstreetmap.atlas.checks.utility.KeyFullyCheckedUtils;
import org.openstreetmap.atlas.checks.utility.SQLiteUtils;
import org.openstreetmap.atlas.checks.utility.feature_change.IFeatureChange;
import org.openstreetmap.atlas.checks.utility.feature_change.RemoveTagFeatureChange;
import org.openstreetmap.atlas.checks.utility.feature_change.ReplaceTagFeatureChange;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic tag check using OSM Wiki Data and Tag Info databases.
 *
 * @author Taylor Smock
 */
public class GenericTagCheck extends BaseCheck<String>
{
    private static final long serialVersionUID = 5150282147895785829L;
    private static final String DEFAULT_NR_TAGS_INSTRUCTION = "OSM feature {0,number,#} has invalid tags";
    private static final String DEFAULT_INSTRUCTION = "Check the following tags for missing, conflicting, or incorrect values: {0}";
    private static final String INSTRUCTION_CONFLICTING_TAGS = "Check the following tags for missing, conflicting, or incorrect values: {0}";
    private static final String INSTRUCTION_INVALID_TAGS = "OSM feature {0,number,#} has invalid tags";
    private static final String INSTRUCTION_MISMATCHED_REGEX = "{0} does not match the regex {1} for the key {2} (see https://wiki.osm.org/Item:{3})";
    private static final String INSTRUCTION_PROHIBITED_USAGE = "{0}={1} is prohibited on {2}. To determine whether to remove or change this tag please see this tag's Wiki Data page, https://wiki.osm.org/Item:{3} or the associated documentation Wiki Page on the Wiki Data page).";
    private static final String INSTRUCTION_REPLACE = "{0} should probably be replaced with {1} (see https://wiki.osm.org/Item:{2})";
    private static final String INSTRUCTION_TAGINFO_POPULAR = "{0} is probably an undocumented {1} ({2} instances). This should be documented on the wiki at https://wiki.osm.org/{3} and in the OpenStreetMap Wiki Data ( https://wiki.osm.org/Data_items ).";
    private static final String INSTRUCTION_TAGINFO_UNPOPULAR = "{0} is an unpopular key ({1} instances)";
    private static final String INSTRUCTION_TAG_SHOULD_NOT_BE_USED_IN_REGION = "{0}={1} should not be used in {2} (see https://wiki.osm.org/Item:{3})";
    private static final String INSTRUCTION_UNDOCUMENTED_POPULAR_TAG = "{0}={1} is not currently documented. Its global popularity ({2} uses) may merit adding wiki documentation for this value. Please consider adding a new Wiki Data page for {0}={1}.";
    private static final String INSTRUCTION_UNKNOWN_RELATION_ROLE = "{0}={1} is an unknown relation role for {0} ({2} {3,number,#} on relation {4,number,#}, see https://wiki.osm.org/Item:{5})";
    private static final String INSTRUCTION_UNKNOWN_RELATION_TYPE = "type={0} is an unknown relation type (relation {1,number,#}, see https://wiki.osm.org/Item:{2})";
    private static final String INSTRUCTION_WELL_DEFINED_TAG_UNKNOWN_VALUE = "{0} is not a well-known value for {1}";
    private static final String INSTRUCTION_WIKI_DATA_REMOVAL = "{0}={1} should probably be removed (it is {2}, see https://wiki.osm.org/Item:{3})";
    private static final List<String> FALLBACK_INSTRUCTIONS = Stream.of(DEFAULT_NR_TAGS_INSTRUCTION,
            DEFAULT_INSTRUCTION, INSTRUCTION_INVALID_TAGS, INSTRUCTION_CONFLICTING_TAGS,
            INSTRUCTION_MISMATCHED_REGEX, INSTRUCTION_UNDOCUMENTED_POPULAR_TAG,
            INSTRUCTION_WELL_DEFINED_TAG_UNKNOWN_VALUE, INSTRUCTION_WIKI_DATA_REMOVAL,
            INSTRUCTION_PROHIBITED_USAGE, INSTRUCTION_TAGINFO_UNPOPULAR,
            INSTRUCTION_TAGINFO_POPULAR, INSTRUCTION_REPLACE, INSTRUCTION_UNKNOWN_RELATION_ROLE,
            INSTRUCTION_UNKNOWN_RELATION_TYPE, INSTRUCTION_TAG_SHOULD_NOT_BE_USED_IN_REGION)
            .collect(Collectors.toCollection(ArrayList::new));
    private static final long DEFAULT_MIN_TAG_USAGE = 100;

    private static final String DEFAULT_WIKI_TABLE = "wiki_data";
    private static final String DEFAULT_TAGINFO_TAG_TABLE = "tags";
    private static final String DEFAULT_TAGINFO_KEY_TABLE = "keys";
    // The following are to ensure that the databases are present and have expected keys
    // highway=residential should be present all the time
    private static final boolean DEFAULT_ERROR_IF_DATABASE_IS_MISSING = true;

    private static final Logger logger = LoggerFactory.getLogger(GenericTagCheck.class);

    private final Long minTagUsage;
    // Due to serialization, this *cannot* be final
    private transient Collection<Predicate<Taggable>> ignoreTags = KeyFullyCheckedUtils
            .populateIgnoreTags();

    private static final List<String> DEFAULT_TAGS_TO_REMOVE = Arrays.asList("abandoned",
            "discardable", "imported", "obsolete", "rejected");
    private final List<String> tagsToRemove;
    private SQLiteUtils sqliteUtilsTagInfoTagTable;
    private SQLiteUtils sqliteUtilsTagInfoKeyTable;
    private SQLiteUtils sqliteUtilsWikiData;
    private static final String DEFAULT_TAGINFO_DB = "taginfo-db.db";
    private static final String DEFAULT_WIKIDATA_DB = "wikidata.db";
    private static final int DEFAULT_TAG_PERCENTAGE_KEY = 10;
    private final String tagInfoDB;
    private final String wikiDataDB;
    private final String wikiTable;
    private final String tagInfoTagTable;
    private final String tagInfoKeyTable;
    private final int popularTagPercentageKey;

    /**
     * Check if an object is an area (i.e., an actual Area object OR a multipolygon)
     *
     * @param object
     *            The object to check
     * @return {@code true} if the object is an area
     */
    private static boolean isArea(final Taggable object)
    {
        return object instanceof Area
                || (object instanceof Relation && ((Relation) object).isMultiPolygon());
    }

    /**
     * Check if an object may be an area
     *
     * @param object
     *            The object to check
     * @return {@code true} if the object <i>may</i> be an area.
     */
    private static boolean isClosedLine(final Taggable object)
    {
        return object instanceof LineItem && ((LineItem) object).isClosed();
    }

    /**
     * Convert a tag entry into a string
     *
     * @param tag
     *            The tag to convert
     * @return A &ltkey&gt=&ltvalue&gt string
     */
    private static String tagToString(final Map.Entry<String, String> tag)
    {
        return Stream.of(tag.getKey(), tag.getValue()).filter(Objects::nonNull)
                .filter(string -> !string.isBlank()).collect(Collectors.joining("="));
    }

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     * @param fileFetcher
     *            A fetcher to get external data with
     */
    public GenericTagCheck(final Configuration configuration, final ExternalDataFetcher fileFetcher)
    {
        super(configuration);
        this.minTagUsage = this.configurationValue(configuration, "tag.usage.min",
                DEFAULT_MIN_TAG_USAGE);

        this.tagsToRemove = this.configurationValue(configuration, "wikidata.tag_removal",
                DEFAULT_TAGS_TO_REMOVE);
        this.wikiTable = DEFAULT_WIKI_TABLE;
        this.tagInfoTagTable = DEFAULT_TAGINFO_TAG_TABLE;
        this.tagInfoKeyTable = DEFAULT_TAGINFO_KEY_TABLE;
        this.tagInfoDB = this.configurationValue(configuration, "db.taginfo", DEFAULT_TAGINFO_DB);
        this.wikiDataDB = this.configurationValue(configuration, "db.wikidata",
                DEFAULT_WIKIDATA_DB);

        // At time of implementation, this.configurationValue(..., ..., Integer) returns
        // a Long.
        this.popularTagPercentageKey = this
                .configurationValue(configuration, "tag.percentage_of_key_for_popular",
                        ((Integer) DEFAULT_TAG_PERCENTAGE_KEY).longValue())
                .intValue();

        // No matter what the atlas is, we need the db files. But only if we can get
        // them.
        if (fileFetcher == null)
        {
            return;
        }
        for (final String file : Arrays.asList(this.tagInfoDB, this.wikiDataDB))
        {
            final Optional<Resource> resource = fileFetcher.apply(file);
            if (!resource.isPresent() && logger.isDebugEnabled())
            {
                logger.debug(MessageFormat.format("{0} is not present", file));
            }
        }
        this.fetchWikiData(fileFetcher);
        this.fetchTagInfo(fileFetcher);

        /*
         * Prevent this check from running, unless the user has indicated that not all databases are
         * required Specifically leave these configuration values undocumented -- if people want to
         * use only one or the other, they should understand all the issues involved. Namely, they
         * may have many issues where they are told "key is not documented or popular" even if it is
         * popular (i.e., they ran with wiki data but no tag info). Regardless, wiki data is
         * *always* required.
         */
        final boolean errorIfDatabaseNotFound = this.configurationValue(configuration,
                "db.require_all", DEFAULT_ERROR_IF_DATABASE_IS_MISSING);
        if (errorIfDatabaseNotFound && (this.sqliteUtilsTagInfoKeyTable == null
                || this.sqliteUtilsTagInfoTagTable == null || this.sqliteUtilsWikiData == null))
        {
            throw new CoreException(
                    "GenericTagCheck: All databases are required and must be readable");
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
        return !object.getOsmTags().isEmpty()
                && !this.isFlagged(this.getUniqueOSMIdentifier(object));
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
        final Map<String, Collection<IFeatureChange>> instructions = new TreeMap<>();

        if (this.sqliteUtilsTagInfoTagTable != null && this.sqliteUtilsWikiData != null)
        {
            if (this.ignoreTags == null || this.ignoreTags.isEmpty())
            {
                this.ignoreTags = KeyFullyCheckedUtils.populateIgnoreTags();
            }
            this.flagTags(object, instructions);
        }

        if (!instructions.isEmpty())
        {
            // Mark objects flagged by their class and id to allow for the same id in
            // different object types
            this.markAsFlagged(this.getUniqueOSMIdentifier(object));

            // Create a flag with generic instructions
            final String instruction = this.getLocalizedInstruction(
                    FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_INVALID_TAGS),
                    object.getOsmIdentifier());
            // If the object is an edge add the edges with the same OSM id
            final CheckFlag flag = (object instanceof Edge)
                    ? this.createFlag(new OsmWayWalker((Edge) object).collectEdges(), instruction)
                    : this.createFlag(object, instruction);

            // Add the specific instructions
            instructions.keySet().forEach(flag::addInstruction);
            /*
             * Only add autofixes when instructions are the same size as the changes. This avoids
             * situations where a change is available and is valid, but there is an additional issue
             * that is not able to be automatically fixed. If MapRoulette ever supports a method to
             * indicate that a change partially fixes the flag, this can be changed back to {@code
             * changes.isEmpty()}.
             */
            if (instructions.entrySet().stream().noneMatch(entry -> entry.getValue().isEmpty()))
            {
                flag.addFixSuggestions(Collections.singleton(
                        IFeatureChange.createFeatureChange(FeatureChange::add, (AtlasEntity) object,
                                instructions.values().stream().flatMap(Collection::stream)
                                        .filter(Objects::nonNull).distinct()
                                        .collect(Collectors.toList()))));
            }
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
     * Check for country specific rules
     *
     * @param instructions
     *            The instructions for the object
     * @param object
     *            The taggable object which should have country tags
     * @param tag
     *            The actual tag we are checking
     * @param wikiMap
     *            The map of wiki information
     * @return {@code true} if the check failed (i.e., an instruction has been generated)
     */
    private boolean checkCountrySpecific(final Map<String, Collection<IFeatureChange>> instructions,
            final Taggable object, final Map.Entry<String, String> tag, final WikiData wikiMap)
    {
        // P48 shouldn't be used on actual key/tag items
        if (wikiMap != null && (!wikiMap.getMustOnlyBeUsedInRegionP29().isEmpty()
                || !wikiMap.getNotToBeUsedInRegionP30().isEmpty()))
        {
            /* Create a function that converts country/region wiki data into ISO3 codes */
            final UnaryOperator<Collection<String>> toWikiData = countries -> countries.stream()
                    .map(this::getWikiDataId)
                    .filter(Objects::nonNull/* Ignore values that aren't in the db */)
                    .map(WikiData::getGeographicCodeP49)
                    .filter(Objects::nonNull/* Q21351 has a null value here */)
                    .flatMap(Collection::stream)
                    .map(IsoCountry::iso3ForIso2/* Convert ISO2 to ISO3 codes */)
                    .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
            // Object ISOCountryTags appear to be ISO3 codes.
            final List<String> countries = Iterables.asList(ISOCountryTag.all(object));
            String instruction = null;
            // The region keys are one or the other, but should never be both.
            if (!wikiMap.getMustOnlyBeUsedInRegionP29().isEmpty())
            {
                final Collection<String> onlyUsedInRegion = toWikiData
                        .apply(wikiMap.getMustOnlyBeUsedInRegionP29());
                // Some primitives may cross country lines, so if any country is a valid region,
                // continue on.
                if (countries.stream().noneMatch(onlyUsedInRegion::contains))
                {
                    instruction = INSTRUCTION_TAG_SHOULD_NOT_BE_USED_IN_REGION;
                }
            }
            // check on P30 becomes redundant due to earlier check.
            else
            {
                final Collection<String> notUsedInRegion = toWikiData
                        .apply(wikiMap.getNotToBeUsedInRegionP30());
                // If countries is empty, then don't bother checking. Otherwise, allMatch
                // returns true.
                if (!countries.isEmpty() && countries.stream().allMatch(notUsedInRegion::contains))
                {
                    instruction = INSTRUCTION_TAG_SHOULD_NOT_BE_USED_IN_REGION;
                }
            }
            // If the tag shouldn't be used in the region/area, it should be removed
            if (instruction != null)
            {
                instructions.put(
                        this.getLocalizedInstruction(FALLBACK_INSTRUCTIONS.indexOf(instruction),
                                tag.getKey(), tag.getValue(), String.join(", ", countries),
                                wikiMap.getId()),
                        Collections.singleton(new RemoveTagFeatureChange(tag)));
            }

            return instruction != null;
        }
        return false;
    }

    /**
     * Check for fallback issues (mostly unpopular/popular tags not caught by wiki checks)
     *
     * @param instructions
     *            The instructions to add to
     * @param tag
     *            The tag to check
     * @param keyInfo
     *            The information for the key
     * @param popular
     *            {@code true} if the key is "popular"
     * @param tagOccurrence
     *            The information from TagInfo for the tag
     * @param keyOccurrence
     *            The information from TagInfo for the key
     */
    private void checkFallback(final Map<String, Collection<IFeatureChange>> instructions,
            final Map.Entry<String, String> tag, final WikiData keyInfo, final boolean popular,
            final TagInfoTags tagOccurrence, final TagInfoKeys keyOccurrence)
    {
        if (keyInfo == null)
        {
            final String count;
            final Map.Entry<String, String> messageTag;
            if (tagOccurrence.getCountAll() != null)
            {
                count = tagOccurrence.getCountAll().toString();
                messageTag = tag;
            }
            else if (keyOccurrence.getCountAll() != null)
            {
                count = keyOccurrence.getCountAll().toString();
                messageTag = Map.entry(tag.getKey(), "");
            }
            else
            {
                count = "<" + this.minTagUsage.toString();
                messageTag = tag;
            }
            // Check for unpopular/bad tags not already caught by wiki check
            if (!popular)
            {
                instructions.put(this.getLocalizedInstruction(
                        FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_TAGINFO_UNPOPULAR),
                        tagToString(tag), count), Collections.emptyList());
            }
            else
            {
                final String type = messageTag.getValue().isBlank() ? "Key" : "Tag";
                instructions.put(this.getLocalizedInstruction(
                        FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_TAGINFO_POPULAR),
                        tagToString(messageTag), type.toLowerCase(Locale.ENGLISH), count,
                        new StringBuilder(type).append(':').append(tagToString(messageTag))
                                .toString()),
                        Collections.emptyList());
            }
        }
    }

    /**
     * Check if a wiki data item for a tag redirects to another tag, and if so, create an
     * instruction and change for that.
     *
     * @param instructions
     *            The instructions collection to add to
     * @param tag
     *            The tag to check
     * @param checkInfo
     *            Wiki Data information (more specific)
     * @return {@code true} if the tag fails the regex
     */
    private boolean checkRedirectTo(final Map<String, Collection<IFeatureChange>> instructions,
            final Map.Entry<String, String> tag, final WikiData checkInfo)
    {
        if (checkInfo != null && !checkInfo.getRedirectToP17().isEmpty())
        {
            final String value = checkInfo.getInstanceOfP2().contains("key")
                    ? checkInfo.getPermanentKeyIdP16()
                    : checkInfo.getPermanentTagIdP19();
            final Collection<String> redirects = checkInfo.getRedirectToP17();
            final Map<String, String> replacements = redirects.stream().map(replacementId ->

            WikiData.getWikiData(this.sqliteUtilsWikiData.getRows(Map.of("id", replacementId)))

            ).filter(Objects::nonNull).map(this::parseTags).filter(Objects::nonNull)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            final Collection<IFeatureChange> featureChanges;
            if (!replacements.isEmpty())
            {
                featureChanges = new ArrayList<>();
                featureChanges.add(new RemoveTagFeatureChange(tag));
                for (final Map.Entry<String, String> newTag : replacements.entrySet())
                {
                    featureChanges.add(new ReplaceTagFeatureChange(tag, newTag));
                }
            }
            else
            {
                featureChanges = Collections.emptyList();
            }

            instructions.put(
                    this.getLocalizedInstruction(FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_REPLACE),
                            value,
                            replacements.entrySet().stream().map(GenericTagCheck::tagToString)
                                    .collect(Collectors.joining(", ")),
                            checkInfo.getId()),
                    featureChanges);
            return true;
        }
        return false;
    }

    /**
     * Check regexes for a tag
     *
     * @param instructions
     *            The instruction collection to append instructions to
     * @param tag
     *            The specific tag to check
     * @param checkInfo
     *            Wiki Data information (specific, if available)
     * @return {@code true} if the regex failed
     */
    private boolean checkRegex(final Map<String, Collection<IFeatureChange>> instructions,
            final Map.Entry<String, String> tag, final WikiData checkInfo)
    {
        // "P13" is "value validation regex". We must wrap with "^(" and ")$"
        if (checkInfo != null && checkInfo.getValueValidationRegexP13() != null)
        {
            // Reuse the compiled patterns
            final var pattern = checkInfo.getValueValidationRegexP13();
            if (!pattern.matcher(tag.getValue()).matches())
            {
                instructions.put(this.getLocalizedInstruction(
                        FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_MISMATCHED_REGEX), tag.getValue(),
                        pattern.toString(), tag.getKey(), checkInfo.getId()),
                        Collections.emptyList());
                return true;
            }
        }
        return false;
    }

    /**
     * Do relation specific checks
     *
     * @param instructions
     *            Instructions to add to
     * @param object
     *            The object to check
     * @param checkInfo
     *            The wiki information for the tag to be checked
     * @param tag
     *            The tag to be checked
     * @return {@code true} if the relation had a problem
     */
    private boolean checkRelations(final Map<String, Collection<IFeatureChange>> instructions,
            final Taggable object, final Map.Entry<String, String> tag, final WikiData checkInfo)
    {
        if (checkInfo != null && object instanceof Relation && checkInfo.isUseOnRelationsP36()
                && "type".equals(tag.getKey()))
        {
            final var relation = (Relation) object;
            final var relationInfo = this.getWikiData(
                    Map.of(WikiProperty.PERMANENT_RELATION_TYPE_ID_P41.getId(), tag.getValue()));
            if (relationInfo == null)
            {
                instructions.put(
                        this.getLocalizedInstruction(
                                FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_UNKNOWN_RELATION_TYPE),
                                tag.getValue(), relation.getOsmIdentifier(), checkInfo.getId()),
                        Collections.emptyList());
                return true;
            }
            final Collection<String> additionalInstructions = new TreeSet<>();
            for (final RelationMember relationMember : relation.members())
            {
                // Empty roles should exist in WikiData
                final String role = relationMember.getRole();
                final var roleInfo = this
                        .getWikiData(Map.of(WikiProperty.RELATION_ROLE_ID_P21.getId(),
                                relationInfo.getPermanentRelationTypeIdP41() + "=" + role));
                if (roleInfo == null)
                {
                    final var roleMember = relationMember.getEntity();
                    additionalInstructions.add(this.getLocalizedInstruction(
                            FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_UNKNOWN_RELATION_ROLE),
                            relationInfo.getPermanentRelationTypeIdP41(), role,
                            AtlasToOsmType.convert(roleMember.getType()),
                            roleMember.getOsmIdentifier(), relation.getOsmIdentifier(),
                            checkInfo.getId()));
                }
            }
            additionalInstructions
                    .forEach(instruction -> instructions.put(instruction, Collections.emptyList()));
            return !additionalInstructions.isEmpty();
        }
        return false;
    }

    /**
     * Check the status of a tag
     *
     * @param instructions
     *            The instruction collection to append instructions to
     * @param tag
     *            The specific tag to check
     * @param checkInfo
     *            Wiki Data information (specific, if available)
     * @param wellKnown
     *            If the tag has well-defined values
     * @return {@code true} if the tag should be removed (change already added to {@code changes})
     */
    private boolean checkStatus(final Map<String, Collection<IFeatureChange>> instructions,
            final Map.Entry<String, String> tag, final WikiData checkInfo, final boolean wellKnown)
    {
        if (wellKnown && checkInfo != null && checkInfo.getStatusP6() != null
                && this.tagsToRemove.stream().anyMatch(t -> checkInfo.getStatusP6().matches(t)))
        {
            instructions.put(
                    this.getLocalizedInstruction(
                            FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_WIKI_DATA_REMOVAL),
                            tag.getKey(), tag.getValue(),
                            checkInfo.getStatusP6().getDescriptor().toLowerCase(Locale.ENGLISH),
                            checkInfo.getId()),
                    Collections.singleton(new RemoveTagFeatureChange(tag)));
            return true;
        }
        return false;
    }

    /**
     * Check for instances where a key is well-known but the value is not one of the well-known
     * values.
     *
     * @param instructions
     *            The instructions to add to
     * @param tag
     *            The tag to check
     * @param checkInfo
     *            Wiki Data information (more specific)
     * @param wellKnown
     *            {@code true} if values are well-known
     * @param popular
     *            {@code true} if the tag is popular
     * @param tagOccurrence
     *            The TagInfo information for the tag
     * @return {@code true} if an issue was found
     */
    private boolean checkUndocumentedPopularWellDefined(
            final Map<String, Collection<IFeatureChange>> instructions,
            final Map.Entry<String, String> tag, final WikiData checkInfo, final boolean wellKnown,
            final boolean popular, final TagInfoTags tagOccurrence)
    {
        if (wellKnown && checkInfo == null)
        {
            if (popular)
            {
                instructions.put(
                        this.getLocalizedInstruction(
                                FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_UNDOCUMENTED_POPULAR_TAG),
                                tag.getKey(), tag.getValue(), tagOccurrence.getCountAll()),
                        Collections.emptyList());
            }
            else
            {
                instructions.put(
                        this.getLocalizedInstruction(
                                FALLBACK_INSTRUCTIONS
                                        .indexOf(INSTRUCTION_WELL_DEFINED_TAG_UNKNOWN_VALUE),
                                tag.getValue(), tag.getKey()),
                        Collections.singleton(new RemoveTagFeatureChange(tag)));
            }
            return true;
        }
        return false;
    }

    /**
     * Check the tag against wiki data for cases where it should not appear on the object.
     *
     * @param instructions
     *            The instruction collection to append instructions to
     * @param tag
     *            The specific tag to check
     * @param object
     *            The atlas object (used to check if the tag is expected on the object)
     * @param checkInfo
     *            Wiki Data information (more specific)
     * @return {@code true} if there is an unwanted tag on an object
     */
    private boolean checkUnwantedTagOnObject(final Taggable object,
            final Map<String, Collection<IFeatureChange>> instructions,
            final Map.Entry<String, String> tag, final WikiData checkInfo)
    {
        final List<String> newInstructions = new ArrayList<>();
        if (object instanceof LocationItem && !checkInfo.isUseOnNodesP33())
        {
            newInstructions.add(this.getLocalizedInstruction(
                    FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_PROHIBITED_USAGE), tag.getKey(),
                    tag.getValue(), "node", checkInfo.getId()));
        }

        /*
         * A line may be considered a way or an area. Account for instances where Atlas doesn't know
         * a tag makes something an area. Also account for instances where linear ways have been
         * converted into areas.
         */
        if ((isArea(object) || isClosedLine(object)) && !checkInfo.isUseOnWaysP34()
                && !checkInfo.isUseOnAreasP35())
        {
            newInstructions.add(this.getLocalizedInstruction(
                    FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_PROHIBITED_USAGE), tag.getKey(),
                    tag.getValue(), "area", checkInfo.getId()));
        }
        else if (object instanceof LineItem && !checkInfo.isUseOnWaysP34() && !isClosedLine(object))
        {
            newInstructions.add(this.getLocalizedInstruction(
                    FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_PROHIBITED_USAGE), tag.getKey(),
                    tag.getValue(), "way", checkInfo.getId()));
        }

        if (object instanceof Relation && !checkInfo.isUseOnRelationsP36() && !isArea(object))
        {
            newInstructions.add(this.getLocalizedInstruction(
                    FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_PROHIBITED_USAGE), tag.getKey(),
                    tag.getValue(), "relation", checkInfo.getId()));
        }
        if (!newInstructions.isEmpty())
        {
            final Collection<IFeatureChange> delete = Collections
                    .singleton(new RemoveTagFeatureChange(tag));
            newInstructions.forEach(instruction -> instructions.put(instruction, delete));
            return true;
        }
        return false;
    }

    /**
     * Check the tag to ensure that it exists in Wiki Data Items
     *
     * @param instructions
     *            The instruction collection to append instructions to
     * @param tag
     *            The specific tag to check
     * @param object
     *            The atlas object (used to check if the tag is expected on the object)
     * @param wellKnown
     *            If the tag has well-defined values
     * @param checkInfo
     *            Wiki Data information (specific, if available)
     */
    private void checkWikiData(final Taggable object,
            final Map<String, Collection<IFeatureChange>> instructions,
            final Map.Entry<String, String> tag, final WikiData keyInfo, final WikiData checkInfo,
            final boolean wellKnown)
    {
        final TagInfoTags tagOccurrence = (TagInfoTags) this.getTagInfo(tag.getKey(),
                tag.getValue());
        final TagInfoKeys keyOccurrence = (TagInfoKeys) this.getTagInfo(tag.getKey(), null);
        final boolean popular = this.isPopular(keyOccurrence, tagOccurrence);
        if (this.checkRegex(instructions, tag, checkInfo)
                || this.checkUndocumentedPopularWellDefined(instructions, tag, checkInfo, wellKnown,
                        popular, tagOccurrence)
                || this.checkStatus(instructions, tag, checkInfo, wellKnown)
                || this.checkRedirectTo(instructions, tag, checkInfo)
                || this.checkRelations(instructions, object, tag, checkInfo)
                || this.checkCountrySpecific(instructions, object, tag, checkInfo)
                || (checkInfo != null
                        && this.checkUnwantedTagOnObject(object, instructions, tag, checkInfo)))
        {
            return;
        }

        // Osmose 3040, 3050, and 3150 fallback/extra instructions
        this.checkFallback(instructions, tag, keyInfo, popular, tagOccurrence, keyOccurrence);
    }

    /**
     * Fetch tag info
     *
     * @param fileFetcher
     *            The filefetcher to use
     */
    private void fetchTagInfo(final ExternalDataFetcher fileFetcher)
    {
        this.sqliteUtilsTagInfoKeyTable = new SQLiteUtils(fileFetcher, this.tagInfoDB,
                this.tagInfoKeyTable);
        if (!SQLiteUtils.isValidDatabase(this.sqliteUtilsTagInfoKeyTable.getFile()))
        {
            this.sqliteUtilsTagInfoKeyTable = null;
        }
        this.sqliteUtilsTagInfoTagTable = new SQLiteUtils(fileFetcher, this.tagInfoDB,
                this.tagInfoTagTable);
        if (!SQLiteUtils.isValidDatabase(this.sqliteUtilsTagInfoTagTable.getFile()))
        {
            this.sqliteUtilsTagInfoTagTable = null;
        }
    }

    /**
     * Fetch wiki data
     *
     * @param fileFetcher
     *            The filefetcher to use
     */
    private void fetchWikiData(final ExternalDataFetcher fileFetcher)
    {
        this.sqliteUtilsWikiData = new SQLiteUtils(fileFetcher, this.wikiDataDB, this.wikiTable);
        if (!SQLiteUtils.isValidDatabase(this.sqliteUtilsWikiData.getFile()))
        {
            this.sqliteUtilsWikiData = null;
        }
    }

    /**
     * Flag tags
     *
     * @param object
     *            The object to iterate through
     * @param instructions
     *            The instructions to add to
     */
    private void flagTags(final AtlasObject object,
            final Map<String, Collection<IFeatureChange>> instructions)
    {
        // Check for bad tags (i.e., wrong object type, regex doesn't match, wiki data
        // doesn't exist)
        for (final Map.Entry<String, String> entry : object.getOsmTags().entrySet())
        {
            for (final String value : entry.getValue().split(";", -1))
            {
                final Map.Entry<String, String> tagEntry = Map.entry(entry.getKey(), value);
                // Use TestTaggable to avoid matching on common tags
                if (this.ignoreTags.parallelStream().anyMatch(
                        p -> p.test(new TestTaggable(tagEntry.getKey(), tagEntry.getValue()))))
                {
                    continue;
                }
                // Osmose 3040, 3050, and 3150
                final var keyInfo = this.getWikiData(tagEntry.getKey(), null);
                // "P9" is "key type", "Q8" is "well-known values"
                final boolean wellKnown = keyInfo != null
                        && WikiDataItem.WELL_KNOWN_VALUES_Q8.matches(keyInfo.getKeyTypeP9());

                var checkInfo = this.getWikiData(tagEntry.getKey(), tagEntry.getValue());
                if (checkInfo == null && !wellKnown)
                {
                    checkInfo = keyInfo;
                }

                this.checkWikiData(object, instructions, tagEntry, keyInfo, checkInfo, wellKnown);
            }

        }
    }

    /**
     * Get the TagInfo for a specific key and value
     *
     * @param key
     *            The key to get
     * @param value
     *            The value to get (if {@code null}, information is pulled from the keys table
     *            instead of tags)
     * @return A map entry with a key of the tag (or key, if value is {@code null}) and the number
     *         of worldwide occurrences.
     */
    @Nonnull
    private TagInfoKeyTagCommon getTagInfo(final String key, final String value)
    {
        final Map<String, Object> map;
        if (value != null)
        {
            map = this.sqliteUtilsTagInfoTagTable.getRows(Map.of("key", key, "value", value));
            return new TagInfoTags(map);
        }
        map = this.sqliteUtilsTagInfoKeyTable.getRows(Map.of("key", key));
        return new TagInfoKeys(map);
    }

    /**
     * Get the Wiki Data for a specific key and value
     *
     * @param key
     *            The key to get the wiki data for
     * @param value
     *            The value to get the wiki data for (may be {@code null})
     * @return The map for the specified key/value combination
     */
    @Nullable
    private WikiData getWikiData(final String key, final String value)
    {
        Objects.requireNonNull(key, "key cannot be null");
        if (value == null || value.isBlank())
        {
            // "P16" is the "permanent key id" (e.g. "highway")
            return this.getWikiData(Map.of(WikiProperty.PERMANENT_KEY_ID_P16.getId(), key));
        }
        final var tag = String.join("=", key, value);
        // "P19" is the "permanent tag id" (e.g. "highway=residential")
        return this.getWikiData(Map.of(WikiProperty.PERMANENT_TAG_ID_P19.getId(), tag));
    }

    /**
     * Get arbitrary values from the wiki data
     *
     * @param searchValues
     *            The key-value search map
     * @return A wiki data item, if one exists
     */
    @Nullable
    private WikiData getWikiData(final Map<String, String> searchValues)
    {
        return WikiData.getWikiData(this.sqliteUtilsWikiData.getRows(searchValues));
    }

    /**
     * Get Wiki Data for an id
     *
     * @param identifier
     *            The id to get
     * @return The WikiData item
     */
    @Nullable
    private WikiData getWikiDataId(final String identifier)
    {
        Objects.requireNonNull(identifier, "id cannot be null");
        final var data = WikiData.getWikiData(identifier);
        if (data != null)
        {
            return data;
        }
        // The id column has no ID (P2, P3, etc).
        return this.getWikiData(Map.of(WikiProperty.ID.getDescriptor(), identifier));
    }

    private boolean isPopular(final TagInfoKeys keyOccurrence, final TagInfoTags tagOccurrence)
    {
        if (tagOccurrence != null && tagOccurrence.getCountAll() != null
                && tagOccurrence.getCountAll().longValue() > this.minTagUsage)
        {
            return true;
        }
        return keyOccurrence.getCountAll() != null
                && keyOccurrence.getCountAll().longValue() > this.minTagUsage
                && keyOccurrence.getCountAll().longValue()
                        / keyOccurrence.getValuesAll().longValue() < this.popularTagPercentageKey;
    }

    /**
     * Parse a tag from a map (wiki data)
     *
     * @param map
     *            The map to parse tags from
     * @return An entry for the tag
     */
    @Nullable
    private Map.Entry<String, String> parseTags(@Nonnull final WikiData map)
    {
        final String key = map.getPermanentKeyIdP16();
        if (!key.isBlank())
        {
            return Map.entry(key, "");
        }
        final String tag = map.getPermanentTagIdP19();
        if (!tag.isBlank())
        {
            final String[] tagId = tag.split("=", 2);
            if (tagId.length == 2)
            {
                return Map.entry(tagId[0], tagId[1]);
            }
        }
        return null;
    }
}
