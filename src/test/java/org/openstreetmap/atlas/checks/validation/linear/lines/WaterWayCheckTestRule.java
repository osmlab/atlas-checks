package org.openstreetmap.atlas.checks.validation.linear.lines;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Edge;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Node;

/**
 * WaterWayCheckTest test rule containing sample test atlases
 *
 * @author Taylor Smock
 */
public class WaterWayCheckTestRule extends CoreTestRule
{
    @TestAtlas(nodes = {
            @Node(id = "-101752", coordinates = @Loc(value = "28.92414725033,-89.42596868149")),
            @Node(id = "-101754", coordinates = @Loc(value = "28.93350332367,-89.41806350699")),
            @Node(id = "-101755", coordinates = @Loc(value = "28.92996541203,-89.40144467423")),
            @Node(id = "-101756", coordinates = @Loc(value = "28.91942958225,-89.39012590165")),
            @Node(id = "-101757", coordinates = @Loc(value = "28.91345356126,-89.40234298952")),
            @Node(id = "-101758", coordinates = @Loc(value = "28.91376809726,-89.43081958402")) }, lines = {
                    @Line(id = "-101782", coordinates = {
                            @Loc(value = "28.92414725033,-89.42596868149"),
                            @Loc(value = "28.93350332367,-89.41806350699"),
                            @Loc(value = "28.92996541203,-89.40144467423"),
                            @Loc(value = "28.91942958225,-89.39012590165"),
                            @Loc(value = "28.91345356126,-89.40234298952"),
                            @Loc(value = "28.91376809726,-89.43081958402"),
                            @Loc(value = "28.92414725033,-89.42596868149") }, tags = {
                                    "waterway=river" }) })
    private Atlas circularWaterway;

    @TestAtlas(nodes = {
            @Node(id = "-111085", coordinates = @Loc(value = "28.9290309538,-89.41969873667")),
            @Node(id = "-111086", coordinates = @Loc(value = "28.93028921782,-89.40553667307")),
            @Node(id = "-111087", coordinates = @Loc(value = "28.92973990295,-89.41222610235")),
            @Node(id = "-111098", coordinates = @Loc(value = "28.94326534819,-89.40321924448")),
            @Node(id = "-111099", coordinates = @Loc(value = "28.92839710691,-89.41269815505")),
            @Node(id = "5244990585", coordinates = @Loc(value = "28.92957086477,-89.41283769511")),
            @Node(id = "5244990586", coordinates = @Loc(value = "28.9573101,-89.3934346")) }, lines = {
                    @Line(id = "-101902", coordinates = {
                            @Loc(value = "28.9290309538,-89.41969873667"),
                            @Loc(value = "28.93028921782,-89.40553667307") }, tags = {
                                    "natural=coastline" }),
                    @Line(id = "542508006", coordinates = { @Loc(value = "28.9573101,-89.3934346"),
                            @Loc(value = "28.94326534819,-89.40321924448"),
                            @Loc(value = "28.92957086477,-89.41283769511"),
                            @Loc(value = "28.92973990295,-89.41222610235"),
                            @Loc(value = "28.92839710691,-89.41269815505") }, tags = {
                                    "name=Southwest Pass", "ref=M 55", "waterway=river",
                                    "wikidata=Q7571332" }) })
    private Atlas coastlineWaterway;

    @TestAtlas(nodes = {
            @Node(id = "233320009", coordinates = @Loc(value = "16.9749207,-88.2220252")),
            @Node(id = "2372034993", coordinates = @Loc(value = "16.9906416,-88.3188021")),
            @Node(id = "2598551761", coordinates = @Loc(value = "16.9331377,-88.2363143")),
            @Node(id = "3545089162", coordinates = @Loc(value = "16.9686766,-88.2187145")) }, lines = {
                    @Line(id = "130590450", coordinates = { @Loc(value = "16.9906416,-88.3188021"),
                            @Loc(value = "16.9686766,-88.2187145") }, tags = {
                                    "alt_name=North Stann Creek", "name=North Stann Creek River",
                                    "source=Bing", "waterway=river" }),
                    @Line(id = "254018602", coordinates = { @Loc(value = "16.9331377,-88.2363143"),
                            @Loc(value = "16.9686766,-88.2187145"),
                            @Loc(value = "16.9749207,-88.2220252") }, tags = { "natural=coastline",
                                    "source=PGS" }) })
    private Atlas coastlineWaterwayConnected;

