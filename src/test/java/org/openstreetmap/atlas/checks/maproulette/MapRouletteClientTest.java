package org.openstreetmap.atlas.checks.maproulette;

import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.ChallengeDifficulty;
import org.openstreetmap.atlas.checks.maproulette.data.Project;
import org.openstreetmap.atlas.checks.maproulette.data.ProjectConfiguration;
import org.openstreetmap.atlas.checks.maproulette.data.Task;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

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
    public void testWriteChallengeIdsToFile()
    {
        String fileName = String.format("%s/src/test/resources/challenges/challengeIds",
                System.getProperty("user.dir"));
        Optional<String> fileNameOptional = Optional.of(fileName);
        assertTrue(fileNameOptional.isPresent());
        MapRouletteClient.setChallengeIdFile(fileNameOptional);
        MapRouletteClient.writeChallengeIdsToFile(1);
        MapRouletteClient.writeChallengeIdsToFile(2);
    }
}
