package org.openstreetmap.atlas.checks.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.streaming.resource.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

/**
 * Takes 2 sets of atlas-checks geoJSON flag files and reports the number of additions,
 * subtractions, and changed flags from source to target. Optionally, the reported items can be
 * written to new geoJSON files. Additions and subtractions are based on flag ids. Changes are
 * calculated by differences in the Atlas ids of objects in a flag.
 *
 * @author bbreithaupt
 */
public class AtlasChecksGeoJSONDiffSubCommand extends JSONFlagDiffSubCommand
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationResolver.class);

    public AtlasChecksGeoJSONDiffSubCommand()
    {
        super("geojson-diff",
                "Takes 2 sets of atlas-checks geoJSON flag files and reports the number of additions, subtractions, and changed flags from source to target.",
                "geojson");
    }

    @Override
    protected void mapFeatures(final File file, final HashMap map)
    {
        try (InputStream inputStream = file.isGzipped()
                ? new GZIPInputStream(new FileInputStream(file.getFile()))
                : file.read())
        {
            final JsonObject json = getGson()
                    .fromJson(new JsonReader(new InputStreamReader(inputStream)), JsonObject.class);
            json.get(FEATURES).getAsJsonArray()
                    .forEach(feature -> map.put(feature.getAsJsonObject().get(ID).getAsString(),
                            feature.getAsJsonObject()));
        }
        catch (final IOException exception)
        {
            logger.warn("File read failed with exception", exception);
        }
    }

    @Override
    protected JSONFlagDiff getDiff(final HashMap source, final HashMap target,
            final DiffReturn returnType)
    {
        final JSONFlagDiff diff = new JSONFlagDiff();
        source.forEach((identifier, feature) ->
        {
            // Get missing
            if (!target.containsKey(identifier))
            {
                diff.addMissing((JsonObject) feature);
            }
            // If not missing, check for Atlas id changes
            else if (returnType.equals(DiffReturn.CHANGED) && !this.identicalFeatureIds(
                    ((JsonObject) feature).get(PROPERTIES).getAsJsonObject().get(FEATURE_PROPERTIES)
                            .getAsJsonArray(),
                    ((HashMap<String, JsonObject>) target).get(identifier).getAsJsonObject()
                            .get(PROPERTIES).getAsJsonObject().get(FEATURE_PROPERTIES)
                            .getAsJsonArray()))
            {
                diff.addChanged((JsonObject) feature);
            }
        });
        return diff;
    }

    @Override
    protected boolean identicalFeatureIds(final JsonArray sourceArray, final JsonArray targetArray)
    {
        final ArrayList<String> sourceIds = new ArrayList<>();
        final ArrayList<String> targetIds = new ArrayList<>();
        sourceArray.forEach(object ->
        {
            if (object.getAsJsonObject().has(ITEM_ID))
            {
                sourceIds.add(object.getAsJsonObject().get(ITEM_ID).getAsString());
            }
        });
        targetArray.forEach(object ->
        {
            if (object.getAsJsonObject().has(ITEM_ID))
            {
                targetIds.add(object.getAsJsonObject().get(ITEM_ID).getAsString());
            }
        });
        return sourceIds.containsAll(targetIds) && targetIds.containsAll(sourceIds);
    }

    @Override
    protected void writeSetToGeoJSON(final Set<JsonObject> flags, final File output)
    {
        final JsonArray featureArray = new JsonArray();
        flags.forEach(featureArray::add);
        final JsonObject featureCollection = new JsonObject();
        featureCollection.add(TYPE, new JsonPrimitive(FEATURE_COLLECTION));
        featureCollection.add(FEATURES, featureArray);

        super.writeSetToGeoJSON(Collections.singleton(featureCollection), output);
    }
}
