package org.openstreetmap.atlas.checks.flag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.checks.utility.tags.SyntheticHighlightPointTag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.geojson.GeoJsonUtils;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * A flag for a {@code point} {@link Location} P*
 *
 * @author brian_l_davis
 * @author seancoulter
 */
public class FlaggedPoint extends FlaggedObject
{
    private static final Logger logger = LoggerFactory.getLogger(FlaggedPoint.class);
    private static final long serialVersionUID = -5912453173756416690L;
    private LocationItem locationItem;
    private final Location point;
    private final Map<String, String> properties;
    private final String country;

    @SuppressWarnings("unchecked")
    public FlaggedPoint(final Location point)
    {
        this.locationItem = null;
        this.point = point;
        // Add a synthetic tag to Point features used to highlight FlaggedObjects
        this.properties = Map.of(SyntheticHighlightPointTag.KEY,
                SyntheticHighlightPointTag.YES.name().toLowerCase());
        // This point is not necessarily from an atlas, thus we set its country to the default
        this.country = FlaggedObject.COUNTRY_MISSING;
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
        this.properties = this.initProperties(locationItem);
        this.country = this.initCountry(locationItem);
    }

    @Override
    public JsonObject asGeoJsonFeature(final String flagIdentifier)
    {
        final JsonObject geoJsonGeometry;
        final JsonObject geoJsonProperties;
        if (this.locationItem != null)
        {
            geoJsonGeometry = this.locationItem.asGeoJsonGeometry();
            geoJsonProperties = this.locationItem.getGeoJsonProperties();
        }
        else
        {
            geoJsonProperties = new JsonObject();
            geoJsonGeometry = this.point.asGeoJsonGeometry();
        }

        geoJsonProperties.addProperty("flag:id", flagIdentifier);
        geoJsonProperties.addProperty("flag:type", FlaggedPoint.class.getSimpleName());
        return GeoJsonUtils.feature(geoJsonGeometry, geoJsonProperties);
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
    public String getCountry()
    {
        return this.country;
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

    private String initCountry(final AtlasObject object)
    {
        final Map<String, String> tags = object.getTags();
        if (tags.containsKey(ISOCountryTag.KEY))
        {
            return tags.get(ISOCountryTag.KEY);
        }
        return ISOCountryTag.COUNTRY_MISSING;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> initProperties(final LocationItem locationItem)
    {
        final Map<String, String> tags = new HashMap<>(locationItem.getTags());
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
