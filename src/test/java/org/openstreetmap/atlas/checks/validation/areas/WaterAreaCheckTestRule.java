package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.testing.CoreTestRule;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Area;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Line;
import org.openstreetmap.atlas.utilities.testing.TestAtlas.Loc;

/**
 * Atlases for WaterAreaCheckTest
 *
 * @author Taylor Smock
 */
public class WaterAreaCheckTestRule extends CoreTestRule
{

    // Mango Creek waterway area at 16.5504092, -88.4017051. It is simplified a bit.
    private static final String MCA1 = "16.5531584, -88.4067505";
    private static final String MCA2 = "16.5502345, -88.4020858";
    private static final String MCA3 = "16.5504092, -88.4017051";
    private static final String MCA4 = "16.5506639, -88.4012453";
    private static final String MCA5 = "16.5513499, -88.4013399";
    private static final String MCA6 = "16.551958, -88.4017335";
    private static final String MCA7 = "16.5537792, -88.4067808";
    // The unnamed waterway connected to the Mango Creek area
    private static final String UA1 = "16.5541018, -88.4017725";
    private static final String UA2 = MCA6;
    private static final String UA3 = MCA5;
    private static final String UA4 = "16.5540768, -88.4011307";
    // Downstream Mango Creek waterway area
    private static final String MCB1 = MCA4;
    private static final String MCB2 = MCA3;
    private static final String MCB3 = MCA2;
    private static final String MCB4 = "16.5425772, -88.3997794";
    private static final String MCB5 = "16.5431686, -88.3989688";
    private static final String MCB6 = "16.5442013, -88.3972504";
    // Mango Creek waterway (starting outside of the waterway area)
    private static final String MCW1 = "16.5534022, -88.4073261";
    private static final String MCW2 = "16.5534353, -88.4068047";
    private static final String MCW3 = "16.5511222, -88.402569";
    private static final String MCW4 = MCA3;
    private static final String MCW5 = "16.549232, -88.4011271";
    private static final String MCW6 = "16.5435986, -88.3992923";
    private static final String MCW7 = MCB5;
    // Unnamed waterway (starting outside of the waterway area)
    private static final String UW1 = "16.5542841, -88.4014292";
    private static final String UW2 = "16.5520628, -88.4014292";
    private static final String UW3 = MCW4;

    @TestAtlas(areas = {
            @Area(id = "265672061", coordinates = { @Loc(value = MCA1), @Loc(value = MCA2),
                    @Loc(value = MCA3), @Loc(value = MCA4), @Loc(value = MCA5), @Loc(value = MCA6),
                    @Loc(value = MCA7), @Loc(value = MCA1) }, tags = { "natural=water",
                            "water=river", "waterway=riverbank" }),
            @Area(id = "601378260", coordinates = { @Loc(value = UA1), @Loc(value = UA2),
                    @Loc(value = UA3), @Loc(value = UA4),
                    @Loc(value = UA1) }, tags = { "waterway=riverbank" }),
            @Area(id = "265672060", coordinates = { @Loc(value = MCB1), @Loc(value = MCB2),
                    @Loc(value = MCB3), @Loc(value = MCB4), @Loc(value = MCB5), @Loc(value = MCB6),
                    @Loc(value = MCB1) }, tags = { "natural=water", "water=river" }) }, lines = {
                            @Line(id = "265670075", coordinates = { @Loc(value = MCW1),
                                    @Loc(value = MCW2), @Loc(value = MCW3), @Loc(value = MCW4),
                                    @Loc(value = MCW5), @Loc(value = MCW6),
                                    @Loc(value = MCW7) }, tags = { "name=Mango Creek",
                                            "waterway=river" }),
                            @Line(id = "265625753", coordinates = { @Loc(value = UW1),
                                    @Loc(value = UW2),
                                    @Loc(value = UW3) }, tags = { "waterway=river" }) })
    private Atlas mangoCreekAtlasGood;

    // Make a bad node for the main Mango Creek area
    private static final String MCA6B = "16.55196571318903, -88.40172679447748";
    @TestAtlas(areas = {
            @Area(id = "265672061", coordinates = { @Loc(value = MCA1), @Loc(value = MCA2),
                    @Loc(value = MCA3), @Loc(value = MCA4), @Loc(value = MCA5), @Loc(value = MCA6B),
                    @Loc(value = MCA7), @Loc(value = MCA1) }, tags = { "natural=water",
                            "water=river", "waterway=riverbank" }),
            @Area(id = "601378260", coordinates = { @Loc(value = UA1), @Loc(value = UA2),
                    @Loc(value = UA3), @Loc(value = UA4),
                    @Loc(value = UA1) }, tags = { "waterway=riverbank" }),
            @Area(id = "265672060", coordinates = { @Loc(value = MCB1), @Loc(value = MCB2),
                    @Loc(value = MCB3), @Loc(value = MCB4), @Loc(value = MCB5), @Loc(value = MCB6),
                    @Loc(value = MCB1) }, tags = { "natural=water", "water=river" }) }, lines = {
                            @Line(id = "265670075", coordinates = { @Loc(value = MCW1),
                                    @Loc(value = MCW2), @Loc(value = MCW3), @Loc(value = MCW4),
                                    @Loc(value = MCW5), @Loc(value = MCW6),
                                    @Loc(value = MCW7) }, tags = { "name=Mango Creek",
                                            "waterway=river" }),
                            @Line(id = "265625753", coordinates = { @Loc(value = UW1),
                                    @Loc(value = UW2),
                                    @Loc(value = UW3) }, tags = { "waterway=river" }) })
    private Atlas mangoCreekAtlasBad;

