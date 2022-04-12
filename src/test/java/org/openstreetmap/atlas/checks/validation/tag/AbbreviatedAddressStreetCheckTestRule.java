package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link AbbreviatedAddressStreetCheckTest} data generator.
 *
 * @author vlemberg
 */

public class AbbreviatedAddressStreetCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "37.335310,-122.009566";
    private static final String TEST_2 = "37.331471,-122.030481";
    private static final String TEST_3 = "37.325440,-122.033948";
    private static final String TEST_4 = "37.332451,-122.028932";
    private static final String TEST_5 = "37.317585,-122.052138";
    private static final String TEST_6 = "37.390535,-122.031007";

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "1000000001", coordinates = @Loc(value = TEST_1), tags = {
                    "addr:street=Test Street" }),
            @Node(id = "1000000002", coordinates = @Loc(value = TEST_2)),
            @Node(id = "1000000003", coordinates = @Loc(value = TEST_3)),
            @Node(id = "1000000004", coordinates = @Loc(value = TEST_4)),
            @Node(id = "1000000005", coordinates = @Loc(value = TEST_5)) },
            // edges
            edges = {
                    @Edge(id = "1000000007", coordinates = { @Loc(value = TEST_2),
                            @TestAtlas.Loc(value = TEST_3) }, tags = { "addr:street=Test Avenue" }),
                    @Edge(id = "1000000008", coordinates = { @Loc(value = TEST_4),
                            @Loc(value = TEST_5) }, tags = { "addr:street=Test Way NW" }),
                    @Edge(id = "1000000009", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_3) }, tags = { "addr:street=Test Via" }),
                    @Edge(id = "1000000010", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_1) }, tags = { "addr:street=Test Centre" }) })
    private Atlas atlasValidRoadType;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "1000000001", coordinates = @Loc(value = TEST_1), tags = {
                    "addr:street=1St St" }) })
    private Atlas atlasInvalidRoadTypeNumericStreet;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "1000000001", coordinates = @Loc(value = TEST_1), tags = {
                    "addr:street=Rochester St. W." }) })
    private Atlas atlasFalsePositiveCase1;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "1000000001", coordinates = @Loc(value = TEST_1), tags = {
                    "addr:street=Lower Honoapiilani Rd." }) })
    private Atlas atlasFalsePositiveCase2;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "1000000001", coordinates = @Loc(value = TEST_1), tags = {
                    "addr:street=N. Harbor Village W. Dr." }) })
    private Atlas atlasFalsePositiveCase3;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "1000000001", coordinates = @Loc(value = TEST_1), tags = {
                    "addr:street=Fox Run Pkwy." }) })
    private Atlas atlasFalsePositiveCase4;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "1000000001", coordinates = @Loc(value = TEST_1), tags = {
                    "addr:street=Test St" }) })
    private Atlas atlasInvalidRoadTypePoint;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "1000000001", coordinates = @Loc(value = TEST_1), tags = {
                    "addr:street=Test St." }) })
    private Atlas atlasInvalidRoadTypePoint2;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "1000000001", coordinates = @Loc(value = TEST_1)),
            @Node(id = "1000000002", coordinates = @Loc(value = TEST_2)) },
            // edges
            edges = { @Edge(id = "1000000007", coordinates = { @Loc(value = TEST_1),
                    @TestAtlas.Loc(value = TEST_2) }, tags = { "addr:street=Test Blvd" }) })
    private Atlas atlasInvalidRoadTypeWay;

    @TestAtlas(nodes = {
            // nodes
            @Node(id = "1000000001", coordinates = @Loc(value = TEST_1), tags = {
                    "addr:street=Test St NW" }) })
    private Atlas atlasInvalidRoadTypeSuffix;

    public Atlas getFalsePositiveCase1()
    {
        return this.atlasFalsePositiveCase1;
    }

    public Atlas getFalsePositiveCase2()
    {
        return this.atlasFalsePositiveCase2;
    }

    public Atlas getFalsePositiveCase3()
    {
        return this.atlasFalsePositiveCase3;
    }

    public Atlas getFalsePositiveCase4()
    {
        return this.atlasFalsePositiveCase4;
    }

    public Atlas getInvalidRoadTypeNumericStreet()
    {
        return this.atlasInvalidRoadTypeNumericStreet;
    }

    public Atlas getInvalidRoadTypePoint()
    {
        return this.atlasInvalidRoadTypePoint;
    }

    public Atlas getInvalidRoadTypePoint2()
    {
        return this.atlasInvalidRoadTypePoint2;
    }

    public Atlas getInvalidRoadTypeSuffix()
    {
        return this.atlasInvalidRoadTypeSuffix;
    }

    public Atlas getInvalidRoadTypeWay()
    {
        return this.atlasInvalidRoadTypeWay;
    }

    public Atlas getValidRoadType()
    {
        return this.atlasValidRoadType;
    }

}
