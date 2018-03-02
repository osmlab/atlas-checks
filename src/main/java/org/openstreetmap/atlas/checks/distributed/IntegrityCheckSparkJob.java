package org.openstreetmap.atlas.checks.distributed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.spark.TaskContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.base.CheckResourceLoader;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.constants.CommonConstants;
import org.openstreetmap.atlas.checks.event.CheckFlagFileProcessor;
import org.openstreetmap.atlas.checks.event.CheckFlagGeoJsonProcessor;
import org.openstreetmap.atlas.checks.event.EventService;
import org.openstreetmap.atlas.checks.event.MetricFileGenerator;
import org.openstreetmap.atlas.checks.maproulette.MapRouletteClient;
import org.openstreetmap.atlas.checks.maproulette.MapRouletteConfiguration;
import org.openstreetmap.atlas.checks.persistence.SparkFileHelper;
import org.openstreetmap.atlas.checks.persistence.SparkFileOutput;
import org.openstreetmap.atlas.checks.persistence.SparkFilePath;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.generator.tools.spark.SparkJob;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

/**
 * Executes integrity checks as a {@link SparkJob}. The job parallelizes on the number of countries.
 * Each slave will process a country and run all enabled checks.
 *
 * @author mgostintsev
 */
public class IntegrityCheckSparkJob extends SparkJob
{
    @Deprecated
    protected static final Switch<String> ATLAS_FOLDER = new Switch<>("inputFolder",
            "Path of folder which contains Atlas file(s)", StringConverter.IDENTITY,
            Optionality.OPTIONAL);
    // Configuration
    private static final Switch<StringList> CONFIGURATION_FILES = new Switch<>("configFiles",
            "Comma-separated list of configuration datasources.",
            value -> StringList.split(value, CommonConstants.COMMA), Optionality.OPTIONAL);
    private static final Switch<String> CONFIGURATION_JSON = new Switch<>("configJson",
            "Json formatted configuration.", StringConverter.IDENTITY, Optionality.OPTIONAL);
    private static final Switch<String> COUNTRIES = new Switch<>("countries",
            "Comma-separated list of country ISO3 codes to be processed", StringConverter.IDENTITY,
            Optionality.REQUIRED);
    private static final Switch<MapRouletteConfiguration> MAP_ROULETTE = new Switch<>("maproulette",
            "Map roulette server information, format <Host>:<Port>:<ProjectName>:<ApiKey>, projectName is optional.",
            MapRouletteConfiguration::parse, Optionality.OPTIONAL);
    private static final Switch<Rectangle> PBF_BOUNDING_BOX = new Switch<>("pbfBoundingBox",
            "OSM protobuf data will be loaded only in this bounding box", Rectangle::forString,
            Optionality.OPTIONAL);
    private static final Switch<Boolean> PBF_SAVE_INTERMEDIATE_ATLAS = new Switch<>("savePbfAtlas",
            "Saves intermediate atlas files created when processing OSM protobuf data.",
            Boolean::valueOf, Optionality.OPTIONAL, "false");
    private static final Switch<Set<OutputFormats>> OUTPUT_FORMATS = new Switch<>("outputFormats",
            String.format("Comma-separated list of output formats (flags, metrics, geojson)."),
            csv_formats -> Stream.of(csv_formats.split(","))
                    .map(format -> Enum.valueOf(OutputFormats.class, format.toUpperCase()))
                    .collect(Collectors.toSet()),
            Optionality.OPTIONAL, "flags,metrics");

    // Indicator key for ignored countries
    private static final String IGNORED_KEY = "Ignored";

    // Outputs
    private static final String OUTPUT_FLAG_FOLDER = "flag";
    private static final String OUTPUT_GEOJSON_FOLDER = "geojson";
    private static final String OUTPUT_ATLAS_FOLDER = "atlas";
    private static final String INTERMEDIATE_ATLAS_EXTENSION = FileSuffix.ATLAS.toString()
            + FileSuffix.GZIP.toString();
    private static final String OUTPUT_METRIC_FOLDER = "metric";
    private static final String METRICS_FILENAME = "check-run-time.csv";

    private static final Logger logger = LoggerFactory.getLogger(IntegrityCheckSparkJob.class);
    private static final long serialVersionUID = 2990087219645942330L;

