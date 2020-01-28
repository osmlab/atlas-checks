package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * {@link ImproperAndUnknownRoadNameCheckTest} test data
 *
 * @author mgostintsev
 */
public class ImproperAndUnknownRoadNameCheckTestRule extends CoreTestRule
{
    public static final String UNKNOWN_EDGE_ID_1 = "100740465";
    public static final String UNKNOWN_EDGE_ID_2 = "101740465";
    public static final String VALID_EDGE_ID_3 = "102740465";
    public static final String VALID_EDGE_ID_4 = "103740465";
    public static final String INVALID_EDGE_ID_5 = "104740465";
    public static final String INVALID_EDGE_ID_6 = "105540465";
    public static final String INVALID_EDGE_ID_7 = "106740465";
    public static final String INVALID_EDGE_ID_8 = "107740465";
    public static final String INVALID_EDGE_ID_9 = "108740465";
    public static final String INVALID_EDGE_ID_10 = "109740465";
    public static final String INVALID_EDGE_ID_11 = "110740465";
    public static final String INVALID_EDGE_ID_12 = "-102740465";

    private static final String ONE = "29.920386, -2.089355";
    private static final String TWO = "29.920535, -2.088497";

    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)) }, edges = {
                    @Edge(id = UNKNOWN_EDGE_ID_1, tags = { "name=unknown" }, coordinates = {
                            @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = UNKNOWN_EDGE_ID_2, tags = { "name=UNKNOWN" }, coordinates = {
                            @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = VALID_EDGE_ID_3, tags = { "name=Seattle Way" }, coordinates = {
                            @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = VALID_EDGE_ID_4, tags = { "name=unknown boulevard" }, coordinates = {
                            @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = INVALID_EDGE_ID_5, tags = { "name=asphalt" }, coordinates = {
                            @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = INVALID_EDGE_ID_6, tags = { "name=viaduct" }, coordinates = {
                            @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = INVALID_EDGE_ID_7, tags = { "name=unpaved" }, coordinates = {
                            @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = INVALID_EDGE_ID_8, tags = { "name=primary" }, coordinates = {
                            @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = INVALID_EDGE_ID_9, tags = { "name=motorway" }, coordinates = {
                            @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = INVALID_EDGE_ID_10, tags = { "alt_name:=motorway" }, coordinates = {
                            @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = INVALID_EDGE_ID_11, tags = {
                            "alt_name:ru=motorway" }, coordinates = { @Loc(value = ONE),
                                    @Loc(value = TWO) }) })
    private Atlas testAtlas;

    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)) }, edges = { @Edge(tags = {
                    "name=ConfigTest" }, coordinates = { @Loc(value = ONE), @Loc(value = TWO) }) })
    private Atlas configAtlas;

    @TestAtlas(nodes = { @Node(id = "1", coordinates = @Loc(value = ONE)),
            @Node(id = "2", coordinates = @Loc(value = TWO)) }, edges = {
                    @Edge(id = VALID_EDGE_ID_3, tags = { "name=Seattle Way" }, coordinates = {
                            @Loc(value = ONE), @Loc(value = TWO) }),
                    @Edge(id = INVALID_EDGE_ID_12, tags = { "name=ConfigTest" }, coordinates = {
                            @Loc(value = ONE), @Loc(value = TWO) }) })
    private Atlas invalidEdgeIdentifier;

    public Atlas configAtlas()
    {
        return this.configAtlas;
    }

    public Set<String> improperAndUnknownEdgeIdentifiers()
    {
        return new HashSet<String>(Arrays.asList(UNKNOWN_EDGE_ID_1, UNKNOWN_EDGE_ID_2,
                INVALID_EDGE_ID_5, INVALID_EDGE_ID_6, INVALID_EDGE_ID_7, INVALID_EDGE_ID_8,
                INVALID_EDGE_ID_9, INVALID_EDGE_ID_10, INVALID_EDGE_ID_11));
    }

    public Atlas inValidEdgeIdentifier()
    {
        return this.invalidEdgeIdentifier;
    }

    public Atlas testAtlas()
    {
        return this.testAtlas;
    }

    public Set<String> validEdgeIdentifiers()
    {
        return new HashSet<String>(Arrays.asList(VALID_EDGE_ID_3, VALID_EDGE_ID_4));
    }
}
