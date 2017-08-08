package org.openstreetmap.atlas.checks.distributed;

import org.apache.commons.lang.StringUtils;
import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.event.EventService;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.maproulette.MapRouletteClient;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class to with helper methods for {@link RunnableCheck}
 *
 * @author mkalender
 * @param <T>
 *            this would either be a {@link Check} type
 */
public abstract class RunnableCheckBase<T extends Check>
{
    private static final Logger logger = LoggerFactory.getLogger(RunnableCheckBase.class);
    private static final int MINIMUM_DURATION_SECONDS = 5;
    private static final int MINIMUM_DURATION_RATIO = 10;

    private final T check;
    private final Challenge challenge;
    private final String country;
    private final String name;
    private final MapRouletteClient client;
    private final Iterable<AtlasObject> objects;
    private final EventService eventService;

    /**
     * Calculates max {@link Duration} timeout for given batch size
     *
     * @param batchSize
     *            batch size to calculate timeout for
     * @return {@link Duration} timeout for given batch size
     */
    private static Duration maxDurationForBatch(final int batchSize)
    {
        return Duration
                .seconds(Math.max(MINIMUM_DURATION_SECONDS, batchSize / MINIMUM_DURATION_RATIO));
    }

    /**
     * Default constructor
     *
     * @param country
     *            country that is being processed
     * @param check
     *            check that is being executed
     * @param objects
     *            {@link AtlasObject}s that are going to be executed
     * @param client
     *            {@link MapRouletteClient} that will upload the tasks to MapRoulette
     */
    public RunnableCheckBase(final String country, final T check,
            final Iterable<AtlasObject> objects, final MapRouletteClient client)
    {
        this.country = country;
        this.check = check;
        this.challenge = check.getChallenge();
        this.name = this.check.getCheckName();
        if (StringUtils.isEmpty(this.challenge.getName()))
        {
            this.challenge.setName(this.name);
        }
        this.objects = objects;
        this.client = client;
        this.eventService = EventService.get(country);
    }

    /**
     * @return The {@link EventService} events are published to
     */
    public EventService getEventService()
    {
        return this.eventService;
    }

    /**
     * Adds a {@link CheckFlag} to {@link MapRouletteClient}
     *
     * @param flag
     *            {@link CheckFlag} to create MapRoulette task
     */
    protected void addTask(final CheckFlag flag)
    {
        if (this.client != null)
        {
            try
            {
                this.client.addTask(this.check.getChallenge(), flag.getMapRouletteTask());
            }
            catch (final Exception e)
            {
                logger.warn("Failed to create a MapRoulette task for [{}, {}]. Exception: {}.",
                        this.name, flag, e);
            }
        }
    }

    protected T getCheck()
    {
        return this.check;
    }

    protected MapRouletteClient getClient()
    {
        return this.client;
    }

    protected String getCountry()
    {
        return this.country;
    }

    protected String getName()
    {
        return this.name;
    }

    protected Iterable<AtlasObject> getObjects()
    {
        return this.objects;
    }

    /**
     * Uploads {@link CheckFlag}s to MapRoulette in a separate thread. Thread will timeout if it
     * does not complete by given timeout time.
     */
    protected void uploadTasks()
    {
        if (this.client != null)
        {
            try (Pool uploadPool = new Pool(1,
                    String.format("MR upload pool for %s (%s)", this.getName(), this.getCountry()),
                    maxDurationForBatch(this.getClient().getCurrentBatchSize())))
            {
                uploadPool.queue(() -> this.getClient().uploadTasks());
            }
            catch (final Exception e)
            {
                logger.error("Failed to upload tasks to MapRoulette client {}.", this.client, e);
            }
        }
        else
        {
            // setting as trace otherwise will just through
            logger.trace(
                    "Ignoring upload to MapRoulette. Client was never initialized correctly. See beginning of log for more details.");
        }
    }
}
