package org.openstreetmap.atlas.checks.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.openstreetmap.atlas.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.init.ScriptUtils;

/**
 * Connect and create schema for CheckFlag database
 *
 * @author danielbaah
 */
public class DatabaseConnection
{

    private Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    /**
     * Default constructor takes in a url of the form host[:port]/database. Port and additional
     * query params are optional; i.e, localhost/database would default to port 5432. Also,
     * additional query parameters can be appended to the database name; i.e.
     * {@code localhost/database?user=postgres&currentSchema=public&password=private}
     * 
     * @see <a href="https://jdbc.postgresql.org/documentation/head/connect.html">JDBC connection
     *      parameters</a>
     * @param connectionUrl
     *            a database url
     */
    public DatabaseConnection(final String connectionUrl)
    {
        try
        {
            final URI connectionURI = this.createConnectionURI(connectionUrl);

            this.connection = DriverManager
                    .getConnection(String.format("jdbc:%s", connectionURI.toString()));
            this.createDatabaseSchema();
        }
        catch (final SQLException error)
        {
            throw new CoreException("Invalid connection string. host[:port]/database", error);
        }
    }

    public Connection getConnection()
    {
        return this.connection;
    }

    private URI createConnectionURI(final String connectionString)
    {
        return URI.create(String.format("postgresql://%s", connectionString));
    }

    private void createDatabaseSchema()
    {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(DatabaseConnection.class.getResourceAsStream("schema.sql")));
        final LineNumberReader lnReader = new LineNumberReader(reader);
        try (Statement sql = this.connection.createStatement())
        {
            final String query = ScriptUtils
                    .readScript(lnReader, ScriptUtils.DEFAULT_COMMENT_PREFIX,
                            ScriptUtils.DEFAULT_STATEMENT_SEPARATOR)
                    .replace("{schema}", this.connection.getSchema());

            sql.execute(query);
            logger.info("Successfully created database schema.");
        }
        catch (final IOException error)
        {
            throw new CoreException("Error reading schema.sql", error);
        }
        catch (final SQLException error)
        {
            throw new CoreException("Error executing create schema script.", error);
        }
    }
}
