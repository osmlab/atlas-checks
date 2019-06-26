package org.openstreetmap.atlas.checks.validation.linear.lines;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags generalized coastlines-- that is, OSM ways with the tag natural=coastline where x% or more
 * node pairs are y or more meters apart. X and y can be specified in a {@link Configuration}.
 * Coastlines can be represented in Atlas by {@link LineItem}s and may be members of
 * {@link Relation}s.
 *
 * @author seancoulter
 */
public class GeneralizedCoastlineCheck extends BaseCheck<Long>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "This coastline is generalized, as {0}% of node pairs are {1} or more meters apart. To fix, increase the number of nodes along this coastline. (recommended: place a couple more nodes near the dotted portion of the line)");
    private static final long MINIMUM_DISTANCE_BETWEEN_NODES = 100;
    private static final double MINIMUM_NODE_PAIR_THRESHOLD_PERCENTAGE = 30.0;

    private static final double HUNDRED_PERCENT = 100.0;
    private final double percentageThreshold;
    private final long minimumDistanceBetweenNodes;

    public GeneralizedCoastlineCheck(final Configuration configuration)
    {
        super(configuration);
        this.percentageThreshold = this.configurationValue(configuration, "node.minimum.threshold",
                MINIMUM_NODE_PAIR_THRESHOLD_PERCENTAGE);
        this.minimumDistanceBetweenNodes = this.configurationValue(configuration,
                "node.minimum.distance", MINIMUM_DISTANCE_BETWEEN_NODES);
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
        return object instanceof LineItem
                && (Validators.isOfType(object, NaturalTag.class, NaturalTag.COASTLINE)
                        || ((LineItem) object).relations().stream().anyMatch(relation -> Validators
                                .isOfType(relation, NaturalTag.class, NaturalTag.COASTLINE)));
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
        final List<Location> pointsForFlagging = this.getPointsForFlagging((LineItem) object);
        return generalizedSegments >= this.percentageThreshold
                ? Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(0, generalizedSegments,
                                this.minimumDistanceBetweenNodes),
                        pointsForFlagging))
                : Optional.empty();
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
        final double innerCount = segments.stream()
                .filter(segment -> segment.length().asMeters() >= this.minimumDistanceBetweenNodes)
                .count();
        return segments.size() == 0 ? 0.0 : HUNDRED_PERCENT * (innerCount / segments.size());
    }

    /**
     * @param line
     *            The LineItem whose midpoints are flagged
     * @return A {@link List} of {@link Location}s, each element of the list being the midpoint of
     *         generalized segments of the parameter coastline
     */
    private List<Location> getPointsForFlagging(final LineItem line)
    {
        return line.asPolyLine().segments().stream()
                .filter(segment -> segment.length().asMeters() >= MINIMUM_DISTANCE_BETWEEN_NODES)
                .map(segment -> segment.start().midPoint(segment.end()))
                .collect(Collectors.toList());
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

}
