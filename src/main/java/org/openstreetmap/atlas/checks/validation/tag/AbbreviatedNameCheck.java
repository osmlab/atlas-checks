package org.openstreetmap.atlas.checks.validation.tag;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

/**
 * Flags names that have abbreviations in them. Per OSM wiki, if the name can be spelled without an
 * abbreviation, then it must not be abbreviated. Computers can easily shorten words, but not the
 * other way (St. could be Street or Saint).
 *
 * @see <a href="http://wiki.openstreetmap.org/wiki/Names#Abbreviation_.28don.27t_do_it.29">wiki</a>
 *      for more information.
 * @author mkalender
 */
public class AbbreviatedNameCheck extends BaseCheck<String>
{
    private static final long serialVersionUID = -3648610800112828238L;

    // Abbreviation config
    private static final String ABBREVIATION_KEY = "abbreviations";

    // Config to create Locale object
    private static final String LOCALE_KEY = "locale";

    // Splitter to parse name
    private static final Splitter NAME_SPLITTER = Splitter
            .on(CharMatcher.JAVA_LETTER_OR_DIGIT.negate()).omitEmptyStrings();

    private final Set<String> abbreviations;
    private final Locale locale;

    /**
     * Generates a unique identifier given an {@link AtlasEntity}. OSM/Atlas objects with different
     * types can share the identifier (way 12345 - node 12345). This method makes sure we generate a
     * truly unique identifier among different types for an {@link AtlasEntity}.
     *
     * @param entity
     *            {@link AtlasEntity} to generate unique identifier for
     * @return unique object identifier among different types
     */
    private static String getUniqueObjectIdentifier(final AtlasEntity entity)
    {
        return String.format("%s%s", entity.getType().toShortString(), entity.getOsmIdentifier());
    }

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public AbbreviatedNameCheck(final Configuration configuration)
    {
        super(configuration);
        this.locale = this.configurationValue(configuration, LOCALE_KEY,
                Locale.getDefault().getLanguage(), locale -> new Locale(locale));
        this.abbreviations = this
                .configurationValue(configuration, ABBREVIATION_KEY, new ArrayList<String>(),
                        Sets::newHashSet)
                .stream().map(abbreviation -> abbreviation.toLowerCase(this.locale))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof AtlasEntity
                && !this.isFlagged(getUniqueObjectIdentifier((AtlasEntity) object));
    }

    /**
     * Flags {@link AtlasObject}s that has names with abbreviations.
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Mark OSM identifier as we are processing it
        this.markAsFlagged(getUniqueObjectIdentifier((AtlasEntity) object));

        // Fetch the name
        final Optional<String> optionalName = NameTag.getNameOf(object);
        if (!optionalName.isPresent())
        {
            return Optional.empty();
        }

        // Lowercase name and parse it into tokens
        final String name = optionalName.get();
        final String lowercaseName = name.toLowerCase(this.locale);
        final Set<String> tokens = Sets.newHashSet(NAME_SPLITTER.split(lowercaseName));

        // Flag if it has any abbreviations
        if (tokens.stream().anyMatch(this.abbreviations::contains))
        {
            final CheckFlag flag = this.createFlag(object,
                    String.format(
                            "OSM feature with id %s's name tag (`name` = **%s**) has an abbreviation. Please update the `name` tag to not use abbreviation.",
                            object.getOsmIdentifier(), name));
            return Optional.of(flag);
        }

        return Optional.empty();
    }
}
