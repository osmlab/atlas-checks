package org.openstreetmap.atlas.checks.validation.areas;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit tests for {@link ShadowDetectionCheck}.
 *
 * @author bbreithaupt
 */
public class ShadowDetectionCheckTest
{
    @Rule
    public ShadowDetectionCheckTestRule setup = new ShadowDetectionCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void validBuildingTest()
    {
        this.verifier.actual(this.setup.validBuildingAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidFloatingHeightBuildingTest()
    {
        this.verifier.actual(this.setup.invalidFloatingHeightBuildingAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidFloatingLevelBuildingTest()
    {
        this.verifier.actual(this.setup.invalidFloatingLevelBuildingAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validBuildingPartsTouchTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsTouchAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingPartsTouchGroundTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsTouchGroundAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingPartsIntersectTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsIntersectAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidBuildingPartsIntersectTest()
    {
        this.verifier.actual(this.setup.invalidBuildingPartsIntersectAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidBuildingPartsDisparateTest()
    {
        this.verifier.actual(this.setup.invalidBuildingPartsDisparateAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void validBuildingPartsEnclosePartTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsEnclosePartAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingPartsEncloseNeighborTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsEncloseNeighborAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingPartsStackedTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsStackedAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingPartsStackedMixedTagsTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsStackedMixedTagsAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void invalidBuildingPartsManyFloatTest()
    {
        this.verifier.actual(this.setup.invalidBuildingPartsManyFloatAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getFlaggedObjects().size()));
    }
}
