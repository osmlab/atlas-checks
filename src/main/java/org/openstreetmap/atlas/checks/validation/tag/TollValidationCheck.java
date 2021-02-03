package org.openstreetmap.atlas.checks.validation.tag;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.BarrierTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.TollTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Counter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This check attempts to validate toll tags based on 3 scenarios. 1. Edge intersects toll feature
 * but is missing toll tag 2. Edge has inconsistent toll tag compared to surrounding edges 3. Edge
 * has route that can escape toll feature so the toll tag is modeled incorrectly.
 *
 * @author greichenberger
 */
public class TollValidationCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -4286937145318778446L;
    private static final String INTERSECTS_TOLL_FEATURE = "Way {0, number, #} intersects toll feature but is missing toll tag, please investigate toll tag addition.";
    private static final String ESCAPABLE_TOLL = "Toll tags need to be investigated for removal on way {0, number, #}. Please check ways {1, number, #} and {2, number, #} and affected nearby ways for modeling issues. Nearby toll features "
            + "that might be helpful are: upstream {3, number, #} and downstream {4, number, #}.";
    private static final String INCONSISTENT_TOLL_TAGS = "Way {0, number, #} has an inconsistent toll tag with its surrounding ways. Please check for proper toll tag modeling.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INTERSECTS_TOLL_FEATURE,
            ESCAPABLE_TOLL, INCONSISTENT_TOLL_TAGS);
    private static final String HIGHWAY_MINIMUM_DEFAULT = HighwayTag.RESIDENTIAL.toString();
    private static final Double MAX_ANGLE_DIFF_DEFAULT = 40.0;
    private static final double MIN_IN_OUT_EDGES_DEFAULT = 1.0;
    private static final double MAX_ITERATION_FOR_SEARCH_DEFAULT = 15.0;
    private final HighwayTag minHighwayType;
    private final double minInAndOutEdges;
    private final double maxAngleDiffForContiguousWays;
    private final double maxIterationForNearbySearch;

    /**
     * @param configuration
     *            config file params if any.
     */
    public TollValidationCheck(final Configuration configuration)
    {
        super(configuration);
        final String highwayType = this.configurationValue(configuration, "minHighwayType",
                HIGHWAY_MINIMUM_DEFAULT);
        this.minHighwayType = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());
        this.maxAngleDiffForContiguousWays = this.configurationValue(configuration,
                "maxAngleDiffForContiguousWays", MAX_ANGLE_DIFF_DEFAULT);
        this.minInAndOutEdges = this.configurationValue(configuration, "minInAndOutEdges",
                MIN_IN_OUT_EDGES_DEFAULT);
        this.maxIterationForNearbySearch = this.configurationValue(configuration,
                "maxIterationForNearbySearch", MAX_ITERATION_FOR_SEARCH_DEFAULT);
    }

    /**
     * @param object
     *            The {@link AtlasObject} you are checking
     * @return validation check
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMainEdge()
                && ((Edge) object).highwayTag().isMoreImportantThan(this.minHighwayType)
                && !isFlagged(object.getOsmIdentifier())
                && !this.isPrivateAccess(object.getOsmTags());
    }

    /**
     * @param object
     *            object in question
     * @return flag
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edgeInQuestion = ((Edge) object).getMainEdge();
        final Map<String, String> edgeInQuestionTags = edgeInQuestion.getOsmTags();
        final Set<Long> alreadyCheckedNearbyTollEdges = new HashSet<>();
        final Set<Long> alreadyCheckedObjectIds = new HashSet<>();

        // Case One: Edge intersects toll feature but is missing toll tag.
        if (this.isCaseOne(edgeInQuestion, edgeInQuestionTags))
        {
            markAsFlagged(edgeInQuestion.getOsmIdentifier());
            return Optional.of(this
                    .createFlag(new OsmWayWalker(edgeInQuestion).collectEdges(),
                            this.getLocalizedInstruction(0, edgeInQuestion.getOsmIdentifier()))
                    .addFixSuggestion(FeatureChange.add(
                            (AtlasEntity) ((CompleteEntity) CompleteEntity
                                    .from((AtlasEntity) object)).withAddedTag(TollTag.KEY,
                                            TollTag.YES.toString().toLowerCase()),
                            object.getAtlas())));
        }

        // Case Two: Inconsistent toll tags on edge.
        if (this.isCaseTwo(edgeInQuestion, edgeInQuestionTags))
        {
            markAsFlagged(edgeInQuestion.getOsmIdentifier());
            return Optional.of(this
                    .createFlag(new OsmWayWalker(edgeInQuestion).collectEdges(),
                            this.getLocalizedInstruction(2, edgeInQuestion.getOsmIdentifier()))
                    .addFixSuggestion(FeatureChange.add(
                            (AtlasEntity) ((CompleteEntity) CompleteEntity
                                    .from((AtlasEntity) object)).withAddedTag(TollTag.KEY,
                                            TollTag.YES.toString().toLowerCase()),
                            object.getAtlas())));
        }

        final Edge escapableInEdge = this
                .edgeProvingBackwardsIsEscapable(edgeInQuestion, alreadyCheckedObjectIds)
                .orElse(null);
        final Edge escapableOutEdge = this
                .edgeProvingForwardIsEscapable(edgeInQuestion, alreadyCheckedObjectIds)
                .orElse(null);

        // Case three: tag modeling needs to be investigated on and around edge in question/proved
        // escapable routes
        if (this.escapableEdgesNullChecker(escapableInEdge, escapableOutEdge) && !this.hasInconsistentTollTag(escapableInEdge)
                && !this.hasInconsistentTollTag(escapableOutEdge)
                && this.isCaseThree(edgeInQuestion, edgeInQuestionTags, escapableInEdge, escapableOutEdge))
        {
            markAsFlagged(object.getOsmIdentifier());
            final Counter counter = new Counter();
            final Long nearbyTollFeatureUpstream = this.getNearbyTollFeatureInEdgeSide(
                    edgeInQuestion, alreadyCheckedNearbyTollEdges, counter).orElse(null);
            counter.reset();
            final Long nearbyTollFeatureDownstream = this.getNearbyTollFeatureOutEdgeSide(
                    edgeInQuestion, alreadyCheckedNearbyTollEdges, counter).orElse(null);
            return Optional.of(this.createFlag(new OsmWayWalker(edgeInQuestion).collectEdges(),
                    this.getLocalizedInstruction(1, edgeInQuestion.getOsmIdentifier(),
                            escapableInEdge.getOsmIdentifier(), escapableOutEdge.getOsmIdentifier(),
                            nearbyTollFeatureUpstream, nearbyTollFeatureDownstream)));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * @param edge1
     *            just an edge
     * @param edge2
     *            just another edge
     * @return angle in degrees between edges (segments)
     */
    private Angle angleDiffBetweenEdges(final Edge edge1, final Edge edge2)
    {
        final Optional<Heading> edge1heading = edge1.asPolyLine().finalHeading();
        final Optional<Heading> edge2heading = edge2.asPolyLine().initialHeading();
        if (edge1heading.isPresent() && edge2heading.isPresent())
        {
            return edge1heading.get().difference(edge2heading.get());
        }
        return Angle.NONE;
    }

    /**
     * @param tags
     *            some osm tags
     * @return boolean if the barrier tag contains 'toll'
     */
    private boolean barrierTagContainsToll(final Map<String, String> tags)
    {
        return tags.get(BarrierTag.KEY).contains(TollTag.KEY);
    }

    /**
     * @param tags
     *            some edge tags
     * @param tags2
     *            some other edge tags
     * @return both sets of tags have toll=yes
     */
    private boolean bothTollYesTag(final Map<String, String> tags, final Map<String, String> tags2)
    {
        return this.hasTollYesTag(tags) && this.hasTollYesTag(tags2);
    }

    /**
     * @param tags
     *            some osm tags
     * @return boolean if tags contains key 'barrier'
     */
    private boolean containsBarrierTag(final Map<String, String> tags)
    {
        return tags.containsKey(BarrierTag.KEY);
    }

    /**
     * @param tags
     *            some osm tags
     * @return boolean if tags contains key 'highway'
     */
    private boolean containsHighwayTag(final Map<String, String> tags)
    {
        return tags.containsKey(HighwayTag.KEY);
    }

    /**
     * @param tags
     *            some osm tags
     * @return boolean if tags contains key 'toll'
     */
    private boolean containsTollTag(final Map<String, String> tags)
    {
        return tags.containsKey(TollTag.KEY);
    }

    /**
     * @param edge
     *            some edge
     * @return boolean if edge intersects toll feature
     */
    private boolean edgeIntersectsTollFeature(final Edge edge)
    {
        final Iterable<Area> intersectingAreas = edge.getAtlas().areasIntersecting(edge.bounds());
        final Iterable<Node> edgeNodes = edge.connectedNodes();
        for (final Area area : intersectingAreas)
        {
            final boolean areaContainsPolyline = area.asPolygon().overlaps(edge.asPolyLine());
            final Map<String, String> areaTags = area.getOsmTags();
            if (areaContainsPolyline && this.containsBarrierTag(areaTags)
                    && this.barrierTagContainsToll(areaTags))
            {
                return true;
            }
        }

        for (final Node node : edgeNodes)
        {
            final Map<String, String> nodeTags = node.getOsmTags();
            if ((this.containsHighwayTag(nodeTags) && this.highwayTagContainsToll(nodeTags))
                    || this.containsBarrierTag(nodeTags) && this.barrierTagContainsToll(nodeTags))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param edge
     *            edge in question
     * @param alreadyCheckedObjectIds
     *            already been touched IDs
     * @return edge that proves backwards is escapable
     */
    private Optional<Edge> edgeProvingBackwardsIsEscapable(final Edge edge,
            final Set<Long> alreadyCheckedObjectIds)
    {
        final List<Edge> inEdges = this.getInEdges(edge);

        for (final Edge inEdge : inEdges)
        {
            if (inEdges.size() >= this.minInAndOutEdges
                    && !alreadyCheckedObjectIds.contains(inEdge.getIdentifier())
                    && inEdge.highwayTag().isMoreImportantThan(this.minHighwayType)
                    && this.hasSameHighwayTag(edge, inEdge)
                    && this.angleDiffBetweenEdges(inEdge, edge)
                            .asDegrees() <= this.maxAngleDiffForContiguousWays)
            {
                alreadyCheckedObjectIds.add(inEdge.getIdentifier());
                final Map<String, String> keySet = inEdge.getOsmTags();

                if ((!this.containsTollTag(keySet)) || (this.containsTollTag(keySet)
                        && keySet.get(TollTag.KEY).equalsIgnoreCase(TollTag.NO.toString())))
                {
                    return Optional.of(inEdge);
                }

                if (!this.edgeIntersectsTollFeature(inEdge) && this.containsTollTag(keySet)
                        && keySet.get(TollTag.KEY).equalsIgnoreCase(TollTag.YES.toString()))
                {
                    return this.edgeProvingBackwardsIsEscapable(inEdge, alreadyCheckedObjectIds);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * @param edge
     *            edge in question
     * @param alreadyCheckedObjectIds
     *            already been touched Ids
     * @return edge proving forward is escapable.
     */
    private Optional<Edge> edgeProvingForwardIsEscapable(final Edge edge,
            final Set<Long> alreadyCheckedObjectIds)
    {
        final List<Edge> outEdges = this.getOutEdges(edge);
        for (final Edge outEdge : outEdges)
        {
            if (outEdges.size() >= this.minInAndOutEdges
                    && !alreadyCheckedObjectIds.contains(outEdge.getIdentifier())
                    && outEdge.highwayTag().isMoreImportantThan(this.minHighwayType)
                    && this.hasSameHighwayTag(edge, outEdge)
                    && this.angleDiffBetweenEdges(edge, outEdge)
                            .asDegrees() <= this.maxAngleDiffForContiguousWays)
            {
                alreadyCheckedObjectIds.add(outEdge.getIdentifier());
                final Map<String, String> keySet = outEdge.getOsmTags();

                if ((!this.containsTollTag(keySet)) || (this.containsTollTag(keySet)
                        && keySet.get(TollTag.KEY).equalsIgnoreCase(TollTag.NO.toString())))
                {
                    return Optional.of(outEdge);
                }
                if (!this.edgeIntersectsTollFeature(outEdge) && this.containsTollTag(keySet)
                        && keySet.get(TollTag.KEY).equalsIgnoreCase(TollTag.YES.toString()))
                {
                    return this.edgeProvingForwardIsEscapable(outEdge, alreadyCheckedObjectIds);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * @param escapableInEdge
     *            escapable in edge
     * @param escapableOutEdge
     *            escapable out edge
     * @return boolean for if they are both null
     */
    private boolean escapableEdgesNullChecker(final Edge escapableInEdge,
            final Edge escapableOutEdge)
    {
        return escapableInEdge != null && escapableOutEdge != null;
    }

    /**
     * @param edge
     *            an edge
     * @param alreadyCheckedNearbyTollEdges
     *            edge that have already been touched when recursing.
     * @return Id for intersecting toll feature.
     */
    private Optional<Long> getAreaOrNodeIntersectionId(final Edge edge,
            final Set<Long> alreadyCheckedNearbyTollEdges)
    {
        alreadyCheckedNearbyTollEdges.add(edge.getIdentifier());
        final Iterable<Area> intersectingAreas = edge.getAtlas().areasIntersecting(edge.bounds());
        final Iterable<Node> edgeNodes = edge.connectedNodes();
        for (final Area area : intersectingAreas)
        {
            final boolean areaContainsPolyline = area.asPolygon().overlaps(edge.asPolyLine());
            final Map<String, String> areaTags = area.getOsmTags();
            if (areaContainsPolyline && this.containsBarrierTag(areaTags)
                    && (this.barrierTagContainsToll(areaTags)))
            {
                return Optional.of(area.getOsmIdentifier());
            }
        }

        for (final Node node : edgeNodes)
        {
            final Map<String, String> nodeTags = node.getOsmTags();
            if ((this.containsHighwayTag(nodeTags) && this.highwayTagContainsToll(nodeTags))
                    || (this.containsBarrierTag(nodeTags) && this.barrierTagContainsToll(nodeTags)))
            {
                return Optional.of(node.getOsmIdentifier());
            }
        }
        return Optional.empty();
    }

    /**
     * @param edge
     *            some edge
     * @return in edges that are car navigable and positive (eliminates reverse edges)
     */
    private List<Edge> getInEdges(final Edge edge)
    {
        return edge.inEdges().stream().filter(
                someEdge -> someEdge.isMainEdge()
                        && HighwayTag.isCarNavigableHighway(someEdge)).sorted().collect(Collectors.toList());
    }

    /**
     * @param edge
     *            edge in question
     * @return nearby toll feature id on the in edge side of the edge in question (upstream)
     */
    private Optional<Long> getNearbyTollFeatureInEdgeSide(final Edge edge,
            final Set<Long> alreadyCheckedNearbyTollEdges, final Counter counter)
    {
        final List<Edge> inEdges = this.getInEdges(edge);
        for (final Edge inEdge : inEdges)
        {
            if (inEdges.size() >= this.minInAndOutEdges && this.edgeIntersectsTollFeature(inEdge)
                    && !alreadyCheckedNearbyTollEdges.contains(inEdge.getIdentifier()))
            {
                return this.getAreaOrNodeIntersectionId(inEdge, alreadyCheckedNearbyTollEdges);
            }
            if (counter.getValue() <= this.maxIterationForNearbySearch
                    && inEdges.size() >= this.minInAndOutEdges
                    && !this.edgeIntersectsTollFeature(inEdge)
                    && !alreadyCheckedNearbyTollEdges.contains(inEdge.getIdentifier()))
            {
                alreadyCheckedNearbyTollEdges.add(inEdge.getIdentifier());
                counter.add(1);
                return this.getNearbyTollFeatureInEdgeSide(inEdge, alreadyCheckedNearbyTollEdges,
                        counter);
            }
        }
        return Optional.empty();
    }

    /**
     * @param edge
     *            edge in question
     * @return nearby toll feature id on the out edge side of the edge in question (downstream)
     */
    private Optional<Long> getNearbyTollFeatureOutEdgeSide(final Edge edge,
            final Set<Long> alreadyCheckedNearbyTollEdges, final Counter counter)
    {
        final List<Edge> outEdges = this.getOutEdges(edge);

        for (final Edge outEdge : outEdges)
        {
            if (outEdges.size() >= this.minInAndOutEdges && this.edgeIntersectsTollFeature(outEdge)
                    && !alreadyCheckedNearbyTollEdges.contains(outEdge.getIdentifier()))
            {
                return this.getAreaOrNodeIntersectionId(outEdge, alreadyCheckedNearbyTollEdges);
            }
            if (counter.getValue() <= this.maxIterationForNearbySearch
                    && outEdges.size() >= this.minInAndOutEdges
                    && !this.edgeIntersectsTollFeature(outEdge)
                    && !alreadyCheckedNearbyTollEdges.contains(outEdge.getIdentifier()))
            {
                alreadyCheckedNearbyTollEdges.add(outEdge.getIdentifier());
                counter.add(1);
                return this.getNearbyTollFeatureOutEdgeSide(outEdge, alreadyCheckedNearbyTollEdges,
                        counter);
            }
        }
        return Optional.empty();
    }

    /**
     * @param edge
     *            some edge
     * @return out edges that are car navigable and positive (eliminates reverse edges)
     */
    private List<Edge> getOutEdges(final Edge edge)
    {
        return edge.outEdges().stream().filter(
                someEdge -> someEdge.isMainEdge()
                        && HighwayTag.isCarNavigableHighway(someEdge)).sorted().collect(Collectors.toList());
    }

    /**
     * @param edge
     *            some edge
     * @return boolean if there are tag inconsistencies between 3 consecutive edges.
     */
    private boolean hasInconsistentTollTag(final Edge edge)
    {
        List<Edge> completeWay = new OsmWayWalker(edge).collectEdges().stream().sorted().collect(Collectors.toList());
        final List<Edge> inEdges = completeWay.get(0).inEdges().stream()
                .filter(inEdge -> inEdge.isMainEdge() && HighwayTag.isCarNavigableHighway(inEdge)).sorted().collect(Collectors.toList());

        final List<Edge> outEdges = completeWay.get(completeWay.size() - 1).outEdges().stream()
                .filter(outEdge -> outEdge.isMainEdge() && HighwayTag.isCarNavigableHighway(outEdge)).sorted().collect(Collectors.toList());

        if (inEdges.size() == 1 && outEdges.size() == 1)
        {
            return this.inconsistentTollTagLogic(inEdges, outEdges, edge);
        }
        return false;

    }

    /**
     * @param edge1
     *            some edge
     * @param edge2
     *            some other edge
     * @return boolean regarding if they have same highway tag?\
     */
    private boolean hasSameHighwayTag(final Edge edge1, final Edge edge2)
    {
        if (HighwayTag.highwayTag(edge1).isPresent() && HighwayTag.highwayTag(edge2).isPresent())
        {
            return edge1.highwayTag().equals(edge2.highwayTag());
        }
        return false;
    }

    /**
     * @param tags
     *            some edge tags
     * @return if tags contains toll=yes
     */
    private boolean hasTollYesTag(final Map<String, String> tags)
    {
        return tags.keySet().stream().anyMatch(tag -> tag.equals(TollTag.KEY))
                && tags.get(TollTag.KEY).equalsIgnoreCase(TollTag.YES.toString());
    }

    /**
     * @param tags
     *            some osm tags
     * @return boolean for if the highway tag contains 'toll'
     */
    private boolean highwayTagContainsToll(final Map<String, String> tags)
    {
        return tags.get(HighwayTag.KEY).contains(TollTag.KEY);
    }

    /**
     * @param inEdges
     *            some inedges
     * @param outEdges
     *            some outedges
     * @param edge
     *            some edge
     * @return boolean for inconsistent tagging
     */
    private boolean inconsistentTollTagLogic(final List<Edge> inEdges, final List<Edge> outEdges,
            final Edge edge)
    {
        for (final Edge inEdge : inEdges)
        {
            for (final Edge outEdge : outEdges)
            {
                if (this.hasSameHighwayTag(edge, inEdge) && this.hasSameHighwayTag(edge, outEdge)
                        && this.angleDiffBetweenEdges(edge, outEdge)
                                .asDegrees() <= this.maxAngleDiffForContiguousWays
                        && this.angleDiffBetweenEdges(inEdge, edge)
                                .asDegrees() <= this.maxAngleDiffForContiguousWays)
                {
                    final Map<String, String> inEdgeOsmTags = inEdge.getOsmTags();
                    final Map<String, String> outEdgeOsmTags = outEdge.getOsmTags();
                    if (this.bothTollYesTag(inEdgeOsmTags, outEdgeOsmTags))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param edgeInQuestion
     *            the edge in question
     * @param edgeInQuestionTags
     *            tags of edge in question
     * @return boolean if is case one This case checks if an edge is intersecting a toll feature and
     *         is missing a toll tag.
     */
    private boolean isCaseOne(final Edge edgeInQuestion,
            final Map<String, String> edgeInQuestionTags)
    {
        return !this.hasTollYesTag(edgeInQuestionTags)
                && this.edgeIntersectsTollFeature(edgeInQuestion);
    }

    /**
     * @param edgeInQuestion
     *            edge in question
     * @param edgeInQuestionTags
     *            edge in question osm tags
     * @param escapableInEdge
     *            edge that proves edge in question is toll escapable
     * @param escapableOutEdge
     *            edge that proves edge in question is toll escapable
     * @return boolean if is case three This case checks edges with a toll tag to see if it has has
     *         a route that can escape the toll, if so there is a modeling issue either on the edge
     *         in question or nearby on the escapable route.
     */
    private boolean isCaseThree(final Edge edgeInQuestion,
            final Map<String, String> edgeInQuestionTags, final Edge escapableInEdge,
            final Edge escapableOutEdge)
    {
        return this.hasTollYesTag(edgeInQuestionTags)
                && !this.edgeIntersectsTollFeature(edgeInQuestion)
                && !this.hasInconsistentTollTag(escapableOutEdge)
                && !this.hasInconsistentTollTag(escapableInEdge);
    }

    /**
     * @param edgeInQuestion
     *            edge in question
     * @param edgeInQuestionTags
     *            edge in question tags
     * @return boolean if is case two This case checks for a way without a toll tag between 2 ways
     *         that do have a toll=yes tag
     */
    private boolean isCaseTwo(final Edge edgeInQuestion,
            final Map<String, String> edgeInQuestionTags)
    {
        return !this.hasTollYesTag(edgeInQuestionTags)
                && this.hasInconsistentTollTag(edgeInQuestion);
    }

    /**
     * @param tags
     *            some edge tags
     * @return boolean regarding access=private tags.
     */
    private boolean isPrivateAccess(final Map<String, String> tags)
    {
        if (tags.containsKey(AccessTag.KEY))
        {
            return tags.get(AccessTag.KEY).equalsIgnoreCase(AccessTag.PRIVATE.toString());
        }
        return false;
    }
}
