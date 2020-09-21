package org.openstreetmap.atlas.checks.event;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.event.ShutdownEvent;
import org.openstreetmap.atlas.generator.tools.filesystem.FileSystemHelper;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.streaming.resource.Resource;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Tests for {@link CheckFlagTippecanoeProcessor}.
 *
 * @author hallahan
 */
public class CheckFlagTippecanoeProcessorTest
{
    private static final Map<String, String> FILE_SYSTEM_CONFIG = new HashMap<>();
    private static final Gson GSON_BUILDER = new Gson();
    private static final int BATCH_SIZE = FileProcessor.BATCH_SIZE;

    /**
     * Might as well just use the test rule for CheckFlagGeoJsonProcessor, as that works fine for
     * this too.
     */
    @Rule
    public CheckFlagGeoJsonProcessorTestRule setup = new CheckFlagGeoJsonProcessorTestRule();

    @Test
    public void testHundredEvent() throws IOException
    {
        this.processCompleteAndValidate(100);
    }

    @Test
    public void testOneEvent() throws IOException
    {
        this.processCompleteAndValidate(1);
    }

    @Test
    public void testTenEvent() throws IOException
    {
        this.processCompleteAndValidate(10);
    }

    @Test
    public void testTwoEvent() throws IOException
    {
        this.processCompleteAndValidate(2);
    }

    @Test
    public void testZeroEvent() throws IOException
    {
        this.processCompleteAndValidate(0);
    }

    private void processCompleteAndValidate(final int eventCount)
    {
        // Generate
        final File tempDirectory = Files.createTempDir();

        final CheckFlagTippecanoeProcessor processor = new CheckFlagTippecanoeProcessor(
                new SparkFileHelper(FILE_SYSTEM_CONFIG), tempDirectory.getAbsolutePath());

        for (int index = 0; index < eventCount; index++)
        {
            processor.process(this.setup.getCheckFlagEvent());
        }
        processor.process(new ShutdownEvent());

        // Validate
        final List<Resource> files = FileSystemHelper.resources(tempDirectory.getAbsolutePath(),
                FILE_SYSTEM_CONFIG);
        Assert.assertEquals(Math.max((int) Math.ceil(eventCount / (double) BATCH_SIZE), 1),
                files.size());

        this.processTippecanoeLineDelimitedGeoJson(eventCount, files);

        // Cleanup
        tempDirectory.delete();
    }

    private void processTippecanoeLineDelimitedGeoJson(final int eventCount,
            final List<Resource> files)
    {
        int checkFlags = 0;
        for (final Resource file : files)
        {
            final String ldgeojson = file.readAndClose();

            // If we have zero events, the file should just be an empty string.
            if ("".equals(ldgeojson))
            {
                Assert.assertEquals(0, eventCount);
                return;
            }

            final String[] features = ldgeojson.split("\n");

            /*
             * From the test fixture, each event is a check flag + 3 flagged objects.
             */
            Assert.assertEquals(4 * eventCount, features.length);

            for (final String featureString : features)
            {
                final JsonObject feature = GSON_BUILDER.fromJson(featureString, JsonObject.class);
                final String flagType = feature.get("properties").getAsJsonObject().get("flag:type")
                        .getAsString();
                if ("CheckFlag".equals(flagType))
                {
                    ++checkFlags;
                }
            }
        }

        // A given event should have one check flag.
        Assert.assertEquals(eventCount, checkFlags);
    }
}
