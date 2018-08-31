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
            .getResource("geo_11.geojson").getPath();
    private static final String GEO_12 = AtlasChecksGeoJSONDiffSubCommandTest.class
            .getResource("geo_12.geojson").getPath();
    private static final String GEO_21 = AtlasChecksGeoJSONDiffSubCommandTest.class
            .getResource("geo_21.geojson").getPath();
    private static final String GEO_22 = AtlasChecksGeoJSONDiffSubCommandTest.class
            .getResource("geo_22.geojson").getPath();

    @Test
    public void fileCreationTest()
    {
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "geoJSON-diff", String.format("-source=%s,%s", GEO_11, GEO_12),
                String.format("-target=%s,%s", GEO_21, GEO_22),
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
