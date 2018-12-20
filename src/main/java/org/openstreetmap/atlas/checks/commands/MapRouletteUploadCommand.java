package org.openstreetmap.atlas.checks.commands;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.openstreetmap.atlas.checks.maproulette.MapRouletteCommand;
import org.openstreetmap.atlas.checks.maproulette.MapRouletteConfiguration;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.Task;
import org.openstreetmap.atlas.checks.maproulette.serializer.ChallengeDeserializer;
import org.openstreetmap.atlas.checks.maproulette.serializer.TaskDeserializer;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.configuration.StandardConfiguration;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Given a directory of log files created by atlas-checks, upload those files to MapRoulette.
 *
 * @author nachtm
 */
public class MapRouletteUploadCommand extends MapRouletteCommand
{
    private static final Switch<File> INPUT_DIRECTORY = new Switch<>("logfiles",
            "Path to folder containing log files to upload to MapRoulette.", File::new,
            Optionality.REQUIRED);
    private static final Switch<File> CONFIG_LOCATION = new Switch<>("config",
            "Path to a file containing MapRoulette challenge configuration.", File::new,
            Optionality.OPTIONAL, "config/configuration.json");
    private static final String PARAMETER_CHALLENGE = "challenge";
    private static final String LOG_EXTENSION = "log";
    private static final Logger logger = LoggerFactory.getLogger(MapRouletteUploadCommand.class);
    private final Map<String, Challenge> checkNameChallengeMap;

    public MapRouletteUploadCommand()
    {
        super();
        this.checkNameChallengeMap = new HashMap<>();
    }

    @Override
    protected void execute(final CommandMap commandMap,
            final MapRouletteConfiguration configuration)
    {
        final Gson gson = new GsonBuilder().registerTypeAdapter(Task.class,
                new TaskDeserializer(configuration.getProjectName())).create();
        final Configuration instructions = this.loadConfiguration(commandMap);
        ((File) commandMap.get(INPUT_DIRECTORY)).listFilesRecursively().forEach(logFile ->
        {
            if (FilenameUtils.getExtension(logFile.getName()).equals(LOG_EXTENSION))
            {
                try (BufferedReader reader = new BufferedReader(new FileReader(logFile.getPath())))
                {
                    reader.lines().forEach(line ->
                    {
                        final Task task = gson.fromJson(line, Task.class);
                        try
                        {
                            this.addTask(this.getChallenge(task.getChallengeName(), instructions),
                                    task);
                        }
                        catch (URISyntaxException | UnsupportedEncodingException error)
                        {
                            logger.error("Error thrown while adding task: ", error);
                        }
                    });
                    this.uploadTasks();
                }
                catch (final IOException error)
                {
                    logger.error(
                            MessageFormat.format("Error while reading {0}: ", logFile.getName()),
                            error);
                }
            }
        });
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
            result.setName(checkName);
            return result;
        });
    }

    @Override
    public SwitchList switches()
    {
        return super.switches().with(INPUT_DIRECTORY, CONFIG_LOCATION);
    }

    public static void main(final String[] args)
    {
        new MapRouletteUploadCommand().run(args);
    }
}
