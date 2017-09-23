package org.openstreetmap.atlas.checks.persistence;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.Resource;

import com.google.common.base.Objects;
import com.google.common.io.Files;

/**
 * Tests for {@link SparkFileHelper}.
 *
 * @author mkalender
 */
public class SparkFileHelperTest
{
    // A helper without any specific configuration
    // Empty configuration targets local filesystem
    private static final SparkFileHelper TEST_HELPER = new SparkFileHelper(Collections.emptyMap());

    @Test(expected = CoreException.class)
    public void testDeleteNonExistingDirectory()
    {
        // Generate a temp path
        final File tempFolder = Files.createTempDir();
        final String path = tempFolder.getAbsolutePath();
        tempFolder.delete();

        // Try to delete directory
        TEST_HELPER.deleteDirectory(path);
    }

    @Test
    public void testIsDirectory() throws IOException
    {
        // Test a file
        final File tempFile = File.createTempFile("test", FileSuffix.TEMPORARY.toString());
        tempFile.deleteOnExit();
        Assert.assertFalse(TEST_HELPER.isDirectory(tempFile.getAbsolutePath()));

        // Test a folder
        final File tempFolder = Files.createTempDir();
        tempFolder.deleteOnExit();
        Assert.assertTrue(TEST_HELPER.isDirectory(tempFolder.getAbsolutePath()));
    }

    @Test
    public void testList() throws IOException
    {
        // Start with an empty folder
        final File tempFolder = Files.createTempDir();
        tempFolder.deleteOnExit();
        Assert.assertTrue(TEST_HELPER.list(tempFolder.getAbsolutePath()).isEmpty());

        // Add a file and then delete
        final File tempFile1 = File.createTempFile("test1", FileSuffix.TEMPORARY.toString(),
                tempFolder);
        Assert.assertEquals(1, TEST_HELPER.list(tempFolder.getAbsolutePath()).size());
        tempFile1.delete();
        Assert.assertTrue(TEST_HELPER.list(tempFolder.getAbsolutePath()).isEmpty());

        // Generate random number of files
        final int randomFileCount = new Random().nextInt(50) + 1;
        final File[] randomFiles = new File[randomFileCount];
        for (int index = 0; index < randomFileCount; index++)
        {
            randomFiles[index] = File.createTempFile("test" + index,
                    FileSuffix.TEMPORARY.toString(), tempFolder);
            Assert.assertEquals(index + 1, TEST_HELPER.list(tempFolder.getAbsolutePath()).size());
        }

        Assert.assertEquals(randomFileCount, TEST_HELPER.list(tempFolder.getAbsolutePath()).size());

        // Go over and make sure files are there
        final List<Resource> files = TEST_HELPER.list(tempFolder.getAbsolutePath());
        for (final File randomFile : randomFiles)
        {
            Assert.assertTrue(
                    files.stream().anyMatch(resource -> Objects.equal(randomFile.getPath(),
                            SparkFileHelper.combine(tempFolder.getPath(), resource.getName()))));
        }
    }

    @Test
    public void testMkdirAndDeleteDirectory()
    {
        // Generate a temp path
        final File tempFolder = Files.createTempDir();
        final String path = tempFolder.getAbsolutePath();
        tempFolder.delete();

        // Make sure folder doesn't exist yet
        Assert.assertFalse(TEST_HELPER.isDirectory(path));
        Assert.assertFalse(new File(path).exists());

        // Create directory
        TEST_HELPER.mkdir(path);
        Assert.assertTrue(TEST_HELPER.isDirectory(path));
        Assert.assertTrue(new File(path).exists());

        // Delete directory
        TEST_HELPER.deleteDirectory(path);
        Assert.assertFalse(TEST_HELPER.isDirectory(path));
        Assert.assertFalse(new File(path).exists());
    }

    @Test
    public void testRename() throws IOException
    {
        // Create temporary files
        // Delete one and rename one to another
        final File tempFile = File.createTempFile("test", FileSuffix.TEMPORARY.toString());
        tempFile.deleteOnExit();
        Assert.assertTrue(tempFile.exists());

        final File tempFile2 = File.createTempFile("test-another", FileSuffix.TEMPORARY.toString());
        tempFile2.delete();
        Assert.assertFalse(tempFile2.exists());

        // Rename test to test-another
        TEST_HELPER.rename(tempFile.getAbsolutePath(), tempFile2.getAbsolutePath());
        Assert.assertFalse(tempFile.exists());
        Assert.assertTrue(tempFile2.exists());
    }
}
