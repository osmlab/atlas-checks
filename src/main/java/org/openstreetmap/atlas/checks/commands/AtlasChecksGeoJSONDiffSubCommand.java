package org.openstreetmap.atlas.checks.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.runtime.FlexibleSubCommand;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AtlasChecksGeoJSONDiffSubCommand implements FlexibleSubCommand
{
    private static final Switch<File> SOURCE_FILE_PARAMETER = new Switch<>("source",
            "A geoJSON containing atlas-checks flags to compare changes from.", File::new,
            Optionality.REQUIRED);

    private static final Switch<File> TARGET_FILE_PARAMETER = new Switch<>("target",
            "A geoJSON containing atlas-checks flags to compare changes to.", File::new,
            Optionality.REQUIRED);

    private static final Switch<File> OUTPUT_FOLDER_PARAMETER = new Switch<>("output",
            "A directory to place output geoJSON files in. If not included no outputs files will be written.", File::new,
            Optionality.OPTIONAL);

    private static final String NAME = "geoJSON-diff";
    private static final String DESCRIPTION = "Takes 2 atlas-checks geoJSON flag files and reports the number of additions, subtractions, and changed flags from source to target.";

    @Override
    public int execute(final CommandMap command)
    {
        final HashMap source = mapFeatures(((File) command.get(SOURCE_FILE_PARAMETER)).read());
        final HashMap target = mapFeatures(((File) command.get(TARGET_FILE_PARAMETER)).read());
        final HashSet<JsonObject> additions = (HashSet<JsonObject>) getMissingAndChanged(target, source, true).get(0);
        final ArrayList<HashSet<JsonObject>> subAndChange = getMissingAndChanged(source, target, false);
        final HashSet<JsonObject> subtractions = subAndChange.get(0);
        final HashSet<JsonObject> changes = subAndChange.get(1);
        System.out.printf("\n Total Items: %d\n   Additions: %d\n     Changes: %d\nSubtractions: %d\n", source.size()+additions.size(), additions.size(), changes.size(), subtractions.size());

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
        return new Command.SwitchList().with(SOURCE_FILE_PARAMETER, TARGET_FILE_PARAMETER, OUTPUT_FOLDER_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.print("-source=path/to/first/geoJSON : geoJSON to compare changes from\n");
        writer.print("-target=path/to/second/geoJSON : geoJSON to compare changes to\n");
        writer.print("-output=path/to/first/geoJSON : optional directory to write output files to\n");
    }

    private JsonObject JsonReader(final InputStream input) throws IOException
    {
        JsonReader reader = new JsonReader(new InputStreamReader(input));
        Gson gson = new Gson();
        return gson.fromJson(reader, JsonObject.class);
    }

    private HashMap<String, JsonObject> mapFeatures(final InputStream stream){
        HashMap featureMap = new HashMap<String, JsonObject>();
        try
        {
            final JsonObject source = JsonReader(stream);
            source.get("features").getAsJsonArray().forEach(feature -> featureMap.put(feature.getAsJsonObject().get("id").getAsString(), feature.getAsJsonObject()));
        }
        catch (IOException e)
        {
            Streams.close(stream);
            e.printStackTrace();
        }
        return featureMap;
    }

    private ArrayList<HashSet<JsonObject>> getMissingAndChanged(final HashMap<String, JsonObject> source, final HashMap<String, JsonObject> target, boolean onlyMissing){
        final HashSet<JsonObject> missing = new HashSet<>();
        final HashSet<JsonObject> changed = new HashSet<>();
        source.forEach((id, feature) -> {
            if (!target.containsKey(id)){
                missing.add(feature);
            }
            else if (!onlyMissing && !identicalFeatureIds(feature.get("properties").getAsJsonObject().get("feature_properties").getAsJsonArray(), target.get(id).getAsJsonObject().get("properties").getAsJsonObject().get("feature_properties").getAsJsonArray())){
                changed.add(feature);
            }
        });
        final ArrayList<HashSet<JsonObject>> missingAndChanged =  new ArrayList<>();
        missingAndChanged.add(missing);
        missingAndChanged.add(changed);
        return missingAndChanged;
    }

    private boolean identicalFeatureIds(final JsonArray sourceArray, final JsonArray targetArray){
        ArrayList<String> sourceIds = new ArrayList<>();
        ArrayList<String> targetIds = new ArrayList<>();
        sourceArray.forEach(object -> sourceIds.add(object.getAsJsonObject().get("ItemId").getAsString()));
        targetArray.forEach(object -> targetIds.add(object.getAsJsonObject().get("ItemId").getAsString()));
        return sourceIds.containsAll(targetIds) && targetIds.containsAll(sourceIds);
    }
}
