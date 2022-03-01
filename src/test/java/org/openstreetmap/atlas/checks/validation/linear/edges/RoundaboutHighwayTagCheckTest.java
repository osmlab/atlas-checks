package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Tests for {@link RoundaboutHighwayTagCheck}
 *
 * @author mselaineleong
 * @author brianjor
 */
public class RoundaboutHighwayTagCheckTest
{
    @Rule
    public RoundaboutHighwayTagCheckTestRule setup = new RoundaboutHighwayTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    private final RoundaboutHighwayTagCheck emptyCheck = new RoundaboutHighwayTagCheck(ConfigurationResolver.emptyConfiguration());

    @Test
    public void roundaboutCorrectTagConnectedToSplitStreetTest()
    {
        this.verifier.actual(this.setup.roundaboutCorrectTagConnectedToSplitStreetAtlas(), this.emptyCheck);
        this.verifier.verifyEmpty();
    }

    @Test
    public void roundaboutCorrectTagIgnoreLinksTest()
    {
        this.verifier.actual(this.setup.roundaboutCorrectTagIgnoreLinksAtlas(), this.emptyCheck);
        this.verifier.verifyEmpty();
    }

    @Test
    public void roundaboutCorrectTagNoHigherThroughRoadsTest()
    {
        this.verifier.actual(this.setup.roundaboutCorrectTagNoHigherThroughRoadsAtlas(), this.emptyCheck);
        this.verifier.verifyEmpty();
    }

    @Test
    public void roundaboutCorrectTagAgainstManyTest()
    {
        this.verifier.actual(this.setup.roundaboutCorrectTagAgainstManyAtlas(), this.emptyCheck);
        this.verifier.verifyEmpty();
    }

    @Test
    public void roundaboutPrimaryLinkShouldBePrimaryTest()
    {
        this.verifier.actual(this.setup.roundaboutPrimaryLinkShouldBePrimaryAtlas(), this.emptyCheck);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void roundaboutCorrectTagPrimaryTest()
    {
        this.verifier.actual(this.setup.roundaboutCorrectTagPrimaryAtlas(), this.emptyCheck);
        this.verifier.verifyEmpty();
    }
}