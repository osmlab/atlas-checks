package org.openstreetmap.atlas.checks.distributed;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.atlas.CountrySpecificAtlasFilePathFilter;
import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.constants.CommonConstants;
import org.openstreetmap.atlas.checks.maproulette.MapRouletteConfiguration;
import org.openstreetmap.atlas.generator.tools.filesystem.FileSystemHelper;
import org.openstreetmap.atlas.generator.tools.spark.SparkJob;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles arguments and base functionality for integrity check sparkjobs generating commands
 *
 * @author jklamer
 * @author bbreithaupt
 */
public abstract class IntegrityChecksCommandArguments extends SparkJob
{
    /**
     * @author brian_l_davis
     */
    protected enum OutputFormats
    {
        FLAGS,
        GEOJSON,
        METRICS,
        TIPPECANOE
    }

    /**
     * @deprecated in favor of INPUT from SparkJob
     */
    @Deprecated(since = "6.0.2")
    protected static final Switch<String> ATLAS_FOLDER = new Switch<>("inputFolder",
            "Path of folder which contains Atlas file(s)", StringConverter.IDENTITY,
            Optionality.OPTIONAL);
    protected static final String OUTPUT_ATLAS_FOLDER = "atlas";
    // Outputs
    protected static final String OUTPUT_FLAG_FOLDER = "flag";
    protected static final String OUTPUT_GEOJSON_FOLDER = "geojson";
    protected static final String OUTPUT_METRIC_FOLDER = "metric";
    protected static final String OUTPUT_TIPPECANOE_FOLDER = "tippecanoe";
    static final Switch<List<String>> CHECK_FILTER = new Switch<>("checkFilter",
            "Comma-separated list of checks to run",
            checks -> Arrays.asList(checks.split(CommonConstants.COMMA)), Optionality.OPTIONAL);
    // Configuration
    static final Switch<StringList> CONFIGURATION_FILES = new Switch<>("configFiles",
            "Comma-separated list of configuration datasources.",
            value -> StringList.split(value, CommonConstants.COMMA), Optionality.OPTIONAL);
    static final Switch<String> CONFIGURATION_JSON = new Switch<>("configJson",
            "Json formatted configuration.", StringConverter.IDENTITY, Optionality.OPTIONAL);
    static final Switch<Long> MAXPOOLMINUTES = new Switch<>("maxPoolMinutes",
            "Maximum number of minutes for pool duration.", Long::valueOf, Optionality.OPTIONAL);
    static final Switch<String> COUNTRIES = new Switch<>("countries",
            "Comma-separated list of country ISO3 codes to be processed", StringConverter.IDENTITY,
            Optionality.REQUIRED);
    static final Switch<MapRouletteConfiguration> MAP_ROULETTE = new Switch<>("maproulette",
            "Map roulette server information, format <Host>:<Port>:<ProjectName>:<ApiKey>, projectName is optional.",
            MapRouletteConfiguration::parse, Optionality.OPTIONAL);
    static final Switch<Set<OutputFormats>> OUTPUT_FORMATS = new Switch<>("outputFormats",
            "Comma-separated list of output formats (flags, metrics, geojson, tippecanoe).",
            csvFormats -> Stream.of(csvFormats.split(","))
                    .map(format -> Enum.valueOf(OutputFormats.class, format.toUpperCase()))
                    .collect(Collectors.toSet()),
            Optionality.OPTIONAL, "flags,metrics");
    static final Switch<Rectangle> PBF_BOUNDING_BOX = new Switch<>("pbfBoundingBox",
            "OSM protobuf data will be loaded only in this bounding box", Rectangle::forString,
            Optionality.OPTIONAL);
    static final Switch<Boolean> PBF_SAVE_INTERMEDIATE_ATLAS = new Switch<>("savePbfAtlas",
            "Saves intermediate atlas files created when processing OSM protobuf data.",
            Boolean::valueOf, Optionality.OPTIONAL, "false");
    private static final String ATLAS_FILENAME_PATTERN_FORMAT = "^%s_([0-9]+)-([0-9]+)-([0-9]+)";
    private static final Logger logger = LoggerFactory
            .getLogger(IntegrityChecksCommandArguments.class);
    private static final long serialVersionUID = 3411367641498888770L;

