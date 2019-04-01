package org.openstreetmap.atlas.checks.maproulette;

import java.util.Collections;
import java.util.Set;

import org.junit.After;
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

    @Rule
    public final MapRouletteUploadCommandTestRule setup = new MapRouletteUploadCommandTestRule();

    @Before
    public void writeFiles()
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
    }

    @After
    public void cleanup()
    {
        new File(FOLDER.getPath()).deleteRecursively();
    }

    @Test
    public void testExecute()
    {
        // Set up some arguments
        final MapRouletteCommand command = new MapRouletteUploadCommand();
        final String[] arguments = { String.format("-logfiles=%s", FOLDER.getPath()),
                MAPROULETTE_CONFIG };
        final CommandMap map = command.getCommandMap(arguments);
        final TestMapRouletteConnection connection = new TestMapRouletteConnection();

        // Run the command
        command.onRun(map, configuration -> new MapRouletteClient(configuration, connection));

        // Test the results
        final Set<Project> projects = connection.uploadedProjects();
        Assert.assertEquals(1, projects.size());
        projects.forEach(project ->
        {
            Assert.assertEquals("project", project.getName());
            final Set<Challenge> challenges = connection.challengesForProject(project);
            Assert.assertEquals(1, challenges.size());
            challenges.forEach(challenge -> Assert.assertEquals(2,
                    connection.tasksForChallenge(challenge).size()));
        });
    }
}
