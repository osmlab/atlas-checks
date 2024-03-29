package org.openstreetmap.atlas.checks.maproulette;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.http.HttpHost;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.ChallengeDifficulty;
import org.openstreetmap.atlas.checks.maproulette.data.ProjectConfiguration;
import org.openstreetmap.atlas.checks.maproulette.data.Task;
import org.openstreetmap.atlas.geography.atlas.AtlasLoadingCommand;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an abstract command that gives you a base that can be used for executing data against
 * MapRoulette
 *
 * @author cuthbertm
 * @author mgostintsev
 * @author nachtm
 */
public abstract class MapRouletteCommand extends AtlasLoadingCommand
{
    private static final Logger logger = LoggerFactory.getLogger(MapRouletteCommand.class);
    private static final boolean DEFAULT_ENABLED = true;
    private static final Switch<MapRouletteConfiguration> MAP_ROULETTE = new Switch<>("maproulette",
            "Map roulette server information, format <host>:<port>:<project>:<api_key>",
            MapRouletteConfiguration::parse);
    private static final Switch<String> PROJECT_DISPLAY_NAME = new Switch<>("projectDisplayName",
            "Display name of the project under which all of the challenges will be submitted",
            StringConverter.IDENTITY);
    protected static final Switch<String> OUTPUT_PATH = new Switch<>("outputPath",
            "Full path to file where project id, challenge id are stored after creation in MapRoulette.",
            StringConverter.IDENTITY);
    private static final Switch<String> PROXY = new Switch<>("proxy",
            "scheme://hostname:port for an http proxy", StringConverter.IDENTITY,
            Optionality.OPTIONAL);

    private MapRouletteClient mapRouletteClient;

    public MapRouletteClient getClient()
    {
        return this.mapRouletteClient;
    }

    protected synchronized void addTask(final String challengeName, final Task task)
            throws UnsupportedEncodingException, URISyntaxException
    {
        this.mapRouletteClient.addTask(
                new Challenge(challengeName, "", "", "", ChallengeDifficulty.EASY, "", false),
                task);
    }

    protected synchronized void addTask(final Challenge challenge, final Task task)
            throws UnsupportedEncodingException, URISyntaxException
    {
        this.mapRouletteClient.addTask(challenge, task);
    }

    /**
     * Function will check to see how many tasks are currently added in the batch, and if there are
     * more than the current threshold, then it will upload the current tasks otherwise it will do
     * nothing.
     *
     * @param threshold
     *            the number of tasks that it needs to execute the upload
     */
    protected void checkUploadTasks(final int threshold)
    {
        if (this.getMapRouletteClient().getCurrentBatchSize() >= threshold)
        {
            this.uploadTasks();
        }
    }

    protected abstract void execute(CommandMap commandMap, MapRouletteConfiguration configuration);

    protected MapRouletteClient getMapRouletteClient()
    {
        return this.mapRouletteClient;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected int onRun(final CommandMap commandMap)
    {
        return this.onRun(commandMap, MapRouletteClient::new);
    }

    protected int onRun(final CommandMap commandMap,
            final BiFunction<MapRouletteConfiguration, HttpHost, MapRouletteClient> clientConstructor)
    {
        final MapRouletteConfiguration mapRoulette = this.fromCommandMap(commandMap);
        HttpHost proxy = null;
        final Optional<String> proxyOptional = (Optional<String>) commandMap.getOption(PROXY);
        if (proxyOptional.isPresent())
        {
            logger.info("Using Proxy: {}", proxyOptional.get());
            proxy = HttpHost.create(proxyOptional.get());
        }

        if (mapRoulette != null)
        {
            try
            {
                this.mapRouletteClient = clientConstructor.apply(mapRoulette, proxy);
            }
            catch (final IllegalArgumentException e)
            {
                logger.warn("Failed to initialize the MapRouletteClient", e);
            }
        }
        this.mapRouletteClient.setOutputPath((Optional<String>) commandMap.getOption(OUTPUT_PATH));
        this.execute(commandMap, mapRoulette);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(MAP_ROULETTE, PROXY);
    }

    protected void uploadTasks()
    {
        if (this.mapRouletteClient != null)
        {
            this.mapRouletteClient.uploadTasks();
        }
    }

    private MapRouletteConfiguration fromCommandMap(final CommandMap commandMap)
    {
        final MapRouletteConfiguration mapRoulette = (MapRouletteConfiguration) commandMap
                .get(MAP_ROULETTE);
        final String projectDisplayName = (String) commandMap.get(PROJECT_DISPLAY_NAME);

        if (projectDisplayName == null)
        {
            return mapRoulette;
        }
        final ProjectConfiguration project = new ProjectConfiguration(mapRoulette.getProjectName(),
                mapRoulette.getProjectName(), projectDisplayName, DEFAULT_ENABLED);
        return new MapRouletteConfiguration(mapRoulette.getScheme(), mapRoulette.getServer(),
                mapRoulette.getPort(), project, mapRoulette.getApiKey());
    }
}
