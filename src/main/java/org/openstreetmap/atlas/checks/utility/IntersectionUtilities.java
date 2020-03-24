package org.openstreetmap.atlas.checks.utility;

import java.util.Set;

import org.locationtech.jts.geom.TopologyException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.clipping.Clip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A set of utilities that are common among intersection checks.
 *
 * @author bbreithaupt
 */
public final class IntersectionUtilities
{
    private static final Logger logger = LoggerFactory.getLogger(IntersectionUtilities.class);

    /**
     * Find the percentage of overlap for given {@link Polygon}s.
     *
     * @param polygon
     *            {@link Polygon} to check for intersection
     * @param otherPolygon
     *            Another {@link Polygon} to check against for intersection
     * @return percentage of overlap as a double; 0 if unable to clip
     */
    public static double findIntersectionPercentage(final Polygon polygon,
            final Polygon otherPolygon)
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
            if (polyline instanceof Polygon)
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

    /**
     * Verifies intersections of given {@link PolyLine} and {@link LineItem} are explicit
     * {@link Location}s for both items
     *
     * @param lineCrossed
     *            {@link PolyLine} being crossed
     * @param crossingItem
     *            {@link LineItem} crossing
     * @return whether given {@link PolyLine} and {@link LineItem}'s intersections are actual
     *         {@link Location}s for both items
     */
    public static boolean haveExplicitLocationsForIntersections(final PolyLine lineCrossed,
            final LineItem crossingItem)
    {
        // Find out intersections
        final PolyLine crossingItemAsPolyLine = crossingItem.asPolyLine();
        final Set<Location> intersections = lineCrossed.intersections(crossingItemAsPolyLine);

        // Verify intersections are explicit locations for both geometries
        for (final Location intersection : intersections)
        {
            if (!lineCrossed.contains(intersection)
                    || !crossingItemAsPolyLine.contains(intersection))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Overloaded: avoid recomputing the intersection locations if they're given.
     *
     * @param lineCrossed
     *            {@link PolyLine} being crossed
     * @param crossingItem
     *            {@link LineItem} crossing
     * @param intersections
     *            the intersections between lineCrossed and crossingItem
     * @return whether given {@link PolyLine} and {@link LineItem}'s intersections are actual
     *         {@link Location}s for both items
     */
    public static boolean haveExplicitLocationsForIntersections(final PolyLine lineCrossed,
            final LineItem crossingItem, final Set<Location> intersections)
    {
        // Find out intersections
        final PolyLine crossingItemAsPolyLine = crossingItem.asPolyLine();

        // Verify intersections are explicit locations for both geometries
        for (final Location intersection : intersections)
        {
            if (!lineCrossed.contains(intersection)
                    || !crossingItemAsPolyLine.contains(intersection))
            {
                return false;
            }
        }
        return true;
    }

    private IntersectionUtilities()
    {
    }
}
