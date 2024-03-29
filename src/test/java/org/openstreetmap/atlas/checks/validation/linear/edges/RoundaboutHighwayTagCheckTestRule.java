package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link RoundaboutHighwayTagCheckTest} data generator
 *
 * @author mselaineleong
 * @author brianjor
 */

public class RoundaboutHighwayTagCheckTestRule extends CoreTestRule
{
    // Roundabout Nodes
    private static final String TEST_1 = "48.1031871960688,-122.798829929851";
    private static final String TEST_2 = "48.1032655888312,-122.798728190788";
    private static final String TEST_3 = "48.103417147833,-122.798739929911";
    private static final String TEST_4 = "48.1034667963744,-122.798880799383";
    private static final String TEST_5 = "48.1034563440538,-122.7990451471";
    private static final String TEST_6 = "48.103357046903,-122.799170364408";
    private static final String TEST_7 = "48.1032211662805,-122.799123407918";
    private static final String TEST_8 = "48.103179356786,-122.799029494936";

    // Non-roundabout Nodes
    private static final String TEST_9 = "48.1028945286992,-122.798998190609";
    private static final String TEST_10 = "48.1033831777508,-122.798168625942";
    private static final String TEST_11 = "48.1037751388732,-122.799041234059";
    private static final String TEST_12 = "48.103226392465,-122.799600798906";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)), @Node(coordinates = @Loc(value = TEST_3)),
            @Node(coordinates = @Loc(value = TEST_4)), @Node(coordinates = @Loc(value = TEST_5)),
            @Node(coordinates = @Loc(value = TEST_6)), @Node(coordinates = @Loc(value = TEST_7)),
            @Node(coordinates = @Loc(value = TEST_8)), @Node(coordinates = @Loc(value = TEST_9)),
            @Node(coordinates = @Loc(value = TEST_10)), @Node(coordinates = @Loc(value = TEST_11)),
            @Node(coordinates = @Loc(value = TEST_12)) }, edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1240", coordinates = { @Loc(value = TEST_7),
                            @Loc(value = TEST_8) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1241", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1242", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_9) }, tags = { "highway=primary" }),
                    @Edge(id = "1243", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_10) }, tags = { "highway=primary" }),
                    @Edge(id = "1244", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_11) }, tags = { "highway=primary" }),
                    @Edge(id = "1245", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_12) }, tags = { "highway=primary" }) })
    private Atlas roundaboutCorrectTagPrimaryAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)), @Node(coordinates = @Loc(value = TEST_3)),
            @Node(coordinates = @Loc(value = TEST_4)), @Node(coordinates = @Loc(value = TEST_5)),
            @Node(coordinates = @Loc(value = TEST_6)), @Node(coordinates = @Loc(value = TEST_7)),
            @Node(coordinates = @Loc(value = TEST_8)), @Node(coordinates = @Loc(value = TEST_9)),
            @Node(coordinates = @Loc(value = TEST_10)), @Node(coordinates = @Loc(value = TEST_11)),
            @Node(coordinates = @Loc(value = TEST_12)) }, edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1240", coordinates = { @Loc(value = TEST_7),
                            @Loc(value = TEST_8) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1241", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout",
                                    "highway=primary" }),
                    @Edge(id = "1242", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_9) }, tags = { "highway=secondary" }),
                    @Edge(id = "1243", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_10) }, tags = { "highway=secondary" }),
                    @Edge(id = "1244", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_11) }, tags = { "highway=tertiary" }),
                    @Edge(id = "1245", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_12) }, tags = { "highway=tertiary" }) })
    private Atlas roundaboutCorrectTagAgainstManyAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)), @Node(coordinates = @Loc(value = TEST_3)),
            @Node(coordinates = @Loc(value = TEST_4)), @Node(coordinates = @Loc(value = TEST_5)),
            @Node(coordinates = @Loc(value = TEST_6)), @Node(coordinates = @Loc(value = TEST_7)),
            @Node(coordinates = @Loc(value = TEST_8)), @Node(coordinates = @Loc(value = TEST_9)),
            @Node(coordinates = @Loc(value = TEST_10)), @Node(coordinates = @Loc(value = TEST_11)),
            @Node(coordinates = @Loc(value = TEST_12)) }, edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                                    "highway=primary_link" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout",
                                    "highway=primary_link" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout",
                                    "highway=primary_link" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "junction=roundabout",
                                    "highway=primary_link" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6) }, tags = { "junction=roundabout",
                                    "highway=primary_link" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "junction=roundabout",
                                    "highway=primary_link" }),
                    @Edge(id = "1240", coordinates = { @Loc(value = TEST_7),
                            @Loc(value = TEST_8) }, tags = { "junction=roundabout",
                                    "highway=primary_link" }),
                    @Edge(id = "1241", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout",
                                    "highway=primary_link" }),
                    @Edge(id = "1242", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_9) }, tags = { "highway=primary" }),
                    @Edge(id = "1243", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_10) }, tags = { "highway=primary" }),
                    @Edge(id = "1244", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_11) }, tags = { "highway=primary" }),
                    @Edge(id = "1245", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_12) }, tags = { "highway=primary" }) })
    private Atlas roundaboutPrimaryLinkShouldBePrimaryAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)), @Node(coordinates = @Loc(value = TEST_3)),
            @Node(coordinates = @Loc(value = TEST_4)), @Node(coordinates = @Loc(value = TEST_5)),
            @Node(coordinates = @Loc(value = TEST_6)), @Node(coordinates = @Loc(value = TEST_7)),
            @Node(coordinates = @Loc(value = TEST_8)), @Node(coordinates = @Loc(value = TEST_9)),
            @Node(coordinates = @Loc(value = TEST_10)), @Node(coordinates = @Loc(value = TEST_11)),
            @Node(coordinates = @Loc(value = TEST_12)) }, edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1240", coordinates = { @Loc(value = TEST_7),
                            @Loc(value = TEST_8) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1241", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1242", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_9) }, tags = { "highway=secondary" }),
                    @Edge(id = "1243", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_10) }, tags = { "highway=secondary" }),
                    @Edge(id = "1244", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_11) }, tags = { "highway=primary" }) })
    private Atlas roundaboutCorrectTagNoHigherThroughRoadsAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)), @Node(coordinates = @Loc(value = TEST_3)),
            @Node(coordinates = @Loc(value = TEST_4)), @Node(coordinates = @Loc(value = TEST_5)),
            @Node(coordinates = @Loc(value = TEST_6)), @Node(coordinates = @Loc(value = TEST_7)),
            @Node(coordinates = @Loc(value = TEST_8)), @Node(coordinates = @Loc(value = TEST_9)),
            @Node(coordinates = @Loc(value = TEST_10)), @Node(coordinates = @Loc(value = TEST_11)),
            @Node(coordinates = @Loc(value = TEST_12)) }, edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1240", coordinates = { @Loc(value = TEST_7),
                            @Loc(value = TEST_8) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1241", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1242", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_9) }, tags = { "highway=secondary" }),
                    @Edge(id = "1243", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_10) }, tags = { "highway=secondary" }),
                    @Edge(id = "1244", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_11) }, tags = { "highway=primary_link" }),
                    @Edge(id = "1245", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_12) }, tags = { "highway=primary_link" }) })
    private Atlas roundaboutCorrectTagIgnoreLinksAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)), @Node(coordinates = @Loc(value = TEST_3)),
            @Node(coordinates = @Loc(value = TEST_4)), @Node(coordinates = @Loc(value = TEST_5)),
            @Node(coordinates = @Loc(value = TEST_6)), @Node(coordinates = @Loc(value = TEST_7)),
            @Node(coordinates = @Loc(value = TEST_8)), @Node(coordinates = @Loc(value = TEST_9)),
            @Node(coordinates = @Loc(value = TEST_10)),
            @Node(coordinates = @Loc(value = TEST_11)) }, edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1240", coordinates = { @Loc(value = TEST_7),
                            @Loc(value = TEST_8) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1241", coordinates = { @Loc(value = TEST_8),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout",
                                    "highway=secondary" }),
                    @Edge(id = "1242", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_9) }, tags = { "highway=primary", "name=split road",
                                    "oneway=yes" }),
                    @Edge(id = "1243", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_9) }, tags = { "highway=primary", "name=split road",
                                    "oneway=yes" }),
                    @Edge(id = "1245", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_10) }, tags = { "highway=secondary" }),
                    @Edge(id = "1246", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_11) }, tags = { "highway=secondary" }) })
    private Atlas roundaboutCorrectTagConnectedToSplitStreetAtlas;

    public Atlas roundaboutCorrectTagAgainstManyAtlas()
    {
        return this.roundaboutCorrectTagAgainstManyAtlas;
    }

    public Atlas roundaboutCorrectTagConnectedToSplitStreetAtlas()
    {
        return this.roundaboutCorrectTagConnectedToSplitStreetAtlas;
    }

    public Atlas roundaboutCorrectTagIgnoreLinksAtlas()
    {
        return this.roundaboutCorrectTagIgnoreLinksAtlas;
    }

    public Atlas roundaboutCorrectTagNoHigherThroughRoadsAtlas()
    {
        return this.roundaboutCorrectTagNoHigherThroughRoadsAtlas;
    }

    public Atlas roundaboutCorrectTagPrimaryAtlas()
    {
        return this.roundaboutCorrectTagPrimaryAtlas;
    }

    public Atlas roundaboutPrimaryLinkShouldBePrimaryAtlas()
    {
        return this.roundaboutPrimaryLinkShouldBePrimaryAtlas;
    }
}
