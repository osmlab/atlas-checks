package org.openstreetmap.atlas.checks.distributed;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;

/**
 * A {@link PathFilter} to find Flag log files
 *
 * @author mkalender
 * @author brian_l_davis
 */
public class LogFilePathFilter implements PathFilter
{
    private static final String UNCOMPRESSED_FILE_EXTENSION = ".log";
    private static final String COMPRESSED_FILE_EXTENSION = UNCOMPRESSED_FILE_EXTENSION
            + FileSuffix.GZIP.toString();

    private final String extension;

    /**
     * Default constructor
     */
    public LogFilePathFilter()
    {
        this(true);
    }

    /**
     * Constructs a {@link LogFilePathFilter} with compression flag
     *
     * @param compressed
     *            if {@code true} filters on the compressed file form
     */
    public LogFilePathFilter(final boolean compressed)
    {
        this.extension = compressed ? COMPRESSED_FILE_EXTENSION : UNCOMPRESSED_FILE_EXTENSION;
    }

    @Override
    public boolean accept(final Path path)
    {
        return path.getName().endsWith(this.extension);
    }

    /**
     * @return The file extension used for filtering
     */
    public String getExtension()
    {
        return this.extension;
    }
}
