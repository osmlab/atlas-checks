package org.openstreetmap.atlas.checks.commands;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

/**
 * Takes 2 sets of atlas-checks log files and reports the number of additions, subtractions, and
 * changed flags from source to target. Optionally, the reported items can be written to new log
 * files.
 *
 * @author bbreithaupt
 */
public class AtlasChecksLogDiffSubCommand implements FlexibleSubCommand
{
    private static final Switch<Set<String>> SOURCE_FILE_PARAMETER = new Switch<>("source",
            "A comma separated set of log files containing atlas-checks flags to compare changes from.",
            csv -> Stream.of(csv.split(",")).collect(Collectors.toSet()), Optionality.REQUIRED);

    private static final Switch<Set<String>> TARGET_FILE_PARAMETER = new Switch<>("target",
            "A comma separated set of log files containing atlas-checks flags to compare changes to.",
            csv -> Stream.of(csv.split(",")).collect(Collectors.toSet()), Optionality.REQUIRED);

    private static final Switch<String> OUTPUT_FOLDER_PARAMETER = new Switch<>("output",
            "A directory to place output log files in. If not included no outputs files will be written.",
            String::new, Optionality.OPTIONAL);

    private static final String NAME = "log-diff";
    private static final String DESCRIPTION = "Takes 2 sets of atlas-checks log flag files and reports the number of additions, subtractions, and changed flags from source to target.";

    private final HashMap<String, HashMap<String, JsonObject>> source = new HashMap<>();
    private final HashMap<String, HashMap<String, JsonObject>> target = new HashMap<>();

