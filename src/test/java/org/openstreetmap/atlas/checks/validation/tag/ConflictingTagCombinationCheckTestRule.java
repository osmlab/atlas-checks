package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area.Known;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * Data generator for {@link ConflictingTagCombinationCheckTest}
 *
 * @author mkalender <<<<<<< HEAD
 */

public class ConflictingTagCombinationCheckTestRule extends CoreTestRule
{
    private static final String STORE = "37.3314171,-122.0304871";
    private static final String COLLEGE_CAMPUS_2 = "37.33531,-122.009566";

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "12345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=*",
                                    "highway=trunk" }),
                    @Edge(id = "-12345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=*" }),
                    @Edge(id = "22345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=alley",
                                    "highway=primary" }),
                    @Edge(id = "32345678000001", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=*",
                                    "highway=motorway" }),
                    @Edge(id = "32345678000002", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=*",
                                    "highway=motorway" }),
                    @Edge(id = "42345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=*",
                                    "waterway=ditch", "highway=secondary" }) })
    private Atlas invalidServiceAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "12345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=*",
                                    "highway=service" }),
                    @Edge(id = "22345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=alley",
                                    "highway=service" }),
                    @Edge(id = "32345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=*", "railway=*" }),
                    @Edge(id = "42345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=*",
                                    "waterway=canal" }) })
    private Atlas validServiceAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "12345678000001", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=trunk",
                                    "natural=*" }),
                    @Edge(id = "12345678000002", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=*", "natural=*" }),
                    @Edge(id = "12345678000003", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=*", "natural=*" }),
                    @Edge(id = "22345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=unclassified",
                                    "natural=water" }) })
    private Atlas invalidHighwayNaturalAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "12345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=*" }),
                    @Edge(id = "22345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=service" }),
                    @Edge(id = "32345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=motorway" }) })
    private Atlas validHighwayNaturalAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "12345678000001", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=service",
                                    "building=*" }),
                    @Edge(id = "12345678000002", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=*", "building=*" }),
                    @Edge(id = "12345678000003", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=*", "building=*" }),
                    @Edge(id = "22345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=unclassified",
                                    "building=yes" }) })
    private Atlas invalidHighwayBuildingAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "12345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=*" }),
                    @Edge(id = "22345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=service" }),
                    @Edge(id = "32345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = {
                                    "highway=motorway" }) }, areas = {
                                            @Area(id = "92345678000000", known = Known.SILICON_VALLEY, tags = {
                                                    "building=yes" }) })
    private Atlas validHighwayBuldingAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "12345678000001", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=track",
                                    "route=ferry" }),
                    @Edge(id = "12345678000002", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=*",
                                    "route=ferry" }),
                    @Edge(id = "12345678000003", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=*",
                                    "route=ferry" }),
                    @Edge(id = "22345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=unclassified",
                                    "route=ferry" }) })
    private Atlas invalidHighwayFerryAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "12345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=*" }),
                    @Edge(id = "22345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=service" }),
                    @Edge(id = "32345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "route=ferry" }) })
    private Atlas validHighwayFerryAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "12345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=*",
                                    "highway=motorway", "natural=wood", "route=ferry",
                                    "building=yes" }),
                    @Edge(id = "-12345678000000", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=*",
                                    "highway=motorway", "natural=wood", "route=ferry",
                                    "building=yes" }),
                    @Edge(id = "22345678000001", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=alley",
                                    "highway=primary", "natural=sand", "route=ferry",
                                    "building=yes" }),
                    @Edge(id = "22345678000002", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=alley",
                                    "highway=primary", "natural=sand", "route=ferry",
                                    "building=yes" }) })
    private Atlas invalidAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "1234567880", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "service=Sixth Avenue",
                                    "highway=residential" }) })
    private Atlas invalidServiceTagValueAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "1234567889", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "construction=service",
                                    "highway=construction" }) })
    private Atlas validNonNavigableHighwayAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "1234567889", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "landuse=basin",
                                    "highway=primary" }) })
    private Atlas invalidHighwayLanduseTagAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, edges = {
                    @Edge(id = "1234567889", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=trunk",
                                    "place=locality" }) })
    private Atlas invalidHighwayPlaceTagAtlas;

    @TestAtlas(nodes = { @Node(coordinates = @Loc(value = STORE)),
            @Node(coordinates = @Loc(value = COLLEGE_CAMPUS_2)) }, lines = {
                    @Line(id = "1234567889", coordinates = { @Loc(value = STORE),
                            @Loc(value = COLLEGE_CAMPUS_2) }, tags = { "highway=construction",
                                    "service=driveway" }) })
    private Atlas validHighwayServiceTagAtlas;

    public Atlas invalidAtlas()
    {
        return this.invalidAtlas;
    }

    public Atlas invalidHighwayBuildingAtlas()
    {
        return this.invalidHighwayBuildingAtlas;
    }

    public Atlas invalidHighwayFerryAtlas()
    {
        return this.invalidHighwayFerryAtlas;
    }

    public Atlas invalidHighwayLanduseTagAtlas()
    {
        return this.invalidHighwayLanduseTagAtlas;
    }

    public Atlas invalidHighwayNaturalAtlas()
    {
        return this.invalidHighwayNaturalAtlas;
    }

    public Atlas invalidHighwayPlaceTagAtlas()
    {
        return this.invalidHighwayPlaceTagAtlas;
    }

    public Atlas invalidServiceAtlas()
    {
        return this.invalidServiceAtlas;
    }

    public Atlas invalidServiceTagValueAtlas()
    {
        return this.invalidServiceTagValueAtlas;
    }

    public Atlas validHighwayBuldingAtlas()
    {
        return this.validHighwayBuldingAtlas;
    }

    public Atlas validHighwayFerryAtlas()
    {
        return this.validHighwayFerryAtlas;
    }

    public Atlas validHighwayNaturalAtlas()
    {
        return this.validHighwayNaturalAtlas;
    }

    public Atlas validHighwayServiceTagAtlas()
    {
        return this.validHighwayServiceTagAtlas;
    }

    public Atlas validNonNavigableHighwayAtlas()
    {
        return this.validNonNavigableHighwayAtlas;
    }

    public Atlas validServiceAtlas()
    {
        return this.validServiceAtlas;
    }
}
