package org.openstreetmap.atlas.checks.validation.linear;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Relation;

/**
 * Test data for {@link MalformedPolyLineCheckTest}
 *
 * @author sayas01
 */
public class MalformedPolyLineCheckTestRule extends CoreTestRule
{
    private static final String TEST_3 = "47.2136626201459,-122.441897992465";
    private static final String TEST_4 = "47.2138114677627,-122.440990166979";
    private static final String TEST_5 = "29.2601483, 48.1656914";
    private static final String TEST_6 = "29.2688082, 48.0907369";
    private static final String TEST_7 = "24.9865524, 55.0190518";

    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // Lines
            lines = { @Line(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                    @Loc(value = TEST_4), @Loc(value = TEST_5) }, tags = { "highway=motorway" }),
                    @Line(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "highway=motorway" }) })
    private Atlas maxLengthPolyLineAtlas;
    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_3)),
                    @Node(coordinates = @Loc(value = TEST_4)),
                    @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // Lines
            lines = { @Line(id = "1001000001", coordinates = { @Loc(value = TEST_3),
                    @Loc(value = TEST_4), @Loc(value = TEST_5) }, tags = { "natural=coastline" }),
                    @Line(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                            @Loc(value = TEST_6),
                            @Loc(value = TEST_7) }, tags = { "waterway=river", "natural=water" }) })
    private Atlas complexPolyLineAtlas;
    @TestAtlas(
            // Nodes
            nodes = { @Node(coordinates = @Loc(value = TEST_5)),
                    @Node(coordinates = @Loc(value = TEST_6)),
                    @Node(coordinates = @Loc(value = TEST_7)) },
            // Lines
            lines = { @Line(id = "1002000001", coordinates = { @Loc(value = TEST_5),
                    @Loc(value = TEST_6), @Loc(value = TEST_7) }, tags = { "highway=motorway" }) },
            // Relations
            relations = { @Relation(id = "123", members = {
                    @Relation.Member(id = "1002000001", type = "line", role = RelationTypeTag.MULTIPOLYGON_TYPE) }, tags = {
                            "natural=water" }) })
    private Atlas relationWithWaterTagAtlas;
    @TestAtlas(loadFromTextResource = "MalformedPolyLine.txt.gz")
    private Atlas malformedPolyLineAtlas;

    public Atlas getComplexPolyLineAtlas()
    {
        return this.complexPolyLineAtlas;
    }

    public Atlas getMalformedPolyLineAtlas()
    {
        return this.malformedPolyLineAtlas;
    }

    public Atlas getMaxLengthAtlas()
    {
        return this.maxLengthPolyLineAtlas;
    }

    public Atlas getRelationWithWaterTagAtlas()
    {
        return this.relationWithWaterTagAtlas;
    }
}
