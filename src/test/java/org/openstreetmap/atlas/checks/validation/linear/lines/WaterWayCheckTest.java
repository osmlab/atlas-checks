package org.openstreetmap.atlas.checks.validation.linear.lines;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.utility.ElevationUtilities;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.utilities.collections.Iterables;

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

    private WaterWayCheck defaultWaterWayCheck;

    @Before
    public void setUp()
    {
        this.defaultWaterWayCheck = new WaterWayCheck(ConfigurationResolver.emptyConfiguration(),
                null);
    }

    /**
     * Look for waterways that connect with themselves
     */
    @Test
    public void testCircularWaterway()
    {
        this.verifier.actual(this.atlases.getCircularWaterway(), this.defaultWaterWayCheck);
        this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
        // We don't care about the order of the instructions
        this.verifier.verify(flag -> assertTrue(flag.getRawInstructions().stream().anyMatch(
                "The waterway 0 loops back on itself. This is typically impossible."::equals)));
        this.verifier.verify(flag -> assertTrue(flag.getRawInstructions().stream().anyMatch(
                "The waterway 0 does not end in a sink (ocean/sinkhole/waterway/drain)."::equals)));
        this.verifier.verify(flag -> assertTrue(flag.getFixSuggestions().isEmpty()));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Ensure that waterways that end inside oceans are not flagged
     */
    @Test
    public void testCoastWaterway()
    {
        this.verifier.actual(this.atlases.getCoastlineWaterway(), this.defaultWaterWayCheck);
        this.verifier.verifyEmpty();
    }

    /**
     * Ensure that waterways that end on an ocean is not flagged
     */
    @Test
    public void testCoastWaterwayConnected()
    {
        this.verifier.actual(this.atlases.getCoastlineWaterwayConnected(),
                this.defaultWaterWayCheck);
        this.verifier.verifyEmpty();
    }

    @Test
    public void testCoastWaterwayConnectedElevation() throws ReflectiveOperationException
    {
        final short[][] map = new short[][] { { 15, 14, 13, 12 }, { 11, 10, 9, 8 }, { 7, 6, 5, 4 },
                { 3, 2, 1, 0 } };
        final WaterWayCheck check = new WaterWayCheck(
                ConfigurationResolver.inlineConfiguration(
                        "{\"WaterWayCheck.waterway.elevation.resolution.min.uphill\": 30000.0}"),
                null);
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
        final WaterWayCheck check = new WaterWayCheck(
                ConfigurationResolver.inlineConfiguration(
                        "{\"WaterWayCheck.waterway.elevation.resolution.min.uphill\": 30000.0}"),
                null);

        final Field elevationUtilsField = WaterWayCheck.class.getDeclaredField("elevationUtils");
        elevationUtilsField.setAccessible(true);
        final ElevationUtilities elevationUtils = (ElevationUtilities) elevationUtilsField
                .get(check);
        elevationUtils.putMap(Location.forString("16.9906416,-88.3188021"), map);
        this.verifier.actual(this.atlases.getCoastlineWaterwayConnected(), check);
        this.verifier.verify(flag -> assertEquals(1, flag.getRawInstructions().size()));
        this.verifier.verify(flag -> assertEquals(
                "The waterway 130 probably does not go up hill.\nPlease check (source elevation data resolution was about 26,585.386 meters).",
                flag.getRawInstructions().get(0)));
        this.verifier.verify(flag -> assertEquals(1, flag.getFixSuggestions().size()));
        this.verifier.verify(flag ->
        {
            final FeatureChange suggestion = flag.getFixSuggestions().iterator().next();
            final Line after = (Line) suggestion.getAfterView();
            final Line before = (Line) suggestion.getBeforeView();
            assertNull(suggestion.getTags());
            final List<Location> afterLocations = Iterables.stream(after).collectToList();
            final List<Location> beforeLocations = Iterables.stream(before).collectToList();
            assertFalse(ListUtils.isEqualList(afterLocations, beforeLocations));
            Collections.reverse(beforeLocations);
            assertTrue(ListUtils.isEqualList(afterLocations, beforeLocations));
        });
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testCoastWaterwayConnectedElevationReversedLongDistance()
            throws ReflectiveOperationException
    {
        final short[][] map = { { 0, 1, 2, 3 }, { 4, 5, 6, 7 }, { 8, 9, 10, 11 },
                { 12, 13, 14, 15 } };
        final WaterWayCheck check = new WaterWayCheck(ConfigurationResolver.inlineConfiguration(
                "{\"WaterWayCheck\": {\"waterway.elevation.resolution.min.uphill\": 30000.0, \"waterway.elevation.distance.min.start.end\": 1.0}}"),
                null);

        final Field elevationUtilsField = WaterWayCheck.class.getDeclaredField("elevationUtils");
        elevationUtilsField.setAccessible(true);
        final ElevationUtilities elevationUtils = (ElevationUtilities) elevationUtilsField
                .get(check);
        elevationUtils.putMap(Location.forString("16.9906416,-88.3188021"), map);
        this.verifier.actual(this.atlases.getCoastlineWaterwayConnected(), check);
        this.verifier.verify(flag -> assertEquals(1, flag.getRawInstructions().size()));
        this.verifier.verify(flag -> assertEquals("The waterway 130 probably does not go up hill.\n"
                + "Please check (source elevation data resolution was about 26,585.386 meters).",
                flag.getRawInstructions().get(0)));
        this.verifier.verify(flag -> assertEquals(1, flag.getFixSuggestions().size()));
        this.verifier.verify(flag ->
        {
            final FeatureChange suggestion = flag.getFixSuggestions().iterator().next();
            final Line after = (Line) suggestion.getAfterView();
            final Line before = (Line) suggestion.getBeforeView();
            assertNull(suggestion.getTags());
            final List<Location> afterLocations = Iterables.stream(after).collectToList();
            final List<Location> beforeLocations = Iterables.stream(before).collectToList();
            assertFalse(ListUtils.isEqualList(afterLocations, beforeLocations));
            Collections.reverse(beforeLocations);
            assertTrue(ListUtils.isEqualList(afterLocations, beforeLocations));
        });
        this.verifier.verifyExpectedSize(1);
    }

    @Test
    public void testCoastWaterwayConnectedElevationReversedLowResolution()
            throws ReflectiveOperationException
    {
        final short[][] map = new short[][] { { 0, 1, 2, 3 }, { 4, 5, 6, 7 }, { 8, 9, 10, 11 },
                { 12, 13, 14, 15 } };

        final Field elevationUtilsField = WaterWayCheck.class.getDeclaredField("elevationUtils");
        elevationUtilsField.setAccessible(true);
        final ElevationUtilities elevationUtils = (ElevationUtilities) elevationUtilsField
                .get(this.defaultWaterWayCheck);
        elevationUtils.putMap(Location.forString("16.9906416,-88.3188021"), map);
        this.verifier.actual(this.atlases.getCoastlineWaterwayConnected(),
                this.defaultWaterWayCheck);
        this.verifier.verifyEmpty();
    }

    /**
     * Check that a waterway ending in an ocean is not flagged
     */
    @Test
    public void testCoastWaterwayReversed()
    {
        this.verifier.actual(this.atlases.getCoastlineWaterwayReversed(),
                this.defaultWaterWayCheck);
        this.verifier.verify(flag -> assertEquals(1, flag.getRawInstructions().size()));
        this.verifier.verify(flag -> assertEquals(
                "The waterway 542 does not end in a sink (ocean/sinkhole/waterway/drain).\n"
                        + "The waterway crosses a coastline, which means it is possible for the coastline to have an incorrect direction.\n"
                        + "Land should be to the LEFT of the coastline and the ocean should be to the RIGHT of the coastline (for more information, see https://wiki.osm.org/Tag:natural=coastline).",
                flag.getRawInstructions().get(0)));
        this.verifier.verify(flag -> assertTrue(flag.getFixSuggestions().isEmpty()));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Check that two crossing waterways are flagged
     */
    @Test
    public void testCrossingWaterway()
    {
        this.verifier.actual(this.atlases.getCrossingWaterways(), this.defaultWaterWayCheck);
        this.verifier.verify(flag -> assertEquals(1, flag.getRawInstructions().size()));
        this.verifier.verify(flag -> assertEquals("The waterway 0 crosses the waterway 0.",
                flag.getRawInstructions().get(0)));
        this.verifier.verify(flag -> assertTrue(flag.getFixSuggestions().isEmpty()));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Check that two connected crossing waterways are flagged
     */
    @Test
    public void testCrossingWaterwayConnected()
    {
        this.verifier.actual(this.atlases.getCrossingWaterwaysConnected(),
                this.defaultWaterWayCheck);
        this.verifier.verifyEmpty();
    }

    /**
     * Check that two crossing waterways on different layers are not flagged
     */
    @Test
    public void testCrossingWaterwayDifferentLayers()
    {
        this.verifier.actual(this.atlases.getCrossingWaterwaysDifferentLayers(),
                this.defaultWaterWayCheck);
        this.verifier.verifyEmpty();
    }

    /**
     * Check that a dead end waterway is flagged
     */
    @Test
    public void testDeadendWaterway()
    {
        this.verifier.actual(this.atlases.getDeadendWaterway(), this.defaultWaterWayCheck);
        this.verifier.verify(flag -> assertEquals(1, flag.getRawInstructions().size()));
        this.verifier.verify(flag -> assertEquals(
                "The waterway 538 does not end in a sink (ocean/sinkhole/waterway/drain).",
                flag.getRawInstructions().get(0)));
        this.verifier.verify(flag -> assertTrue(flag.getFixSuggestions().isEmpty()));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Check that two connected dead end waterways are flagged
     */
    @Test
    public void testDeadendWaterways()
    {
        this.verifier.actual(this.atlases.getDeadendWaterways(), this.defaultWaterWayCheck);
        this.verifier.verify(flag -> assertEquals(1, flag.getRawInstructions().size()));
        this.verifier.verify(flag -> assertEquals(
                "The waterway 538 does not end in a sink (ocean/sinkhole/waterway/drain).",
                flag.getRawInstructions().get(0)));
        this.verifier.verify(flag -> assertTrue(flag.getFixSuggestions().isEmpty()));
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
                this.defaultWaterWayCheck);
        this.verifier.verifyEmpty();
    }

    /**
     * Check that a waterway ending inside a sinkhole area is not flagged
     */
    @Test
    public void testSinkholeAreaWaterway()
    {
        this.verifier.actual(this.atlases.getSinkholeArea(), this.defaultWaterWayCheck);
        this.verifier.verify(flag -> assertEquals(1, flag.getRawInstructions().size()));
        this.verifier.verify(flag -> assertEquals(
                "The waterway 538 does not end in a sink (ocean/sinkhole/waterway/drain).",
                flag.getRawInstructions().get(0)));
        this.verifier.verify(flag -> assertTrue(flag.getFixSuggestions().isEmpty()));
        this.verifier.verifyExpectedSize(1);
    }

    /**
     * Check that a waterway that ends with a sinkhole point is not flagged
     */
    @Test
    public void testSinkholePointWaterway()
    {
        this.verifier.actual(this.atlases.getSinkholePoint(), this.defaultWaterWayCheck);
        this.verifier.verifyEmpty();
    }

    /**
     * Check that a waterway ending in an ocean is not flagged
     */
    @Test
    public void testWaterwayEndingInOceanArea()
    {
        this.verifier.actual(this.atlases.getWaterwayEndingInOceanArea(),
                this.defaultWaterWayCheck);
        this.verifier.verifyEmpty();
    }

    /**
     * Check that a waterway ending in a strait is not flagged
     */
    @Test
    public void testWaterwayEndingInStraitArea()
    {
        this.verifier.actual(this.atlases.getWaterwayEndingInStraitArea(),
                this.defaultWaterWayCheck);
        this.verifier.verifyEmpty();
    }

    /**
     * Test that a waterway that ends on another waterway (edge) is appropriately accounted for
     */
    @Test
    public void testWaterwayEndingOnOtherWaterway()
    {
        this.verifier.actual(this.atlases.getWaterwayEndingOnOtherWaterway(),
                this.defaultWaterWayCheck);
        this.verifier.verifyEmpty();
    }
}
