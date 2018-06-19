package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * Tests for {@link MixedCaseNameCheck}
 *
 * @author bbreithaupt
 */
public class MixedCaseNameCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "47.2136626201459,-122.443275382856";
    private static final String TEST_2 = "47.2138327316739,-122.44258668766";
    private static final String TEST_3 = "47.2136626201459,-122.441897992465";

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=tEst TeSt" }) })
    private Atlas invalidNamePoint;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=tEst TeSt" }) })
    private Atlas invalidNameNode;

    @TestAtlas(
            // Lines
            lines = { @Line(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                    @Loc(value = TEST_3) }, tags = { "iso_country_code=USA", "name=tEst TeSt" }) })
    private Atlas invalidNameLine;

    @TestAtlas(
            // Areas
            areas = { @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                    @Loc(value = TEST_3) }, tags = { "iso_country_code=USA", "name=tEst TeSt" }) })
    private Atlas invalidNameArea;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = { @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                    @Loc(value = TEST_3) }, tags = { "iso_country_code=USA", "name=tEst TeSt" }) })
    private Atlas invalidNameEdge;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=tEst" }) })
    private Atlas invalidNamePointOneWord;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=Test-Test" }) })
    private Atlas validNamePointHyphen;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=Test-TesT" }) })
    private Atlas invalidNamePointHyphen;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=McMan" }) })
    private Atlas validNamePointAffix;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=McMaN" }) })
    private Atlas invalidNamePointAffix;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=O'Flanagan" }) })
    private Atlas validNamePointApostrophe;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=Scott's" }) })
    private Atlas validNamePointApostropheLower;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=SCOTT'S" }) })
    private Atlas validNamePointApostropheAllCaps;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=Scott'S" }) })
    private Atlas invalidNamePointApostrophe;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=TEST" }) })
    private Atlas validNamePointAllCaps;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=test" }) })
    private Atlas validNamePointNoCaps;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=Test of Test" }) })
    private Atlas validNamePointLowerCaseWord;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name:en=tEst TeSt" }) })
    private Atlas invalidNamePointEn;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=GRC",
                    "name=τΕστ ΤεΣτ" }) })
    private Atlas invalidNamePointGreek;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=GRC",
                    "name=Τεστ Τεστ" }) })
    private Atlas validNamePointGreek;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=GRC",
                    "name:el=τΕστ ΤεΣτ" }) })
    private Atlas invalidNamePointGreekEl;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=GRC",
                    "name:el=Τεστ Τεστ" }) })
    private Atlas validNamePointGreekEl;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=CHN",
                    "name:en=tEst TeSt", "name=Test of Test" }) })
    private Atlas invalidNamePointChn;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=CHN",
                    "name:en=Test of Test", "name=tEst TeSt" }) })
    private Atlas validNamePointChn;

    public Atlas invalidNamePoint()
    {
        return this.invalidNamePoint;
    }

    public Atlas invalidNameNode()
    {
        return this.invalidNameNode;
    }

    public Atlas invalidNameLine()
    {
        return this.invalidNameLine;
    }

    public Atlas invalidNameArea()
    {
        return this.invalidNameArea;
    }

    public Atlas invalidNameEdge()
    {
        return this.invalidNameEdge;
    }

    public Atlas invalidNamePointOneWord()
    {
        return this.invalidNamePointOneWord;
    }

    public Atlas validNamePointHyphen()
    {
        return this.validNamePointHyphen;
    }

    public Atlas invalidNamePointHyphen()
    {
        return this.invalidNamePointHyphen;
    }

    public Atlas validNamePointAffix()
    {
        return this.validNamePointAffix;
    }

    public Atlas invalidNamePointAffix()
    {
        return this.invalidNamePointAffix;
    }

    public Atlas validNamePointApostrophe()
    {
        return this.validNamePointApostrophe;
    }

    public Atlas validNamePointApostropheLower()
    {
        return this.validNamePointApostropheLower;
    }

    public Atlas validNamePointApostropheAllCaps()
    {
        return this.validNamePointApostropheAllCaps;
    }

    public Atlas invalidNamePointApostrophe()
    {
        return this.invalidNamePointApostrophe;
    }

    public Atlas validNamePointAllCaps()
    {
        return this.validNamePointAllCaps;
    }

    public Atlas validNamePointNoCaps()
    {
        return this.validNamePointNoCaps;
    }

    public Atlas validNamePointLowerCaseWord()
    {
        return this.validNamePointLowerCaseWord;
    }

    public Atlas invalidNamePointEn()
    {
        return this.invalidNamePointEn;
    }

    public Atlas invalidNamePointGreek()
    {
        return this.invalidNamePointGreek;
    }

    public Atlas validNamePointGreek()
    {
        return this.validNamePointGreek;
    }

    public Atlas invalidNamePointGreekEl()
    {
        return this.invalidNamePointGreekEl;
    }

    public Atlas validNamePointGreekEl()
    {
        return this.validNamePointGreekEl;
    }

    public Atlas invalidNamePointChn()
    {
        return this.invalidNamePointChn;
    }

    public Atlas validNamePointChn()
    {
        return this.validNamePointChn;
    }
}
