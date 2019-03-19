package org.openstreetmap.atlas.checks.maproulette.data;

/**
 * Helper class to decouple MapRouletteConfiguration from configuring projects. This allows easier
 * updating of code that handles projects without touching general Map Roulette configuration code.
 *
 * @author nachtm
 */
public class ProjectConfiguration
{
    private String name;
    private String description;
    private String displayName;
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

    public String getName()
    {
        return this.name;
    }

    public String getDescription()
    {
        return this.description;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }
}
