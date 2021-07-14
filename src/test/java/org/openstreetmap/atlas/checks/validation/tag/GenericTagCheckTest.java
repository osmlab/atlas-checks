package org.openstreetmap.atlas.checks.validation.tag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openstreetmap.atlas.checks.base.ExternalDataFetcher;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.checks.database.wikidata.WikiData;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.SQLiteUtils;
import org.openstreetmap.atlas.checks.validation.verifier.ConsumerBasedExpectedCheckVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for {@link GenericTagCheck}
 *
 * @author Taylor Smock
 */
// Ignore, as all the file separators are in URLs/regex escapes
@SuppressWarnings("HardcodedFileSeparator")
public class GenericTagCheckTest
{
    private static final String JDBC_SQLITE = "jdbc:sqlite:";
    @Rule
    public GenericTagCheckTestRule setup = new GenericTagCheckTestRule();

    @Rule
    public ConsumerBasedExpectedCheckVerifier verifier = new ConsumerBasedExpectedCheckVerifier();

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static Map<String, String> databases;
    private static GenericTagCheck genericTagCheck;

    private static final String TAGINFO = "taginfo";
    private static final String WIKI_DATA = "wikidata";

    private static final String TAGINFO_COASTLINE = "INSERT INTO tags (key, value, count_all) VALUES (''natural'', ''coastline'', {0})";
    private static final String TAGINFO_NATURAL = "INSERT INTO keys (key, count_all) VALUES (''natural'', {0})";
    private static final String TAGINFO_TREE = "INSERT INTO tags (key, value, count_all) VALUES (''natural'', ''tree'', {0})";

    private static final String WIKI_RELATION_TYPE = "INSERT INTO wiki_data (id, P2, P6, P9, P16, P25, P33, P34, P35, P36) VALUES ('Q798', 'key', 'de facto', 'Q8', 'type', 'Q4671', 'is prohibited', 'is prohibited', 'is prohibited', 'is allowed')";
    private static final String WIKI_NATURAL = "INSERT INTO wiki_data (id, P2, P9, P16) VALUES ('Q491', 'key', 'Q8', 'natural')";
    private static final String WIKI_TYPE = "INSERT INTO wiki_data (id, P2, P9, P16) VALUES ('Q798', 'key', 'Q8', 'type')";
    private static final String WIKI_COASTLINE = "INSERT INTO wiki_data (id, P2, P19, P33, P34, P35, P36) VALUES ('Q4719', 'tag', 'natural=coastline', 'Q8001', 'Q8000', 'Q8001', 'Q8001')";
    private static final String WIKI_MULTIPOLYGON = "INSERT INTO wiki_data (id, P2, P19) VALUES ('Q16042', 'tag', 'type=multipolygon')";
    private static final String WIKI_MULTIPOLYGON_RELATION = "INSERT INTO wiki_data (id, P2 ,P6, P25, P40, P41) VALUES ('Q16053', 'Q6', '[\"de facto\", {\"approved\": {\"P26\": [\"Q7804\", \"Q7792\", \"Q7796\", \"Q7798\", \"Q7799\", \"Q7809\", \"Q7788\", \"Q7814\"]}}]', '[\"Q4671\", {\"Q4694\": {\"P26\": [\"Q7806\"]}}]', 'Q16042', 'multipolygon')";
    private static final String WIKI_MULTIPOLYGON_OUTER = "INSERT INTO wiki_data (id, P2, P21, P34, P43) VALUES ('Q16069', 'Q4667', 'multipolygon=outer', 'is allowed', 'Q16053');";
    private static final String WIKI_TREE = "INSERT INTO wiki_data (id, P2, P19) VALUES ('Q4723', 'tag', 'natural=tree')";
    private static final String WIKI_HIGHWAY = "INSERT INTO wiki_data (id, P2, P9, P16) VALUES ('Q335', 'key', 'Q8', 'highway')";
    private static final String WIKI_HIGHWAY_TRUNK = "INSERT INTO wiki_data (id, P2, P19, P33, P34, P35, P36) VALUES ('Q5021', 'tag', 'highway=trunk', 'Q8001', 'Q8000', 'Q8001', 'Q8001')";
    private static final String WIKI_HIGHWAY_SECONDARY = "INSERT INTO wiki_data (id, P2, P19, P33, P34, P35, P36) VALUES ('Q4882', 'tag', 'highway=secondary', 'Q8001', 'Q8000', 'Q8001', 'Q8001')";

