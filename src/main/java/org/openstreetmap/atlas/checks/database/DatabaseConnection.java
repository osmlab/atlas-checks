package org.openstreetmap.atlas.checks.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.openstreetmap.atlas.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public class DatabaseConnection
{

    private Connection databaseConnection;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    /**
     * Default constructor takes in a url of the form host[:port]/database. Port and additional
     * query params are optional; i.e, localhost/database would default to port 5432. Also,
     * additional query parameters can be appended to the database name; i.e.
     * localhost/database?user=postgres&currentSchema=public&password=private.
     * 
     * @see <a href="https://jdbc.postgresql.org/documentation/head/connect.html">JDBC connection
     *      parameters</a>
     * @param connectionUrl
     *            a database url
     */
    public DatabaseConnection(String connectionUrl)
    {
        try
        {
            Class.forName("org.postgresql.Driver");
            URI connectionURI = this.createConnectionURI(connectionUrl);

            this.databaseConnection = DriverManager
                    .getConnection(String.format("jdbc:%s", connectionURI.toString()));
            this.createDatabaseSchema();
        }
        catch (ClassNotFoundException error)
        {
            logger.error("Postgres Driver class not found", error);
        }
        catch (SQLException error)
        {
            throw new CoreException("Invalid connection string. host[:port]/database", error);
        }
    }

    private URI createConnectionURI(String connectionString)
    {
        return URI.create(String.format("postgresql://%s", connectionString));
    }

    public Connection getDatabaseConnection()
    {
        return databaseConnection;
    }

    private boolean createDatabaseSchema()
    {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(DatabaseConnection.class.getResourceAsStream("schema.sql")));
        final LineNumberReader lnReader = new LineNumberReader(reader);
        try
        {
            String query = ScriptUtils
                    .readScript(lnReader, ScriptUtils.DEFAULT_COMMENT_PREFIX,
                            ScriptUtils.DEFAULT_STATEMENT_SEPARATOR)
                    .replace("{schema}", databaseConnection.getSchema());

            this.databaseConnection.createStatement().execute(query);
            logger.info("Successfully created database schema.");

            return true;
        }
        catch (final IOException e)
        {
            logger.error("Error thrown reading schema.sql", e);
            return false;
        }
        catch (final SQLException error)
        {
            throw new CoreException("Error executing create schema script.", error);
        }
    }
}
