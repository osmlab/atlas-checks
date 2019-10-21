package org.openstreetmap.atlas.checks.database;

import static org.openstreetmap.atlas.checks.utility.FileUtility.LogOutputFileType;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.FEATURES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.serializer.CheckFlagDeserializer;
import org.openstreetmap.atlas.checks.utility.FileUtility;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Upload Atlas Checks flags into a Postgres database.
 *
 * @author danielbaah
 */
public class FlagDatabaseSubCommand extends AbstractAtlasShellToolsCommand
{
    private static final String FLAG_PATH_INPUT = "flag_path";
    private static final String DATABASE_URL_INPUT = "database_url";
    private static final String ISO_COUNTRY_CODE = "iso_country_code";
    private static final String OSM_ID_LEGACY = "osmid";
    private static final String CREATE_FLAG_SQL = "INSERT INTO flag(flag_id, check_name, instructions, date_created) VALUES (?,?,?,?);";
    private static final String CREATE_FEATURE_SQL = String.format(
            "INSERT INTO feature (flag_id, geom, osm_id, atlas_id, iso_country_code, tags, item_type, date_created) VALUES (?,%s,?,?,?,?);",
            "ST_GeomFromGeoJSON(?), ?, ?");
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int SEVEN = 7;
    private static final int EIGHT = 8;
    private static final int BATCH_SIZE = 1000;

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(CheckFlag.class, new CheckFlagDeserializer()).create();
    private static final Logger logger = LoggerFactory.getLogger(FlagDatabaseSubCommand.class);
    private static final Set<String> blacklistKeys = new HashSet<>();

    static
    {
        blacklistKeys.add("itemType");
        blacklistKeys.add("identifier");
        blacklistKeys.add(OSM_ID_LEGACY);
        blacklistKeys.add("osmIdentifier");
        blacklistKeys.add("relations");
        blacklistKeys.add("members");
        blacklistKeys.add(ISO_COUNTRY_CODE);
    }

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private Timestamp timestamp;

    public static void main(final String[] args)
    {
        new FlagDatabaseSubCommand().runSubcommandAndExit(args);
    }

