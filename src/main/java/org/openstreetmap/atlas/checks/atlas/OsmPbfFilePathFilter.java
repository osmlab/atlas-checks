package org.openstreetmap.atlas.checks.atlas;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;

/**
 * Filters out OSM protobuf files given a Hadoop file {@link Path}
 *
 * @author brian_l_davis
 */
public class OsmPbfFilePathFilter implements PathFilter
{
    @Override
    public boolean accept(final Path path)
    {
        return path.getName().endsWith(FileSuffix.PBF.toString());
    }
}
