package org.openstreetmap.atlas.checks.distributed;

import org.apache.commons.lang.StringUtils;
import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.event.EventService;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
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

    private final T check;
    private final Challenge challenge;
    private final String country;
    private final String name;
    private final Iterable<AtlasObject> objects;
    private final EventService eventService;

    /**
     * Default constructor
     *
     * @param country
     *            country that is being processed
     * @param check
     *            check that is being executed
     * @param objects
     *            {@link AtlasObject}s that are going to be executed
     */
    public RunnableCheckBase(final String country, final T check,
            final Iterable<AtlasObject> objects, final EventService eventService)
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
        this.eventService = eventService;
    }

    /**
     * @return The {@link EventService} events are published to
     */
    public EventService getEventService()
    {
        return this.eventService;
    }

    protected T getCheck()
    {
        return this.check;
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
}
