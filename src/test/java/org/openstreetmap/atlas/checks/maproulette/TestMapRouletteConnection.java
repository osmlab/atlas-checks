package org.openstreetmap.atlas.checks.maproulette;

import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.Project;
import org.openstreetmap.atlas.checks.maproulette.data.Task;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A stub MapRouletteConnection that doesn't actually connect to anything.
 *
 * @author nachtm
 */
public class TestMapRouletteConnection implements TaskLoader
{

    private final Map<Project, Set<Challenge>> projectToChallenges;
    private final Map<Long, Set<Task>> challengeToTasks;

    public TestMapRouletteConnection()
    {
        this.projectToChallenges = new HashMap<>();
        this.challengeToTasks = new HashMap<>();
    }

    @Override public String getConnectionInfo()
    {
        return "";
    }

    @Override public boolean uploadBatchTasks(final long challengeId, final Set<Task> tasks)
            throws UnsupportedEncodingException, URISyntaxException
    {
        if (this.challengeToTasks.containsKey(challengeId))
        {
            this.challengeToTasks.get(challengeId).addAll(tasks);
        }
        else
        {
            this.challengeToTasks.put(challengeId, tasks);
        }
    }

    @Override public boolean uploadTask(final long challengeId, final Task task)
            throws UnsupportedEncodingException, URISyntaxException
    {
        return false;
    }

    @Override public long createProject(final Project project)
            throws UnsupportedEncodingException, URISyntaxException
    {
        this.projectToChallenges.put(project, Collections.emptySet());
        return 0;
    }

    @Override public long createChallenge(final Project project, final Challenge challenge)
            throws UnsupportedEncodingException, URISyntaxException
    {
        if (this.projectToChallenges.containsKey(project))
        {
            this.projectToChallenges.get(project).add(challenge);
        }
        else
        {
            final Set<Challenge> newSet = new HashSet<>();
            newSet.add(challenge);
            this.projectToChallenges.put(project, newSet);
        }
        return 0;
    }

    public Set<Project> uploadedProjects()
    {
        return projectToChallenges.keySet();
    }
}
