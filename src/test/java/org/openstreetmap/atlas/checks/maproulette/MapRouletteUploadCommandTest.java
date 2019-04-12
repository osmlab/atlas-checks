package org.openstreetmap.atlas.checks.maproulette;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.checks.event.CheckFlagEvent;
import org.openstreetmap.atlas.checks.event.CheckFlagFileProcessor;
import org.openstreetmap.atlas.checks.event.FileProcessor;
import org.openstreetmap.atlas.checks.event.ShutdownEvent;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.Project;
import org.openstreetmap.atlas.generator.tools.spark.utilities.SparkFileHelper;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Unit tests for MapRouletteUploadCommand.
 *
 * @author nachtm
 */
public class MapRouletteUploadCommandTest
{
    private static final String MAPROULETTE_CONFIG = "-maproulette=host:2222:project:api";
    private static final File FOLDER = File.temporaryFolder();

    private boolean filesCreated = false;

    @Rule
    public final MapRouletteUploadCommandTestRule setup = new MapRouletteUploadCommandTestRule();

    @Before
    public void writeFiles()
    {
        if (!this.filesCreated)
        {
            // Create an unzipped file
            final FileProcessor<CheckFlagEvent> unzippedProcessor = new CheckFlagFileProcessor(
                    new SparkFileHelper(Collections.emptyMap()),
                    FOLDER.child("unzipped.log").toString()).withCompression(false);
            unzippedProcessor.process(setup.getOneBasicFlag());
            unzippedProcessor.process(new ShutdownEvent());

            // Create a zipped file
            final FileProcessor<CheckFlagEvent> zippedProcessor = new CheckFlagFileProcessor(
                    new SparkFileHelper(Collections.emptyMap()),
                    FOLDER.child("zipped.log.gz").toString()).withCompression(true);
            zippedProcessor.process(setup.getAnotherBasicFlag());
            zippedProcessor.process(new ShutdownEvent());

            this.filesCreated = true;
        }
    }

    @AfterClass
    public static void cleanup()
    {
        new File(FOLDER.getPath()).deleteRecursively();
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
        final MapRouletteCommand command = new MapRouletteUploadCommand();
        final String[] arguments = { String.format("-logfiles=%s", FOLDER.getPath()),
                MAPROULETTE_CONFIG };
        final CommandMap map = command
                .getCommandMap((String[]) ArrayUtils.addAll(arguments, additionalArguments));
        final TestMapRouletteConnection connection = new TestMapRouletteConnection();

        // Run the command
        command.onRun(map, configuration -> new MapRouletteClient(configuration, connection));

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

    @Test
    public void testExecute()
    {
        final String[] additionalArguments = {};
        this.runAndTest(additionalArguments, 1, 2, 2);
    }

    @Test
    public void testCheckFilter()
    {
        final String[] additionalArguments = { "-checks=SomeCheck" };
        this.runAndTest(additionalArguments, 1, 1, 1);
    }

    @Test
    public void testCountryFilter()
    {
        final String[] additionalArguments = { "-countries=CAN" };
        this.runAndTest(additionalArguments, 1, 1, 1);
    }
}
