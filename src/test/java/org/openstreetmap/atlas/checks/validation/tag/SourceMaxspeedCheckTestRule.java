package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * @author mm-ciub
 */
public class SourceMaxspeedCheckTestRule extends CoreTestRule
{

    private static final String TEST_1 = "37.3314171,-122.0304871";
    private static final String TEST_2 = "37.331547, -122.031065";
    private static final String TEST_3 = "37.331614, -122.030593";
    private static final String TEST_4 = "37.331272, -122.031280";

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_2)) },
            // points
            points = {
                    @TestAtlas.Point(id = "2400000001", coordinates = @TestAtlas.Loc(value = TEST_1), tags = {
                            "source:maxspeed=RO-TST:urban", "iso_country_code=RO" }) })
    private Atlas countryExceptionConfigAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(id = "1", coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(id = "2", coordinates = @TestAtlas.Loc(value = TEST_2)),
                    @TestAtlas.Node(id = "3", coordinates = @TestAtlas.Loc(value = TEST_3)) },
            // points
            points = {
                    @TestAtlas.Point(id = "1000000001", coordinates = @TestAtlas.Loc(value = TEST_4), tags = {
                            "source:maxspeed=sign", "iso_country_code=SGP" }) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1001000001", coordinates = {
                            @TestAtlas.Loc(value = TEST_1),
                            @TestAtlas.Loc(value = TEST_2) }, tags = { "highway=road", "name=John",
                                    "source:maxspeed=US:some value", "iso_country_code=SGP" }),
                    @TestAtlas.Edge(id = "1001000002", coordinates = {
                            @TestAtlas.Loc(value = TEST_2),
                            @TestAtlas.Loc(value = TEST_3) }, tags = { "highway=road",
                                    "source:maxspeed=US:some value", "iso_country_code=SGP",
                                    "name=Smith" }) })
    private Atlas edgeWalkerAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(id = "1", coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(id = "2", coordinates = @TestAtlas.Loc(value = TEST_2)),
                    @TestAtlas.Node(id = "3", coordinates = @TestAtlas.Loc(value = TEST_3)) },
            // points
            points = {
                    @TestAtlas.Point(id = "2400000001", coordinates = @TestAtlas.Loc(value = TEST_4), tags = {
                            "source:maxspeed=zome", "iso_country_code=UK" }) },
            // edges
            edges = { @TestAtlas.Edge(id = "12", coordinates = { @TestAtlas.Loc(value = TEST_1),
                    @TestAtlas.Loc(value = TEST_2) }, tags = { "highway=road", "name=John" }),
                    @TestAtlas.Edge(id = "23", coordinates = { @TestAtlas.Loc(value = TEST_2),
                            @TestAtlas.Loc(value = TEST_3) }, tags = { "highway=road",
                                    "name=Smith" }) })
    private Atlas exceptionCountryAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(id = "1", coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(id = "2", coordinates = @TestAtlas.Loc(value = TEST_2)),
                    @TestAtlas.Node(id = "3", coordinates = @TestAtlas.Loc(value = TEST_3)) },
            // points
            points = {
                    @TestAtlas.Point(id = "1000000001", coordinates = @TestAtlas.Loc(value = TEST_4), tags = {
                            "source:maxspeed=zome", "iso_country_code=SGP" }) },
            // edges
            edges = {
                    @TestAtlas.Edge(id = "1001000001", coordinates = {
                            @TestAtlas.Loc(value = TEST_1),
                            @TestAtlas.Loc(value = TEST_2) }, tags = { "highway=road", "name=John",
                                    "source:maxspeed=US:some value", "iso_country_code=SGP" }),
                    @TestAtlas.Edge(id = "1002000001", coordinates = {
                            @TestAtlas.Loc(value = TEST_2),
                            @TestAtlas.Loc(value = TEST_3) }, tags = { "highway=road",
                                    "name=Smith" }) })
    private Atlas invalidContextAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_2)) },
            // edges
            edges = { @TestAtlas.Edge(id = "1000000001", coordinates = {
                    @TestAtlas.Loc(value = TEST_1), @TestAtlas.Loc(value = TEST_2) }, tags = {
                            "highway=motorway", "source:maxspeed=VV:urban",
                            "iso_country_code=SGP" }) })
    private Atlas invalidCountryCodeAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_2)) },
            // points
            points = {
                    @TestAtlas.Point(id = "2400000001", coordinates = @TestAtlas.Loc(value = TEST_3), tags = {
                            "source:maxspeed=sign at entrance to subdivision",
                            "iso_country_code=SGP" }) },
            // edges
            edges = { @TestAtlas.Edge(id = "1000000001", coordinates = {
                    @TestAtlas.Loc(value = TEST_1), @TestAtlas.Loc(value = TEST_2) }) })
    private Atlas invalidValueAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_2)) },
            // points
            points = {
                    @TestAtlas.Point(id = "2400000001", coordinates = @TestAtlas.Loc(value = TEST_1), tags = {
                            "source=imagery", "iso_country_code=SGP" }) })
    private Atlas noSourceMaxspeedAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_2)) },
            // points
            points = {
                    @TestAtlas.Point(id = "2400000001", coordinates = @TestAtlas.Loc(value = TEST_1), tags = {
                            "source:maxspeed=RO:70", "iso_country_code=RO" }) })
    private Atlas numberContextAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_2)) },
            // points
            points = {
                    @TestAtlas.Point(id = "2400000001", coordinates = @TestAtlas.Loc(value = TEST_1), tags = {
                            "source:maxspeed=sign", "iso_country_code=SGP" }) },
            // edges
            edges = { @TestAtlas.Edge(id = "1000000001", coordinates = {
                    @TestAtlas.Loc(value = TEST_1), @TestAtlas.Loc(value = TEST_2) }, tags = {
                            "highway=motorway", "source:maxspeed=BE-VLG:urban",
                            "iso_country_code=BE" }) })
    private Atlas validAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_2)) },
            // points
            points = {
                    @TestAtlas.Point(id = "2400000001", coordinates = @TestAtlas.Loc(value = TEST_1), tags = {
                            "source:maxspeed=RO:urban", "iso_country_code=RO" }) })
    private Atlas validContextAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_2)) },
            // points
            points = {
                    @TestAtlas.Point(id = "2400000001", coordinates = @TestAtlas.Loc(value = TEST_1), tags = {
                            "source:maxspeed=implied", "iso_country_code=SGP" }) })
    private Atlas validValueImpliedAtlas;

    @TestAtlas(
            // nodes
            nodes = { @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_1)),
                    @TestAtlas.Node(coordinates = @TestAtlas.Loc(value = TEST_2)) },
            // points
            points = {
                    @TestAtlas.Point(id = "2400000001", coordinates = @TestAtlas.Loc(value = TEST_1), tags = {
                            "source:maxspeed=zone", "iso_country_code=SGP" }) },
            // edges
            edges = { @TestAtlas.Edge(id = "1000000001", coordinates = {
                    @TestAtlas.Loc(value = TEST_1), @TestAtlas.Loc(value = TEST_2) }, tags = {
                            "highway=motorway", "source:maxspeed=US:zone:30",
                            "iso_country_code=SGP" }) })
    private Atlas zoneAtlas;

    public Atlas countryExceptionConfigAtlas()
    {
        return this.countryExceptionConfigAtlas;
    }

    public Atlas edgeWalkerAtlas()
    {
        return this.edgeWalkerAtlas;
    }

    public Atlas exceptionCountryAtlas()
    {
        return this.exceptionCountryAtlas;
    }

    public Atlas invalidContextAtlas()
    {
        return this.invalidContextAtlas;
    }

    public Atlas invalidCountryCodeAtlas()
    {
        return this.invalidCountryCodeAtlas;
    }

    public Atlas invalidValueAtlas()
    {
        return this.invalidValueAtlas;
    }

    public Atlas noSourceMaxspeedAtlas()
    {
        return this.noSourceMaxspeedAtlas;
    }

    public Atlas numberContextAtlas()
    {
        return this.numberContextAtlas;
    }

    public Atlas validAtlas()
    {
        return this.validAtlas;
    }

    public Atlas validContextAtlas()
    {
        return this.validContextAtlas;
    }

    public Atlas validValueImpliedAtlas()
    {
        return this.validValueImpliedAtlas;
    }

    public Atlas zoneAtlas()
    {
        return this.zoneAtlas;
    }
}
