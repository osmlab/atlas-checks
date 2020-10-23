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
 * This check identifies ways that make suspiciously large jumps in highway classification
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
            return HighwayTag.isCarNavigableHighway(edge)
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

        final HighwayTag edgeBeingVerifiedHighwayTag = HighwayTag.highwayTag(edgeBeingVerified)
                .orElse(HighwayTag.NO);

        final Set<HighwayTag> firstEdgeStartNodeEdgesHighwayTags = this
                .getHighwayTags(firstEdgeStartNodeEdges);
        final Set<HighwayTag> lastEdgeEndNodeEdgesHighwayTags = this
                .getHighwayTags(lastEdgeEndNodeEdges);

        // Check ways' first and last edge's connected edges for suspiciously large highway tag
        // jumps.
        if (this.firstEdgeStartNodeEdgesHighwayTags(edgeBeingVerifiedHighwayTag,
                firstEdgeStartNodeEdges, firstEdgeStartNodeEdgesHighwayTags)
                || this.lastEdgeEndNodeEdgesHighwayTage(edgeBeingVerifiedHighwayTag,
                        lastEdgeEndNodeEdges, lastEdgeEndNodeEdgesHighwayTags))
        {
            markAsFlagged(object.getOsmIdentifier());
            return Optional.of(
                    createFlag(object, this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }

        markAsFlagged(edgeBeingVerified.getOsmIdentifier());
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Case one: edge being verified is motorway, primary, trunk
     * 
     * @param edgeHighwayTag
     *            tag for edge being verified
     * @return boolean
     */
    private boolean edgeBeingVerifiedCaseOne(final HighwayTag edgeHighwayTag)
    {
        return HighwayTag.MOTORWAY.equals(edgeHighwayTag)
                || HighwayTag.PRIMARY.equals(edgeHighwayTag)
                || HighwayTag.TRUNK.equals(edgeHighwayTag);
    }

    /**
     * Case three: edge being verified is tertiary or tertiary_link
     * 
     * @param edgeHighwayTag
     *            tag for edge being verified
     * @return boolean
     */
    private boolean edgeBeingVerifiedCaseThree(final HighwayTag edgeHighwayTag)
    {
        return HighwayTag.TERTIARY.equals(edgeHighwayTag)
                || HighwayTag.TERTIARY_LINK.equals(edgeHighwayTag);
    }

    /**
     * case two: edge being verified is any link but tertiary.
     * 
     * @param edgeHighwayTag
     *            tag for edge being verified
     * @return boolean
     */
    private boolean edgeBeingVerifiedCaseTwo(final HighwayTag edgeHighwayTag)
    {
        return HighwayTag.MOTORWAY_LINK.equals(edgeHighwayTag)
                || HighwayTag.PRIMARY_LINK.equals(edgeHighwayTag)
                || HighwayTag.TRUNK_LINK.equals(edgeHighwayTag)
                || HighwayTag.SECONDARY.equals(edgeHighwayTag)
                || HighwayTag.SECONDARY_LINK.equals(edgeHighwayTag);
    }

    /**
     * case one: edge checked against is tertiary, residential, service, or unclassified
     * 
     * @param edgeHighwayTag
     *            connected edge highway tag
     * @return boolean
     */
    private boolean edgeCheckedAgainstCaseOne(final HighwayTag edgeHighwayTag)
    {
        return HighwayTag.TERTIARY.equals(edgeHighwayTag)
                || HighwayTag.UNCLASSIFIED.equals(edgeHighwayTag)
                || HighwayTag.RESIDENTIAL.equals(edgeHighwayTag)
                || HighwayTag.SERVICE.equals(edgeHighwayTag);
    }

    /**
     * case three: edge checked against is living_Street, service, or track
     * 
     * @param edgeHighwayTag
     *            connected edge highway tag
     * @return boolean
     */
    private boolean edgeCheckedAgainstCaseThree(final HighwayTag edgeHighwayTag)
    {
        return HighwayTag.LIVING_STREET.equals(edgeHighwayTag)
                || HighwayTag.TRACK.equals(edgeHighwayTag)
                || HighwayTag.SERVICE.equals(edgeHighwayTag);
    }

    /**
     * case two: edge checked against is residential, service, or unclassified
     * 
     * @param edgeHighwayTag
     *            connected edge highway tag
     * @return boolean
     */
    private boolean edgeCheckedAgainstCaseTwo(final HighwayTag edgeHighwayTag)
    {
        return HighwayTag.UNCLASSIFIED.equals(edgeHighwayTag)
                || HighwayTag.RESIDENTIAL.equals(edgeHighwayTag)
                || HighwayTag.SERVICE.equals(edgeHighwayTag);
    }

    /**
     * checks if edge is roundabout or circular
     * 
     * @param edge
     *            edge to check if roundabout or circular
     * @return boolean
     */
    private boolean edgeIsRoundaboutOrCircular(final Edge edge)
    {
        return JunctionTag.isCircular(edge) || JunctionTag.isRoundabout(edge);
    }

    /**
     * checks if edge being verified's first edge start node connected edges make suspicious jumps
     * 
     * @param edgeBeingVerifiedHighwayTag
     *            edge being verified highway tag
     * @param firstEdgeStartNodeEdges
     *            first edge start node edges
     * @param firstEdgeStartNodeEdgesHighwayTags
     *            first edge start node edge highway tags
     * @return boolean
     */
    private boolean firstEdgeStartNodeEdgesHighwayTags(final HighwayTag edgeBeingVerifiedHighwayTag,
            final Set<Edge> firstEdgeStartNodeEdges,
            final Set<HighwayTag> firstEdgeStartNodeEdgesHighwayTags)
    {
        boolean suspiciousJump = false;
        for (final Edge firstEdgeEdge : firstEdgeStartNodeEdges)
        {
            final HighwayTag firstEdgeEdgeHighwayTag = HighwayTag.highwayTag(firstEdgeEdge)
                    .orElse(HighwayTag.NO);
            if ((!edgeBeingVerifiedHighwayTag.equals(HighwayTag.NO)
                    && !firstEdgeEdgeHighwayTag.equals(HighwayTag.NO)
                    && !firstEdgeStartNodeEdgesHighwayTags.contains(edgeBeingVerifiedHighwayTag)
                    && !this.edgeIsRoundaboutOrCircular(firstEdgeEdge))
                    && (this.isCaseOne(edgeBeingVerifiedHighwayTag, firstEdgeEdgeHighwayTag)
                            || this.isCaseTwo(edgeBeingVerifiedHighwayTag, firstEdgeEdgeHighwayTag)
                            || this.isCaseThree(edgeBeingVerifiedHighwayTag,
                                    firstEdgeEdgeHighwayTag)))
            {
                suspiciousJump = true;
            }
        }
        return suspiciousJump;
    }

    /**
     * gets set of highway tags
     * 
     * @param edges
     *            set of edges
     * @return set of highway tags
     */
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

    /**
     * checks if case one for edge being verified and edge checked against is true
     * 
     * @param edgeHighwayTag1
     *            some edge tag
     * @param edgeHighwayTag2
     *            some edge tag
     * @return boolean
     */
    private boolean isCaseOne(final HighwayTag edgeHighwayTag1, final HighwayTag edgeHighwayTag2)
    {
        return this.edgeBeingVerifiedCaseOne(edgeHighwayTag1)
                && this.edgeCheckedAgainstCaseOne(edgeHighwayTag2);
    }

    /**
     * checks if case three for edge being verified and edge checked against is true
     * 
     * @param edgeHighwayTag1
     *            some edge tag
     * @param edgeHighwayTag2
     *            some edge tag
     * @return boolean
     */
    private boolean isCaseThree(final HighwayTag edgeHighwayTag1, final HighwayTag edgeHighwayTag2)
    {
        return this.edgeBeingVerifiedCaseThree(edgeHighwayTag1)
                && this.edgeCheckedAgainstCaseThree(edgeHighwayTag2);
    }

    /**
     * checks if case two for edge being verified and edge checked against is true
     * 
     * @param edgeHighwayTag1
     *            some edge tag
     * @param edgeHighwayTag2
     *            some edge tag
     * @return boolean
     */
    private boolean isCaseTwo(final HighwayTag edgeHighwayTag1, final HighwayTag edgeHighwayTag2)
    {
        return this.edgeBeingVerifiedCaseTwo(edgeHighwayTag1)
                && this.edgeCheckedAgainstCaseTwo(edgeHighwayTag2);
    }

    /**
     * checks if edge being verified last edge's end node connected edges make suspicious jumps
     * 
     * @param edgeBeingVerifiedHighwayTag
     *            edge being verified highway tags
     * @param lastEdgeEndNodeEdges
     *            last edge end node edges
     * @param lastEdgeEndNodeEdgesHighwayTags
     *            last edge end node edge highway tags
     * @return boolean
     */
    private boolean lastEdgeEndNodeEdgesHighwayTage(final HighwayTag edgeBeingVerifiedHighwayTag,
            final Set<Edge> lastEdgeEndNodeEdges,
            final Set<HighwayTag> lastEdgeEndNodeEdgesHighwayTags)
    {
        boolean suspiciousJump = false;
        for (final Edge lastEdgeEdge : lastEdgeEndNodeEdges)
        {
            final HighwayTag lastEdgeEdgeHighwayTag = HighwayTag.highwayTag(lastEdgeEdge)
                    .orElse(HighwayTag.NO);
            if ((!lastEdgeEdgeHighwayTag.equals(HighwayTag.NO)
                    && !edgeBeingVerifiedHighwayTag.equals(HighwayTag.NO)
                    && !lastEdgeEndNodeEdgesHighwayTags.contains(edgeBeingVerifiedHighwayTag)
                    && !this.edgeIsRoundaboutOrCircular(lastEdgeEdge))
                    && (this.isCaseOne(edgeBeingVerifiedHighwayTag, lastEdgeEdgeHighwayTag)
                            || this.isCaseTwo(edgeBeingVerifiedHighwayTag, lastEdgeEdgeHighwayTag)
                            || this.isCaseThree(edgeBeingVerifiedHighwayTag,
                                    lastEdgeEdgeHighwayTag)))
            {
                suspiciousJump = true;
            }
        }
        return suspiciousJump;
    }

}
