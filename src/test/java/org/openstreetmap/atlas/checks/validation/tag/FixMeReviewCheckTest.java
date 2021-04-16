package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link FixMeReviewCheck}
 *
 * @author v-garei
 */
public class FixMeReviewCheckTest
{

    @Rule
    public FixMeReviewCheckTestRule setup = new FixMeReviewCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final FixMeReviewCheck check = new FixMeReviewCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"FixMeReviewCheck\": {\"fixMe.supported.values\": [\"verify\", \"position\", \"resurvey\", \"Revisar: este punto fue creado por importación directa\", \"continue\", \"name\", \"incomplete\", \"draw geometry and delete this point\", \"unfinished\", \"recheck\"], \"min.highway.type\": \"tertiary\"}}"));

    @Test
    public void nodeWithValidFixMe()
    {
        this.verifier.actual(this.setup.nodeWithValidFixMe(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void wayWithValidFixMe()
    {
        this.verifier.actual(this.setup.wayWithValidFixMe(), this.check);
        this.verifier.verifyExpectedSize(1);
    }
}
