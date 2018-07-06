package org.openstreetmap.atlas.checks.validation.utility;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.clipping.Clip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.TopologyException;

/**
 * A set of utilities that are common among intersection checks.
 *
 * @author bbreithaupt
 */
public final class IntersectionUtilities
{
    private static final Logger logger = LoggerFactory.getLogger(IntersectionUtilities.class);

    private IntersectionUtilities()
    {
    }

    /**
     * Find the percentage of overlap for given {@link Polygon}s.
     *
     * @param polygon
     *            {@link Polygon} to check for intersection
     * @param otherPolygon
     *            Another {@link Polygon} to check against for intersection
     * @return percentage of overlap as a double; 0 if unable to clip
     */
    public static double findIntersectionPercentage(final Polygon polygon, final Polygon otherPolygon)
    {
        Clip clip = null;
        try
        {
            clip = polygon.clip(otherPolygon, Clip.ClipType.AND);
        }
        catch (final TopologyException e)
        {
            logger.warn(String.format("Skipping intersection check. Error clipping [%s] and [%s].",
                    polygon, otherPolygon), e);
        }

        // Skip if nothing is returned
        if (clip == null)
        {
            return 0.0;
        }

        // Sum intersection area
        long intersectionArea = 0;
        for (final PolyLine polyline : clip.getClip())
        {
            if (polyline != null && polyline instanceof Polygon)
            {
                final Polygon clippedPolygon = (Polygon) polyline;
                intersectionArea += clippedPolygon.surface().asDm7Squared();
            }
        }

        // Avoid division by zero
        if (intersectionArea == 0)
        {
            return 0.0;
        }

        // Pick the smaller building's area as baseline
        final long baselineArea = Math.min(polygon.surface().asDm7Squared(),
                otherPolygon.surface().asDm7Squared());
        return (double) intersectionArea / baselineArea;
    }
}
