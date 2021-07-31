package org.openstreetmap.atlas.checks.validation.relations;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Finds relations that meet at least one of the following requirements: The track of this route
 * contains gaps, The stop or platform is too far from the track of this route, Non route relation
 * member in route_master relation, Public transport relation route not in route_master relation,
 * network, operator, ref, colour tag should be the same on route and route_master relations.
 *
 * @author micah-nacht
 */
public class RouteRelationCheckTest
{

    private static final RouteRelationCheck check = new RouteRelationCheck(
            ConfigurationResolver.emptyConfiguration());
    @Rule
    public RouteRelationCheckTestRule setup = new RouteRelationCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void invalidRouteFourTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteFour(), check);

        this.verifier.verifyExpectedSize(1);
        this.verifier
                .verify(flag -> Assert.assertTrue(flag.getInstructions().contains("The platforms")
                        && flag.getInstructions().contains("are too far from the track")));
    }

    @Test
    public void invalidRouteMasterFourTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteMasterFour(), check);

        this.verifier.verifyExpectedSize(3); //
    }

    @Test
    public void invalidRouteMasterOneTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteMasterOne(), check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions().contains(
                "inconsistent network, operator, ref, or colour tag with its route master")));
    }

    @Test
    public void invalidRouteMasterThreeTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteMasterThree(), check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("contains non route element")));
    }

    @Test
    public void invalidRouteMasterTwoTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteMasterTwo(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verifyExpectedSize(1);

        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions().contains(
                "inconsistent network, operator, ref, or colour tag with its route master")));
    }

    @Test
    public void invalidRouteOneTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteOne(), check);

        this.verifier.verifyExpectedSize(1);

        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions()
                .contains("It should be contained in a Route Master relation")));
    }

    @Test
    public void invalidRouteThreeTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteThree(), check);

        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert.assertTrue(flag.getInstructions().contains("The stops")
                && flag.getInstructions().contains("are too far from the track")));
    }

    @Test
    public void invalidRouteTwoTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteTwo(), check);

        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("has gaps in the track")));
    }

    @Test
    public void validRouteOneTest()
    {
        this.verifier.actual(this.setup.getValidRouteOne(), check);
        this.verifier.verifyEmpty();
    }

}
