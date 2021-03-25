package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Tests for {@link TollValidationCheck}
 * 
 * @author v-garei
 */
public class TollValidationCheckTestRule extends CoreTestRule
{
    // Specific Scenario for notInconsistentTollTagEscapablePreliminaryCheck
    private static final String way1_n0de1 = "40.9108780, 29.4695635";
    private static final String way1_n0de2_way2_n0de1 = "40.9081586, 29.4686488";
    private static final String way2_n0de2_way3_n0de1_way4_n0de1 = "40.9078200, 29.4685416";
    private static final String way3_n0de2_way5_n0de1 = "40.9070984, 29.4684401";
    private static final String way4_n0de2 = "40.9067710, 29.4681857";
    private static final String way5_n0de2 = "40.9065417, 29.4682921";
    private static final String way0_n0de1 = "40.9121134, 29.4700609";

    // Used for 3 tests - escapableWayNeedsTollTagRemoved, inconsistentTollTags, and
    // intersectingTollFeatureWithoutTag
    private static final String WAY1_NODE1 = "40.9130354, 29.4700719";
    private static final String WAY1_NODE2 = "40.9123887, 29.4698597";
    private static final String WAY2_NODE2 = "40.9118904, 29.4696993";
    private static final String WAY3_NODE2 = "40.9082867, 29.4685152";
    private static final String WAY4_NODE1 = "40.91344, 29.47000";
    private static final String WAY5_NODE2 = "40.91168, 29.46935";
    private static final String AREA_NODE1 = "40.9127774, 29.4698422";
    private static final String AREA_NODE2 = "40.9125929, 29.4704725";
    private static final String AREA_NODE3 = "40.9124979, 29.4704238";
    private static final String AREA_NODE4 = "40.9126826, 29.4697936";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WAY4_NODE1)),
            @Node(coordinates = @Loc(value = WAY1_NODE1)),
            @Node(coordinates = @Loc(value = WAY1_NODE2)),
            @Node(coordinates = @Loc(value = WAY2_NODE2)),
            @Node(coordinates = @Loc(value = WAY5_NODE2)) }, edges = {
                    @Edge(id = "6000001", coordinates = { @Loc(value = WAY4_NODE1),
                            @Loc(value = WAY1_NODE1) }, tags = { "highway=motorway", "toll=no" }),
                    @Edge(id = "7000001", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = { "highway=motorway", "toll=yes" }),
                    @Edge(id = "8000001", coordinates = { @Loc(value = WAY1_NODE2),
                            @Loc(value = WAY2_NODE2) }, tags = { "highway=motorway", "toll=yes" }),
                    @Edge(id = "9000001", coordinates = { @Loc(value = WAY2_NODE2),
                            @Loc(value = WAY5_NODE2) }, tags = { "highway=motorway", "toll=no" }) })
    private Atlas escapableWayNeedsTollTagRemoved;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WAY1_NODE1)),
            @Node(coordinates = @Loc(value = WAY1_NODE2)),
            @Node(coordinates = @Loc(value = WAY2_NODE2)),
            @Node(coordinates = @Loc(value = WAY3_NODE2)) }, edges = {
                    @Edge(id = "3000001", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = { "highway=motorway", "toll=yes" }),
                    @Edge(id = "4000001", coordinates = { @Loc(value = WAY1_NODE2),
                            @Loc(value = WAY2_NODE2) }, tags = "highway=motorway"),
                    @Edge(id = "5000001", coordinates = { @Loc(value = WAY2_NODE2),
                            @Loc(value = WAY3_NODE2) }, tags = { "highway=motorway",
                                    "toll=yes" }) })
    private Atlas inconsistentTollTags;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WAY1_NODE1)),
            @Node(coordinates = @Loc(value = WAY1_NODE2)),
            @Node(coordinates = @Loc(value = AREA_NODE1)),
            @Node(coordinates = @Loc(value = AREA_NODE2)),
            @Node(coordinates = @Loc(value = AREA_NODE3)),
            @Node(coordinates = @Loc(value = AREA_NODE4)) }, edges = {
                    @Edge(id = "1000001", coordinates = { @Loc(value = WAY1_NODE1),
                            @Loc(value = WAY1_NODE2) }, tags = "highway=motorway") }, areas = {
                                    @Area(coordinates = { @Loc(value = AREA_NODE1),
                                            @Loc(value = AREA_NODE2), @Loc(value = AREA_NODE3),
                                            @Loc(value = AREA_NODE4) }, tags = { "building=yes",
                                                    "barrier=toll_booth" }) })
    private Atlas intersectingTollFeatureWithoutTag;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = way1_n0de1)),
            @Node(coordinates = @Loc(value = way1_n0de2_way2_n0de1)),
            @Node(coordinates = @Loc(value = way2_n0de2_way3_n0de1_way4_n0de1)),
            @Node(coordinates = @Loc(value = way3_n0de2_way5_n0de1)),
            @Node(coordinates = @Loc(value = way4_n0de2)),
            @Node(coordinates = @Loc(value = way5_n0de2)),
            @Node(coordinates = @Loc(value = way0_n0de1)) }, edges = {
                    @Edge(id = "11000001", coordinates = { @Loc(value = way0_n0de1),
                            @Loc(value = way1_n0de1) }, tags = { "highway=motorway", "toll=no" }),
                    @Edge(id = "12000001", coordinates = { @Loc(value = way1_n0de1),
                            @Loc(value = way1_n0de2_way2_n0de1) }, tags = { "highway=motorway",
                                    "toll=yes" }),
                    @Edge(id = "13000001", coordinates = { @Loc(value = way1_n0de2_way2_n0de1),
                            @Loc(value = way2_n0de2_way3_n0de1_way4_n0de1) }, tags = {
                                    "highway=motorway", "toll=yes" }),
                    @Edge(id = "14000001", coordinates = {
                            @Loc(value = way2_n0de2_way3_n0de1_way4_n0de1),
                            @Loc(value = way3_n0de2_way5_n0de1) }, tags = { "highway=motorway",
                                    "toll=no" }),
                    @Edge(id = "15000001", coordinates = {
                            @Loc(value = way2_n0de2_way3_n0de1_way4_n0de1),
                            @Loc(value = way4_n0de2) }, tags = { "highway=motorway", "toll=yes" }),
                    @Edge(id = "16000001", coordinates = { @Loc(value = way3_n0de2_way5_n0de1),
                            @Loc(value = way5_n0de2) }, tags = { "highway=motorway", "toll=no" }) })
    private Atlas nonInconsistentTollTagEscapablePreliminaryCheck;

    public Atlas escapableWayNeedsTollTagRemoved()
    {
        return this.escapableWayNeedsTollTagRemoved;
    }

    public Atlas inconsistentTollTags()
    {
        return this.inconsistentTollTags;
    }

    public Atlas intersectingTollFeatureWithoutTag()
    {
        return this.intersectingTollFeatureWithoutTag;
    }

    public Atlas nonInconsistentTollTagEscapablePreliminaryCheck()
    {
        return this.nonInconsistentTollTagEscapablePreliminaryCheck;
    }
}
