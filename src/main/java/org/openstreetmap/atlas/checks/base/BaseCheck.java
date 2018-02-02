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
    public static final String PARAMETER_BLACKLIST_COUNTRIES = "countries.blacklist";
    public static final String PARAMETER_CHALLENGE = "challenge";
    public static final String PARAMETER_FLAG = "flags";
    public static final String PARAMETER_WHITELIST_COUNTRIES = "countries.whitelist";
    public static final String PARAMETER_WHITELIST_TAGS = "tags.filter";
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    private static final String PARAMETER_LOCALE_KEY = "locale";
    private static final Logger logger = LoggerFactory.getLogger(BaseCheck.class);
    private static final long serialVersionUID = 4427673331949586822L;
    private final boolean acceptPiers;
    private final List<String> blacklistCountries;
    private final Challenge challenge;
    private final List<String> countries;
    private final Map<String, List<String>> flagLanguageMap;
    // OSM Identifiers are used to keep track of flagged features
    private final Set<T> flaggedIdentifiers = ConcurrentHashMap.newKeySet();
    private final Locale locale;

    private final String name = this.getClass().getSimpleName();
    private TaggableFilter tagFilter = null;

    /**
     * Default constructor
     *
     * @param configuration
     *            {@link Configuration} required to construct any Check
     */
    @SuppressWarnings("unchecked")
    public BaseCheck(final Configuration configuration)
    {
        this.acceptPiers = configurationValue(configuration, PARAMETER_ACCEPT_PIERS, false);
        this.countries = Collections.unmodifiableList(configurationValue(configuration,
                PARAMETER_WHITELIST_COUNTRIES, Collections.EMPTY_LIST));
        this.blacklistCountries = Collections.unmodifiableList(configurationValue(configuration,
                PARAMETER_BLACKLIST_COUNTRIES, Collections.EMPTY_LIST));
        this.tagFilter = new TaggableFilter(
                configurationValue(configuration, PARAMETER_WHITELIST_TAGS, ""));
        final Map<String, String> challengeMap = configurationValue(configuration,
                PARAMETER_CHALLENGE, Collections.EMPTY_MAP);
        this.flagLanguageMap = configurationValue(configuration, PARAMETER_FLAG,
                Collections.EMPTY_MAP);
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
    }

    @Override
    public Optional<CheckFlag> check(final AtlasObject object)
    {
        try
        {
            if (this.validCheckForObject(object) && this.tagFilter.test(object)
                    && (this.acceptPier() || !ManMadeTag.isPier(object)))
            {
                return this.flag(object);
            }
        }
        catch (final Exception oops)
        {
            logger.error(String.format("%s failed on feature %s.", this.getCheckName(),
                    object.getIdentifier()), oops);
        }

        return Optional.empty();
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

    public List<String> getBlacklistCountries()
    {
        return this.blacklistCountries;
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

    /**
     * Gets the whitelisted countries for this check. If an empty list is returned it safe to assume
     * this check applies to all countries.
     *
     * @return a list of country ISO3 codes
     */
    public List<String> getCountries()
    {
        return this.countries;
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
            instructionFormat = this.flagLanguageMap.containsKey(this.getLocale().getLanguage())
                    ? this.flagLanguageMap.get(this.getLocale().getLanguage()).get(index)
                    : this.flagLanguageMap.containsKey(DEFAULT_LOCALE.getLanguage())
                            ? this.flagLanguageMap.get(DEFAULT_LOCALE.getLanguage()).get(index)
                            : this.getFallbackInstructions().get(index);

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
     * The country check will first check the country whitelist and if the country is contained in
     * the whitelist it is allowed, after that the country is checked it against blacklist and if
     * contained in the blacklist will not be allowed.
     *
     * @param country
     *            country ISO3 code to check
     * @return {@code true} if valid check for country, otherwise {@code false}
     */
    @Override
    public boolean validCheckForCountry(final String country)
    {
        // whitelist is valid if country list is empty or country within the country list. It always
        // takes precedence over blacklist and blacklist is essentially ignored if whitelist is non
        // empty
        if (!this.getCountries().isEmpty())
        {
            return this.getCountries().contains(country);
        }
        else
        {
            return this.getBlacklistCountries().isEmpty()
                    || !this.getBlacklistCountries().contains(country);
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
        this.flaggedIdentifiers.clear();
    }

    protected final String configurationKey(final Class type, final String key)
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

    protected CheckFlag createFlag(final Set<AtlasObject> objects, final String instruction)
    {
        return new CheckFlag(this.getTaskIdentifier(objects), objects,
                Collections.singletonList(instruction));
    }

    protected CheckFlag createFlag(final Set<AtlasObject> objects, final String instruction,
            final List<Location> points)
    {
        return new CheckFlag(this.getTaskIdentifier(objects), objects,
                Collections.singletonList(instruction), points);
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
    protected String getTaskIdentifier(final Set<AtlasObject> objects)
    {
        return new TaskIdentifier(objects).toString();
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

    protected final boolean isFlagged(final T identifier)
    {
        return this.flaggedIdentifiers.contains(identifier);
    }

    protected final void markAsFlagged(final T identifier)
    {
        this.flaggedIdentifiers.add(identifier);
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
