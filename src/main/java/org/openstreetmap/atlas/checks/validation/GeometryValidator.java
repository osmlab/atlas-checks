package org.openstreetmap.atlas.checks.validation;

import java.util.Optional;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;

/**
 * Basic JTS verification for a {@link PolyLine} and {@link Polygon}
 *
 * @author mgostintsev
 * @author bbreithaupt
 */
public final class GeometryValidator
{
    private static final Optional<String> NOT_SIMPLE_LINEAR = Optional.of(
            "Linear geometry intersecting itself at interior points (points other than the boundary)");
    private static final Optional<String> NOT_SIMPLE_POINT = Optional
            .of("Point geometry has repeated point");
    private static final Optional<String> NOT_SIMPLE_POLYGON = Optional
            .of("Polygon geometry is malformed");
    private static final Optional<String> NOT_VALID_LINEAR = Optional
            .of("Linear geometry has exactly two identical points");
    private static final Optional<String> NOT_VALID_POINT = Optional
            .of("Point geometry has invalid dimension value (NaN)");
    private static final Optional<String> NOT_VALID_POLYGON = Optional.of(
            "Polygon geometry has one or more of the following: Invalid coordinates, Invalid linear rings in "
                    + "construction, Holes in the polygon that touch other holes or the outer ring more than at one"
                    + " point, Interior that is not connected (split into two by holes)");
    private static final JtsPointConverter POINT_CONVERTER = new JtsPointConverter();
    private static final JtsPolyLineConverter POLYLINE_CONVERTER = new JtsPolyLineConverter();
    private static final JtsPolygonConverter POLYGON_CONVERTER = new JtsPolygonConverter();

    public static Optional<String> testSimplicity(final Iterable<Location> geometry)
    {
        if (geometry instanceof Location
                && !POINT_CONVERTER.convert((Location) geometry).isSimple())
        {
            return NOT_SIMPLE_POINT;
        }
        if (geometry instanceof PolyLine
                && !POLYLINE_CONVERTER.convert((PolyLine) geometry).isSimple())
        {
            return NOT_SIMPLE_LINEAR;
        }
        if (geometry instanceof Polygon
                && !POLYGON_CONVERTER.convert((Polygon) geometry).isSimple())
        {
            return NOT_SIMPLE_POLYGON;
        }
        return Optional.empty();
    }

    public static Optional<String> testValidity(final Iterable<Location> geometry)
    {
        if (geometry instanceof Location && !POINT_CONVERTER.convert((Location) geometry).isValid())
        {
            return NOT_VALID_POINT;
        }
        if (geometry instanceof PolyLine
                && !POLYLINE_CONVERTER.convert((PolyLine) geometry).isValid())
        {
            return NOT_VALID_LINEAR;
        }
        if (geometry instanceof Polygon && !POLYGON_CONVERTER.convert((Polygon) geometry).isValid())
        {
            return NOT_VALID_POLYGON;
        }
        return Optional.empty();
    }

    private GeometryValidator()
    {
    }
}
