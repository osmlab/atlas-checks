package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link AbbreviatedNameCheckTest} data generator.
 *
 * @author mkalender
 */
public class AbbreviatedNameCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "37.335310,-122.009566";
    private static final String TEST_2 = "37.331471,-122.030481";
    private static final String TEST_3 = "37.325440,-122.033948";
    private static final String TEST_4 = "37.332451,-122.028932";
    private static final String TEST_5 = "37.317585,-122.052138";
    private static final String TEST_6 = "37.390535,-122.031007";

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "1234567891000000", coordinates = @Loc(value = TEST_1), tags = {
                    "name=Test Apt" }),
            @Node(id = "2234567891000000", coordinates = @Loc(value = TEST_2)),
            @Node(id = "3234567891000000", coordinates = @Loc(value = TEST_3)),
            @Node(id = "4234567891000000", coordinates = @Loc(value = TEST_4)),
            @Node(id = "5234567891000000", coordinates = @Loc(value = TEST_5)),
            @Node(id = "6234567891000000", coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = { @Edge(id = "1234567891000000", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2) }, tags = { "highway=secondary", "name=Test St." }),
                    @Edge(id = "-1234567891000000", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_1) }, tags = { "highway=secondary",
                                    "name=Test St." }),
                    @Edge(id = "2234567891000001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=secondary", "name=Test Dr" }),
                    @Edge(id = "2234567891000002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_6) }, tags = { "highway=secondary", "name=Test Dr" }),
                    @Edge(id = "3234567891000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4), @Loc(value = TEST_2) }, tags = {
                                    "highway=secondary", "name=Test Ave" }),
                    @Edge(id = "4234567891000000", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_6) }, tags = { "highway=secondary",
                                    "name=Test Ave. NE" }),
                    @Edge(id = "5234567891000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=secondary", "name=Test Pl" }),
                    @Edge(id = "6234567891000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "highway=secondary",
                                    "name=Test Cir" }) })
    private Atlas atlasWithAbbreviations;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "1234567891000000", coordinates = @Loc(value = TEST_1), tags = {
                    "name=Test Apartment" }),
            @Node(id = "2234567891000000", coordinates = @Loc(value = TEST_2)),
            @Node(id = "3234567891000000", coordinates = @Loc(value = TEST_3)),
            @Node(id = "4234567891000000", coordinates = @Loc(value = TEST_4)),
            @Node(id = "5234567891000000", coordinates = @Loc(value = TEST_5)),
            @Node(id = "6234567891000000", coordinates = @Loc(value = TEST_6)) },
            // edges
            edges = { @Edge(id = "1234567891000000", coordinates = { @Loc(value = TEST_1),
                    @Loc(value = TEST_2) }, tags = { "highway=secondary", "name=Test Street" }),
                    @Edge(id = "-1234567891000000", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_1) }, tags = { "highway=secondary",
                                    "name=Test Street" }),
                    @Edge(id = "2234567891000001", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=secondary",
                                    "name=Test Drive" }),
                    @Edge(id = "2234567891000002", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_6) }, tags = { "highway=secondary",
                                    "name=Test Drive" }),
                    @Edge(id = "3234567891000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4), @Loc(value = TEST_2) }, tags = {
                                    "highway=secondary", "name=Test Avenue" }),
                    @Edge(id = "4234567891000000", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_6) }, tags = { "highway=secondary",
                                    "name=Test Avenue North East" }),
                    @Edge(id = "5234567891000000", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_5) }, tags = { "highway=secondary",
                                    "name=Test Place" }),
                    @Edge(id = "6234567891000000", coordinates = { @Loc(value = TEST_3),
                            @Loc(value = TEST_4) }, tags = { "highway=secondary",
                                    "name=Test Circle" }) })
    private Atlas atlasWithoutAbbreviations;

    public Atlas atlasWithAbbreviations()
    {
        return this.atlasWithAbbreviations;
    }

    public Atlas atlasWithoutAbbreviations()
    {
        return this.atlasWithoutAbbreviations;
    }
}
