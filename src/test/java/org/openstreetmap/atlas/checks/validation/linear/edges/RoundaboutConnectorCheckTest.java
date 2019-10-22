package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit tests for {@link RoundaboutConnectorCheck}.
 *
 * @author bbreithaupt
 */
public class RoundaboutConnectorCheckTest
{

    @Rule
    public RoundaboutConnectorCheckTestRule setup = new RoundaboutConnectorCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void invalidOneWayConnectorsReversedLeftTest()
    {
        this.verifier.actual(this.setup.invalidOneWayConnectorsReversedLeftAtlas(),
                new RoundaboutConnectorCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(2);
    }

    @Test
    public void invalidOneWayConnectorsReversedTest()
    {
        this.verifier.actual(this.setup.invalidOneWayConnectorsReversedAtlas(),
                new RoundaboutConnectorCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(2);
    }

    @Test
    public void invalidTwoWayConnectorLeftTest()
    {
        this.verifier.actual(this.setup.invalidTwoWayConnectorLeftAtlas(),
                new RoundaboutConnectorCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidTwoWayConnectorReversedLeftTest()
    {
        this.verifier.actual(this.setup.invalidTwoWayConnectorReversedLeftAtlas(),
                new RoundaboutConnectorCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidTwoWayConnectorReversedTest()
    {
        this.verifier.actual(this.setup.invalidTwoWayConnectorReversedAtlas(),
                new RoundaboutConnectorCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void invalidTwoWayConnectorTest()
    {
        this.verifier.actual(this.setup.invalidTwoWayConnectorAtlas(),
                new RoundaboutConnectorCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void validOneWayConnectorsLeftTest()
    {
        this.verifier.actual(this.setup.validOneWayConnectorsLeftAtlas(),
                new RoundaboutConnectorCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validOneWayConnectorsReversedThresholdTest()
    {
        this.verifier.actual(this.setup.invalidOneWayConnectorsReversedAtlas(),
                new RoundaboutConnectorCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"RoundaboutConnectorCheck.threshold.one-way\": 160.0}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validOneWayConnectorsTest()
    {
        this.verifier.actual(this.setup.validOneWayConnectorsAtlas(),
                new RoundaboutConnectorCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validTwoWayConnectorsLeftTest()
    {
        this.verifier.actual(this.setup.validTwoWayConnectorsLeftAtlas(),
                new RoundaboutConnectorCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void validTwoWayConnectorsTest()
    {
        this.verifier.actual(this.setup.validTwoWayConnectorsAtlas(),
                new RoundaboutConnectorCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}
