package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.tags.names.ReferenceTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 *
 * @author v-garei
 */
public class HighwayMissingNameOrRefTagCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 8198312161814763037L;
    private static final String MISSING_BOTH_NAME_AND_REF_TAG_INSTRUCTIONS = "Way {0, number, #} is missing both name and ref tag. Way must contain either one.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(MISSING_BOTH_NAME_AND_REF_TAG_INSTRUCTIONS);

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public HighwayMissingNameOrRefTagCheck(final Configuration configuration)
    {
        super(configuration);
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

        // by default we will assume all objects as valid
        return object instanceof Edge && ((Edge) object).isMainEdge()
                && !isFlagged(object.getOsmIdentifier())
                && ((Edge) object).highwayTag().isMoreImportantThanOrEqualTo(HighwayTag.TERTIARY)
                && !HighwayTag.isLinkHighway(((Edge) object).highwayTag())
                && !JunctionTag.isRoundabout(object)
                && !JunctionTag.isCircular(object);
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
        final Map<String,String> tags = object.getTags();
        if (this.highwayMissingBothNameAndRefTag(tags))
        {
            return Optional.of(createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Determines if Edge object is missing both name and ref tag
     * @param tags object tags
     * @return boolean if Edge does not contain either name or ref tag.
     */
    private boolean highwayMissingBothNameAndRefTag(final Map<String,String> tags)
    {
        return !tags.containsKey(NameTag.KEY.toLowerCase()) && !tags.containsKey(ReferenceTag.KEY.toLowerCase());
    }
}
