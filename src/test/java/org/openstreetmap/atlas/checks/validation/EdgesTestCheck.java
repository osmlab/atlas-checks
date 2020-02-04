package org.openstreetmap.atlas.checks.validation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * A check for testing purposes. Flags all Edges.
 *
 * @author bbreithaupt
 */
public class EdgesTestCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -4874724193461354673L;
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList("Test check flag, please ignore.");

    public EdgesTestCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public Optional<CheckFlag> flag(final AtlasObject object)
    {
        return Optional.of(this.createFlag(object, this.getFallbackInstructions().get(0)));
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge;
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
