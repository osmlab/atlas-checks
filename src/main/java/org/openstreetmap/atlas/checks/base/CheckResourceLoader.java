package org.openstreetmap.atlas.checks.base;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Streams;
import com.google.common.reflect.ClassPath;

/**
 * Loads Checks found on the classpath. Checks are discovered by scanning a list of configurable
 * classpath URLs for implementations of a specific type, which also configurable. Enabled checks
 * found are instantiated and added to the {@link Set} of returned checks. Configuration:
 *
 * <pre>
 * {
 *   "CheckResourceLoader": {
 *     "scanUrls": [
 *         "org.openstreetmap.atlas.checks"
 *     ],
 *     "type": "org.openstreetmap.atlas.checks.base.Check",
 *     "enabled": {
 *         "key.template": "%s.enabled",
 *         "value.default": false
 *     }
 *   }
 * }
 * </pre>
 *
 * @author brian_l_davis
 * @author jklamer
 * @author nachtm
 */
public class CheckResourceLoader
{
    private static final String DEFAULT_ENABLED_KEY_TEMPLATE = "%s.enabled";
    private static final String DEFAULT_PACKAGE = "org.openstreetmap.atlas.checks";
    private static final String DEFAULT_TYPE = Check.class.getName();
    private static final Logger logger = LoggerFactory.getLogger(CheckResourceLoader.class);
    private static final String COUNTRY_PERMITLIST_TEMPLATE = "%s."
            + BaseCheck.PARAMETER_PERMITLIST_COUNTRIES;
    private static final String COUNTRY_DENYLIST_TEMPLATE = "%s."
            + BaseCheck.PARAMETER_DENYLIST_COUNTRIES;
    private final Class<?> checkType;
    private final Configuration configuration;
    private final MultiMap<String, String> countryGroups = new MultiMap<>();
    private final Boolean enabledByDefault;
    private final String enabledKeyTemplate;
    private final Set<String> packages;
    private final Optional<List<String>> checkPermitList;
    private final Optional<List<String>> checkDenyList;

    /**
     * Default constructor
     *
     * @param configuration
     *            the {@link Configuration} for loaded checks
     */
    @SuppressWarnings("unchecked")
    public CheckResourceLoader(final Configuration configuration)
    {
        this.packages = Collections.unmodifiableSet(Iterables.asSet((Iterable<String>) configuration
                .get("CheckResourceLoader.scanUrls", Collections.singletonList(DEFAULT_PACKAGE))
                .value()));
        final Map<String, List<String>> groups = configuration.get("groups", Collections.emptyMap())
                .value();
        groups.keySet().forEach(group ->
        {
            groups.get(group).forEach(country -> this.countryGroups.add(country, group));
        });

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try
        {
            this.checkType = loader
                    .loadClass(configuration.get("CheckResourceLoader.type", DEFAULT_TYPE).value());
        }
        catch (final ClassNotFoundException classNotFound)
        {
            throw new CoreException("Unable to initialize CheckResourceLoader", classNotFound);
        }
        this.enabledByDefault = configuration
                .get("CheckResourceLoader.enabled.value.default", false).value();
        this.enabledKeyTemplate = configuration
                .get("CheckResourceLoader.enabledKeyTemplate", DEFAULT_ENABLED_KEY_TEMPLATE)
                .value();
        this.configuration = configuration;
        this.checkPermitList = configuration.get("CheckResourceLoader.checks.permitlist")
                .valueOption();
        this.checkDenyList = configuration.get("CheckResourceLoader.checks.denylist").valueOption();
    }

    public Configuration getConfiguration()
    {
        return this.configuration;
    }

    /**
     * Get configuration for a specific country, overriding for country specific overrides and group
     * specific overrides
     *
     * @param country
     *            country string
     * @return {@link Configuration}
     */
    public Configuration getConfigurationForCountry(final String country)
    {
        Configuration specializedConfiguration = this.configuration
                .configurationForKeyword(country);

        final List<String> groups = this.countryGroups.get(country);
        if (groups != null)
        {
            for (final String group : groups)
            {
                specializedConfiguration = specializedConfiguration.configurationForKeyword(group);
            }
        }
        return specializedConfiguration;
    }

