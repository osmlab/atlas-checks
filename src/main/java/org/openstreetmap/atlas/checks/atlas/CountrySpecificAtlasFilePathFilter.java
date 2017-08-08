package org.openstreetmap.atlas.checks.atlas;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;

/**
 * Filters out country-specific {@link Atlas} files given a Hadoop file {@link Path}
 *
 * @author mgostintsev
 */
public class CountrySpecificAtlasFilePathFilter implements PathFilter
{
    private final String country;

    /**
     * Constructs a filter for given country
     *
     * @param country
     *            country ISO3 code to create filter for
     */
    public CountrySpecificAtlasFilePathFilter(final String country)
    {
        this.country = country;
    }

    @Override
    public boolean accept(final Path path)
    {
        return path.getName().contains(this.country)
                && (path.getName().endsWith(FileSuffix.ATLAS.toString()) || path.getName()
                        .endsWith(FileSuffix.ATLAS.toString() + FileSuffix.GZIP.toString()));
    }

}
