package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link ConstructionCheck}
 *
 * @author v-brjor
 */
public class ConstructionCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "0, 0";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "building=construction",
            "open_date=2017" }) })
    private Atlas isBuildingConstructionAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "construction=any",
            "open_date=2017" }) })
    private Atlas isConstructionAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "highway=construction",
            "open_date=2017" }) })
    private Atlas isHighwayConstructionAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "construction:date=any",
            "open_date=2017" }) })
    private Atlas isNotConstructionColonDateAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "landuse=construction",
            "open_date=2017" }) })
    private Atlas isLandUseConstructionAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "construction:=any",
            "open_date=2017" }) })
    private Atlas isStartsWithConstructionColonAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = {
            "open_date=1 January 2017", "highway=construction" }) })
    private Atlas dateFormatdMMMMyyyyAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "open_date=1-Jan-2017",
            "highway=construction" }) })
    private Atlas dateFormatdMMMyyyyAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "open_date=1-1-2017",
            "highway=construction" }) })
    private Atlas dateFormatdMyyyyAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "open_date=1-2017",
            "highway=construction" }) })
    private Atlas dateFormatdyyyyAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "open_date=Jan-2017",
            "highway=construction" }) })
    private Atlas dateFormatMMMyyyyAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = {
            "open_date=January 2017", "highway=construction" }) })
    private Atlas dateFormatMMMMyyyyAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "open_date=2017",
            "highway=construction" }) })
    private Atlas dateFormatyyyyAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "open_date=2017-1-1",
            "highway=construction" }) })
    private Atlas dateFormatyyyyMdAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "open_date=2017-1",
            "highway=construction" }) })
    private Atlas dateFormatyyyyMAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "check_date=2017",
            "construction=any" }) })
    private Atlas oldCheckDateAtlas;
    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = {
            "last_edit_time=1483257600", "construction=any" }) })
    private Atlas oldLastEditTimeAtlas;

    public Atlas dateFormatMMMMyyyyAtlas()
    {
        return this.dateFormatMMMMyyyyAtlas;
    }

    public Atlas dateFormatMMMyyyyAtlas()
    {
        return this.dateFormatMMMyyyyAtlas;
    }

    public Atlas dateFormatdMMMMyyyyAtlas()
    {
        return this.dateFormatdMMMMyyyyAtlas;
    }

    public Atlas dateFormatdMMMyyyyAtlas()
    {
        return this.dateFormatdMMMyyyyAtlas;
    }

    public Atlas dateFormatdMyyyyAtlas()
    {
        return this.dateFormatdMyyyyAtlas;
    }

    public Atlas dateFormatdyyyyAtlas()
    {
        return this.dateFormatdyyyyAtlas;
    }

    public Atlas dateFormatyyyyAtlas()
    {
        return this.dateFormatyyyyAtlas;
    }

    public Atlas dateFormatyyyyMAtlas()
    {
        return this.dateFormatyyyyMAtlas;
    }

    public Atlas dateFormatyyyyMdAtlas()
    {
        return this.dateFormatyyyyMdAtlas;
    }

    public Atlas isBuildingConstructionAtlas()
    {
        return this.isBuildingConstructionAtlas;
    }

    public Atlas isConstructionAtlas()
    {
        return this.isConstructionAtlas;
    }

    public Atlas isHighwayConstructionAtlas()
    {
        return this.isHighwayConstructionAtlas;
    }

    public Atlas isLandUseConstructionAtlas()
    {
        return this.isLandUseConstructionAtlas;
    }

    public Atlas isNotConstructionColonDateAtlas()
    {
        return this.isNotConstructionColonDateAtlas;
    }

    public Atlas isStartsWithConstructionColonAtlas()
    {
        return this.isStartsWithConstructionColonAtlas;
    }

    public Atlas oldCheckDateAtlas()
    {
        return this.oldCheckDateAtlas;
    }

    public Atlas oldLastEditTimeAtlas()
    {
        return this.oldLastEditTimeAtlas;
    }
}
