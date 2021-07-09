package org.openstreetmap.atlas.checks.validation.tag;

import java.util.*;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * FIXME: include description
 *
 * @author v-naydinyan
 */
public class HighwayAccessCheck extends BaseCheck
{

    private static final long serialVersionUID = -5533238262833368666L;
    private static final List<String> AccessTagsToFlag = Arrays.asList("yes", "permissive");
    private static final List<String> MotorwayHighwayTags = Arrays.asList("motorway", "trunk");
    private static final List<String> FootwayHighwayTags = Arrays.asList("footway", "bridleway", "steps", "path",
            "cycleway", "pedestrian", "track", "bus_guideway", "busway", "raceway");

    private static final String HIGHWAY_IS_MOTORWAY_INSTRUCTION =
            "Including ski, horse, moped, hazmat and so on, unless explicitly excluded.";
    private static final String HIGHWAY_IS_FOOTWAY_INSTRUCTION =
            "Including car, horse, moped, hazmat and so on, unless explicitly excluded.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(HIGHWAY_IS_MOTORWAY_INSTRUCTION,
            HIGHWAY_IS_FOOTWAY_INSTRUCTION);
    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public HighwayAccessCheck(final Configuration configuration)
    {
        super(configuration);
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH,
        // Distance::meters);
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
        return (!this.isFlagged(object.getOsmIdentifier()) && (object instanceof Edge && ((Edge) object).isMainEdge()));
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
        // insert algorithmic check to see whether object needs to be flagged.
        // Example of flagging an object
        // return Optional.of(this.createFlag(object, "Instruction how to fix issue or reason behind
        // flagging the object");
        this.markAsFlagged(object.getOsmIdentifier());

        if (checkAccessTag(object)) {
            if (checkMotorwayHighwayTag(object)) {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0)));
            }
            if (checkFootwayHighwayTag(object)) {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(1)));
            }
        }
            return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions() { return FALLBACK_INSTRUCTIONS; }

    /**
     * FIXME
     * @param object
     * @return
     */
    private boolean checkAccessTag(final AtlasObject object) {
        String accessTag = object.tag(AccessTag.KEY);
        return AccessTagsToFlag.contains(accessTag);
    }

    /**
     * FIXME
     * @param object
     * @return
     */
    private boolean checkMotorwayHighwayTag(final AtlasObject object) {
        String highwayTag = object.tag(HighwayTag.KEY);
        return MotorwayHighwayTags.contains(highwayTag);
    }

    /**
     * FIXME
     * @param object
     * @return
     */
    private boolean checkFootwayHighwayTag(final AtlasObject object) {
        String highwayTag = object.tag(HighwayTag.KEY);
        return FootwayHighwayTags.contains(highwayTag);
    }
}
