package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author mm-ciub on 31/07/2020.
 */
public class BadValueTagCheckTestRule extends CoreTestRule
{

    private static final String TEST_1 = "47.2136626201459,-122.443275382856";
    private static final String TEST_2 = "47.2138327316739,-122.44258668766";

    @TestAtlas(nodes = { @Node(id = "1000000", coordinates = @Loc(value = TEST_1)),
            @Node(id = "2000000", coordinates = @Loc(value = TEST_2)) }, edges = {
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=pedestrian",
                                    "source=https://www.google.com/maps/d/viewer?mid1Ct1kHCSzB-VvPdK7vw3pPnXeifs",
                                    "bicycle=yes" }) })
    private Atlas illegalSourceLinkEdge;

    @TestAtlas(nodes = {
            @Node(id = "1000000", coordinates = @Loc(value = TEST_1), tags = {
                    "source=Here maps" }),
            @Node(id = "2000000", coordinates = @Loc(value = TEST_2), tags = {
                    "source=Vworld" }) }, edges = {
                            @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_1),
                                    @Loc(value = TEST_2) }) })
    private Atlas illegalSourceLinkNode;

    public Atlas getIllegalSourceLinkEdge()
    {
        return this.illegalSourceLinkEdge;
    }

    public Atlas getIllegalSourceLinkNode()
    {
        return this.illegalSourceLinkNode;
    }
}
