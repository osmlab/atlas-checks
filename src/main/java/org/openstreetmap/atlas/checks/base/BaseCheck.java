package org.openstreetmap.atlas.checks.base;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.ChallengeDifficulty;
import org.openstreetmap.atlas.checks.maproulette.serializer.ChallengeDeserializer;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.ManMadeTag;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.OptionalIterable;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.filters.AtlasEntityPolygonsFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Abstract BaseCheck for an Integrity Check
 *
 * @author matthieun
 * @author cuthbertm
 * @author mgostintsev
 * @param <T>
 *            the type for the flagged identifiers
 */
public abstract class BaseCheck<T> implements Check, Serializable
{
    public static final String PARAMETER_ACCEPT_PIERS = "accept.piers";
    public static final String PARAMETER_DENYLIST_COUNTRIES = "countries.denylist";
    public static final String PARAMETER_CHALLENGE = "challenge";
    public static final String PARAMETER_FLAG = "flags";
    public static final String PARAMETER_PERMITLIST_COUNTRIES = "countries.permitlist";
    public static final String PARAMETER_PERMITLIST_TAGS = "tags.filter";
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final String PARAMETER_LOCALE_KEY = "locale";
    private static final Logger logger = LoggerFactory.getLogger(BaseCheck.class);
    private static final long serialVersionUID = 4427673331949586822L;
    private final boolean acceptPiers;
    private final List<String> denylistCountries;
    private final Challenge challenge;
    private final List<String> countries;
    private final Map<String, List<String>> flagLanguageMap;
    // OSM Identifiers are used to keep track of flagged features
    private transient Set<T> flaggedIdentifiers = null;
    private final Locale locale;
    private final String name = this.getClass().getSimpleName();
    // geo filter specific to this check
    private final AtlasEntityPolygonsFilter checkPolygonFilter;
    // geo filter for all checks
    private final AtlasEntityPolygonsFilter globalPolygonFilter;
    private TaggableFilter tagFilter = null;

    /**
     * Default constructor
     *
     * @param configuration
     *            {@link Configuration} required to construct any Check
     */
    public BaseCheck(final Configuration configuration)
    {
        this.acceptPiers = configurationValue(configuration, PARAMETER_ACCEPT_PIERS, false);
        this.countries = Collections.unmodifiableList(configurationValue(configuration,
                PARAMETER_PERMITLIST_COUNTRIES, Collections.emptyList()));
        this.denylistCountries = Collections.unmodifiableList(configurationValue(configuration,
                PARAMETER_DENYLIST_COUNTRIES, Collections.emptyList()));
        this.tagFilter = TaggableFilter
                .forDefinition(configurationValue(configuration, PARAMETER_PERMITLIST_TAGS, ""));
        final Map<String, String> challengeMap = configurationValue(configuration,
                PARAMETER_CHALLENGE, Collections.emptyMap());
        this.flagLanguageMap = configurationValue(configuration, PARAMETER_FLAG,
                Collections.emptyMap());
        this.locale = configurationValue(configuration, PARAMETER_LOCALE_KEY,
                DEFAULT_LOCALE.getLanguage(), Locale::new);
        if (challengeMap.isEmpty())
        {
            this.challenge = new Challenge(this.getClass().getSimpleName(), "", "", "",
                    ChallengeDifficulty.EASY, "");
        }
        else
        {
            final Gson gson = new GsonBuilder().disableHtmlEscaping()
                    .registerTypeAdapter(Challenge.class, new ChallengeDeserializer()).create();
            this.challenge = gson.fromJson(gson.toJson(challengeMap), Challenge.class);
        }
        this.globalPolygonFilter = AtlasEntityPolygonsFilter.forConfiguration(configuration);
        this.checkPolygonFilter = AtlasEntityPolygonsFilter.forConfigurationValues(
                configurationValue(configuration, AtlasEntityPolygonsFilter.INCLUDED_POLYGONS_KEY,
                        Collections.emptyMap()),
                configurationValue(configuration,
                        AtlasEntityPolygonsFilter.INCLUDED_MULTIPOLYGONS_KEY,
                        Collections.emptyMap()),
                configurationValue(configuration, AtlasEntityPolygonsFilter.EXCLUDED_POLYGONS_KEY,
                        Collections.emptyMap()),
                configurationValue(configuration,
                        AtlasEntityPolygonsFilter.EXCLUDED_MULTIPOLYGONS_KEY,
                        Collections.emptyMap()));
    }

