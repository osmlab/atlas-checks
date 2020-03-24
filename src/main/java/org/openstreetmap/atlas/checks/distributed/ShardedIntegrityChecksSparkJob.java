package org.openstreetmap.atlas.checks.distributed;

import static org.openstreetmap.atlas.checks.distributed.IntegrityCheckSparkJob.METRICS_FILENAME;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.base.CheckResourceLoader;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.constants.CommonConstants;
import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.event.CheckFlagFileProcessor;
import org.openstreetmap.atlas.checks.event.CheckFlagGeoJsonProcessor;
import org.openstreetmap.atlas.checks.event.CheckFlagTippecanoeProcessor;
import org.openstreetmap.atlas.checks.event.MetricFileGenerator;
import org.openstreetmap.atlas.checks.utility.UniqueCheckFlagContainer;
import org.openstreetmap.atlas.event.EventService;
import org.openstreetmap.atlas.event.Processor;
import org.openstreetmap.atlas.event.ShutdownEvent;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.generator.sharding.AtlasSharding;
import org.openstreetmap.atlas.generator.tools.caching.HadoopAtlasFileCache;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.dynamic.DynamicAtlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.MergedConfiguration;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.filters.AtlasEntityPolygonsFilter;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import scala.Serializable;
import scala.Tuple2;

/**
 * A spark job for generating integrity checks in a sharded fashion. This allows for a lower local
 * memory profile as well as better parallelization.<br>
 * This implementation currently only supports Atlas files as an input data source. They must be in
 * the structure: folder/iso_code/iso_z-x-y.atlas<br>
 * Also required is a reference for how the Atlases are sharded. This can be provided as a
 * sharding.txt file in the input path, or through the {@code sharding} argument.
 *
 * @author jklamer
 * @author bbreithaupt
 */
public class ShardedIntegrityChecksSparkJob extends IntegrityChecksCommandArguments
{
    private static final Switch<Distance> EXPANSION_DISTANCE = new Switch<>("shardBufferDistance",
            "Distance to expand the bounds of the shard group to create a network in kilometers",
            distanceString -> Distance.kilometers(Double.valueOf(distanceString)),
            Optionality.OPTIONAL, "10.0");
    private static final String ATLAS_SHARDING_FILE = "sharding.txt";
    private static final Switch<String> SHARDING = new Switch<>("sharding",
            "Sharding to load in place of sharding file in Atlas path", StringConverter.IDENTITY,
            Optionality.OPTIONAL);
    private static final Switch<Boolean> MULTI_ATLAS = new Switch<>("multiAtlas",
            "If true then use a multi atlas, else use a dynamic atlas. This works better for running on a single machine",
            Boolean::getBoolean, Optionality.OPTIONAL, "false");

    private static final Logger logger = LoggerFactory
            .getLogger(ShardedIntegrityChecksSparkJob.class);
    private static final long serialVersionUID = -8038802870994470017L;

    private final MultiMap<String, Check> countryChecks = new MultiMap<>();

    public static void main(final String[] args)
    {
        new ShardedIntegrityChecksSparkJob().run(args);
    }

    @Override
    public String getName()
    {
        return "Sharded Integrity Checks Spark Job";
    }

