package org.openstreetmap.atlas.checks.utility;

import java.util.EnumMap;

import javax.annotation.Nullable;

import org.openstreetmap.atlas.checks.validation.relations.InvalidMultiPolygonRelationCheck;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * Convert Atlas ItemTypes into OSM types
 *
 * @author Taylor Smock, extracted from {@link InvalidMultiPolygonRelationCheck}.
 */
public final class AtlasToOsmType
{
    private static final EnumMap<ItemType, String> ATLAS_TO_OSM_TYPE = new EnumMap<>(
            ItemType.class);
    static
    {
        ATLAS_TO_OSM_TYPE.put(ItemType.EDGE, "way");
        ATLAS_TO_OSM_TYPE.put(ItemType.AREA, "way");
        ATLAS_TO_OSM_TYPE.put(ItemType.LINE, "way");
        ATLAS_TO_OSM_TYPE.put(ItemType.NODE, "node");
        ATLAS_TO_OSM_TYPE.put(ItemType.POINT, "node");
        ATLAS_TO_OSM_TYPE.put(ItemType.RELATION, "relation");

    }

    /**
     * Convert an Atlas {@link ItemType} into an OSM type
     *
     * @param itemType
     *            The itemtype to convert
     * @return The string for that item type ("node", "way", "relation", or {@code null} if unknown)
     */
    @Nullable
    public static String convert(final ItemType itemType)
    {
        return ATLAS_TO_OSM_TYPE.getOrDefault(itemType, null);
    }

    private AtlasToOsmType()
    {
        // utility class
    }
}
