package org.openstreetmap.atlas.checks.flag;

import java.util.Collections;
import java.util.Map;

import org.openstreetmap.atlas.geography.Location;

/**
 * A flag for a {@code point} {@link Location} P*
 * 
 * @author brian_l_davis
 */
public class FlaggedPoint extends FlaggedObject
{
    private static final long serialVersionUID = -5912453173756416690L;
    private final Location point;

    /**
     * Default constructor
     * 
     * @param point
     *            the {@code point} {@link Location} to flag
     */
    public FlaggedPoint(final Location point)
    {
        this.point = point;
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
        return Collections.EMPTY_MAP;
    }
}
