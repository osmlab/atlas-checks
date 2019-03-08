package org.openstreetmap.atlas.checks.flag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Point;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation.Member;

/**
 * {@link CheckFlagTest} data generator
 *
 * @author mkalender
 */
public class CheckFlagTestRule extends CoreTestRule
{
    private static final String TEST_1 = "31.335310,-121.009566";
    private static final String TEST_2 = "32.331417,-122.030487";
    private static final String TEST_3 = "33.325440,-123.033948";
    private static final String TEST_4 = "34.332451,-124.028932";
    private static final String TEST_5 = "35.317585,-125.052138";
    private static final String TEST_6 = "36.390535,-126.031007";

    @TestAtlas(
            // nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_1), tags = { "a-tag=a-value" }),
                    @Node(coordinates = @Loc(value = TEST_2), tags = {
                            "another-tag=another-value" }),
                    @Node(coordinates = @Loc(value = TEST_3), tags = { "third-tag=" }) },
            // points
            points = {
                    @Point(coordinates = @Loc(value = TEST_4), tags = {
                            "sample-tag=sample-value" }),
                    @Point(coordinates = @Loc(value = TEST_5), tags = { "test-tag=sample-value" }),
                    @Point(coordinates = @Loc(value = TEST_6)) },
            // lines
            lines = { @Line(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_6),
                    @Loc(value = TEST_1) }, tags = { "sample-tag=sample-value" }) },
            // edges
            edges = { @Edge(coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                    @Loc(value = TEST_3) }, tags = { "highway=primary" }) },
            // areas
            areas = { @Area(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_2),
                    @Loc(value = TEST_4), @Loc(value = TEST_1),
                    @Loc(value = TEST_6) }, tags = { "building=yes" }) })
    private Atlas atlas;
    @TestAtlas(
            // Nodes
            nodes = {
                    @Node(id = "1", coordinates = @Loc(value = TEST_1), tags = { "a-tag=a-value" }),
                    @Node(id = "2", coordinates = @Loc(value = TEST_2), tags = {
                            "another-tag=another-value" }),
                    @Node(id = "3", coordinates = @Loc(value = TEST_3), tags = { "third-tag=" }),
                    @Node(id = "4", coordinates = @Loc(value = TEST_4), tags = { "fourth-tag=" }),
                    @Node(id = "5", coordinates = @Loc(value = TEST_5), tags = { "fifth-tag=" }),
                    @Node(id = "6", coordinates = @Loc(value = TEST_6), tags = { "sixth-tag=" }), },
            // Points
            points = {
                    @Point(coordinates = @Loc(value = TEST_4), tags = {
                            "sample-tag=sample-value" }),
                    @Point(coordinates = @Loc(value = TEST_5), tags = { "test-tag=sample-value" }),
                    @Point(coordinates = @Loc(value = TEST_6)) },
            // Lines
            lines = { @Line(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_6),
                    @Loc(value = TEST_1) }, tags = { "sample-tag=sample-value" }) },
            // Edges
            edges = {
                    @Edge(id = "12", coordinates = { @Loc(value = TEST_1), @Loc(value = TEST_2),
                            @Loc(value = TEST_3) }, tags = { "highway=primary" }),
                    @Edge(id = "23", coordinates = { @Loc(value = TEST_4), @Loc(value = TEST_5),
                            @Loc(value = TEST_6) }, tags = { "highway=primary" }), },
            // Areas
            areas = { @Area(coordinates = { @Loc(value = TEST_5), @Loc(value = TEST_2),
                    @Loc(value = TEST_4), @Loc(value = TEST_1),
                    @Loc(value = TEST_6) }, tags = { "building=yes" }) },
            // Relations
            relations = { @Relation(id = "123", members = {
                    @Member(id = "12", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_FROM),
                    @Member(id = "2", type = "node", role = RelationTypeTag.RESTRICTION_ROLE_VIA),
                    @Member(id = "23", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_TO) }, tags = {
                            "restriction=no_u_turn" }),
                    @Relation(id = "456", members = {
                            @Member(id = "23", type = "edge", role = RelationTypeTag.RESTRICTION_ROLE_FROM),
                            @Member(id = "1", type = "node", role = RelationTypeTag.RESTRICTION_ROLE_VIA) }, tags = {
                                    "restriction=no_right_turn" }) })
    private Atlas atlasWithRelations;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Atlas getAtlasWithRelations()
    {
        return this.atlasWithRelations;
    }
}
