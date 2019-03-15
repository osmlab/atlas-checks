package org.openstreetmap.atlas.checks.commands;

import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.FEATURES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonUtils.IDENTIFIER;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.streaming.resource.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Takes 2 sets of atlas-checks log files and reports the number of additions, subtractions, and
 * changed flags from source to target. Optionally, the reported items can be written to new log
 * files. Additions and subtractions are based on flag ids. Changes are calculated by differences in
 * the Atlas ids of objects in a flag.
 *
 * @author bbreithaupt
 */
public class AtlasChecksLogDiffSubCommand extends JSONFlagDiffSubCommand
{
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationResolver.class);

    public AtlasChecksLogDiffSubCommand()
    {
        super("log-diff",
                "Takes 2 sets of atlas-checks log flag files and reports the number of additions, subtractions, and changed flags from source to target.",
                "log");
    }

    @Override
    protected HashMap<String, HashMap<String, JsonObject>> mapFeatures(final File file)
    {
        final HashMap<String, HashMap<String, JsonObject>> checkFeatureMap = new HashMap<>();
        try (InputStreamReader inputStreamReader = file.isGzipped()
                ? new InputStreamReader(new GZIPInputStream(new FileInputStream(file.getFile())))
                : new FileReader(file.getPath()))
        {
            try (BufferedReader reader = new BufferedReader(inputStreamReader))
            {
                String line;
                // Read each line (flag) from the log file
                while ((line = reader.readLine()) != null)
                {
                    // Parse the json
                    final JsonObject source = getGson().fromJson(line, JsonObject.class);
                    // Get the check name
                    final String checkName = source.get(PROPERTIES).getAsJsonObject().get(GENERATOR)
                            .getAsString();
                    // Add the check name as a key
                    checkFeatureMap.putIfAbsent(checkName, new HashMap<>());
                    // Add the geoJSON as a value
                    checkFeatureMap.get(checkName).put(
                            source.get(PROPERTIES).getAsJsonObject().get(ID).getAsString(), source);
                }
            }
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
        source.forEach((check, flags) -> flags.forEach((identifier, featureCollection) ->
        {
            // Get missing
            if (!target.containsKey(check) || !target.get(check).containsKey(identifier))
            {
                diff.addMissing(featureCollection);
            }
            // If not missing, check for Atlas id changes
            else if (returnType.equals(DiffReturn.CHANGED)
                    && !this.identicalFeatureIds(featureCollection.get(FEATURES).getAsJsonArray(),
                            target.get(check).get(identifier).get(FEATURES).getAsJsonArray()))
            {
                diff.addChanged(featureCollection);
            }
        }));
        return diff;
    }

    @Override
    protected boolean identicalFeatureIds(final JsonArray sourceArray, final JsonArray targetArray)
    {
        final ArrayList<String> sourceIds = new ArrayList<>();
        final ArrayList<String> targetIds = new ArrayList<>();
        // The array must be the same size to match
        if (sourceArray.size() != targetArray.size())
        {
            return false;
        }
        // Gather all the source ids
        sourceArray.forEach(object ->
        {
            // Handle Locations that were added and don't have an id
            if (object.getAsJsonObject().get(PROPERTIES).getAsJsonObject().has(IDENTIFIER))
            {
                sourceIds.add(object.getAsJsonObject().get(PROPERTIES).getAsJsonObject()
                        .get(IDENTIFIER).getAsString());
            }
        });
        // Gather all the target ids
        targetArray.forEach(object ->
        {
            // Handle Locations that were added and don't have an id
            if (object.getAsJsonObject().get(PROPERTIES).getAsJsonObject().has(IDENTIFIER))
            {
                targetIds.add(object.getAsJsonObject().get(PROPERTIES).getAsJsonObject()
                        .get(IDENTIFIER).getAsString());
            }
        });
        // Compare the two id lists
        return sourceIds.containsAll(targetIds) && targetIds.containsAll(sourceIds);
    }
}