    // Thread pool settings
    private static final Duration POOL_DURATION_BEFORE_KILL = Duration.minutes(300);

    /**
     * Main entry point for the Spark job
     *
     * @param args
     *            arguments for the Spark job
     */
    public static void main(final String[] args)
    {
        new IntegrityCheckSparkJob().run(args);
    }

    /**
     * Executes all {@link BaseCheck}s on the given {@link Atlas}. Each check runs in a separate
     * thread. The checks go over all {@link AtlasEntity}s and {@link Relation}s.
     * {@link ComplexEntity}s can be processed by using the appropriate {@link Finder} and adding
     * them to the {@link Iterable} of objects.
     *
     * @param atlas
     *            the {@link Atlas} on which the checks will be run
     * @param checksToRun
     *            the set of {@link BaseCheck}s to execute
     * @param configuration
     *            {@link MapRouletteConfiguration} to create a new {@link MapRouletteClient}s
     */
    @SuppressWarnings("rawtypes")
    private static void executeChecks(final String country, final Atlas atlas,
            final Set<BaseCheck> checksToRun, final MapRouletteConfiguration configuration)
    {
        final Pool checkExecutionPool = new Pool(checksToRun.size(), "Check execution pool",
                POOL_DURATION_BEFORE_KILL);
        checksToRun.stream().filter(check -> check.validCheckForCountry(country))
                .forEach(check -> checkExecutionPool.queue(new RunnableCheck(country, check,
                        new MultiIterable<>(atlas.items(), atlas.relations()), MapRouletteClient
                                .instance(configuration))));
        checkExecutionPool.close();
    }

    private static SparkFilePath initializeOutput(final String output, final TaskContext context,
            final String country, final String temporaryOutputFolder,
            final String targetOutputFolder)
    {
        // Create temporary folder for flag output
        final String workerOutputFolder = SparkFileHelper.combine(temporaryOutputFolder,
                String.format("p%s_a%s", context.partitionId(), context.taskAttemptId()));
        final String temporaryFilePath = SparkFileHelper.combine(workerOutputFolder, output,
                country);
        final String targetFilePath = SparkFileHelper.combine(targetOutputFolder, output, country);

        return new SparkFilePath(temporaryFilePath, targetFilePath);
    }

    private static void writeAtlas(final Atlas atlas, final String country,
            final SparkFilePath output, final SparkFileHelper fileHelper)
    {
        final String fileName = String.format("%s_%s", country, atlas.getName());
        final SparkFileOutput file = SparkFileOutput.from(atlas::save, output, fileName,
                INTERMEDIATE_ATLAS_EXTENSION, "Intermediate Atlas");
        fileHelper.save(file);
    }

