package org.openstreetmap.atlas.checks.flag;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
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
    private static final Logger logger = LoggerFactory.getLogger(FlaggedPoint.class);
    private static final long serialVersionUID = -5912453173756416690L;
    private LocationItem locationItem;
    private final Location point;
    private final Map<String, String> properties;

    @SuppressWarnings("unchecked")
    public FlaggedPoint(final Location point)
    {
        this.locationItem = null;
        this.point = point;
        this.properties = Collections.EMPTY_MAP;
    }

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

    @Override
    public JsonObject asGeoJsonFeature(final String flagIdentifier)
    {
        final JsonObject geoJsonGeometry;
        final JsonObject properties;
        if (this.locationItem != null)
        {
            geoJsonGeometry = this.locationItem.asGeoJsonGeometry();
            properties = this.locationItem.getGeoJsonProperties();
        }
        else
        {
            properties = new JsonObject();
            geoJsonGeometry = this.point.asGeoJsonGeometry();
        }

        properties.addProperty("flag:id", flagIdentifier);
        properties.addProperty("flag:type", FlaggedPoint.class.getSimpleName());
        return GeoJsonUtils.feature(geoJsonGeometry, properties);
    }

    @Override
    public Rectangle bounds()
    {
        if (this.locationItem != null)
        {
            return this.locationItem.bounds();
        }
        else
        {
            return this.point.bounds();
        }
    }

    @Override
    public FlaggedObject getAsCompleteFlaggedObject()
    {
        if (this.locationItem instanceof Point)
        {
            this.locationItem = CompletePoint.from((Point) this.locationItem);
        }
        else if (this.locationItem instanceof Node)
        {
            this.locationItem = CompleteNode.from((Node) this.locationItem);
        }
        return this;
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
    protected Optional<AtlasObject> getObject()
    {
        return Optional.ofNullable(this.locationItem);
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
