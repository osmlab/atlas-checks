package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.gson.JsonElement;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

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
//    private static final String EDGE_DEVIATION_INSTRUCTION = "Way {0,number,#} is crude. Please add more nodes/rearrange current nodes to more closely match the road from imagery";
//    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
//            .singletonList(EDGE_DEVIATION_INSTRUCTION);

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
//        && !isFlagged(object.getOsmIdentifier())
        if (object instanceof Edge && ((Edge) object).isMasterEdge() &&
                !isFlagged(object.getOsmIdentifier())) {
            final Edge edge = (Edge) object;
            JsonElement highWayField = edge.end().getGeoJsonProperties().get("highway");

            return !edge.highwayTag().isLink() && HighwayTag.isCarNavigableHighway(object) &&
                    !isFlagged(object.getOsmIdentifier()) &&
                    edge.highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayClass) &&
                    isNotTrafficSignalRoundabout(highWayField) &&
                    !JunctionTag.isRoundabout(edge) &&
                    !JunctionTag.isCircular(edge);

        }
        return false;
    }

    public static boolean isNotTrafficSignalRoundabout(JsonElement element) {
        if (element == null) {
            return true;
        }
        if (element.toString().contains("traffic_signal") || element.toString().equals("mini_roundabout")) {
            return false;
        }
        return true;
    }

//    @Override
//    protected List<String> getFallbackInstructions()
//    {
//        return FALLBACK_INSTRUCTIONS;
//    }

    /**
     * Calculates the angle between the two segments. Uses the Law of Cosines to find the angle.
     * Assumes that the segments connect at an end point.
     *
     * @return the angle between the two segments <= 180 degrees
     */
    private double findAngle(final Segment lastBaseEdgeSegment, final Segment firstOutEdgeSegment)
    {
        final double aLength = lastBaseEdgeSegment.length().asMeters();
        final double bLength = firstOutEdgeSegment.length().asMeters();
        final double cLength = new Segment(lastBaseEdgeSegment.start(), firstOutEdgeSegment.end()).length().asMeters();
        return Math.toDegrees(Math.acos(
                (pow(aLength, 2) + pow(bLength, 2) - pow(cLength, 2)) / (2 * aLength * bLength)));
    }

    private boolean isOutEdgeDiffHighwayTag(final Edge baseEdge, final Edge outEdge) {
        return baseEdge.highwayTag() != outEdge.highwayTag();
    }

    private boolean isOutEdgeLink(final Edge outEdge) {
        return outEdge.highwayTag().isLink();
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
        // insert algorithmic check to see whether object needs to be flagged.
        // Example of flagging an object
        // return Optional.of(this.createFlag(object, "Instruction how to fix issue or reason behind
        // flagging the object");

        /**
         * Consecutive segment angle must be greater than 90 degrees
         * touching segments must be different highway tag
         * out edge must not be *_link
         */

        final Edge baseEdge = (Edge) object;
        this.markAsFlagged(baseEdge.getOsmIdentifier());

        OsmWayWalker wayWalker = new OsmWayWalker((Edge) object);

        Set wayEdges = wayWalker.collectEdges();


        Set<Edge> outEdges = baseEdge.outEdges();
        List baseSegments = baseEdge.asPolyLine().segments();
        Segment lastBaseSegment = (Segment) baseSegments.get(baseSegments.size() - 1);

        JsonElement baseEdgeEndNodeHighwayField = baseEdge.end().getGeoJsonProperties().get("highway");


        for (Edge outEdge : outEdges) {
            List outEdgeSegments = outEdge.asPolyLine().segments();
            Segment firstOutEdgeSegment = (Segment) outEdgeSegments.get(0);
            Double angleBetweenSegments = findAngle(lastBaseSegment, firstOutEdgeSegment);
            if (angleBetweenSegments > minAngle && angleBetweenSegments < maxAngle &&
                    isOutEdgeDiffHighwayTag(baseEdge, outEdge) &&
                    outEdge.highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayClass) &&
                    !isOutEdgeLink(outEdge) &&
                    !JunctionTag.isRoundabout(outEdge) &&
                    !JunctionTag.isCircular(outEdge) &&
                    HighwayTag.isCarNavigableHighway(outEdge)) {
                System.out.println("base edge osm object identifier: " + baseEdge.getOsmIdentifier());
                System.out.println("out edge osm object identifier: " + outEdge.getOsmIdentifier());
                System.out.println("base edge end node highway: " + baseEdgeEndNodeHighwayField);
                System.out.println("base edge end node osm id: " + baseEdge.end().getOsmIdentifier());
                System.out.println("segment angles: " + angleBetweenSegments);
                System.out.println("base edge higwayTag: " + baseEdge.highwayTag());
                System.out.println("out edge higwayTag: " + outEdge.highwayTag());
                return Optional.of(
                        createFlag(object, "test highway change"));
            }
        }

        return Optional.empty();
    }
}
