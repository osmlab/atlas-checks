package org.openstreetmap.atlas.checks.utility;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * A set of utility classes for reading files
 *
 * @author danielbaah
 */
public final class FileUtility
{

    private static final String LOG_EXTENSION = "log";
    private static final String ZIPPED_LOG_EXTENSION = ".log.gz";

    /**
     * An enum containing the different types of input files that we can handle.
     */
    public enum LogOutputFileType
    {
        LOG,
        COMPRESSED_LOG
    }

    /**
     * Determine whether or not this file is something we can handle, and classify it accordingly.
     *
     * @param logFile
     *            any file
     * @return if this file is something this command can handle, the appropriate LogOutputFileType
     *         enum value; otherwise, an empty optional.
     */
    public static Optional<LogOutputFileType> getOptionalLogOutputType(final File logFile)
    {
        // Note that technically the true extension is just .gz, so we can't use the same method as
        // below.
        if (logFile.getName().endsWith(ZIPPED_LOG_EXTENSION))
        {
            return Optional.of(LogOutputFileType.COMPRESSED_LOG);
        }
        else if (FilenameUtils.getExtension(logFile.getName()).equals(LOG_EXTENSION))
        {
            return Optional.of(LogOutputFileType.LOG);
        }
        return Optional.empty();
    }

    /**
     * Read a file that we know we should be able to handle
     *
     * @param inputFile
     *            Some file with a valid, appropriate extension.
     * @param fileType
     *            The type of file that inputFile is
     * @return a BufferedReader to read inputFile
     * @throws IOException
     *             if the file is not found or is poorly formatted, given its extension. For
     *             example, if this file is gzipped and something goes wrong in the unzipping
     *             process, it might throw an error
     */
    public static BufferedReader getReader(final File inputFile, final LogOutputFileType fileType)
            throws IOException
    {
        if (fileType == LogOutputFileType.LOG)
        {
            return new BufferedReader(new FileReader(inputFile.getPathString()));
        }
        return new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(inputFile.getPathString()))));
    }

    private FileUtility()
    {
        // No op
    }
}
