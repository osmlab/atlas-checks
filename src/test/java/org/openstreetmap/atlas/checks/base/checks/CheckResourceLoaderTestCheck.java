package org.openstreetmap.atlas.checks.base.checks;

import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * A simple {@link BaseCheck} that is similar to {@link BaseTestCheck} however instead of returning
 * true and a flag, it returns false and null.
 * 
 * @author brian_l_davis
 */
public class CheckResourceLoaderTestCheck extends BaseCheck<Long>
{
    /**
     * Default constructor
     * 
     * @param configuration
     *            {@link Configuration} that will be used for testing configuration changes for the
     *            ResourceLoader
     */
    public CheckResourceLoaderTestCheck(final Configuration configuration)
    {
        super(configuration);
    }

    /**
     * Always return false for this specific unit test base check
     * 
     * @param object
     *            The {@link AtlasObject} you are checking
     * @return false
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return false;
    }

    /**
     * This function will never be called, because the {@link this#validCheckForObject(AtlasObject)}
     * always returns false
     * 
     * @param object
     *            The {@link AtlasObject} being processed
     * @return null always
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        return null;
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return null;
    }
}
