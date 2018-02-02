package org.openstreetmap.atlas.checks.persistence;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.openstreetmap.atlas.checks.atlas.CountrySpecificAtlasFilePathFilter;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.generator.tools.filesystem.FileSystemCreator;
import org.openstreetmap.atlas.generator.tools.filesystem.FileSystemHelper;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.streaming.resource.http.GetResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.runtime.Retry;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for listing and validating the presence of {@link Atlas} files
 *
 * @author mgostintsev
 * @author Sid
 */
public class SparkFileHelper implements Serializable
{
    // Constants for I/O operations
    public static final String TEMPORARY_FOLDER_NAME = "_temp";
    public static final String EMPTY_STRING = "";
    public static final String DIRECTORY_SEPARATOR = "/";
    public static final String EXTENSION_SEPARATOR = ".";
    private static final long serialVersionUID = -5716285735225965942L;
    private static final Logger logger = LoggerFactory.getLogger(SparkFileHelper.class);
    private static final Duration MAX_DURATION_FOR_IO = Duration.hours(3);
    private static final int IO_RETRY_COUNT = 5;
    private static final Duration WAIT_DURATION_BEFORE_IO_RETRY = Duration.seconds(5);
    private static Retry IO_RETRY = new Retry(IO_RETRY_COUNT, WAIT_DURATION_BEFORE_IO_RETRY);

    // Spark context useful for read/write from/to different file systems
    private final Map<String, String> sparkContext;

    /**
     * Combines given paths
     *
     * @param basePath
     *            a base path
     * @param paths
     *            a list of paths
     * @return the combined path
     */
    public static String combine(final String basePath, final String... paths)
    {
        final StringBuilder builder = new StringBuilder(pathNotEndingWithSeparator(basePath));
        for (final String path : paths)
        {
            builder.append(DIRECTORY_SEPARATOR);
            builder.append(pathNotStartingWithSeparator(path));
        }

        return builder.toString();
    }

    /**
     * Removes {@code EXTENSION_SEPARATOR} from the beginning of given path
     *
     * @param path
     *            Path to remove {@code EXTENSION_SEPARATOR} from the beginning
     * @return a path not starting with {@code EXTENSION_SEPARATOR}
     */
    public static String extensionStartingWithSeparator(final String path)
    {
        return pathStartingWithSeparator(path, EXTENSION_SEPARATOR);
    }

    /**
     * Returns parent of given path
     *
     * @param path
     *            a path
     * @return the parent path
     */
    public static String parentPath(final String path)
    {
        if (path == null)
        {
            logger.warn("Null path. Returning empty path.");
            return EMPTY_STRING;
        }

        final int lastSeparatorIndex = path.lastIndexOf(DIRECTORY_SEPARATOR);
        if (lastSeparatorIndex > 0)
        {
            return path.substring(0, lastSeparatorIndex);
        }

        logger.debug("Given path doesn't have a parent path. Returning the path as is.");
        return path;
    }

    /**
     * Removes {@code PATH_SEPARATOR} from the end of given path
     *
     * @param path
     *            a path
     * @return a path not ending with {@code PATH_SEPARATOR}
     */
    public static String pathNotEndingWithSeparator(final String path)
    {
        return pathNotEndingWithSeparator(path, DIRECTORY_SEPARATOR);
    }

    /**
     * Removes {@code PATH_SEPARATOR} from the beginning of given path.
     *
     * @param path
     *            a path
     * @return a path not beginning with {@code PATH_SEPARATOR}
     */
    public static String pathNotStartingWithSeparator(final String path)
    {
        return pathNotStartingWithSeparator(path, DIRECTORY_SEPARATOR);
    }

    private static String pathNotEndingWithSeparator(final String path, final String separator)
    {
        if (path == null)
        {
            logger.warn("Null path. Returning empty path.");
            return EMPTY_STRING;
        }

        if (separator == null)
        {
            logger.warn("Null separator. Returning empty path.");
            return EMPTY_STRING;
        }

        final int lastSeparatorIndex = path.lastIndexOf(separator);
        return lastSeparatorIndex == path.length() - 1 ? path.substring(0, lastSeparatorIndex)
                : path;
    }

    private static String pathNotStartingWithSeparator(final String path, final String separator)
    {
        if (path == null)
        {
            logger.warn("Null path. Returning empty path.");
            return EMPTY_STRING;
        }

        if (separator == null)
        {
            logger.warn("Null separator. Returning empty path.");
            return EMPTY_STRING;
        }

        final int firstSeparatorIndex = path.indexOf(separator);
        return firstSeparatorIndex == 0 ? path.substring(1) : path;
    }

    private static String pathStartingWithSeparator(final String path, final String separator)
    {
        if (path == null)
        {
            logger.warn("Null path. Returning empty path.");
            return EMPTY_STRING;
        }

        if (separator == null)
        {
            logger.warn("Null separator. Returning empty path.");
            return EMPTY_STRING;
        }

        final int firstSeparatorIndex = path.indexOf(separator);
        return firstSeparatorIndex != 0 ? separator + path : path;
    }

