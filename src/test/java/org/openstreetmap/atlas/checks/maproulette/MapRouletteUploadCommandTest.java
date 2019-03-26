package org.openstreetmap.atlas.checks.maproulette;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.Project;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Unit tests for MapRouletteUploadCommand.
 *
 * @author nachtm
 */
public class MapRouletteUploadCommandTest
{
    private static final String MAPROULETTE_CONFIG = "-maproulette=host:2222:project:api";
    private static final String SHORT_LOG_FILES = String.format("-logfiles=%s",
            MapRouletteUploadCommand.class.getResource("short_log_files").getPath());

    @Test
    public void testExecute()
    {
        // Set up some arguments
        final MapRouletteCommand command = new MapRouletteUploadCommand();
        final String[] arguments = { SHORT_LOG_FILES, MAPROULETTE_CONFIG };
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
