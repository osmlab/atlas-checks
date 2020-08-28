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
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Atlas Test Rule class for {@link GenericTagCheckTest}.
 *
 * @author Taylor Smock
 */
public class GenericTagCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "20.538246,10.546134";
    private static final String TEST_2 = "20.535768,10.543755";
    private static final String TEST_3 = "20.535773, 10.548353";

    @TestAtlas(nodes = @Node(id = "7893900752000000", tags = "cycleway=asl", coordinates = @Loc("14.5547361,121.0241506")))
    private Atlas badCycleTag;

    @TestAtlas(points = @Point(id = "1000000", tags = "natural"))
    private Atlas badTag;

    @TestAtlas(nodes = { @Node(id = "176217884", coordinates = @Loc("39.0653698,-108.5644657")),
            @Node(id = "176234889", coordinates = @Loc("39.0663279,-108.5644415")),
            @Node(id = "3201841673", coordinates = @Loc("39.0653712,-108.5645876")),
            @Node(id = "3203209393", coordinates = @Loc("39.06527,-108.5644812")),
            @Node(id = "3203209395", coordinates = @Loc("39.0654607,-108.5644191")),
            @Node(id = "5174849668", coordinates = @Loc("39.0653675,-108.563718")) }, edges = {
                    @Edge(id = "17000329", coordinates = { @Loc("39.0653698,-108.5644657"),
                            @Loc("39.0654607,-108.5644191"),
                            @Loc("39.0663279,-108.5644415") }, tags = { "highway=secondary",
                                    "lanes=2.1" }),
                    @Edge(id = "314272370", coordinates = { @Loc("39.06527,-108.5644812"),
                            @Loc("39.0653698,-108.5644657") }, tags = { "highway=trunk",
                                    "lanes=0.5" }),
                    @Edge(id = "533392552", coordinates = { @Loc("39.0653675,-108.563718"),
                            @Loc("39.0653698,-108.5644657") }, tags = { "highway=trunk",
                                    "lanes=4" }),
                    @Edge(id = "705816846", coordinates = { @Loc("39.0653698,-108.5644657"),
                            @Loc("39.0653712,-108.5645876") }, tags = { "highway=trunk",
                                    "lanes=2" }) })
    private Atlas lanes;

    @TestAtlas(points = @Point(id = "1000000", coordinates = @Loc(TEST_1), tags = "natural=coastline"), lines = {
            @Line(id = "6000000", coordinates = { @Loc(TEST_1),
                    @Loc(TEST_2) }, tags = "natural=coastline"),
            @Line(id = "6000001", coordinates = { @Loc(TEST_1), @Loc(TEST_2), @Loc(TEST_3),
                    @Loc(TEST_1) }, tags = "natural=coastline") }, areas = @Area(id = "7000000", coordinates = {
                            @Loc(TEST_1), @Loc(TEST_2),
                            @Loc(TEST_3) }, tags = "natural=coastline"), relations = {
                                    @Relation(id = "8000000", members = @Member(id = "1000000", role = "member", type = "point"), tags = "natural=coastline"),
                                    @Relation(id = "9000000", members = @Member(id = "6000001", role = "outer", type = "line"), tags = {
                                            "iso_country_code=GBR", "natural=coastline",
                                            "type=multipolygon" }) })
    private Atlas testAtlasCoastline;

    @TestAtlas(points = @Point(id = "1000000", tags = { "natural=tree", "iso_country_code=USA" }))
    private Atlas tree;

    @TestAtlas(points = { @Point(id = "1000000", coordinates = @Loc("0,0")),
            @Point(id = "2000000", coordinates = @Loc("1,1")),
            @Point(id = "3000000", coordinates = @Loc("-1,1")) }, lines = @Line(id = "4000000", tags = "natural=tree", coordinates = {
                    @Loc("0,0"), @Loc("1,1"), @Loc("-1,1"), @Loc("0,0") }))
    private Atlas treeClosedLine;

    @TestAtlas(points = { @Point(id = "1000000", coordinates = @Loc("0,0")),
            @Point(id = "2000000", coordinates = @Loc("1,1")),
            @Point(id = "3000000", coordinates = @Loc("-1,1")) }, lines = @Line(id = "4000000", tags = "natural=tree", coordinates = {
                    @Loc("0,0"), @Loc("1,1"), @Loc("-1,1") }))
    private Atlas treeOpenLine;

    /**
     * Get an atlas object with a false positive bad tag
     *
     * @return An object with a false positive bad tag ("cycleway=asl")
     */
    public Atlas getBadCycleTag()
    {
        return this.badCycleTag;
    }

    /**
     * Get an atlas object with a bad tag
     *
     * @return An object with a bad tag ("natural=")
     */
    public Atlas getBadTag()
    {
        return this.badTag;
    }

    /**
     * Get a variety of highways with lanes (lanes are 2.1, 0.5, 4, and 2)
     *
     * @return An atlas with lanes
     */
    public Atlas getLanes()
    {
        return this.lanes;
    }

    /**
     * An atlas with a single object, a tree
     *
     * @return A well-tree'ted tree
     */
    public Atlas getTree()
    {
        return this.tree;
    }

    /**
     * Get a natural=tree line (closed)
     *
     * @return A closed tree line
     */
    public Atlas getTreeClosedLine()
    {
        return this.treeClosedLine;
    }

    /**
     * Get a natural=tree line (open)
     *
     * @return An open tree line
     */
    public Atlas getTreeOpenLine()
    {
        return this.treeOpenLine;
    }

    /**
     * Get a coastline
     *
     * @return A coastline
     */
    public Atlas testAtlasCoastline()
    {
        return this.testAtlasCoastline;
    }
}
