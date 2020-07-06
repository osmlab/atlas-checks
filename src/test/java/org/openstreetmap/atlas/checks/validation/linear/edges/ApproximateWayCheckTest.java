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

    private final ApproximateWayCheck check = new ApproximateWayCheck(ConfigurationResolver
            .inlineConfiguration("{\"ApproximateWayCheck\":{" + "\"deviation\": {"
                    + "\"minimum.meters\": 10.0," + "\"ratio\": {" + "\"max\": 0.04" + "}" + "},"
                    + "\"angle\": {" + "\"min\": 60.0," + "\"max\": 160.0" + "},"
                    + "\"bezierStep\": 0.01," + "\"highway.minimum\": \"service\"" + "}}"));

    @Test
    public void testInvalidApproximateWay()
    {
        this.verifier.actual(this.setup.invalidApproximateWayAtlas(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testSingleSegmentAtlas()
    {
        this.verifier.actual(this.setup.singleSegmentAtlas(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidApproximateWay()
    {
        this.verifier.actual(this.setup.validApproximateWayAtlas(), this.check);
        this.verifier.verifyEmpty();
    }

}