    @Override
    public void start(final CommandMap commandMap)
    {
        final Time start = Time.now();
        final String atlasDirectory = (String) commandMap.get(ATLAS_FOLDER);
        final String input = Optional.ofNullable(input(commandMap)).orElse(atlasDirectory);

        // Gather arguments
        final String output = output(commandMap);
        @SuppressWarnings("unchecked")
        final Set<OutputFormats> outputFormats = (Set<OutputFormats>) commandMap
                .get(OUTPUT_FORMATS);
        final StringList countries = StringList.split((String) commandMap.get(COUNTRIES),
                CommonConstants.COMMA);
        @SuppressWarnings("unchecked")
        final Optional<List<String>> checkFilter = (Optional<List<String>>) commandMap
                .getOption(CHECK_FILTER);

        final Configuration checksConfiguration = new MergedConfiguration(Stream
                .concat(Stream.of(ConfigurationResolver.loadConfiguration(commandMap,
                        CONFIGURATION_FILES, CONFIGURATION_JSON)),
                        Stream.of(checkFilter
                                .<Configuration> map(permitlist -> new StandardConfiguration(
                                        "PermitListConfiguration",
                                        Collections.singletonMap(
                                                "CheckResourceLoader.checks.permitlist",
                                                permitlist)))
                                .orElse(ConfigurationResolver.emptyConfiguration())))
                .collect(Collectors.toList()));

        final Map<String, String> sparkContext = configurationMap();

        // File loading helpers
        final AtlasFilePathResolver resolver = new AtlasFilePathResolver(checksConfiguration);
        final SparkFileHelper fileHelper = new SparkFileHelper(sparkContext);
        final CheckResourceLoader checkLoader = new CheckResourceLoader(checksConfiguration);

        // Get sharding
        @SuppressWarnings("unchecked")
        final Optional<String> alternateShardingFile = (Optional<String>) commandMap
                .getOption(SHARDING);
        final String shardingPathInAtlas = "dynamic@"
                + SparkFileHelper.combine(input, ATLAS_SHARDING_FILE);
        final String shardingFilePath = alternateShardingFile.orElse(shardingPathInAtlas);
        final Sharding sharding = AtlasSharding.forString(shardingFilePath,
                this.configurationMap());
        final Broadcast<Sharding> shardingBroadcast = this.getContext().broadcast(sharding);
        final Distance distanceToLoadShards = (Distance) commandMap.get(EXPANSION_DISTANCE);

        // Check inputs
        if (countries.isEmpty())
        {
            throw new CoreException("No countries found to run.");
        }
        for (final String country : countries)
        {
            final Set<Check> checksLoadedForCountry = checkLoader.loadChecksForCountry(country);
            if (checksLoadedForCountry.isEmpty())
            {
                logger.warn("No checks loaded for country {}. Skipping execution", country);
            }
            else
            {
                checksLoadedForCountry.forEach(check -> this.countryChecks.add(country, check));
            }
        }
        if (this.countryChecks.isEmpty())
        {
            throw new CoreException("No checks loaded for any of the countries provided.");
        }

        // Find the shards for each country atlas files
        final MultiMap<String, Shard> countryShards = countryShardMapFromShardFiles(
                countries.stream().collect(Collectors.toSet()), resolver, input, sparkContext);
        if (countryShards.isEmpty())
        {
            throw new CoreException("No atlas files found in input.");
        }

        if (!countries.stream().allMatch(countryShards::containsKey))
        {
            final Set<String> missingCountries = countries.stream()
                    .filter(aCountry -> !countryShards.containsKey(aCountry))
                    .collect(Collectors.toSet());
            throw new CoreException(
                    "Unable to find standardized named shard files in the path {}/<countryName> for the countries {}. \n Files must be in format <country>_<zoom>_<x>_<y>.atlas",
                    input, missingCountries);
        }

        // Countrify spark parallelization for better debugging
        try (Pool checkPool = new Pool(countryShards.size(), "Countries Execution Pool"))
        {
            for (final Map.Entry<String, List<Shard>> countryShard : countryShards.entrySet())
            {
                checkPool.queue(() ->
                {
                    // Generate a task for each shard
                    final List<ShardedCheckFlagsTask> tasksForCountry = countryShard.getValue()
                            .stream()
                            .map(shard -> new ShardedCheckFlagsTask(countryShard.getKey(), shard,
                                    this.countryChecks.get(countryShard.getKey())))
                            .collect(Collectors.toList());

                    // Set spark UI job title
                    this.getContext().setLocalProperty("callSite.short", String
                            .format("Running checks on %s", tasksForCountry.get(0).getCountry()));

                    this.getContext().parallelize(tasksForCountry, tasksForCountry.size())
                            .mapToPair(produceFlags(input, output, this.configurationMap(),
                                    fileHelper, shardingBroadcast, distanceToLoadShards,
                                    (Boolean) commandMap.get(MULTI_ATLAS)))
                            .reduceByKey(UniqueCheckFlagContainer::combine)
                            // Generate outputs
                            .foreach(processFlags(output, fileHelper, outputFormats));
                });
            }
        }

        logger.info("Sharded checks completed in {}", start.elapsedSince());
    }

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(EXPANSION_DISTANCE, MULTI_ATLAS, SHARDING);
    }

    /**
     * Get the fetcher to use for Atlas files. The fetcher uses a hadoop cache to reduce remote
     * reads.
     *
     * @param input
     *            {@link String} input folder path
     * @param country
     *            {@link String} country code
     * @param configuration
     *            {@link org.openstreetmap.atlas.generator.tools.spark.SparkJob} configuration map
     * @return {@link Function} that fetches atlases/
     */
    private Function<Shard, Optional<Atlas>> atlasFetcher(final String input, final String country,
            final Map<String, String> configuration)
    {
        final HadoopAtlasFileCache cache = new HadoopAtlasFileCache(input, configuration);
        final AtlasResourceLoader loader = new AtlasResourceLoader();
        return (Function<Shard, Optional<Atlas>> & Serializable) shard -> cache.get(country, shard)
                .map(loader::load);
    }

    /**
     * Process {@link org.openstreetmap.atlas.checks.flag.CheckFlag}s through an event service to
     * produce output files.
     *
     * @param output
     *            {@link String} output folder path
     * @param fileHelper
     *            {@link SparkFileHelper}
     * @param outputFormats
     *            {@link Set} of
     *            {@link org.openstreetmap.atlas.checks.distributed.IntegrityChecksCommandArguments.OutputFormats}
     * @return {@link VoidFunction} that takes a {@link Tuple2} of a {@link String} country code and
     *         a {@link UniqueCheckFlagContainer}
     */
    @SuppressWarnings("unchecked")
    private VoidFunction<Tuple2<String, UniqueCheckFlagContainer>> processFlags(final String output,
            final SparkFileHelper fileHelper, final Set<OutputFormats> outputFormats)
    {
        return tuple ->
        {
            final String country = tuple._1();
            final UniqueCheckFlagContainer flagContainer = tuple._2();
            final EventService<CheckFlagEvent> eventService = EventService.get(country);

            if (outputFormats.contains(OutputFormats.FLAGS))
            {
                eventService.register(new CheckFlagFileProcessor(fileHelper,
                        SparkFileHelper.combine(output, OUTPUT_FLAG_FOLDER, country)));
            }

            if (outputFormats.contains(OutputFormats.GEOJSON))
            {

                eventService.register(new CheckFlagGeoJsonProcessor(fileHelper,
                        SparkFileHelper.combine(output, OUTPUT_GEOJSON_FOLDER, country)));
            }

            if (outputFormats.contains(OutputFormats.TIPPECANOE))
            {
                eventService.register(new CheckFlagTippecanoeProcessor(fileHelper,
                        SparkFileHelper.combine(output, OUTPUT_TIPPECANOE_FOLDER, country)));
            }

            flagContainer.reconstructEvents().parallel().forEach(eventService::post);
            eventService.complete();
        };
    }

    /**
     * {@link PairFunction} to run each {@link ShardedCheckFlagsTask} through to produce
     * {@link org.openstreetmap.atlas.checks.flag.CheckFlag}s.
     *
     * @param input
     *            {@link String} input folder path
     * @param output
     *            {@link String} output folder path
     * @param configurationMap
     *            {@link org.openstreetmap.atlas.generator.tools.spark.SparkJob} configuration map
     * @param fileHelper
     *            {@link SparkFileHelper}
     * @param sharding
     *            spark {@link Broadcast} of the current {@link Sharding}
     * @param shardDistanceExpansion
     *            {@link Distance} to expand the shard group
     * @param multiAtlas
     *            boolean whether to use a multi or dynamic Atlas
     * @return {@link PairFunction} that takes {@link ShardedCheckFlagsTask} and returns a
     *         {@link Tuple2} of a {@link String} country code and {@link UniqueCheckFlagContainer}
     */
    @SuppressWarnings("unchecked")
    private PairFunction<ShardedCheckFlagsTask, String, UniqueCheckFlagContainer> produceFlags(
            final String input, final String output, final Map<String, String> configurationMap,
            final SparkFileHelper fileHelper, final Broadcast<Sharding> sharding,
            final Distance shardDistanceExpansion, final boolean multiAtlas)
    {
        return task ->
        {
            // Get the atlas
            final Function<Shard, Optional<Atlas>> fetcher = this.atlasFetcher(input,
                    task.getCountry(), configurationMap);
            final Atlas atlas;

            // Use dynamic or multi atlas (multi runs faster locally)
            if (multiAtlas)
            {
                atlas = new MultiAtlas(
                        StreamSupport
                                .stream(sharding.getValue()
                                        .shards(task.getShard().bounds()
                                                .expand(shardDistanceExpansion))
                                        .spliterator(), true)
                                .map(fetcher).filter(Optional::isPresent).map(Optional::get)
                                .collect(Collectors.toList()));
            }
            else
            {
                final DynamicAtlasPolicy policy = new DynamicAtlasPolicy(fetcher,
                        sharding.getValue(), Collections.singleton(task.getShard()),
                        task.getShard().bounds().expand(shardDistanceExpansion))
                                .withDeferredLoading(true).withAggressivelyExploreRelations(true)
                                .withExtendIndefinitely(false);
                atlas = new DynamicAtlas(policy);
                ((DynamicAtlas) atlas).preemptiveLoad();
            }

            final AtlasEntityPolygonsFilter boundaryFilter = AtlasEntityPolygonsFilter.Type.INCLUDE
                    .polygons(Collections.singleton(task.getShard().bounds()));

            // Prepare the event service
            final EventService eventService = task.getEventService();
            final UniqueCheckFlagContainer container = new UniqueCheckFlagContainer();
            eventService.register(new Processor<CheckFlagEvent>()
            {
                @Override
                public void process(final ShutdownEvent event)
                {
                    // no-op
                }

                @Override
                @Subscribe
                @AllowConcurrentEvents
                public void process(final CheckFlagEvent event)
                {
                    container.add(event.getCheckName(), event.getCheckFlag().makeComplete());
                }
            });
            // Metrics are output on a per shard level
            final MetricFileGenerator metricFileGenerator = new MetricFileGenerator(
                    task.getShard().getName() + "_" + METRICS_FILENAME, fileHelper,
                    SparkFileHelper.combine(output, OUTPUT_METRIC_FOLDER, task.getCountry()));
            eventService.register(metricFileGenerator);

            // Run all checks in parallel
            try (Pool checkPool = new Pool(task.getChecks().size(),
                    "Sharded Checks Execution Pool"))
            {
                for (final Check check : task.getChecks())
                {
                    checkPool.queue(new RunnableCheck(task.getCountry(), check,
                            objectsToCheck(atlas, check, boundaryFilter), eventService));
                }
            }

            eventService.complete();
            return new Tuple2<>(task.getCountry(), container);
        };
    }
}
