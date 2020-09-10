package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
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

import static java.lang.Math.pow;

/**
 * Auto generated Check template
 *
 * @author v-garei
 */
public class SuddenHighwayChange extends BaseCheck<Long>
{

    // You can use serialver to regenerate the serial UID.
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
    private final int inEdgeMin;
    private final int inEdgeMax;
    private final int outEdgeMin;
    private final int outEdgeMax;
    private final HighwayTag minHighwayClass;
    private final Distance shortEdgeThreshold;
    private final Distance longEdgeThreshold;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public SuddenHighwayChange(final Configuration configuration)
    {
        super(configuration);
        final String highwayType = configurationValue(configuration, "minHighwayType", "tertiary");
        this.minAngle = configurationValue(configuration, "angle.min", MIN_ANGLE_DEFAULT);
        this.maxAngle = configurationValue(configuration, "angle.max", MAX_ANGLE_DEFAULT);
        this.minHighwayClass = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());
        this.inEdgeMin = configurationValue(configuration, "edgeCounts.inEdgeMin", 2);
        this.inEdgeMax = configurationValue(configuration, "edgeCounts.inEdgeMax", 6);
        this.outEdgeMin = configurationValue(configuration, "edgeCounts.outEdgeMin", 2);
        this.outEdgeMax = configurationValue(configuration, "edgeCounts.outEdgeMax", 6);
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
        // by default we will assume all objects as valid
        if (object instanceof Edge
                && !isFlagged(object.getOsmIdentifier())
                && ((Edge) object).isMasterEdge()) {
            final Edge edge = (Edge) object;

            return !isEdgeLink(edge)
                    && HighwayTag.isCarNavigableHighway(object)
                    && baseEdgeStartHasAtLeastTwoConnectedEdges(edge)
                    && baseEdgeEndHasAtLeastTwoConnectedEdges(edge)
                    && lengthOfWay(edge) >= this.shortEdgeThreshold.asMeters()
                    && lengthOfWay(edge) <= this.longEdgeThreshold.asMeters()
                    && edge.highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayClass)
                    && !JunctionTag.isRoundabout(edge)
                    && !JunctionTag.isCircular(edge);

        }
        return false;
    }

    @Override
    protected List<String> getFallbackInstructions() {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks if json element highway field is signal or roundabout
     * we don't want either
     */
    private boolean isNodeTrafficSignalOrRoundabout(Optional<String> tag) {
        return tag.filter(s -> s.contains("traffic_signal") || s.contains("roundabout")).isPresent();
    }

    /**
     * return length of whole way
     * @param object
     * @return
     */
    private double lengthOfWay(AtlasObject object) {
        double lengthSum = 0;
        Set<Edge> completeWay = new OsmWayWalker((Edge) object).collectEdges();
        for (Edge edge : completeWay) {
            lengthSum += edge.length().asMeters();
        }
        return lengthSum;
    }

    /**
     * in or out edge should not be a roundabout.
     */
    private boolean hasInOrOutEdgeAsRoundabout(Set<Edge> inEdges, Set<Edge> outEdges) {
        boolean hasRoundaboutTouching = false;
        for (Edge inEdge : inEdges) {
            if (JunctionTag.isRoundabout(inEdge) || JunctionTag.isCircular(inEdge)) {
                hasRoundaboutTouching = true;
            }
        }
        for (Edge outEdge : outEdges) {
            if(JunctionTag.isRoundabout(outEdge) || JunctionTag.isCircular(outEdge)) {
                hasRoundaboutTouching = true;
            }
        }
        return hasRoundaboutTouching;
    }

    /**
     * Calculates the angle between the two segments. Uses the Law of Cosines to find the angle.
     * Assumes that the segments connect at an end point.
     *
     * @return the angle between the two segments baseed on config values.
     */
    private double findAngle(final Segment segment1, final Segment segment2)
    {
        final double aLength = segment1.length().asMeters();
        final double bLength = segment2.length().asMeters();
        final double cLength = new Segment(segment1.start(), segment2.end()).length().asMeters();
        return Math.toDegrees(Math.acos(
                (pow(aLength, 2) + pow(bLength, 2) - pow(cLength, 2)) / (2 * aLength * bLength)));
    }

    /**
     * Checks if outEdge different highway tag from baseEdge
     * @param baseEdge
     * @param outEdge
     * @return
     */
    private boolean isInOrOutEdgeDiffHighwayTag(final Edge baseEdge, final Edge inEdge, final Edge outEdge) {
        return baseEdge.highwayTag() != inEdge.highwayTag() || baseEdge.highwayTag() != outEdge.highwayTag();
    }

    /**
     * merge list of sets of edges together
     * @param sets
     * @return
     */
    private Set<Edge> mergeAllEdges(List<Set<Edge>> sets) {
            Set<Edge> allEdges = new HashSet<>();
            for(Set<Edge> s : sets) {
                allEdges.addAll(s);
            }
            return allEdges;
    }

    /**
     * Checks if split road away from intersection shares other split road with 2 iterations of connected edges
     * @param connectedStartNodeEdges
     * @param connectedEndNodeEdges
     * @return
     */
    private boolean splitRoadOut(Edge baseEdge, Set<Edge> connectedStartNodeEdges, Set<Edge> connectedEndNodeEdges) {
        boolean splitRoadNotLink = false;
        Set<Long> osmIdentifiers = new HashSet<>();
        Set<Edge> allMergedEdges;
        List<Set<Edge>> connectedEdgeSets = new ArrayList<>();
        if (baseEdge.getTag("oneway").isPresent() && baseEdge.getTag("oneway").equals("yes")) {
            for (Edge connectedEdge : connectedStartNodeEdges) {
                if (connectedEdge.equals(baseEdge)) {
                    continue;
                }
                if (connectedEdge.isMasterEdge()) {
                    connectedEdgeSets.add(connectedEdge.connectedEdges());
                }
            }
            allMergedEdges = mergeAllEdges(connectedEdgeSets);
            for (Edge mergedEdge : allMergedEdges) {
                if (mergedEdge.getTag("oneway").isPresent() && mergedEdge.getTag("oneway").equals("yes")) {
                    osmIdentifiers.add(mergedEdge.getOsmIdentifier());
                }
            }
            for (Edge endNodeEdge : connectedEndNodeEdges) {
                if (endNodeEdge.equals(baseEdge)) {
                    continue;
                }
                if (osmIdentifiers.contains(endNodeEdge.getOsmIdentifier())) {
                    splitRoadNotLink = true;
                }
            }
        }
        return splitRoadNotLink;
    }

    /**
     * Checks if split road toward intersection shares other split road with 2 iterations of connected edges
     * @param connectedStartNodeEdges
     * @param connectedEndNodeEdges
     * @return
     */
    private boolean splitRoadIn(Edge baseEdge, Set<Edge> connectedStartNodeEdges, Set<Edge> connectedEndNodeEdges) {
        boolean splitRoadNotLink = false;
        if (baseEdge.getTag("oneway").isPresent() && baseEdge.getTag("oneway").equals("yes")) {
            Set<Long> osmIdentifiers = new HashSet<>();
            Set<Edge> allMergedEdges;
            List<Set<Edge>> connectedEdgeSets = new ArrayList<>();
            for (Edge connectedEdge : connectedEndNodeEdges) {
                if (connectedEdge.equals(baseEdge)) {
                    continue;
                }
                if (connectedEdge.isMasterEdge()) {
                    connectedEdgeSets.add(connectedEdge.connectedEdges().stream()
                            .filter(Edge::isMasterEdge).collect(Collectors.toSet()));
                }
            }
            allMergedEdges = mergeAllEdges(connectedEdgeSets);
            for (Edge mergedEdge : allMergedEdges) {
                if (mergedEdge.getTag("oneway").isPresent() && mergedEdge.getTag("oneway").equals("yes")) {
                    osmIdentifiers.add(mergedEdge.getOsmIdentifier());
                }
            }
            for (Edge startNodeEdge : connectedStartNodeEdges) {
                if (startNodeEdge.equals(baseEdge)) {
                    continue;
                }
                if (osmIdentifiers.contains(startNodeEdge.getOsmIdentifier())) {
                    splitRoadNotLink = true;
                }
            }
        }
        return splitRoadNotLink;
    }

    /**
     * Checks if edge is a link - we want to exclude links
     * @param edge
     * @return
     */
    private boolean isEdgeLink(final Edge edge) {
        return edge.highwayTag().isLink();
    }

    /**
     * Check if base edge has at least 2 outedges not including self
     * @param baseEdge
     * @return
     */
    private boolean baseEdgeEndHasAtLeastTwoConnectedEdges(Edge baseEdge) {
        long baseEdgeOsmId = baseEdge.getOsmIdentifier();
        Set<Edge> baseEdgeEndConnectedEdges = baseEdge.end().connectedEdges();
        baseEdgeEndConnectedEdges.removeIf(baseEdgeConnectedEdge -> baseEdgeConnectedEdge.getOsmIdentifier() == baseEdgeOsmId);
        return baseEdgeEndConnectedEdges.size() >= outEdgeMin;
    }

    /**
     * Checks if base edge has at least 2 inedges not including self
     * @param baseEdge
     * @return
     */
    private boolean baseEdgeStartHasAtLeastTwoConnectedEdges(Edge baseEdge) {
        long baseEdgeOsmId = baseEdge.getOsmIdentifier();
        Set<Edge> baseEdgeStartConnectedEdges = baseEdge.start().connectedEdges();
        baseEdgeStartConnectedEdges.removeIf(baseEdgeConnectedEdge -> baseEdgeConnectedEdge.getOsmIdentifier() == baseEdgeOsmId);
        return baseEdgeStartConnectedEdges.size() >= inEdgeMin;
    }

    /**
     * in and out edge should not share a name
     * @param inEdges
     * @param outEdges
     * @return
     */
    private boolean inAndOutEdgeShareNameOrRef(Set<Edge> inEdges, Set<Edge> outEdges) {
        boolean edgesShareName = false;
        boolean edgesShareRef = false;
        for (Edge inEdge : inEdges) {
            Optional<String> inEdgeName = inEdge.getTag("name");
            Optional<String> inEdgeRef = inEdge.getTag("ref");
            if (inEdgeName.isPresent() || inEdgeRef.isPresent()) {
                for (Edge outEdge : outEdges) {
                    Optional<String> outEdgeName = outEdge.getTag("name");
                    Optional<String> outEdgeRef = outEdge.getTag("ref");
                    if (outEdgeName.isPresent() || outEdgeRef.isPresent()) {
                        if (inEdgeName.equals(outEdgeName)) {
                            edgesShareName = true;
                        }
                        if (inEdgeRef.equals(outEdgeRef)) {
                            edgesShareRef = true;
                        }
                    }
                }
            }
        }
        return edgesShareName || edgesShareRef;
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object) {

        /**
         * Consecutive segment angle must be greater than 100-ish degrees and less than 170-ish
         * in or out edge must be different highway tag from base
         * in and out edge must not be *_link
         */

        final Edge baseEdge = (Edge) object;

        this.markAsFlagged(baseEdge.getOsmIdentifier());

        List<Segment> baseSegments = baseEdge.asPolyLine().segments();
        Segment firstBaseSegment = baseSegments.get(0);
        Segment lastBaseSegment = baseSegments.get(baseSegments.size() - 1);

        Optional<String> endHighWayField = baseEdge.end().getTag("highway");
        Optional<String> startHighWayField = baseEdge.start().getTag("highway");

        Set<Edge> connectedToBaseStartEdges = baseEdge.start().connectedEdges().stream()
                .filter(Edge::isMasterEdge).collect(Collectors.toSet());
        Set<Edge> connectedToBaseEndEdges = baseEdge.end().connectedEdges().stream()
                .filter(Edge::isMasterEdge).collect(Collectors.toSet());

        Set<Edge> inEdges = baseEdge.inEdges().stream()
                .filter(Edge::isMasterEdge).collect(Collectors.toSet());
        Set<Edge> outEdges = baseEdge.outEdges().stream()
                .filter(Edge::isMasterEdge).collect(Collectors.toSet());

        if (!hasInOrOutEdgeAsRoundabout(inEdges, outEdges) &&
                !isNodeTrafficSignalOrRoundabout(startHighWayField) &&
                !inAndOutEdgeShareNameOrRef(inEdges, outEdges) &&
                !isNodeTrafficSignalOrRoundabout(endHighWayField) &&
                !splitRoadIn(baseEdge, connectedToBaseStartEdges, connectedToBaseEndEdges)
                && !splitRoadOut(baseEdge, connectedToBaseStartEdges, connectedToBaseEndEdges)) {
            label:
            for (Edge inEdge : inEdges) {
                if (isEdgeLink(inEdge) || !inEdge.highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayClass) ||
                        baseEdge.getOsmIdentifier() == inEdge.getOsmIdentifier()) {
                    break;
                }

                for (Edge outEdge : outEdges) {
                    if (isEdgeLink(outEdge) || !outEdge.highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayClass) ||
                            baseEdge.getOsmIdentifier() == outEdge.getOsmIdentifier()) {
                        break label;
                    }

                    List<Segment> inEdgeSegments = inEdge.asPolyLine().segments();
                    Segment lastInEdgeSegment = inEdgeSegments.get(inEdgeSegments.size() - 1);
                    double angleBetweenInAndBaseSegments = findAngle(lastInEdgeSegment, firstBaseSegment);

                    List<Segment> outEdgeSegments = outEdge.asPolyLine().segments();
                    Segment firstOutEdgeSegment = outEdgeSegments.get(0);
                    double angleBetweenOutAndBaseSegments = findAngle(lastBaseSegment, firstOutEdgeSegment);

                    if (angleBetweenOutAndBaseSegments > minAngle && angleBetweenOutAndBaseSegments < maxAngle &&
                            angleBetweenInAndBaseSegments > minAngle && angleBetweenInAndBaseSegments < maxAngle &&
                            HighwayTag.isCarNavigableHighway(outEdge) &&
                            isInOrOutEdgeDiffHighwayTag(baseEdge, inEdge, outEdge)) {
                        System.out.println("base edge osm object identifier: " + baseEdge.getOsmIdentifier());
                        System.out.println("inEdge name tag: " + inEdge.getTag("name"));
                        System.out.println("outEdge name tag: " + outEdge.getTag("name"));
                        System.out.println("start node osm tags: " + baseEdge.start().getOsmTags());
                        System.out.println("end node osm tags: " + baseEdge.end().getOsmTags());
                        System.out.println("inEdge ref: " + inEdge.getTag("ref"));
                        System.out.println("outEdge ref: " + outEdge.getTag("ref"));
//                    return Optional.of(
//                            createFlag(object, this.getLocalizedInstruction(0, object.getOsmIdentifier())));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
