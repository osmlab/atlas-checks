package org.openstreetmap.atlas.checks.maproulette;

import static org.openstreetmap.atlas.checks.utility.FileUtility.LogOutputFileType;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.Task;
import org.openstreetmap.atlas.checks.maproulette.serializer.ChallengeDeserializer;
import org.openstreetmap.atlas.checks.maproulette.serializer.TaskDeserializer;
import org.openstreetmap.atlas.checks.utility.FileUtility;
import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Given a directory of log files created by atlas-checks, upload those files to MapRoulette.
 *
 * @author nachtm
 * @author bbreithaupt
 */
public class MapRouletteUploadCommand extends MapRouletteCommand
{

    private static final Switch<File> INPUT_DIRECTORY = new Switch<>("logfiles",
            "Path to folder containing log files to upload to MapRoulette.", File::new,
            Optionality.REQUIRED);
    private static final Switch<File> CONFIG_LOCATION = new Switch<>("config",
            "Path to a file containing MapRoulette challenge configuration.", File::new,
            Optionality.OPTIONAL, "config/configuration.json");
    private static final Switch<List<String>> COUNTRIES = new Switch<>("countries",
            "A comma separated list of ISO3 country codes to filter flags by.",
            string -> Arrays.asList(string.split(",")), Optionality.OPTIONAL);
    private static final Switch<List<String>> CHECKS = new Switch<>("checks",
            "A comma separated list of check names to filter flags by.",
            string -> Arrays.asList(string.split(",")), Optionality.OPTIONAL);
    private static final String PARAMETER_CHALLENGE = "challenge";

    private static final Logger logger = LoggerFactory.getLogger(MapRouletteUploadCommand.class);
    private final Map<String, Challenge> checkNameChallengeMap;

    public static void main(final String[] args)
    {
        new MapRouletteUploadCommand().run(args);
    }

    public MapRouletteUploadCommand()
    {
        super();
        this.checkNameChallengeMap = new HashMap<>();
    }

    @Override
    public SwitchList switches()
    {
        return super.switches().with(INPUT_DIRECTORY, CHALLENGE_ID_FILE, CONFIG_LOCATION, COUNTRIES,
                CHECKS);
    }

    @Override
    protected void execute(final CommandMap commandMap,
            final MapRouletteConfiguration configuration)
    {
        final Gson gson = new GsonBuilder().registerTypeAdapter(Task.class,
                new TaskDeserializer(configuration.getProjectName())).create();
        final Configuration instructions = this.loadConfiguration(commandMap);
        // Get the countries filter
        final Optional<List<String>> countries = (Optional<List<String>>) commandMap
                .getOption(COUNTRIES);
        // Get the checks filter
        final Optional<List<String>> checks = (Optional<List<String>>) commandMap.getOption(CHECKS);

        ((File) commandMap.get(INPUT_DIRECTORY)).listFilesRecursively().forEach(logFile ->
        {
            // If this file is something we handle, read and upload the tasks contained within
            final Optional<LogOutputFileType> optionalHandledFileType = FileUtility
                    .getOptionalLogOutputType(logFile);
            optionalHandledFileType.ifPresent(logOutputFileType ->
            {
                try (BufferedReader reader = FileUtility.getReader(logFile, logOutputFileType))
                {
                    reader.lines().forEach(line ->
                    {
                        // Get Task from geojson
                        final Task task = gson.fromJson(line, Task.class);
                        // Get the first country code from the Task
                        final Optional<String> countryCode = Iterables
                                .stream(task.getGeoJson().orElse(new JsonArray()))
                                .map(this::getElementCountryCode).firstMatching(Optional::isPresent)
                                .get();
                        // Get the challenge name from the Task
                        final String check = task.getChallengeName();
                        // If the country filter and country code exist, check that the country code
                        // is in the filter.
                        if ((!countries.isPresent() || (countryCode.isPresent()
                                && countries.get().contains(countryCode.get())))
                                // If the checks filter exists, check that the challenge name is in
                                // the checks filter.
                                && (!checks.isPresent() || checks.get().contains(check)))
                        {
                            try
                            {
                                final Challenge challenge = this
                                        .getChallenge(task.getChallengeName(), instructions);
                                // Prepend the challenge name with the full country name if one
                                // exists, else country code if it exists. Then try to add the task
                                // for upload
                                countryCode.ifPresent(iso -> challenge.setName(String.join(" - ",
                                        Arrays.stream(countryCode.get().split(","))
                                                .map(country -> IsoCountry.displayCountry(country)
                                                        .orElse(country))
                                                .collect(Collectors.toList()).toString()
                                                .replace("[", "").replace("]", ""),
                                        task.getChallengeName())));
                                this.addTask(challenge, task);
                            }
                            catch (URISyntaxException | UnsupportedEncodingException error)
                            {
                                logger.error("Exception thrown while adding task: ", error);
                            }
                        }
                    });
                    this.uploadTasks();
                }
                catch (final IOException error)
                {
                    logger.error("Exception while reading {}:", logFile, error);
                }
            });
        });
    }

    /**
     * If we've already looked up checkName, return the cached result from our store. Otherwise,
     * read the challenge parameters from fallbackConfiguration and deserialize them into a
     * Challenge object.
     *
     * @param checkName
     *            the name of the check
     * @param fallbackConfiguration
     *            the full configuration, which contains challenge parameters for checkName.
     * @return the check's challenge parameters, stored as a Challenge object.
     */
    private Challenge getChallenge(final String checkName,
            final Configuration fallbackConfiguration)
    {
        return this.checkNameChallengeMap.computeIfAbsent(checkName, name ->
        {
            final Map<String, String> challengeMap = fallbackConfiguration
                    .get(getChallengeParameter(checkName), Collections.emptyMap()).value();
            final Gson gson = new GsonBuilder().disableHtmlEscaping()
                    .registerTypeAdapter(Challenge.class, new ChallengeDeserializer()).create();
            final Challenge result = gson.fromJson(gson.toJson(challengeMap), Challenge.class);
            result.setName(result.getName().isEmpty() ? checkName : result.getName());
            return result;
        });
    }

    /**
     * Returns a string which can be used as a key in a configuration to get checkName's challenge
     * configuration
     *
     * @param checkName
     *            the name of the a check in a configuration file
     * @return checkName.challenge
     */
    private String getChallengeParameter(final String checkName)
    {
        return MessageFormat.format("{0}.{1}", checkName, PARAMETER_CHALLENGE);
    }

    /**
     * Gathers the ISO3 country code from a {@link JsonElement}, if it has one.
     *
     * @param element
     *            a {@link JsonElement}
     * @return an {@link Optional} {@link String} containing the ISO3 country code
     */
    private Optional<String> getElementCountryCode(final JsonElement element)
    {
        final JsonObject elementJson = element.getAsJsonObject();
        // If the element has properties
        if (elementJson.has(PROPERTIES))
        {
            final JsonObject properties = elementJson.get(PROPERTIES).getAsJsonObject();
            // And the properties have a country code
            if (properties.has(ISOCountryTag.KEY))
            {
                // Get the country code
                return Optional.of(properties.get(ISOCountryTag.KEY).getAsString());
            }
        }
        return Optional.empty();
    }

    /**
     * Given a command map, load the configuration defined by the CONFIG_LOCATION switch and return
     * it
     *
     * @param map
     *            the input map of arguments passed to this command
     * @return the configuration at the specified file location
     */
    private Configuration loadConfiguration(final CommandMap map)
    {
        return new StandardConfiguration((File) map.get(CONFIG_LOCATION));
    }
}
