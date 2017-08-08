package org.openstreetmap.atlas.checks.event;

import org.openstreetmap.atlas.utilities.scalars.Duration;

/**
 * A wrapper around a timed metric with a name and a {@link Duration}. This class is useful to
 * generate in-memory metric events from integrity checks to the processors of integrity check
 * results.
 *
 * @author mkalender
 */
public final class MetricEvent extends Event
{
    private final String name;
    private final Duration duration;

    /**
     * @return header following {@code toString()} method format
     */
    public static String header()
    {
        return "name,duration (ms)";
    }

    /**
     * Default constructor
     *
     * @param name
     *            name of metric
     * @param duration
     *            {@link Duration} of the metric
     */
    public MetricEvent(final String name, final Duration duration)
    {
        this.name = name;
        this.duration = duration;
    }

    /**
     * @return {@link Duration} of the metric
     */
    public Duration getDuration()
    {
        return this.duration;
    }

    /**
     * @return name of the metric
     */
    public String getName()
    {
        return this.name;
    }

    @Override
    public String toString()
    {
        return String.format("%s,%s", this.getName(), this.getDuration().asMilliseconds());
    }
}
