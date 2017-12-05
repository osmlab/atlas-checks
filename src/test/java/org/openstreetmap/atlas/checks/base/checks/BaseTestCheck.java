package org.openstreetmap.atlas.checks.base.checks;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * A class that implements the base methods so as to easily be used for unit tests
 * 
 * @author cuthbertm
 */
public class BaseTestCheck extends BaseCheck<Long>
{
    /**
     * Default constructor, that simply delegates to the super class
     * 
     * @param configuration
     *            {@link Configuration}, which in our test case generally would not be used
     */
    public BaseTestCheck(final Configuration configuration)
    {
        super(configuration);
    }

    /**
     * For unit tests this would always return true no matter what
     * 
     * @param object
     *            The {@link AtlasObject} you are checking
     * @return true
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return true;
    }

    /**
     * For unit tests we always create a flag with just some default text as the instruction
     * 
     * @param object
     *            The object being checked
     * @return A default flag for unit tests
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0)));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return Arrays.asList("Default Instruction");
    }
}