    @TestAtlas(nodes = {
            @Node(id = "-111085", coordinates = @Loc(value = "28.9290309538,-89.41969873667")),
            @Node(id = "-111086", coordinates = @Loc(value = "28.93028921782,-89.40553667307")),
            @Node(id = "-111087", coordinates = @Loc(value = "28.92973990295,-89.41222610235")),
            @Node(id = "-111098", coordinates = @Loc(value = "28.94326534819,-89.40321924448")),
            @Node(id = "-111099", coordinates = @Loc(value = "28.92839710691,-89.41269815505")),
            @Node(id = "5244990585", coordinates = @Loc(value = "28.92957086477,-89.41283769511")),
            @Node(id = "5244990586", coordinates = @Loc(value = "28.9573101,-89.3934346")) }, lines = {
                    @Line(id = "-101902", coordinates = {
                            @Loc(value = "28.93028921782,-89.40553667307"),
                            @Loc(value = "28.9290309538,-89.41969873667") }, tags = {
                                    "natural=coastline" }),
                    @Line(id = "542508006", coordinates = { @Loc(value = "28.9573101,-89.3934346"),
                            @Loc(value = "28.94326534819,-89.40321924448"),
                            @Loc(value = "28.92973990295,-89.41222610235"),
                            @Loc(value = "28.92957086477,-89.41283769511"),
                            @Loc(value = "28.92839710691,-89.41269815505") }, tags = {
                                    "name=Southwest Pass", "ref=M 55", "waterway=river",
                                    "wikidata=Q7571332" }) })
    private Atlas coastlineWaterwayReversed;

    @TestAtlas(nodes = {
            @Node(id = "-111055", coordinates = @Loc(value = "-2.66048072885,3.07273796082")),
            @Node(id = "-111056", coordinates = @Loc(value = "-2.66048072885,3.60488822937"), tags = {
                    "natural=sinkhole" }),
            @Node(id = "-111057", coordinates = @Loc(value = "-2.50751452055,3.34258964539")),
            @Node(id = "-111058", coordinates = @Loc(value = "-2.8244009971,3.34190299988"), tags = {
                    "natural=sinkhole" }) }, lines = {
                            @Line(id = "-101798", coordinates = {
                                    @Loc(value = "-2.66048072885,3.07273796082"),
                                    @Loc(value = "-2.66048072885,3.60488822937") }, tags = {
                                            "waterway=river" }),
                            @Line(id = "-101799", coordinates = {
                                    @Loc(value = "-2.50751452055,3.34258964539"),
                                    @Loc(value = "-2.8244009971,3.34190299988") }, tags = {
                                            "waterway=river" }) })
    private Atlas crossingWaterways;

