package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Test data for {@link SelfIntersectingPolyLineCheckTest}
 *
 * @author mgostintsev
 */
public class SelfIntersectingPolylineTestCaseRule extends CoreTestRule
{
    public static final String VALID_EDGE_ID = "101740465";
    public static final String VALID_LINE_ID = "102740465";
    public static final String VALID_AREA_ID = "103740465";
    public static final String INVALID_EDGE_ID_1 = "104740465";
    public static final String INVALID_EDGE_ID_2 = "105540465";
    public static final String INVALID_LINE_ID_1 = "106740465";
    public static final String INVALID_LINE_ID_2 = "107740465";
    public static final String INVALID_AREA_ID_1 = "108740465";
    public static final String INVALID_AREA_ID_2 = "109740465";
    private static final String ONE = "-0.8385242, -80.4892702";
    private static final String TWO = "-0.8385546, -80.4891487";
    private static final String THREE = "-0.8386005, -80.4892233";
    private static final String FOUR = "-0.8384783, -80.4891956";
    private static final String FIVE = "-0.83855, -80.48922";
    @TestAtlas(

            nodes = {

                    @Node(id = "1", coordinates = @Loc(value = ONE)),
                    @Node(id = "2", coordinates = @Loc(value = TWO)),
                    @Node(id = "3", coordinates = @Loc(value = THREE)),
                    @Node(id = "4", coordinates = @Loc(value = FOUR)),
                    @Node(id = "5", coordinates = @Loc(value = FIVE)) },

            lines = {
                    // Valid Line with no self-intersections
                    @Line(id = VALID_LINE_ID, coordinates = { @Loc(value = ONE),
                            @Loc(value = THREE), @Loc(value = TWO) }),
                    // Invalid Line with a non-shape self-intersection
                    @Line(id = INVALID_LINE_ID_1, coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR) }),
                    // Invalid Line with a shape-point self-intersection
                    @Line(id = INVALID_LINE_ID_2, coordinates = { @Loc(value = ONE),
                            @Loc(value = FIVE), @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FIVE), @Loc(value = FOUR) }) },

            edges = {
                    // Valid Edge with no self-intersections
                    @Edge(id = VALID_EDGE_ID, coordinates = { @Loc(value = ONE),
                            @Loc(value = THREE), @Loc(value = TWO) }, tags = { "highway=trunk" }),
                    // Invalid Edge with a non-shape self-intersection
                    @Edge(id = INVALID_EDGE_ID_1, coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "highway=trunk" }),
                    // Invalid Edge with a shape-point self-intersection
                    @Edge(id = INVALID_EDGE_ID_2, coordinates = { @Loc(value = ONE),
                            @Loc(value = FIVE), @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FIVE), @Loc(value = FOUR) }, tags = { "highway=trunk" }) },

            areas = {
                    // Valid Area with no self-intersections
                    @Area(id = VALID_AREA_ID, coordinates = { @Loc(value = ONE),
                            @Loc(value = THREE), @Loc(value = TWO), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "leisure=park" }),
                    // Invalid Area with a non shape-point self-intersection
                    @Area(id = INVALID_AREA_ID_1, coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "leisure=park" }),
                    // Invalid Area with a shape point self-intersection
                    @Area(id = INVALID_AREA_ID_2, coordinates = { @Loc(value = ONE),
                            @Loc(value = FIVE), @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FIVE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "leisure=park" }) })

    private Atlas atlas;

    public Atlas getAtlas()
    {
        return this.atlas;
    }

    public Set<String> getInvalidAtlasEntityIdentifiers()
    {
        final Set<String> invalidIdentifiers = new HashSet<>();
        invalidIdentifiers.add(INVALID_LINE_ID_1);
        invalidIdentifiers.add(INVALID_LINE_ID_2);
        invalidIdentifiers.add(INVALID_EDGE_ID_1);
        invalidIdentifiers.add(INVALID_EDGE_ID_2);
        invalidIdentifiers.add(INVALID_AREA_ID_1);
        invalidIdentifiers.add(INVALID_AREA_ID_2);
        return invalidIdentifiers;
    }

    public Set<String> getValidAtlasEntityIdentifiers()
    {
        final Set<String> validIdentifiers = new HashSet<>();
        validIdentifiers.add(VALID_EDGE_ID);
        validIdentifiers.add(VALID_LINE_ID);
        validIdentifiers.add(VALID_AREA_ID);
        return validIdentifiers;
    }

}
