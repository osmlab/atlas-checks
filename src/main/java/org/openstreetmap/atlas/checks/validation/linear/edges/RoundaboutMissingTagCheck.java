package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.ServiceTag;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Flags edges that are closed and round shaped without junction=roundabout tag and have minimum of
 * two intersections with navigable roads {@link RoundaboutMissingTagCheck#MINIMUM_INTERSECTION}
 * connections. See https://wiki.openstreetmap.org/wiki/Tag:junction%3Droundabout for more
 * information about roundabouts
 *
 * @author vladlemberg
 */

public class RoundaboutMissingTagCheck extends BaseCheck<Long>
{
    // Instructions
    public static final String MISSING_JUNCTION_TAG_INSTRUCTION = "This edge might be a roundabout";
    // Minimum intersection with Navigable Roads
    private static final int MINIMUM_INTERSECTION = 2;
    private static final int TURNING_CIRCLE_SECTIONS = 2;
    private static final long TURNING_CIRCLE_LENGTH_THRESHOLD_DEFAULT = 4;
    private static final int MODULUS = 10;
    private static final int FIRST_EDGE_SECTION = 1;
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(MISSING_JUNCTION_TAG_INSTRUCTION);
    private static final double MAX_THRESHOLD_DEGREES_DEFAULT = 40.0;
    private static final double MIN_THRESHOLD_DEGREES_DEFAULT = 10.0;
    private static final String TAG_FILTER_IGNORE_DEFAULT = "motor_vehicle->!no&foot->!yes&footway->!&access->!private&construction->!";
    private static final long serialVersionUID = 5171171744111206429L;
    private final Angle maxAngleThreshold;
    private final Angle minAngleThreshold;
    private final long turningCircleLengthThreshold;
    private final TaggableFilter tagFilterIgnore;

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public RoundaboutMissingTagCheck(final Configuration configuration)
    {
        super(configuration);
        this.maxAngleThreshold = this.configurationValue(configuration,
                "angle.threshold.maximum_degree", MAX_THRESHOLD_DEGREES_DEFAULT, Angle::degrees);
        this.minAngleThreshold = this.configurationValue(configuration,
                "angle.threshold.minimum_degree", MIN_THRESHOLD_DEGREES_DEFAULT, Angle::degrees);
        this.tagFilterIgnore = this.configurationValue(configuration, "ignore.tags.filter",
                TAG_FILTER_IGNORE_DEFAULT, TaggableFilter::forDefinition);
        this.turningCircleLengthThreshold = this.configurationValue(configuration,
                "turning.circle.length.threshold", TURNING_CIRCLE_LENGTH_THRESHOLD_DEFAULT);
    }

    /**
     * Validates if given {@link AtlasObject} is actually an {@link Edge} and is a potential
     * roundabout and also corresponding OSM identifier shouldn't be flagged before (this is for
     * avoiding duplicate flags)
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && !this.isFlagged(object.getOsmIdentifier())
                && object.getIdentifier() % MODULUS == FIRST_EDGE_SECTION
                && ((Edge) object).isMainEdge() && HighwayTag.isCarNavigableHighway(object)
                && this.tagFilterIgnore.test(object) && object.getTag(JunctionTag.KEY).isEmpty()
                && object.getTag(AreaTag.KEY).isEmpty() && this.isPartOfClosedWay((Edge) object)
                && this.intersectingWithMoreThan((Edge) object)
                && !this.isTurningCircle((Edge) object);
    }

    /**
     * Flags an {@link Edge} is its circular shape and connected to at least
     * {@link RoundaboutMissingTagCheck#MINIMUM_INTERSECTION} navigable roads. {@link Edge} doesn't
     * have junction=roundabout or area=yes tags.
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;

        final PolyLine originalGeom = this.buildOriginalOsmWayGeometry(edge);
        // check maximum angle
        final List<Tuple<Angle, Location>> maxOffendingAngles = originalGeom
                .anglesGreaterThanOrEqualTo(this.maxAngleThreshold);
        // check minimum angle
        final List<Tuple<Angle, Location>> minOffendingAngles = originalGeom
                .anglesLessThanOrEqualTo(this.minAngleThreshold);

        if (maxOffendingAngles.isEmpty() && minOffendingAngles.isEmpty())
        {
            this.markAsFlagged(object.getOsmIdentifier());
            return Optional.of(this.createFlag(new OsmWayWalker(edge).collectEdges(),
                    this.getLocalizedInstruction(0)));
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Build original OSW way geometry from all MainEdge sections
     *
     * @param edge
     *            entity to check
     * @return original Way geometry polyline
     */
    private PolyLine buildOriginalOsmWayGeometry(final Edge edge)
    {
        // Identify and sort by IDs all sections of original OSM way
        final List<Edge> sortedEdges = new ArrayList<>(new OsmWayWalker(edge).collectEdges());
        // Build original OSM polyline
        PolyLine geometry = new PolyLine(sortedEdges.get(0).getRawGeometry());
        for (int index = 1; index < sortedEdges.size(); index++)
        {
            geometry = geometry.append(sortedEdges.get(index).asPolyLine());
        }
        return geometry;
    }

    /**
     * Check if original OSM Way is intersecting with CAR_NAVIGABLE_HIGHWAYS. See {@link HighwayTag}
     *
     * @param edge
     *            entity to check
     * @return true if way intersecting with more more than
     *         {@link RoundaboutMissingTagCheck#MINIMUM_INTERSECTION}
     */
    private boolean intersectingWithMoreThan(final Edge edge)
    {
        // Identify all sections of original OSM way
        final Set<Edge> edgesFormingOSMWay = new HashSet<>(new OsmWayWalker(edge).collectEdges());
        final Set<Long> connectedEdges = new HashSet<>();
        edgesFormingOSMWay.forEach(
                connectedEdge -> connectedEdge.connectedEdges().stream().filter(Edge::isMainEdge)
                        // intersection with navigable roads
                        .filter(HighwayTag::isCarNavigableHighway)
                        .filter(obj -> obj.getTag(ServiceTag.KEY).isEmpty())
                        // de-duplication sectioned edges
                        .forEach(wayId ->
                        {
                            if (wayId.getOsmIdentifier() != edge.getOsmIdentifier())
                            {
                                connectedEdges.add(wayId.getOsmIdentifier());
                            }
                        }));

        return connectedEdges.size() >= MINIMUM_INTERSECTION;
    }

    /**
     * Check if Edge is part of Closed Way. See https://wiki.openstreetmap.org/wiki/Item:Q4669
     *
     * @param edge
     *            entity to check
     * @return true if edge is part of closed way.
     */
    private boolean isPartOfClosedWay(final Edge edge)
    {
        return edge.inEdges().stream().filter(Edge::isMainEdge)
                .filter(inEdge -> inEdge.getOsmIdentifier() == edge.getOsmIdentifier())
                .count() == 1;
    }

    /**
     * Check if original OSM Way is a Turning Circle. See
     * https://wiki.openstreetmap.org/wiki/Tag:highway%3Dturning_circle
     *
     * @param edge
     *            entity with valence equal to
     *            {@link RoundaboutMissingTagCheck#MINIMUM_INTERSECTION}
     * @return true if way is a turning circle. OSM Examples:
     *         https://www.openstreetmap.org/way/220432725
     *         https://www.openstreetmap.org/way/693834061
     *         https://www.openstreetmap.org/way/685695323
     *         https://www.openstreetmap.org/way/685704336
     */
    private boolean isTurningCircle(final Edge edge)
    {
        final List<Edge> edgesFormingOSMWay = new ArrayList<>(
                new OsmWayWalker(edge).collectEdges());
        if (edgesFormingOSMWay.size() != TURNING_CIRCLE_SECTIONS)
        {
            return false;
        }
        final Distance edge1 = edgesFormingOSMWay.get(0).asPolyLine().length();
        final Distance edge2 = edgesFormingOSMWay.get(1).asPolyLine().length();

        return (edge1.isGreaterThan(edge2))
                ? edge1.asMeters() / edge2.asMeters() > turningCircleLengthThreshold
                : edge2.asMeters() / edge1.asMeters() > turningCircleLengthThreshold;
    }
}
