package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit test for {@link InvalidPiersCheck}
 *
 * @author sayas01
 */
public class InvalidPiersCheckTest
{
    @Rule
    public InvalidPiersCheckTestRule setup = new InvalidPiersCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void testLinearPierWithHighwayTag()
    {
        this.verifier.actual(this.setup.getLinearPierWithHighwayTag(),
                new InvalidPiersCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testLinearPierConnectedToBuilding()
    {
        this.verifier.actual(this.setup.getLinearPierConnectedToBuildingAtlas(),
                new InvalidPiersCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testPolygonalPierOverlappingHighway()
    {
        this.verifier.actual(this.setup.getPolygonalPierOverlappingHighwayAtlas(),
                new InvalidPiersCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testPolygonalPierConnectedToBuilding()
    {
        this.verifier.actual(this.setup.getPolygonalPierConnectedToBuildingAtlas(),
                new InvalidPiersCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyNotNull();
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testValidPolygonalPier()
    {
        this.verifier.actual(this.setup.getValidPierAtlas(),
                new InvalidPiersCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}
