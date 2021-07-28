package org.openstreetmap.atlas.checks.database.wikidata;

/**
 * An interface with common wiki item and wiki property values
 *
 * @author Taylor Smock
 */
public interface WikiItemInterface
{
    /**
     * @return The base description for the wiki item
     */
    String getDescriptor();

    /**
     * @return The unique ID for the wiki item
     */
    String getId();
}
