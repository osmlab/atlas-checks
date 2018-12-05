package org.openstreetmap.atlas.checks.vectortiles;

import java.util.function.Consumer;

import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.utilities.vectortiles.TippecanoeGeoJsonExtension;

import com.google.gson.JsonObject;

/**
 * This utility class provides the settings you need for running tippecanoe.
 *
 * @author hallahan
 */
public final class TippecanoeCheckSettings
{
    private TippecanoeCheckSettings()
    {
        // Utility Class
    }

    private static final int FLAG_MINIMUM_ZOOM = 1;
    private static final int FEATURE_MINIMUM_ZOOM = 8;

    public static final Consumer<JsonObject> JSON_MUTATOR = jsonObject ->
    {
        final JsonObject properties = jsonObject.getAsJsonObject("properties");
        final String type = properties.get("flag:type").getAsString();

        final TippecanoeGeoJsonExtension tippecanoe = new TippecanoeGeoJsonExtension()
                .addTo(jsonObject);
        tippecanoe.layer(type);

        if (CheckFlag.class.getSimpleName().equals(type))
        {
            tippecanoe.minimumZoom(FLAG_MINIMUM_ZOOM);
        }
        else
        {
            tippecanoe.minimumZoom(FEATURE_MINIMUM_ZOOM);
        }
    };
}
