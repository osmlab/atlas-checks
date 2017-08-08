package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * The Test Rule class is used to build a Test Atlas from a resource file. The resource Atlas file
 * that we are using is built from an AIA atlas file and grabbing just a small section of the Atlas
 * file that contains a pool that we can test against. The OSM id of the pool is 361663854.
 *
 * @author cuthbertm
 */
public class PoolSizeCheckTestRule extends CoreTestRule
{
    @TestAtlas(loadFromTextResource = "poolsize.atlas")
    private Atlas poolAtlas;

    public Atlas getPoolAtlas()
    {
        return this.poolAtlas;
    }
}
