package org.openstreetmap.atlas.checks.commands;

import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.FEATURES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.TYPE;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonType.FEATURE_COLLECTION;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonUtils.IDENTIFIER;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final Pattern NAME_PATTERN = Pattern.compile("([^ ]+) ?");

    public AtlasChecksGeoJSONDiffSubCommand()
    {
        super("geojson-diff",
                "Takes 2 sets of atlas-checks geoJSON flag files and reports the number of additions, subtractions, and changed flags from source to target.",
                "geojson");
    }

    @Override
    protected HashMap<String, HashMap<String, JsonObject>> mapFeatures(final File file)
    {
        final HashMap<String, HashMap<String, JsonObject>> checkFeatureMap = new HashMap<>();
        try (InputStream inputStream = file.isGzipped()
                ? new GZIPInputStream(new FileInputStream(file.getFile()))
                : file.read())
        {
            // Parse the json
            final JsonObject json = getGson()
                    .fromJson(new JsonReader(new InputStreamReader(inputStream)), JsonObject.class);
            // Map each feature
            json.get(FEATURES).getAsJsonArray().forEach(feature ->
            {
                final JsonObject jsonFeature = feature.getAsJsonObject();
                // Get the check name. Use regex to disregard the appended highway tag values.
                final Matcher nameMatch = NAME_PATTERN.matcher(
                        jsonFeature.get(PROPERTIES).getAsJsonObject().get(NAME).getAsString());
                nameMatch.find();
                final String checkName = nameMatch.group(1);
                // Add the check name as a key
                checkFeatureMap.putIfAbsent(checkName, new HashMap<>());
                // Add the geoJSON as a value
                checkFeatureMap.get(checkName).put(feature.getAsJsonObject().get(ID).getAsString(),
                        jsonFeature);
            });
        }
        catch (final IOException exception)
        {
            logger.warn("File read failed with exception", exception);
        }
        return checkFeatureMap;
    }

    @Override
    protected JSONFlagDiff getDiff(final HashMap<String, HashMap<String, JsonObject>> source,
            final HashMap<String, HashMap<String, JsonObject>> target, final DiffReturn returnType)
    {
        final JSONFlagDiff diff = new JSONFlagDiff();
        source.forEach((check, flags) -> flags.forEach((identifier, feature) ->
        {
            // Get missing
            if (!target.containsKey(check) || !target.get(check).containsKey(identifier))
            {
                diff.addMissing(feature);
            }
            // If not missing, check for Atlas id changes
            else if (returnType.equals(DiffReturn.CHANGED) && !this.identicalFeatureIds(
                    feature.get(PROPERTIES).getAsJsonObject().get(FEATURE_PROPERTIES)
                            .getAsJsonArray(),
                    target.get(check).get(identifier).getAsJsonObject().get(PROPERTIES)
                            .getAsJsonObject().get(FEATURE_PROPERTIES).getAsJsonArray()))
            {
                diff.addChanged(feature);
            }
        }));
        return diff;
    }

    @Override
    protected boolean identicalFeatureIds(final JsonArray sourceArray, final JsonArray targetArray)
    {
        final ArrayList<String> sourceIds = new ArrayList<>();
        final ArrayList<String> targetIds = new ArrayList<>();
        sourceArray.forEach(object ->
        {
            if (object.getAsJsonObject().has(IDENTIFIER))
            {
                sourceIds.add(object.getAsJsonObject().get(IDENTIFIER).getAsString());
            }
        });
        targetArray.forEach(object ->
        {
            if (object.getAsJsonObject().has(IDENTIFIER))
            {
                targetIds.add(object.getAsJsonObject().get(IDENTIFIER).getAsString());
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
        featureCollection.add(TYPE, new JsonPrimitive(FEATURE_COLLECTION.toString()));
        featureCollection.add(FEATURES, featureArray);

        super.writeSetToGeoJSON(Collections.singleton(featureCollection), output);
    }
}
