package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link RoadNameSpellingConsistencyCheck} test atlas
 *
 * @author seancoulter
 */
public class RoadNameSpellingConsistencyCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "47.2136626201459,-122.443275382856";
    private static final String TEST_2 = "47.2138327316739,-122.44258668766";
    private static final String TEST_3 = "47.2136626201459,-122.441897992465";
    private static final String TEST_4 = "47.2138114677627,-122.440990166979";
    private static final String TEST_5 = "47.2136200921786,-122.44001973284";
    private static final String TEST_6 = "47.2135137721113,-122.439127559518";
    private static final String TEST_7 = "47.2136200921786,-122.438157125378";
    private static final String TEST_8 = "47.2136413561665,-122.437468430183";
    private static final String TEST_9 = "47.2137689399148,-122.436717126333";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), id = "0"),
                    @Node(coordinates = @Loc(value = TEST_2), id = "1"),
                    @Node(coordinates = @Loc(value = TEST_3), id = "2"),
                    @Node(coordinates = @Loc(value = TEST_4), id = "3"),
                    @Node(coordinates = @Loc(value = TEST_5), id = "4"),
                    @Node(coordinates = @Loc(value = TEST_6), id = "5"),
                    @Node(coordinates = @Loc(value = TEST_7), id = "6"),
                    @Node(coordinates = @Loc(value = TEST_8), id = "7"),
                    @Node(coordinates = @Loc(value = TEST_9), id = "8") },
            // edges
            edges = {
                    @Edge(id = "1000000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=motorway", "name=aroad" }),
                    @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "name=aroad" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "highway=motorway", "name=aroåd" }),
                    @Edge(id = "1000000003", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "name=aroad" }) })
    private Atlas oneSegmentInconsistentSpelling;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), id = "0"),
                    @Node(coordinates = @Loc(value = TEST_2), id = "1"),
                    @Node(coordinates = @Loc(value = TEST_3), id = "2"),
                    @Node(coordinates = @Loc(value = TEST_4), id = "3"),
                    @Node(coordinates = @Loc(value = TEST_5), id = "4"),
                    @Node(coordinates = @Loc(value = TEST_6), id = "5"),
                    @Node(coordinates = @Loc(value = TEST_7), id = "6"),
                    @Node(coordinates = @Loc(value = TEST_8), id = "7"),
                    @Node(coordinates = @Loc(value = TEST_9), id = "8") },
            // edges
            edges = {
                    @Edge(id = "1000000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=motorway", "name=aroad" }),
                    @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "name=Åroad" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "highway=motorway", "name=aroåd" }),
                    @Edge(id = "1000000003", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "name=aroad" }) })
    private Atlas moreThanOneSegmentInconsistentSpelling;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), id = "0"),
                    @Node(coordinates = @Loc(value = TEST_2), id = "1"),
                    @Node(coordinates = @Loc(value = TEST_3), id = "2"),
                    @Node(coordinates = @Loc(value = TEST_4), id = "3"),
                    @Node(coordinates = @Loc(value = TEST_5), id = "4"),
                    @Node(coordinates = @Loc(value = TEST_6), id = "5"),
                    @Node(coordinates = @Loc(value = TEST_7), id = "6"),
                    @Node(coordinates = @Loc(value = TEST_8), id = "7"),
                    @Node(coordinates = @Loc(value = TEST_9), id = "8") },
            // edges
            edges = {
                    @Edge(id = "1000000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=motorway", "name=aroad" }),
                    @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "name=aroad" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "highway=motorway", "name=aroad" }),
                    @Edge(id = "1000000003", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "name=aroad" }) })
    private Atlas noSegmentsInconsistentSpelling;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), id = "0"),
                    @Node(coordinates = @Loc(value = TEST_2), id = "1"),
                    @Node(coordinates = @Loc(value = TEST_3), id = "2"),
                    @Node(coordinates = @Loc(value = TEST_4), id = "3"),
                    @Node(coordinates = @Loc(value = TEST_5), id = "4"),
                    @Node(coordinates = @Loc(value = TEST_6), id = "5"),
                    @Node(coordinates = @Loc(value = TEST_7), id = "6"),
                    @Node(coordinates = @Loc(value = TEST_8), id = "7"),
                    @Node(coordinates = @Loc(value = TEST_9), id = "8") },
            // edges
            edges = {
                    @Edge(id = "1000000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=motorway", "name=aroad" }),
                    @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "name=aroad" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "highway=motorway", "name=aroad" }),
                    @Edge(id = "1000000003", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "name=arøad" }) })
    private Atlas oneSegmentInconsistentSpellingAccent;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), id = "0"),
                    @Node(coordinates = @Loc(value = TEST_2), id = "1"),
                    @Node(coordinates = @Loc(value = TEST_3), id = "2"),
                    @Node(coordinates = @Loc(value = TEST_4), id = "3"),
                    @Node(coordinates = @Loc(value = TEST_5), id = "4"),
                    @Node(coordinates = @Loc(value = TEST_6), id = "5"),
                    @Node(coordinates = @Loc(value = TEST_7), id = "6"),
                    @Node(coordinates = @Loc(value = TEST_8), id = "7"),
                    @Node(coordinates = @Loc(value = TEST_9), id = "8") },
            // edges
            edges = {
                    @Edge(id = "1000000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=motorway", "name=aroad" }),
                    @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=motorway", "name=åroad" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "highway=motorway", "name=aroAd" }),
                    @Edge(id = "1000000003", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway", "name=aroa∂" }) })
    private Atlas allSegmentsInconsistentSpelling;

    public Atlas getAllSegmentsInconsistentSpelling()
    {
        return this.allSegmentsInconsistentSpelling;
    }

    public Atlas getMoreThanOneSegmentInconsistentSpelling()
    {
        return this.moreThanOneSegmentInconsistentSpelling;
    }

    public Atlas getNoSegmentsInconsistentSpelling()
    {
        return this.noSegmentsInconsistentSpelling;
    }

    public Atlas getOneSegmentInconsistentSpelling()
    {
        return this.oneSegmentInconsistentSpelling;
    }

    public Atlas getOneSegmentInconsistentSpellingAccent()
    {
        return this.oneSegmentInconsistentSpellingAccent;
    }

}
