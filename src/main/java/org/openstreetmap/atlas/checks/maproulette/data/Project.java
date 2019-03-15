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
    private long id = -1;
    private final String name;
    private final String description;
    private final String displayName;

    public Project(final String name)
    {
        this.name = name;
        this.description = name;
        this.displayName = name;
    }

    public Project(final String name, final String description)
    {
        this.name = name;
        this.description = description;
        this.displayName = name;
    }

    public Project(final String name, final String description, final String displayName)
    {
        this.name = name;
        this.description = description;
        this.displayName = displayName;
    }

    public long getId()
    {
        return id;
    }

    public void setId(final long identifier)
    {
        this.id = identifier;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public JsonObject toJson()
    {
        return new GsonBuilder().disableHtmlEscaping().create().toJsonTree(this).getAsJsonObject();
    }
}
