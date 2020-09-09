package org.openstreetmap.atlas.checks.utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Used to determine assumed direction for some features (e.g. waterways). Assumptions are that any
 * SRTM file follows the specification here:
 * https://dds.cr.usgs.gov/srtm/version2_1/Documentation/Quickstart.pdf
 *
 * @author Taylor Smock
 */
public final class ElevationUtilities implements Serializable
{
    private static final long serialVersionUID = -5929570973909280629L;
    /**
     * The assumed file extension
     */
    private static final String SRTM_EXTENSION = "hgt";
    /**
     * The assumed extent of a HGT SRTM file (lat/lon) in degrees
     */
    private static final double SRTM_EXTENT = 1;
    /**
     * A non-number when there is no elevation data available. This is currently returns
     * {@link Short#MIN_VALUE}.
     */
    public static final short NO_ELEVATION = Short.MIN_VALUE;

    private static final short[][] EMPTY_MAP = new short[][] {};

    /** Just an int for converting a decimal to a percentage */
    private static final int DECIMAL_TO_PERCENTAGE = 100;

    /** A map of {lat, lon} pairs with a loaded srtm in a byte array */
    private final Map<Pair<Integer, Integer>, short[][]> loadedSrtm = new HashMap<>();

    private final double srtmExtent;

    private final String srtmExtension;

    private final String srtmPath;

    /**
     * Configuration Keys in the Integrity Framework are based on the check simple classname.
     *
     * @param key
     *            key part for a specific configuration item defined for this class
     * @return complete key for lookup
     */
    private static String configurationKey(final String key)
    {
        return formatKey(ElevationUtilities.class.getSimpleName(), key);
    }

    private static <U, V> V configurationValue(final Configuration configuration, final String key,
            final U defaultValue, final Function<U, V> transform)
    {
        return configuration.get(configurationKey(key), defaultValue, transform).value();
    }

    private static String formatKey(final String name, final String key)
    {
        return String.format("%s.%s", name, key);
    }

    /**
     * Create a configured ElevationUtilities
     *
     * @param configuration
     *            A configuration which should (at a minimum) have a file path for elevation files.
     */
    public ElevationUtilities(final Configuration configuration)
    {
        this(configurationValue(configuration, "elevation.srtm_extent", SRTM_EXTENT,
                Function.identity()),
                configurationValue(configuration, "elevation.srtm_ext", SRTM_EXTENSION,
                        Function.identity()),
                configurationValue(configuration, "elevation.path", "elevation",
                        Function.identity()));

    }

    /**
     * Create a configured ElevationUtilities
     *
     * @param srtmExtent
     *            The extent of the files. The units are in degrees, and the default is 1 degree.
     * @param srtmExtension
     *            The extension of the files. Archive/compression file endings are not required
     *            (they will be automatically detected).
     * @param srtmPath
     *            The path for the files.
     */
    public ElevationUtilities(final double srtmExtent, final String srtmExtension,
            final String srtmPath)
    {
        this.srtmExtension = srtmExtension;
        this.srtmExtent = srtmExtent;
        this.srtmPath = srtmPath;
    }

    /**
     * Get the elevation of a location
     *
     * @param location
     *            The location to get the elevation of
     * @return The meters of the elevation (does not show fractions of meters)
     */
    public short getElevation(final Location location)
    {
        final short[][] map = getMap(location);
        if (Arrays.equals(EMPTY_MAP, map))
        {
            return NO_ELEVATION;
        }
        final int[] index = getIndex(location, map.length);
        return map[index[0]][index[1]];
    }

    /**
     * Get the incline between two points
     *
     * @param start
     *            The start of the slope
     * @param end
     *            The end of the slope
     * @return The incline from the start to the end (may be negative). May also return
     *         {@link Double#NaN} if one of the elevations cannot be obtained.
     * @see <a href="https://wiki.openstreetmap.org/wiki/Key:incline">OSM Wiki Incline</a>
     */
    public double getIncline(final Location start, final Location end)
    {
        final short startElevation = getElevation(start);
        final short endElevation = getElevation(end);
        if (startElevation == NO_ELEVATION || endElevation == NO_ELEVATION)
        {
            return Double.NaN;
        }
        final double distance = end.distanceTo(start).asMeters();
        return ((endElevation - startElevation) / distance) * DECIMAL_TO_PERCENTAGE;
    }

    /**
     * Get the index to use for a short[latitude][longitude] = height in meters array. Generally,
     * this is used to get data from a map obtained via {@link #getMap(Location)}. You also need
     * that map for the {@code mapSize} parameter.
     *
     * @param location
     *            The location to get the index for
     * @param mapSize
     *            The size of the map
     * @return A [latitude, longitude] = int (index) array.
     */
    public int[] getIndex(final Location location, final int mapSize)
    {
        final double latDegrees = location.getLatitude().asDegrees();
        final double lonDegrees = location.getLongitude().asDegrees();

        final float fraction = ((float) this.srtmExtent) / mapSize;
        int latitude = (int) Math.floor(Math.abs(latDegrees - (int) latDegrees) / fraction);
        int longitude = (int) Math.floor(Math.abs(lonDegrees - (int) lonDegrees) / fraction);
        if (latDegrees >= 0)
        {
            latitude = mapSize - 1 - latitude;
        }
        if (lonDegrees < 0)
        {
            longitude = mapSize - 1 - longitude;
        }
        return new int[] { latitude, longitude };
    }