    // Remove a waterway from the Mango Creek area
    @TestAtlas(areas = {
            @Area(id = "265672061", coordinates = { @Loc(value = MCA1), @Loc(value = MCA2),
                    @Loc(value = MCA3), @Loc(value = MCA4), @Loc(value = MCA5), @Loc(value = MCA6),
                    @Loc(value = MCA7), @Loc(value = MCA1) }, tags = { "natural=water",
                            "water=river", "waterway=riverbank" }),
            @Area(id = "601378260", coordinates = { @Loc(value = UA1), @Loc(value = UA2),
                    @Loc(value = UA3), @Loc(value = UA4),
                    @Loc(value = UA1) }, tags = { "waterway=riverbank" }),
            @Area(id = "265672060", coordinates = { @Loc(value = MCB1), @Loc(value = MCB2),
                    @Loc(value = MCB3), @Loc(value = MCB4), @Loc(value = MCB5), @Loc(value = MCB6),
                    @Loc(value = MCB1) }, tags = { "natural=water", "water=river" }) }, lines = {
                            @Line(id = "265670075", coordinates = { @Loc(value = MCW1),
                                    @Loc(value = MCW2), @Loc(value = MCW3), @Loc(value = MCW4),
                                    @Loc(value = MCW5), @Loc(value = MCW6),
                                    @Loc(value = MCW7) }, tags = { "name=Mango Creek",
                                            "waterway=river" }) })
    private Atlas mangoCreekAtlasBadWaterway;

    // False positive anabranch/braided stream on Belize River around 17.2037351,
    // -89.0095902. This "false positive" occured due to the age of the Belize pbf
    // file.
    private static final String BA1 = "17.2053526, -89.009155";
    private static final String BA2 = "17.2050073, -89.0092547";
    private static final String BA3 = "17.2046956, -89.0094701";
    private static final String BA4 = "17.2042592, -89.0095549";
    private static final String BA5 = "17.2039225, -89.0096072";
    private static final String BA6 = "17.2037351, -89.0095902";
    private static final String BA7 = "17.2036013, -89.0094864";
    private static final String BA8 = "17.2034191, -89.009327";
    private static final String BA9 = "17.2032691, -89.0090662";
    private static final String BA10 = "17.2029231, -89.0086411";
    private static final String BA11 = "17.2027293, -89.0083175";
    private static final String BA12 = "17.2026462, -89.0081074";
    private static final String BA13 = "17.2025609, -89.0079866";
    private static final String BA14 = "17.202457, -89.0076702";
    private static final String BA15 = "17.2022656, -89.0072307";
    private static final String BA16 = "17.2021861, -89.0069771";
    private static final String BAA1 = "17.2037668, -89.0096328";
    private static final String BAA2 = "17.2037933, -89.0095804";
    private static final String BAA3 = "17.2036708, -89.0095106";
    private static final String BAA4 = "17.2036708, -89.0095106";
    private static final String BAA5 = "17.2032762, -89.0090171";
    private static final String BAA6 = "17.2031122, -89.0088239";
    private static final String BAA7 = "17.2030268, -89.0087348";
    private static final String BAA8 = "17.2029956, -89.0087023";
    private static final String BAA9 = "17.2029162, -89.0086684";
    private static final String BAA10 = "17.2030969, -89.0089044";
    private static final String BAA11 = "17.2032711, -89.009119";
    private static final String BAA12 = "17.2034094, -89.0093604";
    private static final String BAA13 = "17.2036759, -89.0095964";

    @TestAtlas(areas = { @Area(id = "527986191", coordinates = { @Loc(value = BAA1),
            @Loc(value = BAA2), @Loc(value = BAA3), @Loc(value = BAA4), @Loc(value = BAA5),
            @Loc(value = BAA6), @Loc(value = BAA7), @Loc(value = BAA8), @Loc(value = BAA9),
            @Loc(value = BAA10), @Loc(value = BAA11), @Loc(value = BAA12), @Loc(value = BAA13),
            @Loc(value = BAA1) }, tags = { "natural=water", "water=river" }) }, lines = {
                    @Line(id = "684050065", coordinates = { @Loc(value = BA1), @Loc(value = BA2),
                            @Loc(value = BA3), @Loc(value = BA4), @Loc(value = BA5),
                            @Loc(value = BA6), @Loc(value = BA7), @Loc(value = BA8),
                            @Loc(value = BA9), @Loc(value = BA10), @Loc(value = BA11),
                            @Loc(value = BA12), @Loc(value = BA13), @Loc(value = BA14),
                            @Loc(value = BA15),
                            @Loc(value = BA16) }, tags = { "waterway=stream" }) })
    private Atlas brazilRiverFalsePositive;

