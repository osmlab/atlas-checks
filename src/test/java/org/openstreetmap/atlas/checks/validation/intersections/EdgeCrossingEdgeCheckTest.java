package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * @author mkalender, bbreithaupt, vlemberg
 */
public class EdgeCrossingEdgeCheckTest
{
    @Rule
    public EdgeCrossingEdgeCheckTestRule setup = new EdgeCrossingEdgeCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    private final Configuration configuration = ConfigurationResolver.emptyConfiguration();

    @Test
    public void testInvalidCrossingCarNavigation()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlasCarPedestrian(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"car.navigable\":true,\"pedestrian.navigable\":false,\"crossing.car.navigable\":true,\"crossing.pedestrian.navigable\":false,"
                                + "\"indoor.mapping\": \"indoor->*|highway->corridor,steps|level->*\"}}")));

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlas(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"car.navigable\":true,\"pedestrian.navigable\":false,\"crossing.car.navigable\":true,\"crossing.pedestrian.navigable\":false,"
                                + "\"indoor.mapping\": \"indoor->*|highway->corridor,steps|level->*\"}}")));

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(7, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingItemsAtlasArea()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlasArea(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"car.navigable\":true,\"pedestrian.navigable\":false,\"crossing.car.navigable\":true,\"crossing.pedestrian.navigable\":false,"
                                + "\"indoor.mapping\": \"indoor->*|highway->corridor,steps|level->*\"}}")));

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingItemsAtlasCarAndPedestrian()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlasCarPedestrian(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"car.navigable\":true,\"pedestrian.navigable\":true,\"crossing.car.navigable\":true,\"crossing.pedestrian.navigable\":true,"
                                + "\"indoor.mapping\": \"indoor->*|highway->corridor,steps|level->*\"}}")));

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(7, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingItemsAtlasCarAndPedestrianCase2()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlasCarPedestrian(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"car.navigable\":true,\"pedestrian.navigable\":true,\"crossing.car.navigable\":true,\"crossing.pedestrian.navigable\":false,"
                                + "\"indoor.mapping\": \"indoor->*|highway->corridor,steps|level->*\"}}")));

        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidCrossingItemsAtlasCarAndPedestrianCase3()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlasCarPedestrian(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"car.navigable\":false,\"pedestrian.navigable\":false,\"crossing.car.navigable\":true,\"crossing.pedestrian.navigable\":true,"
                                + "\"indoor.mapping\": \"indoor->*|highway->corridor,steps|level->*\"}}")));

        this.verifier.verifyEmpty();
    }

    @Test
    public void testInvalidCrossingItemsAtlasIndoorMapping()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlasIndoorMapping(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"car.navigable\":true,\"pedestrian.navigable\":true,\"crossing.car.navigable\":true,\"crossing.pedestrian.navigable\":true,"
                                + "\"indoor.mapping\": \"indoor->*|highway->corridor,steps|level->*\"}}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInvalidCrossingItemsWithDifferentLayerTagAtlas()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsWithDifferentLayerTagAtlas(),
                new EdgeCrossingEdgeCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInvalidCrossingItemsWithInvalidLayerTagAtlas()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsWithInvalidLayerTagAtlas(),
                new EdgeCrossingEdgeCheck(this.configuration));
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingItemsWithSameLayerTagAtlas()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsWithSameLayerTagAtlas(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"minimum.highway.type\":\"track\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingNonMainItemsAtlas()
    {
        this.verifier.actual(this.setup.invalidCrossingNonMainItemsAtlas(),
                new EdgeCrossingEdgeCheck(this.configuration));
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(7, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingPedestrianNavigation()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlasCarPedestrian(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"car.navigable\":false,\"pedestrian.navigable\":true,\"crossing.car.navigable\":false,\"crossing.pedestrian.navigable\":true,"
                                + "\"indoor.mapping\": \"indoor->*|highway->corridor,steps|level->*\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidDisconnectedEdgeCrossingTest()
    {
        this.verifier.actual(this.setup.invalidDisconnectedEdgeCrossingAtlas(),
                new EdgeCrossingEdgeCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidDisconnectedNodesCrossingTest()
    {
        this.verifier.actual(this.setup.invalidDisconnectedNodesCrossingAtlas(),
                new EdgeCrossingEdgeCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testInvalidMultiClusterAtlas()
    {
        this.verifier.actual(this.setup.invalidMultiClusterAtlas(),
                new EdgeCrossingEdgeCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testNoCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.noCrossingItemsAtlas(),
                new EdgeCrossingEdgeCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.validCrossingItemsAtlas(),
                new EdgeCrossingEdgeCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidIntersectionItemsAtlas()
    {
        this.verifier.actual(this.setup.validIntersectionItemsAtlas(),
                new EdgeCrossingEdgeCheck(this.configuration));
        this.verifier.verifyEmpty();
    }
}
