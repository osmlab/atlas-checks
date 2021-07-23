package org.openstreetmap.atlas.checks.validation.relations;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for RoutePrintCheck.
 *
 * @author Lluc
 */
public class RouteRelationCheckTest {

    private static final RouteRelationCheck check = new RouteRelationCheck(
            ConfigurationResolver.emptyConfiguration());
    @Rule
    public RouteRelationCheckTestRule setup = new RouteRelationCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void validRouteOneTest()
    {
        this.verifier.actual(this.setup.getValidRouteOne(), check);
        this.verifier.verifyEmpty();
    }



    @Test
    public void invalidRouteOneTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteOne(), check);

        this.verifier.verifyExpectedSize(1);

        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("It should be contained in a Route Master relation")));
    }


    @Test
    public void invalidRouteTwoTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteTwo(), check);
        this.verifier.verifyExpectedSize(1);

        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("It should be contained in a Route Master relation")
                        && flag.getInstructions().contains("has gaps in the track")
                        && flag.getInstructions().contains("should be contained in a Route Master relation")));
    }


    @Test
    public void invalidRouteThreeTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteThree(), check);
        this.verifier.verifyExpectedSize(1);

        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("has gaps in the track")));
    }


    @Test
    public void validRouteMasterTest()
    {
        this.verifier.actual(this.setup.getValidRouteMaster(), check);
        this.verifier.verifyEmpty();
    }


    @Test
    public void invalidRouteMasterOneTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteMasterOne(), check);
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert
                .assertTrue(flag.getInstructions().contains("has inconsistent network tag with its route master")
                        && flag.getInstructions().contains("are too far from the track")
                        && flag.getInstructions().contains("contains non route element")));
    }


    @Test
    public void invalidRouteMasterTwoTest()
    {
        this.verifier.actual(this.setup.getInvalidRouteMasterTwo(), check);
        this.verifier.verifyNotEmpty();
        this.verifier.verifyExpectedSize(3);

        this.verifier.verify(flag -> Assert
                .assertTrue((flag.getInstructions().contains("has inconsistent network tag with its route master")
                        && flag.getInstructions().contains("has inconsistent ref tag with its route master")
                        && flag.getInstructions().contains("has inconsistent colour tag with its route master")
                        && flag.getInstructions().contains("contains non route element")) ||
                        flag.getInstructions().contains("has gaps in the track")
                        || (flag.getInstructions().contains("It should be contained in a Route Master relation")
                        && flag.getInstructions().contains("has gaps in the track"))));
    }

}
