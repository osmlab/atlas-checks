package org.openstreetmap.atlas.checks.vectortiles;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.geojson.LineDelimitedGeoJsonConverter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.runtime.RunScript;
import org.openstreetmap.atlas.utilities.time.Time;
import org.openstreetmap.atlas.utilities.vectortiles.TippecanoeCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple CLI that will take tippecanoe line-delimited GeoJSON atlas check output and
 * convert it into vector tiles with tippecanoe.
 *
 * @author hallahan
 */
public class TippecanoeConverter extends Command
{
    private static final int EXIT_FAILURE = 1;

    private static final Logger logger = LoggerFactory.getLogger(TippecanoeConverter.class);

    private static final Command.Switch<Path> GEOJSON_DIRECTORY = new Command.Switch<>(
            "geojsonDirectory", "The directory to read line-delimited GeoJSON.", Paths::get,
            Command.Optionality.REQUIRED);

    private static final Switch<Path> MBTILES = new Switch<>("mbtiles",
            "The MBTiles file to which tippecanoe will write vector tiles.", Paths::get,
            Optionality.REQUIRED);

    private static final Command.Switch<Boolean> OVERWRITE = new Command.Switch<>("overwrite",
            "Choose to automatically overwrite an MBTiles file if it exists at the given path.",
            Boolean::new, Command.Optionality.OPTIONAL, "false");

    public static void main(final String[] args)
    {
        new TippecanoeConverter().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        if (!TippecanoeCommands.hasValidTippecanoe())
        {
            logger.error(
                    "Your system does not have a valid installation of tippecanoe installed in its path.");
            logger.error("https://github.com/mapbox/tippecanoe");

            System.exit(EXIT_FAILURE);
        }

        final Path mbtiles = (Path) command.get(MBTILES);
        final Path geojsonDirectory = (Path) command.get(GEOJSON_DIRECTORY);
        final Path geojson = geojsonDirectory.resolve(LineDelimitedGeoJsonConverter.EVERYTHING);
        final Boolean overwrite = (Boolean) command.get(OVERWRITE);

        decompress(geojsonDirectory);
        concatenate(geojsonDirectory);

        TippecanoeCommands.runTippecanoe(geojson, mbtiles, overwrite, TippecanoeCheckSettings.ARGS);

        return 0;
    }

    public static void decompress(final Path geojsonDirectory)
    {
        final Time time = Time.now();
        final String directory = geojsonDirectory.toString();
        final String cat = String.format("gzip -dr '%s'/*/*.geojson.gz", directory);
        final String[] bashCommandArray = new String[] { "bash", "-c", cat };
        try
        {
            RunScript.run(bashCommandArray);
            logger.info("Decompressed line-delimited GeoJSON in {}", time.elapsedSince());
        }
        catch (final CoreException exception)
        {
            logger.warn("Not finding any .geojson.gz to decompress. Continuing...", exception);
        }
    }

    public static void concatenate(final Path geojsonDirectory)
    {
        final Time time = Time.now();
        final String directory = geojsonDirectory.toString();
        final String cat = String.format("cat '%s/'*/*.geojson > '%s/'%s", directory, directory,
                LineDelimitedGeoJsonConverter.EVERYTHING);
        final String[] bashCommandArray = new String[] { "bash", "-c", cat };
        RunScript.run(bashCommandArray);
        logger.info("Concatenated to {} in {}", LineDelimitedGeoJsonConverter.EVERYTHING,
                time.elapsedSince());
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(GEOJSON_DIRECTORY, MBTILES, OVERWRITE);
    }
}
