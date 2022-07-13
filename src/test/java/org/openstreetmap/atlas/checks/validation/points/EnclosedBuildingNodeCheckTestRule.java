package org.openstreetmap.atlas.checks.validation.points;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * {@link EnclosedBuildingNodeCheckTest} data generator
 *
 * @author vladlemberg
 */

public class EnclosedBuildingNodeCheckTestRule extends CoreTestRule
{

    private static final String BUILDING_1 = "37.3260680,-121.9199798";
    private static final String BUILDING_2 = "37.3261021,-121.9200220";
    private static final String BUILDING_3 = "37.3261418,-121.9199718";
    private static final String BUILDING_4 = "37.3261076,-121.9199296";
    private static final String BUILDING_NODE_1 = "37.3260911,-121.9199859";
    private static final String BUILDING_NODE_2 = "37.3260605,-121.9199564";

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "100000", coordinates = @Loc(value = BUILDING_1)),
                    @Node(id = "200000", coordinates = @Loc(value = BUILDING_2)),
                    @Node(id = "300000", coordinates = @Loc(value = BUILDING_3)),
                    @Node(id = "400000", coordinates = @Loc(value = BUILDING_4)) },
            // points
            points = { @Point(id = "500000", coordinates = @Loc(value = BUILDING_NODE_1), tags = {
                    "building=yes" }) },

            // areas
            areas = { @Area(id = "600000", coordinates = { @Loc(value = BUILDING_1),
                    @Loc(value = BUILDING_2), @Loc(value = BUILDING_3), @Loc(value = BUILDING_4),
                    @Loc(value = BUILDING_1) }, tags = { "building=yes" }) })
    private Atlas enclosedBuildingNodeAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = BUILDING_1)),
                    @Node(id = "2000000", coordinates = @Loc(value = BUILDING_2)),
                    @Node(id = "3000000", coordinates = @Loc(value = BUILDING_3)),
                    @Node(id = "4000000", coordinates = @Loc(value = BUILDING_4)) },
            // points
            points = { @Point(id = "5000000", coordinates = @Loc(value = BUILDING_NODE_1), tags = {
                    "building=yes", "addr:housenumber=123" }) },

            // areas
            areas = { @Area(id = "600000", coordinates = { @Loc(value = BUILDING_1),
                    @Loc(value = BUILDING_2), @Loc(value = BUILDING_3), @Loc(value = BUILDING_4),
                    @Loc(value = BUILDING_1) }, tags = { "building=yes" }) })
    private Atlas enclosedBuildingNodeAddressAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = BUILDING_1)),
                    @Node(id = "2000000", coordinates = @Loc(value = BUILDING_2)),
                    @Node(id = "3000000", coordinates = @Loc(value = BUILDING_3)),
                    @Node(id = "4000000", coordinates = @Loc(value = BUILDING_4)) },
            // points
            points = { @Point(id = "5000000", coordinates = @Loc(value = BUILDING_NODE_1), tags = {
                    "building=yes", "addr:housenumber=123" }) },

            // areas
            areas = { @Area(id = "600000", coordinates = { @Loc(value = BUILDING_1),
                    @Loc(value = BUILDING_2), @Loc(value = BUILDING_3), @Loc(value = BUILDING_4),
                    @Loc(value = BUILDING_1) }) })
    private Atlas enclosedBuildingNodeAreaAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = BUILDING_1)),
                    @Node(id = "2000000", coordinates = @Loc(value = BUILDING_2)),
                    @Node(id = "3000000", coordinates = @Loc(value = BUILDING_3)),
                    @Node(id = "4000000", coordinates = @Loc(value = BUILDING_4)) },
            // points
            points = { @Point(id = "5000000", coordinates = @Loc(value = BUILDING_NODE_2), tags = {
                    "building=yes", "addr:housenumber=123" }) },

            // areas
            areas = { @Area(id = "600000", coordinates = { @Loc(value = BUILDING_1),
                    @Loc(value = BUILDING_2), @Loc(value = BUILDING_3), @Loc(value = BUILDING_4),
                    @Loc(value = BUILDING_1) }, tags = { "building=yes" }) })
    private Atlas loneBuildingNodeAtlas;

    public Atlas buildingNodeAddressAtlas()
    {
        return this.enclosedBuildingNodeAddressAtlas;
    }

    public Atlas buildingNodeAreaAtlas()
    {
        return this.enclosedBuildingNodeAreaAtlas;
    }

    public Atlas buildingNodeAtlas()
    {
        return this.enclosedBuildingNodeAtlas;
    }

    public Atlas loneBuildingNodeAtlas()
    {
        return this.loneBuildingNodeAtlas;
    }
}
