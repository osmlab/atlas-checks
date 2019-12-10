package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link InconsistentRoadClassificationCheckTest} data generator
 *
 * @author mkalender
 * @author micahnacht
 */
public class InconsistentRoadClassificationCheckTestRule extends CoreTestRule
{
    private static final String WAY_1_LOCATION_1 = "33.720903396606445, 38.65441572712308";
    private static final String WAY_1_LOCATION_2 = "33.71472358703613, 38.66279368133164";
    private static final String WAY_1_LOCATION_4 = "33.691463470458984, 38.691807337062414";
    private static final String WAY_1_LOCATION_5 = "33.68330955505371, 38.70178853088837";
    private static final String WAY_2_LOCATION_1 = "33.712406158447266, 38.67994867266122";
    private static final String WAY_2_LOCATION_3 = "33.69532585144043, 38.67029949585995";
    private static final String WAY_3_LOCATION_1 = "33.71056079864502, 38.66942832560808";
    private static final String WAY_3_LOCATION_3 = "33.68549823760986, 38.69709955289876";
    private static final String WAY_4_LOCATION_1 = "33.685712814331055, 38.69977900653675";
    private static final String INTERSECTION_LOCATION = "33.705453872680664, 38.67478920520597";
    private static final String WAY_5_LOCATION_1 = "47.594488, -122.257879";
    private static final String WAY_5_LOCATION_2 = "47.600276, -122.098578";
    private static final String WAY_6_LOCATION_2 = "47.589296, -121.726033";
    private static final String WAY_7_LOCATION_2 = "47.577932, -121.626166";
    private static final String WAY_8_LOCATION_2 = "47.435378, -122.113299";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = { @Edge(id = "1000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                    @Loc(value = WAY_1_LOCATION_2) }, tags = { "highway=secondary" }) })
    private Atlas singleEdge;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "2000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2) }, tags = { "highway=trunk" }),
                    @Edge(id = "3000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=trunk" }) })
    private Atlas twoEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "4000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2) }, tags = { "highway=trunk" }),
                    @Edge(id = "5000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=secondary" }) })
    private Atlas twoEdgesInconsistentTypes;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "6000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2) }, tags = { "highway=tertiary" }),
                    @Edge(id = "7000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=secondary" }),
                    @Edge(id = "8000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5) }, tags = { "highway=tertiary" }) })
    private Atlas threeEdgesInconsistentTypes;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "9000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2) }, tags = { "highway=primary_link" }),
                    @Edge(id = "10000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=primary" }),
                    @Edge(id = "11000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5) }, tags = { "highway=primary_link" }) })
    private Atlas threeEdgesInconsistentTypesWithLink;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "12000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2) }, tags = { "highway=primary" }),
                    @Edge(id = "13000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=primary_link" }),
                    @Edge(id = "14000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5) }, tags = { "highway=primary" }) })
    private Atlas threeEdgesInconsistentButSimilarTypes;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "15000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2) }, tags = { "highway=primary" }),
                    @Edge(id = "16000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=tertiary_link",
                                    "junction=roundabout" }),
                    @Edge(id = "17000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5) }, tags = { "highway=primary" }) })
    private Atlas threeEdgesInconsistentButRoundabout;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "18000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2) }, tags = { "highway=trunk" }),
                    @Edge(id = "19000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=trunk" }),
                    @Edge(id = "20000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5) }, tags = { "highway=secondary" }) })
    private Atlas threeEdgesInconsistentTypesAtTheEnd;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "21000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2) }, tags = { "highway=unclassified" }),
                    @Edge(id = "22000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=secondary" }),
                    @Edge(id = "23000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5) }, tags = { "highway=unclassified" }) })
    private Atlas threeEdgesInconsistentButLowImportantTypes;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "24000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2) }, tags = { "highway=tertiary_link" }),
                    @Edge(id = "25000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=secondary" }),
                    @Edge(id = "26000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5) }, tags = { "highway=tertiary_link" }) })
    private Atlas threeEdgesInconsistentButLinkType;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "27000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=trunk" }),
                    @Edge(id = "28000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5) }, tags = { "highway=trunk" }),
                    @Edge(id = "29000001", coordinates = { @Loc(value = WAY_2_LOCATION_1),
                            @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_2_LOCATION_3) }, tags = { "highway=secondary" }) })
    private Atlas inconsistentEdgesButDifferentDirection;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = WAY_3_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_3_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "30000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=trunk" }),
                    @Edge(id = "31000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=trunk" }),
                    @Edge(id = "32000001", coordinates = { @Loc(value = WAY_3_LOCATION_1),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=secondary" }),
                    @Edge(id = "33000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_3_LOCATION_3) }, tags = { "highway=secondary" }) })
    private Atlas inconsistentEdgesWithSimilarDirection;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = WAY_3_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_3_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "34000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=trunk" }),
                    @Edge(id = "35000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=trunk" }),
                    @Edge(id = "36000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5) }, tags = { "highway=trunk" }),
                    @Edge(id = "37000001", coordinates = { @Loc(value = WAY_3_LOCATION_1),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=secondary" }),
                    @Edge(id = "38000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_3_LOCATION_3) }, tags = { "highway=secondary" }) })
    private Atlas inconsistentEdgesWithSimilarDirectionButMerge;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = WAY_3_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_3_LOCATION_3)),
                    @Node(coordinates = @Loc(value = WAY_4_LOCATION_1)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "39000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=trunk" }),
                    @Edge(id = "40000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=trunk" }),
                    @Edge(id = "41000001", coordinates = { @Loc(value = WAY_3_LOCATION_1),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=secondary" }),
                    @Edge(id = "42000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_3_LOCATION_3) }, tags = { "highway=secondary" }),
                    @Edge(id = "43000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_4_LOCATION_1) }, tags = {
                                    "highway=secondary_link" }) })
    private Atlas inconsistentFourEdgesWithSimilarDirection;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = WAY_3_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_3_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    @Edge(id = "44000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=trunk" }),
                    @Edge(id = "45000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=trunk" }),
                    @Edge(id = "46000001", coordinates = { @Loc(value = WAY_3_LOCATION_1),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=primary" }),
                    @Edge(id = "47000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_3_LOCATION_3) }, tags = { "highway=primary_link" }) })
    private Atlas inconsistentEdgesWithSimilarDirectionWithLinks;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)) },
            // edges
            edges = {
                    // Way osm_id:1
                    @Edge(id = "48000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2) }, tags = { "highway=tertiary" }),
                    @Edge(id = "48000002", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=secondary" }),
                    @Edge(id = "48000003", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5) }, tags = { "highway=tertiary" }),
                    // Way osm_id:2
                    @Edge(id = "49000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=secondary" }) })
    private Atlas overlappingInconsistentEdge;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_1_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_5)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = WAY_3_LOCATION_1)) },
            // edges
            edges = {
                    @Edge(id = "21000001", coordinates = { @Loc(value = WAY_1_LOCATION_1),
                            @Loc(value = WAY_1_LOCATION_2) }, tags = { "highway=unclassified" }),
                    @Edge(id = "22000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=secondary" }),
                    @Edge(id = "23000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5) }, tags = { "highway=unclassified" }),
                    @Edge(id = "24000001", coordinates = { @Loc(value = WAY_1_LOCATION_5),
                            @Loc(value = WAY_2_LOCATION_1) }, tags = { "highway=unclassified" }),
                    @Edge(id = "25000001", coordinates = { @Loc(value = WAY_1_LOCATION_5),
                            @Loc(value = WAY_2_LOCATION_3) }, tags = { "highway=unclassified" }),
                    @Edge(id = "26000001", coordinates = { @Loc(value = WAY_1_LOCATION_5),
                            @Loc(value = WAY_3_LOCATION_1) }, tags = { "highway=unclassified" }) })
    private Atlas outEdgesGreaterThanTwo;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_3_LOCATION_3)) },
            // edges
            edges = {
                    @Edge(id = "50000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=primary" }),
                    @Edge(id = "51000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5),
                            @Loc(value = WAY_3_LOCATION_3) }, tags = { "highway=secondary" }),
                    @Edge(id = "52000001", coordinates = { @Loc(value = WAY_3_LOCATION_3),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=primary" }) })
    private Atlas loopsBack;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_3_LOCATION_3)) },
            // edges
            edges = {
                    @Edge(id = "50000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=primary" }),
                    @Edge(id = "51000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_1_LOCATION_5),
                            @Loc(value = WAY_3_LOCATION_3) }, tags = { "highway=secondary_link" }),
                    @Edge(id = "52000001", coordinates = { @Loc(value = WAY_3_LOCATION_3),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=primary" }) })
    private Atlas loopsBackLink;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_3_LOCATION_1)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_4_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)) },
            // edges
            edges = {
                    @Edge(id = "53000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = WAY_3_LOCATION_1) }, tags = { "highway=secondary" }),
                    @Edge(id = "54000001", coordinates = { @Loc(value = WAY_3_LOCATION_1),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=primary" }),
                    @Edge(id = "55000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=secondary" }),
                    @Edge(id = "56000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_2_LOCATION_1) }, tags = { "highway=primary" }),
                    @Edge(id = "57000001", coordinates = { @Loc(value = WAY_3_LOCATION_1),
                            @Loc(value = WAY_2_LOCATION_3) }, tags = { "highway=primary" }) })
    private Atlas inconsistentButTwoMatchingEdges;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_3_LOCATION_1)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_1)) },
            // edges
            edges = {
                    @Edge(id = "53000001", coordinates = { @Loc(value = WAY_1_LOCATION_2),
                            @Loc(value = WAY_3_LOCATION_1) }, tags = { "highway=secondary" }),
                    @Edge(id = "54000001", coordinates = { @Loc(value = WAY_3_LOCATION_1),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=primary" }),
                    @Edge(id = "55000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=secondary" }),
                    @Edge(id = "56000001", coordinates = { @Loc(value = WAY_3_LOCATION_1),
                            @Loc(value = WAY_2_LOCATION_1) }, tags = { "highway=primary" }),
                    @Edge(id = "-54000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_3_LOCATION_1) }, tags = { "highway=primary" }) })
    private Atlas inconsistentButOneMatchingEdgeShort;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_5_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_5_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_6_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_7_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_8_LOCATION_2)) },
            // edges
            edges = {
                    @Edge(id = "57000001", coordinates = { @Loc(value = WAY_5_LOCATION_1),
                            @Loc(value = WAY_5_LOCATION_2) }, tags = { "highway=secondary" }),
                    @Edge(id = "58000001", coordinates = { @Loc(value = WAY_5_LOCATION_2),
                            @Loc(value = WAY_6_LOCATION_2) }, tags = { "highway=primary" }),
                    @Edge(id = "59000001", coordinates = { @Loc(value = WAY_6_LOCATION_2),
                            @Loc(value = WAY_7_LOCATION_2) }, tags = { "highway=secondary" }),
                    @Edge(id = "60000001", coordinates = { @Loc(value = WAY_5_LOCATION_2),
                            @Loc(value = WAY_8_LOCATION_2) }, tags = { "highway=primary" }) })
    private Atlas inconsistentButOneMatchingEdgeLong;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_5_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_5_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_6_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_7_LOCATION_2)) },
            // edges
            edges = {
                    @Edge(id = "57000001", coordinates = { @Loc(value = WAY_5_LOCATION_1),
                            @Loc(value = WAY_5_LOCATION_2) }, tags = { "highway=secondary" }),
                    @Edge(id = "58000001", coordinates = { @Loc(value = WAY_5_LOCATION_2),
                            @Loc(value = WAY_6_LOCATION_2) }, tags = { "highway=primary" }),
                    @Edge(id = "59000001", coordinates = { @Loc(value = WAY_6_LOCATION_2),
                            @Loc(value = WAY_7_LOCATION_2) }, tags = { "highway=secondary" }) })
    private Atlas inconsistentButLong;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_5_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_5_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_6_LOCATION_2)),
                    @Node(coordinates = @Loc(value = WAY_7_LOCATION_2)) },
            // edges
            edges = {
                    @Edge(id = "57000001", coordinates = { @Loc(value = WAY_5_LOCATION_1),
                            @Loc(value = WAY_5_LOCATION_2) }, tags = { "highway=secondary" }),
                    @Edge(id = "58000001", coordinates = { @Loc(value = WAY_5_LOCATION_2),
                            @Loc(value = WAY_6_LOCATION_2) }, tags = { "highway=primary_link" }),
                    @Edge(id = "59000001", coordinates = { @Loc(value = WAY_6_LOCATION_2),
                            @Loc(value = WAY_7_LOCATION_2) }, tags = { "highway=secondary" }) })
    private Atlas inconsistentButLongAndLink;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_3_LOCATION_1)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_4_LOCATION_1)),
                    @Node(coordinates = @Loc(value = WAY_2_LOCATION_3)) },
            // edges
            edges = {
                    @Edge(id = "53000001", coordinates = { @Loc(value = WAY_3_LOCATION_1),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=primary" }),
                    @Edge(id = "54000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=secondary" }),
                    @Edge(id = "55000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_4_LOCATION_1) }, tags = { "highway=primary" }),
                    @Edge(id = "56000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=primary" }) })
    private Atlas inconsistentButBypassed;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_3_LOCATION_1)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_4_LOCATION_1)) },
            // edges
            edges = {
                    @Edge(id = "53000001", coordinates = { @Loc(value = WAY_3_LOCATION_1),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=primary" }),
                    @Edge(id = "54000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=secondary_link" }),
                    @Edge(id = "55000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_4_LOCATION_1) }, tags = { "highway=primary" }), })
    private Atlas inconsistentDifferentLinkLevels;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = WAY_3_LOCATION_1)),
                    @Node(coordinates = @Loc(value = INTERSECTION_LOCATION)),
                    @Node(coordinates = @Loc(value = WAY_1_LOCATION_4)),
                    @Node(coordinates = @Loc(value = WAY_4_LOCATION_1)) },
            // edges
            edges = {
                    @Edge(id = "53000001", coordinates = { @Loc(value = WAY_3_LOCATION_1),
                            @Loc(value = INTERSECTION_LOCATION) }, tags = { "highway=primary" }),
                    @Edge(id = "54000001", coordinates = { @Loc(value = INTERSECTION_LOCATION),
                            @Loc(value = WAY_1_LOCATION_4) }, tags = { "highway=secondary" }),
                    @Edge(id = "55000001", coordinates = { @Loc(value = WAY_1_LOCATION_4),
                            @Loc(value = WAY_4_LOCATION_1) }, tags = { "highway=primary" }), })
    private Atlas inconsistentLongLessImportantEdge;

    public Atlas overlappingInconsistentEdge()
    {
        return this.overlappingInconsistentEdge;
    }

    protected Atlas inconsistentButBypassed()
    {
        return this.inconsistentButBypassed;
    }

    protected Atlas inconsistentButLong()
    {
        return this.inconsistentButLong;
    }

    protected Atlas inconsistentButLongAndLink()
    {
        return this.inconsistentButLongAndLink;
    }

    protected Atlas inconsistentButOneMatchingEdgeLong()
    {
        return this.inconsistentButOneMatchingEdgeLong;
    }

    protected Atlas inconsistentButOneMatchingEdgeShort()
    {
        return this.inconsistentButOneMatchingEdgeShort;
    }

    protected Atlas inconsistentButTwoMatchingEdges()
    {
        return this.inconsistentButTwoMatchingEdges;
    }

    protected Atlas inconsistentDifferentLinkLevels()
    {
        return this.inconsistentDifferentLinkLevels;
    }

    protected Atlas inconsistentEdgesButDifferentDirectionAtlas()
    {
        return this.inconsistentEdgesButDifferentDirection;
    }

    protected Atlas inconsistentEdgesWithSimilarDirectionAtlas()
    {
        return this.inconsistentEdgesWithSimilarDirection;
    }

    protected Atlas inconsistentEdgesWithSimilarDirectionAtlasButMerge()
    {
        return this.inconsistentEdgesWithSimilarDirectionButMerge;
    }

    protected Atlas inconsistentEdgesWithSimilarDirectionWithLinksAtlas()
    {
        return this.inconsistentEdgesWithSimilarDirectionWithLinks;
    }

    protected Atlas inconsistentFourEdgesWithSimilarDirectionAtlas()
    {
        return this.inconsistentFourEdgesWithSimilarDirection;
    }

    protected Atlas inconsistentLongLessImportantEdge()
    {
        return this.inconsistentLongLessImportantEdge;
    }

    protected Atlas loopsBack()
    {
        return this.loopsBack;
    }

    protected Atlas loopsBackLink()
    {
        return this.loopsBackLink;
    }

    protected Atlas outEdgesGreaterThanTwo()
    {
        return this.outEdgesGreaterThanTwo;
    }

    protected Atlas singleEdgeAtlas()
    {
        return this.singleEdge;
    }

    protected Atlas threeEdgesInconsistentButLinkTypeAtlas()
    {
        return this.threeEdgesInconsistentButLinkType;
    }

    protected Atlas threeEdgesInconsistentButLowImportantTypesAtlas()
    {
        return this.threeEdgesInconsistentButLowImportantTypes;
    }

    protected Atlas threeEdgesInconsistentButRoundaboutAtlas()
    {
        return this.threeEdgesInconsistentButRoundabout;
    }

    protected Atlas threeEdgesInconsistentButSimilarTypesAtlas()
    {
        return this.threeEdgesInconsistentButSimilarTypes;
    }

    protected Atlas threeEdgesInconsistentTypesAtTheEndAtlas()
    {
        return this.threeEdgesInconsistentTypesAtTheEnd;
    }

    protected Atlas threeEdgesInconsistentTypesAtlas()
    {
        return this.threeEdgesInconsistentTypes;
    }

    protected Atlas threeEdgesInconsistentTypesWithLinkAtlas()
    {
        return this.threeEdgesInconsistentTypesWithLink;
    }

    protected Atlas twoEdgesAtlas()
    {
        return this.twoEdges;
    }

    protected Atlas twoEdgesInconsistentTypesAtlas()
    {
        return this.twoEdgesInconsistentTypes;
    }

}
