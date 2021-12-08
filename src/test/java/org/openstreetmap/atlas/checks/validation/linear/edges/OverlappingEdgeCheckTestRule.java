package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * @author brian_l_davis
 * @author sayana_saithu
 */
public class OverlappingEdgeCheckTestRule extends CoreTestRule
{
    private static final String ONE = "37.7785877, -122.47495";
    private static final String TWO = "37.7786587, -122.473859";
    private static final String THREE = "37.7767922, -122.473706";
    private static final String FOUR = "37.7767352, -122.474839";
    private static final String FIVE = "37.778717, -122.472711";
    private static final String SIX = "37.7768484, -122.472569";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)), @Node(coordinates = @Loc(value = FIVE)),
            @Node(coordinates = @Loc(value = SIX)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ONE), @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }),
                    @Edge(coordinates = { @Loc(value = FIVE), @Loc(value = TWO),
                            @Loc(value = THREE), @Loc(value = SIX) }) })
    private Atlas overlappingMiddleAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = TWO)),
            @Node(coordinates = @Loc(value = THREE)), @Node(coordinates = @Loc(value = FOUR)),
            @Node(coordinates = @Loc(value = SIX)) }, edges = {
                    @Edge(coordinates = { @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }),
                    @Edge(coordinates = { @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = SIX) }) })
    private Atlas overlappingHeadAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FIVE)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }),
                    @Edge(coordinates = { @Loc(value = FIVE), @Loc(value = TWO),
                            @Loc(value = THREE) }) })
    private Atlas overlappingTailAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)), @Node(coordinates = @Loc(value = FIVE)),
            @Node(coordinates = @Loc(value = SIX)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ONE), @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }),
                    @Edge(coordinates = { @Loc(value = FIVE), @Loc(value = TWO),
                            @Loc(value = THREE) }),
                    @Edge(coordinates = { @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = SIX) }) })
    private Atlas multipleOverlappingAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = SIX)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ONE), @Loc(value = TWO),
                            @Loc(value = THREE) }),
                    @Edge(coordinates = { @Loc(value = THREE), @Loc(value = SIX) }) })
    private Atlas nonOverlappingAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)),
            @Node(coordinates = @Loc(value = THREE)) }, edges = { @Edge(coordinates = {
                    @Loc(value = ONE), @Loc(value = TWO), @Loc(value = THREE) }) })
    private Atlas singleEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)) }, edges = {
                    @Edge(id = "521118537000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO) }),
                    @Edge(id = "521118537000001", coordinates = { @Loc(value = TWO),
                            @Loc(value = ONE) }) })
    private Atlas singleOverlappingWayAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)),
            @Node(coordinates = @Loc(value = THREE)) }, edges = {
                    @Edge(id = "521118537000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "highway=pedestrian", "area=yes" }),
                    @Edge(id = "521118538000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=primary" }) })
    private Atlas pedestrianAreaOverlapEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)) }, edges = {
                    @Edge(id = "521118537000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "highway=pedestrian", "area=yes" }),
                    @Edge(id = "521118538000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE), @Loc(value = SIX), @Loc(value = FIVE),
                            @Loc(value = TWO) }, tags = { "highway=pedestrian", "area=yes" }) })
    private Atlas pedestrianAreaOverlapPedestrianAreaAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)) }, edges = {
                    @Edge(id = "521118537000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "highway=pedestrian", "man_made=pier" }),
                    @Edge(id = "521118538000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE), @Loc(value = SIX), @Loc(value = FIVE),
                            @Loc(value = TWO) }, tags = { "highway=pedestrian",
                                    "man_made=pier" }) })
    private Atlas pierOverlapPierAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)) }, edges = {
                    @Edge(id = "521118537000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "highway=pedestrian" }),
                    @Edge(id = "521118538000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE), @Loc(value = SIX), @Loc(value = FIVE),
                            @Loc(value = TWO) }, tags = { "highway=pedestrian" }) })
    private Atlas pedestrianAreaOverlapPedestrianAreaClosedWayAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)), }, edges = {
                    @Edge(id = "521118537000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "highway=pedestrian", "area=yes" }),
                    @Edge(id = "521118538000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=pedestrian" }),
                    @Edge(id = "521118539000000", coordinates = { @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "highway=footway" }) })
    private Atlas pedestrianAreaOverlapPedestrianEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)) }, edges = {
                    @Edge(id = "521118537000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "highway=service", "area=yes" }),
                    @Edge(id = "521118538000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE), @Loc(value = SIX), @Loc(value = FIVE),
                            @Loc(value = TWO) }, tags = { "highway=service", "area=yes" }) })
    private Atlas serviceAreaOverlapServiceAreaAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)),
            @Node(coordinates = @Loc(value = THREE)) }, edges = {
                    @Edge(id = "521118537000000", coordinates = { @Loc(value = ONE),
                            @Loc(value = TWO), @Loc(value = THREE), @Loc(value = FOUR),
                            @Loc(value = ONE) }, tags = { "highway=service", "area=yes" }),
                    @Edge(id = "521118538000000", coordinates = { @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "highway=primary" }) })
    private Atlas serviceAreaOverlapEdgeAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)), @Node(coordinates = @Loc(value = FIVE)),
            @Node(coordinates = @Loc(value = SIX)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ONE), @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "level=1" }),
                    @Edge(coordinates = { @Loc(value = FIVE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "level=1" }),
                    @Edge(coordinates = { @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = SIX) }, tags = { "level=1" }) })
    private Atlas sameLevelOverlappingEdgesAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = ONE)),
            @Node(coordinates = @Loc(value = TWO)), @Node(coordinates = @Loc(value = THREE)),
            @Node(coordinates = @Loc(value = FOUR)), @Node(coordinates = @Loc(value = FIVE)),
            @Node(coordinates = @Loc(value = SIX)) }, edges = {
                    @Edge(coordinates = { @Loc(value = ONE), @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = FOUR) }, tags = { "level=1" }),
                    @Edge(coordinates = { @Loc(value = FIVE), @Loc(value = TWO),
                            @Loc(value = THREE) }, tags = { "level=2" }),
                    @Edge(coordinates = { @Loc(value = TWO), @Loc(value = THREE),
                            @Loc(value = SIX) }, tags = { "level=3" }) })
    private Atlas differentLevelOverlappingEdgesAtlas;

    public Atlas getDifferentLevelOverlappingEdgesAtlas()
    {
        return this.differentLevelOverlappingEdgesAtlas;
    }

    public Atlas getMultipleOverlappingAtlas()
    {
        return this.multipleOverlappingAtlas;
    }

    public Atlas getNonOverlappingAtlas()
    {
        return this.nonOverlappingAtlas;
    }

    public Atlas getOverlappingHeadAtlas()
    {
        return this.overlappingHeadAtlas;
    }

    public Atlas getOverlappingMiddleAtlas()
    {
        return this.overlappingMiddleAtlas;
    }

    public Atlas getOverlappingTailAtlas()
    {
        return this.overlappingTailAtlas;
    }

    public Atlas getPedestrianAreaOverlapEdgeAtlas()
    {
        return this.pedestrianAreaOverlapEdgeAtlas;
    }

    public Atlas getPedestrianAreaOverlapPedestrianAreaAtlas()
    {
        return this.pedestrianAreaOverlapPedestrianAreaAtlas;
    }

    public Atlas getPedestrianAreaOverlapPedestrianAreaClosedWayAtlas()
    {
        return this.pedestrianAreaOverlapPedestrianAreaClosedWayAtlas;
    }

    public Atlas getPedestrianAreaOverlapPedestrianEdgeAtlas()
    {
        return this.pedestrianAreaOverlapPedestrianEdgeAtlas;
    }

    public Atlas getPierOverlapPierAtlas()
    {
        return this.pierOverlapPierAtlas;
    }

    public Atlas getSameLevelOverlappingEdgesAtlas()
    {
        return this.sameLevelOverlappingEdgesAtlas;
    }

    public Atlas getServiceAreaOverlapEdgeAtlas()
    {
        return this.serviceAreaOverlapEdgeAtlas;
    }

    public Atlas getServiceAreaOverlapServiceAreaAtlas()
    {
        return this.serviceAreaOverlapServiceAreaAtlas;
    }

    public Atlas getSingleEdgeAtlas()
    {
        return this.singleEdgeAtlas;
    }

    public Atlas getSingleOverlappingWayAtlas()
    {
        return this.singleOverlappingWayAtlas;
    }
}