    private static final String WIKI_CYCLEWAY = "INSERT INTO wiki_data (id, P2, P6, P16, P25, P28, P31, P33, P34, P35, P36, P45) VALUES ('Q198', 'key', '[\"de facto\", {\"in use\": {\"P26\": [\"Q7785\", \"Q6994\", \"Q7799\", \"Q7809\"]}}]', 'cycleway', 'Q4704', 'Dsc01078 clip.jpg', '[\"Key:cycleway\", \"Cs:Key:cycleway\", \"Da:Key:cycleway\", \"DE:Key:cycleway\", \"ES:Key:cycleway\", \"FR:Key:cycleway\", \"IT:Key:cycleway\", \"JA:Key:cycleway\", \"Lt:Key:cycleway\", \"NL:Key:cycleway\", \"Pl:Key:cycleway\", \"Pt:Key:cycleway\", {\"Pt-br:Key:cycleway\": {\"P50\": [\"Pt:Key:cycleway\"]}}, \"RU:Key:cycleway\"]', 'is prohibited', 'is allowed', 'is prohibited', 'is prohibited', 'Q19374')";
    private static final String WIKI_CYCLEWAY_ASL = "INSERT INTO wiki_data (id, P2, P6, P10, P12, P19, P28, P31, P33, P34, P35, P36, P46) VALUES ('Q5688', 'tag', 'de facto', 'Q198', 'Q3134674', 'cycleway=asl', '[\"Praha-\\u010cern\\u00fd Most cyklobox 1.JPG\", {\"Bike Box.jpeg\": {\"P26\": [\"Q7785\", \"Q7792\"]}}]', '[\"Tag:cycleway=asl\", \"Cs:Tag:cycleway=asl\", \"FR:Tag:cycleway=asl\"]', 'is allowed', 'is prohibited', 'is prohibited', 'is prohibited', '[\"Q4874\", \"Q6474\"]')";

    private static final String WIKI_GB = "INSERT INTO wiki_data (id, P2, P12, P49) VALUES ('Q21157', 'Q19531', 'Q145', 'GB')";
    private static final String WIKI_US = "INSERT INTO wiki_data (id, P2, P12, P49) VALUES ('Q21158', 'Q19531', 'Q30', 'US')";

    @BeforeClass
    public static void setUpClass() throws IOException, SQLException, ReflectiveOperationException
    {
        // There is no significant difference between using in-memory db's and file
        // db's. Either one takes 0.25-0.5s per initialization, so avoid this when
        // possible.
        databases = createDatabasesFile();
        // This call also takes ~0.25s, so avoid where possible.
        genericTagCheck = new GenericTagCheck(ConfigurationResolver.inlineConfiguration(
                "{\"GenericTagCheck\":{\"filters.resource.override\": true,\"filters.classes.tags\":[], "
                        + databaseConfiguration(databases) + "}}"),
                new ExternalDataFetcher("", Collections.emptyMap()));

        databases.put(TAGINFO, getGenericTagCheckSqliteUtils(genericTagCheck).get(0).getFile());
        databases.put(WIKI_DATA, getGenericTagCheckSqliteUtils(genericTagCheck).get(1).getFile());
    }

    private static Map<String, String> createDatabasesFile() throws IOException, SQLException
    {
        final File taginfo = temporaryFolder.newFile("taginfo.db");
        // WARNING: This is a minimally viable table. More data could be added or
        // removed.
        try (Connection taginfoDB = DriverManager
                .getConnection(JDBC_SQLITE + taginfo.getCanonicalPath()))
        {
            createTagInfoDatabase(taginfoDB);
        }

        final File wikidata = temporaryFolder.newFile("wikidata.db");
        try (Connection memoryWikiData = DriverManager
                .getConnection(JDBC_SQLITE + wikidata.getCanonicalPath()))
        {
            createWikiDataDatabase(memoryWikiData);
        }

        return new HashMap<>(Map.of(TAGINFO, taginfo.getCanonicalPath(), WIKI_DATA,
                wikidata.getCanonicalPath()));
    }

    /**
     * Create a basic TagInfo db
     *
     * @param taginfoDB
     *            The connection to the db
     * @throws SQLException
     *             If something happens
     */
    private static void createTagInfoDatabase(final Connection taginfoDB) throws SQLException
    {
        try (Statement createTable = taginfoDB.createStatement())
        {
            createTable.addBatch(
                    "CREATE TABLE IF NOT EXISTS keys (key VARCHAR, count_all INTEGER, values_all INTEGER)");
            createTable.addBatch(
                    "CREATE TABLE IF NOT EXISTS tags (key VARCHAR, value VARCHAR, count_all INTEGER)");
            createTable.addBatch(
                    "CREATE INDEX IF NOT EXISTS tags_key_count_all_idx ON tags (key, count_all DESC)");
            createTable.executeBatch();
        }
    }

