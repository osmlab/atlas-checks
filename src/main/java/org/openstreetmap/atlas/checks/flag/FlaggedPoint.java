package org.openstreetmap.atlas.checks.flag;

import java.util.Collections;
import java.util.Map;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.geojson.GeoJsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * A flag for a {@code point} {@link Location} P*
 * 
 * @author brian_l_davis
 */
public class FlaggedPoint extends FlaggedObject
{
    private static final long serialVersionUID = -5912453173756416690L;
    private static final Logger logger = LoggerFactory.getLogger(FlaggedPoint.class);
    private final Location point;
    private final Map<String, String> properties;

    private final LocationItem locationItem;

    /**
     * Default constructor
     * 
     * @param locationItem
     *            the {@link LocationItem} to flag
     */
    public FlaggedPoint(final LocationItem locationItem)
    {
        this.locationItem = locationItem;
        this.point = locationItem.getLocation();
        this.properties = initProperties(locationItem);
    }

    @SuppressWarnings("unchecked")
    public FlaggedPoint(final Location point)
    {
        this.locationItem = null;
        this.point = point;
        this.properties = Collections.EMPTY_MAP;
    }

    @Override
    public Iterable<Location> getGeometry()
    {
        return this.point;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getProperties()
    {
        return this.properties;
    }

    @Override
    public JsonObject asGeoJsonFeature(final String flagIdentifier)
    {
        final JsonObject feature;
        final JsonObject properties;
        if (locationItem != null)
        {
            feature = locationItem.asGeoJsonGeometry();
            properties = feature.getAsJsonObject("properties");
        }
        else
        {
            properties = new JsonObject();
            feature = GeoJsonUtils.feature(point.asGeoJsonGeometry(), properties);
        }

        properties.addProperty("flag:id", flagIdentifier);
        properties.addProperty("flag:type", FlaggedPoint.class.getSimpleName());

        return feature;
    }

    @Override
    public Rectangle bounds()
    {
        if (locationItem != null)
        {
            return locationItem.bounds();
        }
        else
        {
            return point.bounds();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> initProperties(final LocationItem locationItem)
    {
        final Map<String, String> tags = locationItem.getTags();
        tags.put(ITEM_IDENTIFIER_TAG, locationItem.getIdentifier() + "");
        tags.put(OSM_IDENTIFIER_TAG, locationItem.getOsmIdentifier() + "");
        if (locationItem instanceof Node)
        {
            tags.put(ITEM_TYPE_TAG, NODE_TAG);
        }
        else if (locationItem instanceof Point)
        {
            tags.put(ITEM_TYPE_TAG, POINT_TAG);
        }
        else
        {
            logger.warn("Flagged LocationItem with unknown item type {}", locationItem);
        }
        return tags;
    }
}
