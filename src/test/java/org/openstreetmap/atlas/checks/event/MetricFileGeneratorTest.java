package org.openstreetmap.atlas.checks.event;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openstreetmap.atlas.checks.persistence.SparkFileHelper;
import org.openstreetmap.atlas.generator.tools.filesystem.FileSystemHelper;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.scalars.Duration;

/**
 * Tests for {@link MetricFileGenerator}.
 *
 * @author mkalender
 */
public class MetricFileGeneratorTest
{
    private static final Map<String, String> FILE_SYSTEM_CONFIG = new HashMap<>();
    private static final int BATCH_SIZE = FileProcessor.BATCH_SIZE;
    private static final MetricEvent SAMPLE_EVENT;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    static
    {
        SAMPLE_EVENT = new MetricEvent("a-metric-name", Duration.ONE_MINUTE);
    }

    @Test
    public void testFileName() throws IOException
    {
        final File tempDirectory = tempFolder.newFolder();
        final MetricFileGenerator generator = new MetricFileGenerator("some-metrics-log.csv",
                new SparkFileHelper(FILE_SYSTEM_CONFIG), tempDirectory.getAbsolutePath());

        final String filename = generator.getFilename();

        Assert.assertTrue(filename.matches("^some-metrics-log-\\d+.csv$"));
    }

    @Test
    public void testFileLabelWithUnknownSuffix() throws IOException
    {
        final File tempDirectory = tempFolder.newFolder();
        final MetricFileGenerator generator = new MetricFileGenerator("some.metrics.out",
                new SparkFileHelper(FILE_SYSTEM_CONFIG), tempDirectory.getAbsolutePath());

        final String filename = generator.getFilename();

        Assert.assertTrue(filename.matches("^some\\.metrics\\.out-\\d+\\.csv$"));
    }

    @Test
    public void testFileLabelWithKnownSuffix() throws IOException
    {
        final File tempDirectory = tempFolder.newFolder();
        final MetricFileGenerator generator = new MetricFileGenerator("some.metrics.txt",
                new SparkFileHelper(FILE_SYSTEM_CONFIG), tempDirectory.getAbsolutePath());

        final String filename = generator.getFilename();

        Assert.assertTrue(filename.matches("^some\\.metrics-\\d+\\.txt$"));
    }

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

    private void processCompleteAndValidate(final int eventCount) throws IOException
    {
        // Generate
        final File tempDirectory = tempFolder.newFolder();
        final MetricFileGenerator processor = new MetricFileGenerator("some-file-name.csv",
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
                // This is first line
                if (actualEventCount == 0)
                {
                    Assert.assertEquals(MetricEvent.header(), line);
                }
                else
                {
                    Assert.assertEquals("a-metric-name,60000", line);
                }

                actualEventCount++;
            }
        }

        // Plus 1 is for the header
        Assert.assertEquals(eventCount + 1, actualEventCount);
    }
}
