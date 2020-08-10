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
 * Unit test rule for {@link InvalidTagsCheck}.
 *
 * @author bbreithaupt
 */
public class InvalidTagsCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "20.538246,10.546134";
    private static final String TEST_2 = "20.535768,10.543755";
    private static final String TEST_3 = "20.535773, 10.548353";

    @TestAtlas(
            // nodes
            nodes = {
                    @Node(id = "1000000", coordinates = @Loc(value = TEST_1), tags = {
                            "crossing=traffic_signals", "place=island", "natural=lake" }),
                    @Node(id = "2000000", coordinates = @Loc(value = TEST_2), tags = {}),
                    @Node(id = "3000000", coordinates = @Loc(value = TEST_3), tags = {
                            "place=island", "natural=lake" }) },
            // points
            points = { @Point(id = "1000000", coordinates = @Loc(value = TEST_1), tags = {
                    "crossing=traffic_signals" }) },
            // edges
            edges = {
                    @Edge(id = "5000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "route=ferry", "highway=ferry" }),
                    @Edge(id = "5000002", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "route=ferry", "highway=ferry" }),
                    @Edge(id = "5000003", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                                    "highway=primary", "area=yes" }),
                    @Edge(id = "5000004", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "junction=roundabout",
                                    "highway=secondary", "area=no" }) },
            // lines
            lines = { @Line(id = "6000000", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2) }, tags = { "construction=primary", "water=lake" }) },
            // areas
            areas = { @Area(id = "7000000", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2),
                    @Loc(value = TEST_3) }, tags = { "boundary=protected_area" }) },
            // relations
            relations = {
                    @Relation(id = "8000000", members = @Member(id = "1000000", role = "member", type = "node"), tags = {
                            "boundary=protected_area" }) })
    private Atlas testAtlas;

    public Atlas testAtlas()
    {
        return this.testAtlas;
    }
}
