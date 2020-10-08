package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
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
    private static final String SUDDEN_HIGHWAY_TYPE_CHANGE_INSTRUCTION = "Way {0,number,#} has a connected edge which jumps significantly in highway classification. Please make sure the highway tag is not suspicious.";
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
            return HighwayTag.isCarNavigableHighway(edge) && HighwayTag.highwayTag(edge).isPresent()
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
        final Edge edgeBeingVerified = (Edge) object;

        final List<Edge> completeWayEdges = new ArrayList<>(
                new OsmWayWalker(edgeBeingVerified).collectEdges());

        final Node firstEdgeStartNode = completeWayEdges.get(0).start();
        final Node lastEdgeEndNode = completeWayEdges.get(completeWayEdges.size() - 1).end();

        final Set<Edge> firstEdgeStartNodeEdges = firstEdgeStartNode.connectedEdges();
        firstEdgeStartNodeEdges
                .removeIf(edge -> edge.getOsmIdentifier() == edgeBeingVerified.getOsmIdentifier());
        final Set<Edge> lastEdgeEndNodeEdges = lastEdgeEndNode.connectedEdges();
        lastEdgeEndNodeEdges
                .removeIf(edge -> edge.getOsmIdentifier() == edgeBeingVerified.getOsmIdentifier());

        HighwayTag edgeBeingVerifiedHighwayTag = HighwayTag.NO;
        final Optional<HighwayTag> edgeBeingVerifiedHighwayTagOptional = HighwayTag
                .highwayTag(edgeBeingVerified);
        if (edgeBeingVerifiedHighwayTagOptional.isPresent())
        {
            edgeBeingVerifiedHighwayTag = edgeBeingVerifiedHighwayTagOptional.get();
        }
        final Set<HighwayTag> firstEdgeStartNodeEdgesHighwayTags = this
                .getHighwayTags(firstEdgeStartNodeEdges);
        final Set<HighwayTag> lastEdgeEndNodeEdgesHighwayTags = this
                .getHighwayTags(lastEdgeEndNodeEdges);

        for (final Edge firstEdgeEdge : firstEdgeStartNodeEdges)
        {
            if (HighwayTag.highwayTag(firstEdgeEdge).isPresent()
                    && !isFlagged(firstEdgeEdge.getOsmIdentifier())
                    && !isFlagged(edgeBeingVerified.getOsmIdentifier())
                    && !firstEdgeStartNodeEdgesHighwayTags.contains(edgeBeingVerifiedHighwayTag)
                    && !this.edgeIsRoundaboutOrCircular(firstEdgeEdge))
            {
                HighwayTag firstEdgeEdgeHighwayTag = HighwayTag.NO;
                final Optional<HighwayTag> firstEdgeEdgeHighwayTagOptional = HighwayTag
                        .highwayTag(firstEdgeEdge);
                if (firstEdgeEdgeHighwayTagOptional.isPresent())
                {
                    firstEdgeEdgeHighwayTag = firstEdgeEdgeHighwayTagOptional.get();
                }
                markAsFlagged(firstEdgeEdge.getOsmIdentifier());

                // Case 1
                if (this.isCaseOne(edgeBeingVerifiedHighwayTag, firstEdgeEdgeHighwayTag)
                        || this.isCaseTwo(edgeBeingVerifiedHighwayTag, firstEdgeEdgeHighwayTag)
                        || this.isCaseThree(edgeBeingVerifiedHighwayTag, firstEdgeEdgeHighwayTag))
                {
                    markAsFlagged(edgeBeingVerified.getOsmIdentifier());
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(0, object.getOsmIdentifier())));
                }

                for (final Edge lastEdgeEdge : lastEdgeEndNodeEdges)
                {
                    if (HighwayTag.highwayTag(lastEdgeEdge).isPresent()
                            && !isFlagged(lastEdgeEdge.getOsmIdentifier())
                            && !isFlagged(edgeBeingVerified.getOsmIdentifier())
                            && !lastEdgeEndNodeEdgesHighwayTags
                                    .contains(edgeBeingVerifiedHighwayTag)
                            && !this.edgeIsRoundaboutOrCircular(lastEdgeEdge))
                    {
                        HighwayTag lastEdgeEdgeHighwayTag = HighwayTag.NO;
                        final Optional<HighwayTag> lastEdgeEdgeHighwayTagOptional = HighwayTag
                                .highwayTag(lastEdgeEdge);
                        if (lastEdgeEdgeHighwayTagOptional.isPresent())
                        {
                            lastEdgeEdgeHighwayTag = lastEdgeEdgeHighwayTagOptional.get();
                        }
                        markAsFlagged(lastEdgeEdge.getOsmIdentifier());

                        // Case 1
                        if (this.isCaseOne(edgeBeingVerifiedHighwayTag, lastEdgeEdgeHighwayTag)
                                || this.isCaseTwo(edgeBeingVerifiedHighwayTag,
                                        lastEdgeEdgeHighwayTag)
                                || this.isCaseThree(edgeBeingVerifiedHighwayTag,
                                        lastEdgeEdgeHighwayTag))
                        {
                            markAsFlagged(edgeBeingVerified.getOsmIdentifier());
                            return Optional.of(this.createFlag(object,
                                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
                        }
                    }
                }
            }
        }

        markAsFlagged(object.getOsmIdentifier());
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private boolean edgeBeingVerifiedCaseOne(final HighwayTag edgeHighwayTag)
    {
        return HighwayTag.MOTORWAY.equals(edgeHighwayTag)
                || HighwayTag.PRIMARY.equals(edgeHighwayTag)
                || HighwayTag.TRUNK.equals(edgeHighwayTag);
    }

    private boolean edgeBeingVerifiedCaseThree(final HighwayTag edgeHighwayTag)
    {
        return HighwayTag.TERTIARY.equals(edgeHighwayTag)
                || HighwayTag.TERTIARY_LINK.equals(edgeHighwayTag);
    }

    private boolean edgeBeingVerifiedCaseTwo(final HighwayTag edgeHighwayTag)
    {
        return HighwayTag.MOTORWAY_LINK.equals(edgeHighwayTag)
                || HighwayTag.PRIMARY_LINK.equals(edgeHighwayTag)
                || HighwayTag.TRUNK_LINK.equals(edgeHighwayTag)
                || HighwayTag.SECONDARY.equals(edgeHighwayTag)
                || HighwayTag.SECONDARY_LINK.equals(edgeHighwayTag);
    }

    private boolean edgeCheckedAgainstCaseOne(final HighwayTag edgeHighwayTag)
    {
        return HighwayTag.TERTIARY.equals(edgeHighwayTag)
                || HighwayTag.UNCLASSIFIED.equals(edgeHighwayTag)
                || HighwayTag.RESIDENTIAL.equals(edgeHighwayTag)
                || HighwayTag.SERVICE.equals(edgeHighwayTag);
    }

    private boolean edgeCheckedAgainstCaseThree(final HighwayTag edgeHighwayTag)
    {
        return HighwayTag.LIVING_STREET.equals(edgeHighwayTag)
                || HighwayTag.TRACK.equals(edgeHighwayTag)
                || HighwayTag.SERVICE.equals(edgeHighwayTag);
    }

    private boolean edgeCheckedAgainstCaseTwo(final HighwayTag edgeHighwayTag)
    {
        return HighwayTag.UNCLASSIFIED.equals(edgeHighwayTag)
                || HighwayTag.RESIDENTIAL.equals(edgeHighwayTag)
                || HighwayTag.SERVICE.equals(edgeHighwayTag);
    }

    private boolean edgeIsRoundaboutOrCircular(final Edge edge)
    {
        return JunctionTag.isCircular(edge) || JunctionTag.isRoundabout(edge);
    }

    private Set<HighwayTag> getHighwayTags(final Set<Edge> edges)
    {
        final Set<HighwayTag> highwayTags = new HashSet<>();
        for (final Edge edge : edges)
        {
            final Optional<HighwayTag> highwayTagOptional = HighwayTag.highwayTag(edge);
            if (highwayTagOptional.isPresent())
            {
                final HighwayTag highwayTag = highwayTagOptional.get();
                highwayTags.add(highwayTag);
            }
        }
        return highwayTags;
    }

    private boolean isCaseOne(final HighwayTag edgeHighwayTag1, final HighwayTag edgeHighwayTag2)
    {
        return edgeBeingVerifiedCaseOne(edgeHighwayTag1)
                && edgeCheckedAgainstCaseOne(edgeHighwayTag2);
    }

    private boolean isCaseThree(final HighwayTag edgeHighwayTag1, final HighwayTag edgeHighwayTag2)
    {
        return edgeBeingVerifiedCaseThree(edgeHighwayTag1)
                && edgeCheckedAgainstCaseThree(edgeHighwayTag2);
    }

    private boolean isCaseTwo(final HighwayTag edgeHighwayTag1, final HighwayTag edgeHighwayTag2)
    {
        return edgeBeingVerifiedCaseTwo(edgeHighwayTag1)
                && edgeCheckedAgainstCaseTwo(edgeHighwayTag2);
    }

}
