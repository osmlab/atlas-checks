package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * {@link SuddenHighwayChangeCheck} unit test
 *
 * @author v-garei
 */
public class SuddenHighwayChangeCheckTest
{
    @Rule
    public SuddenHighwayChangeCheckTestRule setup = new SuddenHighwayChangeCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final SuddenHighwayChangeCheck check = new SuddenHighwayChangeCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"SuddenHighwayChangeCheck\":{" + "\"minHighwayClass\": tertiary,"
                            + "\"angle\": {" + "\"min\": 100.0," + "\"max\": 170.0" + "}"));

    @Test
    public void testInvalidSuddenHighwayChange()
    {
        this.verifier.actual(this.setup.falsePositiveSuddenHighwayChangeCheck(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testValidSuddenHighwayChange()
    {
        this.verifier.actual(this.setup.truePositiveSuddenHighwayChangeCheck(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
}
