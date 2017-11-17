package org.openstreetmap.atlas.checks.event;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import org.openstreetmap.atlas.checks.persistence.SparkFileHelper;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
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

    private final String label;
    private final FileSuffix suffix;

    /**
     * Default constructor
     *
     * @param label
     *            label for the metric file
     * @param fileHelper
     *            {@link SparkFileHelper} for I/O operations
     * @param outputFolder
     *            output folder path to write files to
     */
    public MetricFileGenerator(final String label, final SparkFileHelper fileHelper,
            final String outputFolder)
    {
        super(fileHelper, outputFolder);
        final Optional<FileSuffix> knownSuffix = Arrays.stream(FileSuffix.values())
                .filter(suffix -> label.endsWith(suffix.toString())).findFirst();
        if (knownSuffix.isPresent())
        {
            this.label = label.substring(0, label.lastIndexOf(String.valueOf(knownSuffix.get())));
            this.suffix = knownSuffix.get();
        }
        else
        {
            this.label = label;
            this.suffix = FileSuffix.CSV;
        }

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
        return String.format("%s-%s%s", label, new Date().getTime(), suffix);
    }
}
