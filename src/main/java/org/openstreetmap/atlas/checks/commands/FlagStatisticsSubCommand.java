package org.openstreetmap.atlas.checks.commands;

import static org.openstreetmap.atlas.checks.constants.CommonConstants.COMMA;
import static org.openstreetmap.atlas.checks.constants.CommonConstants.LINE_SEPARATOR;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.openstreetmap.atlas.checks.configuration.ConfigurationResolver;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.scalars.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * This command takes a folder of directories of atlas-checks log files and counts the number of
 * flags per country per check. It optionally takes a second folder and returns the difference
 * between the inputs.
 *
 * @author bbreithaupt
 */
public class FlagStatisticsSubCommand extends AbstractAtlasShellToolsCommand
{
    private static final String INPUT_OPTION = "input";
    private static final String DIFFERENCE_OPTION = "difference";
    private static final String OUTPUT_OPTION = "output";
    private static final String GENERATOR = "generator";

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationResolver.class);

    private final Gson gson = new Gson();
    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new FlagStatisticsSubCommand().runSubcommandAndExit(args);
    }

    public FlagStatisticsSubCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    @SuppressWarnings("squid:S3655")
    public int execute()
    {
        // Read the main input folder
        final Map<String, Map<String, Counter>> inputCounts = this.getCountryCheckCounts(
                this.optionAndArgumentDelegate.getOptionArgument(INPUT_OPTION).get());

        final Optional<String> differenceFilePath = this.optionAndArgumentDelegate
                .getOptionArgument(DIFFERENCE_OPTION);
        try
        {
            // If a second input is supplied..
            if (differenceFilePath.isPresent())
            {
                // Read the second input
                final Map<String, Map<String, Counter>> targetCounts = this
                        .getCountryCheckCounts(differenceFilePath.get());
                // Get the difference between the outputs and output it
                this.writeOutput(getDifference(inputCounts, targetCounts),
                        this.optionAndArgumentDelegate.getOptionArgument(OUTPUT_OPTION));
            }
            else
            {
                // Else, just output the counts from the main input
                this.writeOutput(inputCounts,
                        this.optionAndArgumentDelegate.getOptionArgument(OUTPUT_OPTION));
            }
        }
        catch (final IOException exception)
        {
            this.outputDelegate.printlnStderr(exception.toString());
            return 1;
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "flag-stats";
    }

    @Override
    public String getSimpleDescription()
    {
        return "get flag counts for a set of atlas checks' log files";
    }

    @Override
    public void registerManualPageSections()
    {
        this.addManualPageSection("DESCRIPTION", FlagStatisticsSubCommand.class
                .getResourceAsStream("FlagStatisticsSubCommandDescriptionSection.txt"));
        this.addManualPageSection("EXAMPLES", FlagStatisticsSubCommand.class
                .getResourceAsStream("FlagStatisticsSubCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        this.registerOptionWithRequiredArgument(INPUT_OPTION, 'i',
                "A directory of folders containing atlas-checks log files.",
                OptionOptionality.REQUIRED, INPUT_OPTION);
        this.registerOptionWithRequiredArgument(DIFFERENCE_OPTION, 'd',
                "A second set of log files to diff against.", OptionOptionality.OPTIONAL,
                DIFFERENCE_OPTION);
        this.registerOptionWithRequiredArgument(OUTPUT_OPTION, 'o', "A csv to output results to.",
                OptionOptionality.OPTIONAL, OUTPUT_OPTION);
        super.registerOptionsAndArguments();
    }

    /**
     * Given a path to a folder, read all log files and map the counts of each check by country.
     *
     * @param path
     *            {@link String} folder path
     * @return a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *         {@link String}
     */
    private Map<String, Map<String, Counter>> getCountryCheckCounts(final String path)
    {
        final Map<String, Map<String, Counter>> countryCheckMap = new HashMap<>();
        logger.info("Reading files from: {}", path);

        // Check all files in the folder and all sub-folders
        new File(path).listFilesRecursively().stream()
                // Filter the files to only include log files, either gzipped or uncompressed
                .filter(file -> FilenameUtils
                        .getExtension(file.isGzipped() ? FilenameUtils.getBaseName(file.getName())
                                : file.getName())
                        .equalsIgnoreCase("log"))
                .forEach(file ->
                {
                    logger.info("Reading: {}", file.getName());
                    // Get the parent folder name and assume it is a county code
                    final String country = FilenameUtils.getName(file.getParent());
                    // Add the country to the map
                    countryCheckMap.putIfAbsent(country, new HashMap<>());

                    // Read the log file
                    try (InputStreamReader inputStreamReader = file.isGzipped()
                            ? new InputStreamReader(
                                    new GZIPInputStream(new FileInputStream(file.getFile())))
                            : new FileReader(file.getPath()))
                    {
                        try (BufferedReader reader = new BufferedReader(inputStreamReader))
                        {
                            String line;
                            // Read each line (flag) from the log file
                            while ((line = reader.readLine()) != null)
                            {
                                // Parse the json
                                final JsonObject source = this.gson.fromJson(line,
                                        JsonObject.class);
                                // Get the check name
                                final String checkName = source.get(PROPERTIES).getAsJsonObject()
                                        .get(GENERATOR).getAsString();
                                // Add the check to the map
                                countryCheckMap.get(country).putIfAbsent(checkName, new Counter());
                                // Increment the counter for the check/country
                                countryCheckMap.get(country).get(checkName).increment();
                            }
                        }
                    }
                    catch (final IOException exception)
                    {
                        this.outputDelegate.printlnWarnMessage(
                                "File read failed with exception" + exception.getMessage());
                    }
                });

        return countryCheckMap;
    }

    /**
     * Converts a map of flag counts per check per country into a printable csv table, and writes it
     * to standard out. Optionally also prints the table to a supplied file path.
     *
     * @param countryCheckCounts
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *            {@link String}
     * @param outputPath
     *            an {@link Optional} {@link String} file path
     * @throws IOException
     */
    private void writeOutput(final Map<String, Map<String, Counter>> countryCheckCounts,
            final Optional<String> outputPath) throws IOException
    {
        logger.info("Generating output");
        // Get a list of country names in alphabetical order
        final List<String> countries = new ArrayList<>(countryCheckCounts.keySet());
        java.util.Collections.sort(countries);
        // Get a list of check names in alphabetical order
        final List<String> checks = countryCheckCounts.entrySet().stream()
                .flatMap(entry -> entry.getValue().keySet().stream()).distinct().sorted()
                .collect(Collectors.toList());
        // A list to hold all the output strings as a table of lines
        final List<List<String>> outputLines = new ArrayList<>();

        // Generate the header row with all country names
        final List<String> headers = new ArrayList<>();
        headers.add("Check");
        headers.addAll(countries);
        headers.add("Total");
        outputLines.add(headers);

        // Generate a row for each check
        checks.forEach(check ->
        {
            final List<String> checkRow = new ArrayList<>();
            checkRow.add(check);
            // Get the value of the check for each country
            checkRow.addAll(countries.stream().map(country ->
            {
                if (countryCheckCounts.get(country).containsKey(check))
                {
                    return String.valueOf(countryCheckCounts.get(country).get(check).getValue());
                }
                // If there is no value for a country us ND for No Data
                return "ND";
            }).collect(Collectors.toList()));
            // Get the total count for this check across all countries
            checkRow.add(String.valueOf((Long) countries.stream()
                    .filter(country -> countryCheckCounts.get(country).containsKey(check))
                    .mapToLong(country -> countryCheckCounts.get(country).get(check).getValue())
                    .sum()));
            outputLines.add(checkRow);
        });

        // Get totals for all the countries
        final List<String> totals = new ArrayList<>();
        totals.add("Total");
        // Calculate the totals and store them
        final List<Long> countryCounts = countries.stream()
                .map(country -> countryCheckCounts.get(country).entrySet().stream()
                        .mapToLong(entry -> entry.getValue().getValue()).sum())
                .collect(Collectors.toList());
        // Convert the totals to strings
        totals.addAll(countryCounts.stream().map(String::valueOf).collect(Collectors.toList()));
        // Sum the totals to get a total number of flags for all countries and checks
        totals.add(String.valueOf(countryCounts.stream().mapToLong(Long::longValue).sum()));
        outputLines.add(totals);

        // Generate a string from the list of lines
        final String outputString = outputLines.stream().map(line -> String.join(COMMA, line))
                .collect(Collectors.joining(LINE_SEPARATOR));

        // If an output path is supplied write the table to the file
        if (outputPath.isPresent())
        {
            try (FileWriter outputWriter = new FileWriter(new File(outputPath.get()).getFile()))
            {
                outputWriter.write(outputString);
            }
        }

        // Print the table to standard out
        this.outputDelegate.printStdout(LINE_SEPARATOR + outputString + LINE_SEPARATOR);
    }

    /**
     * Given two maps of counts per check per country, get the difference between the counts.
     *
     * @param source
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *            {@link String}
     * @param target
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *            {@link String}
     * @return a 2D {@link Map} of flag difference {@link Counter}s per check {@link String} per
     *         country {@link String}
     */
    private Map<String, Map<String, Counter>> getDifference(
            final Map<String, Map<String, Counter>> source,
            final Map<String, Map<String, Counter>> target)
    {
        final Map<String, Map<String, Counter>> difference = new HashMap<>();

        // For each counter in the source map, add a value to the difference map equal to the
        // mathematical difference of the companion target counter (defaulting to 0 if absent) and
        // the source counter
        source.forEach((country, checkCounts) ->
        {
            difference.putIfAbsent(country, new HashMap<>());
            checkCounts
                    .forEach((check, count) -> difference.get(country).put(check,
                            new Counter(target.getOrDefault(country, new HashMap<>())
                                    .getOrDefault(check, new Counter()).getValue()
                                    - count.getValue())));
        });

        // If a target counter exists but a companion source counter does not, add the value of the
        // target counter to the difference map
        target.forEach((country, checkCounts) ->
        {
            difference.putIfAbsent(country, new HashMap<>());
            checkCounts
                    .forEach((check, count) -> difference.get(country).putIfAbsent(check, count));
        });

        return difference;
    }
}
