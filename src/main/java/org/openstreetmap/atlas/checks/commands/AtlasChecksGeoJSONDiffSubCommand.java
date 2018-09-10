package org.openstreetmap.atlas.checks.commands;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.openstreetmap.atlas.streaming.resource.File;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

/**
 * Takes 2 sets of atlas-checks geoJSON flag files and reports the number of additions,
 * subtractions, and changed flags from source to target. Optionally, the reported items can be
 * written to new geoJSON files.
 *
 * @author bbreithaupt
 */
public class AtlasChecksGeoJSONDiffSubCommand extends AbstractJsonFlagDiffSubCommand
{
    public AtlasChecksGeoJSONDiffSubCommand()
    {
        super("geoJSON-diff",
                "Takes 2 sets of atlas-checks geoJSON flag files and reports the number of additions, subtractions, and changed flags from source to target.",
                "geojson");
    }

    @Override
    protected void mapFeatures(final File file, final HashMap map)
    {
        final JsonObject json = getGson()
                .fromJson(new JsonReader(new InputStreamReader(file.read())), JsonObject.class);
        json.get("features").getAsJsonArray().forEach(feature -> map
                .put(feature.getAsJsonObject().get("id").getAsString(), feature.getAsJsonObject()));
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
            else if (returnType.equals(DiffReturn.CHANGED) && !identicalFeatureIds(
                    ((JsonObject) feature).get("properties").getAsJsonObject()
                            .get("feature_properties").getAsJsonArray(),
                    ((HashMap<String, JsonObject>) target).get(identifier).getAsJsonObject()
                            .get("properties").getAsJsonObject().get("feature_properties")
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
        sourceArray.forEach(
                object -> sourceIds.add(object.getAsJsonObject().get("ItemId").getAsString()));
        targetArray.forEach(
                object -> targetIds.add(object.getAsJsonObject().get("ItemId").getAsString()));
        return sourceIds.containsAll(targetIds) && targetIds.containsAll(sourceIds);
    }

    @Override
    protected void writeSetToGeoJSON(final Set<JsonObject> flags, final File output)
    {
        final JsonArray featureArray = new JsonArray();
        flags.forEach(featureArray::add);
        final JsonObject featureCollection = new JsonObject();
        featureCollection.add("type", new JsonPrimitive("FeatureCollection"));
        featureCollection.add("features", featureArray);

        super.writeSetToGeoJSON(Collections.singleton(featureCollection), output);
    }
}
