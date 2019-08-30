package org.openstreetmap.atlas.checks.base.checks;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import java.util.Optional;

public class ContextAwareTestCheck extends BaseCheck<Long>
{
    private final int data;

    public ContextAwareTestCheck(final Configuration configuration, final int otherData)
    {
        super(configuration);
        this.data = otherData;
    }

    @Override public boolean validCheckForObject(final AtlasObject object)
    {
        return false;
    }

    @Override protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        return Optional.empty();
    }

    public int getData()
    {
        return this.data;
    }
}
