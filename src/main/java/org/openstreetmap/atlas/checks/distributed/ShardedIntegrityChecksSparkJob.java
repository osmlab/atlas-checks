package org.openstreetmap.atlas.checks.distributed;

import static org.openstreetmap.atlas.checks.distributed.IntegrityCheckSparkJob.METRICS_FILENAME;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.spark.api.java.AbstractJavaRDDLike;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.storage.StorageLevel;
import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.base.CheckResourceLoader;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.constants.CommonConstants;
import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.event.CheckFlagFileProcessor;
import org.openstreetmap.atlas.checks.event.CheckFlagGeoJsonProcessor;
import org.openstreetmap.atlas.checks.event.CheckFlagTippecanoeProcessor;
import org.openstreetmap.atlas.checks.event.MapRouletteClientProcessor;
import org.openstreetmap.atlas.checks.event.MetricFileGenerator;
import org.openstreetmap.atlas.checks.maproulette.MapRouletteConfiguration;
import org.openstreetmap.atlas.checks.utility.ShardGroup;
import org.openstreetmap.atlas.checks.utility.ShardGrouper;
import org.openstreetmap.atlas.checks.utility.UniqueCheckFlagContainer;
import org.openstreetmap.atlas.event.EventService;
import org.openstreetmap.atlas.event.Processor;
import org.openstreetmap.atlas.event.ShutdownEvent;
import org.openstreetmap.atlas.generator.sharding.AtlasSharding;
import org.openstreetmap.atlas.generator.tools.caching.HadoopAtlasFileCache;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.geography.Rectangle;
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
 * memory profile as well as better parallelization
 *
 * @author jklamer
 */
public class ShardedIntegrityChecksSparkJob extends IntegrityChecksCommandArguments
{

    private static final Switch<Distance> EXPANSION_DISTANCE = new Switch<>("shardBufferDistance",
            "Distance to expand the bounds of the shard group to create a network in kilometers",
            distanceString -> Distance.kilometers(Double.valueOf(distanceString)),
            Optionality.OPTIONAL, "10.0");
    private static final String SHARDING_FILE = "sharding.txt";
    private static final Switch<Integer> SHARD_LOAD_MAX = new Switch<>("maxShardLoad",
            "The maximum amount of shards loaded into memory per executor", Integer::valueOf,
            Optionality.OPTIONAL, "45");
    private static final Switch<Boolean> DYNAMIC_ATLAS = new Switch<>("dynamicAtlas",
            "if true then use a dynamic atlas, else use a multi atlas", Boolean::new,
            Optionality.OPTIONAL, "false");
    private static final Switch<String> SPARK_STORAGE_DISK_ONLY = new Switch<>(
            "sparkStorageDiskOnly", "Store cached RDDs exclusively on disk",
            StringConverter.IDENTITY, Optionality.OPTIONAL);
    private static final Logger logger = LoggerFactory
            .getLogger(ShardedIntegrityChecksSparkJob.class);
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
        final String shardingPathInAtlas = "dynamic@"
                + SparkFileHelper.combine(input, SHARDING_FILE);

        final String output = output(commandMap);
        @SuppressWarnings("unchecked")
        final Set<OutputFormats> outputFormats = (Set<OutputFormats>) commandMap
                .get(OUTPUT_FORMATS);
        final StringList countries = StringList.split((String) commandMap.get(COUNTRIES),
                CommonConstants.COMMA);
        final MapRouletteConfiguration mapRouletteConfiguration = (MapRouletteConfiguration) commandMap
                .get(MAP_ROULETTE);
        @SuppressWarnings("unchecked")
        final Optional<List<String>> checkFilter = (Optional<List<String>>) commandMap
                .getOption(CHECK_FILTER);

        final Configuration checksConfiguration = new MergedConfiguration(Stream
                .concat(Stream.of(ConfigurationResolver.loadConfiguration(commandMap,
                        CONFIGURATION_FILES, CONFIGURATION_JSON)),
                        Stream.of(checkFilter
                                .<Configuration> map(whitelist -> new StandardConfiguration(
                                        "WhiteListConfiguration",
                                        Collections.singletonMap(
                                                "CheckResourceLoader.checks.whitelist", whitelist)))
                                .orElse(ConfigurationResolver.emptyConfiguration())))
                .collect(Collectors.toList()));

