package org.openstreetmap.atlas.checks.maproulette;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.checks.maproulette.data.ProjectConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cuthbertm
 * @author mgostintsev
 * @author nachtm
 */
public class MapRouletteConfiguration implements Serializable
{
    private static final int API_KEY_INDEX = 3;
    private static final int NUMBER_OF_COMPONENTS = 4;
    private static final int PORT_INDEX = 1;
    private static final int PROJECT_NAME_INDEX = 2;
    private static final int SERVER_INDEX = 0;
    private static final Logger logger = LoggerFactory.getLogger(MapRouletteConfiguration.class);
    private static final long serialVersionUID = -1060265212173405828L;
    private static final String DELIMITER = ":";
    private final String apiKey;
    private final int port;
    private final String server;
    private final ProjectConfiguration projectConfiguration;

    /**
     * Parses a map roulette configuration object from a string that follows the structure
     * [SERVER]:[PORT]:[PROJECT_NAME]:[API_KEY]
     *
     * @param configuration
     *            The configuration string to parse
     * @return A valid Map Roulette Configuration object, null returned if the configuration string
     *         is not valid
     */
    public static MapRouletteConfiguration parse(final String configuration)
    {
        if (StringUtils.isNotEmpty(configuration))
        {
            final List<String> components = new LinkedList<>(Arrays.asList(configuration.split(DELIMITER)));
            if (components.size() == NUMBER_OF_COMPONENTS)
            {
                final ProjectConfiguration projectConfiguration = new ProjectConfiguration(
                        components.get(PROJECT_NAME_INDEX));
                components.remove(PROJECT_NAME_INDEX);
                return MapRouletteConfiguration.parseWithProject(components, projectConfiguration);
            }
        }
        logger.debug(
                String.format("Map Roulette configuration not set, invalid string passed in. [%s]",
                        configuration));
        return null;
    }

    /**
     * Parses a map roulette configuration object from a string that follows the structure
     * [SERVER]:[PORT]:[API_KEY], taking information about the particular project from
     * projectConfiguration.
     * 
     * @param configuration
     *            The configuration string to parse
     * @param projectConfiguration
     *            The details of the project to which we will be uploading.
     * @return A valid Map Roulette Configuration object, null returned if the configuration string
     *         is not valid.
     */
    public static MapRouletteConfiguration parseWithProject(final String configuration,
            final ProjectConfiguration projectConfiguration)
    {
        if (StringUtils.isNotEmpty(configuration))
        {
            final List<String> components = Arrays.asList(configuration.split(DELIMITER));
            return MapRouletteConfiguration.parseWithProject(components, projectConfiguration);
        }
        logger.debug("Map Roulette configuration not set, empty configuration string.");
        return null;
    }

    /**
     * After some pre-processing has been performed, initialize a Map Roulette Configuration object
     * from some network information and a project configuration.
     *
     * @param components
     *            An ordered list of components defining a connection to Map Roulette. Requires that
     *            server is at SERVER_INDEX, port is at PORT_INDEX, and the api key is at
     *            API_KEY_INDEX - 1.
     * @param configuration
     *            A ProjectConfiguration defining the project that this connection will connect to
     *            on Map Roulette
     * @return A valid Map Roulette Configuration object, null returned if the configuration string
     *         is not valid.
     */
    private static MapRouletteConfiguration parseWithProject(final List<String> components,
            final ProjectConfiguration configuration)
    {
        // TODO clean up indexing/length comparison
        if (components.size() == NUMBER_OF_COMPONENTS - 1)
        {
            return new MapRouletteConfiguration(components.get(SERVER_INDEX),
                    Integer.parseInt(components.get(PORT_INDEX)), configuration,
                    components.get(API_KEY_INDEX - 1));
        }
        logger.debug(String.format(
                "Map Roulette configuration not set, invalid number of arguments. [%s]",
                String.join(DELIMITER, components)));
        return null;
    }

    public MapRouletteConfiguration(final String server, final int port, final String projectName,
            final String apiKey)
    {
        this(server, port, new ProjectConfiguration(projectName), apiKey);
    }

    public MapRouletteConfiguration(final String server, final int port,
            final ProjectConfiguration projectConfiguration, final String apiKey)
    {
        this.server = server;
        this.port = port;
        this.projectConfiguration = projectConfiguration;
        this.apiKey = apiKey;
    }

    public String getApiKey()
    {
        return this.apiKey;
    }

    public int getPort()
    {
        return this.port;
    }

    public String getProjectName()
    {
        return this.projectConfiguration.getName();
    }

    public String getServer()
    {
        return this.server;
    }

    public ProjectConfiguration getProjectConfiguration()
    {
        return this.projectConfiguration;
    }

    @Override
    public String toString()
    {
        return String.format("%s:%d:%s:%s", this.server, this.port,
                this.projectConfiguration.getName(), this.apiKey);
    }
}
