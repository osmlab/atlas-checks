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
    public void invalidBuildingAndPartStackedTest()
    {
        this.verifier.actual(this.setup.invalidBuildingAndPartStackedAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void invalidBuildingBadMinTest()
    {
        this.verifier.actual(this.setup.invalidBuildingBadMinAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidBuildingMissingTag()
    {
        this.verifier.actual(this.setup.invalidBuildingMissingTagAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidBuildingPartSingleAtlas()
    {
        this.verifier.actual(this.setup.invalidBuildingPartSingleAtlas(),
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
    public void invalidBuildingPartsIntersectTest()
    {
        this.verifier.actual(this.setup.invalidBuildingPartsIntersectAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidBuildingPartsManyFloatTest()
    {
        this.verifier.actual(this.setup.invalidBuildingPartsManyFloatAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getFlaggedObjects().size()));
    }

    @Test
    public void invalidBuildingRelationAndPartStackedTest()
    {
        this.verifier.actual(this.setup.invalidBuildingRelationAndPartStackedAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void invalidBuildingsStackedTest()
    {
        this.verifier.actual(this.setup.invalidBuildingsStackedAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
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
    public void invalidFloatingLevelRelationBuildingTest()
    {
        this.verifier.actual(this.setup.invalidFloatingLevelRelationBuildingAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void invalidUntaggedAreasStackedTest()
    {
        this.verifier.actual(this.setup.invalidUntaggedAreasStackedAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingAndPartStackedTest()
    {
        this.verifier.actual(this.setup.validBuildingAndPartStackedAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingBadMinTest()
    {
        this.verifier.actual(this.setup.validBuildingBadMinAtlas(),
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
    public void validBuildingPartsEnclosePartTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsEnclosePartAtlas(),
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
    public void validBuildingPartsStackedMixedTagsTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsStackedMixedTagsAtlas(),
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
    public void validBuildingPartsTouchGroundTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsTouchGroundAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingPartsTouchHeightFeetInchesTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsTouchHeightFeetInchesAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingPartsTouchHeightMetersTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsTouchHeightMetersAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingPartsTouchTest()
    {
        this.verifier.actual(this.setup.validBuildingPartsTouchAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingRelationAndPartStackedTest()
    {
        this.verifier.actual(this.setup.validBuildingRelationAndPartStackedAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingRoofTest()
    {
        this.verifier.actual(this.setup.validBuildingRoofAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validBuildingTest()
    {
        this.verifier.actual(this.setup.validBuildingAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validFloatingLevelRelationBuildingTest()
    {
        this.verifier.actual(this.setup.validFloatingLevelRelationBuildingAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void validUntaggedAreasStackedBuildingRelationTest()
    {
        this.verifier.actual(this.setup.validUntaggedAreasStackedBuildingRelationAtlas(),
                new ShadowDetectionCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }
}