        final Map<String, String> sparkContext = configurationMap();
        final AtlasFilePathResolver resolver = new AtlasFilePathResolver(checksConfiguration);
        final SparkFileHelper fileHelper = new SparkFileHelper(sparkContext);
        final CheckResourceLoader checkLoader = new CheckResourceLoader(checksConfiguration);
        final Sharding sharding = AtlasSharding.forString(shardingPathInAtlas,
                this.configurationMap());
        final Broadcast<Sharding> shardingBroadcast = this.getContext().broadcast(sharding);

        // check inputs
        if (countries.isEmpty())
        {
            logger.error("No countries found to run");
            return;
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
            logger.error("No checks loaded for any of the countries provided. Skipping execution");
            return;
        }

        // find the shards for each country atlas files
        final MultiMap<String, Shard> countryShards = countryShardMapFromShardFiles(
                countries.stream().collect(Collectors.toSet()), resolver, input, sparkContext);
        if (countryShards.isEmpty())
        {
            logger.info("No atlas files found in input ");
        }

        if (!countries.stream().allMatch(countryShards::containsKey))
        {
            final Set<String> missingCountries = countries.stream()
                    .filter(aCountry -> !countryShards.containsKey(aCountry))
                    .collect(Collectors.toSet());
            logger.error(
                    "Unable to find standardized named shard files in the path {}/<countryName> for the countries {}. \n Files must be in format <country>_<zoom>_<x>_<y>.atlas or <zoom>_<x>_<y>.pbf",
                    input, missingCountries);
            return;
        }

        final Integer maxShardLoad = (Integer) commandMap.get(SHARD_LOAD_MAX);
        final Distance distanceToLoadShards = (Distance) commandMap.get(EXPANSION_DISTANCE);

        final List<JavaPairRDD<String, UniqueCheckFlagContainer>> countryRdds = countryShards
                .entrySet().stream()
                .map(countryShard -> new ShardGrouper(countryShard.getValue(), maxShardLoad,
                        distanceToLoadShards)
                                .getGroups().stream()
                                .map(group -> new ShardedCheckFlagsTask(countryShard.getKey(),
                                        group, this.countryChecks.get(countryShard.getKey())))
                                .collect(Collectors.toList()))
                .map(tasksForCountry ->
                {
                    this.getContext().setJobGroup("0", String.format("Running checks on %s",
                            tasksForCountry.get(0).getCountry()));
                    return this.getContext().parallelize(tasksForCountry, tasksForCountry.size())
                            .mapToPair(produceFlags(input, output, this.configurationMap(),
                                    fileHelper, shardingBroadcast, distanceToLoadShards,
                                    (Boolean) commandMap.get(DYNAMIC_ATLAS)));
                }).collect(Collectors.toList());

        final StorageLevel storageLevel = Optional
                .ofNullable(commandMap.get(SPARK_STORAGE_DISK_ONLY)).orElse("false").equals("true")
                        ? StorageLevel.DISK_ONLY()
                        : StorageLevel.MEMORY_AND_DISK();
        countryRdds.forEach(rdd -> rdd.persist(storageLevel));

        countryRdds.forEach(AbstractJavaRDDLike::count);

        final JavaPairRDD<String, UniqueCheckFlagContainer> firstCountryRdd = countryRdds.get(0);
        countryRdds.remove(0);

        this.getContext().union(firstCountryRdd, countryRdds)
                .reduceByKey(UniqueCheckFlagContainer::combine)
                .foreach(processFlags(output, fileHelper, outputFormats, mapRouletteConfiguration));