    /**
     * Constructs a helper with given context
     *
     * @param sparkContext
     *            Spark context as key-value pairs to use as context
     */
    public SparkFileHelper(final Map<String, String> sparkContext)
    {
        this.sparkContext = sparkContext;
    }

    /**
     * Returns a list of Atlas {@link Resource}s for the given country in the supplied directory
     *
     * @param directory
     *            the directory from which to collect the {@link Atlas} files
     * @param country
     *            the country, whose {@link Atlas} files we're interested in
     * @param recursive
     *            {@code true} to search the given directory and all sub-directories, {@code false}
     *            to only search the root directory
     * @return a list of Atlas {@link Resource}s for the given country
     */
    public List<Resource> collectAtlasFiles(final String directory, final String country,
            final boolean recursive)
    {
        final CountrySpecificAtlasFilePathFilter filter = new CountrySpecificAtlasFilePathFilter(
                country);
        return recursive
                ? FileSystemHelper.listResourcesRecursively(directory, this.sparkContext, filter)
                : FileSystemHelper.resources(directory, this.sparkContext, filter);
    }

    /**
     * Returns an Atlas {@link Resource} for the given location URI string. The resource is resolve
     * and returned if the URI points to single resource, not a resource directory, that conforms to
     * a path defined by at least one of the provided {@link PathFilter}s. The {@link PathFilter}s
     * provide a way to find well known data types that can be used either directly as or
     * transformed to an Atlas.
     *
     * @param uri
     *            the location of the Atlas datasource
     * @param filters
     *            {@link PathFilter}s used to find datasource types
     * @return an {@link Atlas} {@link Resource}
     */
    public Optional<Resource> collectSourceFile(final String uri, final PathFilter... filters)
    {
        final Path path = new Path(uri);
        Resource resource = null;
        if (Stream.of(filters).anyMatch(filter -> filter.accept(path)))
        {
            final String schema = URI.create(uri).getScheme();
            if ("http".equals(schema) || "https".equals(schema))
            {
                logger.info("Downloading {}", uri);
                resource = new InputStreamResource(
                        new BufferedInputStream(new GetResource(uri).read())).withName(uri);
            }
            if (resource == null)
            {
                logger.info("Loading {}", uri);
                resource = FileSystemHelper.resource(uri, this.sparkContext);
            }
        }
        return Optional.ofNullable(resource);
    }

    /**
     * Returns an list of Atlas {@link Resource}s from the given location URI string. Resources are
     * resolve and returned that conform to any one of the {@link PathFilter}s. The
     * {@link PathFilter}s provide a way to find well known data types that can be used either
     * directly as or transformed to an Atlas.
     *
     * @param directory
     *            a location of the Atlas datasource
     * @param recursive
     *            {@code true}, to search the given directory and all sub-directories. {@code false}
     *            , to only search the root directory
     * @param filters
     *            {@link PathFilter}s used to find datasource types
     * @return an Atlas {@link Resource}
     */
    public List<Resource> collectSourceFiles(final String directory, final boolean recursive,
            final PathFilter... filters)
    {
        return Iterables.stream(Iterables.from(filters))
                .flatMap(filter -> recursive
                        ? FileSystemHelper.listResourcesRecursively(directory, this.sparkContext,
                                filter)
                        : FileSystemHelper.resources(directory, this.sparkContext, filter))
                .collectToList();
    }

    /**
     * Renames the {@link SparkFilePath#temporaryPath} to the {@link SparkFilePath#targetPath},
     * taking care to avoid producing nested directories.
     *
     * @param path
     *            {@link SparkFilePath} to commit
     */
    public void commit(final SparkFilePath path)
    {
        try
        {
            if (this.isDirectory(path.getTemporaryPath()))
            {
                logger.debug("Path {} is a directory. Renaming all the files under.", path);
                if (!this.exists(path.getTargetPath()))
                {
                    logger.debug("Creating {}.", path.getTargetPath());
                    this.mkdir(path.getTargetPath());
                }

                this.list(path.getTemporaryPath()).forEach(resource ->
                {
                    logger.debug("Renaming {} in {} into {}.", resource.getName(),
                            path.getTemporaryPath(), path.getTargetPath());
                    this.rename(
                            SparkFileHelper.combine(path.getTemporaryPath(), resource.getName()),
                            SparkFileHelper.combine(path.getTargetPath(), resource.getName()));
                });
            }
            else
            {
                logger.debug("Renaming {} to {}.", path.getTemporaryPath(), path.getTargetPath());
                this.rename(path.getTemporaryPath(), path.getTargetPath());
            }
        }
        catch (final Exception e)
        {
            logger.warn("Renaming {} failed!", path, e);
        }
    }

