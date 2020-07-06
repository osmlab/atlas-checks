package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author laura on 09/06/2020.
 */
public class ConditionalRestrictionCheckTestRule extends CoreTestRule
{

    private static final String TEST_1 = "47.2136626201459,-122.443275382856";
    private static final String TEST_2 = "47.2138327316739,-122.44258668766";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)) }, edges = {
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=pedestrian",
                                    "access:lanes:left:conditional=|yes|no|yes @ (Mo-Fr 06:00-11:00,17:00-19:00;Sa 03:30-19:00)",
                                    "bicycle=yes", "bicycle:conditional=no @ (Sa 08:00-16:00)" }) })
    private Atlas accessLanes;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)) }, edges = {
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=pedestrian",
                                    "motor_vehicle:conditional=delivery @ (Mo-Fr 06:00-11:00,17:00-19:00;Sa 03:30-19:00)",
                                    "bicycle=yes", "bicycle:conditional=no @ (Sa 08:00-16:00)" }) })
    private Atlas conditionalWay;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)) }, edges = {
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=pedestrian",
                                    "hgv:maxweight:conditional=none @ delivery" }) })
    private Atlas invalidConditionalKey;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)) }, edges = {
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=pedestrian",
                                    "motor_vehicle:conditional=delivery @ (Mo-Fr 06:00-11:00,17:00-19:00;Sa 03:30-19:00)",
                                    "bicycle=yes", "bicycle:conditional=no@(Sa 08:00-16:00)" }) })
    private Atlas invalidConditionFormat;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)) }, edges = {
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=pedestrian",
                                    "psv:lanes:conditional:=1 @ (Mo-Fr 06:30-09:00,16:00-18:30)",
                                    "bicycle=yes", "bicycle:conditional=no @ (Sa 08:00-16:00)" }) })
    private Atlas invalidAccessLanes;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)) }, edges = {
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=pedestrian",
                                    "motor_vehicle:conditional=delivery @ (Mo-Fr 06:00-11:00,17:00-19:00;Sa 03:30-19:00)",
                                    "bicycle=yes",
                                    "bicycle:conditional=notallowed @ (Sa 08:00-16:00)" }) })
    private Atlas invalidAccessType;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)) }, edges = {
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=pedestrian",
                                    "lanes:psv:conditional:=1 @ (Mo-Fr 06:30-09:00,16:00-18:30)",
                                    "bicycle=yes", "bicycle:conditional=no @ (Sa 08:00-16:00)" }) })
    private Atlas reversedLanesTransport;

    public Atlas getAccessLanes()
    {
        return this.accessLanes;
    }

    public Atlas getConditionalWay()
    {
        return this.conditionalWay;
    }

    public Atlas getInvalidAccessLanes()
    {
        return this.invalidAccessLanes;
    }

    public Atlas getInvalidAccessType()
    {
        return this.invalidAccessType;
    }

    public Atlas getInvalidConditionFormat()
    {
        return this.invalidConditionFormat;
    }

    public Atlas getInvalidConditionalKey()
    {
        return this.invalidConditionalKey;
    }

    public Atlas getReversedLanesTransport()
    {
        return this.reversedLanesTransport;
    }
}
