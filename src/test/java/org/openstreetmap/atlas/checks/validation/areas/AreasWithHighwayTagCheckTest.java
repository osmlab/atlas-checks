package org.openstreetmap.atlas.checks.validation.areas;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * @author matthieun
 * @author daniel-baah
 */
public class AreasWithHighwayTagCheckTest
{

    private final AreasWithHighwayTagCheck check = new AreasWithHighwayTagCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"AreasWithHighwayTagCheck\":{\"tags.filter\":\"highway->*&area->yes\"}}"));

    @Rule
    public AreasWithHighwayTagCheckTestRule setup = new AreasWithHighwayTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void areaNoHighwayTag()
    {
        this.verifier.actual(this.setup.areaNoHighwayTagAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void validHighwayPedestrianTag()
    {
        this.verifier.actual(this.setup.validHighwayPedestrianTagAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void invalidAreaHighwayPrimaryTag()
    {
        this.verifier.actual(this.setup.invalidAreaHighwayPrimaryTagAtlas(), check);
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void invalidAreaHighwayFootwayTag()
    {
        this.verifier.actual(this.setup.invalidAreaHighwayFootwayTagAtlas(), check);
        this.verifier.verifyNotEmpty();
    }

    @Test
    public void invalidHighwayPedestrianNoAreaTag()
    {
        this.verifier.actual(this.setup.invalidHighwayPedestrianNoAreaTagAtlas(), check);
        this.verifier.verify(flag -> Assert.assertEquals(flag.getInstructions(),
                String.format("Area with OSM ID %s is missing area tag.",
                        AreasWithHighwayTagCheckTestRule.INVALID_AREA_ID)));
    }

    @Test
    public void validAreaHighwayPlatform()
    {
        this.verifier.actual(this.setup.validAreaHighwayPlatform(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void invalidEdgeHighwaySecondary()
    {
        this.verifier.actual(this.setup.invalidEdgeHighwaySecondary(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void validEdgeHighwayService()
    {
        this.verifier.actual(this.setup.validEdgeHighwayService(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void validEdgeNoAreaTag()
    {
        this.verifier.actual(this.setup.validEdgeNoAreaTag(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void validAreaHighwayPrimaryNoAreaTag()
    {
        this.verifier.actual(this.setup.validAreaHighwayPrimaryNoAreaTag(), check);
        this.verifier.verifyEmpty();
    }
}
