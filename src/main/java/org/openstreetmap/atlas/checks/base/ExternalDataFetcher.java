package org.openstreetmap.atlas.checks.base;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.openstreetmap.atlas.checks.distributed.ShardedIntegrityChecksSparkJob;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.generator.tools.filesystem.FileSystemHelper;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.ConcurrentResourceCache;
import org.openstreetmap.atlas.utilities.caching.strategies.NamespaceCachingStrategy;
import org.openstreetmap.atlas.utilities.runtime.Retry;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The fetcher to use for generic resources. The fetcher uses hadoop cache to reduce remote reads.
 * See {@link ShardedIntegrityChecksSparkJob#atlasFetcher}. This a separate class so that it can
 * implement {@link Serializable}
 *
 * @author Taylor Smock
 */
public class ExternalDataFetcher implements Function<String, Optional<Resource>>, Serializable
{
    /**
     * Cache external data sources locally. Does not implement {@link Serializable} since
     * {@link ConcurrentResourceCache} does not offer a no-args constructor, and is not serializable
     * itself.
     *
     * @author Taylor Smock
     */
    private class ExternalDataResourceCache extends ConcurrentResourceCache
    {
        ExternalDataResourceCache()
        {
            super(new NamespaceCachingStrategy(GLOBAL_HADOOP_FILECACHE_NAMESPACE),
                    new ExternalFileFetcher());
        }
    }

    /**
     * Create a serializable function to get external files for use with a
     * {@link org.openstreetmap.atlas.utilities.caching.ResourceCache}.
     *
     * @author Taylor Smock
     */
    private class ExternalFileFetcher implements Function<URI, Optional<Resource>>, Serializable
    {
        private static final long serialVersionUID = 1721253891315559418L;

        @Override
        public Optional<Resource> apply(final URI uri)
        {
            final Retry retry = new Retry(RETRY_ATTEMPTS, Duration.ONE_SECOND).withQuadratic(true);
            final boolean exists = retry.run(() ->
            {
                try (InputStream inputStream = FileSystemHelper
                        .resource(uri.toString(), ExternalDataFetcher.this.configuration).read())
                {
                    return true;
                }
                catch (final Exception e)
                {
                    if (e.getMessage().contains(FileSystemHelper.FILE_NOT_FOUND))
                    {
                        return false;
                    }
                    else
                    {
                        throw new CoreException("Unable to test existence of {}", uri, e);
                    }
                }
            });
            if (!exists)
            {
                if (!ExternalDataFetcher.this.silent)
                {
                    logger.warn("Fetcher: resource {} does not exist!", uri);
                }
                return Optional.empty();
            }
            return Optional.ofNullable(FileSystemHelper.resource(uri.toString(),
                    ExternalDataFetcher.this.configuration));
        }
    }

    private static final long serialVersionUID = 724339604023082195L;
    private static final String GLOBAL_HADOOP_FILECACHE_NAMESPACE = "__HadoopExternalFileCache_global_namespace__";
    private static final Logger logger = LoggerFactory.getLogger(ExternalDataFetcher.class);
    /** Maximum number of retries for where an error occurs when getting a file */
    private static final int RETRY_ATTEMPTS = 5;
    /** The input folder path (same as the atlas file paths) */
    private final String input;
    /** The configuration used to create the cache */
    private final Map<String, String> configuration;
    /** The actual caching object */
    private transient ExternalDataResourceCache cache;
    /**
     * {@code true} implies that the caller does not want to log that files are not present
     */
    private boolean silent;

    /**
     * Create the fetcher to use for generic resources. The fetcher uses hadoop cache to reduce
     * remote reads. See {@link ShardedIntegrityChecksSparkJob#atlasFetcher}.
     *
     * @param input
     *            {@link String} input folder path
     * @param configuration
     *            {@link org.openstreetmap.atlas.generator.tools.spark.SparkJob} configuration map
     */
    public ExternalDataFetcher(final String input, final Map<String, String> configuration)
    {
        this.input = input;
        this.configuration = configuration;
    }

    @Override
    public Optional<Resource> apply(final String string)
    {
        return this.getCache().get(this.getUri(string));
    }

    /**
     * Make missed file messages silent (use when checking for files -- please log actual issues
     * when this is used) Unfortunately, this does not suppress <i>all</i> messages from missed
     * files.
     *
     * @param silent
     *            {@code true} suppresses some logging messages from non-existent files
     */
    public void setSilent(final boolean silent)
    {
        this.silent = silent;
    }

    /**
     * @return The resource cacher to use
     */
    @Nonnull
    private ExternalDataResourceCache getCache()
    {
        if (this.cache == null)
        {
            this.cache = new ExternalDataResourceCache();
        }
        return this.cache;
    }

    /**
     * Get a URI for a path string
     *
     * @param string
     *            The path
     * @return A URI for the string
     */
    private URI getUri(final String string)
    {
        final String atlasURIString = SparkFileHelper.combine(this.input, string);
        try
        {
            return new URI(atlasURIString);
        }
        catch (final URISyntaxException exception)
        {
            throw new CoreException("Bad URI syntax: {}", atlasURIString, exception);
        }
    }

}
