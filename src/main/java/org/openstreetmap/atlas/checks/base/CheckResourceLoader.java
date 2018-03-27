package org.openstreetmap.atlas.checks.base;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 */
public class CheckResourceLoader
{
    private static final String DEFAULT_ENABLED_KEY_TEMPLATE = "%s.enabled";
    private static final String DEFAULT_PACKAGE = "org.openstreetmap.atlas.checks";
    private static final String DEFAULT_TYPE = Check.class.getName();
    private static final Logger logger = LoggerFactory.getLogger(CheckResourceLoader.class);
    private final Class<?> checkType;
    private final Configuration configuration;
    private final MultiMap<String, String> countryGroups = new MultiMap<>();
    private final Boolean enabledByDefault;
    private final String enabledKeyTemplate;
    private final Set<String> packages;

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
        final Set<T> checks = new HashSet<>();
        final Time time = Time.now();
        try
        {
            final ClassPath classPath = ClassPath
                    .from(Thread.currentThread().getContextClassLoader());
            packages.forEach(packageName -> classPath.getTopLevelClassesRecursive(packageName)
                    .forEach(classInfo ->
                    {
                        final Class<?> checkClass = classInfo.load();
                        if (checkType.isAssignableFrom(checkClass)
                                && !Modifier.isAbstract(checkClass.getModifiers())
                                && isEnabled.test(checkClass))
                        {
                            try
                            {
                                Object check;
                                try
                                {
                                    check = checkClass.getConstructor(Configuration.class)
                                            .newInstance(configuration);
                                }
                                catch (final InvocationTargetException oops)
                                {
                                    throw new CoreException(
                                            "Unable to create a configurable instance of {}",
                                            checkClass.getSimpleName(), oops);
                                }
                                catch (final NoSuchMethodException oops)
                                {
                                    check = checkClass.newInstance();
                                }

                                if (check != null)
                                {
                                    checks.add((T) check);
                                }
                            }
                            catch (final ClassCastException | InstantiationException
                                    | IllegalAccessException oops)
                            {
                                logger.error("Failed to instantiate {}, ignoring. Reason: {}",
                                        checkClass.getName(), oops.getMessage());
                            }
                        }
                    }));
        }
        catch (final IOException oops)
        {
            throw new CoreException("Failed to discover {} classes on classpath",
                    checkType.getSimpleName());
        }

        logger.info("Loaded {} {} in {}", checks.size(), checkType.getSimpleName(),
                time.elapsedSince());
        return checks;
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

    public <T extends Check> Set<T> loadChecksForCountry(final Predicate<Class> isEnabled,
            final String country)
    {
        return loadChecks(isEnabled, this.getConfigurationForCountry(country));
    }

    public <T extends Check> Set<T> loadChecksForCountry(final String country)
    {
        return loadChecksForCountry(this::isEnabledByConfiguration, country);
    }

    private boolean isEnabledByConfiguration(final Class checkClass)
    {
        final String key = String.format(enabledKeyTemplate, checkClass.getSimpleName());
        return configuration.get(key, enabledByDefault).value();
    }
}