    @Override
    public Optional<CheckFlag> check(final AtlasObject object)
    {
        try
        {
            if (this.checkObjectFilter().test(object))
            {
                return this.flag(object);
            }
        }
        catch (final Exception oops)
        {
            logger.error(String.format("%s failed on feature %s (%s).", this.getCheckName(),
                    object.getIdentifier(), object.getOsmIdentifier()), oops);
        }

        return Optional.empty();
    }

    public final Predicate<AtlasObject> checkObjectFilter()
    {
        return object -> this.validCheckForObject(object) && this.tagFilter.test(object)
                && (!(object instanceof AtlasEntity)
                        || this.checkPolygonFilter.test((AtlasEntity) object)
                                && this.globalPolygonFilter.test((AtlasEntity) object))
                && (this.acceptPier() || !ManMadeTag.isPier(object));
    }

    /**
     * Clears all flagged feature identifiers for this check. Primary purpose is to reset flagged
     * features during testing. Be careful in using this for actual production checks.
     */
    @Override
    public void clear()
    {
        clearFlaggedIdentifiers();
    }

    @Override
    public Iterable<CheckFlag> flags(final Atlas atlas)
    {
        return new OptionalIterable<>(Iterables
                .translate(new MultiIterable<>(atlas.items(), atlas.relations()), this::check));
    }

    @Override
    public Challenge getChallenge()
    {
        return this.challenge;
    }

    @Override
    public final String getCheckName()
    {
        return this.name;
    }

    public AtlasEntityPolygonsFilter getCheckPolygonFilter()
    {
        return this.checkPolygonFilter;
    }

    /**
     * Gets the permitlisted countries for this check. If an empty list is returned it safe to
     * assume this check applies to all countries.
     *
     * @return a list of country ISO3 codes
     */
    public List<String> getCountries()
    {
        return this.countries;
    }

    public List<String> getDenylistCountries()
    {
        return this.denylistCountries;
    }

    public AtlasEntityPolygonsFilter getGlobalPolygonFilter()
    {
        return this.globalPolygonFilter;
    }

    public Locale getLocale()
    {
        return this.locale;
    }

    /**
     * Uses the default of configured locale to grab the localized instruction format from the
     * configuration. If the localized version does not contain the given index instruction then it
     * will be grabbed from the FallBack Instructions. A {@link IndexOutOfBoundsException} will be
     * thrown if the instruction index is out of the Fallback Instructions. The instruction format
     * will then be formatted using the provided objects and returned.
     *
     * @param index
     *            The index of the desired instruction format.
     * @param objects
     *            The objects to be used in constructing the instruction
     * @return String of the localized instruction, or the {@link BaseCheck#DEFAULT_LOCALE} language
     *         instruction, or the fallback instruction. In that order if the previous isn't present
     *         in the configuration.
     */
    public final String getLocalizedInstruction(final int index, final Object... objects)
    {
        String instructionFormat;
        try
        {
            if (this.flagLanguageMap.containsKey(this.getLocale().getLanguage()))
            {
                instructionFormat = this.flagLanguageMap.get(this.getLocale().getLanguage())
                        .get(index);
            }
            else
            {
                instructionFormat = this.flagLanguageMap.containsKey(DEFAULT_LOCALE.getLanguage())
                        ? this.flagLanguageMap.get(DEFAULT_LOCALE.getLanguage()).get(index)
                        : this.getFallbackInstructions().get(index);
            }

        }
        catch (final IndexOutOfBoundsException exception)
        {
            instructionFormat = this.getFallbackInstructions().get(index);
        }
        return this.formatInstruction(instructionFormat, objects);
    }

