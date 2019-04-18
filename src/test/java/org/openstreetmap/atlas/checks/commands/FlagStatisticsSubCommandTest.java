package org.openstreetmap.atlas.checks.commands;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.event.CheckFlagFileProcessor;
import org.openstreetmap.atlas.checks.event.FileProcessor;
import org.openstreetmap.atlas.checks.event.ShutdownEvent;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.streaming.resource.File;

import com.google.common.collect.ImmutableMap;

/**
 * Unit tests for {@link FlagStatisticsSubCommand}.
 *
 * @author bbreithaupt
 */
public class FlagStatisticsSubCommandTest
{
    // An empty HashMap as an empty Spark configuration
    private static final Map<String, String> FILE_SYSTEM_CONFIG = new HashMap<>();

    // Temp directories
    private static final File SOURCE_DIRECTORY = File.temporaryFolder();
    private static final File TARGET_DIRECTORY = File.temporaryFolder();

    private static final boolean[] COMPRESSION_OPTIONS = { false, true };

    private static final String COUNTRY_1 = "ABC";
    private static final String COUNTRY_2 = "XYZ";

    private static final String CHECK_1 = "Check1";
    private static final String CHECK_2 = "Check2";
    private static final String CHECK_3 = "Check3";

    @Rule
    public final FlagStatisticsSubCommandTestRule setup = new FlagStatisticsSubCommandTestRule();

    /**
     * Generate flag files from {@link CheckFlagEvent}s, into source and target directories. The
     * files can be compressed.
     */
    private void generateLogFilesForCountry(final File directory, final String country,
            final Map<String, Integer> checkFlagCounts)
    {
        final String countryFolderPath = FilenameUtils.concat(directory.getAbsolutePath(), country);

        for (final boolean compression : COMPRESSION_OPTIONS)
        {
            final FileProcessor<CheckFlagEvent> fileProcessor = new CheckFlagFileProcessor(
                    new SparkFileHelper(FILE_SYSTEM_CONFIG), countryFolderPath)
                            .withCompression(compression);
            checkFlagCounts.forEach((check, flagCount) ->
            {
                for (int count = 0; count < flagCount; count++)
                {
                    fileProcessor.process(this.setup.getOneNodeCheckFlagEvent(check));
                }
            });
            fileProcessor.process(new ShutdownEvent());
        }
    }

    /**
     * Generate directories of flag files and gather the path to the first file in each.
     */
    private void populateTestData()
    {
        if (SOURCE_DIRECTORY.listFilesRecursively().isEmpty())
        {
            this.generateLogFilesForCountry(SOURCE_DIRECTORY, COUNTRY_1,
                    ImmutableMap.of(CHECK_1, 3, CHECK_2, 2));
            this.generateLogFilesForCountry(SOURCE_DIRECTORY, COUNTRY_2,
                    ImmutableMap.of(CHECK_1, 2, CHECK_3, 1));
            this.generateLogFilesForCountry(TARGET_DIRECTORY, COUNTRY_1,
                    ImmutableMap.of(CHECK_1, 3, CHECK_2, 1));
            this.generateLogFilesForCountry(TARGET_DIRECTORY, COUNTRY_2,
                    ImmutableMap.of(CHECK_1, 4, CHECK_3, 1));
        }
    }

    @AfterClass
    public static void deleteLogFiles()
    {
        SOURCE_DIRECTORY.deleteRecursively();
        TARGET_DIRECTORY.deleteRecursively();
    }

    @Test
    public void getCountsTest() throws IOException
    {
        this.populateTestData();
        final File outputFile = File.temporary();

        final String[] arguments = { "--input=" + SOURCE_DIRECTORY.getAbsolutePath(),
                "--output=" + outputFile.getAbsolutePath() };
        new FlagStatisticsSubCommand().runSubcommand(arguments);

        final String expectedText = "Check,ABC,XYZ,TotalCheck1,6,4,10Check2,4,ND,4Check3,ND,2,2Total,10,6,16";
        final String actualText = new BufferedReader(new FileReader(outputFile.getFile())).lines()
                .collect(Collectors.joining());

        Assert.assertEquals(expectedText, actualText);

        outputFile.delete();
    }

    @Test
    public void getDiffTest() throws IOException
    {
        this.populateTestData();
        final File outputFile = File.temporary();

        final String[] arguments = { "--input=" + SOURCE_DIRECTORY.getAbsolutePath(),
                "--diff=" + TARGET_DIRECTORY.getAbsolutePath(),
                "--output=" + outputFile.getAbsolutePath() };
        new FlagStatisticsSubCommand().runSubcommand(arguments);

        final String expectedText = "Check,ABC,XYZ,TotalCheck1,0,4,4Check2,-2,ND,-2Check3,ND,0,0Total,-2,4,2";
        final String actualText = new BufferedReader(new FileReader(outputFile.getFile())).lines()
                .collect(Collectors.joining());

        Assert.assertEquals(expectedText, actualText);

        outputFile.delete();
    }
}
