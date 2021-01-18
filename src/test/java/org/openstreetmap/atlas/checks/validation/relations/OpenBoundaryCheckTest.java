package org.openstreetmap.atlas.checks.validation.relations;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link OpenBoundaryCheck}
 *
 * @author v-garei
 */
public class OpenBoundaryCheckTest
{

    @Rule
    public OpenBoundaryCheckTestRule setup = new OpenBoundaryCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final OpenBoundaryCheck check = new OpenBoundaryCheck(ConfigurationResolver
            .inlineConfiguration("{\"OpenBoundaryCheck\": {\"minHighwayType\": \"tertiary\"}}"));

    @Test
    public void polygonClosedFalsePositive()
    {
        this.verifier.actual(this.setup.polygonClosedFalsePositive(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void polygonNotClosedTruePositive()
    {
        this.verifier.actual(this.setup.polygonNotClosedTruePositive(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

}
