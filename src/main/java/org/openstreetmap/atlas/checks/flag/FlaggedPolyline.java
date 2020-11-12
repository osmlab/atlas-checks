package org.openstreetmap.atlas.checks.flag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.geojson.GeoJsonUtils;
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
        this.properties = this.initProperties(atlasItem);
        this.country = this.initCountry(atlasItem);
    }

    @Override
    public JsonObject asGeoJsonFeature(final String flagIdentifier)
    {
        final JsonObject geoJsonGeometry = this.atlasItem.asGeoJsonGeometry();
        final JsonObject jsonProperties = this.atlasItem.getGeoJsonProperties();
        jsonProperties.addProperty("flag:id", flagIdentifier);
        jsonProperties.addProperty("flag:type", FlaggedPolyline.class.getSimpleName());
        return GeoJsonUtils.feature(geoJsonGeometry, jsonProperties);
    }

    @Override
    public Rectangle bounds()
    {
        return this.atlasItem.bounds();
    }

    @Override
    public FlaggedObject getAsCompleteFlaggedObject()
    {
        if (this.atlasItem instanceof Area)
        {
            return new FlaggedPolyline(CompleteArea.from((Area) this.atlasItem));
        }
        else if (this.atlasItem instanceof Edge)
        {
            return new FlaggedPolyline(CompleteEdge.from((Edge) this.atlasItem));
        }
        else if (this.atlasItem instanceof Line)
        {
            return new FlaggedPolyline(CompleteLine.from((Line) this.atlasItem));
        }
        throw new CoreException("FlaggedPolyline has improper Atlas Item {}", this.atlasItem);
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

    @Override
    protected Optional<AtlasObject> getObject()
    {
        return Optional.ofNullable(this.atlasItem);
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
        final Map<String, String> tags = new HashMap<>(object.getTags());
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
