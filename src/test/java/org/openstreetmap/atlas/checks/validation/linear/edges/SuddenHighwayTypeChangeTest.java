package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

public class SuddenHighwayTypeChangeTest {
    @Rule
    public SuddenHighwayTypeChangeTestRule setup = new SuddenHighwayTypeChangeTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final SuddenHighwayChange check = new SuddenHighwayChange(ConfigurationResolver
            .inlineConfiguration("{\"SuddenHighwayChange\":{" + "\"minHighwayClass\": tertiary,"
                    + "\"angle\": {" + "\"min\": 100.0," + "\"max\": 170.0" + "}"));

    @Test
    public void testInvalidApproximateWay()
    {
        this.verifier.actual(this.setup.truePositiveSuddenHighwayChange(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testValidApproximateWay()
    {
        this.verifier.actual(this.setup.falsePositiveSuddenHighwayChange(), this.check);
        this.verifier.verifyEmpty();
    }
}
