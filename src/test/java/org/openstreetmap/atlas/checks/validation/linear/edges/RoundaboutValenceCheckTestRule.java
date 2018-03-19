package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link RoundaboutValenceCheckTest} data generator
 *
 * @author savannahostrowski
 */

public class RoundaboutValenceCheckTestRule extends CoreTestRule
{
    // Roundabout edges
    private static final String TEST_1 = "37.3314171,-122.0304871";
    private static final String TEST_2 = "37.32544,-122.033948";
    private static final String TEST_3 = "37.33531,-122.009566";
    private static final String TEST_4 = "37.390535,-122.031007";

    // Non-roundabout edges
    private static final String TEST_5 = "37.331460, -122.032579";
    private static final String TEST_6 = "37.322020, -122.038963";
    private static final String TEST_7 = "37.344847, -121.996437";
    private static final String TEST_8 = "37.410674, -122.020192";
    private static final String TEST_9 = "37.410261, -122.021378";
    private static final String TEST_10 = "37.409476, -122.029575";
    private static final String TEST_11 = "37.406918, -122.034210";
    private static final String TEST_12 = "37.405281, -122.034768";

    // Roundabout that cannot be exited (valence of 0)
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout" }) })
    private Atlas roundaboutWithValenceZero;

    // Roundabout with valence of 1 (should not be labelled as roundabout but as turning loop)
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)) },
            // edges
            edges = {
                    @Edge(id = "2345", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout" }),
                    @Edge(id = "2346", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout" }),
                    @Edge(id = "2347", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout" }),
                    @Edge(id = "2348", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout" }),
                    @Edge(id = "2349", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }) })
    private Atlas roundaboutWithValenceOne;

    // Roundabout with valence of 2 (this is okay). On conditional threshold
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = {
                    @Edge(id = "3456", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout" }),
                    @Edge(id = "3457", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout" }),
                    @Edge(id = "3458", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout" }),
                    @Edge(id = "3459", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout" }),
                    @Edge(id = "3460", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "3461", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }) })
    private Atlas roundaboutWithValenceTwo;

    // Roundabout with valence of 4 (this is okay)
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)),
                    @Node(coordinates = @Loc(value = TEST_8)) },
            // edges
            edges = {
                    @Edge(id = "4567", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout" }),
                    @Edge(id = "4568", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout" }),
                    @Edge(id = "4569", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout" }),
                    @Edge(id = "4570", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout" }),
                    @Edge(id = "4571", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "4572", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }),
                    @Edge(id = "4573", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }),
                    @Edge(id = "4574", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_8) }, tags = { "highway=motorway" }) })
    private Atlas roundaboutWithValenceFour;

    // Roundabout with valence of 10 (should be flagged for inspection). On conditional threshold
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)),
                    @Node(coordinates = @Loc(value = TEST_8)) },
            // edges
            edges = {
                    @Edge(id = "1111", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1112", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1113", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1114", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1115", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1116", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }),
                    @Edge(id = "1117", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }),
                    @Edge(id = "1118", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_8) }, tags = { "highway=motorway" }),
                    @Edge(id = "1119", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_8) }, tags = { "highway=motorway" }),
                    @Edge(id = "1120", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1121", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }),
                    @Edge(id = "1122", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }),
                    @Edge(id = "1123", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1124", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }) })
    private Atlas roundaboutWithValenceTen;

    // Roundabout with valence of 11 (should be flagged for inspection). Above conditional threshold
    // of 10.
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)),
                    @Node(coordinates = @Loc(value = TEST_8)) },
            // edges
            edges = {
                    @Edge(id = "1000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1003", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1004", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1005", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }),
                    @Edge(id = "1006", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }),
                    @Edge(id = "1007", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_8) }, tags = { "highway=motorway" }),
                    @Edge(id = "1008", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_8) }, tags = { "highway=motorway" }),
                    @Edge(id = "1009", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1010", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }),
                    @Edge(id = "1011", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }),
                    @Edge(id = "1012", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1013", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }),
                    @Edge(id = "1014", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas roundaboutWithValenceEleven;

    // Roundabout with valence of 14 (should be flagged for inspection).
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)),
                    @Node(coordinates = @Loc(value = TEST_8)),
                    @Node(coordinates = @Loc(value = TEST_9)),
                    @Node(coordinates = @Loc(value = TEST_10)),
                    @Node(coordinates = @Loc(value = TEST_11)),
                    @Node(coordinates = @Loc(value = TEST_12)) },
            // edges
            edges = {
                    @Edge(id = "1000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1003", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1004", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1005", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }),
                    @Edge(id = "1006", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }),
                    @Edge(id = "1007", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_8) }, tags = { "highway=motorway" }),
                    @Edge(id = "1008", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_8) }, tags = { "highway=motorway" }),
                    @Edge(id = "1009", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1010", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }),
                    @Edge(id = "1011", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }),
                    @Edge(id = "1012", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1013", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }),
                    @Edge(id = "1014", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_9) }, tags = { "highway=motorway" }),
                    @Edge(id = "1015", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_10) }, tags = { "highway=motorway" }),
                    @Edge(id = "1016", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_11) }, tags = { "highway=motorway" }),
                    @Edge(id = "1017", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_12) }, tags = { "highway=motorway" }) })
    private Atlas roundaboutWithValenceFourteen;

    // Roundabout with valence of 15 (should be flagged for inspection). Above conditional threshold
    // of 14.
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)),
                    @Node(coordinates = @Loc(value = TEST_8)),
                    @Node(coordinates = @Loc(value = TEST_9)),
                    @Node(coordinates = @Loc(value = TEST_10)),
                    @Node(coordinates = @Loc(value = TEST_11)),
                    @Node(coordinates = @Loc(value = TEST_12)) },
            // edges
            edges = {
                    @Edge(id = "1000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1003", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_1) }, tags = { "junction=roundabout" }),
                    @Edge(id = "1004", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1005", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }),
                    @Edge(id = "1006", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }),
                    @Edge(id = "1007", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_8) }, tags = { "highway=motorway" }),
                    @Edge(id = "1008", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_8) }, tags = { "highway=motorway" }),
                    @Edge(id = "1009", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1010", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }),
                    @Edge(id = "1011", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }),
                    @Edge(id = "1012", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Edge(id = "1013", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_6) }, tags = { "highway=motorway" }),
                    @Edge(id = "1014", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_9) }, tags = { "highway=motorway" }),
                    @Edge(id = "1015", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_10) }, tags = { "highway=motorway" }),
                    @Edge(id = "1016", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_11) }, tags = { "highway=motorway" }),
                    @Edge(id = "1017", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_12) }, tags = { "highway=motorway" }),
                    @Edge(id = "1018", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_11) }, tags = { "highway=motorway" }), })
    private Atlas roundaboutWithValenceFifteen;

    public Atlas roundaboutWithValenceZero()
    {
        return this.roundaboutWithValenceZero;
    }

    public Atlas roundaboutWithValenceOne()
    {
        return this.roundaboutWithValenceOne;
    }

    public Atlas roundaboutWithValenceTwo()
    {
        return this.roundaboutWithValenceTwo;
    }

    public Atlas roundaboutWithValenceFour()
    {
        return this.roundaboutWithValenceFour;
    }

    public Atlas roundaboutWithValenceTen()
    {
        return this.roundaboutWithValenceTen;
    }

    public Atlas roundaboutWithValenceEleven()
    {
        return this.roundaboutWithValenceEleven;
    }

    public Atlas roundaboutWithValenceFourteen()
    {
        return this.roundaboutWithValenceFourteen;
    }

    public Atlas roundaboutWithValenceFifteen()
    {
        return roundaboutWithValenceFifteen;
    }
}
