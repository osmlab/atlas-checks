package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Test data for {@link PedestrianAreaOverlappingEdgeCheckTest}
 *
 * @author sayas01
 */
public class PedestrianAreaOverlappingEdgeCheckTestRule extends CoreTestRule
{
    private static final String AREA_LOC_1 = "34.0619736, -4.9729771";
    private static final String AREA_LOC_2 = "34.0619558, -4.9729228";
    private static final String AREA_LOC_3 = "34.0619157, -4.9728423";
    private static final String AREA_LOC_4 = "34.0622502, -4.9725721";
    private static final String AREA_LOC_5 = "34.0623169, -4.9726083";
    private static final String AREA_LOC_6 = "34.0623146, -4.9727816";
    private static final String AREA_LOC_7 = "34.0623136, -4.9729020";

    private static final String LOC_1_WITHIN_AREA = "34.0620432, -4.9728769";
    private static final String LOC_2_WITHIN_AREA = "34.0621669, -4.9728356";
    private static final String LOC_OUTSIDE_AREA = "34.0618719, -4.9729555";

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "3000000001", coordinates = @Loc(value = AREA_LOC_1)),
            @Node(id = "4000000001", coordinates = @Loc(value = AREA_LOC_2)),
            @Node(id = "5000000001", coordinates = @Loc(value = AREA_LOC_3)),
            @Node(id = "6000000001", coordinates = @Loc(value = AREA_LOC_4)),
            @Node(id = "7000000001", coordinates = @Loc(value = AREA_LOC_5)),
            @Node(id = "8000000001", coordinates = @Loc(value = AREA_LOC_6)),
            @Node(id = "9000000001", coordinates = @Loc(value = AREA_LOC_7)),
            @Node(id = "1000000001", coordinates = @Loc(value = LOC_OUTSIDE_AREA)), },
            // areas
            areas = { @Area(id = "1000000001", coordinates = { @Loc(value = AREA_LOC_1),
                    @Loc(value = AREA_LOC_2), @Loc(value = AREA_LOC_3), @Loc(value = AREA_LOC_4),
                    @Loc(value = AREA_LOC_5), @Loc(value = AREA_LOC_6),
                    @Loc(value = AREA_LOC_7) }, tags = { "area=yes",
                            "highway=pedestrian" }) }, edges = {
                                    @Edge(id = "2300000001", coordinates = {
                                            @Loc(value = LOC_OUTSIDE_AREA),
                                            @Loc(value = AREA_LOC_2) }, tags = {
                                                    "highway=residential" }),
                                    @Edge(id = "2000000001", coordinates = {
                                            @Loc(value = AREA_LOC_2),
                                            @Loc(value = AREA_LOC_3) }, tags = {
                                                    "highway=residential" }),

            })
    private Atlas correctlySnappedAtlas;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "3000000001", coordinates = @Loc(value = AREA_LOC_1)),
            @Node(id = "4000000001", coordinates = @Loc(value = AREA_LOC_2)),
            @Node(id = "5000000001", coordinates = @Loc(value = AREA_LOC_3)),
            @Node(id = "6000000001", coordinates = @Loc(value = AREA_LOC_4)),
            @Node(id = "7000000001", coordinates = @Loc(value = AREA_LOC_5)),
            @Node(id = "8000000001", coordinates = @Loc(value = AREA_LOC_6)),
            @Node(id = "9000000001", coordinates = @Loc(value = AREA_LOC_7)),
            @Node(id = "1000000001", coordinates = @Loc(value = LOC_OUTSIDE_AREA)),
            @Node(id = "9100000001", coordinates = @Loc(value = LOC_1_WITHIN_AREA)),
            @Node(id = "1200000001", coordinates = @Loc(value = LOC_2_WITHIN_AREA)) },
            // areas
            areas = { @Area(id = "1000000001", coordinates = { @Loc(value = AREA_LOC_1),
                    @Loc(value = AREA_LOC_2), @Loc(value = AREA_LOC_3), @Loc(value = AREA_LOC_4),
                    @Loc(value = AREA_LOC_5), @Loc(value = AREA_LOC_6),
                    @Loc(value = AREA_LOC_7) }, tags = { "area=yes",
                            "highway=pedestrian" }) }, edges = {
                                    @Edge(id = "2300000001", coordinates = {
                                            @Loc(value = LOC_OUTSIDE_AREA),
                                            @Loc(value = AREA_LOC_2) }, tags = {
                                                    "highway=residential" }),
                                    @Edge(id = "2000000001", coordinates = {
                                            @Loc(value = AREA_LOC_2),
                                            @Loc(value = LOC_1_WITHIN_AREA) }, tags = {
                                                    "highway=cycleway", "foot=yes" }),
                                    @Edge(id = "2500000001", coordinates = {
                                            @Loc(value = LOC_1_WITHIN_AREA),
                                            @Loc(value = LOC_2_WITHIN_AREA) }, tags = {
                                                    "highway=residential" }),
                                    @Edge(id = "2400000001", coordinates = {
                                            @Loc(value = LOC_2_WITHIN_AREA),
                                            @Loc(value = AREA_LOC_6) }, tags = {
                                                    "highway=residential" }) })
    private Atlas inCorrectlySnappedAtlas;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "3000000001", coordinates = @Loc(value = AREA_LOC_1)),
            @Node(id = "4000000001", coordinates = @Loc(value = AREA_LOC_2)),
            @Node(id = "5000000001", coordinates = @Loc(value = AREA_LOC_3)),
            @Node(id = "6000000001", coordinates = @Loc(value = AREA_LOC_4)),
            @Node(id = "7000000001", coordinates = @Loc(value = AREA_LOC_5)),
            @Node(id = "8000000001", coordinates = @Loc(value = AREA_LOC_6)),
            @Node(id = "9000000001", coordinates = @Loc(value = AREA_LOC_7)),
            @Node(id = "1000000001", coordinates = @Loc(value = LOC_OUTSIDE_AREA)),
            @Node(id = "9100000001", coordinates = @Loc(value = LOC_1_WITHIN_AREA)),
            @Node(id = "1200000001", coordinates = @Loc(value = LOC_2_WITHIN_AREA)) },
            // areas
            areas = { @Area(id = "1000000001", coordinates = { @Loc(value = AREA_LOC_1),
                    @Loc(value = AREA_LOC_2), @Loc(value = AREA_LOC_3), @Loc(value = AREA_LOC_4),
                    @Loc(value = AREA_LOC_5), @Loc(value = AREA_LOC_6),
                    @Loc(value = AREA_LOC_7) }, tags = { "area=yes",
                            "highway=pedestrian" }) }, edges = {
                                    @Edge(id = "2300000001", coordinates = {
                                            @Loc(value = LOC_OUTSIDE_AREA),
                                            @Loc(value = AREA_LOC_2) }, tags = {
                                                    "highway=residential" }),
                                    @Edge(id = "2000000001", coordinates = {
                                            @Loc(value = AREA_LOC_2),
                                            @Loc(value = LOC_1_WITHIN_AREA) }, tags = {
                                                    "highway=residential" }) })
    private Atlas intersectingEdgeEndingInsideArea;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "3000000001", coordinates = @Loc(value = AREA_LOC_1)),
            @Node(id = "4000000001", coordinates = @Loc(value = AREA_LOC_2)),
            @Node(id = "5000000001", coordinates = @Loc(value = AREA_LOC_3)),
            @Node(id = "6000000001", coordinates = @Loc(value = AREA_LOC_4)),
            @Node(id = "7000000001", coordinates = @Loc(value = AREA_LOC_5)),
            @Node(id = "8000000001", coordinates = @Loc(value = AREA_LOC_6)),
            @Node(id = "9000000001", coordinates = @Loc(value = AREA_LOC_7)),
            @Node(id = "1000000001", coordinates = @Loc(value = LOC_OUTSIDE_AREA)),
            @Node(id = "9100000001", coordinates = @Loc(value = LOC_1_WITHIN_AREA)),
            @Node(id = "1200000001", coordinates = @Loc(value = LOC_2_WITHIN_AREA)) },
            // areas
            areas = { @Area(id = "1000000001", coordinates = { @Loc(value = AREA_LOC_1),
                    @Loc(value = AREA_LOC_2), @Loc(value = AREA_LOC_3), @Loc(value = AREA_LOC_4),
                    @Loc(value = AREA_LOC_5), @Loc(value = AREA_LOC_6),
                    @Loc(value = AREA_LOC_7) }, tags = { "area=yes",
                            "highway=pedestrian" }) }, edges = {
                                    @Edge(id = "2300000001", coordinates = {
                                            @Loc(value = LOC_OUTSIDE_AREA),
                                            @Loc(value = AREA_LOC_2) }, tags = {
                                                    "highway=residential" }),
                                    @Edge(id = "2000000001", coordinates = {
                                            @Loc(value = AREA_LOC_2),
                                            @Loc(value = LOC_1_WITHIN_AREA) }, tags = {
                                                    "highway=residential", "layer=2" }) })
    private Atlas edgesWithDifferentElevation;

    public Atlas getCorrectlySnappedAtlas()
    {
        return this.correctlySnappedAtlas;
    }

    public Atlas getEdgesWithDifferentElevation()
    {
        return this.edgesWithDifferentElevation;
    }

    public Atlas getInCorrectlySnappedAtlas()
    {
        return this.inCorrectlySnappedAtlas;
    }

    public Atlas getIntersectingEdgeEndingInsideArea()
    {
        return this.intersectingEdgeEndingInsideArea;
    }
}
