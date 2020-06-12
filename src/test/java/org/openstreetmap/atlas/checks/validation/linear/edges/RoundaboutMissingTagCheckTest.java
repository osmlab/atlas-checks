package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit tests for {@link RoundaboutMissingTagCheck}.
 *
 * @author vladlemberg
 */
public class RoundaboutMissingTagCheckTest
{
    @Rule
    public RoundaboutMissingTagCheckTestRule setup = new RoundaboutMissingTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void closedWayMalformedShape()
    {
        this.verifier.actual(this.setup.closedWayMalformedShape(),
                new RoundaboutMissingTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void closedWayNoIntersectionsWithNavigableEdges()
    {
        this.verifier.actual(this.setup.closedWayNoIntersectionsWithNavigableEdges(),
                new RoundaboutMissingTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void closedWayRoundShape()
    {
        this.verifier.actual(this.setup.closedWayRoundShape(),
                new RoundaboutMissingTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void edgeWithAreaTag()
    {
        this.verifier.actual(this.setup.edgeWithAreaTag(),
                new RoundaboutMissingTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void edgeWithRoundaboutTag()
    {
        this.verifier.actual(this.setup.edgeWithRoundaboutTag(),
                new RoundaboutMissingTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void reversedEdge()
    {
        this.verifier.actual(this.setup.reversedEdge(),
                new RoundaboutMissingTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void unClosedWay()
    {
        this.verifier.actual(this.setup.unClosedWay(),
                new RoundaboutMissingTagCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}
