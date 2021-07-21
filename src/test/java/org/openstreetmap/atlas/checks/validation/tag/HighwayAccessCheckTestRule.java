package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;

public class HighwayAccessCheckTestRule extends CoreTestRule
{

    private static final String WAY1_NODE1 = "40.9130354, 29.4700719";
    private static final String WAY1_NODE2 = "40.9123887, 29.4698597";

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=service" }) })
    private Atlas falsePositiveAccessTagIsPermissiveHighwayTagIsWrong;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=private", "highway=motorway" }) })
    private Atlas falsePositiveAccessTagIsWrongHighwayTagIsCorrect;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=private", "highway=service" }) })
    private Atlas falsePositiveAccessTagIsWrongHighwayTagIsWrong;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=service" }) })
    private Atlas falsePositiveAccessTagIsYesHighwayTagIsWrong;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=bridleway" }) })
    private Atlas truePositiveAccessTagIsPermissiveHighwayTagIsBridgleway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=bus_guideway" }) })
    private Atlas truePositiveAccessTagIsPermissiveHighwayTagIsBusguideway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=busway" }) })
    private Atlas truePositiveAccessTagIsPermissiveHighwayTagIsBusway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=cycleway" }) })
    private Atlas truePositiveAccessTagIsPermissiveHighwayTagIsCycleway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=footway" }) })
    private Atlas truePositiveAccessTagIsPermissiveHighwayTagIsFootway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=motorway" }) })
    private Atlas truePositiveAccessTagIsPermissiveHighwayTagIsMotorway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=path" }) })
    private Atlas truePositiveAccessTagIsPermissiveHighwayTagIsPath;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=pedestrian" }) })
    private Atlas truePositiveAccessTagIsPermissiveHighwayTagIsPedestrian;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=raceway" }) })
    private Atlas truePositiveAccessTagIsPermissiveHighwayTagIsRaceway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=steps" }) })
    private Atlas truePositiveAccessTagIsPermissiveHighwayTagIsSteps;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=track" }) })
    private Atlas truePositiveAccessTagIsPermissiveHighwayTagIsTrack;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=permissive", "highway=trunk" }) })
    private Atlas truePositiveAccessTagIsPermissiveHighwayTagIsTrunk;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=bridleway" }) })
    private Atlas truePositiveAccessTagIsYesHighwayTagIsBridgleway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=bus_guideway" }) })
    private Atlas truePositiveAccessTagIsYesHighwayTagIsBusguideway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=busway" }) })
    private Atlas truePositiveAccessTagIsYesHighwayTagIsBusway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=cycleway" }) })
    private Atlas truePositiveAccessTagIsYesHighwayTagIsCycleway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=footway" }) })
    private Atlas truePositiveAccessTagIsYesHighwayTagIsFootway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=motorway" }) })
    private Atlas truePositiveAccessTagIsYesHighwayTagIsMotorway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=path" }) })
    private Atlas truePositiveAccessTagIsYesHighwayTagIsPath;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=pedestrian" }) })
    private Atlas truePositiveAccessTagIsYesHighwayTagIsPedestrian;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=raceway" }) })
    private Atlas truePositiveAccessTagIsYesHighwayTagIsRaceway;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=steps" }) })
    private Atlas truePositiveAccessTagIsYesHighwayTagIsSteps;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=track" }) })
    private Atlas truePositiveAccessTagIsYesHighwayTagIsTrack;

    @TestAtlas(nodes = { @TestAtlas.Node(id = "100001", coordinates = @TestAtlas.Loc(value = WAY1_NODE1)),
            @TestAtlas.Node(id = "100002", coordinates = @TestAtlas.Loc(value = WAY1_NODE2)) },
            edges = {@Edge(id = "100003",
                    coordinates = {@TestAtlas.Loc(value = WAY1_NODE1), @TestAtlas.Loc(value = WAY1_NODE2) },
                    tags = { "access=yes", "highway=trunk" }) })
    private Atlas truePositiveAccessTagIsYesHighwayTagIsTrunk;

    public Atlas falsePositiveAccessTagIsPermissiveHighwayTagIsWrong()
    { return this.falsePositiveAccessTagIsPermissiveHighwayTagIsWrong;}

    public Atlas falsePositiveAccessTagIsWrongHighwayTagIsCorrect()
    { return this.falsePositiveAccessTagIsWrongHighwayTagIsCorrect;}

    public Atlas falsePositiveAccessTagIsWrongHighwayTagIsWrong()
    { return this.falsePositiveAccessTagIsWrongHighwayTagIsWrong; }