    /**
     * Creates a map from country name to {@link List} of {@link Shard} definitions from
     * {@link Atlas} files.
     *
     * @param countries
     *            Set of countries to find out shards for
     * @param atlasFolder
     *            Path to {@link Atlas} folder
     * @param sparkContext
     *            Spark context (or configuration) as a key-value map
     * @return A map from country name to {@link List} of {@link Shard} definitions
     */
    public static MultiMap<String, Shard> countryShardMapFromShardFiles(final Set<String> countries,
            final String atlasFolder, final Map<String, String> sparkContext)
    {
        final MultiMap<String, Shard> countryShardMap = new MultiMap<>();
        logger.info("Building country shard map from country shard files.");

        countries.forEach(country ->
        {
            final String countryDirectory = SparkFileHelper.combine(atlasFolder, country);
            final CountrySpecificAtlasFilePathFilter atlasFilter = new CountrySpecificAtlasFilePathFilter(
                    country);
            final Pattern atlasFilePattern = Pattern
                    .compile(String.format(ATLAS_FILENAME_PATTERN_FORMAT, country));

            // Go over shard files for the country and use file name pattern to find out shards
            FileSystemHelper.streamPathsRecursively(countryDirectory, sparkContext, atlasFilter, 0)
                    .forEach(shardFile ->
                    {
                        final String shardFileName = shardFile.getName();
                        final Matcher matcher = atlasFilePattern.matcher(shardFileName);
                        if (matcher.find())
                        {
                            try
                            {
                                final String zoomString = matcher.group(1);
                                final String xString = matcher.group(2);
                                final String yString = matcher.group(3);
                                countryShardMap.add(country,
                                        new SlippyTile(Integer.parseInt(xString),
                                                Integer.parseInt(yString),
                                                Integer.parseInt(zoomString)));
                            }
                            catch (final Exception e)
                            {
                                logger.warn(String.format("Couldn't parse shard file name %s.",
                                        shardFileName), e);
                            }
                        }
                        else
                        {
                            logger.warn(String.format(
                                    "Skipping atlas file %s, its name does not conform to the sharded standard.",
                                    shardFileName));
                        }
                    });
        });

        return countryShardMap;
    }

    protected static Iterable<AtlasObject> objectsToCheck(final Atlas atlas, final Check check)
    {
        return objectsToCheck(atlas, check, atlasEntity -> true);
    }

    protected static Iterable<AtlasObject> objectsToCheck(final Atlas atlas, final Check check,
            final Predicate<AtlasEntity> geoFilter)
    {
        return new MultiIterable<>(Iterables.filter(atlas.entities(), geoFilter),
                check.finder().map(finder -> finder.find(atlas)).orElse(Collections.emptyList()));
    }

    /**
     * Gets the {@link AtlasDataSource} object to load the Atlas from
     *
     * @param sparkContext
     *            The Spark context
     * @param checksConfiguration
     *            configuration for all the checks
     * @param pbfBoundary
     *            The pbf boundary of type {@link Rectangle}
     * @return A {@link AtlasDataSource}
     */
    protected AtlasDataSource getAtlasDataSource(final Map<String, String> sparkContext,
            final Configuration checksConfiguration, final Rectangle pbfBoundary)
    {
        return new AtlasDataSource(sparkContext, checksConfiguration, pbfBoundary);
    }

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(ATLAS_FOLDER, MAP_ROULETTE, COUNTRIES, CONFIGURATION_FILES,
                CONFIGURATION_JSON, PBF_BOUNDING_BOX, PBF_SAVE_INTERMEDIATE_ATLAS, OUTPUT_FORMATS,
                CHECK_FILTER, MAXPOOLMINUTES);
    }
}