    public <T extends Check> Set<T> loadChecks(final Predicate<Class> isEnabled)
    {
        return loadChecks(isEnabled, this.configuration);
    }

    public <T extends Check> Set<T> loadChecks(final Configuration configuration)
    {
        return loadChecks(this::isEnabledByConfiguration, configuration);
    }

    /**
     * Loads checks that are enabled by some other means, defined by {@code isEnabled}
     *
     * @param isEnabled
     *            {@link Predicate} used to determine if a check is enabled
     * @param configuration
     *            {@link Configuration} used to loadChecks {@link CheckResourceLoader}
     * @param <T>
     *            check type
     * @return a {@link Set} of checks
     */
    @SuppressWarnings("unchecked")
    public <T extends Check> Set<T> loadChecks(final Predicate<Class> isEnabled,
            final Configuration configuration)
    {
        final Class<?>[][] constructorArgumentTypes = new Class<?>[][] { { Configuration.class },
                {} };
        final Object[][] constructorArguments = new Object[][] { { configuration }, {} };
        return this.loadChecksUsingConstructors(isEnabled, constructorArgumentTypes,
                constructorArguments);
    }

    /**
     * Loads checks that are enabled by configuration
     *
     * @param <T>
     *            check type
     * @return a {@link Set} of checks
     */
    public <T extends Check> Set<T> loadChecks()
    {
        return loadChecks(this::isEnabledByConfiguration, this.configuration);
    }

    public <T extends Check> Set<T> loadChecksForCountry(final String country)
    {
        final Configuration countryConfiguration = this.getConfigurationForCountry(country);
        return loadChecks(checkClass -> this.isEnabledByConfiguration(countryConfiguration,
                checkClass, country), countryConfiguration);
    }

    public <T extends Check> Set<T> loadChecksUsingConstructors(
            final Class<?>[][] constructorArgumentTypes, final Object[][] constructorArguments)
    {
        return this.loadChecksUsingConstructors(this::isEnabledByConfiguration,
                constructorArgumentTypes, constructorArguments);
    }

    /**
     * Given a list of corresponding types and arguments, try to initialize each enabled check using
     * each constructor in order.
     *
     * @param isEnabled
     *            {@link Predicate} used to determine if a check is enabled
     * @param constructorArgumentTypes
     *            {@link List} containing arrays of classes. Each array corresponds to one
     *            constructor call. For example, to try a constructor that takes in two doubles but
     *            fall back to a constructor that takes in one double, constructorArgumentTypes
     *            would look like this: [[Double.TYPE, Double.TYPE], [Double.TYPE]]. An empty
     *            constructor is denoted by an empty array.
     * @param constructorArguments
     *            {@link List} containing arrays of objects. This MUST have the same shape as
     *            constructorArgumentTypes and the values MUST be the same types and in the same
     *            order as constructorArgumentTypes. For example, given constructorArgumentTypes of
     *            [[Double.TYPE, Boolean.TYPE], [Boolean.TYPE, Integer.TYPE], []],
     *            constructorArguments needs to look like [[double, boolean], [boolean, int], []].
     * @param <T>
     *            Any class that extends Check.
     * @return A set of enabled, initialized checks.
     */
    public <T extends Check> Set<T> loadChecksUsingConstructors(final Predicate<Class> isEnabled,
            final Class<?>[][] constructorArgumentTypes, final Object[][] constructorArguments)
    {
        final Set<T> checks = new HashSet<>();
        final Time time = Time.now();
        try
        {
            final ClassPath classPath = ClassPath
                    .from(Thread.currentThread().getContextClassLoader());
            this.packages.forEach(packageName -> classPath.getTopLevelClassesRecursive(packageName)
                    .forEach(classInfo ->
                    {
                        final Class<?> checkClass = classInfo.load();
                        if (this.checkType.isAssignableFrom(checkClass)
                                && !Modifier.isAbstract(checkClass.getModifiers())
                                && isEnabled.test(checkClass)
                                && this.checkPermitList.map(permitlist -> permitlist
                                        .contains(checkClass.getSimpleName())).orElse(true)
                                && this.checkDenyList.map(
                                        denylist -> !denylist.contains(checkClass.getSimpleName()))
                                        .orElse(true))
                        {
                            Streams.zip(Stream.of(constructorArgumentTypes),
                                    Stream.of(constructorArguments),
                                    (argTypes, args) -> this.initializeCheckWithArguments(
                                            (Class<T>) checkClass, argTypes, args))
                                    .filter(Optional::isPresent).map(Optional::get).findFirst()
                                    .ifPresent(checks::add);
                        }
                    }));
        }
        catch (final IOException oops)
        {
            throw new CoreException("Failed to discover {} classes on classpath",
                    this.checkType.getSimpleName());
        }

        logger.info("Loaded {} {} in {}", checks.size(), this.checkType.getSimpleName(),
                time.elapsedSince());
        return checks;
    }

