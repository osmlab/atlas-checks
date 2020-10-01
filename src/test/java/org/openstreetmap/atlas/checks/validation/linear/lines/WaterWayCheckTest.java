package org.openstreetmap.atlas.checks.validation.linear.lines;

import java.lang.reflect.Field;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.utility.ElevationUtilities;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.Location;

/**
 * WaterWayCheck test
 *
 * @author Taylor Smock
 */
public class WaterWayCheckTest
{

    /** The rule to get atlases from */
    @Rule
    public WaterWayCheckTestRule atlases = new WaterWayCheckTestRule();
    /** The verifier that actually runs the checks */
    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    /**
     * Look for waterways that connect with themselves
     */
    @Test
    public void testCircularWaterway()
    {
        this.verifier.actual(this.atlases.getCircularWaterway(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Ensure that waterways that end inside oceans are not flagged
     */
    @Test
    public void testCoastWaterway()
    {
        this.verifier.actual(this.atlases.getCoastlineWaterway(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    /**
     * Ensure that waterways that end on an ocean is not flagged
     */
    @Test
    public void testCoastWaterwayConnected()
    {
        this.verifier.actual(this.atlases.getCoastlineWaterwayConnected(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    @Test
    public void testCoastWaterwayConnectedElevation() throws ReflectiveOperationException
    {
        final short[][] map = new short[][] { { 15, 14, 13, 12 }, { 11, 10, 9, 8 }, { 7, 6, 5, 4 },
                { 3, 2, 1, 0 } };
        final WaterWayCheck check = new WaterWayCheck(ConfigurationResolver.inlineConfiguration(
                "{\"WaterWayCheck.waterway.elevation.resolution.min.uphill\": 30000.0}"));
        final Field elevationUtilsField = WaterWayCheck.class.getDeclaredField("elevationUtils");
        elevationUtilsField.setAccessible(true);
        final ElevationUtilities elevationUtils = (ElevationUtilities) elevationUtilsField
                .get(check);
        elevationUtils.putMap(Location.forString("16.9906416,-88.3188021"), map);
        this.verifier.actual(this.atlases.getCoastlineWaterwayConnected(), check);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testCoastWaterwayConnectedElevationReversed() throws ReflectiveOperationException
    {
        final short[][] map = new short[][] { { 0, 1, 2, 3 }, { 4, 5, 6, 7 }, { 8, 9, 10, 11 },
                { 12, 13, 14, 15 } };
        final WaterWayCheck check = new WaterWayCheck(ConfigurationResolver.inlineConfiguration(
                "{\"WaterWayCheck.waterway.elevation.resolution.min.uphill\": 30000.0}"));

        final Field elevationUtilsField = WaterWayCheck.class.getDeclaredField("elevationUtils");
        elevationUtilsField.setAccessible(true);
        final ElevationUtilities elevationUtils = (ElevationUtilities) elevationUtilsField
                .get(check);
        elevationUtils.putMap(Location.forString("16.9906416,-88.3188021"), map);
        this.verifier.actual(this.atlases.getCoastlineWaterwayConnected(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testCoastWaterwayConnectedElevationReversedLongDistance()
            throws ReflectiveOperationException
    {
        final short[][] map = new short[][] { { 0, 1, 2, 3 }, { 4, 5, 6, 7 }, { 8, 9, 10, 11 },
                { 12, 13, 14, 15 } };
        final WaterWayCheck check = new WaterWayCheck(ConfigurationResolver.inlineConfiguration(
                "{\"WaterWayCheck\": {\"waterway.elevation.resolution.min.uphill\": 30000.0, \"waterway.elevation.distance.min.start.end\": 1.0}}"));

        final Field elevationUtilsField = WaterWayCheck.class.getDeclaredField("elevationUtils");
        elevationUtilsField.setAccessible(true);
        final ElevationUtilities elevationUtils = (ElevationUtilities) elevationUtilsField
                .get(check);
        elevationUtils.putMap(Location.forString("16.9906416,-88.3188021"), map);
        this.verifier.actual(this.atlases.getCoastlineWaterwayConnected(), check);
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testCoastWaterwayConnectedElevationReversedLowResolution()
            throws ReflectiveOperationException
    {
        final short[][] map = new short[][] { { 0, 1, 2, 3 }, { 4, 5, 6, 7 }, { 8, 9, 10, 11 },
                { 12, 13, 14, 15 } };
        final WaterWayCheck check = new WaterWayCheck(ConfigurationResolver.emptyConfiguration());

        final Field elevationUtilsField = WaterWayCheck.class.getDeclaredField("elevationUtils");
        elevationUtilsField.setAccessible(true);
        final ElevationUtilities elevationUtils = (ElevationUtilities) elevationUtilsField
                .get(check);
        elevationUtils.putMap(Location.forString("16.9906416,-88.3188021"), map);
        this.verifier.actual(this.atlases.getCoastlineWaterwayConnected(), check);
        this.verifier.verifyEmpty();
    }

    /**
     * Check that a waterway ending in an ocean is not flagged
     */
    @Test
    public void testCoastWaterwayReversed()
    {
        this.verifier.actual(this.atlases.getCoastlineWaterwayReversed(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Check that two crossing waterways are flagged
     */
    @Test
    public void testCrossingWaterway()
    {
        this.verifier.actual(this.atlases.getCrossingWaterways(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Check that two connected crossing waterways are flagged
     */
    @Test
    public void testCrossingWaterwayConnected()
    {
        this.verifier.actual(this.atlases.getCrossingWaterwaysConnected(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    /**
     * Check that two crossing waterways on different layers are not flagged
     */
    @Test
    public void testCrossingWaterwayDifferentLayers()
    {
        this.verifier.actual(this.atlases.getCrossingWaterwaysDifferentLayers(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    /**
     * Check that a dead end waterway is flagged
     */
    @Test
    public void testDeadendWaterway()
    {
        this.verifier.actual(this.atlases.getDeadendWaterway(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Check that two connected dead end waterways are flagged
     */
    @Test
    public void testDeadendWaterways()
    {
        this.verifier.actual(this.atlases.getDeadendWaterways(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Check that layers are properly accounted for when two waterways connect, but have different
     * layers
     */
    @Test
    public void testDifferingLayersConnectedWaterway()
    {
        this.verifier.actual(this.atlases.getDifferingLayersConnectedWaterway(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    /**
     * Check that a waterway ending inside a sinkhole area is not flagged
     */
    @Test
    public void testSinkholeAreaWaterway()
    {
        this.verifier.actual(this.atlases.getSinkholeArea(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Check that a waterway that ends with a sinkhole point is not flagged
     */
    @Test
    public void testSinkholePointWaterway()
    {
        this.verifier.actual(this.atlases.getSinkholePoint(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    /**
     * Check that a waterway ending in an ocean is not flagged
     */
    @Test
    public void testWaterwayEndingInOceanArea()
    {
        this.verifier.actual(this.atlases.getWaterwayEndingInOceanArea(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    /**
     * Check that a waterway ending in a strait is not flagged
     */
    @Test
    public void testWaterwayEndingInStraitArea()
    {
        this.verifier.actual(this.atlases.getWaterwayEndingInStraitArea(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }

    /**
     * Test that a waterway that ends on another waterway (edge) is appropriately accounted for
     */
    @Test
    public void testWaterwayEndingOnOtherWaterway()
    {
        this.verifier.actual(this.atlases.getWaterwayEndingOnOtherWaterway(),
                new WaterWayCheck(ConfigurationResolver.emptyConfiguration()));
        this.verifier.verifyEmpty();
    }
}
