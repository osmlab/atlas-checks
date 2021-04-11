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
            ConfigurationResolver.inlineConfiguration("{\"FixMeReviewCheck\": {"
                    + "\"fixMe.supported.values\": [\"verify\", \"position\", \"resurvey\", \"Revisar:_este_punto_fue_creado_por_importación_directa\", \"continue\", \"name\", \"incomplete\", \"draw_geometry_and_delete_this_point\", \"unfinished\", \"recheck\"],"
                    + "\"min.highway.type\": \"tertiary\"}}"));

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
