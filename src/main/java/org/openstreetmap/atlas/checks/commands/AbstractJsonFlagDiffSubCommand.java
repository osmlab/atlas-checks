package org.openstreetmap.atlas.checks.commands;

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
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.runtime.FlexibleSubCommand;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * An abstract sub-command for creating json and flag based diffing commands.
 *
 * @author bbreithaupt
 */
public abstract class AbstractJsonFlagDiffSubCommand implements FlexibleSubCommand
{
    private static final Command.Switch<Set<String>> SOURCE_FILE_PARAMETER = new Command.Switch<>(
            "source",
            "A comma separated set of files containing atlas-checks flags to compare changes from.",
            csv -> Stream.of(csv.split(",")).collect(Collectors.toSet()),
            Command.Optionality.REQUIRED);

    private static final Command.Switch<Set<String>> TARGET_FILE_PARAMETER = new Command.Switch<>(
            "target",
            "A comma separated set of files containing atlas-checks flags to compare changes to.",
            csv -> Stream.of(csv.split(",")).collect(Collectors.toSet()),
            Command.Optionality.REQUIRED);

    private static final Command.Switch<String> OUTPUT_FOLDER_PARAMETER = new Command.Switch<>(
            "output",
            "A directory to place output log files in. If not included no outputs files will be written.",
            String::new, Command.Optionality.OPTIONAL);

    private final Gson gson = new Gson();
    private final HashMap source = new HashMap();
    private final HashMap target = new HashMap();

    private String name;
    private String description;
    private String fileExtension;

    public AbstractJsonFlagDiffSubCommand(final String name, final String description,
            final String fileExtension)
    {
        this.name = name;
        this.description = description;
        this.fileExtension = fileExtension;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public String getDescription()
    {
        return this.description;
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
                "-source=path/to/first/flag/file,path/to/second/flag/file : file of flags to compare changes from\n");
        writer.print(
                "-target=path/to/first/flag/file,path/to/second/flag/file : file of flags to compare changes to\n");
        writer.print(
                "-output=path/to/output/folder : optional directory to write output files to\n");
    }

    @Override
    public int execute(final CommandMap command)
    {
        // Get files and parse to maps
        ((Set) command.get(SOURCE_FILE_PARAMETER))
                .forEach(path -> this.mapFeatures(new File((String) path), this.source));
        ((Set) command.get(TARGET_FILE_PARAMETER))
                .forEach(path -> this.mapFeatures(new File((String) path), this.target));

        // Get changes from source to target
        final HashSet<JsonObject> additions = getMissingAndChanged(this.target, this.source, true)
                .get(0);
        final ArrayList<HashSet<JsonObject>> subAndChange = getMissingAndChanged(this.source,
                this.target, false);
        final HashSet<JsonObject> subtractions = subAndChange.get(0);
        final HashSet<JsonObject> changes = subAndChange.get(1);

        // Write outputs
        System.out.printf(
                "\n Total Items: %d\n   Additions: %d\n     Changes: %d\nSubtractions: %d\n",
                getSourceSize() + additions.size(), additions.size(), changes.size(),
                subtractions.size());
        final Optional output = command.getOption(OUTPUT_FOLDER_PARAMETER);
        if (output.isPresent())
        {
            writeSetToGeoJSON(additions, new File(String.format("%s/additions-%d-%d.%s",
                    output.get(), new Date().getTime(), additions.size(), this.fileExtension)));
            writeSetToGeoJSON(changes, new File(String.format("%s/changes-%d-%d.%s", output.get(),
                    new Date().getTime(), changes.size(), this.fileExtension)));
            writeSetToGeoJSON(subtractions, new File(String.format("%s/subtractions-%d-%d.%s",
                    output.get(), new Date().getTime(), subtractions.size(), this.fileExtension)));
        }

        return 0;
    }

    /**
     * Parses an atlas-checks flag file and maps each flag to its id.
     *
     * @param file
     *            {@link File} containing the flags
     * @param map
     *            {@link HashMap} to store the mapped flags
     */
    protected void mapFeatures(final File file, final HashMap map)
    {
    }

    /**
     * Takes two {@link HashMap}s containing atlas-checks flags mapped by id. Finds missing elements
     * in the target based on keys. Optionally computes changes in the AtlasObject ids found in each
     * flag.
     *
     * @param source
     *            {@link HashMap} of the flags to compare from
     * @param target
     *            {@link HashMap} of the flags to compare to
     * @param onlyMissing
     *            {@code boolean} value to not calculate changes in Atlas object ids
     * @return an {@link ArrayList} of 2 {@link HashSet}s, the first being the missing features and
     *         the second being the changed features
     */
    protected ArrayList<HashSet<JsonObject>> getMissingAndChanged(final HashMap source,
            final HashMap target, final boolean onlyMissing)
    {
        return new ArrayList<>();
    }

    /**
     * Helper function for {@code getMissingAndChanged} to check for changes in Atlas ids.
     *
     * @param sourceArray
     *            {@code feature} {@link JsonArray} to check from
     * @param targetArray
     *            {@code feature} {@link JsonArray} to check to
     * @return true if all Atlas ids in {@code source} are present in {@code target}, and visa versa
     */
    protected boolean identicalFeatureIds(final JsonArray sourceArray, final JsonArray targetArray)
    {
        return true;
    }

    /**
     * Writes a Set of geoJSON atlas-checks flags to a file.
     *
     * @param flags
     *            {@link Set} of {@link JsonObject}s representing geoJSON flags
     * @param output
     *            {@link File} to output to
     */
    protected void writeSetToGeoJSON(final Set<JsonObject> flags, final File output)
    {
        final JsonWriter writer = new JsonWriter(output);
        flags.forEach(writer::writeLine);
        writer.close();
    }

    /**
     * A getter for the number of flags in {@link #source}
     *
     * @return {@code int}
     */
    protected int getSourceSize()
    {
        return this.source.size();
    }

    /**
     * Getter for {@link #gson}
     *
     * @return {@link Gson}
     */
    protected Gson getGson()
    {
        return this.gson;
    }

    /**
     * Getter for {@link #source}
     *
     * @return {@link HashMap}
     */
    protected HashMap getSource()
    {
        return this.source;
    }

    /**
     * Getter for {@link #target}
     *
     * @return {@link HashMap}
     */
    protected HashMap getTarget()
    {
        return this.target;
    }
}
