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
    private static final String EDGE_DEVIATION_INSTRUCTION = "Way {0,number,#} is crude. Please add more nodes/rearrange current nodes to more closely match the road from imagery";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(EDGE_DEVIATION_INSTRUCTION);
    private final double minAngle;
    private final double maxAngle;
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
        if (object instanceof Edge && !isFlagged(object.getOsmIdentifier()) &&
                ((Edge) object).isMasterEdge()) {
            final Edge edge = (Edge) object;

            return !isEdgeLink(edge) &&
                    HighwayTag.isCarNavigableHighway(object) &&
                    baseEdgeAtLeaseTwoInEdges(edge) &&
                    lengthOfWay(edge) >= this.shortEdgeThreshold.asMeters() &&
                    lengthOfWay(edge) <= this.longEdgeThreshold.asMeters() &&
                    edge.highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayClass) &&
                    baseEdgeBetweenTwoAndFourOutEdges(edge) &&
                    !JunctionTag.isRoundabout(edge) &&
                    !JunctionTag.isCircular(edge);

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
    private boolean isTrafficSignalRoundabout(JsonElement element) {
        if (element != null) {
            return element.toString().contains("traffic_signal") || element.toString().contains("roundabout");
        }
        return false;
    }

    /**
     * return length of whole way
     * @param object
     * @return
     */
