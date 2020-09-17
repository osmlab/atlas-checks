package org.openstreetmap.atlas.checks.database;

import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(CheckFlag.class, new CheckFlagDeserializer()).create();
    private final String flag1 = CheckFlagTest.class.getResource("checkflags1.log").getPath();
    private final String testDatabaseURI = String.format("localhost/%s", Instant.now().getNano());
    @Mock
    private final DatabaseConnection dbConnection = Mockito.mock(DatabaseConnection.class);
    @Mock
    private final Connection connection = Mockito.mock(Connection.class);
    @Mock
    private final Statement statement = Mockito.mock(Statement.class);
    @Mock
    private final PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    @Mock
    private final PreparedStatement preparedStatement2 = Mockito.mock(PreparedStatement.class);

    @Test
    public void batchFeatureStatementTest() throws IOException, SQLException
    {
        final FlagDatabaseSubCommand command = new FlagDatabaseSubCommand();
        final String flag = this.getResource("checkflags1.log").get(0);
        final CheckFlag checkFlag = gson.fromJson(flag, CheckFlag.class);
        final JsonElement feature = new JsonParser().parse(flag).getAsJsonObject().get("features");

        command.batchFlagFeatureStatement(this.preparedStatement, checkFlag, 1,
                feature.getAsJsonArray().get(0).getAsJsonObject());

        Mockito.verify(this.preparedStatement).addBatch();
    }

    @Test
    public void commandFailedExitCodeTest()
    {
        final String[] arguments = { String.format("--flag_path=%s", this.flag1),
                String.format("--database_url=%s", this.testDatabaseURI) };

        final int command = new FlagDatabaseSubCommand().runSubcommand(arguments);

        Assert.assertEquals(1, command);
    }

    @Test
    public void createDatabaseSchemaTest() throws IOException, SQLException
    {
        final FlagDatabaseSubCommand command = new FlagDatabaseSubCommand();
        Mockito.when(this.dbConnection.getConnection()).thenReturn(this.connection);
        Mockito.when(this.connection.createStatement()).thenReturn(this.statement);
        Mockito.when(this.dbConnection.getSchema()).thenReturn("");
        Mockito.when(this.statement.execute(Mockito.anyString())).thenReturn(true);

        command.createDatabaseSchema(this.connection, this.dbConnection.getSchema());

        // Verifies that createDatabaseSchema statements are called
        Mockito.verify(this.connection).createStatement();
        Mockito.verify(this.dbConnection).getSchema();
        Mockito.verify(this.statement).execute(Mockito.anyString());
    }

    @Test
    public void executeFlagStatementTest() throws IOException, SQLException
    {
        final FlagDatabaseSubCommand command = new FlagDatabaseSubCommand();
        final String flag = this.getResource("checkflags1.log").get(0);
        final CheckFlag checkFlag = gson.fromJson(flag, CheckFlag.class);

        // Run the command with the expectation it will fail, to run the argument parser.
        command.runSubcommand("--flag_path=/bad/path", "--database_url=none");
        // Run executeFlagStatement that depends on arguments being parsed.
        command.executeFlagStatement(this.preparedStatement, checkFlag);

        Mockito.verify(this.preparedStatement).executeUpdate();
    }

    @Test
    public void getOsmIdentifierTest() throws IOException
    {
        final FlagDatabaseSubCommand command = new FlagDatabaseSubCommand();

        final String flag1 = this.getResource("checkflags2.log").get(0);
        final String flag2 = this.getResource("checkflags2.log").get(1);
        final JsonElement flag1feature = new JsonParser().parse(flag1).getAsJsonObject()
                .get("features").getAsJsonArray().get(0);
        final JsonElement flag2feature = new JsonParser().parse(flag2).getAsJsonObject()
                .get("features").getAsJsonArray().get(0);
        final JsonObject flag1featureProperties = flag1feature.getAsJsonObject().get(PROPERTIES)
                .getAsJsonObject();
        final JsonObject flag2featureProperties = flag2feature.getAsJsonObject().get(PROPERTIES)
                .getAsJsonObject();

        final long osmId1 = command.getOsmIdentifier(flag1featureProperties);
        final long osmId2 = command.getOsmIdentifier(flag2featureProperties);

        Assert.assertEquals(221079243, osmId1);
        Assert.assertEquals(167709671, osmId2);
    }

    @Test
    public void processCheckFlagsTest() throws IOException, SQLException
    {
        final FlagDatabaseSubCommand command = new FlagDatabaseSubCommand();
        final List<String> flags = this.getResource("checkflags1.log");

        // Run the command with the expectation it will fail, to run the argument parser.
        command.runSubcommand("--flag_path=/bad/path", "--database_url=none");
        // Run processCheckFlags that depends on arguments being parsed.
        command.processCheckFlags(flags, this.preparedStatement, this.preparedStatement2);

        Mockito.verify(this.preparedStatement, Mockito.times(2)).executeUpdate();
        Mockito.verify(this.preparedStatement2).executeBatch();
    }

    @Test
    public void tagDenylistTest() throws IOException
    {
        final FlagDatabaseSubCommand command = new FlagDatabaseSubCommand();

        final String flag = this.getResource("checkflags2.log").get(0);
        final JsonElement feature = new JsonParser().parse(flag).getAsJsonObject().get("features");
        final JsonObject properties = ((JsonArray) feature).get(0).getAsJsonObject().get(PROPERTIES)
                .getAsJsonObject();
        final Map<String, String> tags = command.getTags(properties);

        Assert.assertEquals("yes", tags.get("building"));
        Assert.assertNull(tags.get("osmIdentifier"));
        Assert.assertNull(tags.get("osmId"));
        Assert.assertNull(tags.get("identifier"));
        Assert.assertNull(tags.get("relations"));
        Assert.assertNull(tags.get("members"));
        Assert.assertNull(tags.get("iso_country_code"));
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
