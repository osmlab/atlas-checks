package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link MalformedRoundaboutCheckTest} data generator
 *
 * @author savannahostrowski
 * @author bbreithaupt
 */

public class MalformedRoundaboutCheckTestRule extends CoreTestRule
{
    private static final String CLOCKWISE_1 = "38.905336130818505,-77.03197002410889";
    private static final String CLOCKWISE_2 = "38.90558660084624,-77.03158378601074";
    private static final String CLOCKWISE_3 = "38.905995699991145,-77.0318305492401";
    private static final String CLOCKWISE_4 = "38.90582872103308,-77.03239917755127";
    private static final String CLOCKWISE_5 = "38.905494761938684,-77.03236699104309";

    private static final String COUNTER_CLOCKWISE_1 = "38.905361177861046,-77.03205585479736";
    private static final String COUNTER_CLOCKWISE_2 = "38.905528157918816,-77.03158378601074";
    private static final String COUNTER_CLOCKWISE_3 = "38.905937257400495,-77.031809091568";
    private static final String COUNTER_CLOCKWISE_4 = "38.90588716371307,-77.03230261802673";
    private static final String COUNTER_CLOCKWISE_5 = "38.90551980892527,-77.03236699104309";

    // Clockwise roundabout, left driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = CLOCKWISE_5),
                            @Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = CLOCKWISE_4),
                            @Loc(value = CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }) })
    private Atlas clockwiseRoundaboutLeftDrivingAtlas;

    // Clockwise roundabout, right driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = CLOCKWISE_5),
                            @Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = CLOCKWISE_4),
                            @Loc(value = CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }) })
    private Atlas clockwiseRoundaboutRightDrivingAtlas;

    // Counterclockwise roundabout, left driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }) })
    private Atlas counterClockwiseRoundaboutLeftDrivingAtlas;

    // Counterclockwise roundabout, right driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingAtlas;

    // Multi-directional Atlas
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }) })
    private Atlas multiDirectionalRoundaboutAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = CLOCKWISE_5),
                            @Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = CLOCKWISE_4),
                            @Loc(value = CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_1) }, tags = { "iso_country_code=SGP",
                                    "highway=primary" }) })
    private Atlas clockwiseRoundaboutLeftDrivingMissingTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1240", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1241", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1242", coordinates = { @Loc(value = CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1243", coordinates = { @Loc(value = CLOCKWISE_4),
                            @Loc(value = CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1244", coordinates = { @Loc(value = CLOCKWISE_5),
                            @Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }) })
    private Atlas counterClockwiseConnectedDoubleRoundaboutRightDrivingAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_4)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "iso_country_code=USA",
                                    "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1240", coordinates = { @Loc(value = CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingWrongEdgesTagAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_4)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = CLOCKWISE_4) }, tags = { "iso_country_code=USA",
                                    "highway=primary" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingOutsideConnectionAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "iso_country_code=USA",
                                    "highway=path" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingEnclosedPathAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "iso_country_code=USA",
                                    "highway=primary", "bridge=yes" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingEnclosedBridgeAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary", "bridge=yes" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "iso_country_code=USA",
                                    "highway=primary" }) })
    private Atlas counterClockwiseRoundaboutBridgeRightDrivingEnclosedAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary", "oneway=no" }),
                    @Edge(id = "-1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary", "oneway=no" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary", "oneway=no" }),
                    @Edge(id = "-1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary", "oneway=no" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary", "oneway=no" }),
                    @Edge(id = "-1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary", "oneway=no" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary", "oneway=no" }),
                    @Edge(id = "-1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary", "oneway=no" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary", "oneway=no" }),
                    @Edge(id = "-1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary", "oneway=no" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingOneWayNoAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_5)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = CLOCKWISE_5),
                            @Loc(value = CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = CLOCKWISE_4),
                            @Loc(value = CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1239", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }) })
    private Atlas clockwiseRoundaboutLeftDrivingConcaveAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=elevator" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=primary" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingNonCarNavigableAtlas;

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=cycleway" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=cycleway" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=cycleway" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=cycleway" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=USA", "highway=cycleway" }) })
    private Atlas counterClockwiseRoundaboutRightDrivingCyclewayAtlas;

    // Check enclosed navigable road
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    // parts of roundabout
                    @Edge(id = "1234", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_2) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_3) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_4) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = CLOCKWISE_4),
                            @Loc(value = CLOCKWISE_5) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = CLOCKWISE_5),
                            @Loc(value = CLOCKWISE_1) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    // enclosed navigable roads
                    @Edge(id = "4321", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_4) }, tags = { "highway=primary" }) })
    private Atlas enclosedNavigableRoad;

    // Check enclosed navigable road with Area tag.
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    // parts of roundabout
                    @Edge(id = "1234", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_2) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_3) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_4) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = CLOCKWISE_4),
                            @Loc(value = CLOCKWISE_5) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = CLOCKWISE_5),
                            @Loc(value = CLOCKWISE_1) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    // enclosed navigable roads
                    @Edge(id = "4321", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_4) }, tags = { "highway=primary",
                                    "area=yes" }) })
    private Atlas enclosedNavigableRoadArea;

    // Check enclosed MultiLayer navigable road: tunnel, bridge, layer=*
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_3)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = CLOCKWISE_5)) },
            // edges
            edges = {
                    // parts of roundabout
                    @Edge(id = "1234", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_2) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_3) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_4) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = CLOCKWISE_4),
                            @Loc(value = CLOCKWISE_5) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = CLOCKWISE_5),
                            @Loc(value = CLOCKWISE_1) }, tags = { "iso_country_code=USA",
                                    "junction=roundabout", "highway=primary" }),
                    // enclosed multilayer navigable roads
                    @Edge(id = "4321", coordinates = { @Loc(value = CLOCKWISE_1),
                            @Loc(value = CLOCKWISE_4) }, tags = { "highway=primary",
                                    "tunnel=yes" }),
                    @Edge(id = "4322", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_5) }, tags = { "highway=secondary",
                                    "bridge=yes" }),
                    @Edge(id = "4323", coordinates = { @Loc(value = CLOCKWISE_2),
                            @Loc(value = CLOCKWISE_4) }, tags = { "highway=trunk", "layer=1" }),
                    @Edge(id = "4324", coordinates = { @Loc(value = CLOCKWISE_3),
                            @Loc(value = CLOCKWISE_5) }, tags = { "highway=motorway",
                                    "layer=-1" }) })
    private Atlas enclosedMultiLayerNavigableRoad;

    // Check Synthetic Node with Counterclockwise roundabout, left driving country
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_1)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_2)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_3), tags = {
                            "synthetic_boundary_node=YES" }),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_4)),
                    @Node(coordinates = @Loc(value = COUNTER_CLOCKWISE_5)) },
            // edges
            edges = {
                    @Edge(id = "1234", coordinates = { @Loc(value = COUNTER_CLOCKWISE_1),
                            @Loc(value = COUNTER_CLOCKWISE_2) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1235", coordinates = { @Loc(value = COUNTER_CLOCKWISE_2),
                            @Loc(value = COUNTER_CLOCKWISE_3) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1236", coordinates = { @Loc(value = COUNTER_CLOCKWISE_3),
                            @Loc(value = COUNTER_CLOCKWISE_4) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1237", coordinates = { @Loc(value = COUNTER_CLOCKWISE_4),
                            @Loc(value = COUNTER_CLOCKWISE_5) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }),
                    @Edge(id = "1238", coordinates = { @Loc(value = COUNTER_CLOCKWISE_5),
                            @Loc(value = COUNTER_CLOCKWISE_1) }, tags = { "junction=roundabout",
                                    "iso_country_code=SGP", "highway=primary" }) })
    private Atlas syntheticNode;

    public Atlas clockwiseRoundaboutLeftDrivingAtlas()
    {
        return this.clockwiseRoundaboutLeftDrivingAtlas;
    }

    public Atlas clockwiseRoundaboutLeftDrivingConcaveAtlas()
    {
        return this.clockwiseRoundaboutLeftDrivingConcaveAtlas;
    }

    public Atlas clockwiseRoundaboutLeftDrivingMissingTagAtlas()
    {
        return this.clockwiseRoundaboutLeftDrivingMissingTagAtlas;
    }

    public Atlas clockwiseRoundaboutRightDrivingAtlas()
    {
        return this.clockwiseRoundaboutRightDrivingAtlas;
    }

    public Atlas counterClockwiseConnectedDoubleRoundaboutRightDrivingAtlas()
    {
        return this.counterClockwiseConnectedDoubleRoundaboutRightDrivingAtlas;
    }

    public Atlas counterClockwiseRoundaboutBridgeRightDrivingEnclosedAtlas()
    {
        return this.counterClockwiseRoundaboutBridgeRightDrivingEnclosedAtlas;
    }

    public Atlas counterClockwiseRoundaboutLeftDrivingAtlas()
    {
        return this.counterClockwiseRoundaboutLeftDrivingAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingCyclewayAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingCyclewayAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingEnclosedBridgeAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingEnclosedBridgeAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingEnclosedPathAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingEnclosedPathAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingNonCarNavigableAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingNonCarNavigableAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingOneWayNoAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingOneWayNoAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingOutsideConnectionAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingOutsideConnectionAtlas;
    }

    public Atlas counterClockwiseRoundaboutRightDrivingWrongEdgesTagAtlas()
    {
        return this.counterClockwiseRoundaboutRightDrivingWrongEdgesTagAtlas;
    }

    public Atlas enclosedMultiLayerNavigableRoad()
    {
        return this.enclosedMultiLayerNavigableRoad;
    }

    public Atlas enclosedNavigableRoad()
    {
        return this.enclosedNavigableRoad;
    }

    public Atlas enclosedNavigableRoadArea()
    {
        return this.enclosedNavigableRoadArea;
    }

    public Atlas multiDirectionalRoundaboutAtlas()
    {
        return this.multiDirectionalRoundaboutAtlas;
    }

    public Atlas syntheticNode()
    {
        return this.syntheticNode;
    }
}
