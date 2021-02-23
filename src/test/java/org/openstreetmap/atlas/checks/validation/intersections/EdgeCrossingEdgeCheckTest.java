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
    public void testInvalidCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlas(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"car.navigable\":true,\"pedestrian.navigable\":true,\"crossing.car.navigable\":true,\"crossing.pedestrian.navigable\":true}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(4, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingItemsAtlasCarNavigationOnly()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlasCarPed(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"car.navigable\":true,\"pedestrian.navigable\":false,\"crossing.car.navigable\":true,\"crossing.pedestrian.navigable\":false}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingItemsAtlasPedestrianNavigationOnly()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlasCarPed(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"car.navigable\":false,\"pedestrian.navigable\":true,\"crossing.car.navigable\":false,\"crossing.pedestrian.navigable\":true}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
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
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingItemsWithSameLayerTagAtlas()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsWithSameLayerTagAtlas(),
                new EdgeCrossingEdgeCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"EdgeCrossingEdgeCheck\":{\"minimum.highway.type\":\"track\"}}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingNonMainItemsAtlas()
    {
        this.verifier.actual(this.setup.invalidCrossingNonMainItemsAtlas(),
                new EdgeCrossingEdgeCheck(this.configuration));
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(4, flag.getFlaggedObjects().size()));
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
