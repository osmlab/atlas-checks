package org.openstreetmap.atlas.checks.commands;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

    static final String CHECK_COUNT_FORMAT = "%s: %d%n";

    // Atlas Checks' GeoJSON strings
    static final String FEATURE_PROPERTIES = "feature_properties";
    static final String GENERATOR = "generator";
    static final String NAME = "name";

    private final Gson gson = new Gson();
    private Map<String, Map<Set<String>, JsonObject>> source = new HashMap<>();
    private Map<String, Map<Set<String>, JsonObject>> target = new HashMap<>();

    private String commandName;
    private String description;
    private String fileExtension;

    public JSONFlagDiffSubCommand(final String name, final String description,
            final String fileExtension)
    {
        this.commandName = name;
        this.description = description;
        this.fileExtension = fileExtension;
    }

    @Override
    public String getName()
    {
        return this.commandName;
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
    // Allow System.out for clean printing.
    @SuppressWarnings("squid:S106")
    public int execute(final CommandMap command)
    {
        // Get files and parse to maps
        this.getFilesOfType((File) command.get(SOURCE_FILE_PARAMETER))
                .forEach(path -> this.source = this.mergeMaps(this.mapFeatures(path), this.source));
        this.getFilesOfType((File) command.get(TARGET_FILE_PARAMETER))
                .forEach(path -> this.target = this.mergeMaps(this.mapFeatures(path), this.target));

        // Get changes from source to target
        final Map<String, Set<JsonObject>> additions = this.getDiff(this.target, this.source);
        final Map<String, Set<JsonObject>> subtractions = this.getDiff(this.source, this.target);

        // Write outputs
        System.out.printf("%nTotal Items: %d%n",
                this.getSourceSize() + this.countMapValues(additions));
        System.out.printf("%nAdditions: %d%n", this.countMapValues(additions));
        additions.forEach((check, set) -> System.out.printf(CHECK_COUNT_FORMAT, check, set.size()));
        System.out.printf("%nSubtractions: %d%n", this.countMapValues(subtractions));
        subtractions
                .forEach((check, set) -> System.out.printf(CHECK_COUNT_FORMAT, check, set.size()));

        final Optional output = command.getOption(OUTPUT_FOLDER_PARAMETER);
        if (output.isPresent())
        {
            writeSetToGeoJSON(additions,
                    new File(String.format("%s/additions-%d-%d.%s", output.get(),
                            new Date().getTime(), this.countMapValues(additions),
                            this.fileExtension)));
            writeSetToGeoJSON(subtractions,
                    new File(String.format("%s/subtractions-%d-%d.%s", output.get(),
                            new Date().getTime(), this.countMapValues(subtractions),
                            this.fileExtension)));
        }

        return 0;
    }

    /**
     * Given a folder, gathers all files that have a file extension matching {@link #fileExtension}.
     *
     * @param file
     *            a folder with the files to gather.
     * @return a {@link Set} of {@link File}s
     */
    private Set<File> getFilesOfType(final File file)
    {
        final String fileName = file.isGzipped() ? FilenameUtils.getBaseName(file.getName())
                : file.getName();
        if (FilenameUtils.getExtension(fileName).equalsIgnoreCase(this.fileExtension))
        {
            return Collections.singleton(file);
        }
        else if (file.isDirectory())
        {
            return file.listFilesRecursively().stream().filter(this::checkFileExtension)
                    .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    /**
     * Checks the file extension of the input file
     *
     * @param file
     *            Input file
     * @return true if the file has the given extension
     */
    private boolean checkFileExtension(final File file)
    {
        return FilenameUtils.getExtension(
                file.isGzipped() ? FilenameUtils.getBaseName(file.getName()) : file.getName())
                .equalsIgnoreCase(this.fileExtension);
    }

    /**
     * Merges one 2d check and flags {@link HashMap} into another.
     *
     * @param put
     *            a 2d check and flags {@link HashMap} that will be put into {@code place} to form
     *            the output
     * @param place
     *            2d check and flags {@link HashMap} that will have {@code put} placed into it to
     *            form the output
     * @return a merged 2d check and flags {@link HashMap}
     */
    private Map<String, Map<Set<String>, JsonObject>> mergeMaps(
            final Map<String, Map<Set<String>, JsonObject>> put,
            final Map<String, Map<Set<String>, JsonObject>> place)
    {
        final Map<String, Map<Set<String>, JsonObject>> mergedMap = new HashMap<>(place);
        put.forEach((check, flags) ->
        {
            mergedMap.putIfAbsent(check, new HashMap<>());
            mergedMap.get(check).putAll(flags);
        });
        return mergedMap;
    }

    /**
     * Gets a count of the {@link JsonObject}s a {@link Map} of {@link Set}s of {@link JsonObject}s.
     *
     * @param map
     *            a {@link Map} of {@link Set}s of {@link JsonObject}s
     * @return long count of {@link JsonObject}s
     */
    private long countMapValues(final Map<String, Set<JsonObject>> map)
    {
        return map.values().stream().mapToLong(Collection::size).sum();
    }

    /**
     * Parses an atlas-checks flag file and maps each flag to its id.
     *
     * @param file
     *            {@link File} containing the flags
     * @return a 2d {@link HashMap} containing a {@link HashMap} of {@link JsonObject}s mapped to
     *         {@link String} feature ids, mapped to {@link String} check names.
     */
    protected abstract Map<String, Map<Set<String>, JsonObject>> mapFeatures(File file);

    /**
     * Takes two 2d {@link HashMap}s containing atlas-checks flags mapped by id mapped by check.
     * Finds missing elements in the target based on ids.
     *
     * @param source
     *            {@link HashMap} of the flags to compare from
     * @param target
     *            {@link HashMap} of the flags to compare to
     * @return a {@link Map} of {@link JsonObject} by check
     */
    protected Map<String, Set<JsonObject>> getDiff(
            final Map<String, Map<Set<String>, JsonObject>> source,
            final Map<String, Map<Set<String>, JsonObject>> target)
    {
        final Map<String, Set<JsonObject>> diff = new HashMap<>();
        source.forEach((check, flags) -> flags.forEach((identifier, flag) ->
        {
            // Get missing
            if (!target.containsKey(check) || !target.get(check).containsKey(identifier))
            {
                diff.putIfAbsent(check, new HashSet<>());
                diff.get(check).add(flag);
            }
        }));
        return diff;
    }

    /**
     * Writes a Set of geoJSON atlas-checks flags to a file.
     *
     * @param flags
     *            {@link Set} of {@link JsonObject}s representing geoJSON flags
     * @param output
     *            {@link File} to output to
     */
    protected void writeSetToGeoJSON(final Map<String, Set<JsonObject>> flags, final File output)
    {
        final JsonWriter writer = new JsonWriter(output);
        flags.values().stream().flatMap(Collection::stream).forEach(writer::writeLine);
        writer.close();
    }

    /**
     * A getter for the number of flags in {@link #source}
     *
     * @return {@code int}
     */
    protected int getSourceSize()
    {
        int sourceSize = 0;
        for (final String check : getSource().keySet())
        {
            sourceSize += getSource().get(check).size();
        }
        return sourceSize;
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
    protected Map<String, Map<Set<String>, JsonObject>> getSource()
    {
        return this.source;
    }

    /**
     * Getter for {@link #target}
     *
     * @return {@link HashMap}
     */
    protected Map<String, Map<Set<String>, JsonObject>> getTarget()
    {
        return this.target;
    }
}
