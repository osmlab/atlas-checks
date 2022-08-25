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
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.CheckDateTag;
import org.openstreetmap.atlas.tags.ConstructionDateTag;
import org.openstreetmap.atlas.tags.ConstructionTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LandUseTag;
import org.openstreetmap.atlas.tags.LastEditTimeTag;
import org.openstreetmap.atlas.tags.OpenDateTag;
import org.openstreetmap.atlas.tags.OpeningDateTag;
import org.openstreetmap.atlas.tags.TemporaryDateOnTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The purpose of this check is to identify construction tags where the construction hasn't been
 * checked on recently, or the expected finish date has been passed.
 *
 * @author v-brjor
 */
public class ConstructionCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -5857500094506755337L;
    private static final double OLD_CONSTRUCTION_DAYS_DEFAULT = (double) 365 * 2;
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
    private static final DateTimeFormatter ATLAS_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyyMMdd");
    private static final LocalDate TODAYS_DATE = LocalDate.now();
    private static final List<String> DATE_TAGS = Arrays.asList(OpeningDateTag.KEY, OpenDateTag.KEY,
            ConstructionDateTag.KEY, TemporaryDateOnTag.KEY, "date_on");
    private static final List<String> CONSTRUCTION_TAGS = List.of(HighwayTag.KEY, LandUseTag.KEY,
            BuildingTag.KEY);

    private final int oldConstructionDays;
    private final int oldCheckDateMonths;
    private static final Logger logger = LoggerFactory.getLogger(ConstructionCheck.class);

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
        return !this.isFlagged(object.getOsmIdentifier()) && this.isConstruction(keySet);
    }

    @Override
    protected CheckFlag createFlag(final AtlasObject object, final String instruction)
    {
        if (object instanceof Edge)
        {
            return super.createFlag(new OsmWayWalker((Edge) object).collectEdges(), instruction);
        }
        return super.createFlag(object, instruction);
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
        final LocalDate comparisonDate = this.getComparisonDate(object);

        this.markAsFlagged(object.getOsmIdentifier());

        final Map<String, String> tags = object.getTags();

        final Optional<String> dateTag = this.getDateTag(tags);
        if (dateTag.isPresent())
        {
            final String tagDate = tags.get(dateTag.get());

            final Optional<LocalDate> parsedDate = this.parseDate(tagDate);
            if (parsedDate.isPresent() && parsedDate.get().isBefore(comparisonDate))
            {
                return Optional.of(
                        this.createFlag(object, this.getLocalizedInstruction(0, dateTag.get())));
            }
        }

        if (tags.containsKey(CheckDateTag.KEY))
        {
            final Optional<LocalDate> parseDateChecked = this.parseDate(tags.get(CheckDateTag.KEY));
            if (parseDateChecked.isPresent())
            {
                final long monthsBetween = ChronoUnit.MONTHS.between(parseDateChecked.get(),
                        comparisonDate);
                if (monthsBetween > this.oldCheckDateMonths)
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(1, this.oldCheckDateMonths)));
                }
            }
        }

        if (tags.containsKey(LastEditTimeTag.KEY))
        {
            final long timestamp = Long.parseLong(tags.get(LastEditTimeTag.KEY));
            final LocalDate lastEditDate = Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            final long numberOfDays = ChronoUnit.DAYS.between(lastEditDate, comparisonDate);
            if (numberOfDays > this.oldConstructionDays)
            {
                return Optional.of(this.createFlag(object,
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
     * Get the date the atlas was generated, or use today's date as a fallback
     *
     * @param object
     *            AtlasObject to examine
     * @return LocalDate.
     */
    private LocalDate getComparisonDate(final AtlasObject object)
    {
        final Optional<String> atlasDateString = object.getAtlas().metaData().getDataVersion();
        if (atlasDateString.isPresent() && !atlasDateString.get().equals("unknown"))
        {
            try
            {
                return LocalDate.parse(atlasDateString.get().split("-")[0], ATLAS_DATE_FORMATTER);
            }
            catch (final DateTimeParseException exception)
            {
                logger.error(
                        "Could not parse date {} from Atlas meta data. {}. Assign today's date {} instead.",
                        atlasDateString.get(), exception.getMessage(), TODAYS_DATE);
            }
        }
        return TODAYS_DATE;
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
                .anyMatch(tag -> tag.equals(ConstructionTag.KEY)
                        || tag.startsWith("construction:") && !tag.equals(ConstructionDateTag.KEY))
                || CONSTRUCTION_TAGS.stream()
                        .anyMatch(tag -> ConstructionTag.KEY.equals(tags.get(tag)));
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
