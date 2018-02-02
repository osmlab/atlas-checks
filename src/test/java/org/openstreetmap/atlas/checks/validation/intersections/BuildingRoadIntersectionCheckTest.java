package org.openstreetmap.atlas.checks.validation.intersections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * {@link BuildingRoadIntersectionCheck} unit test
 *
 * @author mgostintsev
 */
public class BuildingRoadIntersectionCheckTest
{

    private static final BuildingRoadIntersectionCheck check = new BuildingRoadIntersectionCheck(
            ConfigurationResolver.emptyConfiguration());
    private static final BuildingRoadIntersectionCheck spanishCheck = new BuildingRoadIntersectionCheck(
            ConfigurationResolver.inlineConfiguration(
                    "{\"CheckResourceLoader\": {\"scanUrls\": [\"org.openstreetmap.atlas.checks\"]},\"BuildingRoadIntersectionCheck\":{\"enabled\":true,\"locale\":\"es\",\"flags\":{\"en\":[\"Building (id-{0,number,#}) intersects road (id-{1,number,#})\"],\"es\":[\"Edificio(id-{0,number,#}) cruza calle(id-{1,number,#})\"]}}}"));

    @Rule
    public BuildingRoadIntersectionTestCaseRule setup = new BuildingRoadIntersectionTestCaseRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    /**
     * Basic test, the default atlas contains two issues that should be retrieved.
     */
    @Test
    public void testCheck()
    {
        this.verifier.actual(this.setup.getAtlas(), check);
        this.verifier.verifyExpectedSize(2);
    }

    /**
     * Unit test to make sure that if the "covered=yes" tag is included that we ignore any building
     * road intersections. In the atlas setup in the rule there is one building road intersection,
     * however the road includes the tag "covered=yes" so no results are expected.
     */
    @Test
    public void testCovered()
    {
        this.verifier.actual(this.setup.getCoveredAtlas(), check);
        this.verifier.verifyExpectedSize(0);
    }

    /**
     * Test to confirm the locality functionality of the check. Changing the instruction to spanish.
     */
    @Test
    public void testSpanishCheck()
    {
        this.verifier.actual(this.setup.getAtlas(), spanishCheck);
        this.verifier.verify(flag ->
        {
            Assert.assertTrue(
                    flag.getInstructions().contains("Edificio(id-323232) cruza calle(id-292929)"));
        });
        this.verifier.verifyExpectedSize(2);

    }

    /**
     * Unit Test to make sure that the tunnel = building passage tag is not being counted as an
     * intersection edge. This test also checks to make sure valid single point intersections are
     * not flagged.
     */
    @Test
    public void testTunnel()
    {
        this.verifier.actual(this.setup.getTunnelBuildingIntersect(), check);
        this.verifier.verifyExpectedSize(0);
    }

    /**
     * Unit test to make sure that when layer tag is used on highways that they are handled
     * properly. And so if highway layer tag = -1 then it does not intersect with the building. As
     * in this particular case it goes under it.
     */
    @Test
    public void testLayered()
    {
        this.verifier.actual(this.setup.getLayeredAtlas(), check);
        this.verifier.verifyExpectedSize(0);
    }
}
