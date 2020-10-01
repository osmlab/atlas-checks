package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.locale.IsoLanguage;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.names.NameFinder;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * The short name check will validate that any and all names contain at least 2 letters in the name
 *
 * @author cuthbertm
 * @author savannahostrowski
 */
public class ShortNameCheck extends BaseCheck<Long>
{
    // This set of non-latin script countries is used to determine if incoming entities should
    // Be compared to the configurable threshold value or the non-latin script threshold
    private static final List<String> NON_LATIN_SCRIPT_COUNTRIES_DEFAULT = Arrays.asList("AFG",
            "ARE", "ARM", "BGD", "BGR", "BHR", "BIH", "BLR", "BRN", "BTN", "CHN", "COM", "CYP",
            "DJI", "DZA", "EGY", "ERI", "ESH", "ETH", "GEO", "GRC", "HKG", "IND", "IRN", "IRQ",
            "ISR", "JOR", "JPN", "KAZ", "KGZ", "KHM", "KOR", "KWT", "LAO", "LBN", "LBY", "LKA",
            "MAC", "MAR", "MDA", "MDV", "MKD", "MMR", "MNE", "MNG", "MRT", "MYS", "NPL", "OMN",
            "PAK", "PRK", "PSE", "QAT", "RUS", "SAU", "SDN", "SGP", "SOM", "SRB", "SYR", "TCD",
            "THA", "TJK", "TUN", "TWN", "TZA", "UKR", "YEM");
    private static final long THRESHOLD_DEFAULT = 2;
    private static final long NON_LATIN_SCRIPT_THRESHOLD_DEFAULT = 1;
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList("The following names for OSM Id: {0,number,#}, are too short {1}");
    private static final long serialVersionUID = -3935274340982988966L;
    private final Set<String> keyNames = new HashSet<>();
    private final long threshold;
    private final long nonLatinThreshold;
    private final Set<String> nonLatinScriptCountries;

    public ShortNameCheck(final Configuration configuration)
    {
        super(configuration);
        final Set<Optional<IsoLanguage>> languages = IsoLanguage.allLanguageCodes().stream()
                .map(languageCode -> Optional.of(IsoLanguage.forLanguageCode(languageCode).get()))
                .collect(Collectors.toSet());
        // The empty optional is added here so that the keyNames that are populated in the set
        // include the non localized versions, ie. simply "name". If not added will only include
        // localized keys like "name:en" and "name:fr"
        languages.add(Optional.empty());
        for (final Optional<IsoLanguage> language : languages)
        {
            for (final Class<?> klass : NameFinder.STANDARD_TAGS_NON_REFERENCE)
            {
                Validators.localizeKeyName(klass, language).ifPresent(this.keyNames::add);
            }
        }
        this.threshold = configurationValue(configuration, "length.maximum", THRESHOLD_DEFAULT);
        this.nonLatinThreshold = configurationValue(configuration, "non.latin.length.maximum",
                NON_LATIN_SCRIPT_THRESHOLD_DEFAULT);
        this.nonLatinScriptCountries = new HashSet<>(configurationValue(configuration,
                "non.latin.countries", NON_LATIN_SCRIPT_COUNTRIES_DEFAULT));
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        final String objectIsoCountry = object.tag(ISOCountryTag.KEY);
        final String nameTag = object.tag(NameTag.KEY);
        return nameTag != null && objectIsoCountry != null && nameTag
                .length() < (this.nonLatinScriptCountries.contains(objectIsoCountry.toUpperCase())
                        ? this.nonLatinThreshold
                        : this.threshold);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Map<String, String> tags = object.getTags();
        // This stream function, will filter out any tags that is not a name tag and that contains
        // at least X characters in the string, where X is the threshold defined in the constructor.
        // It will convert it into a string to be used in the instruction for the task. Eg. if there
        // are two names "name=e" and "name:en=en" this will produce the string "name=e; name:en=en"
        final String shortNameTags = tags.entrySet().stream()
                .filter(entry -> this.keyNames.contains(entry.getKey())
                        && (StringUtils.isEmpty(entry.getValue())
                                || entry.getValue().length() < this.threshold))
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("; "));
        if (shortNameTags.length() > 0)
        {
            return Optional.of(createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier(), shortNameTags)));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
