package org.openstreetmap.atlas.checks.maproulette;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Set;

import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.Project;
import org.openstreetmap.atlas.checks.maproulette.data.Task;

/**
 * Interface with various methods required to upload tasks to MapRoulette
 *
 * @author cstaylor
 * @author cuthbertm
 */
public interface TaskLoader
{
    /**
     * Creates a new challenge with the given {@link Challenge}, if already exists then will attempt
     * to update
     *
     * @param project
     *            The parent project object of the challenge
     * @param challenge
     *            The challenge to create or update
     * @return The id for the challenge
     * @throws UnsupportedEncodingException
     *             if json data for API payload cannot be encoded correctly
     * @throws URISyntaxException
     *             if the URI for getting/creating/updating project is incorrectly generated
     */
    long createChallenge(Project project, Challenge challenge)
            throws UnsupportedEncodingException, URISyntaxException;

    /**
     * Creates a new project with the {@link Project}, if already exists then will attempt to update
     *
     * @param project
     *            The project object containing name and description
     * @return The id for the project
     * @throws UnsupportedEncodingException
     *             if json data for API payload cannot be encoded correctly
     * @throws URISyntaxException
     *             if the URI for getting/creating/updating project is incorrectly generated
     */
    long createProject(Project project) throws UnsupportedEncodingException, URISyntaxException;

    /**
     * Retrieve the connection info for this connection
     *
     * @return A string
     */
    String getConnectionInfo();

    /**
     * Uploads tasks as a batch, instead of one at a time
     *
     * @param challengeId
     *            The id of the parent challenge
     * @param tasks
     *            The list of tasks that should be uploaded for the challenge
     * @return The JSON payload used to upload the task
     * @throws UnsupportedEncodingException
     *             Exception could be thrown when you try and upload the batch.
     * @throws URISyntaxException
     *             if the URI cannot be built correctly
     */
    boolean uploadBatchTasks(long challengeId, Set<Task> tasks)
            throws UnsupportedEncodingException, URISyntaxException;

    /**
     * Uploads a task to MapRoulette
     *
     * @param challengeId
     *            The id of the parent challenge
     * @param task
     *            The JSON payload used to upload the task
     * @return true if the task was uploaded successfully
     * @throws UnsupportedEncodingException
     *             If the JSON is invalid, which it never should be
     * @throws URISyntaxException
     *             if the URI cannot be built correctly
     */
    boolean uploadTask(long challengeId, Task task)
            throws UnsupportedEncodingException, URISyntaxException;
}
