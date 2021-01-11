package org.openstreetmap.atlas.checks.maproulette.data;

import java.io.Serializable;

/**
 * Helper class to decouple MapRouletteConfiguration from configuring projects. This allows easier
 * updating of code that handles projects without touching general Map Roulette configuration code.
 *
 * @author nachtm
 */
public class ProjectConfiguration implements Serializable
{
    private static final long serialVersionUID = 7825943800619520476L;
    private final String name;
    private final String description;
    private final String displayName;
    private boolean enabled;

    /**
     * Defines a basic project, where all optional fields default to name.
     * 
     * @param name
     *            The name of the project
     */
    public ProjectConfiguration(final String name)
    {
        this.name = name;
        this.description = name;
        this.displayName = name;
        this.enabled = true;
    }

    /**
     * Defines a project and all of its fields.
     * 
     * @param name
     *            The name of the project
     * @param description
     *            The description of the project
     * @param displayName
     *            The name displayed on Map Roulette for the project
     * @param enabled
     *            Whether the project is enabled or not
     */
    public ProjectConfiguration(final String name, final String description,
            final String displayName, final boolean enabled)
    {
        this.name = name;
        this.description = description;
        this.displayName = displayName;
        this.enabled = enabled;
    }

    /**
     * Initialize a project defined by this configuration.
     * 
     * @return A new project with the parameters held in this configuration.
     */
    public Project buildProject()
    {
        return new Project(this.name, this.description, this.displayName, this.enabled);
    }

    public String getDescription()
    {
        return this.description;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled)
    {
        this.enabled = enabled;
    }
}
