package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.AerowayTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * This check flags edges that deviate from the assumed curve of a road by at least
 * {@value DEVIATION_MINIMUM_METERS_DEFAULT} meters.
 *
 * @author v-brjor
 */
public class ApproximateWayCheck extends BaseCheck<Long>
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;
    private static final String EDGE_DEVIATION_INSTRUCTION = "Way {0,number,#} deviates by {1,number,#} meters";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(EDGE_DEVIATION_INSTRUCTION);
    public static final double DEVIATION_MINIMUM_METERS_DEFAULT = 35.0;
    private static final String HIGHWAY_MINIMUM_DEFAULT = HighwayTag.SERVICE.toString();
    public static final double MINIMUM_ANGLE_DEFAULT = 100.0;
    public static final double BEZIER_STEP_DEFAULT = 0.01;

    private final Distance minimumDeviation;
    private final HighwayTag highwayMinimum;
    private final double minimumAngle;
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
        this.minimumDeviation = configurationValue(configuration, "deviation.minimum.meters",
                DEVIATION_MINIMUM_METERS_DEFAULT, Distance::meters);
        final String highwayType = this.configurationValue(configuration, "highway.minimum",
            HIGHWAY_MINIMUM_DEFAULT);
        this.highwayMinimum = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());
        this.minimumAngle = configurationValue(configuration, "angle.minimum",
            MINIMUM_ANGLE_DEFAULT);
        this.bezierStep = configurationValue(configuration, "bezierStep",
            BEZIER_STEP_DEFAULT);
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
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * A majority of flagged edges were those that contained correctly mapped ~90 degree angles,
     * we also don't want to worry about sharp angles as those are flagged in {@link SharpAngleCheck}
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        List<Segment> segments = ((Edge) object).asPolyLine().segments();

        if (segments.size() < 2) {
            return Optional.empty();
        }

        OptionalDouble max = IntStream.range(1, segments.size() - 1)
                .mapToDouble(index -> {
                    Segment s1 = segments.get(index);
                    Segment s2 = segments.get(index + 1);
                    if (findAngle(s1 , s2) < minimumAngle) {
                        return 0;
                    }
                    return quadraticBezier(
                        s1.first(),
                        s2.first(), // could also be s1.end()
                        s2.end()
                    );
                })
                .reduce(Math::max);

        if (max.isPresent() && max.getAsDouble() > minimumDeviation.asMeters()) {
            return Optional.of(
                createFlag(
                    object,
                    this.getLocalizedInstruction(
                        0,
                        object.getOsmIdentifier(),
                        max.getAsDouble()
                    )
                )
            );
        }

        return Optional.empty();
    }

    /**
     * Calculates the angle between the two segments.
     * This assumes that s1 and s2 are connected at one end in the same location (s1.end == s2.start)
     */
    private double findAngle(Segment s1, Segment s2) {
        double a = s1.length().asMeters();
        double b = s2.length().asMeters();
        double c = new Segment(s1.start(), s2.end()).length().asMeters();
        return Math.toDegrees(Math.acos((pow(a,2) + pow(b, 2) - pow(c, 2))/(2*a*b)));
    }

    /**
     * Constructs a quadratic bezier curve and finds the closest distance of the curve to the anchor.
     * @param start start point of bezier curve
     * @param anchor anchor for the curve
     * @param end end point of bezier curve
     * @return distance in meters from closest point on bezier curve
     */
    private double quadraticBezier(Location start, Location anchor, Location end) {
        double x0 = start.getLongitude().onEarth().asMeters();
        double y0 = start.getLatitude().onEarth().asMeters();
        double x1 = anchor.getLongitude().onEarth().asMeters();
        double y1 = anchor.getLatitude().onEarth().asMeters();
        double x2 = end.getLongitude().onEarth().asMeters();
        double y2 = end.getLatitude().onEarth().asMeters();

        double min = Double.POSITIVE_INFINITY;
        for (double i = 0; i <= 1; i += bezierStep) {
            double x = (pow(1 - i, 2) * x0) + (2 * i * (1 - i) * x1 + pow(i, 2) * x2);
            double y = (pow(1 - i, 2) * y0) + (2 * i * (1 - i) * y1 + pow(i, 2) * y2);
            // distance from point on bezier curve to anchor
            double d = distance(x, y, x1, y1);
            if (d < min) {
                min = d;
            }
        }
        return min;
    }

    private double distance(double x0, double y0, double x1, double y1) {
        return sqrt(pow(x1 - x0, 2) + pow(y1 - y0, 2));
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

    @Override
    protected List<String> getFallbackInstructions() { return FALLBACK_INSTRUCTIONS; }
}
