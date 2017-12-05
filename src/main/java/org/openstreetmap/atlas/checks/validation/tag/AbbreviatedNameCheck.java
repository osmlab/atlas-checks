package org.openstreetmap.atlas.checks.validation.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    // Abbreviation config
    private static final String ABBREVIATION_KEY = "abbreviations";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "OSM feature with id {0}'s name tag (`name` = **{1}**) has an abbreviation. Please update the `name` tag to not use abbreviation.");
    // Splitter to parse name
    private static final Splitter NAME_SPLITTER = Splitter
            .on(CharMatcher.JAVA_LETTER_OR_DIGIT.negate()).omitEmptyStrings();
    private static final long serialVersionUID = -3648610800112828238L;
    private final Set<String> abbreviations;

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public AbbreviatedNameCheck(final Configuration configuration)
    {
        super(configuration);
        this.abbreviations = this
                .configurationValue(configuration, ABBREVIATION_KEY, new ArrayList<String>(),
                        Sets::newHashSet)
                .stream().map(abbreviation -> abbreviation.toLowerCase(this.getLocale()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof AtlasEntity
                && !this.isFlagged(this.getUniqueOSMIdentifier((AtlasEntity) object));
    }

    /**
     * Flags {@link AtlasObject}s that has names with abbreviations.
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Mark OSM identifier as we are processing it
        this.markAsFlagged(this.getUniqueOSMIdentifier((AtlasEntity) object));

        // Fetch the name
        final Optional<String> optionalName = NameTag.getNameOf(object);
        if (!optionalName.isPresent())
        {
            return Optional.empty();
        }

        // Lowercase name and parse it into tokens
        final String name = optionalName.get();
        final String lowercaseName = name.toLowerCase(this.getLocale());
        final Set<String> tokens = Sets.newHashSet(NAME_SPLITTER.split(lowercaseName));

        // Flag if it has any abbreviations
        if (tokens.stream().anyMatch(this.abbreviations::contains))
        {
            final CheckFlag flag = this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier(), name));
            return Optional.of(flag);
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
