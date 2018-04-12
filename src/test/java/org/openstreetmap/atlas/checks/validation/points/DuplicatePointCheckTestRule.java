package org.openstreetmap.atlas.checks.validation.points;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * {@link DuplicatePointCheckTest} data generator
 *
 * @author savannahostrowski
 */
public class DuplicatePointCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "37.3314171,-122.0304871";

    @TestAtlas(
            // points
            points = { @Point(id = "1", coordinates = @Loc(value = TEST_1)) })
    private Atlas singlePointAtlas;
    @TestAtlas(
            // points
            points = { @Point(id = "2", coordinates = @Loc(value = TEST_1)),
                    @Point(id = "3", coordinates = @Loc(value = TEST_1)) })
    private Atlas duplicatePointAtlas;

    public Atlas singlePointAtlas()
    {
        return this.singlePointAtlas;
    }

    public Atlas duplicatePointAtlas()
    {
        return this.duplicatePointAtlas;
    }

}
