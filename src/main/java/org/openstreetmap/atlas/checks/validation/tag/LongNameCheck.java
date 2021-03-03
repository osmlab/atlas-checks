package org.openstreetmap.atlas.checks.validation.tag;

import static org.openstreetmap.atlas.checks.constants.CommonConstants.COLON;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.names.NameFinder;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags features with name greater than a configurable number of characters.
 *
 * @author bbreithaupt
 */
public class LongNameCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -8395117392888327533L;
    private static final long NAME_MAX_DEFAULT = 40;
    private static final Set<String> NAME_TAG_KEYS = NameFinder.STANDARD_TAGS_NON_REFERENCE.stream()
            .map(Validators::findTagNameIn).collect(Collectors.toSet());
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "Feature {0,number,#} has the following tags with over {1,number,#} characters: {2}.");

    private final long nameMax;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public LongNameCheck(final Configuration configuration)
    {
        super(configuration);
        this.nameMax = this.configurationValue(configuration, "name.max", NAME_MAX_DEFAULT);
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
        return !this.isFlagged(object.getOsmIdentifier())
                && object.getTags().keySet().stream().anyMatch(this::isNameTag);
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
        final Set<String> invalidNameTags = object.getTags().entrySet().stream()
                .filter(entry -> this.isNameTag(entry.getKey()))
                .filter(entry -> entry.getValue().length() >= this.nameMax).map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        if (!invalidNameTags.isEmpty())
        {
            this.markAsFlagged(object.getOsmIdentifier());
            final String instruction = this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                    this.nameMax, String.join(", ", invalidNameTags));
            return Optional.of(object instanceof Edge
                    ? this.createFlag(new OsmWayWalker((Edge) object).collectEdges(), instruction)
                    : this.createFlag(object, instruction));
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks if the given {@link String} is in {@link LongNameCheck#NAME_TAG_KEYS}, ignoring
     * localizations.
     *
     * @param key
     *            {@link String}
     * @return boolean
     */
    private boolean isNameTag(final String key)
    {
        return NAME_TAG_KEYS.contains(key.split(COLON)[0]);
    }
}
