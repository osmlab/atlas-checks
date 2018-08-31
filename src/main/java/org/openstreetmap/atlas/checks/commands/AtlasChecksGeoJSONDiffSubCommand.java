package org.openstreetmap.atlas.checks.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.writers.JsonWriter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.runtime.FlexibleSubCommand;

import com.google.gson.Gson;
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
public class AtlasChecksGeoJSONDiffSubCommand implements FlexibleSubCommand
{
    private static final Switch<Set<String>> SOURCE_FILE_PARAMETER = new Switch<>("source",
            "A comma separated set of geoJSON files containing atlas-checks flags to compare changes from.",
            csv -> Stream.of(csv.split(",")).collect(Collectors.toSet()), Optionality.REQUIRED);

    private static final Switch<Set<String>> TARGET_FILE_PARAMETER = new Switch<>("target",
            "A comma separated set of geoJSON files containing atlas-checks flags to compare changes to.",
            csv -> Stream.of(csv.split(",")).collect(Collectors.toSet()), Optionality.REQUIRED);

    private static final Switch<String> OUTPUT_FOLDER_PARAMETER = new Switch<>("output",
            "A directory to place output geoJSON files in. If not included no outputs files will be written.",
            String::new, Optionality.OPTIONAL);

    private static final String NAME = "geoJSON-diff";
    private static final String DESCRIPTION = "Takes 2 sets of atlas-checks geoJSON flag files and reports the number of additions, subtractions, and changed flags from source to target.";

    @Override
    public int execute(final CommandMap command)
    {
        // Get geojsons and parse to maps
        final HashMap<String, JsonObject> source = new HashMap<>();
        final HashMap<String, JsonObject> target = new HashMap<>();
        ((Set) command.get(SOURCE_FILE_PARAMETER))
                .forEach(path -> source.putAll(mapFeatures(new File((String) path).read())));
        ((Set) command.get(TARGET_FILE_PARAMETER))
                .forEach(path -> target.putAll(mapFeatures(new File((String) path).read())));

        // Get changes from source to target
        final HashSet<JsonObject> additions = (HashSet<JsonObject>) getMissingAndChanged(target,
                source, true).get(0);
        final ArrayList<HashSet<JsonObject>> subAndChange = getMissingAndChanged(source, target,
                false);
        final HashSet<JsonObject> subtractions = subAndChange.get(0);
        final HashSet<JsonObject> changes = subAndChange.get(1);

        // Write outputs
        System.out.printf(
                "\n Total Items: %d\n   Additions: %d\n     Changes: %d\nSubtractions: %d\n",
                source.size() + additions.size(), additions.size(), changes.size(),
                subtractions.size());
        final Optional output = command.getOption(OUTPUT_FOLDER_PARAMETER);
        if (output.isPresent())
        {
            writeSetToGeoJSON(additions, new File(String.format("%s/additions-%d-%d.geojson",
                    output.get(), new Date().getTime(), additions.size())));
            writeSetToGeoJSON(changes, new File(String.format("%s/changes-%d-%d.geojson",
                    output.get(), new Date().getTime(), changes.size())));
            writeSetToGeoJSON(subtractions, new File(String.format("%s/subtractions-%d-%d.geojson",
                    output.get(), new Date().getTime(), subtractions.size())));
        }

        return 0;
    }

    @Override
    public String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public final String getName()
    {
        return NAME;
    }

