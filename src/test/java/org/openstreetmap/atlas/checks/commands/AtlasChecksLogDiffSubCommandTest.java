package org.openstreetmap.atlas.checks.commands;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * Unit tests for {@link AtlasChecksLogDiffSubCommand}.
 *
 * @author bbreithaupt
 */
public class AtlasChecksLogDiffSubCommandTest
{
    private static final String GEO_11 = AtlasChecksLogDiffSubCommandTest.class
            .getResource("geo_11.log").getPath();
    private static final String GEO_12 = AtlasChecksLogDiffSubCommandTest.class
            .getResource("geo_12.log").getPath();
    private static final String GEO_21 = AtlasChecksLogDiffSubCommandTest.class
            .getResource("geo_21.log").getPath();
    private static final String GEO_22 = AtlasChecksLogDiffSubCommandTest.class
            .getResource("geo_22.log").getPath();

    @Test
    public void fileCreationTest()
    {
        final File temp = File.temporaryFolder();

        // Run AtlasJoinerSubCommand
        final String[] args = { "log-diff", String.format("-source=%s,%s", GEO_11, GEO_12),
                String.format("-target=%s,%s", GEO_21, GEO_22),
                String.format("-output=%s", temp.getPath()) };
        new AtlasChecksCommand(args).runWithoutQuitting(args);

        final List<File> outputFiles = temp.listFilesRecursively();
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("additions-\\d+-1.log")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("changes-\\d+-2.log")));
        Assert.assertTrue(outputFiles.stream()
                .anyMatch(file -> file.getName().matches("subtractions-\\d+-3.log")));

        temp.deleteRecursively();
    }
}
