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
            return HighwayTag.isCarNavigableHighway(edge)
                    && HighwayTag.highwayTag(edge).isPresent()
                    && edge.highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayType)
                    && !JunctionTag.isRoundabout(edge)
                    && !JunctionTag.isCircular(edge);
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

        final List<Edge> completeWayEdges = new ArrayList<>(new OsmWayWalker(edgeBeingVerified).collectEdges());

        final Node firstEdgeStartNode = completeWayEdges.get(0).start();
        final Node lastEdgeEndNode = completeWayEdges.get(completeWayEdges.size() - 1).end();

        final Set<Edge> firstEdgeStartNodeEdges = firstEdgeStartNode.connectedEdges();
        firstEdgeStartNodeEdges.removeIf(edge ->
                edge.getOsmIdentifier() == edgeBeingVerified.getOsmIdentifier());
        final Set<Edge> lastEdgeEndNodeEdges = lastEdgeEndNode.connectedEdges();
        lastEdgeEndNodeEdges.removeIf(edge ->
                edge.getOsmIdentifier() == edgeBeingVerified.getOsmIdentifier());

        final String edgeBeingVerifiedHighwayTagString = HighwayTag.highwayTag(edgeBeingVerified).get().toString().toLowerCase();
        final Set<String> firstEdgeStartNodeEdgesHighwayTags = this.getHighwayTags(firstEdgeStartNodeEdges);
        final Set<String> lastEdgeEndNodeEdgesHighwayTags = this.getHighwayTags(lastEdgeEndNodeEdges);

        for (final Edge firstEdgeEdge : firstEdgeStartNodeEdges)
        {
            if (HighwayTag.highwayTag(firstEdgeEdge).isEmpty()
                    || isFlagged(firstEdgeEdge.getOsmIdentifier())
                    || isFlagged(edgeBeingVerified.getOsmIdentifier())
                    || firstEdgeStartNodeEdgesHighwayTags.contains(edgeBeingVerifiedHighwayTagString)
                    || this.edgeIsRoundaboutOrCircular(firstEdgeEdge))
            {
                continue;
            }
            final String firstEdgeEdgeHighwayTag = HighwayTag.highwayTag(firstEdgeEdge).get().toString().toLowerCase();
            markAsFlagged(firstEdgeEdge.getOsmIdentifier());

            //Case 1
            if (this.edgeBeingVerifiedCaseOne(edgeBeingVerifiedHighwayTagString) && this.edgeCheckedAgainstCaseOne(firstEdgeEdgeHighwayTag))
            {
                markAsFlagged(edgeBeingVerified.getOsmIdentifier());
                return Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(0, object.getOsmIdentifier())));
            }

            //Case 2
            if (this.edgeBeingVerifiedCaseTwo(edgeBeingVerifiedHighwayTagString) && this.edgeCheckedAgainstCaseTwo(firstEdgeEdgeHighwayTag))
            {
                markAsFlagged(edgeBeingVerified.getOsmIdentifier());
                return Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(0, object.getOsmIdentifier())));
            }

            //Case 3
            if (this.edgeBeingVerifiedCaseThree(edgeBeingVerifiedHighwayTagString) && this.edgeCheckedAgainstCaseThree(firstEdgeEdgeHighwayTag))
            {
                markAsFlagged(edgeBeingVerified.getOsmIdentifier());
                return Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(0, object.getOsmIdentifier())));
            }

            for (final Edge lastEdgeEdge : lastEdgeEndNodeEdges)
            {
                if (HighwayTag.highwayTag(lastEdgeEdge).isEmpty()
                        || isFlagged(lastEdgeEdge.getOsmIdentifier())
                        || isFlagged(edgeBeingVerified.getOsmIdentifier())
                        || lastEdgeEndNodeEdgesHighwayTags.contains(edgeBeingVerifiedHighwayTagString)
                        || this.edgeIsRoundaboutOrCircular(lastEdgeEdge))
                {
                    continue;
                }
                final String lastEdgeEdgeHighwayTag = HighwayTag.highwayTag(lastEdgeEdge).get().toString().toLowerCase();
                markAsFlagged(lastEdgeEdge.getOsmIdentifier());

                //Case 1
                if (this.edgeBeingVerifiedCaseOne(edgeBeingVerifiedHighwayTagString) && this.edgeCheckedAgainstCaseOne(lastEdgeEdgeHighwayTag))
                {
                    markAsFlagged(edgeBeingVerified.getOsmIdentifier());
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(0, object.getOsmIdentifier())));
                }

                //Case 2
                if (this.edgeBeingVerifiedCaseTwo(edgeBeingVerifiedHighwayTagString) && this.edgeCheckedAgainstCaseTwo(lastEdgeEdgeHighwayTag))
                {
                    markAsFlagged(edgeBeingVerified.getOsmIdentifier());
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(0, object.getOsmIdentifier())));
                }

                //Case 3
                if (this.edgeBeingVerifiedCaseThree(edgeBeingVerifiedHighwayTagString) && this.edgeCheckedAgainstCaseThree(lastEdgeEdgeHighwayTag))
                {
                    markAsFlagged(edgeBeingVerified.getOsmIdentifier());
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(0, object.getOsmIdentifier())));
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

    private boolean edgeBeingVerifiedCaseOne(final String edgeHighwayTag)
    {
        return "motorway".equals(edgeHighwayTag)
                || "primary".equals(edgeHighwayTag)
                || "trunk".equals(edgeHighwayTag);
    }

    private boolean edgeBeingVerifiedCaseThree(final String edgeHighwayTag)
    {
        return "tertiary".equals(edgeHighwayTag)
                || "tertiary_link".equals(edgeHighwayTag);
    }

    private boolean edgeBeingVerifiedCaseTwo(final String edgeHighwayTag)
    {
        return "motorway_link".equals(edgeHighwayTag)
                || "primary_link".equals(edgeHighwayTag)
                || "trunk_link".equals(edgeHighwayTag)
                || "secondary".equals(edgeHighwayTag)
                || "secondary_link".equals(edgeHighwayTag);
    }

    private boolean edgeCheckedAgainstCaseOne(final String edgeHighwayTag)
    {
        return "tertiary".equals(edgeHighwayTag)
                || "unclassified".equals(edgeHighwayTag)
                || "residential".equals(edgeHighwayTag)
                || "service".equals(edgeHighwayTag);
    }

    private boolean edgeCheckedAgainstCaseThree(final String edgeHighwayTag)
    {
        return "living_street".equals(edgeHighwayTag)
                || "track".equals(edgeHighwayTag)
                || "service".equals(edgeHighwayTag);
    }

    private boolean edgeCheckedAgainstCaseTwo(final String edgeHighwayTag)
    {
        return "unclassified".equals(edgeHighwayTag)
                || "residential".equals(edgeHighwayTag)
                || "service".equals(edgeHighwayTag);
    }


    private boolean edgeIsRoundaboutOrCircular(final Edge edge)
    {
        return JunctionTag.isCircular(edge) || JunctionTag.isRoundabout(edge);
    }

    private Set<String> getHighwayTags(final Set<Edge> edges)
    {
        final Set<String> highwayTags = new HashSet<>();
        for (final Edge edge : edges)
        {
            if (HighwayTag.highwayTag(edge).isPresent())
            {
                final String highwayTagString = HighwayTag.highwayTag(edge).get().toString().toLowerCase();
                highwayTags.add(highwayTagString);
            }
        }
        return highwayTags;
    }
}
