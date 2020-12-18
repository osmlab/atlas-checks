package org.openstreetmap.atlas.checks.validation.areas;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link ConcerningAngleBuildingCheck}
 *
 * @author v-garei
 */
public class ConcerningAngleBuildingCheckTest
{
    @Rule
    public ConcerningAngleBuildingCheckTestRule setup = new ConcerningAngleBuildingCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final ConcerningAngleBuildingCheck check = new ConcerningAngleBuildingCheck(
            ConfigurationResolver.inlineConfiguration("{\"ConcerningAngleBuildingCheck\": {"
                    + "\"angles\": " + "{\"minLowAngleDiff\": 80.0," + "\"maxLowAngleDiff\": 89.9,"
                    + "\"minHighAngleDiff\": 90.1," + "\"maxHighAngleDiff\": 100.0" + "},"
                    + "\"angleCounts\": {" + "\"min\": 4.0," + "\"max\": 16.0}}}"));

    @Test
    public void needsSquaredAngleTruePositive()
    {
        this.verifier.actual(this.setup.needsSquaredAngleTruePositive(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void squaredAngleFalsePositive()
    {
        this.verifier.actual(this.setup.squaredAngleFalsePositive(), this.check);
        this.verifier.verifyEmpty();
    }

}