    /**
     * Deletes given directory and all it's child items
     *
     * @param path
     *            a path
     */
    public void deleteDirectory(final String path)
    {
        if (!FileSystemHelper.delete(path, true, this.sparkContext))
        {
            throw new CoreException("Delete directory for {} is failed.", path);
        }
    }

    /**
     * Verifies that the input directory containing {@link Atlas} files contains all the expected
     * countries
     *
     * @param directory
     *            the directory which contains the {@link Atlas} files
     * @param expectedCountries
     *            the expected {@link StringList} of country ISO3 codes
     * @param recursive
     *            {@code true} to search the given directory and all sub-directories, {@code false}
     *            to only search the root directory
     * @return {@code true} if all expected country ISO3 codes are in the given directory,
     *         {@code false} otherwise
     */
    public boolean directoryContainsExpectedCountryAtlases(final String directory,
            final StringList expectedCountries, final boolean recursive)
    {
        for (final String country : expectedCountries)
        {
            final List<Resource> atlases = collectAtlasFiles(directory, country, recursive);
            if (atlases.isEmpty())
            {
                // Fail-fast if we recognize a missing country
                logger.error("Missing Atlas files for {}!", country);
                return false;
            }
        }
        return true;
    }

    /**
     * @param path
     *            Path to check if it exists or not
     * @return true if given path exists, otherwise false
     */
    public boolean exists(final String path)
    {
        try
        {
            final FileSystem fileSystem = new FileSystemCreator().get(path, this.sparkContext);
            return fileSystem.exists(new Path(path));
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to check if given path {} is a directory or not.", path,
                    e);
        }
    }

    /**
     * @param path
     *            Path to check if it is a directory or not
     * @return true if given path is directory, otherwise false.
     */
    public boolean isDirectory(final String path)
    {
        try
        {
            final FileSystem fileSystem = new FileSystemCreator().get(path, this.sparkContext);
            return fileSystem.isDirectory(new Path(path));
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to check if given path {} is a directory or not.", path,
                    e);
        }
    }

    /**
     * @param path
     *            Path to directory to list {@link Resource}s under.
     * @return List of {@link Resource}s under given path.
     */
    public List<Resource> list(final String path)
    {
        return FileSystemHelper.resources(path, this.sparkContext);
    }

    /**
     * Creates a directory via given path
     *
     * @param path
     *            a path
     */
    public void mkdir(final String path)
    {
        if (!FileSystemHelper.mkdir(path, this.sparkContext))
        {
            throw new CoreException("Create directory for {} is failed.", path);
        }
    }

    /**
     * Renames a source path to destination path
     *
     * @param sourcePath
     *            source path
     * @param destinationPath
     *            destination path
     */
    public void rename(final String sourcePath, final String destinationPath)
    {
        if (!FileSystemHelper.rename(sourcePath, destinationPath, this.sparkContext))
        {
            throw new CoreException("Rename from {} to {} is failed.", sourcePath, destinationPath);
        }
    }

    /**
     * Executes {@code SparkFileOutput#getSaveFunc()} for given {@link SparkFileOutput}s
     *
     * @param outputs
     *            {@link SparkFileOutput}s to execute
     */
    public void save(final List<SparkFileOutput> outputs)
    {
        // Write results
        try (Pool writePool = new Pool(outputs.size(), "I/O pool", MAX_DURATION_FOR_IO))
        {
            for (final SparkFileOutput output : outputs)
            {
                writePool.queue(() ->
                {
                    IO_RETRY.run(() ->
                    {
                        logger.debug("Writing {}: {}.", output.getOperationName(),
                                output.getPath().getTemporaryPath());

                        final Time timer = Time.now();
                        output.getSaveFunction().accept(FileSystemHelper.writableResource(
                                output.getPath().getTemporaryPath(), this.sparkContext));
                        logger.debug("{} write took {} ms.", output.getOperationName(),
                                timer.elapsedSince().asMilliseconds());
                    });
                });
            }
        }
        catch (final Exception e)
        {
            logger.error("Failed save files.", e);
        }
    }

    /**
     * Executes {@code SparkFileOutput#getSaveFunc()} for given {@link SparkFileOutput}s
     *
     * @param outputs
     *            {@link SparkFileOutput}s to execute
     */
    public void save(final SparkFileOutput... outputs)
    {
        save(Arrays.asList(outputs));
    }

    /**
     * Writes given content into given directory with given filename
     *
     * @param directory
     *            a directory path to write files into
     * @param filename
     *            the name of the file
     * @param content
     *            file content
     */
    public void write(final String directory, final String filename, final String content)
    {
        IO_RETRY.run(() ->
        {
            final WritableResource resource = FileSystemHelper
                    .writableResource(combine(directory, filename), this.sparkContext);
            try (BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(resource.write(), StandardCharsets.UTF_8)))
            {
                out.write(content);
            }
            catch (final Exception e)
            {
                throw new CoreException(
                        String.format("Could not save into %s.", resource.getName()), e);
            }
        });
    }
}
