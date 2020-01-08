package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cameron_frenette
 */
public class UnwalkableWaysCheckTest
{
    private static final Logger logger = LoggerFactory.getLogger(UnwalkableWaysCheckTest.class);
    @Rule
    public UnwalkableWaysCheckTestRule setup = new UnwalkableWaysCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    private final Configuration config = ConfigurationResolver.emptyConfiguration();
    private final UnwalkableWaysCheck check = new UnwalkableWaysCheck(this.config);

    @Test
    public void doubleCrossingDoubleDisabled()
    {
        this.verifier.actual(this.setup.getSingleFootNoCrossingDoubleAtlas(), this.check);
        this.verifier.verifyEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(flags.size(), 0));
    }

    @Test
    public void doubleCrossingDoubleEnabled()
    {
        final Configuration enabledDoubleCrossingConfig = ConfigurationResolver.inlineConfiguration(
                "{\"UnwalkableWaysCheck\":{\"includeDualCrossingDualCarriageways\":true}}");
        final UnwalkableWaysCheck privateCheck = new UnwalkableWaysCheck(
                enabledDoubleCrossingConfig);
        this.verifier.actual(this.setup.getDoubleCrossingDoubleAtlas(), privateCheck);
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(flags.size(), 2));
    }

    @Test
    // This is a typical H shaped intersection
    public void singleCrossingDouble()
    {

        this.verifier.actual(this.setup.getSingleCrossingDouble(), this.check);
        this.verifier.verifyNotEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(flags.size(), 1));
        this.verifier.verify(flag -> Assert.assertEquals(flag.getFlaggedObjects().size(), 1));
    }

    @Test
    // This is a typical H shaped intersection but the cross piece is foot=no
    public void singleFootNoCrossingDouble()
    {
        this.verifier.actual(this.setup.getSingleFootNoCrossingDoubleAtlas(), this.check);
        this.verifier.verifyEmpty();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(flags.size(), 0));
        this.verifier.verify(flag -> Assert.assertEquals(flag.getFlaggedObjects().size(), 0));
    }
}
