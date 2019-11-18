package org.openstreetmap.atlas.checks.maproulette;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.Project;
import org.openstreetmap.atlas.checks.maproulette.data.ProjectConfiguration;
import org.openstreetmap.atlas.checks.maproulette.data.Task;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stand-alone MapRoulette client
 *
 * @author mgostintsev
 * @author nachtm
 */
public class MapRouletteClient implements Serializable
{
    private static final Logger logger = LoggerFactory.getLogger(MapRouletteClient.class);
    private static final long serialVersionUID = -8121247154514856056L;
    // This map contains the key which is "${projectName}-${challengeName}" mapping to the batch of
    // tasks for the given key
    private final Map<Tuple<String, String>, Set<Task>> batch;
    private final MapRouletteConfiguration configuration;
    private final TaskLoader connection;
    // Map containing all the challenges per project
    private final Map<String, Project> projects;
    private final Map<Long, Map<String, Challenge>> challenges;

    /**
     * Creates a {@link MapRouletteClient} from {@link MapRouletteConfiguration}.
     *
     * @param configuration
     *            {@link MapRouletteConfiguration} to use for connection
     * @return {@link MapRouletteClient} generated from {@link MapRouletteConfiguration}
     */
    public static MapRouletteClient instance(final MapRouletteConfiguration configuration)
    {
        // don't even try to initial a client if the configuration is not valid
        if (configuration != null && !configuration.getServer().isEmpty())
        {
            try
            {
                return new MapRouletteClient(configuration);
            }
            catch (final Exception e)
            {
                logger.warn(String.format(
                        "Failed to create MapRoulette client with [%s]. Will continue on, but will not push any tasks.",
                        configuration), e);
            }
        }

        return null;
    }

    /**
     * Construct
     *
     * @param configuration
     *            The MapRoulette {@link MapRouletteConfiguration} used to create the connection
     */
    public MapRouletteClient(final MapRouletteConfiguration configuration)
    {
        this(configuration, new MapRouletteConnection(configuration));
    }

    MapRouletteClient(final MapRouletteConfiguration configuration, final TaskLoader taskLoader)
    {
        this.batch = new ConcurrentHashMap<>();
        this.projects = new ConcurrentHashMap<>();
        this.challenges = new ConcurrentHashMap<>();
        this.configuration = configuration;
        this.connection = taskLoader;
    }

    public synchronized void addTask(final Challenge challenge, final Task task)
    {
        ProjectConfiguration projectConfiguration;
        if (this.configuration != null)
        {
            projectConfiguration = this.configuration.getProjectConfiguration();
            if (StringUtils.isEmpty(projectConfiguration.getName())
                    && StringUtils.isNotEmpty(task.getProjectName()))
            {
                projectConfiguration = new ProjectConfiguration(task.getProjectName());
            }
        }
        else
        {
            projectConfiguration = new ProjectConfiguration(task.getProjectName());
        }

        this.addTask(projectConfiguration, challenge, task);
    }

    /**
     * Adds tasks for a particular challenge name to the internal batch
     *
     * @param projectName
     *            The name of the project for the task
     * @param challenge
     *            The {@link Challenge} for this {@link Task}
     * @param task
     *            The task itself
     */
    public synchronized void addTask(final String projectName, final Challenge challenge,
            final Task task)
    {
        this.addTask(new ProjectConfiguration(projectName), challenge, task);
    }

    public synchronized void addTask(final ProjectConfiguration projectConfiguration,
            final Challenge challenge, final Task task)
    {
        task.setProjectName(projectConfiguration.getName());
        task.setChallengeName(challenge.getDisplayName());
        task.setCheckName(challenge.getName());
        updateChallengeTaskList(challenge, task, projectConfiguration);
    }

    public int getCurrentBatchSize()
    {
        int size = 0;
        for (final Tuple<String, String> key : this.batch.keySet())
        {
            size += this.batch.get(key).size();
        }
        return size;
    }

    /**
     * Upload batched tasks to MapRoulette
     */
    public void uploadTasks()
    {
        this.batch.forEach((key, value) -> this.upload(key));
    }

    public void uploadTasks(final Tuple<String, String> key)
    {
        this.upload(key);
    }

    private Optional<Challenge> createChallenge(final Project project, final Challenge challenge)
            throws UnsupportedEncodingException, URISyntaxException
    {
        final Map<String, Challenge> challengeMap = this.challenges.getOrDefault(project.getId(),
                new HashMap<>());
        challenge.setParentIdentifier(project.getId());
        if (!challengeMap.containsKey(challenge.getDisplayName()))
        {
            challenge.setId(this.connection.createChallenge(project, challenge));
            challengeMap.put(challenge.getDisplayName(), challenge);
            this.challenges.put(project.getId(), challengeMap);
        }
        return Optional.of(challenge);
    }

    private Project createProject(final ProjectConfiguration projectConfiguration)
            throws UnsupportedEncodingException, URISyntaxException
    {
        final String projectName = projectConfiguration.getName();
        final Project project = this.projects.getOrDefault(projectName,
                projectConfiguration.buildProject());
        if (project.getId() == -1)
        {
            project.setId(this.connection.createProject(project));
            this.projects.put(projectName, project);
        }
        return project;
    }

    private void updateChallengeTaskList(final Challenge challenge, final Task task,
            final ProjectConfiguration projectConfiguration) throws CoreException
    {
        final Tuple<String, String> taskKey = new Tuple<>(task.getProjectName(),
                challenge.getDisplayName());
        Set<Task> challengeTaskList = this.batch.get(taskKey);
        if (challengeTaskList == null)
        {
            challengeTaskList = new HashSet<>();
        }
        final Optional<Task> duplicateTask = challengeTaskList.stream()
                .filter(setTask -> StringUtils.equals(setTask.getTaskIdentifier(),
                        task.getTaskIdentifier()))
                .findAny();
        if (duplicateTask.isPresent())
        {
            logger.trace(
                    "Attempting to add a duplicate task to the batch with id {}, into Project '{}' and Challenge '{}'",
                    task.getTaskIdentifier(), task.getProjectName(), challenge.getDisplayName());
        }
        else
        {
            challengeTaskList.add(task);
            this.batch.put(taskKey, challengeTaskList);
            try
            {
                this.createChallenge(this.createProject(projectConfiguration), challenge);
            }
            catch (final Exception e)
            {
                logger.warn(String.format("Failed to create/update project structure for %s/%s.",
                        task.getProjectName(), challenge.getDisplayName()), e);
            }
        }
    }

    private void upload(final Tuple<String, String> key)
    {
        final Set<Task> batchList = this.batch.get(key);
        if (batchList != null && batchList.size() > 0)
        {
            try
            {
                // retrieve the already created project and challenge
                final Project project = this.projects.get(key.getFirst());
                if (project == null || project.getId() == -1)
                {
                    logger.warn(
                            "Failed to upload batch to MapRoulette, Project {} has not been created.",
                            key.getFirst());
                }
                else
                {
                    final Challenge challenge = this.challenges.get(project.getId())
                            .get(key.getSecond());
                    if (challenge == null || challenge.getId() == -1)
                    {
                        logger.warn(
                                "Failed to upload batch to MapRoulette, Challenge {} has not been created.",
                                key.getSecond());
                    }
                    else
                    {
                        this.connection.uploadBatchTasks(challenge.getId(), batchList);
                        batchList.clear();
                    }
                }
            }
            catch (final Exception e)
            {
                logger.warn(String.format("Failed to upload batch to MapRoulette [%s].",
                        this.connection.getConnectionInfo()), e);
            }
        }
    }
}