    @TestAtlas(nodes = {
            @Node(id = "2136293970", coordinates = @Loc(value = "17.0110643,-88.9352064"), tags = {
                    "natural=sinkhole" }),
            @Node(id = "2465936419", coordinates = @Loc(value = "16.9968515,-88.9201315")),
            @Node(id = "2465936434", coordinates = @Loc(value = "17.0032067,-88.9207212")),
            @Node(id = "2465936435", coordinates = @Loc(value = "17.0037381,-88.9211748")),
            @Node(id = "2465936502", coordinates = @Loc(value = "17.0033866,-88.9208748")),
            @Node(id = "2465936503", coordinates = @Loc(value = "17.0033565,-88.9204809")),
            @Node(id = "2465936534", coordinates = @Loc(value = "16.9983196,-88.9136162")),
            @Node(id = "5697465816", coordinates = @Loc(value = "17.000438,-88.9223765")),
            @Node(id = "5697468126", coordinates = @Loc(value = "17.0035288,-88.9212141")),
            @Node(id = "5697468127", coordinates = @Loc(value = "17.0036881,-88.9211321")) }, lines = {
                    @Line(id = "203621915", coordinates = { @Loc(value = "16.9968515,-88.9201315"),
                            @Loc(value = "17.0032067,-88.9207212"),
                            @Loc(value = "17.0033866,-88.9208748"),
                            @Loc(value = "17.0036881,-88.9211321"),
                            @Loc(value = "17.0037381,-88.9211748"),
                            @Loc(value = "17.0110643,-88.9352064") }, tags = { "waterway=stream" }),
                    @Line(id = "238797678", coordinates = { @Loc(value = "16.9983196,-88.9136162"),
                            @Loc(value = "17.0033565,-88.9204809"),
                            @Loc(value = "17.0033866,-88.9208748"),
                            @Loc(value = "17.0035288,-88.9212141") }, tags = { "waterway=stream" }),
                    @Line(id = "598638239", coordinates = { @Loc(value = "17.000438,-88.9223765"),
                            @Loc(value = "17.0035288,-88.9212141"),
                            @Loc(value = "17.0036881,-88.9211321") }, tags = {
                                    "waterway=stream" }) })
    private Atlas crossingWaterwaysConnected;

    @TestAtlas(nodes = {
            @Node(id = "-111055", coordinates = @Loc(value = "-2.66048072885,3.07273796082")),
            @Node(id = "-111056", coordinates = @Loc(value = "-2.66048072885,3.60488822937"), tags = {
                    "natural=sinkhole" }),
            @Node(id = "-111057", coordinates = @Loc(value = "-2.50751452055,3.34258964539")),
            @Node(id = "-111058", coordinates = @Loc(value = "-2.8244009971,3.34190299988"), tags = {
                    "natural=sinkhole" }) }, lines = {
                            @Line(id = "-101798", coordinates = {
                                    @Loc(value = "-2.66048072885,3.07273796082"),
                                    @Loc(value = "-2.66048072885,3.60488822937") }, tags = {
                                            "waterway=river" }),
                            @Line(id = "-101799", coordinates = {
                                    @Loc(value = "-2.50751452055,3.34258964539"),
                                    @Loc(value = "-2.8244009971,3.34190299988") }, tags = {
                                            "layer=1", "waterway=river" }) })
    private Atlas crossingWaterwaysDifferentLayers;

    @TestAtlas(nodes = {
            @Node(id = "5215738262", coordinates = @Loc(value = "39.2176586,-105.4097737")),
            @Node(id = "5215738336", coordinates = @Loc(value = "39.219347,-105.4093051")) }, lines = {
                    @Line(id = "538934102", coordinates = { @Loc(value = "39.2176586,-105.4097737"),
                            @Loc(value = "39.219347,-105.4093051") }, tags = {
                                    "waterway=stream" }) })
    private Atlas deadendWaterway;

    @TestAtlas(nodes = {
            @Node(id = "5215738262", coordinates = @Loc(value = "39.2176586,-105.4097737")),
            @Node(id = "5215738336", coordinates = @Loc(value = "39.219347,-105.4093051")),
            @Node(id = "5215738538", coordinates = @Loc(value = "39.2211332,-105.4101387")) }, lines = {
                    @Line(id = "538934084", coordinates = { @Loc(value = "39.2211332,-105.4101387"),
                            @Loc(value = "39.219347,-105.4093051") }, tags = { "name=Lost Creek",
                                    "tunnel=yes", "waterway=stream" }),
                    @Line(id = "538934102", coordinates = { @Loc(value = "39.2176586,-105.4097737"),
                            @Loc(value = "39.219347,-105.4093051") }, tags = {
                                    "waterway=stream" }) })
    private Atlas deadendWaterways;

