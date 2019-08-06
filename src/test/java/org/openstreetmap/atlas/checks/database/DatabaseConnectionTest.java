package org.openstreetmap.atlas.checks.database;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
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

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionTest.class);

    @Test
    public void createConnectionTest()
    {
        final DatabaseConnection dbConnection = Mockito.mock(DatabaseConnection.class);
        final Connection connection = Mockito.mock(Connection.class);

        try
        {
            Mockito.when(dbConnection.getConnection()).thenReturn(connection);
        }
        catch (final SQLException error)
        {
            logger.info("Error mocking getConnection().", error);
        }

        Assert.assertNotNull(connection);
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