    public Atlas falsePositiveAccessTagIsYesHighwayTagIsWrong()
    { return this.falsePositiveAccessTagIsYesHighwayTagIsWrong; }

    public Atlas truePositiveAccessTagIsPermissiveHighwayTagIsBridleway()
    { return this.truePositiveAccessTagIsPermissiveHighwayTagIsBridgleway; }

    public Atlas truePositiveAccessTagIsPermissiveHighwayTagIsBusguideway()
    { return this.truePositiveAccessTagIsPermissiveHighwayTagIsBusguideway; }

    public Atlas truePositiveAccessTagIsPermissiveHighwayTagIsBusway()
    { return this.truePositiveAccessTagIsPermissiveHighwayTagIsBusway; }

    public Atlas truePositiveAccessTagIsPermissiveHighwayTagIsCycleway()
    { return this.truePositiveAccessTagIsPermissiveHighwayTagIsCycleway; }

    public Atlas truePositiveAccessTagIsPermissiveHighwayTagIsFootway()
    { return this.truePositiveAccessTagIsPermissiveHighwayTagIsFootway; }

    public Atlas truePositiveAccessTagIsPermissiveHighwayTagIsMotorway()
    { return this.truePositiveAccessTagIsPermissiveHighwayTagIsMotorway; }

    public Atlas truePositiveAccessTagIsPermissiveHighwayTagIsPath()
    { return this.truePositiveAccessTagIsPermissiveHighwayTagIsPath; }

    public Atlas truePositiveAccessTagIsPermissiveHighwayTagIsPedestrian()
    { return this.truePositiveAccessTagIsPermissiveHighwayTagIsPedestrian; }

    public Atlas truePositiveAccessTagIsPermissiveHighwayTagIsRaceway()
    { return this.truePositiveAccessTagIsPermissiveHighwayTagIsRaceway; }

    public Atlas truePositiveAccessTagIsPermissiveHighwayTagIsSteps()
    { return this.truePositiveAccessTagIsPermissiveHighwayTagIsSteps; }

    public Atlas truePositiveAccessTagIsPermissiveHighwayTagIsTrack()
    { return this.truePositiveAccessTagIsPermissiveHighwayTagIsTrack; }

    public Atlas truePositiveAccessTagIsPermissiveHighwayTagIsTrunk()
    { return this.truePositiveAccessTagIsPermissiveHighwayTagIsTrunk; }

    public Atlas truePositiveAccessTagIsYesHighwayTagIsBridleway()
    { return this.truePositiveAccessTagIsYesHighwayTagIsBridgleway; }

    public Atlas truePositiveAccessTagIsYesHighwayTagIsBusguideway()
    { return this.truePositiveAccessTagIsYesHighwayTagIsBusguideway; }

    public Atlas truePositiveAccessTagIsYesHighwayTagIsBusway()
    { return this.truePositiveAccessTagIsYesHighwayTagIsBusway; }

    public Atlas truePositiveAccessTagIsYesHighwayTagIsCycleway()
    { return this.truePositiveAccessTagIsYesHighwayTagIsCycleway; }

    public Atlas truePositiveAccessTagIsYesHighwayTagIsFootway()
    { return this.truePositiveAccessTagIsYesHighwayTagIsFootway; }

    public Atlas truePositiveAccessTagIsYesHighwayTagIsMotorway()
    { return this.truePositiveAccessTagIsYesHighwayTagIsMotorway; }

    public Atlas truePositiveAccessTagIsYesHighwayTagIsPath()
    { return this.truePositiveAccessTagIsYesHighwayTagIsPath; }

    public Atlas truePositiveAccessTagIsYesHighwayTagIsPedestrian()
    { return this.truePositiveAccessTagIsYesHighwayTagIsPedestrian; }

    public Atlas truePositiveAccessTagIsYesHighwayTagIsRaceway()
    { return this.truePositiveAccessTagIsYesHighwayTagIsRaceway; }

    public Atlas truePositiveAccessTagIsYesHighwayTagIsSteps()
    { return this.truePositiveAccessTagIsYesHighwayTagIsSteps; }

    public Atlas truePositiveAccessTagIsYesHighwayTagIsTrack()
    { return this.truePositiveAccessTagIsYesHighwayTagIsTrack; }

    public Atlas truePositiveAccessTagIsYesHighwayTagIsTrunk()
    { return this.truePositiveAccessTagIsYesHighwayTagIsTrunk; }
}
