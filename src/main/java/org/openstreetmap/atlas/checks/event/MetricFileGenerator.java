package org.openstreetmap.atlas.checks.event;

import org.openstreetmap.atlas.checks.persistence.SparkFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * A {@link Processor} for {@link MetricEvent}s to write them into files. By default this supports
 * maximum {@code FileProcessor.BATCH_SIZE} metrics. If number of metrics go beyond that limit, the
 * newest metrics will override the previous ones in the file, because the file name is going to be
 * the same for both write operations.
 *
 * @author mkalender
 */
public final class MetricFileGenerator extends FileProcessor<MetricEvent>
{
    private static final Logger logger = LoggerFactory.getLogger(MetricFileGenerator.class);

    // Filename for the metric file
    private final String filename;

    /**
     * Default constructor
     *
     * @param filename
     *            filename for the metric file
     * @param fileHelper
     *            {@link SparkFileHelper} for I/O operations
     * @param outputFolder
     *            output folder path to write files to
     */
    public MetricFileGenerator(final String filename, final SparkFileHelper fileHelper,
            final String outputFolder)
    {
        super(fileHelper, outputFolder);
        this.filename = filename;

        // This will make sure we have a header for the csv file
        this.process(MetricEvent.header());
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void process(final MetricEvent event)
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
            logger.warn("Metric file write is failed.", e);
        }
    }

    @Override
    protected String getFilename()
    {
        return this.filename;
    }
}
