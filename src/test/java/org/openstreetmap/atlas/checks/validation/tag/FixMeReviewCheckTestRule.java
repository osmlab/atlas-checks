package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Tests for {@link FixMeReviewCheck}
 *
 * @author v-garei
 */
public class FixMeReviewCheckTestRule extends CoreTestRule
{

    private static final String node1 = "40.9108780, 29.4695635";

    private static final String WAY1_NODE1 = "40.9130354, 29.4700719";
    private static final String WAY1_NODE2 = "40.9123887, 29.4698597";
    private static final String WAY2_NODE2 = "40.9118904, 29.4696993";
    private static final String WAY3_NODE2 = "40.9082867, 29.4685152";

    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = node1), tags = {
            "fixme=continue", "place=bees" }) })

    private Atlas nodeWithValidFixMe;

    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY1_NODE2)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY2_NODE2)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = WAY3_NODE2)) }, edges = {
                    @TestAtlas.Edge(id = "3000001", coordinates = {
                            @TestAtlas.Loc(value = WAY1_NODE1),
                            @TestAtlas.Loc(value = WAY1_NODE2) }, tags = { "FIXME=name",
                                    "highway=motorway", "toll=yes" }),
                    @TestAtlas.Edge(id = "4000001", coordinates = {
                            @TestAtlas.Loc(value = WAY1_NODE2),
                            @TestAtlas.Loc(value = WAY2_NODE2) }, tags = "highway=motorway"),
                    @TestAtlas.Edge(id = "5000001", coordinates = {
                            @TestAtlas.Loc(value = WAY2_NODE2),
                            @TestAtlas.Loc(value = WAY3_NODE2) }, tags = { "highway=motorway",
                                    "toll=yes" }) })
    private Atlas wayWithValidFixMe;

    public Atlas nodeWithValidFixMe()
    {
        return this.nodeWithValidFixMe;
    }

    public Atlas wayWithValidFixMe()
    {
        return this.wayWithValidFixMe;
    }
}
