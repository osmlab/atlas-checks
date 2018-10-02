package org.openstreetmap.atlas.checks.commands;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
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
public abstract class JSONFlagDiffSubCommand implements FlexibleSubCommand
{
    private static final Command.Switch<File> SOURCE_FILE_PARAMETER = new Command.Switch<>("source",
            "A file or directory of files containing atlas-checks flags to compare changes from.",
            File::new, Command.Optionality.REQUIRED);

    private static final Command.Switch<File> TARGET_FILE_PARAMETER = new Command.Switch<>("target",
            "A file or directory of files containing atlas-checks flags to compare changes to.",
            File::new, Command.Optionality.REQUIRED);

    private static final Command.Switch<String> OUTPUT_FOLDER_PARAMETER = new Command.Switch<>(
            "output",
            "A directory to place output log files in. If not included no outputs files will be written.",
            String::new, Command.Optionality.OPTIONAL);

    // JSON strings
    static final String FEATURE_COLLECTION = "FeatureCollection";
    static final String FEATURE_PROPERTIES = "feature_properties";
    static final String FEATURES = "features";
    static final String GENERATOR = "generator";
    static final String ID = "id";
    static final String ITEM_ID = "ItemId";
    static final String PROPERTIES = "properties";
    static final String TYPE = "type";

    private final Gson gson = new Gson();
    private final HashMap source = new HashMap();
    private final HashMap target = new HashMap();

    private String name;
    private String description;
    private String fileExtension;

    /**
     * Expected returns from {@link #getDiff(HashMap, HashMap, DiffReturn)}.
     */
    protected enum DiffReturn
    {
        MISSING,
        CHANGED,
    }

    /**
     * Helper class for storing diff results.
     */
    protected class JSONFlagDiff
    {
        private final HashSet<JsonObject> missing = new HashSet<>();
        private final HashSet<JsonObject> changed = new HashSet<>();

        public void addMissing(final JsonObject object)
        {
            this.missing.add(object);
        }

        public void addChanged(final JsonObject object)
        {
            this.changed.add(object);
        }

        public HashSet<JsonObject> getMissing()
        {
            return missing;
        }

        public HashSet<JsonObject> getChanged()
        {
            return changed;
        }
    }

    public JSONFlagDiffSubCommand(final String name, final String description,
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
        getFilesOfType((File) command.get(SOURCE_FILE_PARAMETER))
                .forEach(path -> this.mapFeatures(path, this.source));
        getFilesOfType((File) command.get(TARGET_FILE_PARAMETER))
                .forEach(path -> this.mapFeatures(path, this.target));

        // Get changes from source to target
        final HashSet<JsonObject> additions = this
                .getDiff(this.target, this.source, DiffReturn.MISSING).getMissing();
        final JSONFlagDiff subAndChange = this.getDiff(this.source, this.target,
                DiffReturn.CHANGED);
        final HashSet<JsonObject> subtractions = subAndChange.getMissing();
        final HashSet<JsonObject> changes = subAndChange.getChanged();

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

    private Set<File> getFilesOfType(final File file)
    {
        if (FilenameUtils.getExtension(file.getName()).equalsIgnoreCase(this.fileExtension))
        {
            return Collections.singleton(file);
        }
        else if (file.isDirectory())
        {
            return file
                    .listFilesRecursively().stream().filter(subFile -> FilenameUtils
                            .getExtension(subFile.getName()).equalsIgnoreCase(this.fileExtension))
                    .collect(Collectors.toSet());
        }
        return new HashSet<>();
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
     * @param returnType
     *            {@link DiffReturn}; If this is {@code CHANGED} the {@code changed} attribute of
     *            the returned {@link JSONFlagDiff} will be populated.
     * @return an {@link JSONFlagDiff}
     */
    protected JSONFlagDiff getDiff(final HashMap source, final HashMap target,
            final DiffReturn returnType)
    {
        return new JSONFlagDiff();
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
