package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link UnknownHighwayTagCheck}
 *
 * @author v-garei
 */
public class UnknownHighwayTagCheckTestRule extends CoreTestRule
{

    private static final String WAY1_NODE1 = "40.9130354, 29.4700719";
    private static final String WAY1_NODE2 = "40.9123887, 29.4698597";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WAY1_NODE1)),
            @Node(coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "6000001", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = { "highway=trunk" }) })
    private Atlas falsePositiveKnownHighwayTagOnEdge;

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = WAY1_NODE1), tags = { "highway=bus_stop" }),
            @Node(coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "7000001", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) })
    private Atlas falsePositiveKnownHighwayTagOnNode;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WAY1_NODE1)),
            @Node(coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "8000001", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = { "highway=unknown" }) })
    private Atlas truePositiveUnknownHighwayTagOnEdge;

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = WAY1_NODE1), tags = { "highway=unknown" }),
            @Node(coordinates = @Loc(value = WAY1_NODE2)) }, edges = {
                    @Edge(id = "9000001", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }) })
    private Atlas truePositiveUnknownHighwayTagOnNode;

    public Atlas falsePositiveKnownHighwayTagOnEdge()
    {
        return this.falsePositiveKnownHighwayTagOnEdge;
    }

    public Atlas falsePositiveKnownHighwayTagOnNode()
    {
        return this.falsePositiveKnownHighwayTagOnNode;
    }

    public Atlas truePositiveUnknownHighwayTagOnEdge()
    {
        return this.truePositiveUnknownHighwayTagOnEdge;
    }

    public Atlas truePositiveUnknownHighwayTagOnNode()
    {
        return this.truePositiveUnknownHighwayTagOnNode;
    }
}
