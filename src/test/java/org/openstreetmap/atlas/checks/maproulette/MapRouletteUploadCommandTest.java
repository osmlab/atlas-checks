package org.openstreetmap.atlas.checks.maproulette;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.event.CheckFlagFileProcessor;
import org.openstreetmap.atlas.checks.event.FileProcessor;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.Project;
import org.openstreetmap.atlas.event.ShutdownEvent;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Unit tests for MapRouletteUploadCommand.
 *
 * @author nachtm
 * @author bbreithaupt
 */
public class MapRouletteUploadCommandTest
{
    private static final String MAPROULETTE_CONFIG = "-maproulette=host:2222:project:api";
    private static final File FOLDER = File.temporaryFolder();
    @Rule
    public final MapRouletteUploadCommandTestRule setup = new MapRouletteUploadCommandTestRule();
    private boolean filesCreated = false;

    @AfterClass
    public static void cleanup()
    {
        new File(FOLDER.getPathString()).deleteRecursively();
    }

    @Test
    public void testCheckFilter()
    {
        final String[] additionalArguments = { "-checks=SomeCheck" };
        this.runAndTest(additionalArguments, 1, 2, 2);
    }

    @Test
    public void testCountryFilter()
    {
        final String[] additionalArguments = { "-countries=CAN" };
        this.runAndTest(additionalArguments, 1, 1, 1);
    }

    @Test
    public void testExecute()
    {
        final String[] additionalArguments = {};
        this.runAndTest(additionalArguments, 1, 4, 4);
    }

    @Test
    public void testGetCountryDisplayName()
    {
        final String countries1 = "CAN,MEX";
        final String countries2 = "USA";
        final MapRouletteUploadCommand command = new MapRouletteUploadCommand();
        final String displayCountryNames1 = command.getCountryDisplayName(countries1);
        final String displayCountryNames2 = command.getCountryDisplayName(countries2);

        Assert.assertEquals("Canada, Mexico", displayCountryNames1);
        Assert.assertEquals("United States", displayCountryNames2);
    }

    @Test
    public void testGetCustomChallengeName()
    {
        final String[] additionalArguments = { String.format("-config=%s",
                MapRouletteUploadCommandTest.class.getResource("checksConfig.json").getPath()) };
        final TestMapRouletteConnection connection = this.run(additionalArguments);
        final Set<Project> projects = connection.uploadedProjects();
        final List<String> challengeNames = projects.stream().flatMap(project -> connection
                .challengesForProject(project).stream().map(Challenge::getName))
                .collect(Collectors.toList());

        Collections.sort(challengeNames);
        Assert.assertEquals("Canada - Spiky Buildings", challengeNames.get(0));
        Assert.assertEquals("Mexico, Belize - Intersecting Lines", challengeNames.get(1));
        Assert.assertEquals("United States - Address Point Match", challengeNames.get(2));
        Assert.assertEquals("Uruguay - Address Point Match", challengeNames.get(3));
    }

    @Before
    public void writeFiles()
    {
        if (!this.filesCreated)
        {
            // Create an unzipped file
            final FileProcessor<CheckFlagEvent> unzippedProcessor = new CheckFlagFileProcessor(
                    new SparkFileHelper(Collections.emptyMap()),
                    FOLDER.child("unzipped.log").toString()).withCompression(false);
            unzippedProcessor.process(this.setup.getOneBasicFlag());
            unzippedProcessor.process(this.setup.getTwoCountryFlag());
            unzippedProcessor.process(this.setup.getAnotherBasicFlag());
            unzippedProcessor.process(this.setup.getFlagSameCheck());
            unzippedProcessor.process(new ShutdownEvent());

            // Create a zipped file
            final FileProcessor<CheckFlagEvent> zippedProcessor = new CheckFlagFileProcessor(
                    new SparkFileHelper(Collections.emptyMap()),
                    FOLDER.child("zipped.log.gz").toString()).withCompression(true);
            zippedProcessor.process(this.setup.getAnotherBasicFlag());
            zippedProcessor.process(new ShutdownEvent());

            this.filesCreated = true;
        }
    }

    /**
     * Similar to runAndTest, however this function will return a {@link TestMapRouletteConnection}
     *
     * @param additionalArguments
     *            String[] of extra arguments for {@link MapRouletteUploadCommand}, to add to the
     *            data i/o locations and server config
     * @return TestMapRouletteConnection
     */
    private TestMapRouletteConnection run(final String[] additionalArguments)
    {
        // Set up some arguments
        final MapRouletteCommand command = new MapRouletteUploadCommand();
        final String[] arguments = { String.format("-logfiles=%s", FOLDER.getPathString()),
                MAPROULETTE_CONFIG };
        final CommandMap map = command
                .getCommandMap((String[]) ArrayUtils.addAll(arguments, additionalArguments));
        final TestMapRouletteConnection connection = new TestMapRouletteConnection();

        // Run the command
        command.onRun(map, configuration -> new MapRouletteClient(configuration, connection));

        return connection;
    }

    /**
     * Runs {@link MapRouletteUploadCommand} using a {@link TestMapRouletteConnection}. Tests that
     * the correct number of projects, challenges, and tasks are created.
     *
     * @param additionalArguments
     *            String[] of extra arguments for {@link MapRouletteUploadCommand}, to add to the
     *            data i/o locations and server config
     * @param expectedProjects
     *            int, number of expected projects
     * @param expectedChallenges
     *            int, number of expected challenges per project
     * @param expectedTasks
     *            int, number of expected tasks per challenge per project
     */
    private void runAndTest(final String[] additionalArguments, final int expectedProjects,
            final int expectedChallenges, final int expectedTasks)
    {
        // Set up some arguments
        final TestMapRouletteConnection connection = this.run(additionalArguments);

        // Test the results
        final Set<Project> projects = connection.uploadedProjects();
        Assert.assertEquals(expectedProjects, projects.size());
        projects.forEach(project ->
        {
            Assert.assertEquals("project", project.getName());
            final Set<Challenge> challenges = connection.challengesForProject(project);
            Assert.assertEquals(expectedChallenges, challenges.size());
            challenges.forEach(challenge -> Assert.assertEquals(expectedTasks,
                    connection.tasksForChallenge(challenge).size()));
        });
    }
}
