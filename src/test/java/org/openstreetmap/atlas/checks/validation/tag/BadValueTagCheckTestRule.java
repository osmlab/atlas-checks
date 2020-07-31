package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author mm-ciub on 31/07/2020.
 */
public class BadValueTagCheckTestRule extends CoreTestRule
{

    private static final String TEST_1 = "47.2136626201459,-122.443275382856";
    private static final String TEST_2 = "47.2138327316739,-122.44258668766";

    @TestAtlas(nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_1)),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_2)) }, edges = {
                    @TestAtlas.Edge(id = "1001000001", coordinates = {
                            @TestAtlas.Loc(value = TEST_1),
                            @TestAtlas.Loc(value = TEST_2) }, tags = { "highway=pedestrian",
                                    "source=https://www.google.com/maps/d/viewer?mid1Ct1kHCSzB-VvPdK7vw3pPnXeifs",
                                    "bicycle=yes" }) })
    private Atlas illegalSourceLinkEdge;

    @TestAtlas(nodes = {
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_1), tags = {
                    "source=Here maps" }),
            @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_2), tags = {
                    "source=Vworld" }) }, edges = {
                            @TestAtlas.Edge(id = "1001000001", coordinates = {
                                    @TestAtlas.Loc(value = TEST_1),
                                    @TestAtlas.Loc(value = TEST_2) }) })
    private Atlas illegalSourceLinkNode;

    public Atlas getIllegalSourceLinkEdge()
    {
        return illegalSourceLinkEdge;
    }

    public Atlas getIllegalSourceLinkNode()
    {
        return illegalSourceLinkNode;
    }
}
