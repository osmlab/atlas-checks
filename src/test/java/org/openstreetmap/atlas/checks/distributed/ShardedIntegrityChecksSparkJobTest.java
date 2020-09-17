package org.openstreetmap.atlas.checks.distributed;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * Unit tests for {@link ShardedIntegrityChecksSparkJob}. Runs the spark job with test inputs and
 * validates the output files.
 *
 * @author bbreithaupt
 */
public class ShardedIntegrityChecksSparkJobTest
{
    private static final String COUNTRY_CODE = "UNK";
    private static final int ZOOM_LEVEL = 4;
    private static final File INPUT = File.temporaryFolder();
    private static final File OUTPUT = File.temporaryFolder();

    @Rule
    public ShardedIntegrityChecksSparkJobTestRule setup = new ShardedIntegrityChecksSparkJobTestRule();

    @AfterClass
    public static void cleanUp()
    {
        INPUT.deleteRecursively();
        OUTPUT.deleteRecursively();
    }

    @Test
    public void countFlagsTest() throws FileNotFoundException, IOException
    {
        generateData();

        Assert.assertTrue(OUTPUT.child("flag").child(COUNTRY_CODE).exists());
        final Set<File> flagFiles = OUTPUT.child("flag").child(COUNTRY_CODE).listFilesRecursively()
                .stream().filter(file -> file.getName().startsWith(COUNTRY_CODE))
                .collect(Collectors.toSet());
        for (final File file : flagFiles)
        {
            Assert.assertEquals(2, new BufferedReader(
                    new InputStreamReader(new FileInputStream(file.getAbsolutePathString())))
                            .lines().count());
        }
    }

    @Test
    public void countGeojsonTest()
    {
        generateData();

        Assert.assertTrue(OUTPUT.child("geojson").child(COUNTRY_CODE).exists());
        Assert.assertTrue(OUTPUT.child("geojson").child(COUNTRY_CODE).listFilesRecursively()
                .stream().anyMatch(file -> file.getName().startsWith("EdgesTestCheck")));
    }

    @Test
    public void countMetricsTest()
    {
        generateData();

        Assert.assertTrue(OUTPUT.child("metric").child(COUNTRY_CODE).exists());
        Assert.assertEquals(2, OUTPUT.child("metric").child(COUNTRY_CODE).listFilesRecursively()
                .stream().filter(file -> file.getName().endsWith(".csv")).count());
    }

    @Test
    public void tippecanoeTest()
    {
        generateData();

        Assert.assertTrue(OUTPUT.child("tippecanoe").child(COUNTRY_CODE).exists());
    }

    /**
     * Generate test atlas files with the correct sharding zoom level.
     */
    private void generateAtlases()
    {
        final File countryFolder = new File(
                FilenameUtils.concat(INPUT.getAbsolutePathString(), COUNTRY_CODE));
        countryFolder.mkdirs();

        final SlippyTileSharding sharding = new SlippyTileSharding(ZOOM_LEVEL);

        this.setup.bcAtlas()
                .save(new File(FilenameUtils.concat(countryFolder.getAbsolutePathString(),
                        String.format("UNK_%s.atlas", sharding.shards(this.setup.bcAtlas().bounds())
                                .iterator().next().getName()))));
        this.setup.nzAtlas()
                .save(new File(FilenameUtils.concat(countryFolder.getAbsolutePathString(),
                        String.format("UNK_%s.atlas", sharding.shards(this.setup.nzAtlas().bounds())
                                .iterator().next().getName()))));
    }

    /**
     * Generate test data and populate the input folder.
     */
    private void generateData()
    {
        if (OUTPUT.listFilesRecursively().isEmpty())
        {
            this.generateAtlases();
            this.runShardedIntegrityChecksSparkJob();
        }
    }

    /**
     * Run {@link ShardedIntegrityChecksSparkJob} with test inputs.
     */
    private void runShardedIntegrityChecksSparkJob()
    {
        final String[] arguments = {
                String.format("-inputFolder=%s", INPUT.getAbsolutePathString()),
                String.format("-startedFolder=%s", INPUT.getAbsolutePathString()),
                String.format("-output=%s", OUTPUT.getAbsolutePathString()),
                String.format("-sharding=slippy@%s", ZOOM_LEVEL), "-maxShardLoad=1",
                "-outputFormats=flags,geojson,metrics,tippecanoe", "-compressOutput=false",
                String.format("-countries=%s", COUNTRY_CODE), "-saveCheckOutput=true",
                "-cluster=local",
                String.format("-configFiles=file:%s",
                        ShardedIntegrityChecksSparkJobTest.class
                                .getResource("test_configuration.json").getPath()),
                "-sparkOptions=spark.executor.memory->4g,spark.driver.memory->16g,spark.rdd.compress->true" };

        new ShardedIntegrityChecksSparkJob().runWithoutQuitting(arguments);
    }
}
