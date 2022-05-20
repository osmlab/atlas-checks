package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;

/**
 * Unit tests for {@link SinkIslandCheck}
 *
 * @author matthieun
 * @author gpogulsky
 * @author nachtm
 * @author sayas01
 * @author seancoulter
 * @author bbreithaupt
 */
public class SinkIslandCheckTest
{
    @Rule
    public SinkIslandCheckTestRule setup = new SinkIslandCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @Test
    public void permittedSelectAccessTest()
    {
        this.verifier.actual(this.setup.permittedSelectAccessAtlas(),
                new SinkIslandCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testEdgesEndingInBuilding()
    {
        this.verifier.actual(this.setup.getEdgesEndingInBuilding(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testEdgesWithinAirport()
    {
        this.verifier.actual(this.setup.getEdgesWithinAirport(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testEdgesWithinAreasWithAmenityTags()
    {
        this.verifier.actual(this.setup.getEdgeWithinAreaWithAmenityTag(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testFerryValid()
    {
        this.verifier.actual(this.setup.ferryAtlas(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 6}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testHighwayImportanceConfiguration()
    {
        this.verifier.actual(this.setup.getServiceSinkIsland(),
                new SinkIslandCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"SinkIslandCheck\": {\"tree.size\": 3, \"minimum.highway.type\": \"RESIDENTIAL\"}}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testInvalidEdges()
    {
        this.verifier.actual(this.setup.getInvalidEdges(),
                new SinkIslandCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert.assertEquals(4, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testMotorcarOverrideVehicleAtlas()
    {
        this.verifier.actual(this.setup.motorcarOverrideVehicleAtlas(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testNonCarNavigableEdges()
    {
        this.verifier.actual(this.setup.getNonCarNavigableEdges(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testParkingGarageEntranceOrExit()
    {
        this.verifier.actual(this.setup.getParkingGarageEntranceOrExit(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testPedestrianFerry()
    {
        this.verifier.actual(this.setup.pedestrianFerryAtlas(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 6}")));
        this.verifier.verifyExpectedSize(2);
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testPedestrianRoadAndMotorVehicleYesRoad()
    {
        this.verifier.actual(this.setup.getPedestrianRoadAndMotorVehicleYesRoad(),
                new SinkIslandCheck(ConfigurationResolver
                        .inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testPsvAndMotorVehicleNoRoad()
    {
        this.verifier.actual(this.setup.getPsvAndMotorVehicleNoRoad(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testServiceInPedestrianNetworkFilterOn()
    {
        this.verifier.actual(this.setup.getEdgeConnectedToPedestrianNetwork(),
                new SinkIslandCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"SinkIslandCheck.filter.pedestrian.network\":true}")));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testServiceSinkIsland()
    {
        this.verifier.actual(this.setup.getServiceSinkIsland(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\":3}")));
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testSingleEdgeAtlas()
    {
        this.verifier.actual(this.setup.getSingleEdgeAtlas(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testSingleEdgePartOfPedestrianNetwork()
    {
        this.verifier.actual(this.setup.getEdgeConnectedToPedestrianNetwork(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testSingleEdgeWithAmenity()
    {
        this.verifier.actual(this.setup.getSingleEdgeWithAmenityAtlas(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(0, flags.size()));
    }

    @Test
    public void testSinkDetection()
    {
        this.verifier.actual(this.setup.getTestAtlas(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.globallyVerify(flags -> Assert.assertEquals(1, flags.size()));
    }

    @Test
    public void testTrackAndPrimarySinkIsland()
    {
        this.verifier.actual(this.setup.getTrackAndPrimarySinkIsland(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.verifyExpectedSize(1);
        this.verifier.verify(flag -> Assert.assertEquals(2, flag.getFlaggedObjects().size()));
    }

    @Test
    public void testTrackSinkIsland()
    {
        this.verifier.actual(this.setup.getTrackSinkIsland(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testTwoEdgesWithAmenity()
    {
        this.verifier.actual(this.setup.getTwoEdgesWithAmenityAtlas(), new SinkIslandCheck(
                ConfigurationResolver.inlineConfiguration("{\"SinkIslandCheck.tree.size\": 3}")));
        this.verifier.verifyEmpty();
    }
}
