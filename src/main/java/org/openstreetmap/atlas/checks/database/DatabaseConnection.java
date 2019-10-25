package org.openstreetmap.atlas.checks.database;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Create a PostgreSQL database connection
 *
 * @author danielbaah
 */
public class DatabaseConnection implements AutoCloseable
{

    private URI connectionURI;
    private Map<String, String> queryParameters = new HashMap<>();
    private static final String DEFAULT_DATABASE_SCHEMA = "public";

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
        this.queryParameters = this.uriQueryToMap(this.connectionURI);
    }

    @Override
    public void close() throws SQLException
    {
        this.getConnection().close();
    }

    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(String.format("jdbc:%s", this.connectionURI.toString()));
    }

    public Map<String, String> getQueryParameters()
    {
        return this.queryParameters;
    }

    public String getSchema()
    {
        return this.uriQueryToMap(this.connectionURI).getOrDefault("currentSchema",
                DEFAULT_DATABASE_SCHEMA);
    }

    public URI getConnectionURI()
    {
        return this.connectionURI;
    }

    private URI createConnectionURI(final String connectionString)
    {
        return URI.create(String.format("postgresql://%s", connectionString));
    }

    private Map<String, String> uriQueryToMap(final URI connectionURI)
    {
        final Map<String, String> queryMap = new HashMap<>();

        if (connectionURI.getQuery() != null)
        {
            Arrays.stream(connectionURI.getQuery().split("&")).map(record -> record.split("="))
                    .forEach(value -> queryMap.put(value[0], value[1]));
        }

        return queryMap;
    }
}
