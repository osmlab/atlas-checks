package org.openstreetmap.atlas.checks.base.checks;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * A simple test rule leveraging the {@link TestAtlas} annotation style of functionality
 * 
 * @author cuthbertm
 */
public class BaseTestRule extends CoreTestRule
{
    private static final String ONE = "29.920386, -2.089355";
    private static final String TWO = "29.920535, -2.088497";
    private static final String THREE = "29.920014, -2.088754";
    private static final String FOUR = "29.915067, -2.065988";
    private static final String FIVE = "29.901898, -2.092338";

    @TestAtlas(nodes = { @TestAtlas.Node(id = "1", coordinates = @TestAtlas.Loc(value = ONE)),
            @TestAtlas.Node(id = "2", coordinates = @TestAtlas.Loc(value = TWO), tags = {
                    "test=ignore" }),
            @TestAtlas.Node(id = "3", coordinates = @TestAtlas.Loc(value = THREE), tags = {
                    "permitlist=true" }),
            @TestAtlas.Node(id = "4", coordinates = @TestAtlas.Loc(value = FOUR), tags = {
                    "permitlist=true", "denylist=true" }),
            @TestAtlas.Node(id = "5", coordinates = @TestAtlas.Loc(value = FIVE)) },

            edges = {
                    // man_made pier will cause this edge to be skipped
                    @TestAtlas.Edge(id = "100", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO),
                            @TestAtlas.Loc(value = THREE) }, tags = { "highway=trunk" }),
                    @TestAtlas.Edge(id = "101", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO), @TestAtlas.Loc(value = FOUR) }, tags = {
                                    "random=na", "highway=trunk" }),
                    @TestAtlas.Edge(id = "102", coordinates = { @TestAtlas.Loc(value = ONE),
                            @TestAtlas.Loc(value = TWO), @TestAtlas.Loc(value = FOUR),
                            @TestAtlas.Loc(value = FIVE) }, tags = { "highway=trunk" }) })

    private Atlas atlas;

    /**
     * Simple getter function for the private atlas
     *
     * @return An {@link Atlas} for unit testing
     */
    public Atlas getAtlas()
    {
        return this.atlas;
    }
}
