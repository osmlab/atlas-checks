package org.openstreetmap.atlas.checks.utility;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;

/**
 * Tests for {@link IntersectionUtilities}
 *
 * @author bbreithaupt
 */
public class IntersectionUtilitiesTest
{
    // Locations
    private static final Location LOCATION1 = Location
            .forString("47.244117672349,-122.396137421285");
    private static final Location LOCATION2 = Location
            .forString("47.2434634265599,-122.396147058415");
    private static final Location LOCATION3 = Location
            .forString("47.2434634265599,-122.395549556359");
    private static final Location LOCATION4 = Location
            .forString("47.2441111299311,-122.395549556359");
    private static final Location LOCATION5 = Location
            .forString("47.2449550951688,-122.396469902267");
    private static final Location LOCATION6 = Location
            .forString("47.2442158085205,-122.396479539397");
    private static final Location LOCATION7 = Location
            .forString("47.2440097223504,-122.395814577432");
    private static final Location LOCATION8 = Location
            .forString("47.2449485528544,-122.395809758867");
    // Polygons
    private static final Polygon POLYGON1 = new Polygon(LOCATION1, LOCATION2, LOCATION3, LOCATION4);
    private static final Polygon POLYGON2 = new Polygon(LOCATION5, LOCATION6, LOCATION7, LOCATION8);
    private static final Polygon POLYGON3 = new Polygon(LOCATION5, LOCATION6, LOCATION8);

    @Test
    public void findIntersectionPercentageIntersectingPolygonTest()
    {
        final double intersectPolygon = IntersectionUtilities.findIntersectionPercentage(POLYGON1,
                POLYGON2);
        assertTrue(intersectPolygon < 1 && intersectPolygon > 0);
    }

    @Test
    public void findIntersectionPercentageNonIntersectingPolygonTest()
    {
        assertTrue(IntersectionUtilities.findIntersectionPercentage(POLYGON1, POLYGON3) == 0);
    }

    @Test
    public void findIntersectionPercentageSamePolygonTest()
    {
        assertTrue(IntersectionUtilities.findIntersectionPercentage(POLYGON1, POLYGON1) >= 1);
    }
}
