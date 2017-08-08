package org.openstreetmap.atlas.checks.flag;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.streaming.resource.WritableResource;

/**
 * A utility for translating a set of flags to GeoJson to visualize them
 *
 * @author matthieun
 * @author cuthbertm
 */
public class FlagVisualizer
{
    /**
     * Default constructor
     *
     * @param flags
     *            a list of {@link CheckFlag}s
     * @param output
     *            a {@link WritableResource} where the GeoJson is saved to
     */
    public void saveAsGeoJson(final Iterable<CheckFlag> flags, final WritableResource output)
    {
        visualize(flags).save(output);
    }

    /**
     * Converts a list of {@link CheckFlag}s to GeoJson
     *
     * @param flags
     *            a list of {@link CheckFlag}s
     * @return a {@link GeoJsonObject} representation
     */
    public GeoJsonObject visualize(final Iterable<CheckFlag> flags)
    {
        final GeoJsonBuilder result = new GeoJsonBuilder();
        final List<GeoJsonBuilder.LocationIterableProperties> shapes = new ArrayList<>();
        flags.forEach(flag ->
        {
            shapes.addAll(flag.getLocationIterableProperties());
        });

        return result.create(shapes);
    }
}
