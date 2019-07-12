package org.openstreetmap.atlas.checks.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.openstreetmap.atlas.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.init.ScriptUtils;

public class DatabaseConnection
{

    private static String DEFAULT_DB_SCHEMA = "public";
    private static String DEFAULT_DB_USER = "postgres";
    private static String DEFAULT_DB_PASSWORD = "";
    private Connection databaseConnection;
    private String schema;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    public DatabaseConnection(String databaseConnectionUrl)
    {
        try
        {
            Class.forName("org.postgresql.Driver");
            this.databaseConnection = this.createConnectionUrl(databaseConnectionUrl);
            this.createDatabaseSchema();
        }
        catch (ClassNotFoundException error)
        {
            logger.error("Postgres Driver class not found", error);
        }
    }

    private Connection createConnectionUrl(String connectionString)
    {

        final String[] values = connectionString.split(":");
        final int minimumSize = 3;
        if (values.length < minimumSize)
        {
            throw new CoreException("DB connection url must have at least host:port:database");
        }

        this.schema = values.length > minimumSize ? values[3] : DEFAULT_DB_SCHEMA;
        Properties properties = new Properties();
        properties.setProperty("host", values[0]);
        properties.setProperty("port", values[1]);
        properties.setProperty("database", values[2]);

        properties.setProperty("currentSchema", this.schema);
        properties.setProperty("password",
                values.length > minimumSize ? values[4] : DEFAULT_DB_PASSWORD);

        try
        {
            return DriverManager
                    .getConnection(
                            String.format("jdbc:postgresql://%s:%s/%s", properties.get("host"),
                                    properties.get("port"), properties.get("database")),
                            properties);
        }
        catch (SQLException error)
        {
            throw new CoreException("Invalid Connection String", error);
        }

    }

    public Connection getDatabaseConnection()
    {
        return databaseConnection;
    }

    public String getSchema()
    {
        return schema;
    }
    
    private boolean createDatabaseSchema()
    {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(DatabaseConnection.class.getResourceAsStream("schema.sql")));
        final LineNumberReader lnReader = new LineNumberReader(reader);
        String query;
        try
        {
            query = ScriptUtils.readScript(lnReader, ScriptUtils.DEFAULT_COMMENT_PREFIX,
                    ScriptUtils.DEFAULT_STATEMENT_SEPARATOR);
            query = query.replace("{schema}", databaseConnection.getSchema().replace("-", "_"));
            
            Statement statement = this.databaseConnection.createStatement();
            statement.execute(query);
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
