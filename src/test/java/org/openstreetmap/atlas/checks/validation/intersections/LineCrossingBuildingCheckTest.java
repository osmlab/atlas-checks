package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * @author mkalender
 */
public class LineCrossingBuildingCheckTest
{
    private static final LineCrossingBuildingCheck check = new LineCrossingBuildingCheck(
            ConfigurationResolver.emptyConfiguration());

    @Rule
    public LineCrossingBuildingCheckTestRule setup = new LineCrossingBuildingCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testInvalidCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(flags.size(), 1));
        this.verifier.verify(flag -> Assert.assertEquals(flag.getFlaggedObjects().size(), 7));
    }

    @Test
    public void testInvalidCrossingItemsForRoofAtlas()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsForRoofAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInvalidCrossingItemsForTollBoothAtlas()
    {
        this.verifier.actual(this.setup.invalidCrossingItemsForTollBoothAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInvalidIntersectionItemsAtlas()
    {
        this.verifier.actual(this.setup.invalidIntersectionItemsAtlas(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(flags.size(), 1));
        this.verifier.verify(flag -> Assert.assertEquals(flag.getFlaggedObjects().size(), 3));
    }

    @Test
    public void testNoCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.noCrossingItemsAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidBuildingEntranceAtlas()
    {
        this.verifier.actual(this.setup.validBuildingEntranceAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidBuildingNoAtlas()
    {
        this.verifier.actual(this.setup.validBuildingNoAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidCrossingItemsAtlas()
    {
        this.verifier.actual(this.setup.validCrossingItemsAtlas(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidIntersectionItemsAtlas()
    {
        this.verifier.actual(this.setup.validIntersectionItemsAtlas(), check);
        this.verifier.verifyEmpty();
    }
}
