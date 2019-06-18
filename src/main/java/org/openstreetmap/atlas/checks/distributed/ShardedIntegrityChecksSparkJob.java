package org.openstreetmap.atlas.checks.distributed;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.base.CheckResourceLoader;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.constants.CommonConstants;
import org.openstreetmap.atlas.checks.maproulette.MapRouletteConfiguration;
import org.openstreetmap.atlas.checks.utility.ShardGroup;
import org.openstreetmap.atlas.checks.utility.ShardGrouper;
import org.openstreetmap.atlas.checks.utility.UniqueCheckFlagContainer;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.MergedConfiguration;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Serializable;
import scala.Tuple2;

public class ShardedIntegrityChecksSparkJob extends IntegrityChecksCommandArguments
{

    private static final Logger logger = LoggerFactory
            .getLogger(ShardedIntegrityChecksSparkJob.class);
    private static final Switch<Distance> EXPANSION_DISTANCE = new Switch<>("shardBufferDistance",
            "Distance to expand the bounds of the shard group to create a network in kilometers",
            distanceString -> Distance.kilometers(Double.valueOf(distanceString)),
            Optionality.OPTIONAL, "10.0");
    private static final Switch<Integer> SHARD_LOAD_MAX = new Switch<>("maxShardLoad",
            "The maximum amount of shards loaded into memory per executor", Integer::valueOf,
            Optionality.OPTIONAL, "60");

    @Override
    public String getName()
    {
        return "Sharded Integrity Checks Spark Job";
    }

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
        final boolean saveIntermediateAtlas = (Boolean) commandMap.get(PBF_SAVE_INTERMEDIATE_ATLAS);
        final AtlasFilePathResolver resolver = new AtlasFilePathResolver(checksConfiguration);
        final SparkFileHelper fileHelper = new SparkFileHelper(sparkContext);
        final CheckResourceLoader checkLoader = new CheckResourceLoader(checksConfiguration);

        // check inputs
        if (countries.isEmpty())
        {
            logger.error("No countries found to run");
            return;
        }
        final MultiMap<String, Check> countryChecks = new MultiMap<>();
        for (final String country : countries)
        {
            final Set<Check> checksLoadedForCountry = checkLoader.loadChecksForCountry(country);
            if (checksLoadedForCountry.isEmpty())
            {
                logger.warn("No checks loaded for country {}. Skipping execution", country);
            }
            else
            {
                checksLoadedForCountry.forEach(check -> countryChecks.add(country, check));
            }
        }
        if (countryChecks.isEmpty())
        {
            logger.error("No checks loaded for any of the countries provided. Skipping execution");
            return;
        }

        // find the shards for each country atlas files
        MultiMap<String, Shard> countryShards = countryShardMapFromShardFiles(
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
                    input, missingCountries.toString());
            return;
        }

        final Integer maxShardLoad = (Integer) commandMap.get(SHARD_LOAD_MAX);
        final Distance distanceToLoadShards = (Distance) commandMap.get(EXPANSION_DISTANCE);
        final List<ShardedCheckFlagsTask> tasks = new LinkedList<>();
        int unitsOfWork = 0;
        for (final Map.Entry<String, List<Shard>> countryShard : countryShards.entrySet())
        {
            final List<ShardGroup> groupsForCountry = new ShardGrouper(countryShard.getValue(),
                    maxShardLoad, distanceToLoadShards).getGroups();
            unitsOfWork += groupsForCountry.size();
            groupsForCountry.stream().map(group -> new ShardedCheckFlagsTask(countryShard.getKey(),
                    group, countryChecks.get(countryShard.getKey()))).forEach(tasks::add);
        }

        this.getContext().parallelize(tasks, unitsOfWork).mapToPair(produceFlags(input, fileHelper))
                .reduceByKey(UniqueCheckFlagContainer::combine)
                .foreach(processFlags(output, fileHelper, outputFormats, mapRouletteConfiguration));
    }

    private PairFunction<ShardedCheckFlagsTask, String, UniqueCheckFlagContainer> produceFlags(
            final String input, final SparkFileHelper fileHelper)
    {
        return task ->
        {

            return new Tuple2<>(task.getCountry(), new UniqueCheckFlagContainer());
        };
    }

    private VoidFunction<Tuple2<String, UniqueCheckFlagContainer>> processFlags(final String output,
            final SparkFileHelper fileHelper, final Set<OutputFormats> outputFormats, final MapRouletteConfiguration mapRouletteConfiguration)
    {
        return tuple ->
        {

            return;
        };
    }

    private Function<Shard, Optional<Atlas>> atlasFetcher(final String countryInput,
            final SparkFileHelper fileHelper)
    {
        return (Function<Shard, Optional<Atlas>> & Serializable) shard ->
        {
            return Optional.empty();
        };
    }

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(SHARD_LOAD_MAX, EXPANSION_DISTANCE);
    }
}
