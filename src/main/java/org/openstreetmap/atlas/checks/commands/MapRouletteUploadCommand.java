package org.openstreetmap.atlas.checks.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
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
    private class TaskDeserializer implements JsonDeserializer<Task>
    {
        private String projectName;

        TaskDeserializer(final String projectName)
        {
            this.projectName = projectName;
        }

        @Override public Task deserialize(final JsonElement json, final Type typeOfT,
                final JsonDeserializationContext context) throws JsonParseException
        {
            final JsonObject full = json.getAsJsonObject();
            final JsonObject properties = full.get("properties").getAsJsonObject();
            final String challengeName = properties.get("generator").getAsString();
            final String instruction = properties.get("instructions").getAsString();
            final String taskID = properties.get("id").getAsString();
            final JsonArray geojson = full.get("features").getAsJsonArray();
            final Task result = new Task();
            //challenge name
            result.setChallengeName(challengeName);
            //geojson
            result.setGeoJson(Optional.of(geojson));
            //instruction
            result.setInstruction(instruction);
            // points should be set at the same time as the geojson
            //project name
            result.setProjectName(projectName);
            //task id
            result.setTaskIdentifier(taskID);
            return result;
        }
    }

    private static final Switch<File> INPUT_DIRECTORY = new Command.Switch<>("logfiles",
            "blah blah blah", File::new, Optionality.REQUIRED);
    private static final Switch<File> CONFIG_LOCATION = new Command.Switch<>("config",
            "blah", File::new, Optionality.REQUIRED);
    private static final String PARAMETER_CHALLENGE = "challenge";
    private static final Logger logger = LoggerFactory.getLogger(MapRouletteUploadCommand.class);
    private final Map<String, Challenge> checkNameChallengeMap;

    @Override public SwitchList switches()
    {
        return super.switches().with(INPUT_DIRECTORY, CONFIG_LOCATION);
    }
    // see RunnableCheck.uploadTasks()

    public MapRouletteUploadCommand()
    {
        super();
        this.checkNameChallengeMap = new HashMap<>();
    }

    @Override protected void execute(final CommandMap commandMap, final MapRouletteConfiguration configuration)
    {
        // read in the directory
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
                final Configuration instructions = this
                        .loadConfiguration(commandMap, CONFIG_LOCATION);
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

    // TODO empty challenge config?
    private Challenge readChallenge(final Configuration configuration, final String checkName)
    {
        final Map<String, String> challengeMap = configuration.get(getChallengeParameter(checkName), Collections.EMPTY_MAP).value();
        final Gson gson = new GsonBuilder().disableHtmlEscaping()
                .registerTypeAdapter(Challenge.class, new ChallengeDeserializer()).create();
        final Challenge result = gson.fromJson(gson.toJson(challengeMap), Challenge.class);
        result.setName(checkName);
        return result;
    }

    private Configuration loadConfiguration(final CommandMap map, final Switch<File> configSwitch) throws IOException
    {
        return new StandardConfiguration(new InputStreamResource(new FileInputStream((File) map.get(configSwitch))));
    }

    private String getChallengeParameter(final String checkName)
    {
        return checkName + "." + PARAMETER_CHALLENGE;
    }

    // loads a challenge from our store if it exists, otherwise reads from configuration
    private Challenge getChallenge(final String checkName, final Configuration fallbackConfiguration)
    {
        return this.checkNameChallengeMap.computeIfAbsent(checkName, name -> readChallenge(fallbackConfiguration, name));
    }

    public static void main(final String[] args)
    {
        new MapRouletteUploadCommand().run(args);
    }
}
