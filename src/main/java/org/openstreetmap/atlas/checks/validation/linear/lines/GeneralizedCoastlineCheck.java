package org.openstreetmap.atlas.checks.validation.linear.lines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Flags generalized coastlines-- that is, OSM ways with the tag natural=coastline where x% or more
 * node pairs are y or more meters apart. X and y can be specified in a {@link Configuration}.
 * Additionally flags sharp angle locations according to the angle.minimum.threshold
 * {@link Configuration} value if it's greater than -1. Coastlines can be represented in Atlas by
 * {@link LineItem}s and may be members of {@link Relation}s.
 *
 * @author seancoulter, a-molis
 */
public class GeneralizedCoastlineCheck extends BaseCheck<Long>
{
    private static final String BASIC_INSTRUCTIONS = "This coastline is generalized, as {0}% of node pairs are {1} or more apart. To fix, add more nodes to this coastline. The midpoints of generalized segments are dotted for convenience.";
    private static final String SHARP_ANGLE_INSTRUCTIONS = "This coastline is generalized, as {0}% of node pairs are {1} or more apart. There are also sharp angles exceeding {2} degrees. To fix, add more nodes to smooth angles and break up long segments of coastline. Suggested areas to add nodes are dotted.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(BASIC_INSTRUCTIONS,
            SHARP_ANGLE_INSTRUCTIONS);
    private static final double MINIMUM_DISTANCE_BETWEEN_NODES = 100;
    private static final double MINIMUM_NODE_PAIR_THRESHOLD_PERCENTAGE = 30.0;
    private static final String COASTLINE_TAG_FILTER_DEFAULT = "source->PGS";
    private static final double SHARP_ANGLE_THRESHOLD_DEFAULT = Integer.MAX_VALUE;

    private static final double HUNDRED_PERCENT = 100.0;
    private static final long serialVersionUID = 1576217971819771231L;
    private final double percentageThreshold;
    private final Distance minimumDistanceBetweenNodes;
    private final TaggableFilter coastlineTagFilter;
    private final Angle sharpAngleThreshold;
    private final double sharpAngleDegrees;

    public GeneralizedCoastlineCheck(final Configuration configuration)
    {
        super(configuration);
        this.percentageThreshold = this.configurationValue(configuration, "node.minimum.threshold",
                MINIMUM_NODE_PAIR_THRESHOLD_PERCENTAGE);
        this.minimumDistanceBetweenNodes = this.configurationValue(configuration,
                "node.minimum.distance", MINIMUM_DISTANCE_BETWEEN_NODES, Distance::meters);
        this.coastlineTagFilter = this.configurationValue(configuration, "coastline.tags.filters",
                COASTLINE_TAG_FILTER_DEFAULT, TaggableFilter::forDefinition);
        // If the below is not set in the configuration, the sharp angle logic in this check will be
        // disregarded
        this.sharpAngleDegrees = this.configurationValue(configuration, "angle.minimum.threshold",
                SHARP_ANGLE_THRESHOLD_DEFAULT);
        this.sharpAngleThreshold = Angle.degrees(this.sharpAngleDegrees);
    }

    /**
     * This method validates or invalidates the supplied atlas object for the check
     *
     * @param object
     *            The {@link AtlasObject} being checked
     * @return True if the {@link AtlasObject} is a {@link LineItem} with the tag natural=coastline,
     *         or has a parent relation with that tag; false otherwise
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        final Predicate<Relation> memberIsCoastline = this::isCoastline;
        final Predicate<Relation> memberIsSourcePGS = this.coastlineTagFilter::test;

        return object instanceof LineItem
                && (isCoastline(object) && this.coastlineTagFilter.test(object)
                        || hasRelationMembers(object, memberIsCoastline)
                                && hasRelationMembers(object, memberIsSourcePGS));
    }

    /**
     * This method flags appropriate atlas objects
     *
     * @param object
     *            The {@link AtlasObject} being analyzed for potential flagging
     * @return A flag on the parameter {@link AtlasObject} if it's a generalized coastline; an empty
     *         Optional otherwise
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final double generalizedSegments = this.getGeneralizedSegmentPercentage((LineItem) object);
        if (generalizedSegments >= this.percentageThreshold)
        {
            // Contains midpoints and sharp angle locations
            final List<Location> pointsForFlagging = this.getPointsForFlagging((LineItem) object);
            final List<Location> sharpAngles = this.getSharpAngleLocations((LineItem) object);
            // If there were sharp angles
            if (!sharpAngles.isEmpty() && this.sharpAngleDegrees != SHARP_ANGLE_THRESHOLD_DEFAULT)
            {
                pointsForFlagging.addAll(sharpAngles);
                return Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(1, generalizedSegments,
                                this.minimumDistanceBetweenNodes,
                                this.sharpAngleThreshold.asDegrees()),
                        pointsForFlagging));
            }
            // No sharp angles
            return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0,
                    generalizedSegments, this.minimumDistanceBetweenNodes), pointsForFlagging));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * This method calculates the percentage of {@link Segment}s in the {@link LineItem} whose
     * lengths are greater than or equal to the minimumDistanceBetweenNodes configuration value
     *
     * @param line
     *            The LineItem whose segments are evaluated
     * @return The percentage of generalized {@link Segment}s in the parameter {@link LineItem},
     *         0-100 inclusive.
     */
    private double getGeneralizedSegmentPercentage(final LineItem line)
    {
        final List<Segment> segments = line.asPolyLine().segments();
        final double innerCount = segments.stream().filter(segment -> segment.length()
                .isGreaterThanOrEqualTo(this.minimumDistanceBetweenNodes)).count();
        return segments.isEmpty() ? 0.0 : HUNDRED_PERCENT * (innerCount / segments.size());
    }

    /**
     * Retrieve a list of {@link Location}s along a coastline, which can be used to indicate where
     * editing should happen.
     *
     * @param line
     *            The LineItem whose midpoints are flagged
     * @return A {@link List} of {@link Location}s, each element of the list being the midpoint of
     *         generalized segments of the parameter coastline
     */
    private List<Location> getPointsForFlagging(final LineItem line)
    {
        return line.asPolyLine().segments().stream()
                .filter(segment -> segment.length().asMeters() >= this.minimumDistanceBetweenNodes
                        .asMeters())
                .map(segment -> segment.start().midPoint(segment.end()))
                .collect(Collectors.toList());
    }
    
     /**
     * Given a coastline {@link LineItem}, extract the {@link Location}s of offending {@link Angle}s
     *
     * @param coast
     *            The {@link LineItem} from which offending angle locations are extracted
     * @return A List of {@link Location}s of offending angles if any; else an empty list
     */
    private List<Location> getSharpAngleLocations(final LineItem coast)
    {
        final List<Tuple<Angle, Location>> offendingAngles = coast.asPolyLine()
                .anglesGreaterThanOrEqualTo(this.sharpAngleThreshold);
        if (!offendingAngles.isEmpty())
        {
            final List<Location> resultList = new ArrayList<>();
            offendingAngles.forEach(tuple -> resultList.add(tuple.getSecond()));
            return resultList;
        }
        return Collections.emptyList();
    }    

    /**
     * This method checks if a {@link AtlasObject} has relation members and satisfies the predicate.
     *
     * @param object
     *            The {@link AtlasObject} being checked.
     * @param relationPredicate
     *            Predicate used to filter the relation.
     * @return true if the {@link AtlasObject} has relation members and satisfies the predicate.
     */
    private boolean hasRelationMembers(final AtlasObject object,
            final Predicate<Relation> relationPredicate)
    {
        return ((LineItem) object).relations().stream().anyMatch(relationPredicate);
    }

    /**
     * This method checks if the AtlasObject has the tag natural=coastline.
     *
     * @param object
     *            The {@link AtlasObject} being checked
     * @return true if the {@link AtlasObject} has the tag natural=coastline.
     */
    private boolean isCoastline(final AtlasObject object)
    {
        return Validators.isOfType(object, NaturalTag.class, NaturalTag.COASTLINE);
    }

}
