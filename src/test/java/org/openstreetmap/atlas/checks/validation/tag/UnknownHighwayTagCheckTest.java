package org.openstreetmap.atlas.checks.validation.tag;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link UnknownHighwayTagCheck}
 *
 * @author v-garei
 */
public class UnknownHighwayTagCheckTest
{
    @Rule
    public UnknownHighwayTagCheckTestRule setup = new UnknownHighwayTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final UnknownHighwayTagCheck check = new UnknownHighwayTagCheck(ConfigurationResolver
            .inlineConfiguration("{\"UnknownHighwayTagCheck\": " + "{" + "}}"));

    @Test
    public void falsePositiveKnownHighwayTagOnEdge()
    {
        this.verifier.actual(this.setup.falsePositiveKnownHighwayTagOnEdge(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void falsePositiveKnownHighwayTagOnNode()
    {
        this.verifier.actual(this.setup.falsePositiveKnownHighwayTagOnNode(), this.check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void truePositiveEdgeTagOnNode()
    {
        this.verifier.actual(this.setup.truePositiveEdgeTagOnNode(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveNodeTagOnEdge()
    {
        this.verifier.actual(this.setup.truePositiveNodeTagOnEdge(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveUnknownHighwayTagOnEdge()
    {
        this.verifier.actual(this.setup.truePositiveUnknownHighwayTagOnEdge(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void truePositiveUnknownHighwayTagOnNode()
    {
        this.verifier.actual(this.setup.truePositiveUnknownHighwayTagOnNode(), this.check);
        this.verifier.verifyExpectedSize(1);
    }

}