        logger.info("Sharded checks completed in {}", start.elapsedSince());
    }

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(SHARD_LOAD_MAX, EXPANSION_DISTANCE, DYNAMIC_ATLAS,
                SPARK_STORAGE_DISK_ONLY);
    }

    private Function<Shard, Optional<Atlas>> atlasFetcher(final String input, final String country,
            final Map<String, String> configuration)
    {
        final HadoopAtlasFileCache cache = new HadoopAtlasFileCache(input, configuration);
        final AtlasResourceLoader loader = new AtlasResourceLoader();
        return (Function<Shard, Optional<Atlas>> & Serializable) shard ->
        {
            return cache.get(country, shard).map(loader::load);
        };
    }

    @SuppressWarnings("unchecked")
    private VoidFunction<Tuple2<String, UniqueCheckFlagContainer>> processFlags(final String output,
            final SparkFileHelper fileHelper, final Set<OutputFormats> outputFormats,
            final MapRouletteConfiguration mapRouletteConfiguration)
    {
        return tuple ->
        {
            final String country = tuple._1();
            final UniqueCheckFlagContainer flagContainer = tuple._2();
            final EventService eventService = EventService.get(country);

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

            if (outputFormats.contains(OutputFormats.METRICS))
            {
                eventService.register(new MetricFileGenerator(METRICS_FILENAME, fileHelper,
                        SparkFileHelper.combine(output, OUTPUT_METRIC_FOLDER, country)));
            }

            if (outputFormats.contains(OutputFormats.TIPPECANOE))
            {
                eventService.register(new CheckFlagTippecanoeProcessor(fileHelper,
                        SparkFileHelper.combine(output, OUTPUT_TIPPECANOE_FOLDER, country)));
            }

            if (Objects.nonNull(mapRouletteConfiguration))
            {
                eventService.register(new MapRouletteClientProcessor(mapRouletteConfiguration,
                        this.countryChecks.get(country)));
            }

            flagContainer.reconstructEvents().parallel().forEach(eventService::post);
            eventService.complete();
            return;
        };
    }

    @SuppressWarnings("unchecked")
    private PairFunction<ShardedCheckFlagsTask, String, UniqueCheckFlagContainer> produceFlags(
            final String input, final String output, final Map<String, String> configurationMap,
            final SparkFileHelper fileHelper, final Broadcast<Sharding> sharding,
            final Distance shardDistanceExpansion, final boolean dynamic)
    {
        return task ->
        {
            final Function<Shard, Optional<Atlas>> fetcher = this.atlasFetcher(input,
                    task.getCountry(), configurationMap);
            final Atlas atlas;

            if (dynamic)
            {
                logger.info("Running with a dynamic atlas");
                final DynamicAtlasPolicy policy = new DynamicAtlasPolicy(fetcher,
                        sharding.getValue(), new HashSet<>(task.getShardGroup()),
                        Rectangle.forLocated(task.getShardGroup()).bounds()
                                .expand(shardDistanceExpansion)).withDeferredLoading(true)
                                        .withAggressivelyExploreRelations(true)
                                        .withExtendIndefinitely(false);
                atlas = new DynamicAtlas(policy);
            }
            else
            {
                logger.info("Running with a multi atlas");
                final ShardGroup shardGroup = task.getShardGroup();
                final Rectangle expansionBounds = Rectangle.forLocated(shardGroup)
                        .expand(shardDistanceExpansion);
                atlas = new MultiAtlas(StreamSupport
                        .stream(sharding.getValue().shards(expansionBounds).spliterator(), true)
                        .map(fetcher).filter(Optional::isPresent).map(Optional::get)
                        .collect(Collectors.toList()));
            }

            final AtlasEntityPolygonsFilter boundaryFilter = AtlasEntityPolygonsFilter.Type.INCLUDE
                    .geometricSurfaces(task.getShardGroup().stream().map(Shard::bounds)
                            .collect(Collectors.toList()));
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

            final MetricFileGenerator metricFileGenerator = new MetricFileGenerator(
                    task.getShardGroup().getName() + "_" + METRICS_FILENAME, fileHelper,
                    SparkFileHelper.combine(output, OUTPUT_METRIC_FOLDER, task.getCountry()));
            eventService.register(metricFileGenerator);

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