    public FlagDatabaseSubCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
    }

    /**
     * Add CheckFlag feature values to parameterized sql INSERT statement
     *
     * @param sql
     *            PreparedStatement to add parameterized values to
     * @param flag
     *            CheckFlag to associate with feature
     * @param flagIdentifier
     *            Flag table record unique identifier
     * @param feature
     *            - Flagged AtlasItem
     */
    public void batchFlagFeatureStatement(final PreparedStatement sql, final CheckFlag flag,
            final int flagIdentifier, final JsonObject feature)
    {

        final JsonObject properties = feature.get(PROPERTIES).getAsJsonObject();

        try
        {
            sql.setInt(1, flagIdentifier);
            sql.setString(2, feature.get("geometry").toString());
            sql.setLong(THREE, this.getOsmIdentifier(properties));
            sql.setLong(FOUR, properties.get("identifier").getAsLong());
            sql.setString(FIVE,
                    properties.has(ISO_COUNTRY_CODE)
                            ? properties.get(ISO_COUNTRY_CODE).getAsString()
                            : "NA");
            sql.setObject(SIX, this.getTags(properties));
            sql.setString(SEVEN, properties.get("itemType").getAsString());
            sql.setObject(EIGHT, this.timestamp);

            sql.addBatch();
        }
        catch (final SQLException error)
        {
            logger.error("Unable to create Flag {} SQL statement", flag.getIdentifier());
        }

    }

    /***
     * Create database schema from schema.sql resource file.
     *
     * @param connection
     *            jdbc Connection object
     */
    public void createDatabaseSchema(final Connection connection)
    {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(DatabaseConnection.class.getResourceAsStream("schema.sql")));
        final LineNumberReader lnReader = new LineNumberReader(reader);
        try (Statement sql = connection.createStatement())
        {
            final String query = ScriptUtils
                    .readScript(lnReader, ScriptUtils.DEFAULT_COMMENT_PREFIX,
                            ScriptUtils.DEFAULT_STATEMENT_SEPARATOR)
                    .replace("{schema}", connection.getSchema());

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

    @Override
    @SuppressWarnings("squid:S3655")
    public int execute()
    {
        final Time timer = Time.now();
        try (DatabaseConnection database = new DatabaseConnection(
                this.optionAndArgumentDelegate.getOptionArgument(DATABASE_URL_INPUT).get());
                Connection databaseConnection = database.getConnection())
        {
            final String inputPath = this.optionAndArgumentDelegate
                    .getOptionArgument(FLAG_PATH_INPUT).get();
            this.timestamp = new Timestamp(Instant.now().toEpochMilli());
            this.createDatabaseSchema(databaseConnection);

            new File(inputPath).listFilesRecursively().forEach(file ->
            {
                // If this file is something we handle, read and upload the tasks contained within
                final Optional<LogOutputFileType> optionalHandledFileType = FileUtility
                        .getOptionalLogOutputType(file);
                optionalHandledFileType.ifPresent(logOutputFileType ->
                {

                    try (BufferedReader reader = FileUtility.getReader(file, logOutputFileType);
                            PreparedStatement flagSqlStatement = databaseConnection
                                    .prepareStatement(CREATE_FLAG_SQL,
                                            Statement.RETURN_GENERATED_KEYS);
                            PreparedStatement featureSqlStatement = databaseConnection
                                    .prepareStatement(CREATE_FEATURE_SQL))
                    {
                        final List<String> lines = reader.lines().collect(Collectors.toList());
                        this.processCheckFlags(lines, flagSqlStatement, featureSqlStatement);
                    }
                    catch (final IOException error)
                    {
                        logger.error("Exception while reading {}:", file, error);
                    }
                    catch (final SQLException error)
                    {
                        logger.error("Exception batch executing flag statements", error);
                    }
                });
            });
        }
        catch (final SQLException error)
        {
            logger.error("Invalid connection string. host[:port]/database", error);
            return 1;
        }

        logger.info("Atlas Checks database upload command finished in {}.", timer.elapsedSince());

        return 0;
    }

    /**
     * Add CheckFlag values to parameterized sql INSERT statement
     *
     * @param sql
     *            - PreparedStatement to add parameterized values to
     * @param flag
     *            - CheckFlag to insert into flag table
     */
    public void executeFlagStatement(final PreparedStatement sql, final CheckFlag flag)
    {
        try
        {
            sql.setString(1, flag.getIdentifier());
            sql.setString(2, flag.getChallengeName().orElse(""));
            sql.setString(THREE, flag.getInstructions().replace("\n", " ").replace("'", "''"));
            sql.setObject(FOUR, this.timestamp);

            sql.executeUpdate();
        }
        catch (final SQLException error)
        {
            logger.error("Unable to create Flag {} SQL statement", flag.getIdentifier(), error);
        }
    }

    @Override
    public String getCommandName()
    {
        return "flag-database";
    }

    /**
     * Returns the OSM identifier for a given JsonObject. Atlas Checks OSM identifier changed from
     * "osmid" to "osmIdentifier"
     *
     * @see <a href="https://github.com/osmlab/atlas-checks/pull/116/files">here</a>
     * @param properties
     *            CheckFlag properties
     * @return OSM identifier
     */
    public long getOsmIdentifier(final JsonObject properties)
    {
        return properties.get(OSM_ID_LEGACY) == null ? properties.get("osmIdentifier").getAsLong()
                : properties.get(OSM_ID_LEGACY).getAsLong();
    }

    @Override
    public String getSimpleDescription()
    {
        return "Upload Atlas Checks flags into a Postgres database";
    }

    /**
     * Filters non OSM tag in CheckFlag properties and converts into Map object for PostgreSQL
     * hstore
     *
     * @param properties
     *            CheckFlag properties
     * @return hstore Map object
     */
    public Map<String, String> getTags(final JsonObject properties)
    {
        final Map<String, String> hstore = new HashMap<>();

        properties.entrySet().stream().filter(key -> !blacklistKeys.contains(key.getKey()))
                .map(Map.Entry::getKey)
                .forEach(key -> hstore.put(key, properties.get(key).getAsString()));

        return hstore;
    }

    @Override
    public void registerManualPageSections()
    {
        this.addManualPageSection("DESCRIPTION", FlagDatabaseSubCommand.class
                .getResourceAsStream("FlagDatabaseSubCommandDescriptionSection.txt"));
        this.addManualPageSection("EXAMPLES", FlagDatabaseSubCommand.class
                .getResourceAsStream("FlagDatabaseSubCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        this.registerOptionWithRequiredArgument(FLAG_PATH_INPUT, 'f',
                "A directory of folders containing atlas-checks log files.",
                OptionOptionality.REQUIRED, FLAG_PATH_INPUT);
        this.registerOptionWithRequiredArgument(DATABASE_URL_INPUT, 't',
                "Database connection string", OptionOptionality.REQUIRED, DATABASE_URL_INPUT);
        super.registerOptionsAndArguments();
    }

    /**
     * Get all geojson features which do contain a properties field from a {@link JsonArray}.
     *
     * @param features
     *            a {@link JsonArray} of geojson features
     * @return a JsonArray containing all Check flag features
     */
    private JsonArray filterOutPointsFromGeojson(final JsonArray features)
    {
        return StreamSupport.stream(features.spliterator(), false).map(JsonElement::getAsJsonObject)
                .filter(feature -> feature.has(PROPERTIES)
                        && !feature.get(PROPERTIES).getAsJsonObject().entrySet().isEmpty())
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

    /**
     * This function handles parsing each CheckFlag, and batching flag features into the database
     * 
     * @param lines
     *            a List of stringified CheckFlags read in from line-delimited json
     * @param flagSqlStatement
     *            Flag PreparedStatement
     * @param featureSqlStatement
     *            Feature PreparedStatement
     */
    public void processCheckFlags(final List<String> lines,
            final PreparedStatement flagSqlStatement, final PreparedStatement featureSqlStatement)
    {
        int counter = 0;
        try
        {
            for (final String line : lines)
            {
                final JsonObject parsedFlag = new JsonParser().parse(line).getAsJsonObject();
                final JsonArray features = this
                        .filterOutPointsFromGeojson(parsedFlag.get(FEATURES).getAsJsonArray());
                final CheckFlag flag = gson.fromJson(line, CheckFlag.class);
                final int flagRecordId;

                // First check if the number of features in our batch is less than the maximum
                if (counter + features.size() > BATCH_SIZE)
                {
                    featureSqlStatement.executeBatch();
                    logger.debug("Batching {} features.", counter);
                    counter = 0;
                }

                // Add flag record to database
                this.executeFlagStatement(flagSqlStatement, flag);

                try (ResultSet resultSet = flagSqlStatement.getGeneratedKeys())
                {
                    if (resultSet != null && resultSet.next())
                    {
                        // Save flag record unique id to use for feature record
                        flagRecordId = resultSet.getInt(1);

                        StreamSupport.stream(features.spliterator(), false)
                                .forEach(feature -> this.batchFlagFeatureStatement(
                                        featureSqlStatement, flag, flagRecordId,
                                        feature.getAsJsonObject()));

                        counter += features.size();
                    }
                }
            }
            // Execute the remaining features
            featureSqlStatement.executeBatch();
            logger.debug("Batching the remaining {} features.", counter);
        }
        catch (final SQLException failure)
        {
            logger.error("Error creating Flag record.", failure);
        }
    }
}
