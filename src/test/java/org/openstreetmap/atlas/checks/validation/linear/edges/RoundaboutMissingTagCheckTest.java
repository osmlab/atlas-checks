package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

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

    private final Configuration inlineConfiguration = ConfigurationResolver.inlineConfiguration(
            "{\"RoundaboutMissingTagCheck\":{\"ignore.tags.filter\":\"motor_vehicle->!no&foot->!yes&footway->!&access->!private&construction->!\"}}");

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
    public void tagFilterTestAccessPrivate()
    {
        this.verifier.actual(this.setup.tagFilterTestAccessPrivate(),
                new RoundaboutMissingTagCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void tagFilterTestConstruction()
    {
        this.verifier.actual(this.setup.tagFilterTestConstruction(),
                new RoundaboutMissingTagCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void tagFilterTestFootYes()
    {
        this.verifier.actual(this.setup.tagFilterTestFootYes(),
                new RoundaboutMissingTagCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void tagFilterTestFootwayTag()
    {
        this.verifier.actual(this.setup.tagFilterTestFootwayTag(),
                new RoundaboutMissingTagCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void tagFilterTestMotorVehicleNo()
    {
        this.verifier.actual(this.setup.tagFilterTestMotorVehicleNo(),
                new RoundaboutMissingTagCheck(this.inlineConfiguration));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void turnLoop()
    {
        this.verifier.actual(this.setup.turnLoop(),
                new RoundaboutMissingTagCheck(this.inlineConfiguration));
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
