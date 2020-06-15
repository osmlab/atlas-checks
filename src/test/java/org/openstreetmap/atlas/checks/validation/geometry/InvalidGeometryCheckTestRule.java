package org.openstreetmap.atlas.checks.validation.geometry;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link InvalidGeometryCheckTest} data generator
 *
 * @author jklamer
 * @author bbreithaupt
 */
public class InvalidGeometryCheckTestRule extends CoreTestRule
{
    public static final String TEST_ID_1 = "123456789000000";
    public static final String TEST_ID_2 = "223456789000000";
    public static final String TEST_ID_3 = "323456789000000";
    public static final String TEST_ID_4 = "423456789000000";
    public static final String TEST_ID_5 = "523456789000000";
    public static final String TEST_ID_6 = "623456789000000";
    public static final String TEST_ID_7 = "723456789000000";
    private static final String LOCATION_1 = "10.13076961357, -80.73619975709";
    private static final String LOCATION_2 = "10.12871245371, -80.69336019785";
    private static final String LOCATION_3 = "10.09288147237, -80.73759291349";
    private static final String LOCATION_4 = "10.09168132254, -80.69405677605";
    private static final String LOCATION_5 = "10.10625426762, -80.7243579277";
    private static final String LOCATION_6 = "10.11654065468, -80.71007807462";
    private static final String LOCATION_7 = "10.11654065468, NaN";
    private static final String LOCATION_8 = "10.11448340362, -80.7306271315";

    @TestAtlas(
            // areas
            areas = { @Area(id = TEST_ID_5, coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_4), @Loc(value = LOCATION_2), @Loc(value = LOCATION_3),
                    @Loc(value = LOCATION_1) }) })
    private Atlas bowtiePolygonAtlas;

    @TestAtlas(
            // areas
            areas = { @Area(id = TEST_ID_7, coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_2), @Loc(value = LOCATION_4), @Loc(value = LOCATION_6),
                    @Loc(value = LOCATION_5), @Loc(value = LOCATION_4), @Loc(value = LOCATION_3),
                    @Loc(value = LOCATION_5), @Loc(value = LOCATION_8), @Loc(value = LOCATION_3),
                    @Loc(value = LOCATION_1) }) })
    private Atlas disconnectedCenterPolygonAtlas;

    @TestAtlas(
            // lines
            lines = { @Line(coordinates = { @Loc(value = LOCATION_3), @Loc(value = LOCATION_4),
                    @Loc(value = LOCATION_2), @Loc(value = LOCATION_6), @Loc(value = LOCATION_5),
                    @Loc(value = LOCATION_1) }) })
    private Atlas fineLinearAtlas;

    @TestAtlas(
            // areas
            areas = { @Area(coordinates = { @Loc(value = LOCATION_1), @Loc(value = LOCATION_2),
                    @Loc(value = LOCATION_4), @Loc(value = LOCATION_3), @Loc(LOCATION_1) }) })
    private Atlas finePolygonAtlas;

    @TestAtlas(
            // areas
            areas = { @Area(id = TEST_ID_6, coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_2), @Loc(value = LOCATION_4), @Loc(value = LOCATION_5),
                    @Loc(value = LOCATION_2) }) })
    private Atlas hangNailPolygonAtlas;

    @TestAtlas(
            // nodes
            nodes = {
                    @Node(coordinates = @Loc(value = LOCATION_5), tags = "synthetic_boundary_node=yes") },
            // edges
            edges = { @Edge(id = TEST_ID_3, coordinates = { @Loc(value = LOCATION_5),
                    @Loc(value = LOCATION_5) }) })
    private Atlas boundaryNodeAtlas;

    @TestAtlas(
            // lines
            lines = { @Line(id = TEST_ID_2, coordinates = { @Loc(value = LOCATION_8),
                    @Loc(value = LOCATION_8) }) })
    private Atlas notValidLinearAtlas;

    @TestAtlas(
            // areas
            areas = { @Area(id = TEST_ID_5, coordinates = { @Loc(value = LOCATION_1),
                    @Loc(value = LOCATION_4), @Loc(value = LOCATION_2), @Loc(value = LOCATION_3),
                    @Loc(value = LOCATION_1) }, tags = "synthetic_geometry_sliced=yes") })
    private Atlas borderSlicedPolygonAtlas;

    public Atlas borderSlicedPolygonAtlas()
    {
        return this.borderSlicedPolygonAtlas;
    }

    public Atlas boundaryNodeAtlas()
    {
        return this.boundaryNodeAtlas;
    }

    public Atlas getBowtiePolygonAtlas()
    {
        return this.bowtiePolygonAtlas;
    }

    public Atlas getDisconnectedCenterPolygonAtlas()
    {
        return this.disconnectedCenterPolygonAtlas;
    }

    public Atlas getFineLinearAtlas()
    {
        return this.fineLinearAtlas;
    }

    public Atlas getFinePolygonAtlas()
    {
        return this.finePolygonAtlas;
    }

    public Atlas getHangNailPolygonAtlas()
    {
        return this.hangNailPolygonAtlas;
    }

    public Atlas getNotValidLinearAtlas()
    {
        return this.notValidLinearAtlas;
    }

}