    @Override
    public Command.SwitchList switches()
    {
        return new Command.SwitchList().with(SOURCE_FILE_PARAMETER, TARGET_FILE_PARAMETER,
                OUTPUT_FOLDER_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.print(
                "-source=path/to/first/geoJSON,path/to/second/geoJSON : geoJSON to compare changes from\n");
        writer.print(
                "-target=path/to/first/geoJSON,path/to/second/geoJSON : geoJSON to compare changes to\n");
        writer.print(
                "-output=path/to/output/folder : optional directory to write output files to\n");
    }

    /**
     * Takes an {@link InputStream} of a json file, reads it, and returns it as a
     * {@link JsonObject}.
     *
     * @param input
     *            {@link InputStream} to read
     * @return a {@link JsonObject} representation of the json file
     * @throws IOException
     */
    private JsonObject jsonReader(final InputStream input) throws IOException
    {
        final JsonReader reader = new JsonReader(new InputStreamReader(input));
        final Gson gson = new Gson();
        return gson.fromJson(reader, JsonObject.class);
    }

    /**
     * Parses an atlas-checks geoJSON flag file and maps each feature to its 'id' parameter.
     *
     * @param stream
     *            {@link InputStream} of an atlas-checks geoJSON flag file
     * @return {@link HashMap} of {@link JsonObject}s representing geoJSON features, mapped by id
     */
    private HashMap<String, JsonObject> mapFeatures(final InputStream stream)
    {
        final HashMap featureMap = new HashMap<String, JsonObject>();
        try
        {
            final JsonObject source = jsonReader(stream);
            source.get("features").getAsJsonArray()
                    .forEach(feature -> featureMap.put(
                            feature.getAsJsonObject().get("id").getAsString(),
                            feature.getAsJsonObject()));
        }
        catch (final IOException exc)
        {
            Streams.close(stream);
            exc.printStackTrace();
        }
        return featureMap;
    }

    /**
     * Takes two {@link HashMap}s containing atlas-checks geoJSON features mapped by id. Finds
     * missing elements in the target based on keys (geoJSON feature 'id' parameter). Optionally
     * computes changes in the Atlas object ids found in each feature.
     *
     * @param source
     *            {@link HashMap} of the features to compare from
     * @param target
     *            {@link HashMap} of the features to compare to
     * @param onlyMissing
     *            {@code boolean} value to not calculate changes in Atlas object ids
     * @return an {@link ArrayList} of 2 {@link HashSet}s of {@link JsonObject}s, the first being
     *         the missing features and the second being the changed features
     */
    private ArrayList<HashSet<JsonObject>> getMissingAndChanged(
            final HashMap<String, JsonObject> source, final HashMap<String, JsonObject> target,
            final boolean onlyMissing)
    {
        final HashSet<JsonObject> missing = new HashSet<>();
        final HashSet<JsonObject> changed = new HashSet<>();
        source.forEach((identifier, feature) ->
        {
            // Get missing
            if (!target.containsKey(identifier))
            {
                missing.add(feature);
            }
            // If not missing, check for Atlas id changes
            else if (!onlyMissing && !identicalFeatureIds(
                    feature.get("properties").getAsJsonObject().get("feature_properties")
                            .getAsJsonArray(),
                    target.get(identifier).getAsJsonObject().get("properties").getAsJsonObject()
                            .get("feature_properties").getAsJsonArray()))
            {
                changed.add(feature);
            }
        });
        final ArrayList<HashSet<JsonObject>> missingAndChanged = new ArrayList<>();
        missingAndChanged.add(missing);
        missingAndChanged.add(changed);
        return missingAndChanged;
    }

    /**
     * Helper function for {@code getMissingAndChanged} to parse and check for changes in Atlas ids.
     *
     * @param sourceArray
     *            {@code feature_properties} {@link JsonArray} to check from
     * @param targetArray
     *            {@code feature_properties} {@link JsonArray} to check to
     * @return true if all Atlas ids in {@code source} are present in {@code target}, and visa versa
     */
    private boolean identicalFeatureIds(final JsonArray sourceArray, final JsonArray targetArray)
    {
        final ArrayList<String> sourceIds = new ArrayList<>();
        final ArrayList<String> targetIds = new ArrayList<>();
        sourceArray.forEach(
                object -> sourceIds.add(object.getAsJsonObject().get("ItemId").getAsString()));
        targetArray.forEach(
                object -> targetIds.add(object.getAsJsonObject().get("ItemId").getAsString()));
        return sourceIds.containsAll(targetIds) && targetIds.containsAll(sourceIds);
    }

    /**
     * Writes a Set of geoJSON features to a geoJSON file.
     *
     * @param features
     *            {@link Set} of {@link JsonObject}s representing geoJSON features
     * @param output
     *            {@link File} to output to
     */
    private void writeSetToGeoJSON(final Set<JsonObject> features, final File output)
    {
        final JsonArray featureArray = new JsonArray();
        features.forEach(featureArray::add);
        final JsonObject featureCollection = new JsonObject();
        featureCollection.add("type", new JsonPrimitive("FeatureCollection"));
        featureCollection.add("features", featureArray);

        final JsonWriter writer = new JsonWriter(output);
        writer.write(featureCollection);
        writer.close();
    }
}
