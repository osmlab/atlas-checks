package org.openstreetmap.atlas.checks.event;

import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * A {@link Processor} for {@link CheckFlagEvent}s to write them into line delimited GeoJson files
 *
 * @author mkalender
 */
public final class CheckFlagFileProcessor extends FileProcessor<CheckFlagEvent>
{
    private static final Logger logger = LoggerFactory.getLogger(CheckFlagFileProcessor.class);

    /**
     * Default constructor
     *
     * @param fileHelper
     *            {@link SparkFileHelper} for I/O operations
     * @param outputFolder
     *            output folder path to write files to
     */
    public CheckFlagFileProcessor(final SparkFileHelper fileHelper, final String outputFolder)
    {
        super(fileHelper, outputFolder);
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void process(final CheckFlagEvent event)
    {
        this.process(event.toString());
    }

    @Override
    @Subscribe
    public void process(final ShutdownEvent event)
    {
        try
        {
            this.write();
        }
        catch (final Exception e)
        {
            logger.warn("CheckFlag file write is failed.", e);
        }
    }
}
