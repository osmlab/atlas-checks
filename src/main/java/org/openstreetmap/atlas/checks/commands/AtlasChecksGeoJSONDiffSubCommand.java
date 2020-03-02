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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.writers.JsonWriter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;

/**
 * Takes 2 sets of atlas-checks geoJSON flag files and reports the number of additions,
 * subtractions, and changed flags from reference to input. Optionally, the reported items can be
 * written to new geoJSON files. Additions and subtractions are based on flag ids. Changes are
 * calculated by differences in the Atlas ids of objects in a flag.
 *
 * @author bbreithaupt
 */
public class AtlasChecksGeoJSONDiffSubCommand extends JSONFlagDiffSubCommand
{
    private static final Logger logger = LoggerFactory
            .getLogger(AtlasChecksGeoJSONDiffSubCommand.class);
    private static final Pattern NAME_PATTERN = Pattern.compile("([^ ]+) ?");

    public AtlasChecksGeoJSONDiffSubCommand()
    {
        super("geojson-diff",
                "Takes 2 sets of atlas-checks geoJSON flag files and reports the number of additions, subtractions, and changed flags from reference to input.",
                "geojson");
    }

    @Override
    protected Map<String, Map<Set<String>, JsonObject>> mapFeatures(final File file)
    {
        final Map<String, Map<Set<String>, JsonObject>> checkFeatureMap = new HashMap<>();
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
                checkFeatureMap.get(checkName)
                        .put(this.getIdentifiers(
                                feature.getAsJsonObject().get(PROPERTIES).getAsJsonObject()),
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
    protected void writeSetToGeoJSON(final Map<String, Set<JsonObject>> flags, final File output)
    {
        final JsonArray featureArray = new JsonArray();
        flags.values().stream().flatMap(Collection::stream).forEach(featureArray::add);
        final JsonObject featureCollection = new JsonObject();
        featureCollection.add(TYPE, new JsonPrimitive(FEATURE_COLLECTION.toString()));
        featureCollection.add(FEATURES, featureArray);

        final JsonWriter writer = new JsonWriter(output);
        writer.writeLine(featureCollection);
        writer.close();
    }

    /**
     * Get the unique ids for a flag. Fall back to getting the atlas ids from the feature properties
     * for reverse compatibility.
     *
     * @param properties
     *            a {@link JsonObject} of a flag
     * @return a {@link Set} of {@link String} ids
     */
    private Set<String> getIdentifiers(final JsonObject properties)
    {
        return properties.has(IDENTIFIERS)
                ? Iterables.stream(properties.get(IDENTIFIERS).getAsJsonArray())
                        .map(JsonElement::getAsString).collectToSet()
                : Iterables.stream(properties.get(FEATURE_PROPERTIES).getAsJsonArray())
                        .filter(object -> object.getAsJsonObject().has(IDENTIFIER))
                        .map(object -> object.getAsJsonObject().get(IDENTIFIER).getAsString())
                        .collectToSet();
    }
}