    @Override
    public String getName()
    {
        return "Integrity Check Spark Job";
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void start(final CommandMap commandMap)
    {
        final String atlasDirectory = (String) commandMap.get(ATLAS_FOLDER);
        final String input = Optional.ofNullable(input(commandMap)).orElse(atlasDirectory);
        final String output = output(commandMap);
        @SuppressWarnings("unchecked")
        final Set<OutputFormats> outputFormats = (Set<OutputFormats>) commandMap
                .get(OUTPUT_FORMATS);
        final StringList countries = StringList.split((String) commandMap.get(COUNTRIES),
                CommonConstants.COMMA);
        final MapRouletteConfiguration mapRouletteConfiguration = (MapRouletteConfiguration) commandMap
                .get(MAP_ROULETTE);
        final Configuration checksConfiguration = ConfigurationResolver
                .loadConfiguration(commandMap, CONFIGURATION_FILES, CONFIGURATION_JSON);
        final boolean saveIntermediateAtlas = (Boolean) commandMap.get(PBF_SAVE_INTERMEDIATE_ATLAS);
        @SuppressWarnings("unchecked")
        final Rectangle pbfBoundary = ((Optional<Rectangle>) commandMap.getOption(PBF_BOUNDING_BOX))
                .orElse(Rectangle.MAXIMUM);
        final boolean compressOutput = Boolean
                .valueOf((String) commandMap.get(SparkJob.COMPRESS_OUTPUT));

        final Map<String, String> sparkContext = configurationMap();
        final CheckResourceLoader checkLoader = new CheckResourceLoader(checksConfiguration);

        // check configuration and country list
        final Set<BaseCheck> preOverriddenChecks = checkLoader.loadChecks();
        if (!isValidInput(countries, preOverriddenChecks))
        {
            logger.error("No countries supplied or checks enabled, exiting!");
            return;
        }

        // Read priority countries from the configuration
        final List<String> priorityCountries = checksConfiguration
                .get("priority.countries", Collections.EMPTY_LIST).value();

        // Create a list of Country to Check tuples
        // Add priority countries first if they are supplied by parameter
        final List<Tuple2<String, Set<BaseCheck>>> countryCheckTuples = new ArrayList<>();
        countries.stream().filter(priorityCountries::contains).forEach(country -> countryCheckTuples
                .add(new Tuple2<>(country, checkLoader.loadChecksForCountry(country))));

        // Then add the rest of the countries
        countries.stream().filter(country -> !priorityCountries.contains(country))
                .forEach(country -> countryCheckTuples
                        .add(new Tuple2<>(country, checkLoader.loadChecksForCountry(country))));

        // Log countries and integrity
        logger.info("Initialized countries: {}", countryCheckTuples.stream().map(tuple -> tuple._1)
                .collect(Collectors.joining(",")));
        logger.info("Initialized checks: {}", preOverriddenChecks.stream()
                .map(BaseCheck::getCheckName).collect(Collectors.joining(",")));

        // Parallelize on the countries
        final JavaPairRDD<String, Set<BaseCheck>> countryCheckRDD = getContext()
                .parallelizePairs(countryCheckTuples, countryCheckTuples.size());

        // Set target and temporary folders
        final String targetOutputFolder = SparkFileHelper.parentPath(output);
        final String temporaryOutputFolder = SparkFileHelper.combine(targetOutputFolder,
                SparkFileHelper.TEMPORARY_FOLDER_NAME);

        // Useful file helper to create/delete/name files and directories
        final SparkFileHelper fileHelper = new SparkFileHelper(sparkContext);
        // Atlas Helper to load different types of Atlas data
        final AtlasDataSource atlasLoader = new AtlasDataSource(sparkContext, checksConfiguration,
                pbfBoundary);

        // Create target folders
        fileHelper.mkdir(SparkFileHelper.combine(targetOutputFolder, OUTPUT_FLAG_FOLDER));
        fileHelper.mkdir(SparkFileHelper.combine(targetOutputFolder, OUTPUT_GEOJSON_FOLDER));
        fileHelper.mkdir(SparkFileHelper.combine(targetOutputFolder, OUTPUT_METRIC_FOLDER));

        // Run the set of flags per country per check. The output will be an RDD pair mapping each
        // country with a set of SparkFilePaths to flags, geojson and metrics generated.
        final JavaPairRDD<String, Set<SparkFilePath>> resultRDD = countryCheckRDD.mapToPair(tuple ->
        {
            final Time timer = Time.now();

            final String country = tuple._1();
            final Set<BaseCheck> checks = tuple._2();

            logger.info("Initialized checks for {}: {}", country,
                    checks.stream().map(BaseCheck::getCheckName).collect(Collectors.joining(",")));

            final Set<SparkFilePath> resultingFiles = new HashSet<>();

            final SparkFilePath flagOutput;
            if (outputFormats.contains(OutputFormats.FLAGS))
            {
                // Initialize flag output processor
                flagOutput = initializeOutput(OUTPUT_FLAG_FOLDER, TaskContext.get(), country,
                        temporaryOutputFolder, targetOutputFolder);
                EventService.get(country).register(
                        new CheckFlagFileProcessor(fileHelper, flagOutput.getTemporaryPath())
                                .withCompression(compressOutput));
            }
            else
            {
                flagOutput = null;
            }
            final SparkFilePath geoJsonOutput;
            if (outputFormats.contains(OutputFormats.GEOJSON))
            {
                // Initialize geojson output processor
                geoJsonOutput = initializeOutput(OUTPUT_GEOJSON_FOLDER, TaskContext.get(), country,
                        temporaryOutputFolder, targetOutputFolder);
                EventService.get(country).register(
                        new CheckFlagGeoJsonProcessor(fileHelper, geoJsonOutput.getTemporaryPath())
                                .withCompression(compressOutput));
            }
            else
            {
                geoJsonOutput = null;
            }

            final SparkFilePath metricOutput;
            if (outputFormats.contains(OutputFormats.METRICS))
            {
                // Initialize metric output processor
                metricOutput = initializeOutput(OUTPUT_METRIC_FOLDER, TaskContext.get(), country,
                        temporaryOutputFolder, targetOutputFolder);
                EventService.get(country).register(new MetricFileGenerator(METRICS_FILENAME,
                        fileHelper, metricOutput.getTemporaryPath()));
            }
            else
            {
                metricOutput = null;
            }

            final Consumer<Atlas> intermediateAtlasHandler;
            if (saveIntermediateAtlas)
            {
                final SparkFilePath atlasOutput = initializeOutput(OUTPUT_ATLAS_FOLDER,
                        TaskContext.get(), country, temporaryOutputFolder, targetOutputFolder);
                intermediateAtlasHandler = atlas ->
                {
                    writeAtlas(atlas, country, atlasOutput, fileHelper);
                    resultingFiles.add(atlasOutput);
                };
            }
            else
            {
                intermediateAtlasHandler = atlas ->
                {
                    // no-op
                };
            }
            try
            {
                final Atlas atlas = atlasLoader.load(input, country, intermediateAtlasHandler);
                if (atlas == null)
                {
                    logger.error("Could not find {} Atlas files. Skipping country!", country);
                }
                else
                {
                    executeChecks(country, atlas, checks, mapRouletteConfiguration);
                    // Add output folders for handling later
                    Stream.of(flagOutput, metricOutput, geoJsonOutput).filter(Objects::nonNull)
                            .forEach(resultingFiles::add);
                }

                EventService.get(country).complete();
                return new Tuple2<>(country, resultingFiles);
            }
            catch (final CoreException e)
            {
                logger.error("Exception running integrity checks on {}", country, e);
            }

            logger.trace("Integrity checks took {} ms to execute for {}.",
                    timer.elapsedSince().asMilliseconds(), country);

            return new Tuple2<>(IGNORED_KEY, null);
        }).filter(tuple -> !tuple._1().equals(IGNORED_KEY));

        // Commit results
        resultRDD.foreach(countryPathPair ->
        {
            final String country = countryPathPair._1();
            final Set<SparkFilePath> paths = countryPathPair._2();
            logger.info("[{}] Committing outputs: {}", country, paths);

            paths.forEach(fileHelper::commit);
        });

        try
        {
            // Clean up
            logger.info("Deleting {}.", temporaryOutputFolder);
            fileHelper.deleteDirectory(temporaryOutputFolder);
        }
        catch (final Exception e)
        {
            logger.warn("Clean up failed!", e);
        }
    }

    /**
     * Defines all the folders to clean before a run
     *
     * @param command
     *            the command parameters sent to the main class
     * @return all the paths to clean
     */
    @Override
    protected List<String> outputToClean(final CommandMap command)
    {
        final String output = output(command);
        final List<String> staticPaths = super.outputToClean(command);
        staticPaths.add(getAlternateSubFolderOutput(output, OUTPUT_FLAG_FOLDER));
        staticPaths.add(getAlternateSubFolderOutput(output, OUTPUT_GEOJSON_FOLDER));
        staticPaths.add(getAlternateSubFolderOutput(output, OUTPUT_ATLAS_FOLDER));
        return staticPaths;
    }

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(ATLAS_FOLDER, MAP_ROULETTE, COUNTRIES, CONFIGURATION_FILES,
                CONFIGURATION_JSON, PBF_BOUNDING_BOX, PBF_SAVE_INTERMEDIATE_ATLAS, OUTPUT_FORMATS);
    }

    /**
     * Basic sanity check to ensure we aren't processing an empty list of countries or integrity
     * checks
     *
     * @param countries
     *            {@link StringList} of country ISO3 codes to process
     * @param checksToExecute
     *            set of {@link BaseCheck}s to execute
     * @return {@code true} if sanity check passes, {@code false} otherwise
     */
    private boolean isValidInput(final StringList countries, final Set<BaseCheck> checksToExecute)
    {
        if (countries.size() == 0 || checksToExecute.size() == 0)
        {
            return false;
        }
        return true;
    }

    /**
     * @author brian_l_davis
     */
    private enum OutputFormats
    {
        FLAGS,
        GEOJSON,
        METRICS
    }
}
