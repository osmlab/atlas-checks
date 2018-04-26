package org.openstreetmap.atlas.checks.flag;

import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * A flag for a {@link PolyLine}
 *
 * @author brian_l_davis
 */
public class FlaggedPolyline extends FlaggedObject
{
    private static final long serialVersionUID = -1184306312148054279L;

    private final String country;
    private final PolyLine polyLine;
    private final Map<String, String> properties;

    /**
     * Default constructor
     * 
     * @param object
     *            the {@link AtlasObject} to flag
     */
    public FlaggedPolyline(final AtlasObject object)
    {
        if (object instanceof AtlasItem)
        {
            if (object instanceof Area)
            {
                // An Area's geometry doesn't include the end (same as start) location. To close the
                // area boundary, we need to add the end location manually.
                final List<Location> geometry = Iterables
                        .asList(((AtlasItem) object).getRawGeometry());
                geometry.add(geometry.get(0));
                this.polyLine = new PolyLine(geometry);
            }
            else
            {
                this.polyLine = new PolyLine(((AtlasItem) object).getRawGeometry());
            }
        }
        else
        {
            this.polyLine = null;
        }
        this.properties = initProperties(object);
        this.country = initCountry(object);
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
