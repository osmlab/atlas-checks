package org.openstreetmap.atlas.checks.flag.serializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.CheckFlagTest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests if CheckFlag can be read from json resource
 *
 * @author danielbaah
 */
public class CheckFlagDeserializerTest
{

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(CheckFlag.class, new CheckFlagDeserializer()).create();

    @Test
    public void checkNameDeserializationTest() throws IOException
    {
        final String flag = this.getResource("checkflags2.log").get(0);
        final CheckFlag checkFlag = gson.fromJson(flag, CheckFlag.class);
        final String checkName = checkFlag.getChallengeName().isPresent()
                ? checkFlag.getChallengeName().get()
                : null;
        Assert.assertEquals("BuildingRoadIntersectionCheck", checkName);
    }

    @Test
    public void deserializationTest() throws IOException
    {

        final List<String> flags = this.getResource("checkflags1.log");

        flags.forEach(flag ->
        {
            final CheckFlag checkFlag = gson.fromJson(flag, CheckFlag.class);
            Assert.assertNotNull(checkFlag);
        });
    }

    @Test
    public void instructionsDeserializationTest() throws IOException
    {
        final String flag = this.getResource("checkflags2.log").get(0);
        final CheckFlag checkFlag = gson.fromJson(flag, CheckFlag.class);
        final String instructions = checkFlag.getInstructions();
        Assert.assertNotEquals(0, instructions.length());
    }

    /**
     * @param resource
     *            resource filename
     * @return A List of lines in .log file as string
     * @throws IOException
     *             IOException
     */
    private List<String> getResource(final String resource) throws IOException
    {
        final BufferedReader reader = new BufferedReader(
                new FileReader(CheckFlagTest.class.getResource(resource).getFile()));
        return reader.lines().collect(Collectors.toList());
    }
}
