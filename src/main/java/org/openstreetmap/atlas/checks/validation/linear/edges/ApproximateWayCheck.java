package org.openstreetmap.atlas.checks.validation.linear.edges;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check flags edges that deviate from the assumed curve of a road by at least
 * {@value DEVIATION_MINIMUM_LENGTH_DEFAULT} meters.
 *
 * @author v-brjor
 */
public class ApproximateWayCheck extends BaseCheck<Long>
{

    private static final long serialVersionUID = 125449616392217396L;
    private static final String EDGE_DEVIATION_INSTRUCTION = "Way {0,number,#} is crude. Please add more nodes/rearrange current nodes to more closely match the road from imagery";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(EDGE_DEVIATION_INSTRUCTION);
    public static final double DEVIATION_MAXIMUM_RATIO_DEFAULT = 0.04;
    public static final double DEVIATION_MINIMUM_LENGTH_DEFAULT = 10;
    private static final String HIGHWAY_MINIMUM_DEFAULT = HighwayTag.SERVICE.toString();
    public static final double MIN_ANGLE_DEFAULT = 60.0;
    public static final double MAX_ANGLE_DEFAULT = 160.0;
    public static final double BEZIER_STEP_DEFAULT = 0.01;

    private final double maxDeviationRatio;
    private final Distance minDeviationLength;
    private final HighwayTag highwayMinimum;
    private final double minAngle;
    private final double maxAngle;
    private final double bezierStep;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public ApproximateWayCheck(final Configuration configuration)
    {
        super(configuration);
        this.maxDeviationRatio = configurationValue(configuration, "deviation.ratio.max",
                DEVIATION_MAXIMUM_RATIO_DEFAULT, Double::doubleValue);
        this.minDeviationLength = configurationValue(configuration, "deviation.minimum.meters",
                DEVIATION_MINIMUM_LENGTH_DEFAULT, Distance::meters);
        final String highwayType = this.configurationValue(configuration, "highway.minimum",
                HIGHWAY_MINIMUM_DEFAULT);
        this.highwayMinimum = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());
        this.minAngle = configurationValue(configuration, "angle.minimum", MIN_ANGLE_DEFAULT);
        this.maxAngle = configurationValue(configuration, "angle.max", MAX_ANGLE_DEFAULT);
        this.bezierStep = configurationValue(configuration, "bezierStep", BEZIER_STEP_DEFAULT);
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
        return TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMasterEdge()
                && HighwayTag.isCarNavigableHighway(object) && isMinimumHighwayType(object);
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged. A
     * majority of flagged edges were those that contained correctly mapped ~90 degree angles, we
     * also don't want to worry about sharp angles as those are flagged in {@link SharpAngleCheck}
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final List<Segment> segments = ((Edge) object).asPolyLine().segments();

        if (segments.size() < 2)
        {
            return Optional.empty();
        }

        final boolean isCrude = IntStream.range(0, segments.size() - 1).anyMatch(index ->
        {
            final Segment seg1 = segments.get(index);
            final Segment seg2 = segments.get(index + 1);
            final double angle = findAngle(seg1, seg2);
            // ignore sharp turns and almost straightaways
            if (angle < minAngle || angle > maxAngle)
            {
                return false;
            }
            final double distance = quadraticBezier(seg1.first(), seg2.first(), seg2.end());
            final double legsLength = seg1.length().asMeters() + seg2.length().asMeters();
            return distance > this.minDeviationLength.asMeters()
                    && distance / legsLength > maxDeviationRatio;
        });

        if (isCrude)
        {
            return Optional.of(
                    createFlag(object, this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Finds the distance between two points
     */
    private double distance(final double startX, final double startY, final double endX,
            final double endY)
    {
        return sqrt(pow(endX - startX, 2) + pow(endY - startY, 2));
    }

    /**
     * Calculates the angle between the two segments. Uses the Law of Cosines to find the angle.
     * Assumes that the segments connect at an end point.
     * 
     * @return the angle between the two segments <= 180 degrees
     */
    private double findAngle(final Segment seg1, final Segment seg2)
    {
        final double aLength = seg1.length().asMeters();
        final double bLength = seg2.length().asMeters();
        final double cLength = new Segment(seg1.start(), seg2.end()).length().asMeters();
        return Math.toDegrees(Math.acos(
                (pow(aLength, 2) + pow(bLength, 2) - pow(cLength, 2)) / (2 * aLength * bLength)));
    }

    /**
     * Checks if highway tag of given {@link AtlasObject} is of greater or equal priority than the
     * minimum highway type given in the configurable. If no value is given in configurable, the
     * default highway type of "SERVICE" will be set as minimum.
     *
     * @param object
     *            an {@link AtlasObject}
     * @return {@code true} if the highway tag of this object is greater than or equal to the
     *         minimum type
     */
    private boolean isMinimumHighwayType(final AtlasObject object)
    {
        final Optional<HighwayTag> highwayTagOfObject = HighwayTag.highwayTag(object);
        return highwayTagOfObject.isPresent()
                && highwayTagOfObject.get().isMoreImportantThanOrEqualTo(this.highwayMinimum);
    }

    /**
     * Constructs a quadratic bezier curve. This give us a way that is a smooth curve that
     * approximates the real way. We then find the closest distance of the curve to the anchor and
     * return that value. https://en.wikipedia.org/wiki/B%C3%A9zier_curve#Quadratic_curves
     * 
     * @param start
     *            start point of bezier curve
     * @param anchor
     *            anchor for the curve
     * @param end
     *            end point of bezier curve
     * @return distance in meters from closest point on bezier curve
     */
    private double quadraticBezier(final Location start, final Location anchor, final Location end)
    {
        final double startX = start.getLongitude().onEarth().asMeters();
        final double startY = start.getLatitude().onEarth().asMeters();
        final double anchorX = anchor.getLongitude().onEarth().asMeters();
        final double anchorY = anchor.getLatitude().onEarth().asMeters();
        final double endX = end.getLongitude().onEarth().asMeters();
        final double endY = end.getLatitude().onEarth().asMeters();

        double min = Double.POSITIVE_INFINITY;
        for (double step = 0; step <= 1; step += bezierStep)
        {
            // https://stackoverflow.com/questions/5634460/quadratic-b%C3%A9zier-curve-calculate-points
            final double pointX = (pow(1 - step, 2) * startX)
                    + (2 * step * (1 - step) * anchorX + pow(step, 2) * endX);
            final double pointY = (pow(1 - step, 2) * startY)
                    + (2 * step * (1 - step) * anchorY + pow(step, 2) * endY);
            // distance from point on bezier curve to anchor
            final double distance = distance(pointX, pointY, anchorX, anchorY);
            if (distance < min)
            {
                min = distance;
            }
        }
        return min;
    }
}
