package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 *
 * @author v-garei
 */
public class SuddenHighwayTypeChangeCheck extends BaseCheck<Long>
{
    private static final String SUDDEN_HIGHWAY_TYPE_CHANGE_INSTRUCTION = "Way {0,number,#} is crude. Please add more nodes/rearrange current nodes to more closely match the road from imagery";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(SUDDEN_HIGHWAY_TYPE_CHANGE_INSTRUCTION);
    private static final String HIGHWAY_MINIMUM_DEFAULT = HighwayTag.RESIDENTIAL.toString();
    private final HighwayTag minHighwayType;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public SuddenHighwayTypeChangeCheck(final Configuration configuration)
    {
        super(configuration);
        final String highwayType = this.configurationValue(configuration, "minHighwayType",
                HIGHWAY_MINIMUM_DEFAULT);
        this.minHighwayType = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());
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
        if (TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMainEdge()
                && !isFlagged(object.getOsmIdentifier()))
        {
            final Edge edge = (Edge) object;
            return HighwayTag.isCarNavigableHighway(edge) && !edge.highwayTag().isLink()
                    && edge.highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayType)
                    && !JunctionTag.isRoundabout(edge) && !JunctionTag.isCircular(edge);
        }
        return false;
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
        final Edge edgeBeingVerified = (Edge) object;
        final Set<Edge> inEdges = edgeBeingVerified.inEdges();
        final Set<Edge> outEdges = edgeBeingVerified.outEdges();
        final double edgeBeingVerifiedHighwayTagValue = this.highwayTagToInt(edgeBeingVerified);
        System.out.println("baseEdge osm identifier: " + edgeBeingVerified.getOsmIdentifier());
        System.out.println("edgeBeingVerifiedInt: " + edgeBeingVerifiedHighwayTagValue);

        for (final Edge inEdge : inEdges)
        {
            markAsFlagged(inEdge.getOsmIdentifier());
            final double inEdgeHighwayTagValue = this.highwayTagToInt(inEdge);
            if (Math.abs(edgeBeingVerifiedHighwayTagValue - inEdgeHighwayTagValue) > 1)
            {
                return Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(0, object.getOsmIdentifier())));
            }
            for (final Edge outEdge : outEdges)
            {
                markAsFlagged(outEdge.getOsmIdentifier());
                final double outEdgeHighwayTagValue = this.highwayTagToInt(outEdge);
                if (Math.abs(edgeBeingVerifiedHighwayTagValue - outEdgeHighwayTagValue) > 1)
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(0, object.getOsmIdentifier())));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private double highwayTagToInt(final Edge edge)
    {
        final String highwayTagString = edge.highwayTag().toString().toLowerCase();
        double highwayTagValue;
        final double bottomLevelHighwayTagValue = 0.0;
        final double tertiaryHighwayTagValue = 2.0;
        final double secondaryHighwayTagValue = 3.0;
        final double primaryHighwayTagValue = 4.0;
        final double trunkHighwayTagValue = 5.0;
        final double motorwayHighwayTagValue = 6.0;

        switch (highwayTagString)
        {
            case "tertiary":
                highwayTagValue = tertiaryHighwayTagValue;
                break;
            case "secondary":
                highwayTagValue = secondaryHighwayTagValue;
                break;
            case "primary":
                highwayTagValue = primaryHighwayTagValue;
                break;
            case "trunk":
                highwayTagValue = trunkHighwayTagValue;
                break;
            case "motorway":
                highwayTagValue = motorwayHighwayTagValue;
                break;
            default:
                highwayTagValue = bottomLevelHighwayTagValue;
        }
        return highwayTagValue;
    }
}
