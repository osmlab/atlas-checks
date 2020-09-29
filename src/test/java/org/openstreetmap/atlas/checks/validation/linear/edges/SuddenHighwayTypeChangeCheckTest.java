package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link SuddenHighwayTypeChangeCheck}
 *
 * @author v-garei
 */
public class SuddenHighwayTypeChangeCheckTest
{

    @Rule
    public SuddenHighwayTypeChangeCheckTestRule setup = new SuddenHighwayTypeChangeCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final SuddenHighwayTypeChangeCheck check = new SuddenHighwayTypeChangeCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"ApproximateWayCheck\": {" + "\"minHighwayType\": tertiary" + "}}"));

    @Test
    public void testFalsePositiveSuddenHighwayTypeChange()
    {
        this.verifier.actual(this.setup.falsePositiveSuddenHighwayTypeChangeCheck(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTruePositiveSuddenHighwayTypeChange()
    {
        this.verifier.actual(this.setup.truePositiveSuddenHighwayTypeChangeCheck(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
}
