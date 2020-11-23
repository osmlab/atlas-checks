package org.openstreetmap.atlas.checks.validation.tag;

import static java.lang.Math.pow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.BarrierTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.TollTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 *
 * @author greichenberger
 */
public class TollValidationCheck extends BaseCheck<Long>
{
    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;
    private static final String INTERSECTS_TOLL_FEATURE = "Way {0, number, #} intersects toll feature but is missing toll tag, please investigate toll tag addition.";
    private static final String ESCAPABLE_TOLL = "Toll tag needs to be investigate for removal on way {0, number, #}. Check ways {1, number, #} and {2, number, #} for modeling issues. Nearby toll features "
            + "that might be helpful are: upstream {3, number, #} and downstream {4, number, #}.";
    private static final String INCONSISTENT_TOLL_TAGS = "Way {0, number, #} has an inconsistent toll tag with its surrounding ways. Please check for proper toll tag modeling.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INTERSECTS_TOLL_FEATURE,
            ESCAPABLE_TOLL, INCONSISTENT_TOLL_TAGS);
    private static final String HIGHWAY_MINIMUM_DEFAULT = HighwayTag.RESIDENTIAL.toString();
    private static final double MIN_ANGLE_DEFAULT = 140.0;
    private static final double MIN_IN_OUT_EDGES = 1.0;
    private final Set<Long> markedInconsistentToll = new HashSet<>();
    private final HighwayTag minHighwayType;
    private final double minInAndOutEdges;
    private final double minAngleForContiguousWays;

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
        this.minAngleForContiguousWays = this.configurationValue(configuration,
                "minAngleForContiguousWays", MIN_ANGLE_DEFAULT);
        this.minInAndOutEdges = this.configurationValue(configuration, "minInAndOutEdges", MIN_IN_OUT_EDGES);
    }

    /**
     * @param object
     *            The {@link AtlasObject} you are checking
     * @return validation check
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        if (TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMainEdge()
                && HighwayTag.isCarNavigableHighway(object)
                && !isFlagged(object.getOsmIdentifier()))
        {
            //
            final Edge edgeInQuestion = ((Edge) object).getMainEdge();
            final Map<String, String> keySet = edgeInQuestion.getOsmTags();
            return !this.isPrivateAccess(keySet)
                    && edgeInQuestion.highwayTag().isMoreImportantThan(this.minHighwayType);
        }
        return false;
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
        final Set<Long> abtNearbyTollEdges = new HashSet<>();
        final Set<Long> abtObjectIds = new HashSet<>();

        if (this.isCaseOne(edgeInQuestion, edgeInQuestionTags))
        {
            markAsFlagged(object.getOsmIdentifier());
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, edgeInQuestion.getOsmIdentifier())));
        }

        if (this.isCaseTwo(edgeInQuestion, edgeInQuestionTags))
        {
            this.markedInconsistentToll.add(edgeInQuestion.getOsmIdentifier());
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(2, edgeInQuestion.getOsmIdentifier())));
        }

        final Edge escapableInEdge = this.edgeProvingBackwardsIsEscapable(edgeInQuestion,
                abtObjectIds);
        final Edge escapableOutEdge = this.edgeProvingForwardIsEscapable(edgeInQuestion,
                abtObjectIds);

        if (this.isCaseThree(edgeInQuestion, edgeInQuestionTags, escapableInEdge, escapableOutEdge))
        {
            markAsFlagged(object.getOsmIdentifier());
            final Long nearbyTollFeatureUpstream = this
                    .getNearbyTollFeatureInEdgeSide(edgeInQuestion, abtNearbyTollEdges);
            final Long nearbyTollFeatureDownstream = this
                    .getNearbyTollFeatureOutEdgeSide(edgeInQuestion, abtNearbyTollEdges);
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(1, edgeInQuestion.getOsmIdentifier(),
                            escapableInEdge.getOsmIdentifier(), escapableOutEdge.getOsmIdentifier(),
                            nearbyTollFeatureUpstream, nearbyTollFeatureDownstream)));
        }

        if (this.escapableEdgesNullChecker(escapableInEdge, escapableOutEdge)
                && !this.markedInconsistentToll.contains(escapableOutEdge.getOsmIdentifier())
                && this.hasInconsistentTollTag(escapableOutEdge))
        {
            this.markedInconsistentToll.add(escapableOutEdge.getOsmIdentifier());
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(2, escapableOutEdge.getOsmIdentifier())));
        }

        if (this.escapableEdgesNullChecker(escapableInEdge, escapableOutEdge)
                && !this.markedInconsistentToll.contains(escapableInEdge.getOsmIdentifier())
                && this.hasInconsistentTollTag(escapableInEdge))
        {
            this.markedInconsistentToll.add(escapableInEdge.getOsmIdentifier());
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(2, escapableInEdge.getOsmIdentifier())));
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
     *            just another edgef
     * @return angle in degrees between edges (segments)
     */
    private double angleBetweenEdges(final Edge edge1, final Edge edge2)
    {
        final List<Segment> edge1Segments = edge1.asPolyLine().segments();
        final List<Segment> edge2Segments = edge2.asPolyLine().segments();
        final Segment edge1EndSeg = edge1Segments.get(edge1Segments.size() - 1);
        final Segment edge2StartSeg = edge2Segments.get(0);
        final double aLength = edge1EndSeg.length().asMeters();
        final double bLength = edge2StartSeg.length().asMeters();
        final double cLength = new Segment(edge1EndSeg.start(), edge2StartSeg.end()).length()
                .asMeters();
        return Math.toDegrees(Math.acos(
                (pow(aLength, 2) + pow(bLength, 2) - pow(cLength, 2)) / (2 * aLength * bLength)));
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
     * @param abtObjectIds
     *            already been touched IDs
     * @return edge that proves backwards is escapable
     */
    private Edge edgeProvingBackwardsIsEscapable(final Edge edge, final Set<Long> abtObjectIds)
    {
        final Set<Edge> inEdges = this.getInEdges(edge);

        for (final Edge inEdge : inEdges)
        {
            if (inEdges.size() >= this.minInAndOutEdges
                    && !abtObjectIds.contains(inEdge.getIdentifier())
                    && inEdge.highwayTag().isMoreImportantThan(this.minHighwayType)
                    && this.hasSameHighwayTag(edge, inEdge)
                    && this.angleBetweenEdges(inEdge, edge) >= this.minAngleForContiguousWays)
            {
                abtObjectIds.add(inEdge.getIdentifier());
                final Map<String, String> keySet = inEdge.getOsmTags();

                if ((!this.containsTollTag(keySet)) || (this.containsTollTag(keySet)
                        && keySet.get(TollTag.KEY).equalsIgnoreCase(TollTag.NO.toString())))
                {
                    return inEdge;
                }

                if (!this.edgeIntersectsTollFeature(inEdge) && this.containsTollTag(keySet)
                        && keySet.get(TollTag.KEY).equalsIgnoreCase(TollTag.YES.toString()))
                {
                    return this.edgeProvingBackwardsIsEscapable(inEdge, abtObjectIds);
                }
            }
        }
        return null;
    }

    /**
     * @param edge
     *            edge in qustion
     * @param abtObjectIds
     *            already been touched Ids
     * @return edge proving forward is escapable.
     */
    private Edge edgeProvingForwardIsEscapable(final Edge edge, final Set<Long> abtObjectIds)
    {
        final Set<Edge> outEdges = this.getOutEdges(edge);
        for (final Edge outEdge : outEdges)
        {
            if (outEdges.size() >= this.minInAndOutEdges
                    && !abtObjectIds.contains(outEdge.getIdentifier())
                    && outEdge.highwayTag().isMoreImportantThan(HighwayTag.RESIDENTIAL)
                    && this.hasSameHighwayTag(edge, outEdge)
                    && this.angleBetweenEdges(edge, outEdge) >= this.minAngleForContiguousWays)
            {
                abtObjectIds.add(outEdge.getIdentifier());
                final Map<String, String> keySet = outEdge.getOsmTags();

                if ((!this.containsTollTag(keySet)) || (this.containsTollTag(keySet)
                        && keySet.get(TollTag.KEY).equalsIgnoreCase(TollTag.NO.toString())))
                {
                    return outEdge;
                }
                if (!this.edgeIntersectsTollFeature(outEdge) && this.containsTollTag(keySet)
                        && keySet.get(TollTag.KEY).equalsIgnoreCase(TollTag.YES.toString()))
                {
                    return this.edgeProvingForwardIsEscapable(outEdge, abtObjectIds);
                }
            }
        }
        return null;
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
     * @param abtNearbyTollEdges
     *            edge that have already been touched when recursing.
     * @return Id for intersecting toll feature.
     */
    private Long getAreaOrNodeIntersectionId(final Edge edge, final Set<Long> abtNearbyTollEdges)
    {
        abtNearbyTollEdges.add(edge.getIdentifier());
        final Iterable<Area> intersectingAreas = edge.getAtlas().areasIntersecting(edge.bounds());
        final Iterable<Node> edgeNodes = edge.connectedNodes();
        for (final Area area : intersectingAreas)
        {
            final boolean areaContainsPolyline = area.asPolygon().overlaps(edge.asPolyLine());
            final Map<String, String> areaTags = area.getOsmTags();
            if (areaContainsPolyline && this.containsBarrierTag(areaTags)
                    && (this.barrierTagContainsToll(areaTags)))
            {
                return area.getOsmIdentifier();
            }
        }

        for (final Node node : edgeNodes)
        {
            final Map<String, String> nodeTags = node.getOsmTags();
            if ((this.containsHighwayTag(nodeTags) && this.highwayTagContainsToll(nodeTags))
                    || (this.containsBarrierTag(nodeTags) && this.barrierTagContainsToll(nodeTags)))
            {
                return node.getOsmIdentifier();
            }
        }
        return null;
    }

    /**
     * @param edge
     *            some edge
     * @return in edges that are car navigable and positive (eliminates reverse edges)
     */
    private Set<Edge> getInEdges(final Edge edge)
    {
        return edge.inEdges().stream().filter(someEdge -> someEdge.getIdentifier() > 0
                && HighwayTag.isCarNavigableHighway(someEdge)).collect(Collectors.toSet());
    }

    /**
     * @param edge
     *            edge in question
     * @return nearby toll feature id on the in edge side of the edge in question (upstream)
     */
    private Long getNearbyTollFeatureInEdgeSide(final Edge edge, final Set<Long> abtNearbyTollEdges)
    {
        final Set<Edge> inEdges = this.getInEdges(edge);
        for (final Edge inEdge : inEdges)
        {
            if (inEdges.size() >= this.minInAndOutEdges
                    && this.edgeIntersectsTollFeature(inEdge)
                    && !abtNearbyTollEdges.contains(inEdge.getIdentifier()))
            {
                return this.getAreaOrNodeIntersectionId(inEdge, abtNearbyTollEdges);
            }
            if (inEdges.size() >= this.minInAndOutEdges
                    && !this.edgeIntersectsTollFeature(inEdge)
                    && !abtNearbyTollEdges.contains(inEdge.getIdentifier()))
            {
                abtNearbyTollEdges.add(inEdge.getIdentifier());
                return this.getNearbyTollFeatureInEdgeSide(inEdge, abtNearbyTollEdges);
            }
        }
        return null;
    }

    /**
     * @param edge
     *            edge in question
     * @return nearby toll feature id on the out edge side of the edge in question (downstream)
     */
    private Long getNearbyTollFeatureOutEdgeSide(final Edge edge,
            final Set<Long> abtNearbyTollEdges)
    {
        final Set<Edge> outEdges = this.getOutEdges(edge);

        for (final Edge outEdge : outEdges)
        {
            if (outEdges.size() >= this.minInAndOutEdges
                    && this.edgeIntersectsTollFeature(outEdge)
                    && !abtNearbyTollEdges.contains(outEdge.getIdentifier()))
            {
                return this.getAreaOrNodeIntersectionId(outEdge, abtNearbyTollEdges);
            }
            if (outEdges.size() >= this.minInAndOutEdges
                    && !this.edgeIntersectsTollFeature(outEdge)
                    && !abtNearbyTollEdges.contains(outEdge.getIdentifier()))
            {
                abtNearbyTollEdges.add(outEdge.getIdentifier());
                return this.getNearbyTollFeatureOutEdgeSide(outEdge, abtNearbyTollEdges);
            }
        }
        return null;
    }

    /**
     * @param edge
     *            some edge
     * @return out edges that are car navigable and positive (eliminates reverse edges)
     */
    private Set<Edge> getOutEdges(final Edge edge)
    {
        return edge.outEdges().stream().filter(someEdge -> someEdge.getIdentifier() > 0
                && HighwayTag.isCarNavigableHighway(someEdge)).collect(Collectors.toSet());
    }

    /**
     * @param edge
     *            some edge
     * @return tag inconsistencies between 3 consecutive edges.
     */
    private boolean hasInconsistentTollTag(final Edge edge)
    {
        final Set<Edge> inEdges = edge.inEdges().stream()
                .filter(inEdge -> inEdge.getOsmIdentifier() != edge.getOsmIdentifier()
                        && inEdge.getIdentifier() > 0 && HighwayTag.isCarNavigableHighway(inEdge))
                .collect(Collectors.toSet());
        final Set<Edge> outEdges = edge.outEdges().stream()
                .filter(outEdge -> outEdge.getOsmIdentifier() != edge.getOsmIdentifier()
                        && outEdge.getIdentifier() > 0 && HighwayTag.isCarNavigableHighway(outEdge))
                .collect(Collectors.toSet());
        if (inEdges.size() == this.minInAndOutEdges && outEdges.size() == this.minInAndOutEdges)
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
    private boolean inconsistentTollTagLogic(final Set<Edge> inEdges, final Set<Edge> outEdges,
            final Edge edge)
    {
        for (final Edge inEdge : inEdges)
        {
            for (final Edge outEdge : outEdges)
            {
                if (this.hasSameHighwayTag(edge, inEdge) && this.hasSameHighwayTag(edge, outEdge)
                        && this.angleBetweenEdges(edge, outEdge) >= this.minAngleForContiguousWays
                        && this.angleBetweenEdges(inEdge, edge) >= this.minAngleForContiguousWays)
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
     * @return boolean if is case one
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
     * @return boolean if is case three
     */
    private boolean isCaseThree(final Edge edgeInQuestion,
            final Map<String, String> edgeInQuestionTags, final Edge escapableInEdge,
            final Edge escapableOutEdge)
    {
        return this.hasTollYesTag(edgeInQuestionTags)
                && !this.edgeIntersectsTollFeature(edgeInQuestion) && escapableInEdge != null
                && escapableOutEdge != null && !this.hasInconsistentTollTag(escapableOutEdge)
                && !this.hasInconsistentTollTag(escapableInEdge);
    }

    /**
     * @param edgeInQuestion
     *            edge in question
     * @param edgeInQuestionTags
     *            edge in question tags
     * @return boolean if is case two
     */
    private boolean isCaseTwo(final Edge edgeInQuestion,
            final Map<String, String> edgeInQuestionTags)
    {
        return !this.hasTollYesTag(edgeInQuestionTags)
                && !this.markedInconsistentToll.contains(edgeInQuestion.getOsmIdentifier())
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