    @TestAtlas(nodes = {
            @Node(id = "1286958997", coordinates = @Loc(value = "43.940881,12.5169471"), tags = {
                    "man_made=manhole", "manhole=drain" }),
            @Node(id = "1286961206", coordinates = @Loc(value = "43.9228377,12.497902")),
            @Node(id = "5278469506", coordinates = @Loc(value = "43.9230335,12.497702")),
            @Node(id = "5548727896", coordinates = @Loc(value = "43.9232093,12.4972691")) }, lines = {
                    @Line(id = "579368984", coordinates = { @Loc(value = "43.9230335,12.497702"),
                            @Loc(value = "43.9228377,12.497902"),
                            @Loc(value = "43.940881,12.5169471") }, tags = { "name=Marano",
                                    "waterway=stream" }),
                    @Line(id = "579368987", coordinates = { @Loc(value = "43.9232093,12.4972691"),
                            @Loc(value = "43.9230335,12.497702") }, tags = { "layer=-1",
                                    "name=Marano", "tunnel=culvert", "waterway=stream" }) })
    private Atlas differingLayersConnectedWaterway;

    @TestAtlas(nodes = {
            @Node(id = "5215738262", coordinates = @Loc(value = "39.2176586,-105.4097737")),
            @Node(id = "5215738336", coordinates = @Loc(value = "39.2193545,-105.4093126")),
            @Node(id = "5215738538", coordinates = @Loc(value = "39.2211332,-105.4101387")),
            @Node(id = "5215773259", coordinates = @Loc(value = "39.2192406,-105.4074739")),
            @Node(id = "5215773273", coordinates = @Loc(value = "39.2175753,-105.4095698")),
            @Node(id = "5215773279", coordinates = @Loc(value = "39.2193873,-105.409484")),
            @Node(id = "5215773282", coordinates = @Loc(value = "39.2183845,-105.4073667")) }, lines = {
                    @Line(id = "538934084", coordinates = { @Loc(value = "39.2211332,-105.4101387"),
                            @Loc(value = "39.2193545,-105.4093126") }, tags = { "name=Lost Creek",
                                    "tunnel=yes", "waterway=stream" }),
                    @Line(id = "538934102", coordinates = { @Loc(value = "39.2176586,-105.4097737"),
                            @Loc(value = "39.2193545,-105.4093126") }, tags = {
                                    "waterway=stream" }),
                    @Line(id = "538940188", coordinates = { @Loc(value = "39.2193873,-105.409484"),
                            @Loc(value = "39.2192406,-105.4074739"),
                            @Loc(value = "39.2183845,-105.4073667"),
                            @Loc(value = "39.2175753,-105.4095698"),
                            @Loc(value = "39.2193873,-105.409484") }, tags = {
                                    "natural=sinkhole" }) })
    private Atlas sinkholeArea;

    @TestAtlas(nodes = {
            @Node(id = "7704038731", coordinates = @Loc(value = "40.7329135,-111.6691476")),
            @Node(id = "7704038746", coordinates = @Loc(value = "40.7376775,-111.6721463"), tags = {
                    "description=The waterway stops being a surface feature here. I cannot see if it is being culverted underground to join the waterway north of I-80. Probably so but I do not know for sure.",
                    "natural=sinkhole" }) }, lines = {
                            @Line(id = "825077302", coordinates = {
                                    @Loc(value = "40.7329135,-111.6691476"),
                                    @Loc(value = "40.7376775,-111.6721463") }, tags = {
                                            "waterway=stream" }) })
    private Atlas sinkholePoint;

