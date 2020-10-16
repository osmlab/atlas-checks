package org.openstreetmap.atlas.checks.utility;

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
 * Unit test rule for {@link OpenStreetMapCheckFlagConverter}.
 *
 * @author bbreithaupt
 */
public class OpenStreetMapCheckFlagConverterTestRule extends CoreTestRule
{
    private static final String TEST1 = "47.6226594,-122.3548000";
    private static final String TEST2 = "47.6226526,-122.3531630";
    private static final String TEST3 = "47.6215390,-122.3531760";
    private static final String TEST4 = "47.6215458,-122.3548130";

    @TestAtlas(
            // Areas
            areas = { @Area(id = "1000000", coordinates = { @Loc(value = TEST1),
                    @Loc(value = TEST2), @Loc(value = TEST3), @Loc(value = TEST4),
                    @Loc(value = TEST1) }, tags = { "building=yes", "name=Key Arena" }) },
            // Lines
            lines = { @Line(id = "1000000", coordinates = { @Loc(value = TEST1),
                    @Loc(value = TEST2) }, tags = "barrier=wall") },
            // Points
            points = {
                    @Point(id = "1000000", coordinates = @Loc(value = TEST1), tags = "place=corner"),
                    @Point(id = "2000000", coordinates = @Loc(value = TEST2), tags = "place=corner") },
            // Nodes
            nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST1)),
                    @Node(id = "3000000", coordinates = @Loc(value = TEST3)) },
            // Edges
            edges = {
                    @Edge(id = "1000001", coordinates = { @Loc(value = TEST1), @Loc(value = TEST2),
                            @Loc(value = TEST3) }, tags = "highway=footway"),
                    @Edge(id = "1000002", coordinates = { @Loc(value = TEST3), @Loc(value = TEST4),
                            @Loc(value = TEST1) }, tags = "highway=footway") },
            // Relations
            relations = { @Relation(id = "1000000", members = {
                    @Member(id = "1000000", type = "area", role = "part") }, tags = {
                            "type=building", "name=Key Arena" }) })
    private Atlas atlas;

    public Atlas atlas()
    {
        return this.atlas;
    }
}
