package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * This identifies roads, that transition from one classification to another and then back to the
 * original classification. The check goes through each edge and finds edges that follow itself with
 * a different highway tag. If a such an edge with different tag is found, the outgoing connections
 * of that second edge is checked. If an edge with similar type is found in a similar direction, we
 * say there is an inconsistency. This check will skip roundabouts by default. Roundabouts take the
 * type of highest priority way connected. However, if a roundabout is not properly tagged (missing
 * junction=roundabout), then it will probably be flagged by this check. The solution is to add
 * junction=roundabout tag and set the highway tag to the highest priority highway tag connecting to
 * junction. Links need to be tagged similar to roundabouts. By default a link way will not be used
 * as reference way. However, link ways could still cause inconsistencies.
 *
 * @author mgostintsev
 * @author mkalender
 * @author brian_l_davis
 * @author savannahostrowski
 * @author micahnacht
 */
public class InconsistentRoadClassificationCheck extends BaseCheck<Long>
{
    private static final String CHANGE_BACK_INSTRUCTION = "Way {0,number,#} goes back to {1} and creates inconsistency.";
    // Constraints to limit search and eliminate some false positives
    private static final String MINIMUM_HIGHWAY_TYPE_DEFAULT = HighwayTag.TERTIARY_LINK.toString();
    private static final double SIMILAR_DIRECTION_DIFFERENCE_THRESHOLD_DEFAULT = 30.0;
    private static final double MAXIMUM_EDGE_LENGTH_DEFAULT = 200.0;
    private static final double LONG_EDGE_THRESHOLD = 1000.0;
    private static final String STARTS_OFF_INSTRUCTION = "Road classification inconsistency exists. Way {0,number,#} starts off as {1}.";
    private static final String WAY_ID_AS_INSTRUCTION = "Way {0,number,#} is identified as {1}.";
    private static final String CURVED_ROAD_INSTRUCTION = "If this edge is part of a curved road, "
            + "then this flag might not require edits or the road classification needs to be modified";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(STARTS_OFF_INSTRUCTION,
            WAY_ID_AS_INSTRUCTION, CHANGE_BACK_INSTRUCTION, CURVED_ROAD_INSTRUCTION);
    private static final int CURVED_ROAD_INSTR_IDX = 3;
    private static final int TWO_CONNECTED_EDGES = 2;
    private static final long serialVersionUID = -7507140896133909501L;
    private final HighwayTag minimumHighwayType;
    private final Distance maximumEdgeLength;
    private final Angle similarDirectionDifferenceThreshold;
    private final Distance longEdgeThreshold;

    public InconsistentRoadClassificationCheck(final Configuration configuration)
    {
        super(configuration);

        final String highwayType = configurationValue(configuration, "minimum.highway.type",
                MINIMUM_HIGHWAY_TYPE_DEFAULT);
        this.minimumHighwayType = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());

        this.maximumEdgeLength = Distance.meters(configurationValue(configuration,
                "maximum.edge.length", MAXIMUM_EDGE_LENGTH_DEFAULT));

