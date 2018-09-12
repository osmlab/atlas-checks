package org.openstreetmap.atlas.checks.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * Unit tests for {@link AtlasChecksGeoJSONDiffSubCommand}.
 *
 * @author bbreithaupt
 */
public class AtlasChecksGeoJSONDiffSubCommandTest
{
    private static final File SOURCE_DIRECTORY = File.temporaryFolder();
    private static final File TARGET_DIRECTORY = File.temporaryFolder();
    private static final String GEO_11 = AtlasChecksGeoJSONDiffSubCommandTest.class
            .getResource("geo_11.geojson").getPath();
    private static final String GEO_21 = AtlasChecksGeoJSONDiffSubCommandTest.class
            .getResource("geo_21.geojson").getPath();

    @BeforeClass
    public static void copyLogFiles() throws IOException
    {
        Files.copy(Paths.get(
                AtlasChecksGeoJSONDiffSubCommandTest.class.getResource("geo_11.geojson").getPath()),
                Paths.get(SOURCE_DIRECTORY.getPath() + "/geo_11.geojson"));
        Files.copy(Paths.get(
                AtlasChecksGeoJSONDiffSubCommandTest.class.getResource("geo_12.geojson").getPath()),
                Paths.get(SOURCE_DIRECTORY.getPath() + "/geo_12.geojson"));
        Files.copy(Paths.get(
                AtlasChecksGeoJSONDiffSubCommandTest.class.getResource("geo_21.geojson").getPath()),
                Paths.get(TARGET_DIRECTORY.getPath() + "/geo_21.geojson"));
        Files.copy(Paths.get(
                AtlasChecksGeoJSONDiffSubCommandTest.class.getResource("geo_22.geojson").getPath()),
                Paths.get(TARGET_DIRECTORY.getPath() + "/geo_22.geojson"));
    }

    @AfterClass
    public static void deleteLogFiles()
    {
        SOURCE_DIRECTORY.deleteRecursively();
        TARGET_DIRECTORY.deleteRecursively();
    }

    @Test
    public void fileCreationFromFileTest()
    {
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "geoJSON-diff", String.format("-source=%s", GEO_11),
                String.format("-target=%s", GEO_21), String.format("-output=%s", temp.getPath()) };
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
    public void fileCreationFromDirectoryTest()
    {
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "geoJSON-diff", String.format("-source=%s", SOURCE_DIRECTORY),
                String.format("-target=%s", TARGET_DIRECTORY),
                String.format("-output=%s", temp.getPath()) };
        new AtlasChecksCommand(args).runWithoutQuitting(args);

        final List<File> outputFiles = temp.listFilesRecursively();
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("additions-\\d+-1.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("changes-\\d+-2.geojson")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("subtractions-\\d+-3.geojson")));

        temp.deleteRecursively();
    }
}