    /**
     * Create a basic WikiData db
     *
     * @param wikiData
     *            The connection to the db
     * @throws SQLException
     *             If something happens
     */
    private static void createWikiDataDatabase(final Connection wikiData) throws SQLException
    {
        try (Statement createTable = wikiData.createStatement())
        {
            createTable.addBatch("CREATE TABLE IF NOT EXISTS wiki_data (id STRING PRIMARY KEY)");
            final Logger logger = LoggerFactory.getLogger(GenericTagCheckTest.class);
            IntStream.rangeClosed(2, 51).forEach(i ->
            {
                try
                {
                    createTable.addBatch(
                            MessageFormat.format("ALTER TABLE wiki_data ADD P{0} BLOB", i));
                }
                catch (final SQLException e)
                {
                    logger.error("P" + i, e);
                }
            });
            createTable.executeBatch();
        }
    }

    /**
     * Create configuration strings for database usage
     *
     * @param databases
     *            The databases to use
     * @return The configuration (assumes all database names are prefixed with "db.")
     */
    private static String databaseConfiguration(final Map<String, String> databases)
    {
        final StringBuilder stringBuilder = new StringBuilder(databases.entrySet().stream()
                .mapToInt(entry -> entry.getKey().length() + entry.getValue().length() - 2).sum());
        for (final Map.Entry<String, String> entry : databases.entrySet())
        {
            stringBuilder.append("\"db.").append(entry.getKey()).append("\": \"")
                    .append(entry.getValue()).append("\", ");
        }
        final String returnString = stringBuilder.toString().strip();
        if (returnString.endsWith(","))
        {
            return returnString.substring(0, returnString.length() - 1);
        }
        return returnString;
    }

    /**
     * Get the SQLiteUtils for a test object
     * 
     * @param genericTagCheck
     *            The object to get the utils from
     * @return A list of utils, TagInfo then WikiData
     * @throws ReflectiveOperationException
     *             If something prevents reading the appropriate fields
     */
    private static List<SQLiteUtils> getGenericTagCheckSqliteUtils(
            final GenericTagCheck genericTagCheck) throws ReflectiveOperationException
    {
        final Field sqliteUtilsTagInfoTagTable = genericTagCheck.getClass()
                .getDeclaredField("sqliteUtilsTagInfoTagTable");
        final Field sqliteUtilsWikiData = genericTagCheck.getClass()
                .getDeclaredField("sqliteUtilsWikiData");
        final Field sqliteUtilsTagInfoKeyTable = genericTagCheck.getClass()
                .getDeclaredField("sqliteUtilsTagInfoKeyTable");
        sqliteUtilsTagInfoTagTable.setAccessible(true);
        sqliteUtilsWikiData.setAccessible(true);
        sqliteUtilsTagInfoKeyTable.setAccessible(true);
        final SQLiteUtils tagInfo = (SQLiteUtils) sqliteUtilsTagInfoTagTable.get(genericTagCheck);
        final SQLiteUtils wikiData = (SQLiteUtils) sqliteUtilsWikiData.get(genericTagCheck);
        final SQLiteUtils tagInfoKey = (SQLiteUtils) sqliteUtilsTagInfoKeyTable
                .get(genericTagCheck);
        // WARNING: Order is important -- DO NOT CHANGE ORDER (specifically, tagInfo and wikiData
        // must be first and second, respectively)
        return Arrays.asList(tagInfo, wikiData, tagInfoKey);
    }

    @Before
    public void setUpBefore() throws SQLException, ReflectiveOperationException
    {
        // Deleting table rows is faster than recreating, at least with current test
        // data sizes.
        try (Connection dbConnection = DriverManager
                .getConnection(JDBC_SQLITE + databases.get(WIKI_DATA));
                Statement statement = dbConnection.createStatement())
        {
            statement.addBatch("DELETE FROM wiki_data");
            statement.executeBatch();
        }
        try (Connection dbConnection = DriverManager
                .getConnection(JDBC_SQLITE + databases.get(TAGINFO));
                Statement statement = dbConnection.createStatement())
        {
            statement.addBatch("DELETE FROM keys");
            statement.addBatch("DELETE FROM tags");
            statement.executeBatch();
        }
        WikiData.clearWikiData();
        getGenericTagCheckSqliteUtils(genericTagCheck).forEach(SQLiteUtils::clear);
    }

