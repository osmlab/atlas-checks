package org.openstreetmap.atlas.checks.maproulette.data;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * Very basic class defining the structure of the MapRoulette Project
 * 
 * @author cuthbertm
 * @author nachtm
 */
public class Project
{
    @SuppressWarnings("checkstyle:memberName")
    private long identifier = -1;
    private final String name;
    private final String description;
    private final String displayName;
    private final boolean enabled;

    public Project(final String name)
    {
        this.name = name;
        this.description = name;
        this.displayName = name;
        this.enabled = true;
    }

    public Project(final String name, final String description)
    {
        this.name = name;
        this.description = description;
        this.displayName = name;
        this.enabled = true;
    }

    public Project(final String name, final String description, final String displayName,
            final boolean enabled)
    {
        this.name = name;
        this.description = description;
        this.displayName = displayName;
        this.enabled = enabled;
    }

    public String getDescription()
    {
        return this.description;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public long getId()
    {
        return this.identifier;
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void setId(final long identifier)
    {
        this.identifier = identifier;
    }

    public JsonObject toJson()
    {
        return new GsonBuilder().disableHtmlEscaping().create().toJsonTree(this).getAsJsonObject();
    }
}
