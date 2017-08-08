package org.openstreetmap.atlas.checks.flag;

import java.io.Serializable;
import java.util.Map;

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
    private static final long serialVersionUID = -2898518269816777421L;

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
}
