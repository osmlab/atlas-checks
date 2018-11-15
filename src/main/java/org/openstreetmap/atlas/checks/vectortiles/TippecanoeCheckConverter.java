package org.openstreetmap.atlas.checks.vectortiles;

import org.openstreetmap.atlas.geography.atlas.geojson.LineDelimitedGeoJsonConverter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.vectortiles.TippecanoeCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This is a simple CLI that will take tippecanoe line-delimited GeoJSON atlas check output and convert it into vector tiles with tippecanoe.
 *
 * @author hallahan
 */
public class TippecanoeCheckConverter extends Command
{
    private static final int EXIT_FAILURE = 1;

    private static final Logger logger = LoggerFactory.getLogger(TippecanoeCheckConverter.class);

    private static final Command.Switch<Path> GEOJSON_DIRECTORY = new Command.Switch<>("geojsonDirectory",
            "The directory to read line-delimited GeoJSON.", Paths::get, Command.Optionality.REQUIRED);

    private static final Switch<Path> MBTILES = new Switch<>("mbtiles",
            "The MBTiles file to which tippecanoe will write vector tiles.", Paths::get,
            Optionality.REQUIRED);

    private static final Command.Switch<Boolean> OVERWRITE = new Command.Switch<>("overwrite",
            "Choose to automatically overwrite an MBTiles file if it exists at the given path.",
            Boolean::new, Command.Optionality.OPTIONAL, "false");

    public static void main(final String[] args)
    {
        new TippecanoeCheckConverter().run(args);
    }

    @Override
    protected int onRun(CommandMap command) {
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

        LineDelimitedGeoJsonConverter.concatenate(geojsonDirectory);

        TippecanoeCommands.runTippecanoe(geojson, mbtiles, overwrite, TippecanoeCheckSettings.ARGS);

        return 0;
    }

    @Override
    protected SwitchList switches() {
        return new SwitchList().with(GEOJSON_DIRECTORY, MBTILES, OVERWRITE);
    }
}
