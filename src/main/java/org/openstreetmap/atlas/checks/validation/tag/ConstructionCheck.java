package org.openstreetmap.atlas.checks.validation.tag;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * The purpose of this check is to identify construction tags where the construction hasn't been
 * checked on recently, or the expected finish date has been passed.
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
            CONSTRUCTION_PASSED_DATE, CONSTRUCTION_CHECK_DATE_OLD, CONSTRUCTION_LAST_EDITED_OLD);
    private static final List<DateTimeFormatter> YEAR_FORMATTERS = Collections.singletonList(
            // 2020
            DateTimeFormatter.ofPattern("yyyy"));
    private static final List<DateTimeFormatter> YEAR_MONTH_FORMATTERS = Arrays.asList(
            // 2020-1
            DateTimeFormatter.ofPattern("yyyy-M"),
            // 1-2020
            DateTimeFormatter.ofPattern("M-yyyy"),
            // Jan-2020
            DateTimeFormatter.ofPattern("MMM-yyyy"),
            // January 2020
            DateTimeFormatter.ofPattern("MMMM yyyy"));
    private static final List<DateTimeFormatter> FULL_DATE_FORMATTERS = Arrays.asList(
            // 2020-1-1
            DateTimeFormatter.ofPattern("yyyy-M-d"),
            // 1-1-2020
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            // 1-Jan-2020
            DateTimeFormatter.ofPattern("d-MMM-yyyy"),
            // 1 January 2020
            DateTimeFormatter.ofPattern("d MMMM yyyy"));
    private static final LocalDate TODAYS_DATE = LocalDate.now();
    private static final List<String> DATE_TAGS = Arrays.asList("opening_date", "open_date",
            "construction:date", "temporary:date_on", "date_on");
    private static final List<String> CONSTRUCTION_TAGS = List.of("highway", "landuse", "building");

    private final int oldConstructionDays;
    private final int oldCheckDateMonths;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public ConstructionCheck(final Configuration configuration)
    {
        super(configuration);
        this.oldConstructionDays = this.configurationValue(configuration, "oldConstructionDays",
                OLD_CONSTRUCTION_DAYS_DEFAULT, Double::intValue);
        this.oldCheckDateMonths = this.configurationValue(configuration, "oldCheckDateMonth",
                OLD_CHECK_DATE_MONTHS_DEFAULT, Double::intValue);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        final Map<String, String> keySet = object.getOsmTags();
        return !this.isFlagged(object.getOsmIdentifier()) && isConstruction(keySet);
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        this.markAsFlagged(object.getOsmIdentifier());

        final Map<String, String> tags = object.getTags();

        final Optional<String> dateTag = getDateTag(tags);
        if (dateTag.isPresent())
        {
            final String tagDate = tags.get(dateTag.get());

            final Optional<LocalDate> parsedDate = parseDate(tagDate);
            if (parsedDate.isPresent() && parsedDate.get().isBefore(TODAYS_DATE))
            {
                return Optional
                        .of(createFlag(object, this.getLocalizedInstruction(0, dateTag.get())));
            }
        }

        if (tags.containsKey("check_date"))
        {
            final Optional<LocalDate> parseDateChecked = parseDate(tags.get("check_date"));
            if (parseDateChecked.isPresent())
            {
                final long monthsBetween = ChronoUnit.MONTHS.between(parseDateChecked.get(),
                        TODAYS_DATE);
                if (monthsBetween > this.oldCheckDateMonths)
                {
                    return Optional.of(createFlag(object,
                            this.getLocalizedInstruction(1, this.oldCheckDateMonths)));
                }
            }
        }

        if (tags.containsKey("last_edit_time"))
        {
            final long timestamp = Long.parseLong(tags.get("last_edit_time"));
            final LocalDate lastEditDate = Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            final long numberOfDays = ChronoUnit.DAYS.between(lastEditDate, TODAYS_DATE);
            if (numberOfDays > this.oldConstructionDays)
            {
                return Optional.of(createFlag(object,
                        this.getLocalizedInstruction(2, this.oldConstructionDays)));
            }
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Gets the tag that holds a date.
     *
     * @param keySet
     *            Tags from the object.
     * @return a tag that is considered a date for construction.
     */
    private Optional<String> getDateTag(final Map<String, String> keySet)
    {
        return DATE_TAGS.stream().filter(keySet::containsKey).findFirst();
    }

    /**
     * Checks if the tags of an object signify it as being under construction.
     *
     * @param tags
     *            Tags from the object
     * @return true if the object is under construction, otherwise false
     */
    private boolean isConstruction(final Map<String, String> tags)
    {
        return tags.keySet().stream()
                .anyMatch(tag -> tag.equals("construction")
                        || tag.startsWith("construction:") && !tag.equals("construction:date"))
                || CONSTRUCTION_TAGS.stream().anyMatch(tag -> "construction".equals(tags.get(tag)));
    }

    /**
     * Attempts to parse the date string to ISO 8601 yyyy-mm-dd.
     *
     * @param tagDate
     *            String representation of a date from a tag.
     * @return the parsed date.
     */
    private Optional<LocalDate> parseDate(final String tagDate)
    {
        return Stream.of(YEAR_FORMATTERS.stream().map(format ->
        {
            try
            {
                return Year.parse(tagDate, format).atMonth(Month.DECEMBER).atEndOfMonth();
            }
            catch (final DateTimeParseException ignored)
            {
                return null;
            }
        }), YEAR_MONTH_FORMATTERS.stream().map(format ->
        {
            try
            {
                return YearMonth.parse(tagDate, format).atEndOfMonth();
            }
            catch (final Exception ignored)
            {
                return null;
            }
        }), FULL_DATE_FORMATTERS.stream().map(format ->
        {
            try
            {
                return LocalDate.parse(tagDate, format);
            }
            catch (final Exception ignored)
            {
                return null;
            }
        })).flatMap(s -> s).filter(Objects::nonNull).findAny();
    }
}
