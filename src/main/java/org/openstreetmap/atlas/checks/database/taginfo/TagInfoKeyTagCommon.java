package org.openstreetmap.atlas.checks.database.taginfo;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Common fields for Tags and Keys from TagInfo
 *
 * @author Taylor Smock
 */
public abstract class TagInfoKeyTagCommon implements Serializable
{
    private static final long serialVersionUID = -2248695610153869229L;
    private final String key;
    private final Number countAll;
    private final Number countNodes;
    private final Number countWays;
    private final Number countRelations;
    private final boolean inWiki;
    private final boolean inWikiEn;

    /**
     * Get and cast a specified key from a row map
     *
     * @param <T>
     *            The type to return
     * @param row
     *            The row from the database
     * @param key
     *            The key to get from the map
     * @param clazz
     *            The class of the return object
     * @return {@code null} if the key does not exist in the map, or could not be cast.
     */
    @Nullable
    protected static <T> T getAndCast(final Map<String, Object> row, final String key,
            final Class<T> clazz)
    {
        if (row.containsKey(key) && clazz.isInstance(row.get(key)))
        {
            return clazz.cast(row.get(key));
        }
        return null;
    }

    protected TagInfoKeyTagCommon(final Map<String, Object> row)
    {
        this.key = getAndCast(row, "key", String.class);
        this.countAll = getAndCast(row, "count_all", Number.class);
        this.countNodes = getAndCast(row, "count_nodes", Number.class);
        this.countWays = getAndCast(row, "count_ways", Number.class);
        this.countRelations = getAndCast(row, "count_relations", Number.class);
        this.inWiki = Boolean.TRUE.equals(getAndCast(row, "in_wiki", Boolean.class));
        this.inWikiEn = Boolean.TRUE.equals(getAndCast(row, "in_wiki_en", Boolean.class));
    }

    /**
     * @return The count of the occurrences
     */
    public Number getCountAll()
    {
        return this.countAll;
    }

    /**
     * @return The count of the occurrences on nodes
     */
    public Number getCountNodes()
    {
        return this.countNodes;
    }

    /**
     * @return The count of the occurrences on relations
     */
    public Number getCountRelations()
    {
        return this.countRelations;
    }

    /**
     * @return The count of the occurrences on ways
     */
    public Number getCountWays()
    {
        return this.countWays;
    }

    /**
     * @return The key for the tag
     */
    public String getKey()
    {
        return this.key;
    }

    /**
     * @return {@code true} if in the OSM wiki
     */
    public boolean isInWiki()
    {
        return this.inWiki;
    }

    /**
     * @return {@code true} if in the English OSM wiki
     */
    public boolean isInWikiEn()
    {
        return this.inWikiEn;
    }
}
