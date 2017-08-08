package org.openstreetmap.atlas.checks.base;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
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
 */
public class CheckResourceLoader
{
    private static final String DEFAULT_ENABLED_KEY_TEMPLATE = "%s.enabled";
    private static final Logger logger = LoggerFactory.getLogger(CheckResourceLoader.class);
    private static final String DEFAULT_PACKAGE = "org.openstreetmap.atlas.checks";
    private static final String DEFAULT_TYPE = Check.class.getName();

    private final Configuration configuration;
    private final Class<?> checkType;
    private final Set<String> packages;
    private final Boolean enabledByDefault;
    private final String enabledKeyTemplate;

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

    /**
     * Loads checks that are enabled by configuration
     *
     * @param <T>
     *            check type
     * @return a {@link Set} of checks
     */
    public <T extends Check> Set<T> loadChecks()
    {
        return loadChecks(this::isEnabledByConfiguration);
    }

    /**
     * Loads checks that are enabled by some other means, defined by {@code isEnabled}
     *
     * @param isEnabled
     *            {@link Predicate} used to determine if a check is enabled
     * @param <T>
     *            check type
     * @return a {@link Set} of checks
     */
    @SuppressWarnings("unchecked")
    public <T extends Check> Set<T> loadChecks(final Predicate<Class> isEnabled)
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

    private boolean isEnabledByConfiguration(final Class checkClass)
    {
        final String key = String.format(enabledKeyTemplate, checkClass.getSimpleName());
        return configuration.get(key, enabledByDefault).value();
    }
}