    @Override
    public void logStatus()
    {
    }

    /**
     * The country check will first check the country permitlist and if the country is contained in
     * the permitlist it is allowed, after that the country is checked it against denylist and if
     * contained in the denylist will not be allowed.
     *
     * @param country
     *            country ISO3 code to check
     * @return {@code true} if valid check for country, otherwise {@code false}
     */
    @Override
    public boolean validCheckForCountry(final String country)
    {
        // permitlist is valid if country list is empty or country within the country list. It
        // always
        // takes precedence over denylist and denylist is essentially ignored if permitlist is non
        // empty
        if (!this.getCountries().isEmpty())
        {
            return this.getCountries().contains(country);
        }
        else
        {
            return this.getDenylistCountries().isEmpty()
                    || !this.getDenylistCountries().contains(country);
        }
    }

    /**
     * If the check wants to accept piers as part of it's evaluation then it will need to override
     * this function and return {@code true}. By default piers are skipped.
     *
     * @return {@code true} if we want to check pier edges, {@code false} if we want to skip them
     */
    protected boolean acceptPier()
    {
        return this.acceptPiers;
    }

    protected void clearFlaggedIdentifiers()
    {
        this.getFlaggedIdentifiers().clear();
    }

    protected final String configurationKey(final Class<?> type, final String key)
    {
        return formatKey(type.getSimpleName(), key);
    }

    /**
     * Configuration Keys in the Integrity Framework are based on the check simple classname.
     *
     * @param key
     *            key part for a specific configuration item defined for this class
     * @return complete key for lookup
     */
    protected final String configurationKey(final String key)
    {
        return formatKey(getCheckName(), key);
    }

    protected <U> U configurationValue(final Configuration configuration, final String key,
            final U defaultValue)
    {
        return configuration.get(configurationKey(key), defaultValue).value();
    }

    protected <U, V> V configurationValue(final Configuration configuration, final String key,
            final U defaultValue, final Function<U, V> transform)
    {
        return configuration.get(configurationKey(key), defaultValue, transform).value();
    }

    protected CheckFlag createFlag(final AtlasObject object, final String instruction)
    {
        return new CheckFlag(this.getTaskIdentifier(object), Collections.singleton(object),
                Collections.singletonList(instruction));
    }

    protected CheckFlag createFlag(final AtlasObject object, final String instruction,
            final List<Location> points)
    {
        return new CheckFlag(this.getTaskIdentifier(object), Collections.singleton(object),
                Collections.singletonList(instruction), points);
    }

    protected CheckFlag createFlag(final Set<? extends AtlasObject> objects,
            final String instruction)
    {
        return new CheckFlag(this.getTaskIdentifier(objects), objects,
                Collections.singletonList(instruction));
    }

    protected CheckFlag createFlag(final Set<? extends AtlasObject> objects,
            final String instruction, final List<Location> points)
    {
        return new CheckFlag(this.getTaskIdentifier(objects), objects,
                Collections.singletonList(instruction), points);
    }

    protected CheckFlag createFlag(final Set<? extends AtlasObject> objects,
            final List<String> instructions, final List<Location> points)
    {
        return new CheckFlag(this.getTaskIdentifier(objects), objects, instructions, points);
    }

    protected CheckFlag createFlag(final Set<? extends AtlasObject> objects,
            final List<String> instructions)
    {
        return new CheckFlag(this.getTaskIdentifier(objects), objects, instructions);
    }

    protected CheckFlag createFlag(final AtlasObject object, final List<String> instructions)
    {
        return new CheckFlag(this.getTaskIdentifier(object), Collections.singleton(object),
                instructions);
    }

