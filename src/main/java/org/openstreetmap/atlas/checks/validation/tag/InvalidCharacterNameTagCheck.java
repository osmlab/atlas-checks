package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.locale.IsoLanguage;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * The purpose of this check is to identify water features (certain Lines, Areas and Relations that
 * pass a configurable filter) with numbers, special characters, double and smart quotes in their
 * name and localized name tags.
 *
 * @author sayas01
 */
public class InvalidCharacterNameTagCheck extends BaseCheck<String>
{
    private static final long serialVersionUID = -1478870354774039269L;
    private static final String VALID_OBJECT_FILTER_DEFAULT = "";
    private static final Pattern INVALID_CHARS_REGEX_DEFAULT = Pattern
            .compile(".*[0-9#$%^&*@~\"“”].*");
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "OSM feature with id {0,number,#} has one or more of the following invalid characters in its \"{1}\" tags: numbers, special characters(#$%^&*@~), double quotes or smart quotes(“”).");
    private final Set<String> localizedNameTags = new HashSet<>();
    private final TaggableFilter validObjectFilter;

    /**
     * Default constructor
     *
     * @param configuration
     *            {@link Configuration} required to construct any Check
     */
    public InvalidCharacterNameTagCheck(final Configuration configuration)
    {
        super(configuration);
        this.validObjectFilter = configurationValue(configuration, "valid.object.filter",
                VALID_OBJECT_FILTER_DEFAULT, TaggableFilter::forDefinition);
        final Set<Optional<IsoLanguage>> languages = IsoLanguage.allLanguageCodes().stream()
                .map(languageCode -> Optional.of(IsoLanguage.forLanguageCode(languageCode).get()))
                .collect(Collectors.toSet());
        // The empty optional is added here to include the non localized versions, ie. "name",
        // along with the localized versions.
        languages.add(Optional.empty());
        for (final Optional<IsoLanguage> language : languages)
        {
            Validators.localizeKeyName(NameTag.class, language)
                    .ifPresent(this.localizedNameTags::add);

        }
    }

    /**
     * Checks to see whether the supplied object class type is valid for this particular check
     *
     * @param object
     *            The {@link AtlasObject} you are checking
     * @return true if it is
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return (object instanceof Area || object instanceof Line || object instanceof Relation)
                && this.validObjectFilter.test(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Set<String> invalidCharacterNameTags = new HashSet<>();
        final Optional<String> nameTag = NameTag.getNameOf(object);
        // If NameTag is present and its value matches the invalid character pattern, add the Name
        // tag to the invalidCharacterNameTags set.
        if (nameTag.isPresent() && INVALID_CHARS_REGEX_DEFAULT.matcher(nameTag.get()).matches())
        {
            invalidCharacterNameTags.add(NameTag.KEY);

        }
        // If the atlas object has localized name tags that matches the invalid character pattern,
        // add it to the invalidCharacterNameTags set.
        Iterables.stream(object.getOsmTags().keySet()).filter(this.localizedNameTags::contains)
                .filter(localizedNameTag -> INVALID_CHARS_REGEX_DEFAULT
                        .matcher(object.getTag(localizedNameTag).get()).matches())
                .forEach(invalidCharacterNameTags::add);

        return invalidCharacterNameTags.isEmpty() ? Optional.empty()
                : Optional.of(this.createFlag(object, this.getLocalizedInstruction(0,
                        object.getOsmIdentifier(), String.join(", ", invalidCharacterNameTags))));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
