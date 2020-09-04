package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.names.NameLeftTag;
import org.openstreetmap.atlas.tags.names.NameRightTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import com.google.common.base.CharMatcher;

/**
 * This check flags {@link Edge}s that are car navigable highways and have a name tag that contains
 * only integers. Name tags with a single character are optionally ignored, as they will be flagged
 * by {@link ShortNameCheck}.
 *
 * @author bbreithaupt
 */
public class StreetNameIntegersOnlyCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 3439708862406928654L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Street {0,number,#} has a name containing only integers.");

    private static final List<String> NAME_KEYS_DEFAULT = Arrays.asList(NameTag.KEY,
            NameLeftTag.KEY, NameRightTag.KEY);

    private final List<String> nameKeys;
    private final boolean ignoreSingleCharacter;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public StreetNameIntegersOnlyCheck(final Configuration configuration)
    {
        super(configuration);
        this.nameKeys = configurationValue(configuration, "name.keys.filter", NAME_KEYS_DEFAULT);
        this.ignoreSingleCharacter = configurationValue(configuration, "character.single.ignore",
                false);
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
        final Map<String, String> osmTags = object.getOsmTags();
        return object instanceof Edge && ((Edge) object).isMainEdge()
                && HighwayTag.isCarNavigableHighway(object)
                && this.nameKeys.stream().anyMatch(osmTags::containsKey);
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
        // Try to convert name to an integer. If it converts without failure it should be flagged.
        for (final String nameKey : this.nameKeys)
        {
            final Optional<String> nameValue = object.getTag(nameKey);
            // Only test present name tags, and optionally ignore single character name values
            if (nameValue.isPresent()
                    && (!this.ignoreSingleCharacter || nameValue.get().length() > 1))
            {
                try
                {
                    Integer.parseInt(
                            CharMatcher.breakingWhitespace().replaceFrom(nameValue.get(), ""));
                }
                catch (final NumberFormatException e)
                {
                    continue;
                }
                return Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(0, object.getOsmIdentifier())));
            }
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
