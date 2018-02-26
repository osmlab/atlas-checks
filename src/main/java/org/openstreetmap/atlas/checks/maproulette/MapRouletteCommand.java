package org.openstreetmap.atlas.checks.maproulette;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.ChallengeDifficulty;
import org.openstreetmap.atlas.checks.maproulette.data.Task;
import org.openstreetmap.atlas.geography.atlas.AtlasLoadingCommand;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an abstract command that gives you a base that can be used for executing data against
 * MapRoulette
 *
 * @author cuthbertm
 * @author mgostintsev
 */
public abstract class MapRouletteCommand extends AtlasLoadingCommand
{
    private static final Logger logger = LoggerFactory.getLogger(MapRouletteCommand.class);
    private static final Switch<MapRouletteConfiguration> MAP_ROULETTE = new Switch<>("maproulette",
            "Map roulette server information, format <host>:<port>:<project>:<api_key>",
            MapRouletteConfiguration::parse);
    private MapRouletteClient mapRouletteClient;

    public MapRouletteClient getClient()
    {
        return this.mapRouletteClient;
    }

    protected synchronized void addTask(final String challengeName, final Task task)
            throws UnsupportedEncodingException, URISyntaxException
    {
        this.mapRouletteClient.addTask(
                new Challenge(challengeName, "", "", "", ChallengeDifficulty.EASY, ""), task);
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
        final MapRouletteConfiguration mapRoulette = (MapRouletteConfiguration) commandMap
                .get(MAP_ROULETTE);
        if (mapRoulette != null)
        {
            try
            {
                this.mapRouletteClient = new MapRouletteClient(mapRoulette);
            }
            catch (final IllegalArgumentException e)
            {
                logger.warn("Failed to initialize the MapRouletteClient", e);
            }
        }
        execute(commandMap, mapRoulette);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(MAP_ROULETTE);
    }

    protected void uploadTasks()
    {
        if (this.mapRouletteClient != null)
        {
            this.mapRouletteClient.uploadTasks();
        }
    }
}
