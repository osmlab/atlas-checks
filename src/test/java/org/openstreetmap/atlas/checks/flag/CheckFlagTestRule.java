package org.openstreetmap.atlas.checks.flag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;

/**
 * {@link CheckFlagTest} data generator
 *
 * @author mkalender
 */
public class CheckFlagTestRule extends CoreTestRule
{
    private static final String TEST_1 = "31.335310,-121.009566";
    private static final String TEST_2 = "32.331417,-122.030487";
    private static final String TEST_3 = "33.325440,-123.033948";
    private static final String TEST_4 = "34.332451,-124.028932";
    private static final String TEST_5 = "35.317585,-125.052138";
    private static final String TEST_6 = "36.390535,-126.031007";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "a-tag=a-value" }),
                    @Node(coordinates = @Loc(value = TEST_2), tags = {
                            "another-tag=another-value" }),
                    @Node(coordinates = @Loc(value = TEST_3), tags = { "third-tag=" }) },
            // points
            points = {
                    @Point(coordinates = @Loc(value = TEST_4), tags = {
                            "sample-tag=sample-value" }),
                    @Point(coordinates = @Loc(value = TEST_5), tags = { "test-tag=sample-value" }),
                    @Point(coordinates = @Loc(value = TEST_6)) },
            // lines
            lines = { @Line(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_6),
                    @Loc(value = TEST_1) }, tags = { "sample-tag=sample-value" }) },
            // edges
            edges = { @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                    @Loc(value = TEST_3) }, tags = { "highway=primary" }) },
            // areas
            areas = { @Area(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_2),
                    @Loc(value = TEST_4), @Loc(value = TEST_1),
                    @Loc(value = TEST_6) }, tags = { "building=yes" }) })
    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
