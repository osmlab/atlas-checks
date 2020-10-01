package org.openstreetmap.atlas.checks.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Test class for {@link ElevationUtilities}.
 *
 * @author Taylor Smock
 */
public class ElevationUtilitiesTest
{

    private static ElevationUtilities elevationUtilities;

    /**
     * Perform initial setup of the test
     */
    @BeforeClass
    public static void setUp()
    {
        elevationUtilities = new ElevationUtilities(ConfigurationResolver.emptyConfiguration());
        prefillData();
    }

    private static void prefillData()
    {
        // 4x4 array of shorts
        final ByteBuffer byteBuffer = ByteBuffer.allocate(32);
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        for (short i = 0; i < 16; i++)
        {
            byteBuffer.putShort(i);
        }
        try (InputStream inputStream = new ByteArrayInputStream(byteBuffer.array()))
        {
            final Method readStream = ElevationUtilities.class.getDeclaredMethod("readStream",
                    InputStream.class);
            readStream.setAccessible(true);
            short[][] data = (short[][]) readStream.invoke(elevationUtilities, inputStream);

            final Field loadedSrtmField = ElevationUtilities.class.getDeclaredField("loadedSrtm");
            loadedSrtmField.setAccessible(true);
            // This is the return value. If it changes, this needs to change.
            @SuppressWarnings("unchecked")
            final Map<Pair<Integer, Integer>, short[][]> loadedSrtm = (Map<Pair<Integer, Integer>, short[][]>) loadedSrtmField
                    .get(elevationUtilities);
            data[0][0] = 2;
            loadedSrtm.put(Pair.of(-1, 0), data);
            data = Arrays.copyOf(data, data.length);
            data[0][0] = 1;
            loadedSrtm.put(Pair.of(0, 0), data);
            data = Arrays.copyOf(data, data.length);
            data[0][0] = 3;
            loadedSrtm.put(Pair.of(-1, -1), data);
            data = Arrays.copyOf(data, data.length);
            data[0][0] = 4;
            loadedSrtm.put(Pair.of(0, -1), data);
        }
        catch (IOException | NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e)
        {
            throw new AssertionError(e);
        }
    }

    /**
     * Test method for {@link ElevationUtilities#ElevationUtilities}
     * 
     * @throws ReflectiveOperationException
     *             when something goes wrong with reflection
     */
    @Test
    public void testConstructors() throws ReflectiveOperationException
    {
        final double expectedSrtmExtent = 2;
        final String expectedSrtmExtension = "srtm";
        final String expectedSrtmPath = "/tmp";
        final String configuration = MessageFormat.format(
                "'{'\"ElevationUtilities\":'{'\"elevation.srtm_extent\": {0}, \"elevation.srtm_ext\": \"{1}\", \"elevation.path\": \"{2}\"'}}'",
                Double.toString(expectedSrtmExtent), expectedSrtmExtension, expectedSrtmPath);
        final ElevationUtilities config = new ElevationUtilities(
                ConfigurationResolver.inlineConfiguration(configuration));
        final ElevationUtilities custom = new ElevationUtilities(expectedSrtmExtent,
                expectedSrtmExtension, expectedSrtmPath);
        final Field srtmExtentField = ElevationUtilities.class.getDeclaredField("srtmExtent");
        final Field srtmExtensionField = ElevationUtilities.class.getDeclaredField("srtmExtension");
        final Field srtmPathField = ElevationUtilities.class.getDeclaredField("srtmPath");
        for (final Field field : Arrays.asList(srtmExtentField, srtmExtensionField, srtmPathField))
        {
            field.setAccessible(true);
        }
        for (final ElevationUtilities elevationUtility : Arrays.asList(config, custom))
        {
            assertEquals(expectedSrtmExtent, srtmExtentField.getDouble(elevationUtility), 0.001);
            assertEquals(expectedSrtmExtension,
                    srtmExtensionField.get(elevationUtility).toString());
            assertEquals(expectedSrtmPath, srtmPathField.get(elevationUtility).toString());

        }
    }

    /**
     * Test method for {@link ElevationUtilities#getElevation(Location)}.
     */
    @Test
    public void testGetElevation()
    {
        // 4x4 on 1 degree grids
        // 0..0.25..0.5..0.75..1
        // 0..1..2..3
        // 4..5..6..7
        // 8..9..10.11
        // 12.13.14.15

        final double degrees = 0.33;
        assertEquals(9, elevationUtilities
                .getElevation(new Location(Latitude.degrees(degrees), Longitude.degrees(degrees))));
        assertEquals(5, elevationUtilities.getElevation(
                new Location(Latitude.degrees(-degrees), Longitude.degrees(degrees))));
        assertEquals(10, elevationUtilities.getElevation(
                new Location(Latitude.degrees(degrees), Longitude.degrees(-degrees))));
        assertEquals(6, elevationUtilities.getElevation(
                new Location(Latitude.degrees(-degrees), Longitude.degrees(-degrees))));
        assertEquals(ElevationUtilities.NO_ELEVATION, elevationUtilities
                .getElevation(new Location(Latitude.degrees(89), Longitude.degrees(0))));
    }

    /**
     * Test method for {@link ElevationUtilities#getIncline(Location, Location)}.
     */
    @Test
    public void testGetIncline()
    {
        final Location one = new Location(Latitude.degrees(0.75), Longitude.degrees(0.75));
        final Location two = new Location(Latitude.degrees(0.74), Longitude.degrees(0.74));
        final double incline = elevationUtilities.getIncline(one, two);
        assertEquals(incline,
                100 * (elevationUtilities.getElevation(two) - elevationUtilities.getElevation(one))
                        / two.distanceTo(one).asMeters(),
                0.001);
        final Location fakeLocation = new Location(Latitude.degrees(89), Longitude.degrees(0));
        assertTrue(Double.isNaN(elevationUtilities.getIncline(fakeLocation, two)));
        assertTrue(Double.isNaN(elevationUtilities.getIncline(two, fakeLocation)));

    }

    /**
     * Test method for {@link ElevationUtilities#getResolution(Location)}.
     */
    @Test
    public void testGetResolution()
    {
        final Distance resolution = elevationUtilities.getResolution(Location.CENTER);
        // 2 * pi * 6371 / (360 * 4) -- 6371 is the average earths radius used by Atlas
        assertEquals(27798.73166, resolution.asMeters(), 0.001);

        assertEquals(Distance.MAXIMUM, elevationUtilities.getResolution(Location.CROSSING_85_280));
    }

    /**
     * Test method for {@link ElevationUtilities#inSameDataPoint(Location, Location)}.
     */
    @Test
    public void testInSameDataPoint()
    {
        // 4x4 on 1 degree grids
        // 0...0.25...0.5...0.75...1
        Location one = new Location(Latitude.degrees(0.75), Longitude.degrees(0.75));
        Location two = new Location(Latitude.degrees(0.76), Longitude.degrees(0.76));
        assertTrue(elevationUtilities.inSameDataPoint(one, two));
        two = new Location(Latitude.degrees(0.74), Longitude.degrees(0.74));
        assertFalse(elevationUtilities.inSameDataPoint(one, two));
        one = new Location(Latitude.degrees(-0.74), Longitude.degrees(0.74));
        assertFalse(elevationUtilities.inSameDataPoint(one, two));

        assertFalse(elevationUtilities.inSameDataPoint(Location.CROSSING_85_17,
                Location.CROSSING_85_280));
        assertFalse(elevationUtilities.inSameDataPoint(one, Location.CROSSING_85_280));
    }

}
