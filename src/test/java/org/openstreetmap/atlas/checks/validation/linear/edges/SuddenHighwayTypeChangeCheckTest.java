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
                    "{\"SuddenHighwayTypeChangeCheck\": {\"minHighwayType\": \"tertiary\"}}"));

    @Test
    public void testFalsePositiveSuddenHighwayTypeChangeCheck()
    {
        this.verifier.actual(this.setup.falsePositiveSuddenHighwayTypeChangeCheck(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTruePositiveSuddenHighwayTypeChangeCheck()
    {
        this.verifier.actual(this.setup.truePositiveSuddenHighwayTypeChangeCheck(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveCase1()
    {
        this.verifier.actual(this.setup.truePositiveCase1(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveCase2()
    {
        this.verifier.actual(this.setup.truePositiveCase2(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveCase3()
    {
        this.verifier.actual(this.setup.truePositiveCase3(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
}
