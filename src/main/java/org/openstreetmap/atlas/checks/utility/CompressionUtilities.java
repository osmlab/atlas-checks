package org.openstreetmap.atlas.checks.utility;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities that are useful for compressed or archived streams.
 *
 * @author Taylor Smock
 */
public final class CompressionUtilities
{
    private static final Logger logger = LoggerFactory.getLogger(CompressionUtilities.class);

    /**
     * Get an uncompressed and unarchived input stream
     *
     * @param inputStream
     *            The inputstream to make unarchived/uncompressed
     * @return A usuable inputstream (if unarchived, it will be at the first entry)
     * @throws IOException
     *             If the inputstream cannot be read
     */
    public static InputStream getUncompressedInputStream(final InputStream inputStream)
            throws IOException
    {
        final BufferedInputStream bufferedInput = new BufferedInputStream(inputStream);
        try
        {
            return decompressedInputStream(bufferedInput);
        }
        catch (final CompressorException | IOException e)
        {
            // OK. Not compressed.
        }
        try
        {
            return unarchivedInputStream(bufferedInput);
        }
        catch (final ArchiveException | IOException e)
        {
            // OK. Not archived or compressed. Just return it.
            return bufferedInput;
        }
    }

    /**
     * Decompress an inputstream
     *
     * @param inputStream
     *            The inputstream to decompress
     * @return The decompressed (and potentially unarchived) inputstream. If unarchived, the
     *         position will be at the first entry.
     * @throws IOException
     *             If there is a problem reading the stream
     * @throws CompressorException
     *             If there is a problem decompressing, or if it is not a compressed file
     */
    private static InputStream decompressedInputStream(final InputStream inputStream)
            throws IOException, CompressorException
    {
        final InputStream uncompressed = new CompressorStreamFactory()
                .createCompressorInputStream(inputStream);
        final BufferedInputStream buffered = new BufferedInputStream(uncompressed);
        try
        {
            return unarchivedInputStream(buffered);
        }
        catch (final ArchiveException | IOException e)
        {
            // OK. Probably not archived.
        }
        return buffered;
    }

    /**
     * Try to unarchive an inputstream
     *
     * @param inputStream
     *            The inputstream to unarchive
     * @return The unarchived input stream
     * @throws IOException
     *             If there is an IOException
     * @throws ArchiveException
     *             If there is a problem with the archive (or it isn't one)
     */
    private static InputStream unarchivedInputStream(final InputStream inputStream)
            throws IOException, ArchiveException
    {
        final ArchiveInputStream toRead = new ArchiveStreamFactory()
                .createArchiveInputStream(inputStream);
        try
        {
            toRead.getNextEntry();
        }
        catch (final IOException e)
        {
            logger.trace(e.getLocalizedMessage());
        }
        return toRead;
    }

    private CompressionUtilities()
    {
        // Hide constructor
    }
}