    @TestAtlas(nodes = {
            @Node(id = "-105006", coordinates = @Loc(value = "30.92902860424,-80.19023929596")),
            @Node(id = "-105007", coordinates = @Loc(value = "30.93226809162,-80.02647434235")),
            @Node(id = "-105008", coordinates = @Loc(value = "30.87276183185,-80.04570041656")),
            @Node(id = "-105009", coordinates = @Loc(value = "30.96406813792,-80.05462680817")),
            @Node(id = "-105010", coordinates = @Loc(value = "30.97790374137,-79.96776615143")),
            @Node(id = "-105011", coordinates = @Loc(value = "30.86893095441,-79.96433292389")) }, lines = {
                    @Line(id = "-101786", coordinates = {
                            @Loc(value = "30.92902860424,-80.19023929596"),
                            @Loc(value = "30.93226809162,-80.02647434235") }, tags = {
                                    "waterway=river" }) }, areas = {
                                            @Area(id = "-101787", coordinates = {
                                                    @Loc(value = "30.87276183185,-80.04570041656"),
                                                    @Loc(value = "30.96406813792,-80.05462680817"),
                                                    @Loc(value = "30.97790374137,-79.96776615143"),
                                                    @Loc(value = "30.86893095441,-79.96433292389"),
                                                    @Loc(value = "30.87276183185,-80.04570041656") }, tags = {
                                                            "natural=coastline" }) })
    private Atlas waterwayEndingInOceanArea;

    @TestAtlas(nodes = {
            @Node(id = "-105006", coordinates = @Loc(value = "30.92902860424,-80.19023929596")),
            @Node(id = "-105007", coordinates = @Loc(value = "30.93226809162,-80.02647434235")),
            @Node(id = "-105008", coordinates = @Loc(value = "30.87276183185,-80.04570041656")),
            @Node(id = "-105009", coordinates = @Loc(value = "30.96406813792,-80.05462680817")),
            @Node(id = "-105010", coordinates = @Loc(value = "30.97790374137,-79.96776615143")),
            @Node(id = "-105011", coordinates = @Loc(value = "30.86893095441,-79.96433292389")) }, lines = {
                    @Line(id = "-101786", coordinates = {
                            @Loc(value = "30.92902860424,-80.19023929596"),
                            @Loc(value = "30.93226809162,-80.02647434235") }, tags = {
                                    "waterway=river" }) }, areas = {
                                            @Area(id = "-101787", coordinates = {
                                                    @Loc(value = "30.87276183185,-80.04570041656"),
                                                    @Loc(value = "30.96406813792,-80.05462680817"),
                                                    @Loc(value = "30.97790374137,-79.96776615143"),
                                                    @Loc(value = "30.86893095441,-79.96433292389"),
                                                    @Loc(value = "30.87276183185,-80.04570041656") }, tags = {
                                                            "natural=strait" }) })
    private Atlas waterwayEndingInStraitArea;

    @TestAtlas(nodes = {
            @Node(id = "2087680912", coordinates = @Loc(value = "17.1499473,-88.932559"), tags = {
                    "natural=sinkhole" }),
            @Node(id = "2829277378", coordinates = @Loc(value = "17.1465963,-88.9271326")),
            @Node(id = "2829277561", coordinates = @Loc(value = "17.1494322,-88.9299908")),
            @Node(id = "2829277562", coordinates = @Loc(value = "17.149499,-88.9300777")),
            @Node(id = "2829394973", coordinates = @Loc(value = "17.1498467,-88.9295127")),
            @Node(id = "2829394982", coordinates = @Loc(value = "17.1494757,-88.9300148")),
            @Node(id = "2829394983", coordinates = @Loc(value = "17.149467,-88.930036")) }, edges = {
                    @Edge(id = "278577221", coordinates = { @Loc(value = "17.1465963,-88.9271326"),
                            @Loc(value = "17.1494322,-88.9299908"),
                            @Loc(value = "17.149467,-88.930036"),
                            @Loc(value = "17.149499,-88.9300777"),
                            @Loc(value = "17.1499473,-88.932559") }, tags = { "waterway=stream" }),
                    @Edge(id = "278596089", coordinates = { @Loc(value = "17.1498467,-88.9295127"),
                            @Loc(value = "17.1494757,-88.9300148"),
                            @Loc(value = "17.149467,-88.930036") }, tags = { "waterway=stream" }) })
    private Atlas waterwayEndingOnOtherWaterway;

