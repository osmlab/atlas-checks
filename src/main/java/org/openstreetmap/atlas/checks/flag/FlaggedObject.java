package org.openstreetmap.atlas.checks.flag;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;

/**
 * Abstract base class for {@link AtlasObject}s flagged by the integrity framework
 *
 * @author brian_l_davis
 */
public abstract class FlaggedObject implements Serializable
{
    protected static final String COUNTRY_MISSING = "NA";
    protected static final String AREA_TAG = "Area";
    protected static final String EDGE_TAG = "Edge";
    protected static final String OSM_IDENTIFIER_TAG = "osmid";
    protected static final String ITEM_IDENTIFIER_TAG = "ItemId";
    protected static final String ITEM_TYPE_TAG = "ItemType";
    protected static final String LINE_TAG = "Line";
    protected static final String NODE_TAG = "Node";
    protected static final String POINT_TAG = "Point";
    private static final long serialVersionUID = -2898518269816777421L;

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof FlaggedObject))
        {
            return false;
        }

        final FlaggedObject otherObject = (FlaggedObject) other;
        return Objects.equals(this.getCountry(), otherObject.getCountry())
                && Objects.equals(this.getGeometry(), otherObject.getGeometry())
                && Objects.equals(this.getProperties(), otherObject.getProperties());
    }

    /**
     * @return the flagged object's country code
     */
    public String getCountry()
    {
        return COUNTRY_MISSING;
    }

    /**
     * @return flagged geometry
     */
    public abstract Iterable<Location> getGeometry();

    /**
     * @return flag key-value property map
     */
    public abstract Map<String, String> getProperties();

    /**
     * @return {@code true} if the flagged object has a country code property
     */
    public boolean hasCountry()
    {
        return !getCountry().equals(COUNTRY_MISSING);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.getCountry(), this.getGeometry(), this.getProperties());
    }
}
