package org.openstreetmap.atlas.checks.event;

import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link CheckFlagGeoJsonProcessorTest} test data
 *
 * @author brian_l_davis
 */
public class CheckFlagGeoJsonProcessorTestRule extends CoreTestRule
{
    private static final String TEST_1 = "37.335310,-122.009566";
    private static final String TEST_2 = "37.3314171,-122.0304871";
    private static final String TEST_3 = "37.325440,-122.033948";
    private static final String TEST_4 = "37.332451,-122.028932";
    private static final String TEST_5 = "37.317585,-122.052138";
    private static final String TEST_6 = "37.390535,-122.031007";

    @TestAtlas(

            // GeoJson Points
            nodes = {

                    @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)) },

            // GeoJson LineString
            edges = {

                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=primary" }) },

            // GeoJson Polygon
            areas = {

                    @Area(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_2),
                            @Loc(value = TEST_4), @Loc(value = TEST_1),
                            @Loc(value = TEST_6) }, tags = { "building=yes" }) })
    private Atlas atlas;

    public CheckFlagEvent getCheckFlagEvent()
    {
        final CheckFlag flag = new CheckFlag("Test check flag");
        flag.addObject(Iterables.head(atlas.nodes()), "Flagged Node");
        flag.addObject(Iterables.head(atlas.edges()), "Flagged Edge");
        flag.addObject(Iterables.head(atlas.areas()), "Flagged Area");

        final CheckFlagEvent event = new CheckFlagEvent("sample-name", flag);
        event.getCheckFlag().addInstruction("First instruction");
        event.getCheckFlag().addInstruction("Second instruction");
        return event;
    }
}
