package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.*;

import com.google.gson.JsonElement;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import javax.swing.text.html.Option;

import static java.lang.Math.pow;

/**
 * Auto generated Check template
 *
 * @author v-garei
 */
public class SuddenHighwayChange extends BaseCheck
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;
    private final double minAngle;
    private final double maxAngle;
    private final HighwayTag minHighwayClass;
    public static final double MIN_ANGLE_DEFAULT = 90.0;
    public static final double MAX_ANGLE_DEFAULT = 160;
    private static final String EDGE_DEVIATION_INSTRUCTION = "Way {0,number,#} is crude. Please add more nodes/rearrange current nodes to more closely match the road from imagery";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(EDGE_DEVIATION_INSTRUCTION);

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
        final String highwayType = (String) configurationValue(configuration, "minHighwayType", "tertiary");
        this.minAngle = (double) configurationValue(configuration, "angle.min", MIN_ANGLE_DEFAULT);
        this.maxAngle = (double) configurationValue(configuration, "angle.max", MAX_ANGLE_DEFAULT);
        this.minHighwayClass = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());
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
        if (object instanceof Edge && ((Edge) object).isMasterEdge() &&
                !isFlagged(object.getOsmIdentifier())) {
            final Edge edge = (Edge) object;
//            JsonElement endHighWayField = edge.end().getGeoJsonProperties().get("highway");
//            JsonElement startHighWayField = edge.start().getGeoJsonProperties().get("highway");

            return !isEdgeLink(edge) && HighwayTag.isCarNavigableHighway(object) &&
                    !isFlagged(object.getOsmIdentifier()) &&
                    hasInAndOutEdges(edge) &&
                    baseEdgeAtLeaseTwoInEdges(edge) &&
                    edge.highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayClass) &&
                    baseEdgeBetweenTwoAndFourOutEdges(edge) &&
                    !JunctionTag.isRoundabout(edge) &&
                    !JunctionTag.isCircular(edge);

        }
        return false;
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks if json element highway field is signal or roundabout
     * we don't want either
     */
    private boolean isTrafficSignalRoundabout(JsonElement element) {
//        if (element == null) {
//            return false;
//        }
        if (element != null) {
            return element.toString().contains("traffic_signal") || element.toString().equals("mini_roundabout");
        }
        return false;
    }

    private boolean hasInAndOutEdges(Edge edge) {
        Set inEdges = edge.inEdges();
        Set outEdges = edge.outEdges();

        return inEdges.size() > 0 && outEdges.size() > 0;
    }

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
        return baseEdge.outEdges().size() >= 2 && baseEdge.outEdges().size() <= 4;
    }

    /**
     * Checks if base edge has at least 2 inedges
     * @param baseEdge
     * @return
     */
    private boolean baseEdgeAtLeaseTwoInEdges(Edge baseEdge) {
        return baseEdge.start().outEdges().size() > 1;
    }

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
         * Consecutive segment angle must be greater than 90 degrees
         * touching segments must be different highway tag
         * out edge must not be *_link
         */

        final Edge baseEdge = (Edge) object;

        this.markAsFlagged(baseEdge.getOsmIdentifier());

        List baseSegments = baseEdge.asPolyLine().segments();
        Segment firstBaseSegment = (Segment) baseSegments.get(0);
        Segment lastBaseSegment = (Segment) baseSegments.get(baseSegments.size() - 1);

        JsonElement endHighWayField = baseEdge.end().getGeoJsonProperties().get("highway");
        JsonElement startHighWayField = baseEdge.start().getGeoJsonProperties().get("highway");

        Set<Edge> inEdges = baseEdge.inEdges();
        Set<Edge> outEdges = baseEdge.outEdges();

        if (!hasInOrOutEdgeAsRoundabout(inEdges, outEdges) && !isTrafficSignalRoundabout(startHighWayField) &&
                !isTrafficSignalRoundabout(endHighWayField) && !inAndOutEdgeShareName(inEdges, outEdges)) {
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

                    List inEdgeSegments = inEdge.asPolyLine().segments();
                    Segment lastInEdgeSegment = (Segment) inEdgeSegments.get(inEdgeSegments.size() - 1);
                    Double angleBetweenInAndBaseSegments = findAngle(lastInEdgeSegment, firstBaseSegment);

                    List outEdgeSegments = outEdge.asPolyLine().segments();
                    Segment firstOutEdgeSegment = (Segment) outEdgeSegments.get(0);
                    Double angleBetweenOutAndBaseSegments = findAngle(lastBaseSegment, firstOutEdgeSegment);

                    if (angleBetweenOutAndBaseSegments > minAngle && angleBetweenOutAndBaseSegments < maxAngle &&
                            angleBetweenInAndBaseSegments > minAngle && angleBetweenInAndBaseSegments < maxAngle &&
                            HighwayTag.isCarNavigableHighway(outEdge) &&
                            isInOrOutEdgeDiffHighwayTag(baseEdge, inEdge, outEdge)) {
                        System.out.println("base edge osm object identifier: " + baseEdge.getOsmIdentifier());
                        System.out.println("out edge osm object identifier: " + outEdge.getOsmIdentifier());
                        System.out.println("base edge start node: " + baseEdge.start().getOsmIdentifier());
                        System.out.println("base edge end node: " + baseEdge.end().getOsmIdentifier());
                        System.out.println("In and base angle: " + angleBetweenInAndBaseSegments);
                        System.out.println("Base and out angle: " + angleBetweenOutAndBaseSegments);
                        System.out.println("base edge higwayTag: " + baseEdge.highwayTag());
                        System.out.println("in edge higwayTag: " + inEdge.highwayTag());
                        System.out.println("out edge higwayTag: " + outEdge.highwayTag());
//                    return Optional.of(
//                            createFlag(object, "test highway change"));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
