package org.openstreetmap.atlas.checks.database;

import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.FEATURES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.serializer.CheckFlagDeserializer;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String LOG_EXTENSION = "log";
    private static final String ZIPPED_LOG_EXTENSION = ".log.gz";
    private static final String ISO_COUNTRY_CODE = "iso_country_code";
    private static final String OSM_ID_LEGACY = "osmid";
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static final int SIX = 6;
    private static final int SEVEN = 7;
    private static final int EIGHT = 8;
    private static final int BATCH_SIZE = 1000;

    /**
     * An enum containing the different types of input files that we can handle.
     */
    private enum OutputFileType
    {
        LOG,
        COMPRESSED_LOG
    }

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

    @Override
    @SuppressWarnings("squid:S3655")
    public int execute()
    {
        final Time timer = Time.now();
        final DatabaseConnection database = new DatabaseConnection(
                this.optionAndArgumentDelegate.getOptionArgument(DATABASE_URL_INPUT).get());
        final Connection databaseConnection = database.getConnection();
        final String inputPath = this.optionAndArgumentDelegate.getOptionArgument(FLAG_PATH_INPUT)
                .get();
        this.timestamp = new Timestamp(Instant.now().toEpochMilli());

        new File(inputPath).listFilesRecursively().forEach(file ->
        {
            // If this file is something we handle, read and upload the tasks contained within
            final Optional<OutputFileType> optionalHandledFileType = getOptionalOutputType(file);
            optionalHandledFileType.ifPresent(outputFileType ->
            {

                try (BufferedReader reader = this.getReader(file, outputFileType))
                {

                    final PreparedStatement flagSqlStatement = databaseConnection.prepareStatement(
                            "INSERT INTO flag(flag_id, check_name, instructions, date_created) VALUES (?,?,?,?);");

                    final PreparedStatement featureSqlStatement = databaseConnection
                            .prepareStatement(String.format(
                                    "INSERT INTO feature (flag_id, geom, osm_id, atlas_id, iso_country_code, tags, item_type, date_created) VALUES (?,%s,?,?,?,?);",
                                    "ST_GeomFromGeoJSON(?), ?, ?"));

                    final List<String> lines = reader.lines().collect(Collectors.toList());
                    int startIndex = 0;
                    int endIndex;

                    do
                    {
                        endIndex = Math.min(startIndex + BATCH_SIZE, lines.size());
                        final List<String> batchLines = lines.subList(startIndex, endIndex);

                        batchLines.forEach(line ->
                        {
                            final JsonObject parsedFlag = new JsonParser().parse(line)
                                    .getAsJsonObject();
                            final JsonArray features = this.filterOutPointsFromGeojson(
                                    parsedFlag.get(FEATURES).getAsJsonArray());
                            final CheckFlag flag = gson.fromJson(line, CheckFlag.class);

                            this.batchFlagStatement(flagSqlStatement, flag);

                            StreamSupport.stream(features.spliterator(), false).forEach(
                                    feature -> this.batchFlagFeatureStatement(featureSqlStatement,
                                            flag, feature.getAsJsonObject()));
                        });

                        flagSqlStatement.executeBatch();
                        featureSqlStatement.executeBatch();

                        startIndex = startIndex + BATCH_SIZE;
                    }
                    while (endIndex != lines.size() - 1 && startIndex < lines.size());

                    featureSqlStatement.close();
                    flagSqlStatement.close();

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

        logger.info("Atlas Checks database upload command finished in {}.",
                timer.elapsedSince());

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "flag-database";
    }

    @Override
    public String getSimpleDescription()
    {
        return "Upload Atlas Checks flags into a Postgres database";
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
     * Add CheckFlag feature values to parameterized sql INSERT statement
     *
     * @param sql
     *            PreparedStatement to add parameterized values to
     * @param flag
     *            CheckFlag to associate with feature
     * @param feature
     *            - Flagged AtlasItem
     */
    private void batchFlagFeatureStatement(final PreparedStatement sql, final CheckFlag flag,
            final JsonObject feature)
    {

        final JsonObject properties = feature.get(PROPERTIES).getAsJsonObject();

        try
        {
            sql.setString(1, flag.getIdentifier());
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

    /**
     * Add CheckFlag values to parameterized sql INSERT statement
     *
     * @param sql
     *            - PreparedStatement to add parameterized values to
     * @param flag
     *            - CheckFlag to insert into flag table
     */
    private void batchFlagStatement(final PreparedStatement sql, final CheckFlag flag)
    {
        try
        {
            sql.setString(1, flag.getIdentifier());
            sql.setString(2, flag.getChallengeName().orElse(""));
            sql.setString(THREE, flag.getInstructions().replace("\n", " ").replace("'", "''"));
            sql.setObject(FOUR, this.timestamp);

            sql.addBatch();
        }
        catch (final SQLException error)
        {
            logger.error("Unable to create Flag {} SQL statement", flag.getIdentifier());
        }
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
     * Determine whether or not this file is something we can handle, and classify it accordingly.
     *
     * @param logFile
     *            any file
     * @return if this file is something this command can handle, the appropriate OutputFileType
     *         enum value; otherwise, an empty optional.
     */
    private Optional<OutputFileType> getOptionalOutputType(final File logFile)
    {
        // Note that technically the true extension is just .gz, so we can't use the same method as
        // below.
        if (logFile.getName().endsWith(ZIPPED_LOG_EXTENSION))
        {
            return Optional.of(OutputFileType.COMPRESSED_LOG);
        }
        else if (FilenameUtils.getExtension(logFile.getName()).equals(LOG_EXTENSION))
        {
            return Optional.of(OutputFileType.LOG);
        }
        return Optional.empty();
    }

    /**
     * Returns the OSM identifier for a given JsonObject. Atlas Checks OSM identifier changed from
     * "osmid" to "osmIdentifier"
     * {@link <a href="https://github.com/osmlab/atlas-checks/pull/116/files">here</a>}
     *
     * @param properties
     *            CheckFlag properties
     * @return OSM identifier
     */
    private long getOsmIdentifier(final JsonObject properties)
    {
        return properties.get(OSM_ID_LEGACY) == null ? properties.get("osmIdentifier").getAsLong()
                : properties.get(OSM_ID_LEGACY).getAsLong();
    }

    /**
     * Read a file that we know we should be able to handle
     *
     * @param inputFile
     *            Some file with a valid, appropriate extension.
     * @param fileType
     *            The type of file that inputFile is
     * @return a BufferedReader to read inputFile
     * @throws IOException
     *             if the file is not found or is poorly formatted, given its extension. For
     *             example, if this file is gzipped and something goes wrong in the unzipping
     *             process, it might throw an error
     */
    private BufferedReader getReader(final File inputFile, final OutputFileType fileType)
            throws IOException
    {
        if (fileType == OutputFileType.LOG)
        {
            return new BufferedReader(new FileReader(inputFile.getPath()));
        }
        return new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(inputFile.getPath()))));
    }

    /**
     * Filters non OSM tag in CheckFlag properties and converts into Map object for PostgreSQL
     * hstore
     *
     * @param properties
     *            CheckFlag properties
     * @return hstore Map object
     */
    private Map<String, String> getTags(final JsonObject properties)
    {
        final Map<String, String> hstore = new HashMap<>();

        properties.entrySet().stream().filter(key -> !blacklistKeys.contains(key.getKey()))
                .map(Map.Entry::getKey)
                .forEach(key -> hstore.put(key, properties.get(key).getAsString()));

        return hstore;
    }
}
