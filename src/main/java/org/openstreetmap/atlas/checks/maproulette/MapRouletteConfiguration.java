package org.openstreetmap.atlas.checks.maproulette;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.openstreetmap.atlas.checks.maproulette.data.ProjectConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cuthbertm
 * @author mgostintsev
 * @author nachtm
 * @author bbreithaupt
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
    private static final String DELIMITER = "(?<!https|http):";
    private static final String SCHEME_DELIMITER = "://";
    private final String apiKey;
    private final int port;
    private final String scheme;
    private final String server;
    private final ProjectConfiguration projectConfiguration;

    /**
     * Parses a map roulette configuration object from a string that follows one of these structures
     * [SERVER]:[PORT]:[PROJECT_NAME]:[API_KEY] [SCHEME]://[SERVER]:[PORT]:[PROJECT_NAME]:[API_KEY]
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
            final String[] components = configuration.split(DELIMITER);
            if (components.length == NUMBER_OF_COMPONENTS)
            {
                final String scheme;
                final String server;
                final String[] splitServer = components[SERVER_INDEX].split(SCHEME_DELIMITER);
                if (splitServer.length < 2)
                {
                    scheme = HttpHost.DEFAULT_SCHEME_NAME;
                    server = components[SERVER_INDEX];
                }
                else
                {
                    scheme = splitServer[0];
                    server = splitServer[1];
                }
                return new MapRouletteConfiguration(scheme, server,
                        Integer.parseInt(components[PORT_INDEX]),
                        new ProjectConfiguration(components[PROJECT_NAME_INDEX]),
                        components[API_KEY_INDEX]);
            }
        }
        logger.debug("Map Roulette configuration not set, invalid string passed in. [{}]",
                configuration);
        return null;
    }

    public MapRouletteConfiguration(final String server, final int port, final String projectName,
            final String apiKey)
    {
        this(HttpHost.DEFAULT_SCHEME_NAME, server, port, new ProjectConfiguration(projectName),
                apiKey);
    }

    public MapRouletteConfiguration(final String server, final int port,
            final ProjectConfiguration projectConfiguration, final String apiKey)
    {
        this(HttpHost.DEFAULT_SCHEME_NAME, server, port, projectConfiguration, apiKey);
    }

    public MapRouletteConfiguration(final String scheme, final String server, final int port,
            final ProjectConfiguration projectConfiguration, final String apiKey)
    {
        this.scheme = scheme;
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

    public String getScheme()
    {
        return this.scheme;
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
        return String.format("%s://%s:%d:%s:%s", this.scheme, this.server, this.port,
                this.projectConfiguration.getName(), this.apiKey);
    }
}
