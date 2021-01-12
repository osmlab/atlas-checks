package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags Edges and Nodes that have highway tag values that are not contained within the
 * set of known highway tags on the OSM Wiki page - https://wiki.openstreetmap.org/wiki/Key:highway
 *
 * @author v-garei
 */

public class UnknownHighwayTagCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -7273798399961675550L;
    private static final String NODE_WITH_WAY_TAG_INSTRUCTIONS = "This Node's highway tag only belongs on a way, please update this Node's tag with only Node related tags. List of tags can be found at https://wiki.openstreetmap.org/wiki/Key:highway";
    private static final String WAY_WITH_NODE_TAG_INSTRUCTIONS = "This Way's highway tag only belongs on a way, please update this Way's tag with only Way related tags. List of tags can be found at https://wiki.openstreetmap.org/wiki/Key:highway";
    private static final String UNKNOWN_HIGHWAY_TAG_INSTRUCTIONS = "Please update highway tag to a known value.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            NODE_WITH_WAY_TAG_INSTRUCTIONS, WAY_WITH_NODE_TAG_INSTRUCTIONS,
            UNKNOWN_HIGHWAY_TAG_INSTRUCTIONS);
    private static final Set<HighwayTag> NODE_ONLY_HIGHWAYTAGS = Set.of(HighwayTag.BUS_STOP,
            HighwayTag.CROSSING, HighwayTag.EMERGENCY_ACCESS_POINT, HighwayTag.GIVE_WAY,
            HighwayTag.MILESTONE, HighwayTag.MINI_ROUNDABOUT, HighwayTag.MOTORWAY_JUNCTION,
            HighwayTag.PASSING_PLACE, HighwayTag.SPEED_CAMERA, HighwayTag.STREET_LAMP,
            HighwayTag.STOP, /* HighwayTag.TRAFFIC_MIRROR, */ HighwayTag.TRAFFIC_SIGNALS,
            /* HighwayTag.TRAILHEAD, */ HighwayTag.TURNING_LOOP, HighwayTag.TOLL_GANTRY);
    private static final Set<HighwayTag> WAY_ONLY_HIGHWAYTAGS = Set.of(HighwayTag.MOTORWAY,
            HighwayTag.TRUNK, HighwayTag.PRIMARY, HighwayTag.SECONDARY, HighwayTag.TERTIARY,
            HighwayTag.UNCLASSIFIED, HighwayTag.RESIDENTIAL, HighwayTag.MOTORWAY_LINK,
            HighwayTag.TRUNK_LINK, HighwayTag.PRIMARY_LINK, HighwayTag.SECONDARY_LINK,
            HighwayTag.TERTIARY_LINK, HighwayTag.LIVING_STREET, HighwayTag.SERVICE,
            HighwayTag.PEDESTRIAN, HighwayTag.TRACK, HighwayTag.BUS_GUIDEWAY, HighwayTag.ESCAPE,
            HighwayTag.RACEWAY, HighwayTag.ROAD, HighwayTag.FOOTWAY, HighwayTag.BRIDLEWAY,
            HighwayTag.STEPS, HighwayTag.CORRIDOR, HighwayTag.PATH, HighwayTag.CYCLEWAY,
            HighwayTag.PROPOSED, HighwayTag.CONSTRUCTION);

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public UnknownHighwayTagCheck(final Configuration configuration)
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
        return ((object instanceof Edge && ((Edge) object).isMainEdge()) || object instanceof Node)
                && !isFlagged(object.getOsmIdentifier());
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
        markAsFlagged(object.getOsmIdentifier());
        final Optional<String> objectHighwayTag = object.getTag(HighwayTag.KEY);
        final Set<String> nodeOnlyTags = NODE_ONLY_HIGHWAYTAGS.stream().map(Enum::toString)
                .collect(Collectors.toSet());
        final Set<String> wayOnlyTags = WAY_ONLY_HIGHWAYTAGS.stream().map(Enum::toString)
                .collect(Collectors.toSet());
        final Set<String> allHighwayTags = Arrays.stream(HighwayTag.values()).map(Enum::toString)
                .collect(Collectors.toSet());

        if (object instanceof Node && objectHighwayTag.isPresent()
                && wayOnlyTags.contains(objectHighwayTag.get().toUpperCase()))
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }

        if (object instanceof Edge && objectHighwayTag.isPresent()
                && nodeOnlyTags.contains(objectHighwayTag.get().toUpperCase()))
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(1, object.getOsmIdentifier())));
        }

        if (objectHighwayTag.isPresent()
                && !this.isKnownHighwayTag(objectHighwayTag.get().toUpperCase(), allHighwayTags))
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(2, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * @param highwayTag
     *            object highway tag
     * @param knownTags
     *            all known osm tags from https://wiki.openstreetmap.org/wiki/Key:highway
     * @return boolean for if the object's highway tag is contained within the known set of OSM
     *         highway tags.
     */
    private boolean isKnownHighwayTag(final String highwayTag, final Set<String> knownTags)
    {
        return knownTags.contains(highwayTag);
    }
}
