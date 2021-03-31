package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Tests for {@link HighwayMissingNameAndRefTagCheck}
 *
 * @author v-garei
 */
public class HighwayMissingNameOrRefTagCheckTestRule extends CoreTestRule
{

    private static final String WAY1_NODE1 = "40.9130354, 29.4700719";
    private static final String WAY1_NODE2 = "40.9123887, 29.4698597";
    private static final String WAY2_NODE2 = "40.9118904, 29.4696993";
    private static final String WAY3_NODE2 = "40.9082867, 29.4685152";

    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY1_NODE2)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY2_NODE2)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY3_NODE2)) }, edges = {
                    @TestAtlas.Edge(id = "3000001", coordinates = {
                            @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "highway=motorway",
                                    "name=highwayname" }),
                    @TestAtlas.Edge(id = "4000001", coordinates = {
                            @TestAtlas.Loc(value = WAY1_NODE2),
                            @TestAtlas.Loc(value = WAY2_NODE2) }, tags = { "highway=motorway" }),
                    @TestAtlas.Edge(id = "5000001", coordinates = {
                            @TestAtlas.Loc(value = WAY2_NODE2),
                            @TestAtlas.Loc(value = WAY3_NODE2) }, tags = { "highway=motorway",
                                    "name=highwayname" }) })
    private Atlas hasInconsistentTagTruePositive;

    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY1_NODE2)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY2_NODE2)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY3_NODE2)) }, edges = {
                    @TestAtlas.Edge(id = "3000001", coordinates = {
                            @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "highway=motorway",
                                    "name=highwayname" }),
                    @TestAtlas.Edge(id = "4000001", coordinates = {
                            @TestAtlas.Loc(value = WAY1_NODE2),
                            @TestAtlas.Loc(value = WAY2_NODE2) }, tags = { "highway=motorway",
                                    "name=highwayname" }),
                    @TestAtlas.Edge(id = "5000001", coordinates = {
                            @TestAtlas.Loc(value = WAY2_NODE2),
                            @TestAtlas.Loc(value = WAY3_NODE2) }, tags = { "highway=motorway",
                                    "name=highwayname" }) })
    private Atlas hasNameTagFalsePositive;

    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY1_NODE2)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY2_NODE2)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY3_NODE2)) }, edges = {
                    @TestAtlas.Edge(id = "3000001", coordinates = {
                            @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "highway=motorway",
                                    "ref=highwayref" }),
                    @TestAtlas.Edge(id = "4000001", coordinates = {
                            @TestAtlas.Loc(value = WAY1_NODE2),
                            @TestAtlas.Loc(value = WAY2_NODE2) }, tags = { "highway=motorway",
                                    "ref=highwayref" }),
                    @TestAtlas.Edge(id = "5000001", coordinates = {
                            @TestAtlas.Loc(value = WAY2_NODE2),
                            @TestAtlas.Loc(value = WAY3_NODE2) }, tags = { "highway=motorway",
                                    "ref=highwayref" }) })
    private Atlas hasRefTagFalsePositive;

    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY1_NODE2)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY2_NODE2)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY3_NODE2)) }, edges = {
                    @TestAtlas.Edge(id = "3000001", coordinates = {
                            @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "highway=motorway" }),
                    @TestAtlas.Edge(id = "4000001", coordinates = {
                            @TestAtlas.Loc(value = WAY1_NODE2),
                            @TestAtlas.Loc(value = WAY2_NODE2) }, tags = "highway=motorway"),
                    @TestAtlas.Edge(id = "5000001", coordinates = {
                            @TestAtlas.Loc(value = WAY2_NODE2),
                            @TestAtlas.Loc(value = WAY3_NODE2) }, tags = { "highway=motorway",
                                    "toll=yes" }) })
    private Atlas missingNameAndRefTag;

    public Atlas hasInconsistentTagTruePositive()
    {
        return this.hasInconsistentTagTruePositive;
    }

    public Atlas hasNameTagFalsePositive()
    {
        return this.hasNameTagFalsePositive;
    }

    public Atlas hasRefTagFalsePositive()
    {
        return this.hasRefTagFalsePositive;
    }

    public Atlas missingNameAndRefTag()
    {
        return this.missingNameAndRefTag;
    }
}