//    Edge e = (Edge) object;
//    OsmWayWalker walker = new OsmWayWalker(e);
//    Optional<Double> d = walker.collectEdges().stream()
//            .map(edge -> edge.length().asMeters())
//            .reduce((len, n) -> len += n);
    
    private double lengthOfWay(AtlasObject object) {
        double lengthSum = 0;
        Set<Edge> completeWay = new OsmWayWalker((Edge) object).collectEdges();
        for (Edge edge : completeWay) {
            List<Segment> edgeSegments = edge.asPolyLine().segments();
            for (Segment segment : edgeSegments) {
                lengthSum += segment.length().asMeters();
            }
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
        Set<JsonElement> allMergedEdgeNames = new HashSet<>();
        List<Set<Edge>> connectedEdgeSets = new ArrayList<>();
        for (Edge connectedEdge : connectedStartNodeEdges) {
//            System.out.println("connectedEdge.equals(baseEdge): " + connectedEdge.equals(baseEdge));
//            System.out.println("connectedEdge == baseEdge): " + (connectedEdge == baseEdge));
            if (connectedEdge.equals(baseEdge)) {
                continue;
            }
            if (connectedEdge.isMasterEdge()) {
                connectedEdgeSets.add(connectedEdge.connectedEdges());
            }
        }
        allMergedEdges = mergeAllEdges(connectedEdgeSets);

        for (Edge mergedEdge : allMergedEdges) {
            osmIdentifiers.add(mergedEdge.getOsmIdentifier());

        }

//        System.out.println("allmergedEdgesOsmIds:" + osmIdentifiers);

        for (Edge endNodeEdge : connectedEndNodeEdges) {
//            System.out.println("endNodeEdgeOsmID: " + endNodeEdge.getOsmIdentifier());
            if (endNodeEdge.equals(baseEdge)) {
                continue;
            }
            if (osmIdentifiers.contains(endNodeEdge.getOsmIdentifier())) {
                splitRoadNotLink = true;
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
        Set<Long> osmIdentifiers = new HashSet<>();
        Set<JsonElement> allMergedEdgeNames = new HashSet<>();
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
            osmIdentifiers.add(mergedEdge.getOsmIdentifier());
        }
//        System.out.println("allMergedOsmIdentifiers: " + osmIdentifiers);

        for (Edge startNodeEdge : connectedStartNodeEdges) {
//            System.out.println("startNodeEdgeOsmID: " + startNodeEdge.getOsmIdentifier());
            if (startNodeEdge.equals(baseEdge)) {
                continue;
            }
            if (osmIdentifiers.contains(startNodeEdge.getOsmIdentifier())) {
                splitRoadNotLink = true;
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
     * Check if base edge has between 2 and 4 outedges
     * @param baseEdge
     * @return
     */
    private boolean baseEdgeBetweenTwoAndFourOutEdges(Edge baseEdge) {
        return baseEdge.outEdges().size() >= 2 && baseEdge.outEdges().size() <=3;
    }

    /**
     * Checks if base edge has at least 2 inedges
     * @param baseEdge
     * @return
     */
    private boolean baseEdgeAtLeaseTwoInEdges(Edge baseEdge) {
        return baseEdge.inEdges().size() > 1;
    }

    /**
     * in and out edge should not sahre a name
     * @param inEdges
     * @param outEdges
     * @return
     */
    private boolean inAndOutEdgeShareName(Set<Edge> inEdges, Set<Edge> outEdges) {
        boolean edgesShareName = false;
        for (Edge inEdge : inEdges) {
            JsonElement inEdgeName = inEdge.getGeoJsonProperties().get("name");
            if (inEdgeName != null) {
                for (Edge outEdge : outEdges) {
                    JsonElement outEdgeName = outEdge.getGeoJsonProperties().get("name");
                    if (outEdgeName != null) {
                        if (inEdgeName.equals(outEdgeName)) {
                            edgesShareName = true;
                        }
                    }
                }
            }
        }
        return edgesShareName;
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
//        long baseEdgeOsmId = baseEdge.getOsmIdentifier();
        JsonElement endHighWayField = baseEdge.end().getGeoJsonProperties().get("highway");
        JsonElement startHighWayField = baseEdge.start().getGeoJsonProperties().get("highway");

        Set<Edge> connectedToBaseStartEdges = baseEdge.start().connectedEdges().stream()
                .filter(Edge::isMasterEdge).collect(Collectors.toSet());
        Set<Edge> connectedToBaseEndEdges = baseEdge.end().connectedEdges().stream()
                .filter(Edge::isMasterEdge).collect(Collectors.toSet());

        Set<Edge> inEdges = baseEdge.inEdges().stream()
                .filter(Edge::isMasterEdge).collect(Collectors.toSet());
        Set<Edge> outEdges = baseEdge.outEdges().stream()
                .filter(Edge::isMasterEdge).collect(Collectors.toSet());

        if (!hasInOrOutEdgeAsRoundabout(inEdges, outEdges) &&
                !isTrafficSignalRoundabout(startHighWayField) &&
                !inAndOutEdgeShareName(inEdges, outEdges) &&
                !isTrafficSignalRoundabout(endHighWayField) &&
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
                        //345406086 way for checking split edge rule.
                        //32806120
                        System.out.println("base edge osm object identifier: " + baseEdge.getOsmIdentifier());
                        System.out.println("out edge osm object identifier: " + outEdge.getOsmIdentifier());
//                        splitRoadOut(baseEdge, connectedToBaseStartEdges, connectedToBaseEndEdges);
//                        splitRoadIn(baseEdge, connectedToBaseStartEdges, connectedToBaseEndEdges);
//                        System.out.println("length of flagged edge: " + lengthOfWay(baseEdge));
//                        System.out.println("base edge start node: " + baseEdge.start().getOsmIdentifier());
//                        System.out.println("base edge end node: " + baseEdge.end().getOsmIdentifier());
//                        System.out.println("In and base angle: " + angleBetweenInAndBaseSegments);
//                        System.out.println("Base and out angle: " + angleBetweenOutAndBaseSegments);
//                        System.out.println("base edge higwayTag: " + baseEdge.highwayTag());
//                        System.out.println("in edge higwayTag: " + inEdge.highwayTag());
//                        System.out.println("out edge higwayTag: " + outEdge.highwayTag());
//                    return Optional.of(
//                            createFlag(object, "test highway change"));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
