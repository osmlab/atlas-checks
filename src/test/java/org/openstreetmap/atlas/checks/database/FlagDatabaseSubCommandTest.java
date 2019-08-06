package org.openstreetmap.atlas.checks.database;

import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.CheckFlagTest;
import org.openstreetmap.atlas.checks.flag.serializer.CheckFlagDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests for FlagDatabaseSubCommand
 *
 * @author danielbaah
 */
public class FlagDatabaseSubCommandTest
{
    private String flag1 = CheckFlagTest.class.getResource("checkflags1.log").getPath();
    private String testDatabaseURI = String.format("localhost/%s", Instant.now().getNano());
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(CheckFlag.class, new CheckFlagDeserializer()).create();

    @Mock
    private DatabaseConnection dbConnection = Mockito.mock(DatabaseConnection.class);

    @Test
    public void commandFailedExitCodeTest()
    {
        final String[] arguments = { String.format("--flag_path=%s", this.flag1),
                String.format("--database_url=%s", this.testDatabaseURI) };

        final int command = new FlagDatabaseSubCommand().runSubcommand(arguments);

        Assert.assertEquals(1, command);
    }

    @Test
    public void tagBlacklistTest() throws IOException
    {
        final FlagDatabaseSubCommand command = new FlagDatabaseSubCommand();

        final String flag = this.getResource("checkflags2.log").get(0);
        final JsonElement feature = new JsonParser().parse(flag).getAsJsonObject().get("features");
        final JsonObject properties = ((JsonArray) feature).get(0).getAsJsonObject().get(PROPERTIES)
                .getAsJsonObject();
        final Map<String, String> tags = command.getTags(properties);

        Assert.assertEquals("yes", tags.get("building"));
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
