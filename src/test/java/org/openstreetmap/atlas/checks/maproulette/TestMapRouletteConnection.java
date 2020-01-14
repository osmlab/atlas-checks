package org.openstreetmap.atlas.checks.maproulette;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.Project;
import org.openstreetmap.atlas.checks.maproulette.data.Task;

import com.google.gson.JsonArray;

/**
 * A stub MapRouletteConnection that doesn't actually connect to anything.
 *
 * @author nachtm
 */
public class TestMapRouletteConnection implements TaskLoader
{

    private final Map<Project, Set<Challenge>> projectToChallenges;
    private final Map<Long, Set<Task>> challengeToTasks;
    private Map<String, Long> projectNameToId;

    public TestMapRouletteConnection()
    {
        this.projectToChallenges = new HashMap<>();
        this.challengeToTasks = new HashMap<>();
        this.projectNameToId = new HashMap<>();
    }

    public Set<Challenge> challengesForProject(final Project project)
    {
        return this.projectToChallenges.get(project);
    }

    @Override
    public long createChallenge(final Project project, final Challenge challenge)
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

    @Override
    public long createProject(final Project project)
            throws UnsupportedEncodingException, URISyntaxException
    {
        this.projectToChallenges.put(project, new HashSet<>());
        return this.projectNameToId.getOrDefault(project.getName(), 0L);
    }

    @Override
    public String getConnectionInfo()
    {
        return "";
    }

    public void setProjectNameToId(final Map<String, Long> projectIdMap)
    {
        this.projectNameToId = projectIdMap;
    }

    public Set<Task> tasksForChallenge(final Challenge challenge)
    {
        return this.challengeToTasks.get(challenge.getId());
    }

    @Override
    public boolean uploadBatchTasks(final long challengeId, final Set<Task> tasks)
            throws UnsupportedEncodingException, URISyntaxException
    {
        if (this.challengeToTasks.containsKey(challengeId))
        {
            this.challengeToTasks.get(challengeId).addAll(tasks);
        }
        else
        {
            tasks.forEach(task ->
            {
                try
                {
                    this.uploadTask(challengeId, task);
                    // Only record the task as being uploaded if it was generated properly in the
                    // line above
                    this.challengeToTasks.put(challengeId, new HashSet<>(tasks));
                }
                catch (UnsupportedEncodingException | URISyntaxException e)
                {
                    e.printStackTrace();
                }
            });
        }
        return true;
    }

    @Override
    public boolean uploadTask(final long challengeId, final Task task)
            throws UnsupportedEncodingException, URISyntaxException
    {
        final JsonArray taskArray = new JsonArray();
        taskArray.add(task.generateTask(challengeId));
        // At this point we would send the taskArray out to an MR server for upload.
        // Return true in this case if the task was generated properly. An exception is thrown if
        // it's not -- this is how we know if the task wouldn't be uploaded in a nontesting
        // environment.
        return true;
    }

    public Set<Project> uploadedProjects()
    {
        return this.projectToChallenges.keySet();
    }
}
