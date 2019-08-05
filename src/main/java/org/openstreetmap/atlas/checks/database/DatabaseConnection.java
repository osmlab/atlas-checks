package org.openstreetmap.atlas.checks.database;

import java.io.Closeable;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a PostgreSQL database connection
 *
 * @author danielbaah
 */
public class DatabaseConnection implements Closeable
{

    private URI connectionURI;
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
        this.connectionURI = this.createConnectionURI(connectionUrl);
    }

    @Override
    public void close()
    {
        try
        {
            this.getConnection().close();
        }
        catch (final SQLException error)
        {
            logger.error("Error closing jdbc connection.", error);
        }
    }

    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(String.format("jdbc:%s", this.connectionURI.toString()));

    }

    private URI createConnectionURI(final String connectionString)
    {
        return URI.create(String.format("postgresql://%s", connectionString));
    }
}