    /**
     * This check ensures that `Key:cycleway` and `Tag:cycleway=asl` do not conflict.
     *
     * @throws SQLException
     *             If the database cannot be written to
     */
    @Test
    public void testBadCycleWay() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_CYCLEWAY);
            wikiStatement.addBatch(WIKI_CYCLEWAY_ASL);
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getBadCycleTag(), genericTagCheck);
            this.verifier.verifyEmpty();
        }
    }

    @Test
    public void testBadTag() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getBadTag(), genericTagCheck);
            this.verifier.verifyEmpty();
        }
    }

    @Test
    public void testClosedLineNoArea() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(WIKI_TREE);
            wikiStatement.addBatch("UPDATE wiki_data SET P34='is prohibited' WHERE id='Q4723'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTreeClosedLine(), genericTagCheck);
            // Closed line is OK
            this.verifier.verifyExpectedSize(0);
        }
    }

    @Test
    public void testClosedLineNoAreaOrLine() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(WIKI_TREE);
            wikiStatement.addBatch(
                    "UPDATE wiki_data SET P34='Q8001', P35='is prohibited' WHERE id='Q4723'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTreeClosedLine(), genericTagCheck);
            this.verifier.verify(flag -> assertEquals(
                    "natural=tree is prohibited on area. To determine whether to remove or change this tag please see this tags Wiki Data page, https://wiki.osm.org/Item:{3} or the associated documentation Wiki Page on the Wiki Data page).",
                    flag.getRawInstructions().get(1)));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testClosedLineNoLine() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(WIKI_TREE);
            wikiStatement.addBatch("UPDATE wiki_data SET P35='is prohibited' WHERE id='Q4723'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTreeClosedLine(), genericTagCheck);
            // Area is OK, which is a closed line. Atlas just doesn't understand that.
            this.verifier.verifyExpectedSize(0);
        }
    }

    @Test
    public void testClosedLineOK() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(WIKI_TREE);
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTreeClosedLine(), genericTagCheck);
            // Area or line are ok by default
            this.verifier.verifyExpectedSize(0);
        }
    }

    @Test
    public void testCountryProhibitedOnlyBad() throws SQLException
    {
        final String wikiData = databases.get(WIKI_DATA);
        try (Connection memoryWikiData = DriverManager.getConnection(JDBC_SQLITE + wikiData);
                Statement addData = memoryWikiData.createStatement())
        {
            addData.addBatch(WIKI_NATURAL);
            addData.addBatch(WIKI_TREE);
            addData.addBatch(WIKI_US);
            addData.addBatch("UPDATE wiki_data SET P30='Q21158' WHERE id='Q4723'");
            addData.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(flag.getInstructions(),
                    flag.getInstructions().contains("natural=tree should not be used in USA")));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testCountryProhibitedOnlyGood() throws SQLException
    {
        final String wikiData = databases.get(WIKI_DATA);
        try (Connection memoryWikiData = DriverManager.getConnection(JDBC_SQLITE + wikiData);
                Statement addData = memoryWikiData.createStatement())
        {
            addData.addBatch(WIKI_NATURAL);
            addData.addBatch(WIKI_TREE);
            addData.addBatch(WIKI_GB);
            addData.addBatch("UPDATE wiki_data SET P30='Q21157' WHERE id='Q4723'");
            addData.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verifyExpectedSize(0);
        }
    }

    @Test
    public void testCountrySpecificOnlyBad() throws SQLException
    {
        final String wikiData = databases.get(WIKI_DATA);
        try (Connection memoryWikiData = DriverManager.getConnection(JDBC_SQLITE + wikiData);
                Statement addData = memoryWikiData.createStatement())
        {
            addData.addBatch(WIKI_NATURAL);
            addData.addBatch(WIKI_TREE);
            addData.addBatch(WIKI_GB);
            addData.addBatch("UPDATE wiki_data SET P29='Q21157' WHERE id='Q4723'");
            // Unwanted tags on objects comes after country specific tagging.
            // For mutation testing, this test should then fail.
            addData.addBatch("UPDATE wiki_data SET P33='Q8001' WHERE P19='natural=tree'");
            addData.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(flag.getInstructions(),
                    flag.getInstructions().contains("natural=tree should not be used in USA")));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testCountrySpecificOnlyGood() throws SQLException
    {
        final String wikiData = databases.get(WIKI_DATA);
        try (Connection memoryWikiData = DriverManager.getConnection(JDBC_SQLITE + wikiData);
                Statement addData = memoryWikiData.createStatement())
        {
            addData.addBatch(WIKI_NATURAL);
            addData.addBatch(WIKI_TREE);
            addData.addBatch(WIKI_US);
            addData.addBatch("UPDATE wiki_data SET P29='Q21158' WHERE id='Q4723'");
            addData.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verifyEmpty();
        }
    }

    @Test
    public void testFallbackNoDatabases()
    {
        // We have to initialize a new check here just to check that db files aren't
        // used
        this.verifier.actual(this.setup.testAtlasCoastline(),
                new GenericTagCheck(ConfigurationResolver.inlineConfiguration(
                        "{\"GenericTagCheck\":{\"filters.resource.override\": true,\"filters.classes.tags\":[]}}"),
                        null));
        this.verifier.verify(
                flag -> assertTrue(flag.getInstructions().contains("not a well-known value")));
        this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
    }

    @Test
    public void testInvalidAreaValidWayCoastlineAllowNode() throws SQLException
    {
        final String wikiData = databases.get(WIKI_DATA);
        try (Connection memoryWikiData = DriverManager.getConnection(JDBC_SQLITE + wikiData);
                Statement addData = memoryWikiData.createStatement())
        {
            addData.addBatch(WIKI_NATURAL);
            addData.addBatch(WIKI_TYPE);
            addData.addBatch(WIKI_COASTLINE);
            addData.addBatch(WIKI_MULTIPOLYGON);
            addData.addBatch(WIKI_MULTIPOLYGON_RELATION);
            addData.addBatch(WIKI_MULTIPOLYGON_OUTER);
            addData.addBatch("UPDATE wiki_data SET P33='is allowed' WHERE id='Q4719'");
            addData.executeBatch();

            this.verifier.actual(this.setup.testAtlasCoastline(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(
                    flag.getFlaggedObjects().stream().allMatch(flaggedObject -> "Relation8000000"
                            .equals(flaggedObject.getUniqueIdentifier()))));
            this.verifier.verify(flag -> assertEquals(
                    "natural=coastline is prohibited on relation. To determine whether to remove or change this tag please see this tags Wiki Data page, https://wiki.osm.org/Item:{3} or the associated documentation Wiki Page on the Wiki Data page).",
                    flag.getRawInstructions().get(1)));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testInvalidAreaValidWayCoastlineAllowRelation() throws SQLException
    {
        final String wikiData = databases.get(WIKI_DATA);
        try (Connection memoryWikiData = DriverManager.getConnection(JDBC_SQLITE + wikiData);
                Statement addData = memoryWikiData.createStatement())
        {
            addData.addBatch(WIKI_NATURAL);
            addData.addBatch(WIKI_TYPE);
            addData.addBatch(WIKI_COASTLINE);
            addData.addBatch(WIKI_MULTIPOLYGON);
            addData.addBatch(WIKI_MULTIPOLYGON_RELATION);
            addData.addBatch(WIKI_MULTIPOLYGON_OUTER);
            addData.addBatch("UPDATE wiki_data SET P36='is allowed' WHERE id='Q4719'");
            addData.executeBatch();

            this.verifier.actual(this.setup.testAtlasCoastline(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(flag.getFlaggedObjects().stream()
                    .allMatch(flaggedObject -> Arrays.asList("Point", "Node")
                            .contains(flaggedObject.getProperties().get("itemType")))));
            this.verifier.verify(flag -> assertEquals(
                    "natural=coastline is prohibited on node. To determine whether to remove or change this tag please see this tags Wiki Data page, https://wiki.osm.org/Item:{3} or the associated documentation Wiki Page on the Wiki Data page).",
                    flag.getRawInstructions().get(1)));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testOpenLineNoAreaAndLine() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(WIKI_TREE);
            wikiStatement.addBatch(
                    "UPDATE wiki_data SET P34='is prohibited', P35='is prohibited' WHERE id='Q4723'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTreeOpenLine(), genericTagCheck);
            this.verifier.verify(flag -> assertEquals(
                    "natural=tree is prohibited on way. To determine whether to remove or change this tag please see this tags Wiki Data page, https://wiki.osm.org/Item:{3} or the associated documentation Wiki Page on the Wiki Data page).",
                    flag.getRawInstructions().get(1)));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testPopularKeyUndocumentedTag() throws SQLException
    {
        final String taginfo = databases.get(TAGINFO);
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection taginfoDb = DriverManager.getConnection(JDBC_SQLITE + taginfo);
                Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement();
                Statement taginfoStatement = taginfoDb.createStatement())
        {
            taginfoStatement.addBatch(MessageFormat.format(TAGINFO_TREE, "101"));
            taginfoStatement.executeBatch();
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch("UPDATE wiki_data SET P33='Q8001' WHERE P16='natural'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertEquals(
                    "natural=tree is not currently documented. Its global popularity (101 uses) may merit adding wiki documentation for this value. Please consider adding a new Wiki Data page for natural=tree.",
                    flag.getRawInstructions().get(1)));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testPopularKeyUndocumentedUnpopularTag() throws SQLException
    {
        final String taginfo = databases.get(TAGINFO);
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection taginfoDb = DriverManager.getConnection(JDBC_SQLITE + taginfo);
                Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement();
                Statement taginfoStatement = taginfoDb.createStatement())
        {
            taginfoStatement.addBatch(MessageFormat.format(TAGINFO_TREE, "100"));
            taginfoStatement.executeBatch();
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(
                    flag.getInstructions().contains("tree is not a well-known value for natural")));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testPopularUndocumented() throws SQLException
    {
        final String taginfo = databases.get(TAGINFO);
        try (Connection taginfoDb = DriverManager.getConnection(JDBC_SQLITE + taginfo);
                Statement createTable = taginfoDb.createStatement())
        {
            createTable.addBatch(MessageFormat.format(TAGINFO_TREE, "101"));
            createTable.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(flag.getInstructions().contains(
                    "natural=tree is probably an undocumented tag (101 instances). This should be documented on the wiki at https://wiki.osm.org/Tag:natural=tree and in the OpenStreetMap Wiki Data ( https://wiki.osm.org/Data_items ).")));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testPopularUndocumentedKey() throws SQLException
    {
        final String taginfo = databases.get(TAGINFO);
        try (Connection taginfoDb = DriverManager.getConnection(JDBC_SQLITE + taginfo);
                Statement createTable = taginfoDb.createStatement())
        {
            createTable.addBatch(MessageFormat.format(TAGINFO_NATURAL, "101"));
            createTable.addBatch("UPDATE keys SET values_all=1737 where key='natural'");
            createTable.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertEquals(
                    "natural is probably an undocumented key (101 instances). This should be documented on the wiki at https://wiki.osm.org/Key:natural and in the OpenStreetMap Wiki Data ( https://wiki.osm.org/Data_items ).",
                    flag.getRawInstructions().get(1)));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testRedirectToBadKey() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch("UPDATE wiki_data SET P17='Q335' WHERE id='Q491'");
            wikiStatement.addBatch("UPDATE wiki_data SET P9=NULL WHERE id='Q491'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(flag.getFixSuggestions().isEmpty()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testRedirectToKey() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(WIKI_HIGHWAY);
            wikiStatement.addBatch("UPDATE wiki_data SET P17='Q335' WHERE id='Q491'");
            wikiStatement.addBatch("UPDATE wiki_data SET P9=NULL WHERE id='Q491'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(flag.getInstructions()
                    .contains("natural should probably be replaced with highway")
                    && !flag.getInstructions()
                            .contains("natural should probably be replaced with highway=")));
            this.verifier.verify(flag -> assertEquals(1, flag.getFixSuggestions().size()));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testRedirectToMultiple() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(WIKI_TREE);
            wikiStatement.addBatch(WIKI_COASTLINE);
            wikiStatement.addBatch(WIKI_HIGHWAY_TRUNK);
            wikiStatement
                    .addBatch("UPDATE wiki_data SET P17='[\"Q4719\", \"Q5021\"]' WHERE id='Q4723'");
            // This is used for mutation testing (unwanted tag on primitive should always be
            // the last item checked)
            wikiStatement.addBatch("UPDATE wiki_data SET P33='Q8001' WHERE P19='natural=tree'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(
                    flag.getInstructions().contains("natural=tree should probably be replaced with")
                            && flag.getInstructions().contains("natural=coastline")
                            && flag.getInstructions().contains("highway=trunk")));
            this.verifier.verify(flag -> assertEquals(1, flag.getFixSuggestions().size()));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testRedirectToMultipleKey() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(WIKI_COASTLINE);
            wikiStatement.addBatch(WIKI_HIGHWAY);
            wikiStatement
                    .addBatch("UPDATE wiki_data SET P17='[\"Q335\", \"Q4719\"]' WHERE id='Q491'");
            wikiStatement.addBatch("UPDATE wiki_data SET P9=NULL WHERE id='Q491'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(
                    flag.getInstructions().contains("natural should probably be replaced with")
                            && flag.getInstructions().contains("natural=coastline")
                            && flag.getInstructions().contains("highway")
                            && !flag.getInstructions().contains("highway=")));
            this.verifier.verify(flag -> assertEquals(1, flag.getFixSuggestions().size()));
            this.verifier.verify(flag ->
            {
                final Map<String, String> tagMap = flag.getFixSuggestions().iterator().next()
                        .getAfterView().getOsmTags();
                assertEquals("tree", tagMap.get("highway"));
                assertEquals("coastline", tagMap.get("natural"));
            });
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testRedirectToTag() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(WIKI_TREE);
            wikiStatement.addBatch(WIKI_COASTLINE);
            wikiStatement.addBatch("UPDATE wiki_data SET P17='Q4719' WHERE id='Q4723'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(flag.getInstructions()
                    .contains("natural=tree should probably be replaced with natural=coastline")));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testRelationsBadMember() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_RELATION_TYPE);
            wikiStatement.addBatch(WIKI_MULTIPOLYGON);
            wikiStatement.addBatch(WIKI_MULTIPOLYGON_RELATION);
            wikiStatement.addBatch(WIKI_COASTLINE);
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(
                    "UPDATE wiki_data SET P33='Q8000', P35='Q8000', P36='Q8000' WHERE P19='natural=coastline'");
            // Add items that shouldn't be hit for mutation testing. Use countries instead
            // of unwanted object on primitive since the code checks that it can be used on
            // relations.
            wikiStatement.addBatch(WIKI_GB);
            wikiStatement
                    .addBatch("UPDATE wiki_data SET P30='Q21157' WHERE P19='type=multipolygon'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.testAtlasCoastline(), genericTagCheck);
            this.verifier.verify(
                    flag -> assertTrue(flag.getInstructions(), flag.getInstructions().contains(
                            "multipolygon=outer is an unknown relation role for multipolygon (way 6 on relation 9")));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testRelationsUnknownType() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_RELATION_TYPE);
            wikiStatement.addBatch(WIKI_MULTIPOLYGON);
            wikiStatement.addBatch(WIKI_COASTLINE);
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(
                    "UPDATE wiki_data SET P33='Q8000', P35='Q8000', P36='Q8000' WHERE P19='natural=coastline'");
            // Add items that shouldn't be hit for mutation testing. Use countries instead
            // of unwanted object on primitive since the code checks that it can be used on
            // relations.
            wikiStatement.addBatch(WIKI_GB);
            wikiStatement
                    .addBatch("UPDATE wiki_data SET P30='Q21157' WHERE P19='type=multipolygon'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.testAtlasCoastline(), genericTagCheck);
            this.verifier.verify(flag -> assertEquals(
                    "type=multipolygon is an unknown relation type (relation 9, see https://wiki.osm.org/Item:Q16042)",
                    flag.getRawInstructions().get(1)));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testUnpopularUndocumented() throws SQLException
    {
        final String taginfo = databases.get(TAGINFO);
        try (Connection taginfoDb = DriverManager.getConnection(JDBC_SQLITE + taginfo);
                Statement createTable = taginfoDb.createStatement())
        {
            createTable.addBatch(MessageFormat.format(TAGINFO_TREE, "100"));
            createTable.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(flag.getInstructions()
                    .contains("natural=tree is an unpopular key (100 instances)")));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testWellKnownApproved() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(WIKI_TREE);
            wikiStatement.addBatch("UPDATE wiki_data SET P6='approved' WHERE P19='natural=tree'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verifyEmpty();
        }
    }

    @Test
    public void testWellKnownDiscardable() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(WIKI_TREE);
            wikiStatement.addBatch(
                    "UPDATE wiki_data SET P6='discardable', P33='Q8001' WHERE P19='natural=tree'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verify(flag -> assertEquals(
                    "natural=tree should probably be removed (it is discardable, see https://wiki.osm.org/Item:Q4723)",
                    flag.getRawInstructions().get(1)));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testWellKnownObsolete() throws SQLException
    {
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection wikidatadb = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiStatement = wikidatadb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            wikiStatement.addBatch(WIKI_TREE);
            wikiStatement.addBatch("UPDATE wiki_data SET P6='obsolete' WHERE id='Q4723'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertTrue(flag.getInstructions().contains(
                    "natural=tree should probably be removed (it is obsolete, see https://wiki.osm.org/Item:Q4723)")));
            this.verifier.verifyExpectedSize(1);
        }
    }

    @Test
    public void testWikiInstructions() throws SQLException
    {
        final String taginfo = databases.get(TAGINFO);
        final String wikidata = databases.get(WIKI_DATA);
        try (Connection tagInfoDb = DriverManager.getConnection(JDBC_SQLITE + taginfo);
                Statement createTable = tagInfoDb.createStatement();
                Connection wikiInfoDB = DriverManager.getConnection(JDBC_SQLITE + wikidata);
                Statement wikiTable = wikiInfoDB.createStatement())
        {
            createTable.addBatch(MessageFormat.format(TAGINFO_NATURAL, "42520473"));
            createTable.addBatch(MessageFormat.format(TAGINFO_COASTLINE, "1042369"));
            createTable.executeBatch();
            wikiTable.addBatch(WIKI_MULTIPOLYGON);
            wikiTable.addBatch(WIKI_MULTIPOLYGON_OUTER);
            wikiTable.addBatch(WIKI_MULTIPOLYGON_RELATION);
            wikiTable.addBatch(WIKI_NATURAL);
            wikiTable.addBatch(WIKI_RELATION_TYPE);

            wikiTable.executeBatch();
            this.verifier.actual(this.setup.testAtlasCoastline(), genericTagCheck);
            this.verifier.globallyVerify(flags ->
            {
                final Collection<String> instructions = flags.stream()
                        .map(CheckFlag::getInstructions).collect(Collectors.toList());
                assertTrue(instructions.stream()
                        .anyMatch(string -> string.contains("OSM feature 1 has invalid tags")));
                assertTrue(instructions.stream()
                        .anyMatch(string -> string.contains("OSM feature 6 has invalid tags")));
                assertTrue(instructions.stream()
                        .anyMatch(string -> string.contains("OSM feature 7 has invalid tags")));
                assertTrue(instructions.stream()
                        .anyMatch(string -> string.contains("OSM feature 8 has invalid tags")));
                assertTrue(instructions.stream()
                        .anyMatch(string -> string.contains("OSM feature 9 has invalid tags")));
            });
            this.verifier.verify(flag -> assertEquals(
                    "natural=coastline is not currently documented. Its global popularity (1,042,369 uses) may merit adding wiki documentation for this value. Please consider adding a new Wiki Data page for natural=coastline.",
                    flag.getRawInstructions().get(1)));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verifyExpectedSize(5);
        }
    }

    @Test
    public void testWikiRegex() throws SQLException
    {
        final String wikiData = databases.get(WIKI_DATA);
        try (Connection wikiDataDb = DriverManager.getConnection(JDBC_SQLITE + wikiData);
                Statement addData = wikiDataDb.createStatement())
        {
            addData.addBatch(WIKI_HIGHWAY);
            // P34 is Q8001 (not allowed) since unwanted tags on objects are the last things
            // tested, and so for mutation testing, this is ncessary.
            addData.addBatch(
                    "INSERT INTO wiki_data (id, P2, P13, P16, P33, P34, P35, P36) VALUES ('Q372', 'key', '[1-9]\\d*(\\.\\d+)?', 'lanes', 'Q8001', 'Q8000', 'Q8001', 'Q8001')");
            addData.addBatch(WIKI_HIGHWAY_TRUNK);
            addData.addBatch(WIKI_HIGHWAY_SECONDARY);
            addData.executeBatch();

            long currentTimeMillis = System.currentTimeMillis();
            this.verifier.actual(this.setup.getLanes(), genericTagCheck);
            final long initialTimeTaken = System.currentTimeMillis() - currentTimeMillis;
            // Ensure that regexes are cached for further use. Run 5 times just in case...
            // Caching reduces runtime for the check anywhere between 50% and 90%.
            long totalTime = 0;
            for (int i = 0; i < 5; i++)
            {
                currentTimeMillis = System.currentTimeMillis();
                this.verifier.actual(this.setup.getLanes(), genericTagCheck);
                final long secondTimeTaken = System.currentTimeMillis() - currentTimeMillis;
                totalTime += secondTimeTaken;
            }
            assertTrue(initialTimeTaken > totalTime / (5 * 2));
            this.verifier.verify(flag -> assertTrue(
                    flag.getFlaggedObjects().stream().allMatch(flaggedObject -> Objects
                            .equals("Edge", flaggedObject.getProperties().get("itemType")))));
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verify(flag -> assertEquals(
                    "0.5 does not match the regex ^([1-9]\\d*(\\.\\d+)?)$ for the key lanes (see https://wiki.osm.org/Item:Q372)",
                    flag.getRawInstructions().get(1)));
            // We run the same test 6 times, but no deduplication is done. Therefore, there
            // should be 6 flags.
            this.verifier.verifyExpectedSize(6);
        }
    }

    @Test
    public void testWikiRegexGood() throws SQLException
    {
        final String wikiData = databases.get(WIKI_DATA);
        try (Connection wikiDataDb = DriverManager.getConnection(JDBC_SQLITE + wikiData);
                Statement addData = wikiDataDb.createStatement())
        {
            addData.addBatch(WIKI_HIGHWAY);
            addData.addBatch(
                    "INSERT INTO wiki_data (id, P3, P13, P16, P33, P34, P35, P36) VALUES ('Q372', 'key', '[0-9]\\d*(\\.\\d+)?', 'lanes', 'Q8001', 'Q8000', 'Q8001', 'Q8001')");
            addData.addBatch(WIKI_HIGHWAY_TRUNK);
            addData.addBatch(WIKI_HIGHWAY_SECONDARY);
            addData.executeBatch();

            this.verifier.actual(this.setup.getLanes(), genericTagCheck);
            this.verifier.verifyExpectedSize(0);
        }
    }

    @Test
    public void testWikiRegexMutation() throws SQLException
    {
        final String wikiData = databases.get(WIKI_DATA);
        try (Connection wikiDataDb = DriverManager.getConnection(JDBC_SQLITE + wikiData);
                Statement wikiStatement = wikiDataDb.createStatement())
        {
            wikiStatement.addBatch(WIKI_NATURAL);
            // This is used for mutation testing (unwanted tag on primitive should always be
            // the last item checked)
            wikiStatement.addBatch(
                    "UPDATE wiki_data SET P33='Q8001', P13='[0-9]+', P9=null WHERE P16='natural'");
            wikiStatement.executeBatch();
            this.verifier.actual(this.setup.getTree(), genericTagCheck);
            this.verifier.verify(flag -> assertEquals(2, flag.getRawInstructions().size()));
            this.verifier.verify(flag -> assertEquals(
                    "tree does not match the regex ^([0-9]+)$ for the key natural (see https://wiki.osm.org/Item:Q491)",
                    flag.getRawInstructions().get(1)));
            this.verifier.verifyExpectedSize(1);
        }
    }
}
