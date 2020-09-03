package org.openstreetmap.atlas.checks.validation.tag;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 *
 * @author v-brjor
 */
public class ConstructionCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -5857500094506755337L;
    private static final double OLD_CONSTRUCTION_DAYS_DEFAULT = 365 * 2;
    private static final double OLD_CHECK_DATE_MONTHS_DEFAULT = 6;
    private static final String CONSTRUCTION_PASSED_DATE = "The {0} tag has been exceeded. If the construction is still ongoing please update the date with a new completion date from an official source. Otherwise please modify this to be a completed feature";
    private static final String CONSTRUCTION_CHECK_DATE_OLD = "It has been more than {0} months since this construction was last checked. If this is still under construction please update the check_date tag. Otherwise please modify this to be a completed feature.";
    private static final String CONSTRUCTION_LAST_EDITED_OLD = "This feature has had a construction tag, with no updates, for more than {0} days. If this is still under construction please update the check_date tag. Otherwise please modify this to be a completed feature.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            CONSTRUCTION_PASSED_DATE,
            CONSTRUCTION_CHECK_DATE_OLD,
            CONSTRUCTION_LAST_EDITED_OLD
    );
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-M");
    private static final DateTimeFormatter YEAR_MONTH_DAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-M-d");

    private static final LocalDate TODAYS_DATE = LocalDate.now();
    private static final List<String> DATE_TAGS = Arrays.asList(
            "opening_date", "open_date", "construction:date", "temporary:date_on", "date_on"
    );
    private static final List<String> CONSTRUCTION_TAGS = List.of("highway", "landuse", "building");
    private final int oldConstructionDays;
    private final int oldCheckDateMonths;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *         the JSON configuration for this check
     */
    public ConstructionCheck(final Configuration configuration)
    {
        super(configuration);
        this.oldConstructionDays = this.configurationValue(configuration, "oldConstructionDays",
                OLD_CONSTRUCTION_DAYS_DEFAULT, Double::intValue);
        this.oldCheckDateMonths = this.configurationValue(configuration, "oldCheckDateMonth",
                OLD_CHECK_DATE_MONTHS_DEFAULT, Double::intValue);
    }

    private boolean isConstruction(Map<String, String> keySet)
    {
        return keySet.values().stream().anyMatch(value ->
                value.equals("construction")
                        || value.startsWith("construction:")
                        && !value.equals("construction:date")
        )
                || CONSTRUCTION_TAGS.stream()
                .map(tag -> "construction".equals(keySet.get(tag)))
                .reduce(Boolean::logicalOr)
                .orElse(false);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *         the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        Map<String, String> keySet = object.getOsmTags();
        return !this.isFlagged(object.getOsmIdentifier())
                && isConstruction(keySet);
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *         the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        this.markAsFlagged(object.getOsmIdentifier());

        Map<String, String> keySet = object.getTags();

        Optional<String> dateTag = getDateTag(keySet);
        if (dateTag.isPresent())
        {
            String tagDate = keySet.get(dateTag.get());

            Optional<LocalDate> parsedDate = parseDate(tagDate);
            if (parsedDate.isPresent() && parsedDate.get().isBefore(TODAYS_DATE))
            {
                return Optional.of(
                        createFlag(object, this.getLocalizedInstruction(0, dateTag.get())));
            }
        }

        if (keySet.containsKey("check_date"))
        {
            Optional<LocalDate> parseDateChecked = parseDate(keySet.get("check_date"));
            if (parseDateChecked.isPresent())
            {
                long numberOfMonths = ChronoUnit.MONTHS.between(parseDateChecked.get(), TODAYS_DATE);
                if (numberOfMonths > oldCheckDateMonths)
                {
                    return Optional.of(
                            createFlag(object, this.getLocalizedInstruction(1, oldCheckDateMonths))
                    );
                }
            }
        }


        if (keySet.containsKey("last_edit_time"))
        {
            long timestamp = Long.parseLong(keySet.get("last_edit_time"));
            LocalDate lastEditDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate();

            long numberOfDays = ChronoUnit.DAYS.between(lastEditDate, TODAYS_DATE);
            if (numberOfDays > oldConstructionDays)
            {
                return Optional.of(
                        createFlag(object, this.getLocalizedInstruction(2, oldConstructionDays))
                );
            }
        }

        return Optional.empty();
    }

    private Optional<String> getDateTag(Map<String, String> keySet)
    {
        return DATE_TAGS.stream()
                .filter(keySet::containsKey)
                .findFirst();
    }

    private Optional<LocalDate> parseDate(String tagDate)
    {
        LocalDate date = null;
        try
        {
            date = LocalDate.parse(tagDate, YEAR_MONTH_DAY_FORMAT);
        } catch (DateTimeParseException dtpe1)
        {
            try
            {
                date = YearMonth.parse(tagDate, YEAR_MONTH_FORMAT).atDay(28);
            } catch (DateTimeParseException dtpe2)
            {
                try
                {
                    date = Year.parse(tagDate, YEAR_FORMAT).atMonth(12).atDay(31);
                } catch (DateTimeParseException dtpe3)
                {
                    System.out.println("Could not parse date: " + tagDate);
                }
            }
        }
        return Optional.ofNullable(date);
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
