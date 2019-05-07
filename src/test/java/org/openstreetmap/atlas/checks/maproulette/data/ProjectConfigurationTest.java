package org.openstreetmap.atlas.checks.maproulette.data;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for ProjectConfiguration
 *
 * @author nachtm
 */
public class ProjectConfigurationTest
{

    private static final String NAME = "test_project";
    private static final String DISPLAY_NAME = "Test Project";
    private static final String DESCRIPTION = "A test project.";
    private static final boolean ENABLED = true;

    @Test
    public void buildProject()
    {
        final ProjectConfiguration configuration = new ProjectConfiguration(NAME, DISPLAY_NAME,
                DESCRIPTION, ENABLED);
        final Project built = configuration.buildProject();
        Assert.assertEquals(configuration.getName(), built.getName());
        Assert.assertEquals(configuration.getDescription(), built.getDescription());
        Assert.assertEquals(configuration.getDisplayName(), built.getDisplayName());
        Assert.assertEquals(configuration.isEnabled(), built.isEnabled());
    }
}
