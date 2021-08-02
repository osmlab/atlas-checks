package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * HighwayAccessCheck looks for ways that contain the access tag "yes" or "permissive". If the
 * access tag is found, then the highway tag is also checked. Finally, the object is flagged if the
 * highway tag is found in either motorway tags or in the footway tags provided in the beginning.
 *
 * @author v-naydinyan
 */
public class HighwayAccessCheck extends BaseCheck<Long>
{

    private static final long serialVersionUID = -5533238262833368666L;
    private static final List<String> ACCESS_TAGS_TO_FLAG_DEFAULT = Arrays.asList("yes",
            "permissive");
    private static final List<String> HIGHWAY_TAGS_TO_FLAG_DEFAULT = Arrays.asList("motorway",
            "trunk", "footway", "bridleway", "steps", "path", "cycleway", "pedestrian", "track",
            "bus_guideway", "busway", "raceway");

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "The access tag value is probably too generic for this way. A tag value of \"yes\" or \"permissive\" allows access to all types of traffic. If special access is granted or restricted on this way then please specify it or remove the access tag. See https://wiki.openstreetmap.org/wiki/Key:access?uselang=en#Transport_mode_restrictions for more information.");

    private final List<String> accessTagsToFlag;
    private final List<String> highwayTagsToFlag;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation. There are no internal variables
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public HighwayAccessCheck(final Configuration configuration)
    {
        super(configuration);

        this.accessTagsToFlag = this.configurationValue(configuration, "tags.accessTags",
                ACCESS_TAGS_TO_FLAG_DEFAULT);
        this.highwayTagsToFlag = this.configurationValue(configuration, "tags.highwayTags",
                HIGHWAY_TAGS_TO_FLAG_DEFAULT);

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
                && (object instanceof Edge && ((Edge) object).isMainEdge());
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
        this.markAsFlagged(object.getOsmIdentifier());

        final String accessTag = object.tag(AccessTag.KEY);
        final String highwayTag = object.tag(HighwayTag.KEY);

        // Check if the access tag is yes or permissive
        if (this.accessTagsToFlag.contains(accessTag)
                && this.highwayTagsToFlag.contains(highwayTag))
        {
            return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0)));

        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

}