    // Overlapping ponds (simplified, originals at 17.2441802, -88.9930825)
    private static final String PO11 = "17.2443388, -88.9931603";
    private static final String PO12 = "17.2443229, -88.9929948";
    private static final String PO13 = "17.2441959, -88.9930348";
    private static final String PO14 = "17.2441959, -88.9931429";
    private static final String PO21 = "17.2443326, -88.9931606";
    private static final String PO22 = "17.2443357, -88.992988";
    private static final String PO23 = "17.2441926, -88.9930271";
    private static final String PO24 = "17.2442051, -88.9931443";
    @TestAtlas(areas = {
            @Area(id = "448755488", coordinates = { @Loc(value = PO11), @Loc(value = PO12),
                    @Loc(value = PO13), @Loc(value = PO14),
                    @Loc(value = PO11) }, tags = { "natural=water", "water=pond" }),
            @Area(id = "455609652", coordinates = { @Loc(value = PO21), @Loc(value = PO22),
                    @Loc(value = PO23), @Loc(value = PO24),
                    @Loc(value = PO21) }, tags = { "natural=water", "water=pond" }) })
    private Atlas overlappingPonds;

    // Make a pier over a pond (uses same coordinates as the overlapping ponds, but
    // one of the ponds is a pier)
    @TestAtlas(areas = {
            @Area(id = "448755488", coordinates = { @Loc(value = PO11), @Loc(value = PO12),
                    @Loc(value = PO13), @Loc(value = PO14),
                    @Loc(value = PO11) }, tags = { "natural=water", "water=pond" }),
            @Area(id = "455609652", coordinates = { @Loc(value = PO21), @Loc(value = PO22),
                    @Loc(value = PO23), @Loc(value = PO24),
                    @Loc(value = PO21) }, tags = { "man_made=pier" }) })
    private Atlas pondAndPier;

    // Mopan River at 17.1692143, -89.1223818
    private static final String MR11 = "17.1692399, -89.1235319";
    private static final String MR12 = "17.1690338, -89.122337";
    private static final String MR13 = "17.1692143, -89.1223818";
    private static final String MR14 = "17.1693629, -89.1235427";
    private static final String MR21 = "17.1697448, -89.1238431";
    private static final String MR22 = "17.1694217, -89.1224173";
    private static final String MR23 = MR13;
    private static final String MR24 = "17.1696525, -89.123768";
    private static final String MR31 = "17.168961, -89.1212887";
    private static final String MR32 = MR22;
    private static final String MR33 = MR13;
    private static final String MR34 = MR12;
    private static final String MR35 = "17.168626, -89.1212833";
    private static final String MRW11 = "17.1692897, -89.1235861";
    private static final String MRW12 = "17.1691097, -89.1219496";
    private static final String MRW21 = "17.1696881, -89.1238189";
    private static final String MRW22 = MRW12;
    private static final String MRW23 = "17.1684911, -89.1207273";
    @TestAtlas(areas = {
            @Area(id = "591224029", coordinates = { @Loc(value = MR11), @Loc(value = MR12),
                    @Loc(value = MR13), @Loc(value = MR14),
                    @Loc(value = MR11) }, tags = { "waterway=riverbank" }),
            @Area(id = "591224028", coordinates = { @Loc(value = MR21), @Loc(value = MR22),
                    @Loc(value = MR23), @Loc(value = MR24),
                    @Loc(value = MR21) }, tags = { "waterway=riverbank" }),
            @Area(id = "481131794", coordinates = { @Loc(value = MR31), @Loc(value = MR32),
                    @Loc(value = MR33), @Loc(value = MR34), @Loc(value = MR35),
                    @Loc(value = MR31) }, tags = { "waterway=riverbank",
                            "water=river" }) }, lines = {
                                    @Line(id = "287672438", coordinates = { @Loc(value = MRW11),
                                            @Loc(value = MRW12) }, tags = { "waterway=river",
                                                    "source=bing", "name=Mopan River" }),
                                    @Line(id = "31955806", coordinates = { @Loc(value = MRW21),
                                            @Loc(value = MRW22), @Loc(value = MRW23) }, tags = {
                                                    "waterway=river", "source=bing",
                                                    "name=Mopan River", "boat=yes" }) })
    private Atlas mopanRiverFalsePositive;

    public Atlas getBrazilRiverFalsePositive()
    {
        return this.brazilRiverFalsePositive;
    }

    public Atlas getMangoCreekAtlasBad()
    {
        return this.mangoCreekAtlasBad;
    }

    public Atlas getMangoCreekAtlasBadWaterway()
    {
        return this.mangoCreekAtlasBadWaterway;
    }

    public Atlas getMangoCreekAtlasGood()
    {
        return this.mangoCreekAtlasGood;
    }

    public Atlas getMopanRiverFalsePositive()
    {
        return this.mopanRiverFalsePositive;
    }

    public Atlas getOverlappingPonds()
    {
        return this.overlappingPonds;
    }

    public Atlas getPondAndPier()
    {
        return this.pondAndPier;
    }
}