    /**
     * Utility method to try to initialize a given check using a particular constructor.
     * 
     * @param checkClass
     *            the class we want to initialize
     * @param constructorArgumentTypes
     *            the signature for the constructor we want to call
     * @param constructorArguments
     *            the arguments we want to pass to the constructor
     * @param <T>
     *            the type of the class we want to initialize
     * @return an {@link Optional} containing the initialized check if the call was a success, or
     *         empty if something went wrong.
     */
    private <T extends Check> Optional<T> initializeCheckWithArguments(final Class<T> checkClass,
            final Class<?>[] constructorArgumentTypes, final Object[] constructorArguments)
    {
        try
        {
            final Constructor<T> constructor = checkClass.getConstructor(constructorArgumentTypes);
            final T result = constructor.newInstance(constructorArguments);
            return Optional.of(result);
        }
        catch (final NoSuchMethodException oops)
        {
            logger.info("No method found for {} with arguments {}", checkClass,
                    Stream.of(constructorArgumentTypes).map(Class::toString)
                            .collect(Collectors.joining(",")));
        }
        catch (final InvocationTargetException oops)
        {
            logger.error("Unable to create a configurable instance of {}. Reason {}",
                    checkClass.getSimpleName(), oops.getMessage());
        }
        catch (final ClassCastException | InstantiationException | IllegalAccessException oops)
        {
            logger.error("Failed to instantiate {}, ignoring. Reason: {}", checkClass.getName(),
                    oops.getMessage());
        }
        return Optional.empty();
    }

    private boolean isEnabledByConfiguration(final Class checkClass)
    {
        return isEnabledByConfiguration(this.configuration, checkClass);
    }

    private boolean isEnabledByConfiguration(final Configuration configuration,
            final Class checkClass)
    {
        final String key = String.format(this.enabledKeyTemplate, checkClass.getSimpleName());
        return configuration.get(key, this.enabledByDefault).value();
    }

    private boolean isEnabledByConfiguration(final Configuration configuration,
            final Class checkClass, final String country)
    {
        final List<String> countryPermitlist = configuration
                .get(String.format(COUNTRY_PERMITLIST_TEMPLATE, checkClass.getSimpleName()),
                        Collections.emptyList())
                .value();
        final List<String> countryDenylist = configuration
                .get(String.format(COUNTRY_DENYLIST_TEMPLATE, checkClass.getSimpleName()),
                        Collections.emptyList())
                .value();
        return this.isEnabledByConfiguration(configuration, checkClass)
                && countryPermitlist.isEmpty() ? !countryDenylist.contains(country)
                        : countryPermitlist.contains(country);
    }
}
