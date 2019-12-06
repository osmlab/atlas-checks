package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author cuthbertm
 * @author savannahostrowski
 */
public class ShortNameCheckTestCaseRule extends CoreTestRule
{
    public static final long INVALID_ATLAS_ID = 1L;

    public static final long VALID_ATLAS_ID = 2L;

    @TestAtlas(areas = { @TestAtlas.Area(id = INVALID_ATLAS_ID + "", tags = { "name=A",
            "iso_country_code=USA" }) })
    private Atlas badAtlas;

    @TestAtlas(areas = { @TestAtlas.Area(id = VALID_ATLAS_ID + "", tags = { "name=AB",
            "iso_country_code=USA" }) })
    private Atlas goodAtlas;

    @TestAtlas(areas = {
            @TestAtlas.Area(id = VALID_ATLAS_ID + "", tags = { "name=", "iso_country_code=IRN" }) })
    private Atlas shortNameBadAtlas;

    @TestAtlas(areas = { @TestAtlas.Area(id = INVALID_ATLAS_ID + "", tags = { "name=A",
            "iso_country_code=IRN" }) })
    private Atlas shortNameGoodAtlas;

    public Atlas bad()
    {
        return this.badAtlas;
    }

    public Atlas good()
    {
        return this.goodAtlas;
    }

    public Atlas shortNameBadAtlas()
    {
        return this.shortNameBadAtlas;
    }

    public Atlas shortNameGoodAtlas()
    {
        return this.shortNameGoodAtlas;
    }

    public org.openstreetmap.atlas.geography.atlas.items.Area shortNameTag()
    {
        return this.badAtlas.area(INVALID_ATLAS_ID);
    }
}
