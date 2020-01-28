package org.openstreetmap.atlas.checks.configuration;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.streaming.resource.http.GetResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.MergedConfiguration;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves several different {@link Configuration} sources for runtime configuration
 *
 * @author brian_l_davis
 */
public final class ConfigurationResolver
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationResolver.class);

    /**
     * Creates an empty {@link Configuration} for use when no configuration is needed
     * 
     * @return a new {@link Configuration}
     */
    public static Configuration emptyConfiguration()
    {
        return inlineConfiguration("{}");
    }

    /**
     * Helper method that returns an {@link InputStream} to a JSON configuration referenced by URI
     *
     * @param uri
     *            the location of the configuration
     * @return an {@code Optional} {@link InputStream}
     */
    public static Optional<InputStream> getResourceAsStream(final URI uri)
    {
        final byte[] configuration = Optional.ofNullable(uri.getScheme()).map(schema ->
        {
            switch (schema)
            {
                case "http":
                case "https":
                    final GetResource getResource = new GetResource(uri);
                    Optional.ofNullable(uri.getUserInfo()).ifPresent(userInfo ->
                    {
                        final String[] userPass = userInfo.split(":");
                        if (userPass.length == 2)
                        {
                            getResource.setAuth(userPass[0], userPass[1]);
                        }
                    });
                    logger.info("Getting remote configuration {}", uri);
                    final byte[] response = getResource.readBytesAndClose();
                    if (getResource.getStatusCode() != HttpStatus.SC_OK)
                    {
                        throw new CoreException("Failed to get configuration [{}]",
                                new String(response));
                    }
                    return response;
                default:
                    logger.info("Loading local configuration {}", uri);
                    return new File(uri.getPath()).readBytesAndClose();
            }
        }).orElseThrow(() -> new CoreException("Unable to read configuration {}", uri));

        return Optional.of(new BufferedInputStream(new ByteArrayInputStream(configuration)));
    }

    /**
     * Helper method that returns an {@link InputStream} to a JSON configuration available on the
     * {@code ClassPath}
     * 
     * @param resource
     *            a fully qualified resource name
     * @return an {@code Optional} {@link InputStream}
     */
    public static Optional<InputStream> getResourceAsStream(final String resource)
    {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return Optional.ofNullable(loader.getResourceAsStream(resource));
    }

    /**
     * Creates a {@link Configuration} defined inline
     *
     * @param inline
     *            JSON configuration string
     * @return a new {@link Configuration}
     */
    public static Configuration inlineConfiguration(final String inline)
    {
        return new StandardConfiguration(new StringResource(inline));
    }

    /**
     * Resolves a {@link Configuration} for {@link Command}s given the {@link CommandMap} and
     * {@link Command.Switch}s available
     *
     * @param commandMap
     *            the {@link Command}'s {@link CommandMap}
     * @param keyFiles
     *            the {@link Command.Switch} containing a list of URIs for configuration
     * @param keyJson
     *            the {@link Command.Switch} containing inline JSON configuration
     * @return a new {@link Configuration}
     */
    public static Configuration loadConfiguration(final CommandMap commandMap,
            final Command.Switch<StringList> keyFiles, final Command.Switch<String> keyJson)
    {
        final List<InputStream> configurationSources = new ArrayList<>();

        ConfigurationResolver.getResourceAsStream("application.json")
                .ifPresent(configurationSources::add);
        commandMap.getOption(keyFiles)
                .ifPresent(files -> ((StringList) files)
                        .forEach(uri -> ConfigurationResolver.getResourceAsStream(URI.create(uri))
                                .ifPresent(configurationSources::add)));
        commandMap.getOption(keyJson)
                .map(value -> new ByteArrayInputStream(
                        value.toString().getBytes(StandardCharsets.UTF_8)))
                .ifPresent(configurationSources::add);

        final List<Resource> configurationResources = configurationSources.stream()
                .map(inputStream -> new InputStreamResource(() -> inputStream))
                .collect(Collectors.toList());

        final Configuration configuration;
        Throwable thrown = null;
        try
        {
            configuration = new MergedConfiguration(Iterables.head(configurationResources),
                    Iterables.tail(configurationResources));
        }
        finally
        {
            for (final InputStream source : configurationSources)
            {
                try
                {
                    source.close();
                }
                catch (final IOException ioe)
                {
                    thrown = ioe;
                }
            }
        }
        if (thrown != null)
        {
            throw new CoreException("Failed to load configuration", thrown);
        }
        return configuration;
    }

    /**
     * Resolves a {@link Configuration} from the classpath
     *
     * @param path
     *            a fully qualified resource name
     * @param context
     *            a {@code Class} to get the {@code ClassLoader} from
     * @return a new {@link Configuration}
     */
    public static Configuration resourceConfiguration(final String path, final Class context)
    {
        try (InputStream config = context.getResourceAsStream(path))
        {
            return new StandardConfiguration(new InputStreamResource(() -> config));
        }
        catch (final IOException oops)
        {
            Assert.fail(oops.getMessage());
        }
        return null;
    }

    private ConfigurationResolver()
    {
    }
}
