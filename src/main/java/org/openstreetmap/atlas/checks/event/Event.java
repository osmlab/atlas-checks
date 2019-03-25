package org.openstreetmap.atlas.checks.event;

import java.util.Date;

/**
 * Useful base class to hold common information for {@link Event} implementations
 *
 * @deprecated - use {@link org.openstreetmap.atlas.event.Event}.
 * @author mkalender
 */
@Deprecated
public abstract class Event
{
    private final Date timestamp;

    /**
     * Default constructor
     */
    protected Event()
    {
        this.timestamp = new Date();
    }

    protected Date getTimestamp()
    {
        return this.timestamp;
    }
}
