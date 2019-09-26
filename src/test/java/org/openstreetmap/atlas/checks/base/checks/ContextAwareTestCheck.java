package org.openstreetmap.atlas.checks.base.checks;

import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * A simple test Check that stores some extra data passed in through the constructor.
 *
 * @author nachtm
 */
public class ContextAwareTestCheck extends BaseCheck<Long>
{
    private final int data;

    public ContextAwareTestCheck(final Configuration configuration, final int otherData)
    {
        super(configuration);
        this.data = otherData;
    }

    public int getData()
    {
        return this.data;
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return false;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        return Optional.empty();
    }
}