    protected CheckFlag createFlag(final AtlasObject object, final List<String> instructions,
            final List<Location> points)
    {
        return new CheckFlag(this.getTaskIdentifier(object), Collections.singleton(object),
                instructions, points);
    }

    protected abstract Optional<CheckFlag> flag(AtlasObject object);

    /**
     * Method to implement for inheriting checks to return the default set of instruction formats
     * that will be the last resort in {@link BaseCheck#getLocalizedInstruction(int, Object[])}
     *
     * @return The set of instructions to fall back to if configuration results in none.
     */
    protected List<String> getFallbackInstructions()
    {
        return Collections.emptyList();
    }

    protected Set<T> getFlaggedIdentifiers()
    {
        if (this.flaggedIdentifiers == null)
        {
            this.flaggedIdentifiers = ConcurrentHashMap.newKeySet();
        }
        return this.flaggedIdentifiers;
    }

    protected String getTaskIdentifier(final AtlasObject object)
    {
        return new TaskIdentifier(object).toString();
    }

    /**
     * The task identifier is ordered to maintain identifier uniqueness.
     *
     * @param objects
     *            set of {@link AtlasObject}s comprising this task
     * @return a unique string identifier for this task, made from the sorted object identifiers
     */
    protected String getTaskIdentifier(final Set<? extends AtlasObject> objects)
    {
        return new TaskIdentifier(objects).toString();
    }

    /**
     * Generates a unique identifier given an {@link AtlasObject}. OSM/Atlas objects with different
     * types can share the identifier (way 12345 - node 12345). This method makes sure we generate a
     * truly unique identifier, based on the OSM identifier, among different types for an
     * {@link AtlasObject}. If the AtlasObject is an instanceof AtlasEntity then it will simply use
     * the type for the first part of the identifier, otherwise it will use the simple class name.
     *
     * @param object
     *            {@link AtlasObject} to generate unique identifier for
     * @return unique object identifier among different types
     */
    protected String getUniqueOSMIdentifier(final AtlasObject object)
    {
        if (object instanceof AtlasEntity)
        {
            return String.format("%s%s", ((AtlasEntity) object).getType().toShortString(),
                    object.getOsmIdentifier());
        }
        else
        {
            return String.format("%s%s", object.getClass().getSimpleName(), object.getIdentifier());
        }
    }

    /**
     * Similar to {@link BaseCheck#getUniqueOSMIdentifier(AtlasObject)} except instead of using the
     * OSM identifier we use the Atlas identifier
     *
     * @param object
     *            {@link AtlasObject} to generate unique identifier for
     * @return unique object identifier among different types
     */
    protected String getUniqueObjectIdentifier(final AtlasObject object)
    {
        if (object instanceof AtlasEntity)
        {
            return String.format("%s%s", ((AtlasEntity) object).getType().toShortString(),
                    object.getIdentifier());
        }
        else
        {
            return String.format("%s%s", object.getClass().getSimpleName(), object.getIdentifier());
        }
    }

    protected final boolean isFlagged(final T identifier)
    {
        return this.getFlaggedIdentifiers().contains(identifier);
    }

    protected final void markAsFlagged(final T identifier)
    {
        this.getFlaggedIdentifiers().add(identifier);
    }

    /**
     * Utility method to concisely construct a instruction from a {@link MessageFormat} style string
     * and a varying number of objects.
     *
     * @param format
     *            A string embedded with {@link MessageFormat} styled formats.
     * @param objects
     *            The objects to be printed used the embedded formats.
     * @return The fully formatted Instruction.
     */
    private String formatInstruction(final String format, final Object... objects)
    {
        if (objects == null || objects.length == 0)
        {
            return format;
        }
        return new MessageFormat(format).format(objects);
    }

    private String formatKey(final String name, final String key)
    {
        return String.format("%s.%s", name, key);
    }
}
