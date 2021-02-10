package org.openstreetmap.atlas.checks.event;

import java.util.Date;

import org.apache.spark.TaskContext;
import org.openstreetmap.atlas.checks.distributed.GeoJsonPathFilter;
import org.openstreetmap.atlas.checks.vectortiles.TippecanoeCheckSettings;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * This class is similar to CheckFlagFileProcessor, except here we're making line-delimited GeoJSON
 * that plays well with tippecanoe.
 *
 * @author hallahan
 */
public class CheckFlagTippecanoeProcessor extends FileProcessor<CheckFlagEvent>
{

    private static final Logger logger = LoggerFactory
            .getLogger(CheckFlagTippecanoeProcessor.class);

    /**
     * Default constructor
     *
     * @param fileHelper
     *            {@link SparkFileHelper} instance for I/O operations
     * @param directory
     *            The directory to write output
     */
    public CheckFlagTippecanoeProcessor(final SparkFileHelper fileHelper, final String directory)
    {
        super(fileHelper, directory);
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void process(final CheckFlagEvent event)
    {
        super.process(event.asLineDelimitedGeoJsonFeatures(TippecanoeCheckSettings.JSON_MUTATOR));
    }

    @Override
    @Subscribe
    public void process(final org.openstreetmap.atlas.event.ShutdownEvent event)
    {
        try
        {
            super.write();
        }
        catch (final Exception e)
        {
            logger.warn("CheckFlagTippecanoeProcessor write failed.", e);
        }
    }

    /**
     * @return the name of the file to be used in {@code #write()} method to write files
     */
    @Override
    protected String getFilename()
    {
        return String.format("%sP%s-%s%s", new Date().getTime(), TaskContext.getPartitionId(),
                getCount(), new GeoJsonPathFilter(doesCompressOutput()).getExtension());
    }
}
