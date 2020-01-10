package org.openstreetmap.atlas.checks.maproulette;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.ChallengeDifficulty;
import org.openstreetmap.atlas.checks.maproulette.data.Project;
import org.openstreetmap.atlas.checks.maproulette.data.ProjectConfiguration;
import org.openstreetmap.atlas.checks.maproulette.data.Task;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Unit tests for MapRouletteClient
 *
 * @author nachtm
 */
public class MapRouletteClientTest
{

    private static final String CONFIGURATION = "server:123:project:api_key";
    private static final Challenge TEST_CHALLENGE = new Challenge("a name", "a description",
            "a blurb", "an instruction", ChallengeDifficulty.EASY, "");
    private static final Optional<JsonArray> GEOJSON = getSampleGeojson();
    
    @Rule
    public TemporaryFolder challengesDir = new TemporaryFolder();
    
    private Task testTaskOne;
    private TestMapRouletteConnection mockConnection;
    private MapRouletteClient client;

    private static Optional<JsonArray> getSampleGeojson()
    {
        final JsonArray features = new JsonArray();
        final JsonObject propertiesData = new JsonObject();
        propertiesData.add("identifier", new JsonPrimitive("123"));
        propertiesData.add("itemType", new JsonPrimitive("Area"));
        propertiesData.add("flag:check", new JsonPrimitive(TEST_CHALLENGE.getName()));
        propertiesData.add("flag:generator", new JsonPrimitive("Atlas Checks - Test"));
        final JsonObject properties = new JsonObject();
        properties.add("properties", propertiesData);
        features.add(properties);
        return Optional.of(features);
    }

    @Before
    public void setUp()
    {
        this.testTaskOne = new Task();
        this.testTaskOne.setTaskIdentifier("1");
        this.testTaskOne.setInstruction("Dud instruction");
        this.testTaskOne.setGeoJson(GEOJSON);
        this.mockConnection = new TestMapRouletteConnection();
        this.client = new MapRouletteClient(MapRouletteConfiguration.parse(CONFIGURATION),
                this.mockConnection);
    }

    @Test
    public void testAddAndUploadTask()
    {
        this.client.addTask(TEST_CHALLENGE, this.testTaskOne);
        this.client.uploadTasks();
        TEST_CHALLENGE.setId(12);

        Assert.assertEquals(1, this.mockConnection.uploadedProjects().size());
        Assert.assertEquals("project", this.mockConnection.uploadedProjects().stream().findFirst()
                .map(Project::getName).orElse(""));
        Assert.assertEquals(1, this.mockConnection.tasksForChallenge(TEST_CHALLENGE).size());
    }

    @Test
    public void testAddAndUploadTaskToNewProject()
    {
        this.client.addTask("another project", TEST_CHALLENGE, this.testTaskOne);
        this.client.uploadTasks();
        TEST_CHALLENGE.setId(12);

        Assert.assertEquals(1, this.mockConnection.uploadedProjects().size());
        Assert.assertEquals("another project", this.mockConnection.uploadedProjects().stream()
                .findFirst().map(Project::getName).orElse(""));
        Assert.assertEquals(1, this.mockConnection.tasksForChallenge(TEST_CHALLENGE).size());
    }

    @Test
    public void testProjectConfiguration()
    {
        final String name = "yet another project";
        final String description = "description";
        final String displayName = "displayName";
        final ProjectConfiguration configuration = new ProjectConfiguration(name, description,
                displayName, false);
        TEST_CHALLENGE.setId(12);
        this.client.addTask(configuration, TEST_CHALLENGE, this.testTaskOne);
        this.client.uploadTasks();

        Assert.assertEquals(1, this.mockConnection.uploadedProjects().size());
        final Project uploadedProject = this.mockConnection.uploadedProjects().stream().findFirst()
                .orElseGet(() ->
                {
                    Assert.fail(
                            "There should be exactly one uploaded project, so this shouldn't fail.");
                    return new Project("some name");
                });
        Assert.assertEquals(name, uploadedProject.getName());
        Assert.assertEquals(description, uploadedProject.getDescription());
        Assert.assertEquals(displayName, uploadedProject.getDisplayName());
        Assert.assertFalse(uploadedProject.isEnabled());
        Assert.assertEquals(1, this.mockConnection.tasksForChallenge(TEST_CHALLENGE).size());
    }

    @Test
    public void testWriteChallengeIdsToFile() throws Exception
    {
        // Project setup.
        final String projectName = "challengeMetaProject";
        final Map<String, Long> projectNameToId = new HashMap<>();
        projectNameToId.put(projectName, 34096L);
        this.mockConnection.setProjectNameToId(projectNameToId);

        // challenge setup
        final String dirPath = this.challengesDir.newFolder("challenges").getAbsolutePath();
        final Optional<String> directoryPath = Optional.of(dirPath);
        this.client.setOutputPath(directoryPath);
        TEST_CHALLENGE.setId(12);

        // Upload challenge.
        this.client.addTask(projectName, TEST_CHALLENGE, this.testTaskOne);

        final File challengesFile = new File(dirPath, "challenges.txt");
        final String actualData = new String(Files.readAllBytes(challengesFile.toPath()));
        final String expectedData = "project:34096;challenge:12\n";
        assertEquals(expectedData, actualData);
    }
}
