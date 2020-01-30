package org.openstreetmap.atlas.checks.distributed;

import java.util.Optional;

import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.event.MetricEvent;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.maproulette.MapRouletteClient;
import org.openstreetmap.atlas.event.EventService;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a {@link Check} over {@link AtlasObject}s in a separate thread. {@link CheckFlag}s that
 * result from checking an {@link AtlasObject} are posted to the
 * {@link RunnableCheckBase#eventService} along with {@link MapRouletteClient} for handling.
 *
 * @author mkalender
 * @author bbreithaupt
 */
public final class RunnableCheck extends RunnableCheckBase<Check> implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(RunnableCheck.class);

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
    public RunnableCheck(final String country, final Check check,
            final Iterable<AtlasObject> objects, final MapRouletteClient client)
    {
        super(country, check, objects, client);
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
     * @param eventService
     *            {@link EventService} to post to
     */
    public RunnableCheck(final String country, final Check check,
            final Iterable<AtlasObject> objects, final EventService eventService)
    {
        super(country, check, objects, null, eventService);
    }

    /**
     * Runs the {@link Check} over {@link AtlasObject}s, posting resulting {@link CheckFlag}s to
     * {@link RunnableCheckBase#eventService} and {@link MapRouletteClient}
     */
    @Override
    public void run()
    {
        try
        {
            final Time timer = Time.now();
            this.getObjects().forEach(object ->
            {
                final Optional<CheckFlag> flag = this.getCheck().check(object);
                if (flag.isPresent())
                {
                    this.addTask(flag.get());
                    this.getEventService().post(new CheckFlagEvent(this.getName(), flag.get()));
                }
            });

            this.getCheck().clear();
            final Duration checkRunTime = timer.elapsedSince();
            logger.info("{} completed in {}.", this.getName(), checkRunTime);
            this.getEventService().post(new MetricEvent(this.getName(), checkRunTime));

            this.uploadTasks();
        }
        catch (final Exception e)
        {
            logger.error(String.format("%s failed to complete.", this.getName()), e);
        }
    }
}
