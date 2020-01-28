package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author cameron_frenette
 **/
public class UnwalkableWaysCheckTestRule extends CoreTestRule
{
    private static final String WESTLAKE_DENNY = "47.618545, -122.338471";
    private static final String WESTLAKE_JOHN = "47.619585, -122.338485";
    private static final String JOHN_9TH = "47.619703, -122.339772";
    private static final String JOHN_TERRY = "47.619657, -122.337195";
    private static final String TERRY_DENNY = "47.618524, -122.337168";
    private static final String DENNY_9TH = "47.6185454, -122.3398317";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WESTLAKE_DENNY)),
            @Node(coordinates = @Loc(value = WESTLAKE_JOHN)),
            @Node(coordinates = @Loc(value = JOHN_9TH)),
            @Node(coordinates = @Loc(value = JOHN_TERRY)),
            @Node(coordinates = @Loc(value = TERRY_DENNY)),
            @Node(coordinates = @Loc(value = DENNY_9TH)) }, edges = {
                    @Edge(coordinates = { @Loc(value = WESTLAKE_DENNY),
                            @Loc(value = WESTLAKE_JOHN) }, tags = { "highway=secondary" }),
                    @Edge(coordinates = { @Loc(value = JOHN_9TH),
                            @Loc(value = WESTLAKE_JOHN) }, tags = { "highway=motorway",
                                    "name=highway1", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = WESTLAKE_JOHN),
                            @Loc(value = JOHN_TERRY) }, tags = { "highway=motorway",
                                    "name=highway1", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = TERRY_DENNY),
                            @Loc(value = WESTLAKE_DENNY) }, tags = { "highway=motorway",
                                    "name=highway1", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = WESTLAKE_DENNY),
                            @Loc(value = DENNY_9TH) }, tags = { "highway=motorway", "name=highway1",
                                    "oneway=yes" }) })
    private Atlas singleCrossingDoubleAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WESTLAKE_DENNY)),
            @Node(coordinates = @Loc(value = WESTLAKE_JOHN)),
            @Node(coordinates = @Loc(value = JOHN_9TH)),
            @Node(coordinates = @Loc(value = JOHN_TERRY)),
            @Node(coordinates = @Loc(value = TERRY_DENNY)),
            @Node(coordinates = @Loc(value = DENNY_9TH)) }, edges = {
                    @Edge(coordinates = { @Loc(value = WESTLAKE_DENNY),
                            @Loc(value = WESTLAKE_JOHN) }, tags = { "highway=secondary",
                                    "name=someroad", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = JOHN_9TH),
                            @Loc(value = DENNY_9TH) }, tags = { "highway=secondary",
                                    "name=someroad", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = JOHN_9TH),
                            @Loc(value = WESTLAKE_JOHN) }, tags = { "highway=motorway",
                                    "name=highway1", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = WESTLAKE_JOHN),
                            @Loc(value = JOHN_TERRY) }, tags = { "highway=motorway",
                                    "name=highway1", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = TERRY_DENNY),
                            @Loc(value = WESTLAKE_DENNY) }, tags = { "highway=motorway",
                                    "name=highway1", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = WESTLAKE_DENNY),
                            @Loc(value = DENNY_9TH) }, tags = { "highway=motorway", "name=highway1",
                                    "oneway=yes" })

    })
    private Atlas doubleCrossingDoubleAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = WESTLAKE_DENNY)),
            @Node(coordinates = @Loc(value = WESTLAKE_JOHN)),
            @Node(coordinates = @Loc(value = JOHN_9TH)),
            @Node(coordinates = @Loc(value = JOHN_TERRY)),
            @Node(coordinates = @Loc(value = TERRY_DENNY)),
            @Node(coordinates = @Loc(value = DENNY_9TH)) }, edges = {
                    @Edge(coordinates = { @Loc(value = WESTLAKE_DENNY),
                            @Loc(value = WESTLAKE_JOHN) }, tags = { "highway=secondary",
                                    "foot=no" }),
                    @Edge(coordinates = { @Loc(value = JOHN_9TH),
                            @Loc(value = WESTLAKE_JOHN) }, tags = { "highway=motorway",
                                    "name=highway1", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = WESTLAKE_JOHN),
                            @Loc(value = JOHN_TERRY) }, tags = { "highway=motorway",
                                    "name=highway1", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = TERRY_DENNY),
                            @Loc(value = WESTLAKE_DENNY) }, tags = { "highway=motorway",
                                    "name=highway1", "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = WESTLAKE_DENNY),
                            @Loc(value = DENNY_9TH) }, tags = { "highway=motorway", "name=highway1",
                                    "oneway=yes" }),
                    @Edge(coordinates = { @Loc(value = WESTLAKE_DENNY),
                            @Loc(value = DENNY_9TH) }, tags = { "highway=motorway", "name=highway1",
                                    "oneway=yes" }) })
    private Atlas singleFootNoCrossingDoubleAtlas;

    public Atlas getDoubleCrossingDoubleAtlas()
    {
        return this.doubleCrossingDoubleAtlas;
    }

    public Atlas getSingleCrossingDouble()
    {
        return this.singleCrossingDoubleAtlas;
    }

    public Atlas getSingleFootNoCrossingDoubleAtlas()
    {
        return this.singleFootNoCrossingDoubleAtlas;
    }

}