        this.similarDirectionDifferenceThreshold = configurationValue(configuration,
                "maximum.direction.change.degrees", SIMILAR_DIRECTION_DIFFERENCE_THRESHOLD_DEFAULT,
                Angle::degrees);
        this.longEdgeThreshold = configurationValue(configuration, "long.edge.threshold",
                LONG_EDGE_THRESHOLD, Distance::meters);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        if (object instanceof Edge && !isFlagged(object.getOsmIdentifier())
                && ((Edge) object).isMainEdge())
        {
            final Edge edge = (Edge) object;

            return edge.highwayTag().isMoreImportantThanOrEqualTo(this.minimumHighwayType)
                    // Skip links and roundabouts
                    && !edge.highwayTag().isLink() && !JunctionTag.isRoundabout(edge)
                    // Edge must have heading
                    && edge.overallHeading().isPresent() &&
                    // Way must not continue beyond this edge
                    edge.outEdges().stream().noneMatch(connectedEdge -> edge
                            .getMainEdgeIdentifier() != connectedEdge.getMainEdgeIdentifier()
                            && edge.getOsmIdentifier() == connectedEdge.getOsmIdentifier());
        }
        return false;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject item)
    {
        final Edge edge = (Edge) item;

        // Collect inconsistent edges with their inconsistent connections
        final List<Tuple<Edge, Set<Edge>>> inconsistentEdgeTuples = this
                .findInconsistentEdges(edge);
        if (!inconsistentEdgeTuples.isEmpty())
        {
            final CheckFlag flag = createFlag(new OsmWayWalker((Edge) item).collectEdges(),
                    this.getLocalizedInstruction(0, edge.getOsmIdentifier(), edge.highwayTag()));
            markAsFlagged(edge.getOsmIdentifier());
            inconsistentEdgeTuples.forEach(inconsistentEdgeTuple ->
            {
                final Edge inconsistentEdge = inconsistentEdgeTuple.getFirst();

                // Get the incoming and outgoing main edges from each edge
                final Set<Edge> inEdges = inconsistentEdge.inEdges().stream()
                        .filter(Edge::isMainEdge).collect(Collectors.toSet());
                final Set<Edge> outEdges = inconsistentEdge.outEdges().stream()
                        .filter(Edge::isMainEdge).collect(Collectors.toSet());

                // Get the length of each edge
                final Distance edgeLength = inconsistentEdge.asPolyLine().length();

                // If both the incoming and outgoing main edges are less than or equal to 2 or
                // the length of the edge is less than the maximum allowed edge length configured
                if ((inEdges.size() <= TWO_CONNECTED_EDGES
                        && outEdges.size() <= TWO_CONNECTED_EDGES)
                        || edgeLength.isLessThanOrEqualTo(this.maximumEdgeLength))
                {
                    flag.addObject(inconsistentEdge, this.getLocalizedInstruction(1,
                            inconsistentEdge.getOsmIdentifier(), inconsistentEdge.highwayTag()));
                    flag.addPoints(Iterables.iterable(inconsistentEdge.start().getLocation(),
                            inconsistentEdge.end().getLocation()));
                    markAsFlagged(inconsistentEdge.getOsmIdentifier());

                    inconsistentEdgeTuple.getSecond().forEach(followingEdge ->
                    {
                        flag.addObject(followingEdge, this.getLocalizedInstruction(2,
                                followingEdge.getOsmIdentifier(), followingEdge.highwayTag()));
                        markAsFlagged(followingEdge.getOsmIdentifier());
                    });

                    if (edgeLength.isLessThanOrEqualTo(this.maximumEdgeLength)

                            && !(inEdges.size() <= 2 && outEdges.size() <= 2))
                    {
                        flag.addObject(inconsistentEdge,
                                this.getLocalizedInstruction(CURVED_ROAD_INSTR_IDX));
                    }
                }

            });
            return Optional.of(flag);
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Some tests need to be applied to all potentially inconsistent roads. This method returns a
     * {@link Predicate} which, when given an {@link Edge} E, returns true if none of those tests
     * rule E out as an inconsistent edge.
     *
     * @param referenceEdge
     *            the original {@link Edge} with which E is inconsistent
     * @param referenceHighwayType
     *            the original {@link HighwayTag} with which E is inconsistent
     * @return A {@link Predicate} which, when given an {@link Edge} E, returns true if none of the
     *         tests that should be applied to all potentially inconsistent {@link Edge}s rule out E
     *         as inconsistent.
     */
    private Predicate<Edge> allConnectedEdgesFilter(final Edge referenceEdge,
            final HighwayTag referenceHighwayType)
    {
        return connectedEdge -> referenceEdge.getIdentifier() != connectedEdge.getIdentifier()
                && !JunctionTag.isRoundabout(connectedEdge)
                && connectedEdge.highwayTag().isMoreImportantThanOrEqualTo(this.minimumHighwayType)
                && this.areInTheSimilarDirection(referenceEdge, connectedEdge)
                && !referenceHighwayType.isOfEqualClassification(connectedEdge.highwayTag())
                && !this.isContinuousOutgoingEdge(connectedEdge);
    }

    /**
     * Checks whether given {@link Edge}s are going towards the similar direction
     *
     * @param edge
     *            First {@link Edge} to get last {@link Segment}'s {@link Heading}
     * @param anotherEdge
     *            Second {@link Edge} to get first {@link Segment}'s {@link Heading}
     * @return true is first {@link Edge} is followed by second {@link Edge}
     */
    private boolean areInTheSimilarDirection(final Edge edge, final Edge anotherEdge)
    {
        // Get first edge's final heading
        final List<Segment> firstEdgeSegments = edge.asPolyLine().segments();
        final Segment lastSegment = firstEdgeSegments.get(firstEdgeSegments.size() - 1);
        final Optional<Heading> finalHeading = lastSegment.heading();

        // Get second edge's initial heading
        final Segment secondSegment = anotherEdge.asPolyLine().segments().get(0);
        final Optional<Heading> initialHeading = secondSegment.heading();
        return finalHeading.isPresent() && initialHeading.isPresent()
                && finalHeading.get().difference(initialHeading.get())
                        .isLessThan(this.similarDirectionDifferenceThreshold);
    }

    /**
     * Finds connections of given {@link Edge} using given type as reference
     *
     * @param referenceHighwayType
     *            {@link HighwayTag} as reference type
     * @param edge
     *            {@link Edge} to check connections
     * @return Set of {@link Edge}s going towards the same direction with given {@link Edge} and
     *         type
     */
    private Stream<Edge> connectionsSimilarToReferenceEdge(final HighwayTag referenceHighwayType,
            final Edge edge)
    {
        return edge.outEdges().stream().filter(Edge::isMainEdge)
                .filter(connectedEdge -> referenceHighwayType
                        .isOfEqualClassification(connectedEdge.highwayTag())
                        && this.areInTheSimilarDirection(edge, connectedEdge));
    }

    /**
     * Finds inconsistent {@link Edge}s connected to given {@link Edge}.
     *
     * @param referenceEdge
     *            {@link Edge} to check connections for inconsistency
     * @return {@link Edge}s with their inconsistent connections
     */
    private List<Tuple<Edge, Set<Edge>>> findInconsistentEdges(final Edge referenceEdge)
    {
        final HighwayTag referenceHighwayType = referenceEdge.highwayTag();

        // Split the outEdges into those that are inconsistent links and those that are not
        final Map<Boolean, List<Edge>> edgesAreProblematicLinks = referenceEdge.outEdges().stream()
                .filter(Edge::isMainEdge)
                .filter(this.allConnectedEdgesFilter(referenceEdge, referenceHighwayType))
                .collect(Collectors.partitioningBy(
                        edge -> this.isProblematicLink(edge, referenceHighwayType)));

        // Take each edge and turn it into an Optional<Tuple<Edge, Set<Edge>>>, but treat
        // inconsistent links and other edges differently.
        return Stream.concat(
                // We know that we want to flag inconsistent link edges
                edgesAreProblematicLinks.get(true).stream()
                        .map(this.getSimilarEdgesTuple(referenceHighwayType,
                                middleEdge -> endEdge -> true)),
                // Only flag other edges if they pass some other criteria
                edgesAreProblematicLinks.get(false).stream()
                        .filter(this.nonLinkEdgesFilter(referenceHighwayType))
                        .map(this.getSimilarEdgesTuple(referenceHighwayType,
                                middleEdge -> endEdge -> !this.loopsBackOnSelf(referenceEdge,
                                        middleEdge, endEdge))))
                // Turn our Optional<Tuple<Edge, Set<Edge>>> into a List<Tuple<Edge, Set<Edge>>>
                .flatMap(result -> result.isPresent() ? Stream.of(result.get()) : Stream.empty())
                .collect(Collectors.toList());
    }

    /**
     * Get a tuple containing an edge E which is inconsistent with referenceHighwayType, and a set
     * of out edges from E which are consistent with referenceHighwayType, filtered by tuplesFilter.
     *
     * @param referenceHighwayType
     *            the original {@link HighwayTag} against which we are comparing
     * @param tuplesFilter
     *            a curried {@link Predicate} which, when given E and some connected edge F, returns
     *            true if we want to include F in the result set
     * @return a function which, when given a potentially inconsistent {@link Edge}, returns an
     *         Optional<Tuple<Edge, Set<Edge>>>. If the edge is actually inconsistent, this tuple
     *         will contain the inconsistent edge, and a set of all outgoing edges which are
     *         consistent with the referenceHighwayType (and therefore inconsistent with the
     *         inconsistent edge). If the potentially inconsistent {@link Edge} is not actually
     *         inconsistent, then this {@link Optional} will be empty.
     */
    private Function<Edge, Optional<Tuple<Edge, Set<Edge>>>> getSimilarEdgesTuple(
            final HighwayTag referenceHighwayType,
            final Function<Edge, Predicate<Edge>> tuplesFilter)
    {
        return connectedEdge ->
        {
            final Set<Edge> inEdges = connectedEdge.inEdges().stream().filter(Edge::isMainEdge)
                    .collect(Collectors.toSet());
            final Set<Edge> outEdges = connectedEdge.outEdges().stream().filter(Edge::isMainEdge)
                    .collect(Collectors.toSet());
            // Get similar connections using first edge identifier and type as reference
            final Set<Edge> similarConnections = this
                    .connectionsSimilarToReferenceEdge(referenceHighwayType, connectedEdge)
                    .filter(tuplesFilter.apply(connectedEdge)).collect(Collectors.toSet());
            if (!similarConnections.isEmpty() && ((inEdges.size() <= TWO_CONNECTED_EDGES
                    && outEdges.size() <= TWO_CONNECTED_EDGES)
                    || connectedEdge.asPolyLine().length()
                            .isLessThanOrEqualTo(this.maximumEdgeLength)))
            {
                return Optional.of(Tuple.createTuple(connectedEdge, similarConnections));
            }
            return Optional.empty();
        };
    }

    /**
     * Is this edge bypassed by an edge with an identical classification to {@code
     * referenceHighwayTag}?
     *
     * @param inconsistency
     *            An {@link Edge} that might be getting bypassed
     * @param referenceHighwayTag
     *            The {@link HighwayTag} of the bypassing road
     * @return True if there is another edge that goes from {@code inconsistency.start()} to {@code
     *         inconsistency.end()} with a highway tag identical to {@code referenceHighwayTag}.
     */
    private boolean isBypassed(final Edge inconsistency, final HighwayTag referenceHighwayTag)
    {
        return inconsistency.start().outEdges().stream().filter(Edge::isMainEdge)
                .anyMatch(edge -> !edge.equals(inconsistency)
                        && edge.end().equals(inconsistency.end())
                        && edge.highwayTag().isIdenticalClassification(referenceHighwayTag));
    }

    /**
     * Check if the edge continues out at the same highway classification
     *
     * @param edge
     *            {@link Edge} to check connections
     * @return true if the edge continues, otherwise false
     */
    private boolean isContinuousOutgoingEdge(final Edge edge)
    {
        return edge.outEdges().stream().filter(Edge::isMainEdge)
                .anyMatch(connectedEdge -> edge.highwayTag()
                        .isOfEqualClassification(connectedEdge.highwayTag())
                        && this.areInTheSimilarDirection(edge, connectedEdge));
    }

    /**
     * Is this edge less important than its surrounding edges, but sufficiently long?
     *
     * @param connectedEdge
     *            The {@link Edge} that we're examining
     * @param referenceHighwayType
     *            The {@link HighwayTag} of the surrounding edges.
     * @return True if {@code connectedEdge} is less important than {@code referenceHighwayType} but
     *         is sufficiently long.
     */
    private boolean isLongLessImportantEdge(final Edge connectedEdge,
            final HighwayTag referenceHighwayType)
    {
        return referenceHighwayType.isMoreImportantThan(connectedEdge.highwayTag())
                && connectedEdge.length().isGreaterThanOrEqualTo(this.longEdgeThreshold);
    }

    /**
     * Uses some heuristics to determine whether {@code edge} is part of a longer, more important
     * road.
     *
     * @param edge
     *            The potentially inconsistent {@link Edge} we're examining
     * @param referenceHighwayType
     *            The {@link HighwayTag} of the reference edge
     * @return {@code True} if this edge is more important than {@code referenceHighwayType} and is
     *         either sufficiently long or is connected on both ends to edges of equal importance.
     */
    private boolean isPartOfALongerRoad(final Edge edge, final HighwayTag referenceHighwayType)
    {
        return edge.highwayTag().isMoreImportantThan(referenceHighwayType) && (edge.length()
                .isGreaterThanOrEqualTo(this.longEdgeThreshold)
                || edge.connectedNodes().stream().allMatch(node -> node.connectedEdges().stream()
                        .anyMatch(connectedEdge -> edge.getMainEdgeIdentifier() != connectedEdge
                                .getMainEdgeIdentifier()
                                && edge.highwayTag()
                                        .isOfEqualClassification(connectedEdge.highwayTag()))));
    }

    /**
     * Is this a potentially inconsistent link?
     *
     * @param inconsistency
     *            A potentially inconsistent {@link Edge}
     * @param referenceTag
     *            The {@link HighwayTag} of the reference edge
     * @return True if {@code inconsistency} is a link, but {@code referenceTag} is not. Note that
     *         we're checking for equality elsewhere in allConnectedEdgesFilter, so we don't need to
     *         check for equal classification here.
     */
    private boolean isProblematicLink(final Edge inconsistency, final HighwayTag referenceTag)
    {
        return inconsistency.highwayTag().isLink() && !referenceTag.isLink();
    }

    /**
     * Does the path of {@code start}-> {@code inconsistent}-> {@code end} create a situation where
     * all three edges share a single start/end point?
     *
     * @param start
     *            The first {@link Edge} in the potentially inconsistent triplet
     * @param inconsistent
     *            The middle (inconsistent) {@link Edge} in the triplet
     * @param end
     *            The final {@link Edge} in the triplet
     * @return True if either the start or the end node of {@code start} is the same as either the
     *         start or end node of {@code inconsistent} and the start or end node of {@code end}.
     */
    private boolean loopsBackOnSelf(final Edge start, final Edge inconsistent, final Edge end)
    {
        return start.connectedNodes().stream()
                .anyMatch(node -> inconsistent.connectedNodes().contains(node)
                        && end.connectedNodes().contains(node));
    }

    /**
     * These are some tests for marking an edge as an inconsistent road which are only applicable if
     * that edge is not an inconsistent link. This method returns a {@link Predicate} which returns
     * true if an edge, assumed to not be an inconsistent link, passes all of those tests (and is
     * therefore potentially an inconsistent road).
     *
     * @param referenceHighwayType
     *            the original {@link HighwayTag} against which we are comparing
     * @return a {@link Predicate} which returns true for potentially inconsistent edges that
     *         warrant further investigation.
     */
    private Predicate<Edge> nonLinkEdgesFilter(final HighwayTag referenceHighwayType)
    {
        return edge -> !this.isPartOfALongerRoad(edge, referenceHighwayType)
                && !this.isBypassed(edge, referenceHighwayType)
                && !this.isLongLessImportantEdge(edge, referenceHighwayType);
    }

}
