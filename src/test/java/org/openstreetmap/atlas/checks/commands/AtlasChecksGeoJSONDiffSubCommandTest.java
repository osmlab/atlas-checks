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
import org.openstreetmap.atlas.checks.event.ShutdownEvent;
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

    // Temp Files
    private String sourceFile;
    private String targetFile;
    private String sourceFileGZ;
    private String targetFileGZ;

    @Rule
    public final JSONFlagDiffSubCommandTestRule setup = new JSONFlagDiffSubCommandTestRule();

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
                new SparkFileHelper(FILE_SYSTEM_CONFIG), sourceDirectory.getAbsolutePath())
                        .withBatchSizeOverride(2).withCompression(compression);
        sourceProcessor.process(this.setup.getConstantCheckFlagEvent());
        sourceProcessor.process(this.setup.getSubtractionCheckFlagEvent());
        sourceProcessor.process(this.setup.getPreChangeCheckFlagEvent());
        sourceProcessor.process(new ShutdownEvent());

        final CheckFlagGeoJsonProcessor targetProcessor = new CheckFlagGeoJsonProcessor(
                new SparkFileHelper(FILE_SYSTEM_CONFIG), targetDirectory.getAbsolutePath())
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
                .sorted().collect(Collectors.toList()).get(0).getPath();
    }

    /**
     * Generate directories of flag files and gather the path to the first file in each.
     */
    public void populateTestData()
    {
        if (sourceFile == null)
        {
            this.generateLogFiles(SOURCE_DIRECTORY, TARGET_DIRECTORY, false);
            this.generateLogFiles(GZ_SOURCE_DIRECTORY, GZ_TARGET_DIRECTORY, true);

            sourceFile = this.getFirstGeojsonPath(SOURCE_DIRECTORY);
            targetFile = this.getFirstGeojsonPath(TARGET_DIRECTORY);
            sourceFileGZ = this.getFirstGeojsonPath(GZ_SOURCE_DIRECTORY);
            targetFileGZ = this.getFirstGeojsonPath(GZ_TARGET_DIRECTORY);
        }
    }

    @AfterClass
    public static void deleteLogFiles()
    {
        SOURCE_DIRECTORY.deleteRecursively();
        TARGET_DIRECTORY.deleteRecursively();
        GZ_SOURCE_DIRECTORY.deleteRecursively();
        GZ_TARGET_DIRECTORY.deleteRecursively();
    }

    @Test
    public void fileCreationFromFileTest()
    {
        this.populateTestData();
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "geojson-diff", String.format("-source=%s", sourceFile),
                String.format("-target=%s", targetFile),
                String.format("-output=%s", temp.getPath()) };
        new AtlasChecksCommand(args).runWithoutQuitting(args);

        final List<File> outputFiles = temp.listFilesRecursively();
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("additions-\\d+-1.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("changes-\\d+-0.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("subtractions-\\d+-1.geojson")));

        temp.deleteRecursively();
    }

    @Test
    public void fileCreationFromDirectoryTest()
    {
        this.populateTestData();
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "geojson-diff", String.format("-source=%s", SOURCE_DIRECTORY),
                String.format("-target=%s", TARGET_DIRECTORY),
                String.format("-output=%s", temp.getPath()) };
        new AtlasChecksCommand(args).runWithoutQuitting(args);

        final List<File> outputFiles = temp.listFilesRecursively();
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("additions-\\d+-1.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("changes-\\d+-1.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("subtractions-\\d+-1.geojson")));

        temp.deleteRecursively();
    }

    @Test
    public void testFileCreationFromGZippedFile()
    {
        this.populateTestData();
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "geojson-diff", String.format("-source=%s", sourceFileGZ),
                String.format("-target=%s", targetFileGZ),
                String.format("-output=%s", temp.getPath()) };
        new AtlasChecksCommand(args).runWithoutQuitting(args);

        final List<File> outputFiles = temp.listFilesRecursively();
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("additions-\\d+-1.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("changes-\\d+-0.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("subtractions-\\d+-1.geojson")));

        temp.deleteRecursively();
    }

    @Test
    public void testFileCreationFromGZippedAndUnZippedFiles()
    {
        this.populateTestData();
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "geojson-diff", String.format("-source=%s", sourceFileGZ),
                String.format("-target=%s", targetFile),
                String.format("-output=%s", temp.getPath()) };
        new AtlasChecksCommand(args).runWithoutQuitting(args);

        final List<File> outputFiles = temp.listFilesRecursively();
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("additions-\\d+-1.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("changes-\\d+-0.geojson")));
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
        final String[] args = { "geojson-diff", String.format("-source=%s", GZ_SOURCE_DIRECTORY),
                String.format("-target=%s", GZ_TARGET_DIRECTORY),
                String.format("-output=%s", temp.getPath()) };
        new AtlasChecksCommand(args).runWithoutQuitting(args);

        final List<File> outputFiles = temp.listFilesRecursively();
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("additions-\\d+-1.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("changes-\\d+-1.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("subtractions-\\d+-1.geojson")));

        temp.deleteRecursively();
    }
}
