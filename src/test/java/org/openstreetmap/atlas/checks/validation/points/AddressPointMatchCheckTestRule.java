package org.openstreetmap.atlas.checks.validation.points;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * {@link AddressPointMatchCheckTest} data generator
 *
 * @author savannahostrowski
 */

public class AddressPointMatchCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "37.3314171,-122.0304871";
    private static final String TEST_2 = "37.331547, -122.031065";
    private static final String TEST_3 = "37.331614, -122.030593";
    private static final String TEST_4 = "37.331272, -122.031280";
    private static final String TEST_5 = "37.331135, -122.030980";

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = TEST_1)),
                    @Node(id = "2", coordinates = @Loc(value = TEST_2)),
                    @Node(id = "3", coordinates = @Loc(value = TEST_3)) }, points = {
                            @Point(id = "24", coordinates = @Loc(value = TEST_4), tags = {
                                    "addr:housenumber=21", "name=John" }) },
            // edges
            edges = {
                    @Edge(id = "12", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=road", "name=John" }),
                    @Edge(id = "23", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=road", "name=Smith" }) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12", type = "edge", role = "street"),
                    @Member(id = "24", type = "point", role = "house"),
                    @Member(id = "23", type = "edge", role = "street") }, tags = {
                            "type=associatedStreet" }) })
    private Atlas pointWithStreetNameStreetNumberAndAssociatedStreet;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = TEST_1)),
                    @Node(id = "2", coordinates = @Loc(value = TEST_2)),
                    @Node(id = "3", coordinates = @Loc(value = TEST_3)) }, points = {
                            @Point(id = "24", coordinates = @Loc(value = TEST_4), tags = {
                                    "addr:housenumber=21" }) },
            // edges
            edges = {
                    @Edge(id = "12", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=road" }),
                    @Edge(id = "23", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=road" }) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12", type = "edge", role = "street"),
                    @Member(id = "24", type = "point", role = "house"),
                    @Member(id = "23", type = "edge", role = "street") }, tags = {
                            "type=associatedStreet" }) })
    private Atlas pointWithNoStreetNameStreetNumberAndAssociatedStreet;

    @TestAtlas(
            // nodes
            nodes = { @Node(id = "1", coordinates = @Loc(value = TEST_1)),
                    @Node(id = "2", coordinates = @Loc(value = TEST_2)),
                    @Node(id = "3", coordinates = @Loc(value = TEST_3)) }, points = {
                            @Point(id = "24", coordinates = @Loc(value = TEST_4), tags = {
                                    "addr:housenumber=21" }) },
            // edges
            edges = {
                    @Edge(id = "12", coordinates = { @Loc(value = TEST_1),
                            @Loc(value = TEST_2) }, tags = { "highway=road" }),
                    @Edge(id = "23", coordinates = { @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=road" }) },
            // relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12", type = "edge", role = "street"),
                    @Member(id = "23", type = "edge", role = "street") }) })
    private Atlas pointWithNoStreetNameStreetNumberAndNoAssociatedStreet;

    @TestAtlas(points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "addr:housenumber=20",
            "addr:street=Smith" }) })
    private Atlas pointWithStreetNameStreetNumber;

    @TestAtlas(points = {
            @Point(coordinates = @Loc(value = TEST_1), tags = { "addr:housenumber=20" }) })
    private Atlas pointWithStreetNameNoStreetNumberNoCandidates;

    @TestAtlas(points = {
            @Point(coordinates = @Loc(value = TEST_1), tags = { "addr:housenumber=20" }),
            @Point(coordinates = @Loc(value = TEST_2), tags = { "addr:housenumber=25",
                    "addr:street=Smith" }) })
    private Atlas pointWithStreetNameNoStreetNumberPointCandidatesNoDuplicates;

    @TestAtlas(points = {
            @Point(coordinates = @Loc(value = TEST_1), tags = { "addr:housenumber=20" }),

    }, nodes = { @Node(coordinates = @Loc(value = TEST_2)),
            @Node(coordinates = @Loc(value = TEST_3)) }, edges = { @Edge(coordinates = {
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "name=Smith" }), })
    private Atlas pointWithStreetNameNoStreetNumberEdgeCandidatesNoDuplicates;

    @TestAtlas(points = {
            @Point(coordinates = @Loc(value = TEST_1), tags = { "addr:housenumber=20" }),
            @Point(coordinates = @Loc(value = TEST_2), tags = { "addr:housenumber=20",
                    "addr:street=Jones" }),
            @Point(coordinates = @Loc(value = TEST_3), tags = { "addr:housenumber=20",
                    "addr:street=Jones" }),
            @Point(coordinates = @Loc(value = TEST_4), tags = { "addr:housenumber=20",
                    "addr:street=John" }) })
    private Atlas pointWithStreetNameNoStreetNumberPointCandidatesDuplicateNames;

    @TestAtlas(points = { @Point(id = "1234", coordinates = @Loc(value = TEST_1), tags = {
            "addr:housenumber=20" }),

    }, nodes = { @Node(coordinates = @Loc(value = TEST_2)),
            @Node(coordinates = @Loc(value = TEST_3)), @Node(coordinates = @Loc(value = TEST_4)),
            @Node(coordinates = @Loc(value = TEST_5)), }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "name=Jones" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_5) }, tags = {
                            "name=Jones" }) })
    private Atlas pointWithStreetNameNoStreetNumberEdgeCandidatesDuplicateNames;

    @TestAtlas(points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "addr:housenumber=20",
            "addr:street=" }) })
    private Atlas pointWithEmptyStreetNameNoCandidates;

    @TestAtlas(points = {
            @Point(coordinates = @Loc(value = TEST_1), tags = { "addr:housenumber=20",
                    "addr:street=" }),
            @Point(coordinates = @Loc(value = TEST_2), tags = { "addr:housenumber=25",
                    "addr:street=Smith" }) })
    private Atlas pointWithEmptyStreetNamePointCandidatesNoDuplicates;

    @TestAtlas(points = { @Point(coordinates = @Loc(value = TEST_1), tags = { "addr:housenumber=20",
            "addr:street=" }),

    }, nodes = { @Node(coordinates = @Loc(value = TEST_2)),
            @Node(coordinates = @Loc(value = TEST_3)) }, edges = { @Edge(coordinates = {
                    @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = { "name=Smith" }), })
    private Atlas pointWithEmptyStreetNameEdgeCandidatesNoDuplicates;

    @TestAtlas(points = {
            @Point(coordinates = @Loc(value = TEST_1), tags = { "addr:housenumber=20",
                    "addr:street=" }),
            @Point(coordinates = @Loc(value = TEST_2), tags = { "addr:housenumber=20",
                    "addr:street=Jones" }),
            @Point(coordinates = @Loc(value = TEST_3), tags = { "addr:housenumber=20",
                    "addr:street=Jones" }),
            @Point(coordinates = @Loc(value = TEST_4), tags = { "addr:housenumber=20",
                    "addr:street=John" }) })
    private Atlas pointWithEmptyStreetNamePointCandidatesDuplicateNames;

    @TestAtlas(points = { @Point(id = "1234", coordinates = @Loc(value = TEST_1), tags = {
            "addr:housenumber=20", "addr:street=" }),

    }, nodes = { @Node(coordinates = @Loc(value = TEST_2)),
            @Node(coordinates = @Loc(value = TEST_3)), @Node(coordinates = @Loc(value = TEST_4)),
            @Node(coordinates = @Loc(value = TEST_5)), }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "name=Jones" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_5) }, tags = {
                            "name=Jones" }) })
    private Atlas pointWithEmptyStreetNameEdgeCandidatesDuplicateNames;

    public Atlas pointWithStreetNameStreetNumberAndAssociatedStreet()
    {
        return pointWithStreetNameStreetNumberAndAssociatedStreet;
    }

    public Atlas pointWithNoStreetNameStreetNumberAndAssociatedStreet()
    {
        return pointWithNoStreetNameStreetNumberAndAssociatedStreet;
    }

    public Atlas pointWithNoStreetNameStreetNumberAndNoAssociatedStreet()
    {
        return pointWithNoStreetNameStreetNumberAndNoAssociatedStreet;
    }

    public Atlas pointWithStreetNameStreetNumber()
    {
        return pointWithStreetNameStreetNumber;
    }

    public Atlas pointWithStreetNameNoStreetNumberNoCandidates()
    {
        return pointWithStreetNameNoStreetNumberNoCandidates;
    }

    public Atlas pointWithStreetNameNoStreetNumberPointCandidatesNoDuplicates()
    {
        return pointWithStreetNameNoStreetNumberPointCandidatesNoDuplicates;
    }

    public Atlas pointWithStreetNameNoStreetNumberEdgeCandidatesNoDuplicates()
    {
        return pointWithStreetNameNoStreetNumberEdgeCandidatesNoDuplicates;
    }

    public Atlas pointWithStreetNameNoStreetNumberPointCandidatesDuplicateNames()
    {
        return pointWithStreetNameNoStreetNumberPointCandidatesDuplicateNames;
    }

    public Atlas pointWithStreetNameNoStreetNumberEdgeCandidatesDuplicateNames()
    {
        return pointWithStreetNameNoStreetNumberEdgeCandidatesDuplicateNames;
    }

    public Atlas pointWithEmptyStreetNameNoStreetNumberNoCandidates()
    {
        return pointWithEmptyStreetNameNoCandidates;
    }

    public Atlas pointWithEmptyStreetNamePointCandidatesNoDuplicates()
    {
        return pointWithEmptyStreetNamePointCandidatesNoDuplicates;
    }

    public Atlas pointWithEmptyStreetNameEdgeCandidatesNoDuplicates()
    {
        return pointWithEmptyStreetNameEdgeCandidatesNoDuplicates;
    }

    public Atlas pointWithEmptyStreetNamePointCandidatesDuplicateNames()
    {
        return pointWithEmptyStreetNamePointCandidatesDuplicateNames;
    }

    public Atlas pointWithEmptyStreetNameEdgeCandidatesDuplicateNames()
    {
        return pointWithEmptyStreetNameEdgeCandidatesDuplicateNames;
    }

}
