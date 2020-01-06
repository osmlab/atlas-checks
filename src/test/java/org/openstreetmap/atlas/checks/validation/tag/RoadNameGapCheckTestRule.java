package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link RoadNameGapCheck}
 *
 * @author sugandhimaheshwaram
 */
public class RoadNameGapCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "47.2136626201459,-122.443275382856";
    private static final String TEST_2 = "47.2138327316739,-122.44258668766";
    private static final String TEST_3 = "47.2136626201459,-122.441897992465";
    private static final String TEST_4 = "47.2138114677627,-122.440990166979";
    private static final String TEST_5 = "9.8347979,56.024421";
    private static final String TEST_6 = "9.8346593,56.0243953";
    private static final String TEST_7 = "9.8344387,56.0243522";
    private static final String TEST_8 = "9.8343536,56.0243326";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @TestAtlas.Loc(value = TEST_1), id = "0"),
                    @Node(coordinates = @TestAtlas.Loc(value = TEST_2), id = "1"),
                    @Node(coordinates = @TestAtlas.Loc(value = TEST_3), id = "2"),
                    @Node(coordinates = @TestAtlas.Loc(value = TEST_4), id = "3") },
            // edges
            edges = {
                    @Edge(id = "1001000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=motorway", "name=aroad" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=MOTORWAY",
                                    "name=Tsing Long Highway" }),
                    @Edge(id = "1003000002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "highway=SECONDARY_LINK",
                                    "name=Tsing Long Highway" }) })
    private Atlas invalidHighWayTag;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), id = "0"),
                    @Node(coordinates = @Loc(value = TEST_2), id = "1"),
                    @Node(coordinates = @Loc(value = TEST_3), id = "2"),
                    @Node(coordinates = @Loc(value = TEST_4), id = "3") },
            // edges
            edges = {
                    @Edge(id = "1001000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=PRIMARY",
                                    "name=Tsing Long Highway" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=MOTORWAY", "name=failingName",
                                    "junction=roundabout" }),
                    @Edge(id = "1003000002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "highway=SECONDARY",
                                    "name=Tsing Long Highway" }) })
    private Atlas junctionNotRoundAbout;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), id = "0"),
                    @Node(coordinates = @Loc(value = TEST_2), id = "1"),
                    @Node(coordinates = @Loc(value = TEST_3), id = "2"),
                    @Node(coordinates = @Loc(value = TEST_4), id = "3"),
                    @Node(coordinates = @Loc(value = TEST_5), id = "4"),
                    @Node(coordinates = @Loc(value = TEST_6), id = "5"),
                    @Node(coordinates = @Loc(value = TEST_7), id = "6"),
                    @Node(coordinates = @Loc(value = TEST_8), id = "7") },
            // edges
            edges = {
                    @Edge(id = "1001000000", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6) }, tags = { "highway=PRIMARY",
                                    "name=Tsing Long Highway" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=PRIMARY",
                                    "name=failingName" }),
                    @Edge(id = "1003000002", coordinates = { @Loc(value = TEST_7),
                            @Loc(value = TEST_8) }, tags = { "highway=PRIMARY",
                                    "name=Tsing Long Highway" }) })
    private Atlas edgeWithDifferentNameTag;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), id = "0"),
                    @Node(coordinates = @Loc(value = TEST_2), id = "1"),
                    @Node(coordinates = @Loc(value = TEST_3), id = "2"),
                    @Node(coordinates = @Loc(value = TEST_4), id = "3") },
            // edges
            edges = {
                    @Edge(id = "1000000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=PRIMARY",
                                    "name=Tsing Long Highway" }),
                    @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=MOTORWAY",
                                    "name=failingName" }),
                    @Edge(id = "1000000002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "highway=SECONDARY",
                                    "name=Tsing Long Highway" }) })
    private Atlas edgeWithDifferentNameTagButSameOsmId;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), id = "0"),
                    @Node(coordinates = @Loc(value = TEST_2), id = "1"),
                    @Node(coordinates = @Loc(value = TEST_3), id = "2"),
                    @Node(coordinates = @Loc(value = TEST_4), id = "3"),
                    @Node(coordinates = @Loc(value = TEST_5), id = "4"),
                    @Node(coordinates = @Loc(value = TEST_6), id = "5"),
                    @Node(coordinates = @Loc(value = TEST_7), id = "6"),
                    @Node(coordinates = @Loc(value = TEST_8), id = "7") },
            // edges
            edges = {
                    @Edge(id = "1001000000", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6) }, tags = { "highway=PRIMARY",
                                    "name=Tsing Long Highway" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=MOTORWAY" }),
                    @Edge(id = "1003000002", coordinates = { @Loc(value = TEST_7),
                            @Loc(value = TEST_8) }, tags = { "highway=SECONDARY",
                                    "name=Tsing Long Highway" }) })
    private Atlas edgeWithNoNameTag;

    public Atlas getEdgeWithDifferentNameTag()
    {
        return this.edgeWithDifferentNameTag;
    }

    public Atlas getEdgeWithDifferentNameTagButSameOsmId()
    {
        return this.edgeWithDifferentNameTagButSameOsmId;
    }

    public Atlas getEdgeWithNoNameTag()
    {
        return this.edgeWithNoNameTag;
    }

    public Atlas isInvalidHighWayTag()
    {
        return this.invalidHighWayTag;
    }

    public Atlas isJunctionNotRoundAbout()
    {
        return this.junctionNotRoundAbout;
    }

}
