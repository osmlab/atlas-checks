package org.openstreetmap.atlas.checks.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.checks.maproulette.MapRouletteCommand;
import org.openstreetmap.atlas.checks.maproulette.MapRouletteConfiguration;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.Task;
import org.openstreetmap.atlas.checks.maproulette.serializer.ChallengeDeserializer;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Given a directory of log files created by atlas-checks, upload those files to MapRoulette.
 *
 * @author nachtm
 */
public class MapRouletteUploadCommand extends MapRouletteCommand
{
    /**
     * Deserializes a line from a line-delimited geojson log file into into a Task object, given
     * a particular project name.
     */
    private class TaskDeserializer implements JsonDeserializer<Task>
    {
        private static final String PROPERTIES = "properties";
        private static final String GENERATOR = "generator";
        private static final String INSTRUCTIONS = "instructions";
        private static final String ID = "id";
        private static final String FEATURES = "features";
        private String projectName;

        TaskDeserializer(final String projectName)
        {
            this.projectName = projectName;
        }

        @Override public Task deserialize(final JsonElement json, final Type typeOfT,
                final JsonDeserializationContext context) throws JsonParseException
        {
            final JsonObject full = json.getAsJsonObject();
            final JsonObject properties = full.get(PROPERTIES).getAsJsonObject();
            final String challengeName = properties.get(GENERATOR).getAsString();
            final String instruction = properties.get(INSTRUCTIONS).getAsString();
            final String taskID = properties.get(ID).getAsString();
            final JsonArray geojson = full.get(FEATURES).getAsJsonArray();
            final Task result = new Task();
            result.setChallengeName(challengeName);
            // Note: since the points are contained inside the geojson feature, and we're
            // re-serializing on our way to MapRoulette, we don't need to explicitly parse out the
            // points from the geojson. If you're considering pulling this class out for more
            // general use, points should be parsed as well.
            result.setGeoJson(Optional.of(geojson));
            result.setInstruction(instruction);
            result.setProjectName(projectName);
            result.setTaskIdentifier(taskID);
            return result;
        }
    }

    private static final Switch<File> INPUT_DIRECTORY = new Command.Switch<>("logfiles",
            "Path to folder containing log files to upload to MapRoulette.", File::new, Optionality.REQUIRED);
    private static final Switch<File> CONFIG_LOCATION = new Command.Switch<>("config",
            "Path to a file containing MapRoulette challenge configuration.", File::new, Optionality.REQUIRED);
    private static final String PARAMETER_CHALLENGE = "challenge";
    private static final Logger logger = LoggerFactory.getLogger(MapRouletteUploadCommand.class);
    private final Map<String, Challenge> checkNameChallengeMap;

    public MapRouletteUploadCommand()
    {
        super();
        this.checkNameChallengeMap = new HashMap<>();
    }

    @Override protected void execute(final CommandMap commandMap, final MapRouletteConfiguration configuration)
    {
        final File[] files = ((File) commandMap.get(INPUT_DIRECTORY)).listFiles();
        if (files == null)
        {
            logger.error("No files found for the given directory.");
        }
        else
        {
            try
            {
                final Gson gson = new GsonBuilder().registerTypeAdapter(Task.class, new TaskDeserializer(configuration.getProjectName())).create();
                final Configuration instructions = this.loadConfiguration(commandMap);
                for (final File logFile : files)
                {
                    try (BufferedReader reader = new BufferedReader(new FileReader(logFile.getPath())))
                    {
                        reader.lines().forEach(line -> {
                            final Task task = gson.fromJson(line, Task.class);
                            this.getMapRouletteClient().addTask(this.getChallenge(task.getChallengeName(), instructions), task);
                        });
                    }
                    catch (final IOException error)
                    {
                        error.printStackTrace();
                    }
                }
                this.getMapRouletteClient().uploadTasks();
            }
            catch (final IOException error)
            {
                error.printStackTrace();
            }
        }
    }

    /**
     * Given a command map, load the configuration defined by the CONFIG_LOCATION switch and return it
     * @param map the input map of arguments passed to this command
     * @return the configuration at the specified file location
     * @throws FileNotFoundException if the configuration file cannot be found
     */
    private Configuration loadConfiguration(final CommandMap map) throws FileNotFoundException
    {
        return new StandardConfiguration(new InputStreamResource(new FileInputStream((File) map.get(CONFIG_LOCATION))));
    }

    /**
     * Returns a string which can be used as a key in a configuration to get checkName's challenge
     * configuration
     * @param checkName the name of the a check in a configuration file
     * @return checkName.challenge
     */
    private String getChallengeParameter(final String checkName)
    {
        return checkName + "." + PARAMETER_CHALLENGE;
    }

    /**
     * If we've already looked up checkName, return the cached result from our store. Otherwise,
     * read the challenge parameters from fallbackConfiguration and deserialize them into a Challenge
     * object.
     * @param checkName the name of the check
     * @param fallbackConfiguration the full configuration, which contains challenge parameters for
     *                              checkName.
     * @return the check's challenge parameters, stored as a Challenge object.
     */
    private Challenge getChallenge(final String checkName, final Configuration fallbackConfiguration)
    {
        return this.checkNameChallengeMap.computeIfAbsent(checkName, name ->
        {
            final Map<String, String> challengeMap = fallbackConfiguration.get(getChallengeParameter(checkName), Collections.emptyMap()).value();
            final Gson gson = new GsonBuilder().disableHtmlEscaping()
                    .registerTypeAdapter(Challenge.class, new ChallengeDeserializer()).create();
            final Challenge result = gson.fromJson(gson.toJson(challengeMap), Challenge.class);
            result.setName(checkName);
            return result;
        });
    }

    @Override public SwitchList switches()
    {
        return super.switches().with(INPUT_DIRECTORY, CONFIG_LOCATION);
    }

    public static void main(final String[] args)
    {
        new MapRouletteUploadCommand().run(args);
    }
}
