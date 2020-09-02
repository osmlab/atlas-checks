package org.openstreetmap.atlas.checks.validation.tag;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
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
    private static final String CONSTRUCTION_PASSED_DATE = "The {0} tag date has been exceeded. If the construction is still ongoing please update the date with a new completion date from an official source. Otherwise please modify this to be a completed feature";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            CONSTRUCTION_PASSED_DATE);
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-M");
    private static final DateTimeFormatter YEAR_MONTH_DAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-M-d");

    private final LocalDate TODAYS_DATE = LocalDate.now();
    private final List<String> dateTags = Arrays.asList(
        "opening_date", "open_date", "construction:date", "temporary:date_on", "date_on"
    );

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
    }


    private boolean isConstruction(Map<String, String> keySet)
    {
        return keySet.values().stream().anyMatch(value ->
                value.equals("construction")
                        || value.startsWith("construction:")
                        && !value.equals("construction:date")
                )
                || Stream.of("highway", "landuse", "building")
                .map(tag -> "construction".equals(keySet.get(tag)))
                .reduce(Boolean::logicalOr)
                .get();
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
        Map<String, String> keySet = object.getOsmTags();
        return !this.isFlagged(object.getOsmIdentifier())
                && isConstruction(keySet);
    }

    private Optional<String> getDateTag(Map<String, String> keySet)
    {
        return dateTags.stream().filter(keySet::containsKey).findFirst();
//        for (String dTag : dateTags)
//        {
//            if (keySet.containsKey(dTag))
//            {
//                return Optional.of(dTag);
//            }
//        }
//        return Optional.empty();
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
        Map<String, String> keySet = object.getOsmTags();
        Optional<String> dateTag = getDateTag(keySet);
        if (dateTag.isEmpty()) {
            return Optional.empty();
        }
        String tagDate = keySet.get(dateTag.get());
        Optional<LocalDate> parsedDate = parseDate(tagDate);

        if (parsedDate.isPresent()) {
            // is the date before today?
            if (parsedDate.get().compareTo(TODAYS_DATE) < 0) {
                return Optional.of(
                        createFlag(object, this.getLocalizedInstruction(0, dateTag.get())));
            }
        }

        return Optional.empty();
    }

    private Optional<LocalDate> parseDate(String tagDate)
    {
        LocalDate date = null;
        try {
            date = LocalDate.parse(tagDate, YEAR_MONTH_DAY_FORMAT);
        } catch (DateTimeParseException dtpe1) {
            try {
                date = YearMonth.parse(tagDate, YEAR_MONTH_FORMAT).atDay(1);
            } catch (DateTimeParseException dtpe2) {
                try {
                    date = Year.parse(tagDate, YEAR_FORMAT).atMonth(1).atDay(1);
                } catch (DateTimeParseException dtpe3) {
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
