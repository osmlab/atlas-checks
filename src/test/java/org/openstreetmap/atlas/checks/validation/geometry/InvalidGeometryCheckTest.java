package org.openstreetmap.atlas.checks.validation.geometry;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Test class for {@link InvalidGeometryCheck}
 *
 * @author jklamer
 * @author bbreithaupt
 */
public class InvalidGeometryCheckTest
{

    @Rule
    public InvalidGeometryCheckTestRule setup = new InvalidGeometryCheckTestRule();
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();
    private final Configuration configuration = ConfigurationResolver.emptyConfiguration();

    @Test
    public void borderSlicedPolygonTest()
    {
        this.verifier.actual(this.setup.borderSlicedPolygonAtlas(),
                new InvalidGeometryCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void boundaryNodeTest()
    {
        this.verifier.actual(this.setup.boundaryNodeAtlas(),
                new InvalidGeometryCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void bowtiePolygonInvalidTest()
    {
        this.verifier.actual(this.setup.getBowtiePolygonAtlas(),
                new InvalidGeometryCheck(this.configuration));
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag -> Assert.assertEquals(InvalidGeometryCheckTestRule.TEST_ID_5,
                flag.getIdentifier()));
    }

    @Test
    public void disconnectedCenterPolygonInvalidTest()
    {
        this.verifier.actual(this.setup.getDisconnectedCenterPolygonAtlas(),
                new InvalidGeometryCheck(this.configuration));
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag -> Assert.assertEquals(InvalidGeometryCheckTestRule.TEST_ID_7,
                flag.getIdentifier()));
    }

    @Test
    public void hangNailPolygonInvalidTest()
    {
        this.verifier.actual(this.setup.getHangNailPolygonAtlas(),
                new InvalidGeometryCheck(this.configuration));
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag -> Assert.assertEquals(InvalidGeometryCheckTestRule.TEST_ID_6,
                flag.getIdentifier()));
    }

    @Test
    public void testFineLinearTest()
    {
        this.verifier.actual(this.setup.getFineLinearAtlas(),
                new InvalidGeometryCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testFinePolygonTest()
    {
        this.verifier.actual(this.setup.getFinePolygonAtlas(),
                new InvalidGeometryCheck(this.configuration));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testNotValidLinearTest()
    {
        this.verifier.actual(this.setup.getNotValidLinearAtlas(),
                new InvalidGeometryCheck(this.configuration));
        this.verifier.verifyNotEmpty();
        this.verifier.verify(flag -> Assert.assertEquals(InvalidGeometryCheckTestRule.TEST_ID_2,
                flag.getIdentifier()));
    }
}
