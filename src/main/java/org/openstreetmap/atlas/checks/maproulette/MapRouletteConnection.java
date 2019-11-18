package org.openstreetmap.atlas.checks.maproulette;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.openstreetmap.atlas.checks.maproulette.data.Challenge;
import org.openstreetmap.atlas.checks.maproulette.data.Project;
import org.openstreetmap.atlas.checks.maproulette.data.Survey;
import org.openstreetmap.atlas.checks.maproulette.data.Task;
import org.openstreetmap.atlas.streaming.resource.http.GetResource;
import org.openstreetmap.atlas.streaming.resource.http.HttpResource;
import org.openstreetmap.atlas.streaming.resource.http.PostResource;
import org.openstreetmap.atlas.streaming.resource.http.PutResource;
import org.openstreetmap.atlas.utilities.runtime.Retry;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author cuthbertm
 */
public class MapRouletteConnection implements TaskLoader, Serializable
{
    private static final int DEFAULT_CONNECTION_RETRIES = 3;
    private static final int DEFAULT_CONNECTION_WAIT = 5;
    private static final String KEY_API_KEY = "apiKey";
    private static final String KEY_CHALLENGE = "challenge";
    private static final String KEY_SURVEY = "survey";
    private static final String KEY_ID = "id";
    private static final Logger logger = LoggerFactory.getLogger(MapRouletteConnection.class);
    private static final int MAXIMUM_BATCH_SIZE = 5000;
    private static final long serialVersionUID = -8227257938510897604L;
    private final MapRouletteConfiguration configuration;
    private final URIBuilder uriBuilder;

    MapRouletteConnection(final MapRouletteConfiguration configuration)
    {
        if (configuration == null || !isAbleToConnectToMapRoulette(configuration))
        {
            throw new IllegalArgumentException(
                    "configuration can't be null and must be able to connect to MapRouletteServers to create a connection.");
        }
        this.configuration = configuration;
        this.uriBuilder = new URIBuilder().setScheme(this.configuration.getScheme())
                .setHost(this.configuration.getServer()).setPort(this.configuration.getPort());
    }

    /**
     * Will create a challenge if it has not already been created
     *
     * @param getURI
     *            The URI used to retrieve the object
     * @param postURI
     *            The URI used to create the object
     * @param putURI
     *            The URI used to update the object
     * @param data
     *            The data of the object to create/update
     * @param logSuccessMessage
     *            The message to display on successful creation/update of object
     * @return The id of the created object
     * @throws UnsupportedEncodingException
     *             if cannot encode string for post/put to map roulette
     * @throws URISyntaxException
     *             if URI supplied is invalid and cannot be built
     */
    public long create(final String getURI, final String postURI, final String putURI,
            final JsonObject data, final String logSuccessMessage)
            throws UnsupportedEncodingException, URISyntaxException
    {
        HttpResource createUpdate = null;
        final GetResource challengeGet = new GetResource(this.uriBuilder.build().resolve(getURI));
        this.setAuth(challengeGet);
        try
        {
            final int statusCode = challengeGet.getStatusCode();
            if (statusCode == HttpStatus.SC_NOT_FOUND || statusCode == HttpStatus.SC_NO_CONTENT)
            {
                final URIBuilder baseUrl = this.uriBuilder.setPath(postURI);
                // generate the Challenge through the API
                createUpdate = new PostResource(baseUrl.build().toString());
                ((PostResource) createUpdate).setStringBody(data.toString(),
                        ContentType.APPLICATION_JSON);
            }
            // just make sure it is up to date in this case
            else
            {
                // get the ID directly from the response
                final long responseId = new Gson()
                        .fromJson(challengeGet.getRequestBodyAsString(), JsonObject.class)
                        .get(KEY_ID).getAsLong();
                final URIBuilder baseUrl = this.uriBuilder
                        .setPath(String.format(putURI, responseId));
                createUpdate = new PutResource(baseUrl.build().toString());
                data.add(KEY_ID, new JsonPrimitive(responseId));
                ((PutResource) createUpdate).setStringBody(data.toString(),
                        ContentType.APPLICATION_JSON);
            }
            this.setAuth(createUpdate);
            final int createUpdateStatus = createUpdate.getStatusCode();
            switch (createUpdateStatus)
            {
                case HttpStatus.SC_CREATED:
                case HttpStatus.SC_OK:
                    final long responseID = new Gson()
                            .fromJson(createUpdate.getRequestBodyAsString(), JsonObject.class)
                            .get("id").getAsLong();
                    logger.debug(logSuccessMessage, responseID);
                    return responseID;
                default:
                    logger.debug("{} - {}", createUpdate.getStatusCode(),
                            createUpdate.getRequestBodyAsString());
                    return -1;
            }
        }
        finally
        {
            challengeGet.close();
            if (createUpdate != null)
            {
                createUpdate.close();
            }
        }
    }

    @Override
    public long createChallenge(final Project project, final Challenge challenge)
            throws UnsupportedEncodingException, URISyntaxException
    {
        final JsonObject challengeJson = challenge.toJson(challenge.getDisplayName());
        final String type = challengeJson.has(Survey.KEY_ANSWERS) ? KEY_SURVEY : KEY_CHALLENGE;
        return create(
                String.format("/api/v2/project/%d/challenge/%s", project.getId(),
                        URLEncoder.encode(challenge.getDisplayName(), "UTF-8")),
                String.format("/api/v2/%s", type), String.format("/api/v2/%s/", type) + "%s",
                challengeJson, String.format("Created/Updated Challenge with ID {} and name %s",
                        challenge.getDisplayName()));
    }

