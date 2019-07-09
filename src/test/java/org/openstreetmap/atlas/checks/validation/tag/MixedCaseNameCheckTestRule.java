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
    private Atlas invalidNamePointAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=tEst TeSt" }) })
    private Atlas invalidNameNodeAtlas;

    @TestAtlas(
            // Lines
            lines = { @Line(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                    @Loc(value = TEST_3) }, tags = { "iso_country_code=USA", "name=tEst TeSt" }) })
    private Atlas invalidNameLineAtlas;

    @TestAtlas(
            // Areas
            areas = { @Area(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                    @Loc(value = TEST_3) }, tags = { "iso_country_code=USA", "name=tEst TeSt" }) })
    private Atlas invalidNameAreaAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_3)) },
            // edges
            edges = { @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                    @Loc(value = TEST_3) }, tags = { "iso_country_code=USA", "name=tEst TeSt" }) })
    private Atlas invalidNameEdgeAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=tEst" }) })
    private Atlas invalidNamePointOneWordAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=Test-Test" }) })
    private Atlas validNamePointHyphenAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=2test Test" }) })
    private Atlas validNamePointNumberAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=Test-TesT" }) })
    private Atlas invalidNamePointHyphenAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=McMan" }) })
    private Atlas validNamePointAffixAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=McMaN" }) })
    private Atlas invalidNamePointAffixAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=O'Flanagan" }) })
    private Atlas validNamePointApostropheAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=Scott's" }) })
    private Atlas validNamePointApostropheLowerAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=SCOTT'S" }) })
    private Atlas validNamePointApostropheAllCapsAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=SCOTT's" }) })
    private Atlas validNamePointCapsApostropheAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=SCOTTs'" }) })
    private Atlas validNamePointCapsLowerApostropheAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=Scott'S" }) })
    private Atlas invalidNamePointApostropheAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=TEST" }) })
    private Atlas validNamePointAllCapsAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=test" }) })
    private Atlas validNamePointNoCapsAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=Test of Test" }) })
    private Atlas validNamePointLowerCasePrepositionAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=Test a Test" }) })
    private Atlas validNamePointLowerCaseArticleAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=A Test" }) })
    private Atlas validNamePointLowerCaseArticleStartAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=a Test" }) })
    private Atlas invalidNamePointLowerCaseArticleStartAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name=Test (20kV) Test" }) })
    private Atlas validNamePointMixedCaseUnitAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=USA",
                    "name:en=tEst TeSt" }) })
    private Atlas invalidNamePointEnAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=GRC",
                    "name=τΕστ ΤεΣτ" }) })
    private Atlas invalidNamePointGreekAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=GRC",
                    "name=Τεστ Τεστ" }) })
    private Atlas validNamePointGreekAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=GRC",
                    "name:el=τΕστ ΤεΣτ" }) })
    private Atlas invalidNamePointGreekElAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=GRC",
                    "name:el=Τεστ Τεστ" }) })
    private Atlas validNamePointGreekElAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=CHN",
                    "name:en=tEst TeSt", "name=Test of Test" }) })
    private Atlas invalidNamePointChnAtlas;

    @TestAtlas(
            // points
            points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "iso_country_code=CHN",
                    "name:en=Test of Test", "name=tEst TeSt" }) })
    private Atlas validNamePointChnAtlas;

    public Atlas invalidNameAreaAtlas()
    {
        return this.invalidNameAreaAtlas;
    }

    public Atlas invalidNameEdgeAtlas()
    {
        return this.invalidNameEdgeAtlas;
    }

    public Atlas invalidNameLineAtlas()
    {
        return this.invalidNameLineAtlas;
    }

    public Atlas invalidNameNodeAtlas()
    {
        return this.invalidNameNodeAtlas;
    }

    public Atlas invalidNamePointAffixAtlas()
    {
        return this.invalidNamePointAffixAtlas;
    }

    public Atlas invalidNamePointApostropheAtlas()
    {
        return this.invalidNamePointApostropheAtlas;
    }

    public Atlas invalidNamePointAtlas()
    {
        return this.invalidNamePointAtlas;
    }

    public Atlas invalidNamePointChnAtlas()
    {
        return this.invalidNamePointChnAtlas;
    }

    public Atlas invalidNamePointEnAtlas()
    {
        return this.invalidNamePointEnAtlas;
    }

    public Atlas invalidNamePointGreekAtlas()
    {
        return this.invalidNamePointGreekAtlas;
    }

    public Atlas invalidNamePointGreekElAtlas()
    {
        return this.invalidNamePointGreekElAtlas;
    }

    public Atlas invalidNamePointHyphenAtlas()
    {
        return this.invalidNamePointHyphenAtlas;
    }

    public Atlas invalidNamePointLowerCaseArticleStartAtlas()
    {
        return this.invalidNamePointLowerCaseArticleStartAtlas;
    }

    public Atlas invalidNamePointOneWordAtlas()
    {
        return this.invalidNamePointOneWordAtlas;
    }

    public Atlas validNamePointAffixAtlas()
    {
        return this.validNamePointAffixAtlas;
    }

    public Atlas validNamePointAllCapsAtlas()
    {
        return this.validNamePointAllCapsAtlas;
    }

    public Atlas validNamePointApostropheAllCapsAtlas()
    {
        return this.validNamePointApostropheAllCapsAtlas;
    }

    public Atlas validNamePointApostropheAtlas()
    {
        return this.validNamePointApostropheAtlas;
    }

    public Atlas validNamePointApostropheLowerAtlas()
    {
        return this.validNamePointApostropheLowerAtlas;
    }

    public Atlas validNamePointCapsApostropheAtlas()
    {
        return this.validNamePointCapsApostropheAtlas;
    }

    public Atlas validNamePointCapsLowerApostropheAtlas()
    {
        return this.validNamePointCapsLowerApostropheAtlas;
    }

    public Atlas validNamePointChnAtlas()
    {
        return this.validNamePointChnAtlas;
    }

    public Atlas validNamePointGreekAtlas()
    {
        return this.validNamePointGreekAtlas;
    }

    public Atlas validNamePointGreekElAtlas()
    {
        return this.validNamePointGreekElAtlas;
    }

    public Atlas validNamePointHyphenAtlas()
    {
        return this.validNamePointHyphenAtlas;
    }

    public Atlas validNamePointLowerCaseArticleAtlas()
    {
        return this.validNamePointLowerCaseArticleAtlas;
    }

    public Atlas validNamePointLowerCaseArticleStartAtlas()
    {
        return this.validNamePointLowerCaseArticleStartAtlas;
    }

    public Atlas validNamePointLowerCasePrepositionAtlas()
    {
        return this.validNamePointLowerCasePrepositionAtlas;
    }

    public Atlas validNamePointMixedCaseUnitAtlas()
    {
        return this.validNamePointMixedCaseUnitAtlas;
    }

    public Atlas validNamePointNoCapsAtlas()
    {
        return this.validNamePointNoCapsAtlas;
    }

    public Atlas validNamePointNumberAtlas()
    {
        return this.validNamePointNumberAtlas;
    }
}
