package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link HighwayMissingNameAndRefTagCheck}
 *
 * @author v-garei
 */
public class HighwayMissingNameAndRefTagCheckTest
{
    @Rule
    public HighwayMissingNameOrRefTagCheckTestRule setup = new HighwayMissingNameOrRefTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final HighwayMissingNameAndRefTagCheck check = new HighwayMissingNameAndRefTagCheck(
            ConfigurationResolver.inlineConfiguration("{\"min.contiguous.angle\": 30.0}"));

    @Test
    public void hasInconsistentTagTruePositive()
    {
        this.verifier.actual(this.setup.hasInconsistentTagTruePositive(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void hasNameTagFalsePositive()
    {
        this.verifier.actual(this.setup.hasNameTagFalsePositive(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void hasRefTagFalsePositive()
    {
        this.verifier.actual(this.setup.hasRefTagFalsePositive(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void missingNameAndRefTag()
    {
        this.verifier.actual(this.setup.missingNameAndRefTag(), this.check);
        this.verifier.verifyExpectedSize(3);
    }
}
