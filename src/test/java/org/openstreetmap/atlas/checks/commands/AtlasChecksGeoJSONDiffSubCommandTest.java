package org.openstreetmap.atlas.checks.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.event.CheckFlagGeoJsonProcessor;
import org.openstreetmap.atlas.event.ShutdownEvent;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * Unit tests for {@link AtlasChecksGeoJSONDiffSubCommand}.
 *
 * @author bbreithaupt
 * @author sayas01
 */
public class AtlasChecksGeoJSONDiffSubCommandTest
{
    // An empty HashMap as an empty Spark configuration
    private static final Map<String, String> FILE_SYSTEM_CONFIG = new HashMap<>();

    // Temp directories
    private static final File SOURCE_DIRECTORY = File.temporaryFolder();
    private static final File TARGET_DIRECTORY = File.temporaryFolder();
    private static final File GZ_SOURCE_DIRECTORY = File.temporaryFolder();
    private static final File GZ_TARGET_DIRECTORY = File.temporaryFolder();
    @Rule
    public final JSONFlagDiffSubCommandTestRule setup = new JSONFlagDiffSubCommandTestRule();
    // Temp Files
    private String sourceFile;
    private String targetFile;
    private String sourceFileGZ;
    private String targetFileGZ;

    @AfterClass
    public static void deleteLogFiles()
    {
        SOURCE_DIRECTORY.deleteRecursively();
        TARGET_DIRECTORY.deleteRecursively();
        GZ_SOURCE_DIRECTORY.deleteRecursively();
        GZ_TARGET_DIRECTORY.deleteRecursively();
    }

    @Test
    public void fileCreationFromDirectoryTest()
    {
        this.populateTestData();
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "geojson-diff", String.format("-reference=%s", SOURCE_DIRECTORY),
                String.format("-input=%s", TARGET_DIRECTORY),
                String.format("-output=%s", temp.getPathString()) };
        new AtlasChecksCommand(args).runWithoutQuitting(args);

        final List<File> outputFiles = temp.listFilesRecursively();
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("additions-\\d+-2.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("subtractions-\\d+-2.geojson")));

        temp.deleteRecursively();
    }

    @Test
    public void fileCreationFromFileTest()
    {
        this.populateTestData();
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "geojson-diff", String.format("-reference=%s", this.sourceFile),
                String.format("-input=%s", this.targetFile),
                String.format("-output=%s", temp.getPathString()) };
        new AtlasChecksCommand(args).runWithoutQuitting(args);

        final List<File> outputFiles = temp.listFilesRecursively();
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("additions-\\d+-1.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("subtractions-\\d+-1.geojson")));

        temp.deleteRecursively();
    }

    /**
     * Generate directories of flag files and gather the path to the first file in each.
     */
    public void populateTestData()
    {
        if (this.sourceFile == null)
        {
            this.generateLogFiles(SOURCE_DIRECTORY, TARGET_DIRECTORY, false);
            this.generateLogFiles(GZ_SOURCE_DIRECTORY, GZ_TARGET_DIRECTORY, true);

            this.sourceFile = this.getFirstGeojsonPath(SOURCE_DIRECTORY);
            this.targetFile = this.getFirstGeojsonPath(TARGET_DIRECTORY);
            this.sourceFileGZ = this.getFirstGeojsonPath(GZ_SOURCE_DIRECTORY);
            this.targetFileGZ = this.getFirstGeojsonPath(GZ_TARGET_DIRECTORY);
        }
    }

    @Test
    public void testFileCreationFromGZippedAndUnZippedFiles()
    {
        this.populateTestData();
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "geojson-diff", String.format("-reference=%s", this.sourceFileGZ),
                String.format("-input=%s", this.targetFile),
                String.format("-output=%s", temp.getPathString()) };
        new AtlasChecksCommand(args).runWithoutQuitting(args);

        final List<File> outputFiles = temp.listFilesRecursively();
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("additions-\\d+-1.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("subtractions-\\d+-1.geojson")));

        temp.deleteRecursively();
    }

    @Test
    public void testFileCreationFromGZippedDirectory()
    {
        this.populateTestData();
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "geojson-diff", String.format("-reference=%s", GZ_SOURCE_DIRECTORY),
                String.format("-input=%s", GZ_TARGET_DIRECTORY),
                String.format("-output=%s", temp.getPathString()) };
        new AtlasChecksCommand(args).runWithoutQuitting(args);

        final List<File> outputFiles = temp.listFilesRecursively();
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("additions-\\d+-2.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("subtractions-\\d+-2.geojson")));

        temp.deleteRecursively();
    }

    @Test
    public void testFileCreationFromGZippedFile()
    {
        this.populateTestData();
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "geojson-diff", String.format("-reference=%s", this.sourceFileGZ),
                String.format("-input=%s", this.targetFileGZ),
                String.format("-output=%s", temp.getPathString()) };
        new AtlasChecksCommand(args).runWithoutQuitting(args);

        final List<File> outputFiles = temp.listFilesRecursively();
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("additions-\\d+-1.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("subtractions-\\d+-1.geojson")));

        temp.deleteRecursively();
    }

    /**
     * Generate flag files from {@link CheckFlagEvent}s, into source and target directories. The
     * files can be compressed.
     *
     * @param sourceDirectory
     *            a {@link File} resource for a source directory
     * @param targetDirectory
     *            a {@link File} resource for a target directory
     * @param compression
     *            a boolean for whether the files should be compressed
     */
    private void generateLogFiles(final File sourceDirectory, final File targetDirectory,
            final boolean compression)
    {

        final CheckFlagGeoJsonProcessor sourceProcessor = new CheckFlagGeoJsonProcessor(
                new SparkFileHelper(FILE_SYSTEM_CONFIG), sourceDirectory.getAbsolutePathString())
                        .withBatchSizeOverride(2).withCompression(compression);
        sourceProcessor.process(this.setup.getConstantCheckFlagEvent());
        sourceProcessor.process(this.setup.getSubtractionCheckFlagEvent());
        sourceProcessor.process(this.setup.getPreChangeCheckFlagEvent());
        sourceProcessor.process(new ShutdownEvent());

        final CheckFlagGeoJsonProcessor targetProcessor = new CheckFlagGeoJsonProcessor(
                new SparkFileHelper(FILE_SYSTEM_CONFIG), targetDirectory.getAbsolutePathString())
                        .withBatchSizeOverride(2).withCompression(compression);
        targetProcessor.process(this.setup.getConstantCheckFlagEvent());
        targetProcessor.process(this.setup.getAdditionCheckFlagEvent());
        targetProcessor.process(this.setup.getPostChangeCheckFlagEvent());
        targetProcessor.process(new ShutdownEvent());
    }

    /**
     * Finds the first geojson or gzipped geojson file from an alpha-numeric sorting of a directory,
     * and returns its path.
     *
     * @param directory
     *            The directory to get a path from
     * @return a {@link String} path of the first geojson file
     */
    private String getFirstGeojsonPath(final File directory)
    {
        return directory.listFilesRecursively().stream()
                .filter(file -> file.getName().endsWith(".geojson")
                        || file.getName().endsWith(".geojson.gz"))
                .sorted().collect(Collectors.toList()).get(0).getPathString();
    }
}
