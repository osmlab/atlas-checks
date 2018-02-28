package org.openstreetmap.atlas.checks.event;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.generator.tools.filesystem.FileSystemHelper;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.streaming.resource.Resource;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Tests for {@link CheckFlagFileProcessor}.
 *
 * @author mkalender
 */
public class CheckFlagFileProcessorTest
{
    private static final Gson GSON_BUILDER = new Gson();
    private static final Map<String, String> FILE_SYSTEM_CONFIG = new HashMap<>();
    private static final int BATCH_SIZE = FileProcessor.BATCH_SIZE;
    private static final CheckFlagEvent SAMPLE_EVENT;

    static
    {
        SAMPLE_EVENT = new CheckFlagEvent("sample-name", new CheckFlag("a check flag"));
        SAMPLE_EVENT.getCheckFlag().addInstruction("First instruction");
        SAMPLE_EVENT.getCheckFlag().addInstruction("Second instruction");
    }

    @Test
    public void testBatchSizeEvent() throws IOException
    {
        processCompleteAndValidate(BATCH_SIZE);
    }

    @Test
    public void testBatchSizeMinusOneEvent() throws IOException
    {
        processCompleteAndValidate(BATCH_SIZE - 1);
    }

    @Test
    public void testBatchSizePlusOneEvent() throws IOException
    {
        processCompleteAndValidate(BATCH_SIZE + 1);
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
    public void testTwoBatchSizeEvent() throws IOException
    {
        processCompleteAndValidate(2 * BATCH_SIZE);
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

    private void compareJsonAndEventObject(final String json, final CheckFlagEvent event)
    {
        final JsonObject jsonObject = GSON_BUILDER.fromJson(json, JsonObject.class);
        Assert.assertEquals(jsonObject, SAMPLE_EVENT.toGeoJsonFeatureCollection());
    }

    private void processCompleteAndValidate(final int eventCount)
    {
        // Generate
        final File tempDirectory = Files.createTempDir();
        final CheckFlagFileProcessor processor = new CheckFlagFileProcessor(
                new SparkFileHelper(FILE_SYSTEM_CONFIG), tempDirectory.getAbsolutePath());
        for (int index = 0; index < eventCount; index++)
        {
            processor.process(SAMPLE_EVENT);
        }
        processor.process(new ShutdownEvent());

        // Validate
        final List<Resource> files = FileSystemHelper.resources(tempDirectory.getAbsolutePath(),
                FILE_SYSTEM_CONFIG);
        Assert.assertEquals((int) Math.floor(eventCount / (double) BATCH_SIZE) + 1, files.size());

        int actualEventCount = 0;
        for (final Resource file : files)
        {
            for (final String line : file.lines())
            {
                actualEventCount++;
                compareJsonAndEventObject(line, SAMPLE_EVENT);
            }
        }

        Assert.assertEquals(eventCount, actualEventCount);

        // Cleanup
        tempDirectory.delete();
    }
}
