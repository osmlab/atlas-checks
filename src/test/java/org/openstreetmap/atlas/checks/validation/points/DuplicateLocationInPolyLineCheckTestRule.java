package org.openstreetmap.atlas.checks.validation.points;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * A class holding test atlases for {@link DuplicateLocationInPolyLineCheckTest}
 *
 * @author Taylor Smock
 */
public class DuplicateLocationInPolyLineCheckTestRule extends CoreTestRule
{
    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(Location.TEST_6_COORDINATES)),
                    @Node(id = "2000000", coordinates = @Loc(Location.TEST_2_COORDINATES)),
                    @Node(id = "3000000", coordinates = @Loc(Location.TEST_1_COORDINATES)) }, edges = {
                            @Edge(id = "1000000", coordinates = { @Loc(Location.TEST_6_COORDINATES),
                                    @Loc(Location.TEST_2_COORDINATES),
                                    @Loc(Location.TEST_1_COORDINATES),
                                    @Loc(Location.TEST_6_COORDINATES) }) })
    private Atlas validSimpleEdge;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(Location.TEST_6_COORDINATES)),
                    @Node(id = "2000000", coordinates = @Loc(Location.TEST_2_COORDINATES)),
                    @Node(id = "3000000", coordinates = @Loc(Location.TEST_1_COORDINATES)),
                    @Node(id = "4000000", coordinates = @Loc(Location.TEST_3_COORDINATES)) }, edges = {
                            @Edge(id = "1000000", coordinates = { @Loc(Location.TEST_6_COORDINATES),
                                    @Loc(Location.TEST_2_COORDINATES),
                                    @Loc(Location.TEST_1_COORDINATES),
                                    @Loc(Location.TEST_3_COORDINATES),
                                    @Loc(Location.TEST_2_COORDINATES) }, tags = {
                                            "highway=service" }) })
    private Atlas validSimpleServiceEdge;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(Location.TEST_6_COORDINATES)),
                    @Node(id = "2000000", coordinates = @Loc(Location.TEST_2_COORDINATES)),
                    @Node(id = "3000000", coordinates = @Loc(Location.TEST_1_COORDINATES)),
                    @Node(id = "4000000", coordinates = @Loc(Location.TEST_3_COORDINATES)) }, edges = {
                            @Edge(id = "1000000", coordinates = { @Loc(Location.TEST_6_COORDINATES),
                                    @Loc(Location.TEST_2_COORDINATES),
                                    @Loc(Location.TEST_1_COORDINATES),
                                    @Loc(Location.TEST_2_COORDINATES),
                                    @Loc(Location.TEST_3_COORDINATES) }) })
    private Atlas invalidSimpleEdge;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(Location.TEST_6_COORDINATES)),
                    @Node(id = "2000000", coordinates = @Loc(Location.TEST_2_COORDINATES)),
                    @Node(id = "3000000", coordinates = @Loc(Location.TEST_1_COORDINATES)) }, lines = {
                            @Line(id = "1000000", coordinates = { @Loc(Location.TEST_6_COORDINATES),
                                    @Loc(Location.TEST_2_COORDINATES),
                                    @Loc(Location.TEST_1_COORDINATES),
                                    @Loc(Location.TEST_6_COORDINATES) }) })
    private Atlas validSimpleLine;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(Location.TEST_6_COORDINATES)),
                    @Node(id = "2000000", coordinates = @Loc(Location.TEST_2_COORDINATES)),
                    @Node(id = "3000000", coordinates = @Loc(Location.TEST_1_COORDINATES)) }, lines = {
                            @Line(id = "1000000", coordinates = { @Loc(Location.TEST_6_COORDINATES),
                                    @Loc(Location.TEST_2_COORDINATES),
                                    @Loc(Location.TEST_1_COORDINATES),
                                    @Loc(Location.TEST_2_COORDINATES),
                                    @Loc(Location.TEST_3_COORDINATES) }) })
    private Atlas invalidSimpleLine;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(Location.TEST_6_COORDINATES)),
                    @Node(id = "2000000", coordinates = @Loc(Location.TEST_2_COORDINATES)),
                    @Node(id = "3000000", coordinates = @Loc(Location.TEST_1_COORDINATES)) }, areas = {
                            @Area(id = "1000000", coordinates = { @Loc(Location.TEST_6_COORDINATES),
                                    @Loc(Location.TEST_2_COORDINATES),
                                    @Loc(Location.TEST_1_COORDINATES),
                                    @Loc(Location.TEST_6_COORDINATES) }) })
    private Atlas invalidSimpleArea;

    @TestAtlas(
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(Location.TEST_6_COORDINATES)),
                    @Node(id = "2000000", coordinates = @Loc(Location.TEST_2_COORDINATES)),
                    @Node(id = "3000000", coordinates = @Loc(Location.TEST_1_COORDINATES)),
                    @Node(id = "4000000", coordinates = @Loc(Location.TEST_4_COORDINATES)),
                    @Node(id = "5000000", coordinates = @Loc(Location.TEST_3_COORDINATES)) }, lines = {
                            @Line(id = "1000000", coordinates = { @Loc(Location.TEST_6_COORDINATES),
                                    @Loc(Location.TEST_1_COORDINATES),
                                    @Loc(Location.TEST_2_COORDINATES),
                                    @Loc(Location.TEST_6_COORDINATES),
                                    @Loc(Location.TEST_3_COORDINATES),
                                    @Loc(Location.TEST_4_COORDINATES),
                                    @Loc(Location.TEST_6_COORDINATES) }) })
    private Atlas invalidFigureEightLine;

    /**
     * Get a figure 8 line
     *
     * @return A figure 8 line
     */
    public Atlas getInvalidFigureEightLine()
    {
        return this.invalidFigureEightLine;
    }

    /**
     * Get a closed loop area (no tags)
     *
     * @return The closed area (duplicate end point)
     */
    public Atlas getInvalidSimpleArea()
    {
        return this.invalidSimpleArea;
    }

    /**
     * Get an edge that self-intersects and continues on
     *
     * @return The invalid edge
     */
    public Atlas getInvalidSimpleEdge()
    {
        return this.invalidSimpleEdge;
    }

    /**
     * Get a line that self-intersects and continues on
     *
     * @return The invalid line
     */
    public Atlas getInvalidSimpleLine()
    {
        return this.invalidSimpleLine;
    }

    /**
     * Get a closed loop edge (no highway tags)
     *
     * @return The closed loop
     */
    public Atlas getValidSimpleEdge()
    {
        return this.validSimpleEdge;
    }

    /**
     * Get a closed loop line (no tags)
     *
     * @return The closed loop
     */
    public Atlas getValidSimpleLine()
    {
        return this.validSimpleLine;
    }

    /**
     * Get a simple service edge (closed, but not at ends)
     *
     * @return A simple service edge
     */
    public Atlas getValidSimpleServiceEdge()
    {
        return this.validSimpleServiceEdge;
    }
}