    @Override
    public long createProject(final Project project)
            throws UnsupportedEncodingException, URISyntaxException
    {
        return create(String.format("/api/v2/projectByName/%s", project.getName()),
                "/api/v2/project", "/api/v2/project/%s", project.toJson(),
                String.format("Created/Updated Project with ID {} and name %s", project.getName()));
    }

    @Override
    public String getConnectionInfo()
    {
        return this.configuration.toString();
    }

    public HttpResource setAuth(final HttpResource resource)
    {
        resource.setHeader(KEY_API_KEY, this.configuration.getApiKey());
        return resource;
    }

    @Override
    public boolean uploadBatchTasks(final long challengeId, final Set<Task> data)
            throws UnsupportedEncodingException, URISyntaxException
    {
        final List<Task> uniqueTasks = new ArrayList<>(data.size());
        uniqueTasks.addAll(data);
        // MAXIMUM batch size is 5000, so if greater than 5000, we need to make multiple
        // requests in groups of 5000
        boolean succeeded = true;
        int startIndex = 0;
        int endIndex;
        do
        {
            endIndex = Math.min(startIndex + MAXIMUM_BATCH_SIZE, uniqueTasks.size());
            final List<Task> uploadList = uniqueTasks.subList(startIndex, endIndex);
            succeeded &= internalUploadBatchTasks(challengeId, uploadList);
            startIndex += MAXIMUM_BATCH_SIZE;
        }
        while (endIndex != uniqueTasks.size() - 1 && startIndex < uniqueTasks.size());
        return succeeded;
    }

    @Override
    public boolean uploadTask(final long challengeId, final Task task)
            throws UnsupportedEncodingException, URISyntaxException
    {
        final String challengeName = task.getChallengeName();
        final String taskIdentifier = task.getTaskIdentifier();
        logger.debug("Uploading task {} for challenge {}", taskIdentifier, challengeName);
        return uploadTask(challengeId, Collections.singletonList(task), true);
    }

    private boolean internalUploadBatchTasks(final long parentChallengeId, final List<Task> data)
            throws UnsupportedEncodingException, URISyntaxException
    {
        if (data.isEmpty())
        {
            return false;
        }
        logger.debug("Uploading batch of {} tasks for project/challenge {}/{}", data.size(),
                data.get(0).getProjectName(), data.get(0).getChallengeName());
        return uploadTask(parentChallengeId, data, true);
    }

    private boolean isAbleToConnectToMapRoulette(final MapRouletteConfiguration configuration)
    {
        return new Retry(DEFAULT_CONNECTION_RETRIES, Duration.seconds(DEFAULT_CONNECTION_WAIT))
                .run(() ->
                {
                    final String serverConnection = String.format("%s://%s:%s",
                            configuration.getScheme(), configuration.getServer(),
                            configuration.getPort());
                    final GetResource homepage = new GetResource(serverConnection);
                    final int statusCode = homepage.getStatusCode();
                    if (statusCode != HttpStatus.SC_OK)
                    {
                        logger.debug(
                                "Failed to connect to Map Roulette server [{}]. StatusCode: {}",
                                serverConnection, statusCode);
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                });
    }

    /**
     * Will upload a batch of tasks (or a single task) to a map roulette server
     *
     * @param parentChallengeId
     *            The ID of the challenge that is the parent of the list of tasks
     * @param tasks
     *            The list of tasks to upload
     * @param post
     *            Our modified version of maproulette changes the actions of POST and PUT on the
     *            batch upload process. Basically if you POST a batch, it will create any new tasks
     *            and ignore already created tasks, for a PUT it will create new tasks and update
     *            already created new tasks
     * @return the JSON payload used to create the tasks
     * @throws UnsupportedEncodingException
     * @throws URISyntaxException
     *             if the URI is build incorrectly
     */
    private boolean uploadTask(final long parentChallengeId, final List<Task> tasks,
            final boolean post) throws UnsupportedEncodingException, URISyntaxException
    {
        if (tasks.isEmpty())
        {
            logger.debug("No tasks supplied in list to upload");
            return false;
        }
        boolean uploaded = false;
        final JsonArray taskArray = new JsonArray();
        tasks.forEach(element -> taskArray.add(element.generateTask(parentChallengeId)));
        final HttpResource taskCreateUpdate;
        final URIBuilder builder = this.uriBuilder.setPath("/api/v2/tasks");
        if (post)
        {
            taskCreateUpdate = new PostResource(builder.build().toString());
            ((PostResource) taskCreateUpdate).setStringBody(taskArray.toString(),
                    ContentType.APPLICATION_JSON);
        }
        else
        {
            taskCreateUpdate = new PutResource(builder.build().toString());
            ((PutResource) taskCreateUpdate).setStringBody(taskArray.toString(),
                    ContentType.APPLICATION_JSON);
        }
        this.setAuth(taskCreateUpdate);
        final int createStatus = taskCreateUpdate.getStatusCode();
        switch (createStatus)
        {
            case HttpStatus.SC_CREATED:
                logger.debug("Created {} task(s) for challenge {}", tasks.size(),
                        tasks.get(0).getChallengeName());
                uploaded = true;
                break;
            case HttpStatus.SC_OK:
                logger.debug("Updated {} task(s) for challenge {}", tasks.size(),
                        tasks.get(0).getChallengeName());
                uploaded = true;
                break;
            default:
                logger.debug("{} - {}", taskCreateUpdate.getStatusCode(),
                        taskCreateUpdate.getRequestBodyAsString());
        }
        return uploaded;
    }
}
