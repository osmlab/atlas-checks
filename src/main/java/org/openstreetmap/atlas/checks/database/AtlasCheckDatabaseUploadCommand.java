package org.openstreetmap.atlas.checks.database;

import java.io.*;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.zookeeper.Op;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.flag.FlagDeserializer;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import com.google.gson.*;

/**
 * Given a directory of log files created by atlas-checks, upload those files to MapRoulette.
 *
 * @author nachtm
 * @author bbreithaupt
 */
public class AtlasCheckDatabaseUploadCommand extends AbstractAtlasShellToolsCommand {
    private String FLAG_PATH_INPUT = "flag_path";
    private String COUNTRIES_INPUT = "countries";
    private String CHECKS_INPUT = "checks";
    private String DATABASE_URL_INPUT = "database_url";

    private static String DEFAULT_DB_SCHEMA = "";
    private static String DEFAULT_DB_USER = "";
    private static String DEFAULT_DB_PASSWORD = "";

    private static final String PARAMETER_CHALLENGE = "challenge";
    private static final String LOG_EXTENSION = "log";
    private static final String ZIPPED_LOG_EXTENSION = ".log.gz";
    private static final Logger logger = LoggerFactory
            .getLogger(AtlasCheckDatabaseUploadCommand.class);
    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    // private static final Switch<DatabaseContext> DB_URL = new Switch<>("db_url",
    // "Database connection url. host:port:database[:schema[:username:password]]",
    // AtlasCheckDatabaseUploadCommand::dbContextConverter, Optionality.OPTIONAL);

    public AtlasCheckDatabaseUploadCommand() {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute() {

        final DatabaseConnection database = new DatabaseConnection(
                this.optionAndArgumentDelegate.getOptionArgument(DATABASE_URL_INPUT).get());
        final Connection databaseConnection = database.getDatabaseConnection();
        final String inputPath = this.optionAndArgumentDelegate.getOptionArgument(FLAG_PATH_INPUT)
                .get();

        this.createDatabaseSchema(database);

        Time timer = Time.now();

        new File(inputPath).listFilesRecursively().parallelStream().forEach(file ->
        {
            // If this file is something we handle, read and upload the tasks contained within
            final Optional<OutputFileType> optionalHandledFileType = getOptionalOutputType(file);
            optionalHandledFileType.ifPresent(outputFileType ->
            {

                try (BufferedReader reader = this.getReader(file, outputFileType)) {

                    reader.lines().forEach(line ->
                    {
                        JsonObject parsedFlag = new JsonParser().parse(line).getAsJsonObject();
                        JsonArray features = this.filterOutPointsFromGeojson(
                                parsedFlag.get("features").getAsJsonArray());

                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(CheckFlag.class, new FlagDeserializer())
                                .create();
                        final CheckFlag flag = gson.fromJson(line, CheckFlag.class);

                        Optional<PreparedStatement> statement = this.executeFlagStatement(databaseConnection, flag);

                        if (statement.isPresent()) {
                            try {
                                statement.get().execute();

                                PreparedStatement sql = databaseConnection.prepareStatement(
                                        String.format("INSERT INTO feature (flag_id, geom, osm_id, atlas_id) VALUES (%s,%s,?,?);",
                                                "(SELECT id FROM flag WHERE flag_id = ? LIMIT 1)",
                                                "ST_GeomFromGeoJSON(?)"
                                                ));

                                StreamSupport.stream(features.spliterator(), false)
                                        .forEach(feature -> this.executeFlagFeatureStatement(sql, flag, feature.getAsJsonObject()));

                                sql.executeBatch();

                            } catch (SQLException error) {
                                logger.error("Error creating flag {}", flag.getIdentifier(),error);
                            }
                        }


                    });

                } catch (final IOException error) {
                    logger.error("Exception while reading {}:", file, error);
                }
            });
        });

        logger.info("Atlas Checks database upload command finished in {} seconds.", timer.elapsedSince().asSeconds());

        return 0;
    }

    @Override
    public String getCommandName() {
        return "flag-database";
    }

    @Override
    public String getSimpleDescription() {
        return "Upload Atlas Checks flags into a Postgres database";
    }

    @Override
    public void registerManualPageSections() {
        this.addManualPageSection("DESCRIPTION", AtlasCheckDatabaseUploadCommand.class
                .getResourceAsStream("FlagDatabaseSubCommandDescriptionSection.txt"));
        this.addManualPageSection("EXAMPLES", AtlasCheckDatabaseUploadCommand.class
                .getResourceAsStream("FlagDatabaseSubCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments() {
        this.registerOptionWithRequiredArgument(FLAG_PATH_INPUT, 'f',
                "A directory of folders containing atlas-checks log files.",
                OptionOptionality.REQUIRED, FLAG_PATH_INPUT);
        this.registerOptionWithRequiredArgument(DATABASE_URL_INPUT, 't',
                "Database connection string", OptionOptionality.REQUIRED, DATABASE_URL_INPUT);
        this.registerOptionWithRequiredArgument(COUNTRIES_INPUT, 'i',
                "List of iso3 country codes to filter by", OptionOptionality.OPTIONAL,
                COUNTRIES_INPUT);
        this.registerOptionWithRequiredArgument(CHECKS_INPUT, 'c', "A folder to output results to.",
                OptionOptionality.OPTIONAL, CHECKS_INPUT);
        super.registerOptionsAndArguments();
    }

    /**
     * An enum containing the different types of input files that we can handle.
     */
    private enum OutputFileType {
        LOG,
        COMPRESSED_LOG
    }

    /**
     * Determine whether or not this file is something we can handle, and classify it accordingly.
     *
     * @param logFile any file
     * @return if this file is something this command can handle, the appropriate OutputFileType
     * enum value; otherwise, an empty optional.
     */
    private Optional<OutputFileType> getOptionalOutputType(final File logFile) {
        // Note that technically the true extension is just .gz, so we can't use the same method as
        // below.
        if (logFile.getName().endsWith(ZIPPED_LOG_EXTENSION)) {
            return Optional.of(OutputFileType.COMPRESSED_LOG);
        } else if (FilenameUtils.getExtension(logFile.getName()).equals(LOG_EXTENSION)) {
            return Optional.of(OutputFileType.LOG);
        }
        return Optional.empty();
    }

    /**
     * Read a file that we know we should be able to handle
     *
     * @param inputFile Some file with a valid, appropriate extension.
     * @param fileType  The type of file that inputFile is
     * @return a BufferedReader to read inputFile
     * @throws IOException if the file is not found or is poorly formatted, given its extension. For
     *                     example, if this file is gzipped and something goes wrong in the unzipping
     *                     process, it might throw an error
     */
    private BufferedReader getReader(final File inputFile, final OutputFileType fileType)
            throws IOException {
        if (fileType == OutputFileType.LOG) {
            return new BufferedReader(new FileReader(inputFile.getPath()));
        }
        return new BufferedReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(inputFile.getPath()))));
    }

