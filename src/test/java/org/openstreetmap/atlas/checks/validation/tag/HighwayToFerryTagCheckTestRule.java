package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link HighwayToFerryTagCheckTest} test data
 *
 * @author sayas01
 */
public class HighwayToFerryTagCheckTestRule extends CoreTestRule
{
    private static final String LOCATION_1 = "37.3314171,-122.0304871";
    private static final String LOCATION_2 = "37.3314261,-122.0304871";
    private static final String LOCATION_3 = "37.3314171,-122.0304758";
    private static final String LOCATION_4 = "37.3264092,-122.0211657";
    private static final String LOCATION_5 = "37.3353134,-122.0095644";
    private static final String LOCATION_6 = "37.3314171,-122.0304419";

    @TestAtlas(

            nodes = { @Node(id = "1", coordinates = @Loc(value = LOCATION_1)),
                    @Node(id = "2", coordinates = @Loc(value = LOCATION_2)),
                    @Node(id = "3", coordinates = @Loc(value = LOCATION_3)),
                    @Node(id = "4", coordinates = @Loc(value = LOCATION_4)),
                    @Node(id = "5", coordinates = @Loc(value = LOCATION_5)),
                    @Node(id = "6", coordinates = @Loc(value = LOCATION_6)) },

            edges = {
                    @Edge(id = "1000101", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=trunk" }),
                    @Edge(id = "2000102", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_3) }, tags = { "highway=trunk", "route=path",
                                    "ferry=trunk" }),
                    @Edge(id = "3000102", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_3) }, tags = { "highway=primary", "route=primary",
                                    "ferry=primary" }),
                    @Edge(id = "4000102", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_3) }, tags = { "highway=trunk", "route=ferry",
                                    "ferry=trunk" }) })

    private Atlas sameFerryHighwayTagsAtlas;

    @TestAtlas(

            nodes = { @Node(id = "1", coordinates = @Loc(value = LOCATION_1)),
                    @Node(id = "2", coordinates = @Loc(value = LOCATION_2)),
                    @Node(id = "3", coordinates = @Loc(value = LOCATION_3)),
                    @Node(id = "4", coordinates = @Loc(value = LOCATION_4)),
                    @Node(id = "5", coordinates = @Loc(value = LOCATION_5)),
                    @Node(id = "6", coordinates = @Loc(value = LOCATION_6)) },

            edges = {
                    @Edge(id = "1000101", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=trunk" }),
                    @Edge(id = "2000102", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_3) }, tags = { "highway=trunk", "route=path",
                                    "ferry=YES" }),
                    @Edge(id = "3000102", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_3) }, tags = { "highway=trunk", "route=primary",
                                    "ferry=YES" }),
                    @Edge(id = "4000102", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_3) }, tags = { "highway=trunk", "route=ferry",
                                    "ferry=YES" }) })

    private Atlas differentFerryHighwayTagsAtlas;

    @TestAtlas(

            nodes = { @Node(id = "1", coordinates = @Loc(value = LOCATION_1)),
                    @Node(id = "2", coordinates = @Loc(value = LOCATION_2)),
                    @Node(id = "3", coordinates = @Loc(value = LOCATION_3)),
                    @Node(id = "4", coordinates = @Loc(value = LOCATION_4)),
                    @Node(id = "5", coordinates = @Loc(value = LOCATION_5)),
                    @Node(id = "6", coordinates = @Loc(value = LOCATION_6)) },

            edges = {
                    @Edge(id = "1000101", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=trunk" }),
                    @Edge(id = "2000101", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=motorway",
                                    "route=ferry" }),
                    @Edge(id = "3000101", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=path", "route=ferry" }),
                    @Edge(id = "4000101", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=motorway_link",
                                    "route=ferry" }),
                    @Edge(id = "5000102", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_3) }, tags = { "highway=trunk",
                                    "route=ferry" }) })

    private Atlas highwayAtlas;

    @TestAtlas(

            nodes = { @Node(id = "1", coordinates = @Loc(value = LOCATION_1)),
                    @Node(id = "2", coordinates = @Loc(value = LOCATION_2)),
                    @Node(id = "3", coordinates = @Loc(value = LOCATION_3)),
                    @Node(id = "4", coordinates = @Loc(value = LOCATION_4)),
                    @Node(id = "5", coordinates = @Loc(value = LOCATION_5)),
                    @Node(id = "6", coordinates = @Loc(value = LOCATION_6)) },

            edges = {
                    @Edge(id = "1000101", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=trunk" }),
                    @Edge(id = "1000102", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_3) }, tags = { "route=ferry",
                                    "ferry=primary" }) })

    private Atlas validAtlas;
    @TestAtlas(

            nodes = { @Node(id = "1", coordinates = @Loc(value = LOCATION_1)),
                    @Node(id = "2", coordinates = @Loc(value = LOCATION_2)),
                    @Node(id = "3", coordinates = @Loc(value = LOCATION_3)),
                    @Node(id = "4", coordinates = @Loc(value = LOCATION_4)),
                    @Node(id = "5", coordinates = @Loc(value = LOCATION_5)),
                    @Node(id = "6", coordinates = @Loc(value = LOCATION_6)) },

            edges = {
                    @Edge(id = "1000101", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=trunk" }),
                    @Edge(id = "2000102", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_3) }, tags = { "highway=trunk", "route=ferry",
                                    "ferry=YES" }),
                    @Edge(id = "3000105", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_5) }, tags = { "highway=motorway", "route=ferry",
                                    "ferry=YES" }),
                    @Edge(id = "4000104", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_5) }, tags = { "highway=path", "route=ferry",
                                    "ferry=YES" }),
                    @Edge(id = "5000103", coordinates = { @Loc(value = LOCATION_4),
                            @Loc(value = LOCATION_5) }, tags = { "highway=construction",
                                    "route=ferry", "ferry=YES" }) })

    private Atlas minimumHighwayAtlas;

    public Atlas getDifferentFerryHighwayAtlas()
    {
        return this.differentFerryHighwayTagsAtlas;
    }

    public Atlas getHighwayAtlas()
    {
        return this.highwayAtlas;
    }

    public Atlas getMinimumHighwayAtlas()
    {
        return this.minimumHighwayAtlas;
    }

    public Atlas getSameFerryHighwayAtlas()
    {
        return this.sameFerryHighwayTagsAtlas;
    }

    public Atlas getValidAtlas()
    {
        return this.validAtlas;
    }
}
