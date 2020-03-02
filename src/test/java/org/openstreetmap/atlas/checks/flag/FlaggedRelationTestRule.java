package org.openstreetmap.atlas.checks.flag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * Unit test rule for {@link FlaggedRelationTest}.
 *
 * @author bbreithaupt
 */
public class FlaggedRelationTestRule extends CoreTestRule
{
    public static final String TEST_1 = "1.2887014, 103.8628407";
    public static final String TEST_2 = "1.2895026, 103.8645045";
    public static final String TEST_3 = "1.2943360, 103.8638678";
    public static final String TEST_4 = "1.2944915, 103.8631437";
    public static final String TEST_5 = "1.2941844, 103.8637712";
    public static final String TEST_6 = "1.2895198, 103.8643810";

    @TestAtlas(
            // lines
            lines = {
                    @Line(id = "1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2), @Loc(value = TEST_3), @Loc(value = TEST_4) }),
                    @Line(id = "2000000", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5), @Loc(value = TEST_6), @Loc(value = TEST_1) }) },
            // relations
            relations = { @Relation(id = "1000000", members = {
                    @Member(id = "1000000", type = "line", role = "outer"),
                    @Member(id = "2000000", type = "line", role = "outer"), }, tags = {
                            "type=multipolygon" }) })
    private Atlas multipolygonAtlas;

    @TestAtlas(
            // lines
            lines = {
                    @Line(id = "1000000", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }),
                    @Line(id = "2000000", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5), @Loc(value = TEST_6), @Loc(value = TEST_1) }) },
            // relations
            relations = { @Relation(id = "1000000", members = {
                    @Member(id = "1000000", type = "line", role = "outer"),
                    @Member(id = "2000000", type = "line", role = "outer"), }, tags = {
                            "type=multipolygon" }) })
    private Atlas badMultipolygonAtlas;

    @TestAtlas(
            // lines
            lines = {
                    @Line(id = "1000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2), @Loc(value = TEST_3), @Loc(value = TEST_4) }),
                    @Line(id = "2000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_6), @Loc(value = TEST_5), @Loc(value = TEST_4) }) },
            // relations
            relations = { @Relation(id = "1000000", members = {
                    @Member(id = "1000000", type = "line", role = "forward"),
                    @Member(id = "2000000", type = "line", role = "backward"), }, tags = {
                            "type=circuit" }) })
    private Atlas circuitAtlas;

    public Atlas badMultipolygonAtlas()
    {
        return this.badMultipolygonAtlas;
    }

    public Atlas circuitAtlas()
    {
        return this.circuitAtlas;
    }

    public Atlas multipolygonAtlas()
    {
        return this.multipolygonAtlas;
    }
}