    /**
     * Get a circular waterway (not real)
     *
     * @return An atlas with a circular waterway
     */
    public Atlas getCircularWaterway()
    {
        return this.circularWaterway;
    }

    /**
     * Get a waterway that goes through a coastline
     *
     * @return A waterway that ends in an ocean
     */
    public Atlas getCoastlineWaterway()
    {
        return this.coastlineWaterway;
    }

    /**
     * Get a waterway that is connected to a coastline
     *
     * @return A waterway that ends in on an ocean
     */
    public Atlas getCoastlineWaterwayConnected()
    {
        return this.coastlineWaterwayConnected;
    }

    /**
     * Get a waterway that goes through a coastline, in the wrong direction
     *
     * @return A waterway that starts in an ocean
     */
    public Atlas getCoastlineWaterwayReversed()
    {
        return this.coastlineWaterwayReversed;
    }

    /**
     * Get two crossing waterways on the same layer (layer=0, so no layer tag)
     *
     * @return An invalid crossing waterway example
     */
    public Atlas getCrossingWaterways()
    {
        return this.crossingWaterways;
    }

    /**
     * Get two connected crossing waterways on the same layer (layer=0, so no layer tag)
     *
     * @return An valid connected crossing waterway example
     */
    public Atlas getCrossingWaterwaysConnected()
    {
        return this.crossingWaterwaysConnected;
    }

    /**
     * Get two crossing waterways on different layers
     *
     * @return A valid crossing waterway example
     */
    public Atlas getCrossingWaterwaysDifferentLayers()
    {
        return this.crossingWaterwaysDifferentLayers;
    }

    /**
     * Get dead-end waterway
     *
     * @return An atlas with 1 waterway that deadends
     */
    public Atlas getDeadendWaterway()
    {
        return this.deadendWaterway;
    }

    /**
     * Get dead-end waterways
     *
     * @return An atlas with 2 waterways that deadend into each other
     */
    public Atlas getDeadendWaterways()
    {
        return this.deadendWaterways;
    }

    /**
     * Get two waterways that differ by a layer tag and are connected
     *
     * @return An atlas with two connected waterway that have a different layer
     */
    public Atlas getDifferingLayersConnectedWaterway()
    {
        return this.differingLayersConnectedWaterway;
    }

    /**
     * Get some waterways that end in a sinkhole area
     *
     * @return An atlas with a sinkhole as an area
     */
    public Atlas getSinkholeArea()
    {
        return this.sinkholeArea;
    }

    /**
     * Get a waterway that ends in a sinkhole point
     *
     * @return An atlas with a sinkhole as a point
     */
    public Atlas getSinkholePoint()
    {
        return this.sinkholePoint;
    }

    /**
     * Get a waterway that ends in an ocean
     *
     * @return A waterway ending in an ocean
     */
    public Atlas getWaterwayEndingInOceanArea()
    {
        return this.waterwayEndingInOceanArea;
    }

    /**
     * Get a waterway that ends in a strait
     *
     * @return A waterway ending in a strait
     */
    public Atlas getWaterwayEndingInStraitArea()
    {
        return this.waterwayEndingInStraitArea;
    }

    /**
     * Get a waterway that ends on another waterway
     *
     * @return A waterway that ends on another waterway
     */
    public Atlas getWaterwayEndingOnOtherWaterway()
    {
        return this.waterwayEndingOnOtherWaterway;
    }
}
