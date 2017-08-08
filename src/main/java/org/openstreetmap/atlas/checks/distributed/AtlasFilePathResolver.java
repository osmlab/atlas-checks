package org.openstreetmap.atlas.checks.distributed;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This resolver obscures whether or not the {@code basePath} points to a path containing
 * {@link Atlas} files or country folders that contain the {@link Atlas} files. Path templates are
 * configured through a key-value map, mapping {@code schema} to path template. The path template
 * follows {@link String#format} rules, given a base path and country code to determine the final
 * path. <br>
 * <br>
 * Sample Configuration:
 * 
 * <pre>
 * {
 *   "AtlasFilePathResolver": {
 *     "schema.path.templates": {
 *         "file": "%s"
 *         "http": "%s/%s"
 *     }
 *   }
 * }
 * </pre>
 *
 * If no schema templates are found, country specific sub-folders are assumed and {@code "%s/%s"} is
 * used.
 *
 * @author mgostintsev
 * @author brian_l_davis
 */
public final class AtlasFilePathResolver implements Serializable
{
    private static final long serialVersionUID = -6293318204002353071L;
    private static final String DEFAULT_PATH_TEMPLATE = "%s/%s";
    private final Map<String, String> pathTemplate;

    /**
     * Default constructor
     *
     * @param configuration
     *            The {@link Configuration}
     */
    public AtlasFilePathResolver(final Configuration configuration)
    {
        this.pathTemplate = Collections
                .unmodifiableMap(
                        configuration
                                .get(AtlasFilePathResolver.class.getSimpleName()
                                        + ".schema.path.templates", Collections.emptyMap())
                                .value());
    }

    /**
     * @param basePath
     *            the base directory, which contains either the country directories or the atlas
     *            files themselves
     * @param country
     *            the atlas file country we're interested in
     * @return the correct path to look for Atlas files, based on the base path backend
     */
    public String resolvePath(final String basePath, final String country)
    {
        final URI uri = URI.create(basePath);
        final String template = Optional.ofNullable(uri.getScheme()).map(pathTemplate::get)
                .orElse(pathTemplate.getOrDefault("default", DEFAULT_PATH_TEMPLATE));

        return String.format(template, basePath, country);
    }
}
