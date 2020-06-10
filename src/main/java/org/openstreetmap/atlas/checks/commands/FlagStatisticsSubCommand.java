package org.openstreetmap.atlas.checks.commands;

import static org.openstreetmap.atlas.checks.constants.CommonConstants.COMMA;
import static org.openstreetmap.atlas.checks.constants.CommonConstants.EMPTY_STRING;
import static org.openstreetmap.atlas.checks.constants.CommonConstants.LINE_SEPARATOR;
import static org.openstreetmap.atlas.geography.geojson.GeoJsonConstants.PROPERTIES;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
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
    /**
     * Output type argument values
     */
    private enum OutputTypes
    {
        RUN_SUMMARY,
        CHECK_SUMMARY,
        CHECK_BY_COUNTRY
    }

    private static final String INPUT_OPTION = "input";
    private static final String REFERENCE_OPTION = "reference";
    private static final String OUTPUT_OPTION = "output";
    private static final String OUTPUT_TYPES_OPTION = "output-types";
    private static final String GENERATOR = "generator";
    private static final String CHECK = "Check";
    private static final String INPUT = "Input";
    private static final String REFERENCE = "Reference";
    private static final String DIFFERENCE = "Difference";
    private static final String TOTAL = "Total";
    private static final String SUM_SUFFIX = "(sum)";
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
        // Get the output folder path
        final String outputFolder = this.optionAndArgumentDelegate.getOptionArgument(OUTPUT_OPTION)
                .get();
        // Get the output types
        final List<String> outputTypes = Arrays
                .asList(StringUtils
                        .split(this.optionAndArgumentDelegate.getOptionArgument(OUTPUT_TYPES_OPTION)
                                .orElse(String
                                        .join(COMMA,
                                                Arrays.stream(OutputTypes.values()).map(Enum::name)
                                                        .collect(Collectors.toSet())))
                                .toUpperCase(), ','));

        try
        {
            // Write the counts for the input logs is requested
            if (outputTypes.contains(OutputTypes.RUN_SUMMARY.toString()))
            {
                this.writeCSV(outputFolder + "/runSummary.csv", generateFullOutput(inputCounts));
            }
            // Generate the totals output
            List<List<String>> totalsOutput = generateTotalsOutput(inputCounts);
            // Generate the counts output if requested
            List<List<String>> countsOutput = generateCountsOutput(inputCounts);

            // Get the optional reference input
            final Optional<String> referenceFilePath = this.optionAndArgumentDelegate
                    .getOptionArgument(REFERENCE_OPTION);
            // If a second input is supplied...
            if (referenceFilePath.isPresent())
            {
                // Read the second input
                final Map<String, Map<String, Counter>> referenceCounts = this
                        .getCountryCheckCounts(referenceFilePath.get());

                // Get the difference
                final Map<String, Map<String, Counter>> differenceCounts = this
                        .getDifference(referenceCounts, inputCounts);

                // Write outputs for the difference if requested
                if (outputTypes.contains(OutputTypes.RUN_SUMMARY.toString()))
                {
                    this.writeCSV(outputFolder + "/runSummaryDifference.csv",
                            generateFullOutput(differenceCounts));
                }

                // Add the reference and difference metrics to the totals output
                totalsOutput = addReferenceAndDifferenceToTotalsOutput(totalsOutput,
                        referenceCounts, differenceCounts);
                // Add the reference and difference metrics to the counts output
                countsOutput = addReferenceAndDifferenceToCountsOutput(countsOutput,
                        referenceCounts, differenceCounts);
            }

            // Write the totals output if requested
            if (outputTypes.contains(OutputTypes.CHECK_SUMMARY.toString()))
            {
                this.writeCSV(outputFolder + "/checkSummary.csv", totalsOutput);
            }
            // Write the counts output if requested
            if (outputTypes.contains(OutputTypes.CHECK_BY_COUNTRY.toString()))
            {
                this.writeCSV(outputFolder + "/checkByCountry.csv", countsOutput);
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
        return "flag-statistics";
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
        this.registerOptionWithRequiredArgument(REFERENCE_OPTION, 'r',
                "A second set of log files to diff against.", OptionOptionality.OPTIONAL,
                REFERENCE_OPTION);
        this.registerOptionWithRequiredArgument(OUTPUT_OPTION, 'o',
                "A folder to output results to.", OptionOptionality.REQUIRED, OUTPUT_OPTION);
        this.registerOptionWithRequiredArgument(OUTPUT_TYPES_OPTION, 't',
                "A comma separated list of outputs to generate: run_summary,check_summary,check_by_country",
                OptionOptionality.OPTIONAL, OUTPUT_TYPES_OPTION);
        super.registerOptionsAndArguments();
    }

    /**
     * Appends the counts of two maps of flag counts per check per country to the output of
     * {@link #generateCountsOutput(Map)}.
     *
     * @param stage1Output
     *            a 2D {@link List} of {@link String}s representing a table wher each row is a
     *            country, check, and associated count
     * @param referenceCountryCheckCounts
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *            {@link String}
     * @param diffCountryCheckCounts
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *            {@link String}
     * @return a 2D {@link List} of {@link String}s
     */
    private List<List<String>> addReferenceAndDifferenceToCountsOutput(
            final List<List<String>> stage1Output,
            final Map<String, Map<String, Counter>> referenceCountryCheckCounts,
            final Map<String, Map<String, Counter>> diffCountryCheckCounts)
    {
        // Copy the output lines list
        final List<List<String>> outputLines = new ArrayList<>(stage1Output);

        // Add new headers
        outputLines.get(0).add(2, REFERENCE);
        outputLines.get(0).add(DIFFERENCE);

        // For each row...
        for (int index = 1; index < outputLines.size(); index++)
        {
            // Get the reference value for the country and check
            final Optional<Long> referenceCount = getCountryCheckCount(referenceCountryCheckCounts,
                    stage1Output.get(index).get(0), stage1Output.get(index).get(1));
            // Append the value or an empty string
            stage1Output.get(index).add(2,
                    referenceCount.isPresent() ? String.valueOf(referenceCount.get())
                            : EMPTY_STRING);

            // Get the difference value for the country and check
            final Optional<Long> differenceCount = getCountryCheckCount(diffCountryCheckCounts,
                    stage1Output.get(index).get(0), stage1Output.get(index).get(1));
            // Append the value or an empty string
            stage1Output.get(index)
                    .add(differenceCount.isPresent() ? String.valueOf(differenceCount.get())
                            : EMPTY_STRING);
        }

        return outputLines;
    }

    /**
     * Appends the totals of two maps of flag counts per check per country to the output of
     * {@link #generateTotalsOutput(Map)}.
     *
     * @param stage1Output
     *            a 2D {@link List} of {@link String}s representing a table of checks names and
     *            total counts
     * @param referenceCountryCheckCounts
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *            {@link String}
     * @param diffCountryCheckCounts
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *            {@link String}
     * @return a 2D {@link List} of {@link String}s
     */
    private List<List<String>> addReferenceAndDifferenceToTotalsOutput(
            final List<List<String>> stage1Output,
            final Map<String, Map<String, Counter>> referenceCountryCheckCounts,
            final Map<String, Map<String, Counter>> diffCountryCheckCounts)
    {
        // Copy the output lines list
        final List<List<String>> outputLines = new ArrayList<>(stage1Output);

        // Add new headers
        outputLines.get(0).add(1, REFERENCE + SUM_SUFFIX);
        outputLines.get(0).add(DIFFERENCE + SUM_SUFFIX);

        // For each row...
        for (int index = 1; index < outputLines.size(); index++)
        {
            // Get the check name from the row
            final String check = outputLines.get(index).get(0);
            // Add the reference total
            outputLines.get(index).add(1,
                    String.valueOf(getCheckTotal(referenceCountryCheckCounts, check)));
            // Add the difference total
            outputLines.get(index)
                    .add(String.valueOf(getCheckTotal(diffCountryCheckCounts, check)));
        }

        return outputLines;
    }

    /**
     * Converts a map of flag counts per check per country into a table structure with countries as
     * the first column check names as the second, and counts as the third column. All values are
     * converted to strings for easy conversion to a printable csv string.
     *
     * @param countryCheckCounts
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *            {@link String}
     * @return a 2D {@link List} of {@link String}s
     */
    private List<List<String>> generateCountsOutput(
            final Map<String, Map<String, Counter>> countryCheckCounts)
    {
        // Get a list of country names in alphabetical order
        final List<String> countries = new ArrayList<>(countryCheckCounts.keySet());
        java.util.Collections.sort(countries);
        // Get a list of check names in alphabetical order
        final List<String> checks = countryCheckCounts.entrySet().stream()
                .flatMap(entry -> entry.getValue().keySet().stream()).distinct().sorted()
                .collect(Collectors.toList());
        // A list to hold all the output strings as a table of lines
        final List<List<String>> outputLines = new ArrayList<>();

        // Generate the header row
        final List<String> headers = new ArrayList<>();
        headers.add("Country");
        headers.add(CHECK);
        headers.add(INPUT);
        outputLines.add(headers);

        // For each country and check...
        countries.forEach(country -> checks.forEach(check ->
        {
            final List<String> countryCheckRow = new ArrayList<>();
            // Add the county code
            countryCheckRow.add(country);
            // Add the check name
            countryCheckRow.add(check);

            // Get the optional country check count
            final Optional<Long> count = getCountryCheckCount(countryCheckCounts, country, check);
            if (count.isPresent())
            {
                // If present add the value
                countryCheckRow.add(String.valueOf(count.get()));
            }
            else
            {
                // Else add an empty string
                countryCheckRow.add(EMPTY_STRING);
            }

            outputLines.add(countryCheckRow);
        }));

        return outputLines;
    }

    /**
     * Converts a map of flag counts per check per country into a table structure with checks as
     * rows and countries as columns, including row and column totals. All values are converted to
     * strings for easy conversion to a printable csv string.
     *
     * @param countryCheckCounts
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *            {@link String}
     * @return a 2D {@link List} of {@link String}s
     */
    private List<List<String>> generateFullOutput(
            final Map<String, Map<String, Counter>> countryCheckCounts)
    {
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
        headers.add(CHECK);
        headers.addAll(countries);
        headers.add(TOTAL);
        outputLines.add(headers);

        // Generate a row for each check
        checks.forEach(check ->
        {
            final List<String> checkRow = new ArrayList<>();
            checkRow.add(check);
            // Get the value of the check for each country
            checkRow.addAll(countries.stream().map(country ->
            {
                final Optional<Long> count = getCountryCheckCount(countryCheckCounts, country,
                        check);

                if (count.isPresent())
                {
                    return String.valueOf(count.get());
                }
                // If there is no value for a country return an empty string
                return EMPTY_STRING;
            }).collect(Collectors.toList()));
            // Get the total count for this check across all countries
            checkRow.add(String.valueOf(getCheckTotal(countryCheckCounts, check)));
            outputLines.add(checkRow);
        });

        // Get totals for all the countries
        final List<String> totals = new ArrayList<>();
        totals.add(TOTAL);
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

        return outputLines;
    }

    /**
     * Converts a map of flag counts per check per country into a table structure with checks as the
     * first column and total check counts as the second. All values are converted to strings for
     * easy conversion to a printable csv string.
     *
     * @param countryCheckCounts
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *            {@link String}
     * @return a 2D {@link List} of {@link String}s
     */
    private List<List<String>> generateTotalsOutput(
            final Map<String, Map<String, Counter>> countryCheckCounts)
    {
        // Get a list of check names in alphabetical order
        final List<String> checks = countryCheckCounts.entrySet().stream()
                .flatMap(entry -> entry.getValue().keySet().stream()).distinct().sorted()
                .collect(Collectors.toList());
        // A list to hold all the output strings as a table of lines
        final List<List<String>> outputLines = new ArrayList<>();

        // Generate the header row
        final List<String> headers = new ArrayList<>();
        headers.add(CHECK);
        headers.add(INPUT + SUM_SUFFIX);
        outputLines.add(headers);

        // For each check...
        checks.forEach(check ->
        {
            final List<String> checkRow = new ArrayList<>();
            // Add the check name
            checkRow.add(check);
            // Add the total count
            checkRow.add(String.valueOf(getCheckTotal(countryCheckCounts, check)));
            outputLines.add(checkRow);
        });

        return outputLines;
    }

    /**
     * Given a map of flag counts per check per country, get the total count for a single check.
     *
     * @param countryCheckCounts
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *            {@link String}
     * @param check
     *            {@link String} check name
     * @return the count as a {@link long}
     */
    private long getCheckTotal(final Map<String, Map<String, Counter>> countryCheckCounts,
            final String check)
    {
        return countryCheckCounts.keySet().stream().mapToLong(country -> countryCheckCounts
                .get(country).getOrDefault(check, new Counter()).getValue()).sum();
    }

    /**
     * Given a map of flag counts per check per country, get the count value for a specific country
     * and check.
     *
     * @param countryCheckCounts
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country
     *            {@link String}
     * @param country
     *            {@link String} country code
     * @param check
     *            {@link String} check name
     * @return and {@link Optional} of the count as a {@link long}
     */
    private Optional<Long> getCountryCheckCount(
            final Map<String, Map<String, Counter>> countryCheckCounts, final String country,
            final String check)
    {
        if (countryCheckCounts.containsKey(country)
                && countryCheckCounts.get(country).containsKey(check))
        {
            return Optional.of(countryCheckCounts.get(country).get(check).getValue());
        }
        return Optional.empty();
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

        logger.info("Reading files from: {}", path);

        // Check all files in the folder and all sub-folders
        return new File(path).listFilesRecursively().parallelStream()
                // Filter the files to only include log files, either gzipped or uncompressed
                .filter(file -> FilenameUtils
                        .getExtension(file.isGzipped() ? FilenameUtils.getBaseName(file.getName())
                                : file.getName())
                        .equalsIgnoreCase("log"))
                .map(file ->
                {
                    final Map<String, Map<String, Counter>> countryCheckMap = new HashMap<>();
                    logger.info("Reading: {}", file.getName());
                    // Get the parent folder name and assume it is a county code
                    final String country = FilenameUtils.getName(file.getParentPathString());
                    // Add the country to the map
                    countryCheckMap.putIfAbsent(country, new HashMap<>());

                    // Read the log file
                    try (InputStreamReader inputStreamReader = file.isGzipped()
                            ? new InputStreamReader(
                                    new GZIPInputStream(new FileInputStream(file.getFile())))
                            : new FileReader(file.getPathString()))
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
                                String.format("Exception thrown while reading file %s: %s",
                                        file.getName(), exception.getMessage()));
                    }
                    return countryCheckMap;
                }).collect(HashMap::new, this::mergeMaps, this::mergeMaps);
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

    /**
     * Merges one 2d country checks {@link HashMap} into another.
     *
     * @param put
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country *
     *            {@link String}
     * @param place
     *            a 2D {@link Map} of flag {@link Counter}s per check {@link String} per country *
     *            {@link String}
     * @return a merged 2d country checks {@link HashMap}
     */
    private Map<String, Map<String, Counter>> mergeMaps(
            final Map<String, Map<String, Counter>> place,
            final Map<String, Map<String, Counter>> put)
    {
        put.forEach((country, checks) ->
        {
            place.putIfAbsent(country, new HashMap<>());
            checks.forEach((check, counter) ->
            {
                place.get(country).putIfAbsent(check, new Counter());
                place.get(country).get(check).add(counter.getValue());
            });
        });
        return place;
    }

    /**
     * Convert a 2D {@link List} of {@link String}s into a csv {@link String}, and write to a file.
     *
     * @param outputPath
     *            {@link String} path to write the csv to
     * @param table
     *            a 2D {@link List} of {@link String}s
     * @throws IOException
     *             when unable to write output
     */
    private void writeCSV(final String outputPath, final List<List<String>> table)
            throws IOException
    {
        final String outputString = table.stream().map(line -> String.join(COMMA, line))
                .collect(Collectors.joining(LINE_SEPARATOR));

        try (FileWriter outputWriter = new FileWriter(new File(outputPath).getFile()))
        {
            outputWriter.write(outputString);
        }
    }

}
