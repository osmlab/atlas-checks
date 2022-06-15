/**
 *
 */
package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author gpogulsky
 */
public class FloatingEdgeCheckTestRule extends CoreTestRule
{
    private static final String TEST_1 = "37.32544,-122.033948";
    private static final String TEST_2 = "37.33531,-122.009566";
    private static final String TEST_3 = "37.3314171,-122.0304871";

    private static final String TEST_4 = "47.6027,-122.3182";
    private static final String TEST_5 = "47.6027,-122.3130";
    private static final String TEST_6 = "47.6008,-122.3130";
    private static final String TEST_7 = "47.6009,-122.3179";

    private static final String TEST_8 = "47.6025,-122.3181";
    private static final String TEST_9 = "47.6011,-122.3177";

    private static final String TEST_10 = "47.2136626201459,-122.443275382856";
    private static final String TEST_11 = "47.2138327316739,-122.44258668766";
    private static final String TEST_12 = "47.2136626201459,-122.441897992465";
    private static final String TEST_13 = "47.2138114677627,-122.440990166979";
    private static final String TEST_14 = "47.2136200921786,-122.44001973284";
    private static final String TEST_15 = "47.2135137721113,-122.439127559518";
    private static final String TEST_16 = "47.2136200921786,-122.438157125378";
    private static final String TEST_17 = "47.2136413561665,-122.437468430183";
    private static final String TEST_18 = "47.2137689399148,-122.436717126333";
    private static final String TEST_19 = "47.2136413561665,-122.436028431137";
    private static final String TEST_20 = "47.2141623212065,-122.443729295599";
    private static final String TEST_21 = "47.2132054427106,-122.44382320858";
    private static final String TEST_22 = "47.2132267068647,-122.435339735941";
    private static final String TEST_23 = "47.2142154806167,-122.435355388105";

    private static final String LOCATION_1 = "1.11,1.11";
    private static final String LOCATION_2 = "1.12,1.12";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)), @Node(coordinates = @Loc(value = TEST_3)),
            @Node(coordinates = @Loc(value = LOCATION_1)),
            @Node(coordinates = @Loc(value = LOCATION_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=SECONDARY" }) }, areas = {
                                    @Area(coordinates = { @Loc(value = TEST_1),
                                            @Loc(value = TEST_2) }, tags = { "aeroway=taxiway" }) })
    private Atlas airportAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)),
            @Node(coordinates = @Loc(value = TEST_3)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=SECONDARY" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=SECONDARY" }) })
    private Atlas connectedEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
            @Node(coordinates = @Loc(value = LOCATION_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=SECONDARY" }) })
    private Atlas floatingEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = LOCATION_1)),
            @Node(coordinates = @Loc(value = LOCATION_2)) }, edges = {
                    @Edge(id = "1", coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=SECONDARY" }),
                    @Edge(id = "-1", coordinates = { @Loc(value = LOCATION_2),
                            @Loc(value = LOCATION_1) }, tags = { "highway=SECONDARY" }) })
    private Atlas floatingBidirectionalEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_4)),
            @Node(coordinates = @Loc(value = TEST_5)), @Node(coordinates = @Loc(value = TEST_6)),
            @Node(coordinates = @Loc(value = TEST_7)), @Node(coordinates = @Loc(value = TEST_8)),
            @Node(coordinates = @Loc(value = TEST_9)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_8), @Loc(value = TEST_9) }, tags = {
                            "highway=SECONDARY" }) }, areas = {
                                    @Area(coordinates = { @Loc(value = TEST_4),
                                            @Loc(value = TEST_5), @Loc(value = TEST_6),
                                            @Loc(value = TEST_7) }, tags = { "aeroway=taxiway" }) })
    private Atlas floatingEdgeInAirportAtlas;

    @TestAtlas(nodes = {
            @Node(coordinates = @Loc(value = LOCATION_1), tags = { "synthetic_boundary_node=YES" }),
            @Node(coordinates = @Loc(value = LOCATION_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=SECONDARY" }) })
    private Atlas syntheticBorderAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)), @Node(coordinates = @Loc(value = TEST_3)),
            @Node(coordinates = @Loc(value = LOCATION_1)),
            @Node(coordinates = @Loc(value = LOCATION_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=SECONDARY" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "highway=SECONDARY" }),
                    @Edge(coordinates = { @Loc(value = LOCATION_1),
                            @Loc(value = LOCATION_2) }, tags = { "highway=SECONDARY" }) })
    private Atlas mixedAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TEST_1)),
            @Node(coordinates = @Loc(value = TEST_2)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "highway=SECONDARY" }) }, lines = {
                                    @Line(coordinates = { @Loc(value = TEST_2),
                                            @Loc(value = TEST_3) }, tags = { "highway=CONSTRUCTION",
                                                    "construction=RESIDENTIAL" }) })
    private Atlas floatingEdgeConstructionAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_10)),
                    @Node(coordinates = @Loc(value = TEST_12)),
                    @Node(coordinates = @Loc(value = TEST_14)),
                    @Node(coordinates = @Loc(value = TEST_16)) },
            // edges
            edges = { @Edge(id = "1000000001", coordinates = { @Loc(value = TEST_10),
                    @Loc(value = TEST_11), @Loc(value = TEST_12) }, tags = { "highway=motorway" }),
                    @Edge(id = "1001000001", coordinates = { @Loc(value = TEST_12),
                            @Loc(value = TEST_13), @Loc(value = TEST_14) }, tags = {
                                    "highway=motorway", "aeroway=taxiway" }),
                    @Edge(id = "1002000001", coordinates = { @Loc(value = TEST_14),
                            @Loc(value = TEST_15),
                            @Loc(value = TEST_16) }, tags = { "aeroway=aerodrome" }) },
            // areas
            lines = { @Line(id = "1000", coordinates = { @Loc(value = TEST_20),
                    @Loc(value = TEST_21), @Loc(value = TEST_22), @Loc(value = TEST_14),
                    @Loc(value = TEST_20) }) },
            // relations
            relations = { @TestAtlas.Relation(members = {
                    @TestAtlas.Relation.Member(id = "1000000001", type = "edge", role = "") }, tags = {
                            "aeroway=aerodrome", "type=multipolygon" }) })

    private Atlas highwayEdgeWithinRelation;

    public Atlas airportAtlas()
    {
        return this.airportAtlas;
    }

    public Atlas connectedEdgeAtlas()
    {
        return this.connectedEdgeAtlas;
    }

    public Atlas floatingBidirectionalEdgeAtlas()
    {
        return this.floatingBidirectionalEdgeAtlas;
    }

    public Atlas floatingEdgeAtlas()
    {
        return this.floatingEdgeAtlas;
    }

    public Atlas floatingEdgeConstructionAtlas()
    {
        return this.floatingEdgeConstructionAtlas;
    }

    public Atlas floatingEdgeInAirportPolygon()
    {
        return this.floatingEdgeInAirportAtlas;
    }

    public Atlas highwayEdgeWithinRelation()
    {
        return this.highwayEdgeWithinRelation;
    }

    public Atlas mixedAtlas()
    {
        return this.mixedAtlas;
    }

    public Atlas syntheticBorderAtlas()
    {
        return this.syntheticBorderAtlas;
    }
}
