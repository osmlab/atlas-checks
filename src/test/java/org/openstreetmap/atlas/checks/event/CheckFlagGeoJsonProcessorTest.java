package org.openstreetmap.atlas.checks.event;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.generator.tools.filesystem.FileSystemHelper;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.streaming.resource.Resource;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Tests for {@link MetricFileGenerator}.
 *
 * @author brian_l_davis
 */
public class CheckFlagGeoJsonProcessorTest
{
    private static final Map<String, String> FILE_SYSTEM_CONFIG = new HashMap<>();
    private static final Gson GSON_BUILDER = new Gson();

    @Rule
    public CheckFlagGeoJsonProcessorTestRule setup = new CheckFlagGeoJsonProcessorTestRule();

    @Test
    public void testHundredEvent() throws IOException
    {
        processCompleteAndValidate(100);
    }

    @Test
    public void testOneEvent() throws IOException
    {
        processCompleteAndValidate(1);
    }

    @Test
    public void testTenEvent() throws IOException
    {
        processCompleteAndValidate(10);
    }

    @Test
    public void testTwoEvent() throws IOException
    {
        processCompleteAndValidate(2);
    }

    @Test
    public void testZeroEvent() throws IOException
    {
        processCompleteAndValidate(0);
    }

    private void processCompleteAndValidate(final int eventCount)
    {
        // Generate
        final File tempDirectory = Files.createTempDir();
        final CheckFlagGeoJsonProcessor processor = new CheckFlagGeoJsonProcessor(
                new SparkFileHelper(FILE_SYSTEM_CONFIG), tempDirectory.getAbsolutePath())
                        .withBatchSizeOverride(25);
        for (int index = 0; index < eventCount; index++)
        {
            processor.process(this.setup.getCheckFlagEvent());
        }
        processor.process(new ShutdownEvent());

        // Validate
        final List<Resource> files = FileSystemHelper.resources(tempDirectory.getAbsolutePath(),
                FILE_SYSTEM_CONFIG);
        Assert.assertEquals(
                Math.max((int) Math.ceil(eventCount / (double) processor.computeBatchSize()), 1),
                files.size());

        int actualEventCount = 0;
        for (final Resource file : files)
        {
            final JsonObject found = GSON_BUILDER.fromJson(new InputStreamReader(file.read()),
                    JsonObject.class);
            if (Objects.nonNull(found))
            {
                actualEventCount += found.getAsJsonArray("features").size();
            }
        }

        Assert.assertEquals(eventCount, actualEventCount);

        // Cleanup
        tempDirectory.delete();
    }
}
