package org.openstreetmap.atlas.checks.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test simple DatabaseConnection class
 *
 * @author danielbaah
 */
public class DatabaseConnectionTest
{

    @Mock
    private DatabaseConnection dbConnection = Mockito.mock(DatabaseConnection.class);
    @Mock
    private Connection mockConnection = Mockito.mock(Connection.class);

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionTest.class);

    @Test
    public void closeConnectionTest() throws SQLException
    {
        this.dbConnection.close();
        Mockito.verify(this.dbConnection).close();
    }

    @After
    public void closeConnections() throws SQLException
    {
        this.dbConnection.close();
        this.mockConnection.close();
    }

    @Test
    public void createConnectionTest()
    {
        try
        {
            Mockito.when(this.dbConnection.getConnection()).thenReturn(this.mockConnection);
            Assert.assertNotNull(this.dbConnection.getConnection());
        }
        catch (final SQLException error)
        {
            logger.info("Error mocking getConnection().", error);
        }
    }

    @Test
    public void getDefaultSchemaTest()
    {
        final DatabaseConnection databaseConnection = new DatabaseConnection("localhost/testdb");

        Assert.assertEquals("public", databaseConnection.getSchema());
    }

    @Test
    public void getEmptyQueryParamTest()
    {
        final DatabaseConnection databaseConnection = new DatabaseConnection("localhost/testdb");
        final Map<String, String> queryParameters = databaseConnection.getQueryParameters();

        Assert.assertEquals(0, queryParameters.size());
    }

    @Test
    public void getQueryParamTest()
    {
        final DatabaseConnection databaseConnection = new DatabaseConnection(
                "localhost/testdb?user=testuser&password=funnypassword");
        final Map<String, String> queryParameters = databaseConnection.getQueryParameters();

        Assert.assertEquals("testuser", queryParameters.get("user"));
        Assert.assertEquals("funnypassword", queryParameters.get("password"));
    }

    @Test
    public void getSchemaTest()
    {
        final DatabaseConnection databaseConnection = new DatabaseConnection(
                "localhost/testdb?currentSchema=testschema");

        Assert.assertEquals("testschema", databaseConnection.getSchema());
    }

    @Test
    public void schemaConnectionStringParserTest()
    {
        final DatabaseConnection databaseConnection = new DatabaseConnection(
                "localhost/testdb?currentSchema=public");

        Assert.assertEquals("postgresql://localhost/testdb?currentSchema=public",
                databaseConnection.getConnectionURI().toString());
    }

    @Test
    public void simpleConnectionStringParserTest()
    {
        final DatabaseConnection databaseConnection = new DatabaseConnection("localhost/testdb");

        Assert.assertEquals("postgresql://localhost/testdb",
                databaseConnection.getConnectionURI().toString());
    }
}