    public static void main(final String[] args) {
        new AtlasCheckDatabaseUploadCommand().runSubcommandAndExit(args);
    }

    private Optional<PreparedStatement> executeFlagStatement(Connection database, CheckFlag flag) {
        try {
            PreparedStatement sql = database.prepareStatement(
                    "INSERT INTO flag(flag_id, check_name, instructions) VALUES (?,?,?);");
            sql.setString(1, flag.getIdentifier());
            sql.setString(2, flag.getChallengeName().get());
            sql.setString(3, flag.getInstructions().replace("\n", " ").replace("'", "''"));

            return Optional.of(sql);

        } catch (SQLException error) {
            logger.error("Unable to create Flag {} SQL statement", flag.getIdentifier());
        }

        return Optional.empty();
    }

    private void executeFlagFeatureStatement(PreparedStatement sql, CheckFlag flag, JsonObject feature) {

        final JsonObject properties = feature.get("properties").getAsJsonObject();

        try {

            sql.setString(1, flag.getIdentifier());
            sql.setString(2, feature.get("geometry").toString());
            sql.setLong(3, properties.get("osmIdentifier").getAsLong());
            sql.setLong(4, properties.get("identifier").getAsLong());

            sql.addBatch();

        } catch (SQLException error) {
            logger.error("Unable to create Flag {} SQL statement", flag.getIdentifier());
        }

    }

    private JsonArray filterOutPointsFromGeojson(final JsonArray features) {
        return StreamSupport.stream(features.spliterator(), false).map(JsonElement::getAsJsonObject)
                .filter(feature -> feature.has("properties")
                        && !feature.get("properties").getAsJsonObject().entrySet().isEmpty())
                .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

    private boolean createDatabaseSchema(DatabaseConnection databaseConnection) {
        final BufferedReader reader = new BufferedReader(
                new InputStreamReader(DatabaseContext.class.getResourceAsStream("schema.sql")));
        final LineNumberReader lnReader = new LineNumberReader(reader);
        String query;
        try {
            query = ScriptUtils.readScript(lnReader, ScriptUtils.DEFAULT_COMMENT_PREFIX,
                    ScriptUtils.DEFAULT_STATEMENT_SEPARATOR);
            query = query.replace("{schema}", databaseConnection.getSchema().replace("-", "_"));

            try {
                databaseConnection.getDatabaseConnection().createStatement().execute(query);
            } catch (SQLException error) {
                throw new CoreException("Error executing create schema script.", error);
            }

            return true;
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
