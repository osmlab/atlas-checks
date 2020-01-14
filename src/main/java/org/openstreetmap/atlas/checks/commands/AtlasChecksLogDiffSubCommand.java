package org.openstreetmap.atlas.checks.commands;

import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.FEATURES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonUtils.IDENTIFIER;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Takes 2 sets of atlas-checks log files and reports the number of additions, subtractions, and
 * changed flags from reference to input. Optionally, the reported items can be written to new log
 * files. Additions and subtractions are based on flag ids. Changes are calculated by differences in
 * the Atlas ids of objects in a flag.
 *
 * @author bbreithaupt
 */
public class AtlasChecksLogDiffSubCommand extends JSONFlagDiffSubCommand
{
    private static final Logger logger = LoggerFactory
            .getLogger(AtlasChecksLogDiffSubCommand.class);

    public AtlasChecksLogDiffSubCommand()
    {
        super("log-diff",
                "Takes 2 sets of atlas-checks log flag files and reports the number of additions, subtractions, and changed flags from reference to input.",
                "log");
    }

    @Override
    protected Map<String, Map<Set<String>, JsonObject>> mapFeatures(final File file)
    {
        final Map<String, Map<Set<String>, JsonObject>> checkFeatureMap = new HashMap<>();
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
                    if (checkFeatureMap.get(checkName).containsKey(this.getIdentifiers(source)))
                    {
                        logger.info("Duplicate flag found in {}: {}", file.getAbsolutePath(),
                                source);
                    }
                    checkFeatureMap.get(checkName).put(this.getIdentifiers(source), source);
                }
            }
        }
        catch (final IOException exception)
        {
            logger.warn("File read failed with exception", exception);
        }
        return checkFeatureMap;
    }

    /**
     * Get the unique ids for a flag. Fall back to getting the atlas ids from the features for
     * reverse compatibility.
     *
     * @param flagJson
     *            a {@link JsonObject} of a flag
     * @return a {@link Set} of {@link String} ids
     */
    private Set<String> getIdentifiers(final JsonObject flagJson)
    {
        final JsonObject flagProperties = flagJson.get(PROPERTIES).getAsJsonObject();
        return flagProperties.has(IDENTIFIERS)
                ? Iterables.stream(flagProperties.get(IDENTIFIERS).getAsJsonArray())
                        .map(JsonElement::getAsString).collectToSet()
                : Iterables.stream(flagJson.get(FEATURES).getAsJsonArray()).filter(object -> object
                        .getAsJsonObject().get(PROPERTIES).getAsJsonObject().has(IDENTIFIER)
                        || object.getAsJsonObject().get(PROPERTIES).getAsJsonObject().has("ItemId"))
                        .map(object -> Optional
                                .ofNullable(object.getAsJsonObject().get(PROPERTIES)
                                        .getAsJsonObject().get(IDENTIFIER))
                                .orElse(object.getAsJsonObject().get(PROPERTIES).getAsJsonObject()
                                        .get("ItemId"))
                                .getAsString())
                        .collectToSet();
    }
}
