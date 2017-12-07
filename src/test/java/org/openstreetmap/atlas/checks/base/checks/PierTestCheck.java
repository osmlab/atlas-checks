package org.openstreetmap.atlas.checks.base.checks;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * The pier skip check will just look for the highway=primary and flag it
 *
 * @author brian_l_davis
 */
public class PierTestCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 8535115005439595519L;

    /**
     * Default constructor
     * 
     * @param configuration
     *            The {@link Configuration} object that will be used when changing the different
     *            values for the piers
     */
    public PierTestCheck(final Configuration configuration)
    {
        super(configuration);
    }

    /**
     * A pier is always an edge, so check for the instance of the object being an edge.
     * 
     * @param object
     *            The {@link AtlasObject} you are checking
     * @return true if {@link AtlasObject} is an {@link Edge}
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge;
    }

    /**
     * Creates a simple flag, that will replace any Highway tag found in the {@link AtlasObject}
     * being processed with the value "primary"
     * 
     * @param object
     *            The {@link AtlasObject} being processed
     * @return A flag containing generic text
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Map<String, String> tags = object.getTags();
        tags.computeIfPresent(HighwayTag.KEY, (key, value) -> "primary");
        return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0)));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return Arrays.asList("test");
    }
}
