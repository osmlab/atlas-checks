package org.openstreetmap.atlas.checks.atlas;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;

/**
 * Filters out Atlas files given a Hadoop file {@link Path}
 *
 * @author mgostintsev
 */
public class AtlasFilePathFilter implements PathFilter
{

    @Override
    public boolean accept(final Path path)
    {
        return path.getName().endsWith(FileSuffix.ATLAS.toString()) || path.getName()
                .endsWith(FileSuffix.ATLAS.toString() + FileSuffix.GZIP.toString());
    }

}
