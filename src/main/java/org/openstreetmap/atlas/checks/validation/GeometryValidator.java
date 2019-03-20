package org.openstreetmap.atlas.checks.validation;

import org.locationtech.jts.geom.LineString;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;

/**
 * Basic JTS verification for a {@link PolyLine} and {@link Polygon}
 *
 * @author mgostintsev
 */
public final class GeometryValidator
{
    private static final JtsPolyLineConverter POLYLINE_CONVERTER = new JtsPolyLineConverter();
    private static final JtsPolygonConverter POLYGON_CONVERTER = new JtsPolygonConverter();

    /**
     * Tests that the {@link Polygon}'s geometry is valid
     *
     * @param polygon
     *            the {@link Polygon} to test
     * @return {@code true} if the {@link Polygon} has valid geometry, otherwise {@code false}
     */
    public static boolean isValidPolygon(final Polygon polygon)
    {
        final org.locationtech.jts.geom.Polygon jtsPolygon = POLYGON_CONVERTER.convert(polygon);
        return jtsPolygon.isSimple();
    }

    /**
     * Tests that the {@link PolyLine}'s geometry is valid
     *
     * @param polyline
     *            the {@link PolyLine} to test
     * @return {@code true} if the {@link PolyLine} has valid geometry, otherwise {@code false}
     */
    public static boolean isValidPolyLine(final PolyLine polyline)
    {
        final LineString lineString = POLYLINE_CONVERTER.convert(polyline);
        return lineString.isSimple();
    }

    private GeometryValidator()
    {
    }
}
