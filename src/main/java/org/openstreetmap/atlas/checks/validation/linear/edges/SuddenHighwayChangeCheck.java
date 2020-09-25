package org.openstreetmap.atlas.checks.validation.linear.edges;

import static java.lang.Math.pow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;


/**
 * Auto generated Check template
 *
 * @author v-garei
 */
public class SuddenHighwayChangeCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 1L;
    public static final double MIN_ANGLE_DEFAULT = 90.0;
    public static final double MAX_ANGLE_DEFAULT = 160;
    public static final double SHORT_EDGE_THRESHOLD_DEFAULT = 20.0;
    public static final double LONG_EDGE_THRESHOLD_DEFAULT = 150.0;
    private static final String LINK_UPDATE_INSTRUCTION = "Way {0,number,#} should be a link. Please update the highway tag!";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(LINK_UPDATE_INSTRUCTION);
    private final double minAngle;
    private final double maxAngle;
    private final double connectedEdgeMin;
    private final double connectedEdgeMax;
    private final HighwayTag minHighwayClass;
    private final Distance shortEdgeThreshold;
    private final Distance longEdgeThreshold;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     * the JSON configuration for this check
     */
    public SuddenHighwayChangeCheck(final Configuration configuration)
    {
        super(configuration);
        final String highwayType = configurationValue(configuration, "minHighwayType", "tertiary");
        this.minAngle = configurationValue(configuration, "angle.min", MIN_ANGLE_DEFAULT);
        this.maxAngle = configurationValue(configuration, "angle.max", MAX_ANGLE_DEFAULT);
        this.minHighwayClass = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());
        this.connectedEdgeMin = configurationValue(configuration, "edgeCounts.connectedEdgeMin", 2.0);
        this.connectedEdgeMax = configurationValue(configuration, "edgeCounts.connectedEdgeMin", 3.0);
        this.shortEdgeThreshold = configurationValue(configuration, "length.min", SHORT_EDGE_THRESHOLD_DEFAULT, Distance::meters);
        this.longEdgeThreshold = configurationValue(configuration, "length.max", LONG_EDGE_THRESHOLD_DEFAULT, Distance::meters);
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
        if (object instanceof Edge
                && !isFlagged(object.getOsmIdentifier())
                && ((Edge) object).isMainEdge())
        {
            final Edge edge = (Edge) object;

            return !this.isEdgeLink(edge)
                    && HighwayTag.isCarNavigableHighway(object)
                    && this.baseEdgeStartHasTwoOrThreeConnectedEdges(edge)
                    && this.baseEdgeEndHasTwoOrThreeConnectedEdges(edge)
                    && this.lengthOfWay(edge) >= this.shortEdgeThreshold.asMeters()
                    && this.lengthOfWay(edge) <= this.longEdgeThreshold.asMeters()
                    && edge.highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayClass)
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

        final Edge baseEdge = (Edge) object;

        this.markAsFlagged(baseEdge.getOsmIdentifier());

        final List<Segment> baseSegments = baseEdge.asPolyLine().segments();
        final Segment firstBaseSegment = baseSegments.get(0);
        final Segment lastBaseSegment = baseSegments.get(baseSegments.size() - 1);

        final Optional<String> endHighWayField = baseEdge.end().getTag("highway");
        final Optional<String> startHighWayField = baseEdge.start().getTag("highway");

        final Set<Edge> connectedToBaseStartEdges = baseEdge.start().connectedEdges().stream()
                .filter(Edge::isMainEdge).collect(Collectors.toSet());
        final Set<Edge> connectedToBaseEndEdges = baseEdge.end().connectedEdges().stream()
                .filter(Edge::isMainEdge).collect(Collectors.toSet());

        final Set<Edge> inEdges = baseEdge.inEdges().stream()
                .filter(Edge::isMainEdge).collect(Collectors.toSet());
        final Set<Edge> outEdges = baseEdge.outEdges().stream()
                .filter(Edge::isMainEdge).collect(Collectors.toSet());

        if (!this.hasInOrOutEdgeAsRoundabout(inEdges, outEdges)
                && !this.isNodeTrafficSignalOrRoundabout(startHighWayField)
                && !this.isNodeTrafficSignalOrRoundabout(endHighWayField)
                && !this.inAndOutEdgeShareNameOrRef(inEdges, outEdges)
                && !this.splitRoadIn(baseEdge, connectedToBaseStartEdges, connectedToBaseEndEdges)
                && !this.splitRoadOut(baseEdge, connectedToBaseStartEdges, connectedToBaseEndEdges))
        {
            label:
            for (final Edge inEdge : inEdges)
            {
                if (this.isEdgeLink(inEdge) || !inEdge.highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayClass) ||
                        baseEdge.getOsmIdentifier() == inEdge.getOsmIdentifier())
                {
                    break;
                }
                for (final Edge outEdge : outEdges)
                {
                    if (this.isEdgeLink(outEdge) || !outEdge.highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayClass) ||
                            baseEdge.getOsmIdentifier() == outEdge.getOsmIdentifier())
                    {
                        break label;
                    }

                    final List<Segment> inEdgeSegments = inEdge.asPolyLine().segments();
                    final Segment lastInEdgeSegment = inEdgeSegments.get(inEdgeSegments.size() - 1);
                    final double angleBetweenInAndBaseSegments = this.findAngle(lastInEdgeSegment, firstBaseSegment);

                    final List<Segment> outEdgeSegments = outEdge.asPolyLine().segments();
                    final Segment firstOutEdgeSegment = outEdgeSegments.get(0);
                    final double angleBetweenOutAndBaseSegments = this.findAngle(lastBaseSegment, firstOutEdgeSegment);

                    if (angleBetweenOutAndBaseSegments > minAngle && angleBetweenOutAndBaseSegments < maxAngle &&
                            angleBetweenInAndBaseSegments > minAngle && angleBetweenInAndBaseSegments < maxAngle &&
                            HighwayTag.isCarNavigableHighway(outEdge) &&
                            this.isInOrOutEdgeDiffHighwayTag(baseEdge, inEdge, outEdge))
                    {
                        final Set<Edge> baseEdgeStartConnectedEdges = baseEdge.start().connectedEdges();
                        final Set<Edge> baseEdgeEndConnectedEdges = baseEdge.end().connectedEdges();

                        Set<Edge> filteredStartNodeEdges = baseEdgeStartConnectedEdges.stream().filter(baseEdgeConnectedEdge -> baseEdgeConnectedEdge.getIdentifier() != baseEdge.getIdentifier()
                                && baseEdgeConnectedEdge.getIdentifier() > 0
                                && !baseEdgeConnectedEdge.highwayTag().isLessImportantThan(this.minHighwayClass)).collect(Collectors.toSet());

                        Set<Edge> filteredEndNodeEdges = baseEdgeEndConnectedEdges.stream().filter(baseEdgeConnectedEdge -> baseEdgeConnectedEdge.getIdentifier() != baseEdge.getIdentifier()
                                && baseEdgeConnectedEdge.getIdentifier() > 0
                                && !baseEdgeConnectedEdge.highwayTag().isLessImportantThan(this.minHighwayClass)).collect(Collectors.toSet());


                        System.out.println("osm id: " + baseEdge.getOsmIdentifier());
//                        System.out.println("startNodeEdges: " + filteredStartNodeEdges);
//                        System.out.println("endNodeEdges: " + filteredEndNodeEdges);
//                        System.out.println("angle between in and base: " + angleBetweenInAndBaseSegments);
//                        System.out.println("angle between out and base: " + angleBetweenOutAndBaseSegments);

//                        return Optional.of(
//                                createFlag(object, this.getLocalizedInstruction(0, object.getOsmIdentifier())));
                    }
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

    /**
     * Check if base edge has at least 2 outedges not including self
     */
    private boolean baseEdgeEndHasTwoOrThreeConnectedEdges(final Edge baseEdge)
    {
        final long baseEdgeOsmId = baseEdge.getIdentifier();
        final Set<Edge> baseEdgeEndConnectedEdges = baseEdge.end().connectedEdges();
        return (baseEdgeEndConnectedEdges.stream().filter(baseEdgeConnectedEdge -> baseEdgeConnectedEdge.getIdentifier() != baseEdgeOsmId
                && baseEdgeConnectedEdge.getIdentifier() > 0
                && !baseEdgeConnectedEdge.highwayTag().isLessImportantThan(this.minHighwayClass)).count() == connectedEdgeMin);
//                &&
//                (baseEdgeEndConnectedEdges.stream().filter(baseEdgeConnectedEdge -> baseEdgeConnectedEdge.getIdentifier() != baseEdgeOsmId
//                && baseEdgeConnectedEdge.getIdentifier() > 0
//                && !baseEdgeConnectedEdge.highwayTag().isLessImportantThan(this.minHighwayClass)).count() >= connectedEdgeMax);
    }

    /**
     * Checks if base edge has at least 2 inedges not including self
     */
    private boolean baseEdgeStartHasTwoOrThreeConnectedEdges(final Edge baseEdge)
    {
        final long baseEdgeOsmId = baseEdge.getIdentifier();
        final Set<Edge> baseEdgeStartConnectedEdges = baseEdge.start().connectedEdges();
        return (baseEdgeStartConnectedEdges.stream().filter(baseEdgeConnectedEdge -> baseEdgeConnectedEdge.getIdentifier() != baseEdgeOsmId
                && baseEdgeConnectedEdge.getIdentifier() > 0
                && !baseEdgeConnectedEdge.highwayTag().isLessImportantThan(this.minHighwayClass)).count() == connectedEdgeMin);
//                &&
//                (baseEdgeStartConnectedEdges.stream().filter(baseEdgeConnectedEdge -> baseEdgeConnectedEdge.getIdentifier() != baseEdgeOsmId
//                && baseEdgeConnectedEdge.getIdentifier() > 0
//                && !baseEdgeConnectedEdge.highwayTag().isLessImportantThan(this.minHighwayClass)).count() >= connectedEdgeMax);
    }

    /**
     * Calculates the angle between the two segments.
     * Assumes that the segments connect at an end point.
     */
    private double findAngle(final Segment segment1, final Segment segment2)
    {
        final double aLength = segment1.length().asMeters();
        final double bLength = segment2.length().asMeters();
        final double cLength = new Segment(segment1.start(), segment2.end()).length().asMeters();
        return Math.toDegrees(Math.acos((pow(aLength, 2) + pow(bLength, 2) - pow(cLength, 2)) / (2 * aLength * bLength)));
    }

    /**
     * in or out edge should not be a roundabout.
     */
    private boolean hasInOrOutEdgeAsRoundabout(final Set<Edge> inEdges, final Set<Edge> outEdges)
    {
        boolean hasRoundaboutTouching = false;
        for (final Edge inEdge : inEdges)
        {
            if (JunctionTag.isRoundabout(inEdge) || JunctionTag.isCircular(inEdge))
            {
                hasRoundaboutTouching = true;
            }
        }
        for (final Edge outEdge : outEdges)
        {
            if(JunctionTag.isRoundabout(outEdge) || JunctionTag.isCircular(outEdge))
            {
                hasRoundaboutTouching = true;
            }
        }
        return hasRoundaboutTouching;
    }

    /**
     * in and out edge should not share a name
     */
    private boolean inAndOutEdgeShareNameOrRef(final Set<Edge> inEdges, final Set<Edge> outEdges)
    {
        boolean edgesShareName = false;
        boolean edgesShareRef = false;
        for (final Edge inEdge : inEdges)
        {
            final Optional<String> inEdgeName = inEdge.getTag("name");
            final Optional<String> inEdgeRef = inEdge.getTag("ref");
            if (inEdgeName.isPresent() || inEdgeRef.isPresent())
            {
                for (final Edge outEdge : outEdges)
                {
                    final Optional<String> outEdgeName = outEdge.getTag("name");
                    final Optional<String> outEdgeRef = outEdge.getTag("ref");
                    if (inEdgeName.isPresent() && outEdgeName.isPresent())
                    {
                        if (inEdgeName.equals(outEdgeName))
                        {
                            edgesShareName = true;
                        }
                    }
                    if (inEdgeRef.isPresent() && outEdgeRef.isPresent())
                    {
                        if (inEdgeRef.equals(outEdgeRef))
                        {
                            edgesShareRef = true;
                        }
                    }
                }
            }
        }
        return edgesShareName || edgesShareRef;
    }

    /**
     * Checks if edge is a link - we want to exclude links
     */
    private boolean isEdgeLink(final Edge edge)
    {
        return edge.highwayTag().isLink();
    }

    /**
     * Checks if outEdge different highway tag from baseEdge
     */
    private boolean isInOrOutEdgeDiffHighwayTag(final Edge baseEdge, final Edge inEdge, final Edge outEdge)
    {
        return baseEdge.highwayTag() != inEdge.highwayTag() || baseEdge.highwayTag() != outEdge.highwayTag();
    }

    /**
     * Checks if highway field is signal or roundabout
     * we don't want either
     */
    private boolean isNodeTrafficSignalOrRoundabout(final Optional<String> tag)
    {
        return tag.filter(s -> s.contains("signal") || s.contains("roundabout")).isPresent();
    }

    /**
     * return length of whole way
     */
    private double lengthOfWay(final AtlasObject object)
    {
        double lengthSum = 0;
        final Set<Edge> completeWay = new OsmWayWalker((Edge) object).collectEdges();
        for (final Edge edge : completeWay)
        {
            lengthSum += edge.length().asMeters();
        }
        return lengthSum;
    }

    /**
     * merge list of sets of edges together
     */
    private Set<Edge> mergeAllEdges(final List<Set<Edge>> sets)
    {
            final Set<Edge> allEdges = new HashSet<>();
            for(final Set<Edge> set : sets)
            {
                allEdges.addAll(set);
            }
            return allEdges;
    }

    /**
     * Checks if split road toward intersection shares other split road with 2 iterations of connected edges
     */
    private boolean splitRoadIn(final Edge baseEdge, final Set<Edge> connectedStartNodeEdges, final Set<Edge> connectedEndNodeEdges)
    {
        boolean splitRoadNotLink = false;
        if (baseEdge.getTag("oneway").isPresent() && baseEdge.getTag("oneway").get().equals("yes"))
        {
            final Set<Long> osmIdentifiers = new HashSet<>();
            final Set<Edge> allMergedEdges;
            final List<Set<Edge>> connectedEdgeSets = new ArrayList<>();
            for (final Edge connectedEdge : connectedEndNodeEdges)
            {
                if (connectedEdge.equals(baseEdge))
                {
                    continue;
                }
                if (connectedEdge.isMainEdge())
                {
                    connectedEdgeSets.add(connectedEdge.connectedEdges().stream()
                            .filter(Edge::isMainEdge).collect(Collectors.toSet()));
                }
            }
            allMergedEdges = this.mergeAllEdges(connectedEdgeSets);
            for (final Edge mergedEdge : allMergedEdges)
            {
                if (mergedEdge.getTag("oneway").isPresent() && mergedEdge.getTag("oneway").get().equals("yes"))
                {
                    osmIdentifiers.add(mergedEdge.getOsmIdentifier());
                }
            }
            for (final Edge startNodeEdge : connectedStartNodeEdges)
            {
                if (startNodeEdge.equals(baseEdge))
                {
                    continue;
                }
                if (osmIdentifiers.contains(startNodeEdge.getOsmIdentifier()))
                {
                    splitRoadNotLink = true;
                }
            }
        }
        return splitRoadNotLink;
    }

    /**
     * Checks if split road away from intersection shares other split road with 2 iterations of connected edges
     */
    private boolean splitRoadOut(final Edge baseEdge, final Set<Edge> connectedStartNodeEdges, final Set<Edge> connectedEndNodeEdges)
    {
        boolean splitRoadNotLink = false;
        final Set<Long> osmIdentifiers = new HashSet<>();
        final Set<Edge> allMergedEdges;
        final List<Set<Edge>> connectedEdgeSets = new ArrayList<>();
        if (baseEdge.getTag("oneway").isPresent() && baseEdge.getTag("oneway").get().equals("yes"))
        {
            for (final Edge connectedEdge : connectedStartNodeEdges)
            {
                if (connectedEdge.equals(baseEdge))
                {
                    continue;
                }
                if (connectedEdge.isMainEdge())
                {
                    connectedEdgeSets.add(connectedEdge.connectedEdges());
                }
            }
            allMergedEdges = this.mergeAllEdges(connectedEdgeSets);
            for (final Edge mergedEdge : allMergedEdges)
            {
                if (mergedEdge.getTag("oneway").isPresent() && mergedEdge.getTag("oneway").get().equals("yes"))
                {
                    osmIdentifiers.add(mergedEdge.getOsmIdentifier());
                }
            }
            for (final Edge endNodeEdge : connectedEndNodeEdges)
            {
                if (endNodeEdge.equals(baseEdge))
                {
                    continue;
                }
                if (osmIdentifiers.contains(endNodeEdge.getOsmIdentifier()))
                {
                    splitRoadNotLink = true;
                }
            }
        }
        return splitRoadNotLink;
    }
}
