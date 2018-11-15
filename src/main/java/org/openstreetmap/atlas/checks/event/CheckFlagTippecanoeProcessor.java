package org.openstreetmap.atlas.checks.event;

import java.util.Date;
import java.util.function.Consumer;

import org.openstreetmap.atlas.checks.distributed.GeoJsonPathFilter;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonObject;

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

    private static final int FLAG_MINIMUM_ZOOM = 1;
    private static final int FEATURE_MINIMUM_ZOOM = 8;

    private static final Consumer<JsonObject> TIPPECANOE_JSON_MUTATOR = jsonObject ->
    {
        final JsonObject properties = jsonObject.getAsJsonObject("properties");
        final String type = properties.get("flag:type").getAsString();

        final JsonObject tippecanoe = new JsonObject();
        jsonObject.add("tippecanoe", tippecanoe);
        tippecanoe.addProperty("layer", type);

        if (CheckFlag.class.getSimpleName().equals(type))
        {
            tippecanoe.addProperty("minzoom", FLAG_MINIMUM_ZOOM);
        }
        else
        {
            tippecanoe.addProperty("minzoom", FEATURE_MINIMUM_ZOOM);
        }
    };

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
        super.process(event.asLineDelimitedGeoJsonFeatures(TIPPECANOE_JSON_MUTATOR));
    }

    @Override
    @Subscribe
    public void process(final ShutdownEvent event)
    {
        try
        {
            super.write();
        }
        catch (final Exception e)
        {
            logger.warn("CheckFlag file write is failed.", e);
        }
    }

    /**
     * @return the name of the file to be used in {@code #write()} method to write files
     */
    @Override
    protected String getFilename()
    {
        return String.format("%s-%s%s", new Date().getTime(), getCount(),
                new GeoJsonPathFilter(doesCompressOutput()).getExtension());
    }
}
