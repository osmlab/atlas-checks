package org.openstreetmap.atlas.checks.validation.intersections;

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
}
