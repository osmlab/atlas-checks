package org.openstreetmap.atlas.checks.distributed;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;

/**
 * A {@link PathFilter} to find GeoJson files
 *
 * @author brian_l_davis
 */
public class GeoJsonPathFilter implements PathFilter
{
    private static final String UNCOMPRESSED_FILE_EXTENSION = FileSuffix.GEO_JSON.toString();
    private static final String COMPRESSED_FILE_EXTENSION = UNCOMPRESSED_FILE_EXTENSION
            + FileSuffix.GZIP.toString();
    private final String extension;

    /**
     * Default constructor
     */
    public GeoJsonPathFilter()
    {
        this(true);
    }

    /**
     * Constructs a {@link GeoJsonPathFilter} with compression flag
     *
     * @param compressed
     *            if {@code true}, filters on the compressed file form
     */
    public GeoJsonPathFilter(final boolean compressed)
    {
        this.extension = compressed ? COMPRESSED_FILE_EXTENSION : UNCOMPRESSED_FILE_EXTENSION;
    }

    @Override
    public boolean accept(final Path path)
    {
        return path.getName().endsWith(this.extension);
    }

    /**
     * @return the file extension used for filtering
     */
    public String getExtension()
    {
        return extension;
    }
}
