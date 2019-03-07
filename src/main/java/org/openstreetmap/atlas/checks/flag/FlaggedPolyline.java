package org.openstreetmap.atlas.checks.flag;

import java.util.Map;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.tags.ISOCountryTag;

import com.google.gson.JsonObject;

/**
 * A flag for a {@link PolyLine}
 *
 * @author brian_l_davis
 */
public class FlaggedPolyline extends FlaggedObject
{
    private static final long serialVersionUID = -1184306312148054279L;

    private final AtlasItem atlasItem;
    private final String country;
    private final PolyLine polyLine;
    private final Map<String, String> properties;

    /**
     * Default constructor
     *
     * @param atlasItem
     *            the {@link AtlasItem} to flag
     */
    public FlaggedPolyline(final AtlasItem atlasItem)
    {
        this.atlasItem = atlasItem;
        if (atlasItem instanceof Area)
        {
            // We should actually have a FlaggedPolygon class with a polygon in it instead of just
            // making polygons be PolyLines. I am not sure why FlaggedPolygon was not implemented.
            this.polyLine = new PolyLine(((Area) atlasItem).asPolygon().closedLoop());
        }
        else
        {
            this.polyLine = new PolyLine(atlasItem.getRawGeometry());
        }
        this.properties = initProperties(atlasItem);
        this.country = initCountry(atlasItem);
    }

    @Override
    public JsonObject asGeoJsonFeature(final String flagIdentifier)
    {
        final JsonObject feature = this.atlasItem.asGeoJsonGeometry();
        final JsonObject properties = this.atlasItem.getGeoJsonProperties();

        properties.addProperty("flag:id", flagIdentifier);
        properties.addProperty("flag:type", FlaggedPolyline.class.getSimpleName());
        feature.add("properties", properties);
        return feature;
    }

    @Override
    public Rectangle bounds()
    {
        return this.atlasItem.bounds();
    }

    @Override
    public String getCountry()
    {
        return this.country;
    }

    @Override
    public Iterable<Location> getGeometry()
    {
        return this.polyLine;
    }

    @Override
    public Map<String, String> getProperties()
    {
        return this.properties;
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

    private Map<String, String> initProperties(final AtlasObject object)
    {
        final Map<String, String> tags = object.getTags();
        tags.put(ITEM_IDENTIFIER_TAG, object.getIdentifier() + "");
        tags.put(OSM_IDENTIFIER_TAG, object.getOsmIdentifier() + "");
        if (object instanceof Area)
        {
            tags.put(ITEM_TYPE_TAG, AREA_TAG);
        }
        else if (object instanceof Line)
        {
            tags.put(ITEM_TYPE_TAG, LINE_TAG);
        }
        else if (object instanceof Edge)
        {
            tags.put(ITEM_TYPE_TAG, EDGE_TAG);
        }
        else if (object instanceof Node)
        {
            tags.put(ITEM_TYPE_TAG, NODE_TAG);
        }
        else if (object instanceof Point)
        {
            tags.put(ITEM_TYPE_TAG, POINT_TAG);
        }
        return tags;
    }
}