    @Override
    public int execute(final CommandMap command)
    {
        // Get logs and parse to maps
        ((Set) command.get(SOURCE_FILE_PARAMETER))
                .forEach(path -> mapFeatures(new File((String) path), this.source));
        ((Set) command.get(TARGET_FILE_PARAMETER))
                .forEach(path -> mapFeatures(new File((String) path), this.target));

        // Get changes from source to target
        final HashSet<JsonObject> additions = getMissingAndChanged(this.target, this.source, true)
                .get(0);
        final ArrayList<HashSet<JsonObject>> subAndChange = getMissingAndChanged(this.source,
                this.target, false);
        final HashSet<JsonObject> subtractions = subAndChange.get(0);
        final HashSet<JsonObject> changes = subAndChange.get(1);

        // Write outputs
        int sourceSize = 0;
        for (final String check : this.source.keySet())
        {
            sourceSize += this.source.get(check).size();
        }
        System.out.printf(
                "\n Total Items: %d\n   Additions: %d\n     Changes: %d\nSubtractions: %d\n",
                sourceSize + additions.size(), additions.size(), changes.size(),
                subtractions.size());
        final Optional output = command.getOption(OUTPUT_FOLDER_PARAMETER);
        if (output.isPresent())
        {
            writeSetToGeoJSON(additions, new File(String.format("%s/additions-%d-%d.log",
                    output.get(), new Date().getTime(), additions.size())));
            writeSetToGeoJSON(changes, new File(String.format("%s/changes-%d-%d.log", output.get(),
                    new Date().getTime(), changes.size())));
            writeSetToGeoJSON(subtractions, new File(String.format("%s/subtractions-%d-%d.log",
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
                "-source=path/to/first/log,path/to/second/log : log to compare changes from\n");
        writer.print("-target=path/to/first/log,path/to/second/log : log to compare changes to\n");
        writer.print(
                "-output=path/to/output/folder : optional directory to write output files to\n");
    }

    /**
     * Takes a json {@link String}, reads it, and returns it as a {@link JsonObject}.
     *
     * @param json
     *            {@link String} to read
     * @return a {@link JsonObject} representation of the json file
     */
    private JsonObject jsonReader(final String json)
    {
        final Gson gson = new Gson();
        return gson.fromJson(json, JsonObject.class);
    }

    /**
     * Parses an atlas-checks log file and maps each flag to its 'id' parameter.
     *
     * @param file
     *            {@link File} of an atlas-checks log file
     * @return {@link HashMap} of {@link JsonObject}s representing geoJSON feature collections,
     *         mapped by flag id
     */
    private void mapFeatures(final File file,
            final HashMap<String, HashMap<String, JsonObject>> map)
    {
        try
        {
            final BufferedReader reader = new BufferedReader(new FileReader(file.getPath()));
            String line;
            // Read each line (flag) from the log file
            while ((line = reader.readLine()) != null)
            {
                // Parse the json
                final JsonObject source = jsonReader(line);
                // Get the check name
                final String checkName = source.get("properties").getAsJsonObject().get("generator")
                        .getAsString();
                // Add the check name as a key
                if (!map.containsKey(checkName))
                {
                    map.put(checkName, new HashMap<>());
                }
                // Add the geoJSON as a value
                map.get(checkName).put(
                        source.get("properties").getAsJsonObject().get("id").getAsString(), source);
            }
        }
        catch (final IOException exc)
        {
            exc.printStackTrace();
        }
    }

    /**
     * Takes two {@link HashMap}s containing atlas-checks geoJSON flags mapped by id. Finds missing
     * elements in the target based on keys (geoJSON FeatureCollection 'properties.id' parameter).
     * Optionally computes changes in the Atlas object ids found in each flag.
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
            final HashMap<String, HashMap<String, JsonObject>> source,
            final HashMap<String, HashMap<String, JsonObject>> target, final boolean onlyMissing)
    {
        final HashSet<JsonObject> missing = new HashSet<>();
        final HashSet<JsonObject> changed = new HashSet<>();
        source.forEach((check, flag) ->
        {
            flag.forEach((identifier, featureCollection) ->
            {
                // Get missing
                if (!target.containsKey(check) || !target.get(check).containsKey(identifier))
                {
                    missing.add(featureCollection);
                }
                // If not missing, check for Atlas id changes
                else if (!onlyMissing
                        && !identicalFeatureIds(featureCollection.get("features").getAsJsonArray(),
                                target.get(check).get(identifier).get("features").getAsJsonArray()))
                {
                    changed.add(featureCollection);
                }
            });
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
     *            {@code feature} {@link JsonArray} to check from
     * @param targetArray
     *            {@code feature} {@link JsonArray} to check to
     * @return true if all Atlas ids in {@code source} are present in {@code target}, and visa versa
     */
    private boolean identicalFeatureIds(final JsonArray sourceArray, final JsonArray targetArray)
    {
        final ArrayList<String> sourceIds = new ArrayList<>();
        final ArrayList<String> targetIds = new ArrayList<>();
        // The array must be the sam size to match
        if (sourceArray.size() != targetArray.size())
        {
            return false;
        }
        // Gather all the source ids
        sourceArray.forEach(object ->
        {
            // Handle Locations that were added and don't have an id
            if (object.getAsJsonObject().get("properties").getAsJsonObject().has("ItemId"))
            {
                sourceIds.add(object.getAsJsonObject().get("properties").getAsJsonObject()
                        .get("ItemId").getAsString());
            }
        });
        // Gather all the target ids
        targetArray.forEach(object ->
        {
            // Handle Locations that were added and don't have an id
            if (object.getAsJsonObject().get("properties").getAsJsonObject().has("ItemId"))
            {
                targetIds.add(object.getAsJsonObject().get("properties").getAsJsonObject()
                        .get("ItemId").getAsString());
            }
        });
        // Compare the two id lists
        return sourceIds.containsAll(targetIds) && targetIds.containsAll(sourceIds);
    }

    /**
     * Writes a Set of geoJSON feature collections to a log file.
     *
     * @param flags
     *            {@link Set} of {@link JsonObject}s representing geoJSON feature collections
     * @param output
     *            {@link File} to output to
     */
    private void writeSetToGeoJSON(final Set<JsonObject> flags, final File output)
    {
        final JsonWriter writer = new JsonWriter(output);
        flags.forEach(writer::writeLine);
        writer.close();
    }
}
