package org.openstreetmap.atlas.checks.commands;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * Unit tests for {@link AtlasChecksGeoJSONDiffSubCommand}.
 *
 * @author bbreithaupt
 */
public class AtlasChecksGeoJSONDiffSubCommandTest
{
    private static final String GEO_11 = AtlasChecksGeoJSONDiffSubCommandTest.class
            .getResource("source/geo_11.geojson").getPath();
    private static final String GEO_21 = AtlasChecksGeoJSONDiffSubCommandTest.class
            .getResource("target/geo_21.geojson").getPath();
    private static final String SOURCE_DIRECTORY = AtlasChecksGeoJSONDiffSubCommandTest.class
            .getResource("source").getPath();
    private static final String TARGET_DIRECTORY = AtlasChecksGeoJSONDiffSubCommandTest.class
            .getResource("target").getPath();

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
