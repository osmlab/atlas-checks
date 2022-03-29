package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * @author pako.todea
 * @author brianjor
 */
public class HighwayIntersectionCheckTest
{

    @Rule
    public HighwayIntersectionTestCaseRule setup = new HighwayIntersectionTestCaseRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final HighwayIntersectionCheck check = new HighwayIntersectionCheck(
            ConfigurationResolver.emptyConfiguration());

    @Test
    public void testInvalidCrossingHighwayLeisureEdges()
    {
        this.verifier.actual(this.setup.invalidCrossingWaterwayLeisureEdges(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingHighwayPowerLineEdges()
    {
        this.verifier.actual(this.setup.invalidCrossingHighwayPowerLineEdges(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingHighwayWaterEdges()
    {
        this.verifier.actual(this.setup.invalidCrossingHighwayWaterEdges(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingPowerLineFordYesEdges()
    {
        this.verifier.actual(this.setup.invalidCrossingPowerLineFordYesEdges(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidCrossingPowerLineLeisureSlipwayEdges()
    {
        this.verifier.actual(this.setup.invalidCrossingPowerLineLeisureSlipwayEdges(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testInvalidMultipleCrossingHighwayWaterEdges()
    {
        this.verifier.actual(this.setup.invalidMultipleCrossingHighwayWaterEdges(), this.check);
        this.verifier.globallyVerify(flags -> Assert.assertEquals(2, flags.size()));
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testNoCrossingHighwayPowerLineEdges()
    {
        this.verifier.actual(this.setup.noCrossingHighwayPowerLineEdges(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testNoCrossingHighwayWaterEdges()
    {
        this.verifier.actual(this.setup.noCrossingHighwayWaterEdges(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidCrossingHighwayFordYesEdges()
    {
        this.verifier.actual(this.setup.validCrossingWaterwayFordYesEdges(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidCrossingHighwayLeisureSlipwayEdges()
    {
        this.verifier.actual(this.setup.validCrossingWaterwayLeisureSlipwayEdges(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidCrossingHighwayWaterwayDamEdges()
    {
        this.verifier.actual(this.setup.validCrossingHighwayWaterwayDamEdges(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidCrossingHighwayWaterwayWeirEdges()
    {
        this.verifier.actual(this.setup.validCrossingHighwayWaterwayWeirEdges(), this.check);
        this.verifier.verifyEmpty();
    }
}
