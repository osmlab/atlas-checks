package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link SuddenHighwayTypeChangeCheck}
 *
 * @author v-garei
 */
public class SuddenHighwayTypeChangeCheckTestRule extends CoreTestRule
{

    private static final String WAY1_NODE1 = "47.6317191, -122.1961930";
    private static final String WAY1_WAY2_INTERSECTION = "47.6316431, -122.1962111";
    private static final String WAY2_NODE2 = "47.6315832, -122.1961171";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WAY1_NODE1)),
            @Node(coordinates = @Loc(value = WAY1_WAY2_INTERSECTION)),
            @Node(coordinates = @Loc(value = WAY2_NODE2)) }, edges = {
                    @Edge(id = "1000001", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_WAY2_INTERSECTION) }, tags = "highway=service"),
                    @Edge(id = "2000001", coordinates = { @Loc(value = WAY1_WAY2_INTERSECTION),
                            @Loc(value = WAY2_NODE2) }, tags = "highway=primary") })
    private Atlas truePositiveSuddenHighwayTypeChangeCheck;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WAY1_NODE1)),
            @Node(coordinates = @Loc(value = WAY1_WAY2_INTERSECTION)),
            @Node(coordinates = @Loc(value = WAY2_NODE2)) }, edges = {
                    @Edge(id = "3000001", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_WAY2_INTERSECTION) }, tags = "highway=tertiary"),
                    @Edge(id = "4000001", coordinates = { @Loc(value = WAY1_WAY2_INTERSECTION),
                            @Loc(value = WAY2_NODE2) }, tags = "highway=secondary") })
    private Atlas falsePositiveSuddenHighwayTypeChangeCheck;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WAY1_NODE1)),
            @Node(coordinates = @Loc(value = WAY1_WAY2_INTERSECTION)),
            @Node(coordinates = @Loc(value = WAY2_NODE2)) }, edges = {
            @Edge(id = "5000001", coordinates = { @Loc(value = WAY1_NODE1),
                    @Loc(value = WAY1_WAY2_INTERSECTION) }, tags = "highway=motorway"),
            @Edge(id = "6000001", coordinates = { @Loc(value = WAY1_WAY2_INTERSECTION),
                    @Loc(value = WAY2_NODE2) }, tags = "highway=tertiary") })
    private Atlas truePositiveCase1;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WAY1_NODE1)),
            @Node(coordinates = @Loc(value = WAY1_WAY2_INTERSECTION)),
            @Node(coordinates = @Loc(value = WAY2_NODE2)) }, edges = {
            @Edge(id = "7000001", coordinates = { @Loc(value = WAY1_NODE1),
                    @Loc(value = WAY1_WAY2_INTERSECTION) }, tags = "highway=primary_link"),
            @Edge(id = "8000001", coordinates = { @Loc(value = WAY1_WAY2_INTERSECTION),
                    @Loc(value = WAY2_NODE2) }, tags = "highway=residential") })
    private Atlas truePositiveCase2;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WAY1_NODE1)),
            @Node(coordinates = @Loc(value = WAY1_WAY2_INTERSECTION)),
            @Node(coordinates = @Loc(value = WAY2_NODE2)) }, edges = {
            @Edge(id = "9000001", coordinates = { @Loc(value = WAY1_NODE1),
                    @Loc(value = WAY1_WAY2_INTERSECTION) }, tags = "highway=tertiary"),
            @Edge(id = "10000001", coordinates = { @Loc(value = WAY1_WAY2_INTERSECTION),
                    @Loc(value = WAY2_NODE2) }, tags = "highway=service") })
    private Atlas truePositiveCase3;



    public Atlas falsePositiveSuddenHighwayTypeChangeCheck()
    {
        return this.falsePositiveSuddenHighwayTypeChangeCheck;
    }

    public Atlas truePositiveSuddenHighwayTypeChangeCheck()
    {
        return this.truePositiveSuddenHighwayTypeChangeCheck;
    }

    public Atlas truePositiveCase1() {return this.truePositiveCase1;}

    public Atlas truePositiveCase2() {return this.truePositiveCase2;}

    public Atlas truePositiveCase3() {return this.truePositiveCase3;}
}
