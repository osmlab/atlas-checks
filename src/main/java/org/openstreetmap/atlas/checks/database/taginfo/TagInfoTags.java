package org.openstreetmap.atlas.checks.database.taginfo;

import java.util.Map;

/**
 * Class for tag info tags (key=value combinations)
 *
 * @author Taylor Smock
 */
public class TagInfoTags extends TagInfoKeyTagCommon
{
    private static final long serialVersionUID = 2560687923182627883L;
    private final String value;

    /**
     * Create a TagInfo object for the Tags table
     *
     * @param row
     *            The row to create the object from
     */
    public TagInfoTags(final Map<String, Object> row)
    {
        super(row);
        this.value = getAndCast(row, "value", String.class);
    }

    /**
     * @return The value for the tag
     */
    public String getValue()
    {
        return this.value;
    }
}
