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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FilenameUtils;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.scalars.Counter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author bbreithaupt
 */
public class FlagStatisticsSubCommand extends AbstractAtlasShellToolsCommand
{
    private static final String INPUT_OPTION = "input";
    private static final String UNCOMPRESSED_FLAG = "uncompressed";
    private static final String DIFFERENCE_OPTION = "difference";
    private static final String OUTPUT_OPTION = "output";
    private static final String GENERATOR = "generator";

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
    public int execute()
    {
        final HashMap<String, HashMap<String, Counter>> inputCounts = this.getCountryCheckCounts(
                this.optionAndArgumentDelegate.getOptionArgument(INPUT_OPTION).get());

        final Optional<String> differenceFilePath = this.optionAndArgumentDelegate
                .getOptionArgument(DIFFERENCE_OPTION);
        try
        {
            if (differenceFilePath.isPresent())
            {
                final HashMap<String, HashMap<String, Counter>> targetCounts = this
                        .getCountryCheckCounts(differenceFilePath.get());
                this.writeOutput(getDifference(inputCounts, targetCounts),
                        this.optionAndArgumentDelegate.getOptionArgument(OUTPUT_OPTION));
            }
            else
            {
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

    }

    @Override
    public void registerOptionsAndArguments()
    {
        this.registerOptionWithRequiredArgument(INPUT_OPTION,
                "A directory of folders containing atlas-checks log files.",
                OptionOptionality.REQUIRED, INPUT_OPTION);
        this.registerOption(UNCOMPRESSED_FLAG, 'u',
                "Look for uncompressed log files, instead of gzipped ones.",
                OptionOptionality.OPTIONAL);
        this.registerOptionWithRequiredArgument(DIFFERENCE_OPTION, 'd',
                "A second set of log files to diff against.", OptionOptionality.OPTIONAL,
                DIFFERENCE_OPTION);
        this.registerOptionWithRequiredArgument(OUTPUT_OPTION, 'o', "A csv to output results to.",
                OptionOptionality.OPTIONAL, OUTPUT_OPTION);
        super.registerOptionsAndArguments();
    }

    /**
     * @param path
     *            folder path
     * @return a map
     */
    private HashMap<String, HashMap<String, Counter>> getCountryCheckCounts(final String path)
    {
        final HashMap<String, HashMap<String, Counter>> countryCheckMap = new HashMap<>();
        new File(path).listFilesRecursively().stream()
                .filter(file -> !file.isDirectory()
                        && this.optionAndArgumentDelegate.hasOption(UNCOMPRESSED_FLAG)
                                ? FilenameUtils.getExtension(file.getName()).equals("log")
                                : file.isGzipped() && FilenameUtils
                                        .getExtension(FilenameUtils.getBaseName(file.getName()))
                                        .equals("log"))
                .forEach(file ->
                {
                    final String country = FilenameUtils.getName(file.getParent());
                    countryCheckMap.putIfAbsent(country, new HashMap<>());

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
                                countryCheckMap.get(country).putIfAbsent(checkName, new Counter());
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
     * @param countryCheckCounts
     *            map
     * @param outputPath
     *            file
     * @throws IOException
     */
    private void writeOutput(final HashMap<String, HashMap<String, Counter>> countryCheckCounts,
            final Optional<String> outputPath) throws IOException
    {
        final List<String> countries = new ArrayList<>(countryCheckCounts.keySet());
        java.util.Collections.sort(countries);
        final List<String> checks = countryCheckCounts.entrySet().stream()
                .flatMap(entry -> entry.getValue().keySet().stream()).distinct().sorted()
                .collect(Collectors.toList());
        final StringBuilder outputString = new StringBuilder();

        outputString.append("Check,");
        outputString.append(String.join(COMMA, countries));
        outputString.append(",Total");
        outputString.append(LINE_SEPARATOR);

        checks.forEach(check ->
        {
            outputString.append(check).append(COMMA);
            outputString.append(countries.stream().map(country ->
            {
                if (countryCheckCounts.get(country).containsKey(check))
                {
                    return String.valueOf(countryCheckCounts.get(country).get(check).getValue());
                }
                return "ND";
            }).collect(Collectors.joining(COMMA)));
            outputString.append(COMMA);
            outputString.append((Long) countries.stream()
                    .filter(country -> countryCheckCounts.get(country).containsKey(check))
                    .mapToLong(country -> countryCheckCounts.get(country).get(check).getValue())
                    .sum());
            outputString.append(LINE_SEPARATOR);
        });

        outputString.append("Total,");
        final List<Long> countryCounts = countries.stream()
                .map(country -> countryCheckCounts.get(country).entrySet().stream()
                        .mapToLong(entry -> entry.getValue().getValue()).sum())
                .collect(Collectors.toList());
        outputString.append(
                countryCounts.stream().map(String::valueOf).collect(Collectors.joining(COMMA)));
        outputString.append(COMMA);
        outputString.append(countryCounts.stream().mapToLong(Long::longValue).sum());

        if (outputPath.isPresent())
        {
            try (FileWriter outputWriter = new FileWriter(new File(outputPath.get()).getFile()))
            {
                outputWriter.write(outputString.toString());
            }
        }

        this.outputDelegate.printStdout(LINE_SEPARATOR + outputString.toString() + LINE_SEPARATOR);
    }

    /**
     * @param source
     *            map
     * @param target
     *            map
     * @return map
     */
    private HashMap<String, HashMap<String, Counter>> getDifference(
            final HashMap<String, HashMap<String, Counter>> source,
            final HashMap<String, HashMap<String, Counter>> target)
    {
        final HashMap<String, HashMap<String, Counter>> difference = new HashMap<>();
        source.forEach((country, checkCounts) ->
        {
            difference.putIfAbsent(country, new HashMap<>());
            checkCounts
                    .forEach((check, count) -> difference.get(country).put(check,
                            new Counter(target.getOrDefault(country, new HashMap<>())
                                    .getOrDefault(check, new Counter()).getValue()
                                    - count.getValue())));
        });
        target.forEach((country, checkCounts) ->
        {
            difference.putIfAbsent(country, new HashMap<>());
            checkCounts
                    .forEach((check, count) -> difference.get(country).putIfAbsent(check, count));
        });
        return difference;
    }
}
