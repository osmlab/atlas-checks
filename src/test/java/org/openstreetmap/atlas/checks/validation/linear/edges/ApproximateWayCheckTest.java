package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link ApproximateWayCheck}
 *
 * @author v-brjor
 */
public class ApproximateWayCheckTest
{
    @Rule
    public ApproximateWayCheckTestRule setup = new ApproximateWayCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final ApproximateWayCheck check = new ApproximateWayCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"ApproximateWayCheck\":{\"deviation.minimum.meters\": 35.0,\"angle.minimum\": 100.0,\"bezierStep\": 0.01,\"highway.minimum\": \"service\"}}"));

    @Test
    public void testInvalidApproximateWay()
    {
        this.verifier.actual(this.setup.invalidApproximateWayAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testRightAngle()
    {
        this.verifier.actual(this.setup.rightAngleAtlas(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidApproximateWay()
    {
        this.verifier.actual(this.setup.validApproximateWayAtlas(), this.check);
        this.verifier.verifyEmpty();
    }

}