    /**
     * Get the map for a specified location. Unless you need the raw map, you should prefer
     * {@link #getElevation}. This also allows you to directly modify a map (i.e., you know that
     * there is elevation data around a point, but the point is in a {@link #NO_ELEVATION} area). To
     * get the index of a {@link Location}, use {@link #getIndex(Location, int)}. You need the
     * {@code short[][]} from this method.
     *
     * @param location
     *            The location to get the height map for
     * @return A short[latitude][longitude] = height in meters array
     */
    public short[][] getMap(final Location location)
    {
        final double latDegrees = location.getLatitude().asDegrees();
        final double lonDegrees = location.getLongitude().asDegrees();
        final int lat = (int) Math.floor(latDegrees);
        final int lon = (int) Math.floor(lonDegrees);
        return this.loadedSrtm.computeIfAbsent(Pair.of(lat, lon),
                pair -> loadMap(pair.getLeft(), pair.getRight()));
    }

    /**
     * Get the resolution of the data at the location.
     *
     * @param location
     *            The location to get the resolution of
     * @return The resolution of the data, or {@link Distance#MAXIMUM} if there is no data.
     */
    public Distance getResolution(final Location location)
    {
        final short[][] map = getMap(location);
        if (Arrays.equals(EMPTY_MAP, map))
        {
            return Distance.MAXIMUM;
        }
        final float difference = ((float) this.srtmExtent) / map.length;
        final Location temp = new Location(location.getLatitude(),
                Longitude.degrees(location.getLongitude().asDegrees() + difference));
        return temp.distanceTo(location);
    }

    /**
     * The lower-left corner of each file is the file name. This does not append any archive
     * extensions, it is just the name of the file with the configured SRTM extension.
     *
     * @param latitude
     *            The latitude (lower left)
     * @param longitude
     *            The longitude (lower left)
     * @return The expected filename for the location. You should also check for archives.
     */
    public String getSrtmFileName(final int latitude, final int longitude)
    {
        int lat = latitude;
        int lon = longitude;
        String latPrefix = "N";
        if (lat < 0)
        {
            lat = Math.abs(lat);
            latPrefix = "S";
        }

        String lonPrefix = "E";
        if (lon < 0)
        {
            lon = Math.abs(lon);
            lonPrefix = "W";
        }

        return String.format("%s%02d%s%03d.%s", latPrefix, lat, lonPrefix, lon, this.srtmExtension);
    }

    /**
     * Check if two locations are in the same data point (i.e., same pixel in a HGT)
     *
     * @param one
     *            A location to check
     * @param two
     *            Another location to check
     * @return {@code true} if they are in the same grid and location
     */
    public boolean inSameDataPoint(final Location one, final Location two)
    {
        final short[][] mapOne = getMap(one);
        final short[][] mapTwo = getMap(two);
        if (Arrays.equals(mapOne, mapTwo) && !Arrays.equals(EMPTY_MAP, mapOne))
        {
            final int[] indexOne = getIndex(one, mapOne.length);
            final int[] indexTwo = getIndex(two, mapTwo.length);
            if (Arrays.equals(indexOne, indexTwo))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Associate a map with a location. This is mostly useful for test methods. You should use
     * {@link #getElevation} instead (it automatically loads appropriate maps).
     *
     * @param location
     *            The location to use for the map location (uses the lower-left corner of the 1
     *            degree by 1 degree box).
     * @param map
     *            The map to associate
     */
    public void putMap(final Location location, final short[][] map)
    {
        final double latDegrees = location.getLatitude().asDegrees();
        final double lonDegrees = location.getLongitude().asDegrees();
        final int lat = (int) Math.floor(latDegrees);
        final int lon = (int) Math.floor(lonDegrees);
        this.loadedSrtm.put(Pair.of(lat, lon), map);
    }

    /**
     * Load a map for a specified latitude and longitude
     *
     * @param lat
     *            The latitude to use
     * @param lon
     *            The longitude to use
     * @return A short[latitude][longitude] = height in meters array
     */
    private synchronized short[][] loadMap(final int lat, final int lon)
    {
        final String filename = getSrtmFileName(lat, lon);
        Path path = Paths.get(this.srtmPath, filename);
        if (!path.toFile().isFile())
        {
            for (final String ext : new String[] { "zip", "gz", "xz", "bz", "bz2", "tar" })
            {
                path = Paths.get(this.srtmPath, filename + "." + ext);
                if (path.toFile().isFile())
                {
                    break;
                }
            }
        }
        if (!path.toFile().isFile())
        {
            return EMPTY_MAP;
        }
        try (InputStream is = CompressionUtilities
                .getUncompressedInputStream(Files.newInputStream(path)))
        {
            return readStream(is);
        }
        catch (final IOException e)
        {
            return EMPTY_MAP;
        }
    }

    /**
     * Read an input stream into a short[][]
     *
     * @param inputStream
     *            The inputstream to read
     * @return A short[][] where short[latitude][longitude] = height in meters
     * @throws IOException
     *             If the inputstream throws an IOException
     */
    private short[][] readStream(final InputStream inputStream) throws IOException
    {
        int squareSize = 1;
        short[] data = new short[(int) Math.pow(squareSize, 2)];
        // srtm (hgt) is in big-endian
        int index = 0;
        final ByteBuffer byteBuffer = ByteBuffer.wrap(inputStream.readAllBytes());
        byteBuffer.order(ByteOrder.BIG_ENDIAN);
        while (byteBuffer.hasRemaining())
        {
            data[index] = byteBuffer.getShort();
            index++;
            if (index >= data.length && byteBuffer.hasRemaining())
            {
                squareSize += 1;
                data = Arrays.copyOf(data, (int) Math.pow(squareSize, 2));
            }
        }

        final short[][] realData = new short[squareSize][squareSize];
        for (int latitude = 0; latitude < squareSize; latitude++)
        {
            for (int longitude = 0; longitude < squareSize; longitude++)
            {
                realData[latitude][longitude] = data[latitude * squareSize + longitude];
            }
        }
        return realData;
    }
}
