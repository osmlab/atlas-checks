package org.openstreetmap.atlas.checks.validation.linear;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Tests for {@link MalformedPolyLineCheck}
 *
 * @author matthieun
 * @author sayas01
 */
public class MalformedPolyLineCheckTest
{
    @Rule
    public MalformedPolyLineCheckTestRule setup = new MalformedPolyLineCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    private final Configuration configuration = ConfigurationResolver.emptyConfiguration();

    @Test
    public void testComplexPolyLines()
    {
        this.verifier.actual(this.setup.getComplexPolyLineAtlas(),
                new MalformedPolyLineCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testMaximumLength()
    {
        this.verifier.actual(this.setup.getMaxLengthAtlas(),
                new MalformedPolyLineCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("which is longer than the maximum")));
    }

    @Test
    public void testNumberPoints()
    {
        this.verifier.actual(this.setup.getMalformedPolyLineAtlas(),
                new MalformedPolyLineCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions().contains(
                "Line contains 530 points more than maximum of 500 and line is 155.6 km (96.7 miles), which is longer than the maximum of 100.0 km (62.1 miles)")));
    }

    @Test
    public void testPolyLinePartOfWaterwayRelation()
    {
        this.verifier.actual(this.setup.getRelationWithWaterTagAtlas(),
                new MalformedPolyLineCheck(this.configuration));
        this.verifier.verifyEmpty();
    }
}
