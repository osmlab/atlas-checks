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
            ConfigurationResolver.inlineConfiguration("{\"SuddenHighwayChangeCheck\":{"
                    + "\"minHighwayClass\": tertiary," + "\"angle\": {" + "\"min\": 115.0,"
                    + "\"max\": 165.0}," + "\"edgeCounts\": {" + "\"connectedEdgeMin\": 2.0,"
                    + "\"length\": {" + "\"min\": 15.0," + "\"max\": 250.0}}}"));

    @Test
    public void testFalsePositiveSuddenHighwayChange()
    {
        this.verifier.actual(this.setup.falsePositiveSuddenHighwayChangeCheck(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTruePositiveSuddenHighwayChange()
    {
        this.verifier.actual(this.setup.truePositiveSuddenHighwayChangeCheck(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
}
