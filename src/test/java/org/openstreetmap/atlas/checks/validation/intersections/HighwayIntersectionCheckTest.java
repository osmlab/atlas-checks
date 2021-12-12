package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * @author pako.todea
 */
public class HighwayIntersectionCheckTest
{

    @Rule
    public HighwayIntersectionTestCaseRule setup = new HighwayIntersectionTestCaseRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final Configuration configuration = ConfigurationResolver.emptyConfiguration();

    @Test
    public void testInvalidCrossingHighwayLeisureEdges()
    {
        this.verifier.actual(this.setup.invalidCrossingWaterwayLeisureEdges(),
                new HighwayIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingHighwayPowerLineEdges()
    {
        this.verifier.actual(this.setup.invalidCrossingHighwayPowerLineEdges(),
                new HighwayIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingHighwayWaterEdges()
    {
        this.verifier.actual(this.setup.invalidCrossingHighwayWaterEdges(),
                new HighwayIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingPowerLineFordYesEdges()
    {
        this.verifier.actual(this.setup.invalidCrossingPowerLineFordYesEdges(),
                new HighwayIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingPowerLineLeisureSlipwayEdges()
    {
        this.verifier.actual(this.setup.invalidCrossingPowerLineLeisureSlipwayEdges(),
                new HighwayIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidMultipleCrossingHighwayWaterEdges()
    {
        this.verifier.actual(this.setup.invalidMultipleCrossingHighwayWaterEdges(),
                new HighwayIntersectionCheck(this.configuration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(3, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testNoCrossingHighwayPowerLineEdges()
    {
        this.verifier.actual(this.setup.noCrossingHighwayPowerLineEdges(),
                new HighwayIntersectionCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testNoCrossingHighwayWaterEdges()
    {
        this.verifier.actual(this.setup.noCrossingHighwayWaterEdges(),
                new HighwayIntersectionCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidCrossingHighwayFordYesEdges()
    {
        this.verifier.actual(this.setup.validCrossingWaterwayFordYesEdges(),
                new HighwayIntersectionCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidCrossingHighwayLeisureSlipwayEdges()
    {
        this.verifier.actual(this.setup.validCrossingWaterwayLeisureSlipwayEdges(),
                new HighwayIntersectionCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidCrossingHighwayWaterwayDamEdges()
    {
        this.verifier.actual(this.setup.validCrossingHighwayWaterwayDamEdges(),
                new HighwayIntersectionCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidCrossingHighwayWaterwayWeirEdges()
    {
        this.verifier.actual(this.setup.validCrossingHighwayWaterwayWeirEdges(),
                new HighwayIntersectionCheck(this.configuration));
        this.verifier.verifyEmpty();
    }
}
