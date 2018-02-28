package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;

/**
 * {@link RoundaboutValenceCheckTest} data generator
 *
 * @author savannahostrowski
 */

public class RoundaboutValenceTestCaseRule extends CoreTestRule
{
    // Roundabout edges
    private static final String TEST_1 = "37.3314171,-122.0304871";
    private static final String TEST_2 = "37.32544,-122.033948";
    private static final String TEST_3 = "37.33531,-122.009566";
    private static final String TEST_4 = "37.390535,-122.031007";

    // Non-roundabout edges

    private static final String TEST_5 = "37.331460, -122.032579";
    private static final String TEST_6 = "37.322020, -122.038963";
    private static final String TEST_7 = "37.344847, -121.996437";
    private static final String TEST_8 = "37.410674, -122.020192";

    // Non-exitable roundabout (valence of 0)
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "junction=roundabout" }) })
    private Atlas roundaboutWithValenceZero;

    // Roundabout with valence of 1 (should not be labelled as roundabout but as turning loop)
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_5) }) })
    private Atlas roundaboutWithValenceOne;

    // Roundabout with valence of 2 (this is okay). On conditional threshold
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_5) }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_6) }) })
    private Atlas roundaboutWithValenceTwo;

    // Roundabout with valence of 4 (this is okay)
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_5) }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_6) }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_7) }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_8) }) })
    private Atlas roundaboutWithValenceFour;


    // Roundabout with valence of 10 (should be flagged for inspection). On conditional threshold
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_5) }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_6) }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_7) }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_8) }),
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_8) }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_5) }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_6) }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_7) }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_5) }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_6) }) })
    private Atlas roundaboutWithValenceTen;

    // Roundabout with valence of 11 (should be flagged for inspection). Above conditional threshold
    // of 10.
    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1)),
                    @Node(coordinates = @Loc(value = TEST_2)),
                    @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)) },
            // edges
            edges = {
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_3) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_4) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_1) }, tags = {
                            "junction=roundabout" }),
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_5) }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_6) }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_7) }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_8) }),
                    @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_8) }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_5) }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_6) }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_7) }),
                    @Edge(coordinates = { @Loc(value = TEST_3), @Loc(value = TEST_5) }),
                    @Edge(coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_6) }),
                    @Edge(coordinates = { @Loc(value = TEST_2), @Loc(value = TEST_7) }) })
    private Atlas roundaboutWithValenceEleven;
}

