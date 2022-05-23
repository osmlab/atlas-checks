package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.checks.atlas.predicates.TagPredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.AerowayTag;
import org.openstreetmap.atlas.tags.AmenityTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.MotorVehicleTag;
import org.openstreetmap.atlas.tags.MotorcarTag;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.VehicleTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags islands of roads where it is impossible to get out. The simplest is a one-way
 * that dead-ends; that would be a one-edge island.
 *
 * @author matthieun
 * @author cuthbertm
 * @author gpogulsky
 * @author savannahostrowski
 * @author nachtm
 * @author sayas01
 * @author seancoulter
 * @author bbreithaupt
 */
public class SinkIslandCheck extends BaseCheck<Long>
{
    private static final AmenityTag[] AMENITY_VALUES_TO_EXCLUDE = { AmenityTag.PARKING,
            AmenityTag.PARKING_SPACE, AmenityTag.MOTORCYCLE_PARKING, AmenityTag.PARKING_ENTRANCE };
    private static final String DEFAULT_MINIMUM_HIGHWAY_TYPE = "SERVICE";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "This network does not allow vehicles to navigate out of it. Check for missing connections, and over restrictive access tags.");
    private static final float LOAD_FACTOR = 0.8f;
    private static final Predicate<AtlasObject> SERVICE_ROAD = object -> Validators.isOfType(object,
            HighwayTag.class, HighwayTag.SERVICE);
    private static final Predicate<AtlasObject> IS_AT_LEAST_SERVICE_ROAD = object -> ((Edge) object)
            .highwayTag().isMoreImportantThanOrEqualTo(HighwayTag.SERVICE);
    private static final long TREE_SIZE_DEFAULT = 50;
    private static final boolean DEFAULT_SERVICE_IN_PEDESTRIAN_FILTER = false;
    private static final long serialVersionUID = -1432150496331502258L;
    private final HighwayTag minimumHighwayType;
    private final int storeSize;
    private final int treeSize;
    // This can be turned on if we want to flag service roads surrounded by pedestrian networks.
    private final boolean serviceInPedestrianNetworkFilter;

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public SinkIslandCheck(final Configuration configuration)
    {
        super(configuration);
        this.treeSize = configurationValue(configuration, "tree.size", TREE_SIZE_DEFAULT,
                Math::toIntExact);
        this.minimumHighwayType = configurationValue(configuration, "minimum.highway.type",
                DEFAULT_MINIMUM_HIGHWAY_TYPE, string -> HighwayTag.valueOf(string.toUpperCase()));
        // LOAD_FACTOR 0.8 gives us default initial capacity 50 / 0.8 = 62.5
        // map & queue will allocate 64 (the nearest power of 2) for that initial capacity
        // Our algorithm does not allow neither explored set nor candidates queue exceed
        // this.treeSize
        // Therefore underlying map/queue we will never re-double the capacity
        this.storeSize = (int) (this.treeSize / LOAD_FACTOR);
        this.serviceInPedestrianNetworkFilter = configurationValue(configuration,
                "filter.pedestrian.network", DEFAULT_SERVICE_IN_PEDESTRIAN_FILTER);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return this.validEdge(object) && !this.isFlagged(object.getIdentifier())
                && ((Edge) object).highwayTag()
                        .isMoreImportantThanOrEqualTo(this.minimumHighwayType)
                && !RouteTag.isFerry(object)
                && !(SERVICE_ROAD.test(object)
                        && (this.isWithinAreasWithExcludedAmenityTags((Edge) object)
                                || this.intersectsAirportOrBuilding((Edge) object)));
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Flag to keep track of whether we found an issue or not
        boolean haltedSearch = false;

        // The current edge to be explored
        Edge candidate = (Edge) object;

        // A set of all edges that we have already explored
        final Set<AtlasObject> explored = new HashSet<>(this.storeSize, LOAD_FACTOR);
        // A set of all sink edges
        final Set<AtlasObject> terminal = new HashSet<>();
        // Current queue of candidates that we can draw from
        final Queue<Edge> candidates = new ArrayDeque<>(this.storeSize);

        // Start edge always explored
        explored.add(candidate);

        // Keep looping while we still have a valid candidate to explore
        while (candidate != null)
        {
            // If this edge has certain characteristics, we can be sure that we don't want to
            // flag it.
            if (this.edgeCharacteristicsToIgnore(candidate))
            {
                haltedSearch = true;
                explored.add(candidate);
                break;
            }

            // Retrieve all the valid outgoing edges to explore
            final List<Edge> outEdges = candidate.outEdges().stream().filter(this::validEdge)
                    .distinct().sorted().collect(Collectors.toList());

            // Validate highway=pedestrian edges connected to candidate if candidate is
            // motor_vehicle=yes (add to outEdges)
            if (candidate.getTag(MotorVehicleTag.KEY).orElse(MotorVehicleTag.NO.name())
                    .equals(MotorVehicleTag.YES.name()))
            {
                outEdges.addAll(candidate.outEdges().stream()
                        .filter(HighwayTag::isPedestrianNavigableHighway).distinct().sorted()
                        .collect(Collectors.toList()));
            }

            if (outEdges.isEmpty())
            {
                // Sink edge. Don't mark the edge explored until we know how big the tree is
                terminal.add(candidate);
            }

            else
            {
                // Add the current candidate to the set of already explored edges
                explored.add(candidate);

                // From the list of outgoing edges from the current candidate filter out any
                // highway=pedestrian edges that were picked up and filter out any edges that have
                // already been explored and add all the rest to the queue of possible candidates
                outEdges.stream().filter(this::validEdge)
                        .filter(outEdge -> !explored.contains(outEdge)).forEach(candidates::add);

                // If the number of available candidates and the size of the currently explored
                // items is larger then the configurable tree size, then we can break out of the
                // loop and assume that this is not a SinkIsland
                if (candidates.size() + explored.size() > this.treeSize)
                {
                    haltedSearch = true;
                    break;
                }
            }

            // Get the next candidate
            candidate = candidates.poll();
        }

        // Unify all explored edges and mark them so we don't process them more than once
        explored.addAll(terminal);
        explored.forEach(marked -> this.markAsFlagged(marked.getIdentifier()));

        if (!haltedSearch)
        {
            // Include all touched edges
            return Optional.of(createFlag(explored, this.getLocalizedInstruction(0)));
        }
        else if (!terminal.isEmpty())
        {
            // Include only edges explicitly marked as sink islands during processing
            return Optional.of(createFlag(terminal, this.getLocalizedInstruction(0)));
        }
        // No encountered sink edges, and a stop criteria was met.
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * This function checks an edge to determine whether it has certain characteristics that signify
     * to us that we do not want to keep examining this component of the network.
     *
     * @param edge
     *            An Edge we're examining
     * @return {@code true} if the edge is already flagged, has an amenity type we want to exclude,
     *         or ends in a boundary node {@code false} otherwise
     */
    private boolean edgeCharacteristicsToIgnore(final Edge edge)
    {
        // If the edge has already been flagged by another process then we can break out of the
        // loop and assume that whether the check was a flag or not was handled by the other process
        return this.isFlagged(edge.getIdentifier())
                // We don't want to handle certain types of parking amenities
                || this.endOrStartNodeHasAmenityTypeToExclude(edge)
                // Ignore edges that have been way sectioned at the border, as has high probability
                // of creating a false positive due to the sectioning of the way
                || SyntheticBoundaryNodeTag.isBoundaryNode(edge.end())
                || SyntheticBoundaryNodeTag.isBoundaryNode(edge.start())
                // If the serviceInPedestrianNetworkFilter switch is off, ignore edges that are of
                // type at least service and are surrounded by pedestrian navigable ways. To flag
                // such edges, the filter must be on and it's implied that the edge must not have
                // the motor_vehicle tag.
                || !this.serviceInPedestrianNetworkFilter && IS_AT_LEAST_SERVICE_ROAD.test(edge)
                        && this.isConnectedToPedestrianNavigableHighway(edge)
                // Ignore service edges that end in a building or are within an airport polygon
                || SERVICE_ROAD.test(edge) && this.intersectsAirportOrBuilding(edge)
                // Consider car ferries a valid terminus
                || edge.outEdges().stream()
                        .anyMatch(outEdge -> RouteTag.isFerry(outEdge) && this.isAccessible(outEdge)
                                && this.isCarNavigable(outEdge, AccessTag.NO.toString()));
    }

    /**
     * This function checks to see if the end node of an Edge AtlasObject has an amenity tag with
     * one of the AMENITY_VALUES_TO_EXCLUDE or has a start node with amenity value as
     * "parking_entrance"
     *
     * @param object
     *            An AtlasObject (known to be an Edge)
     * @return {@code true} if the end node of the end has one of the AMENITY_VALUES_TO_EXCLUDE or
     *         start node has "amenity=parking_entrance", and {@code false} otherwise
     */
    private boolean endOrStartNodeHasAmenityTypeToExclude(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        return Validators.isOfType(edge.end(), AmenityTag.class, AMENITY_VALUES_TO_EXCLUDE)
                || Validators.isOfType(edge.start(), AmenityTag.class, AmenityTag.PARKING_ENTRANCE);
    }

    /**
     * Finds the car accessibility value for an {@link Edge} by returning the first value found,
     * using a descending list of tags to check. If no values are found fort the list of tags the
     * value is assumed to be YES.
     *
     * @param edge
     *            {@link Edge}
     * @return {@link String} car accessibility value
     */
    @SuppressWarnings({ "squid:S3740" })
    private String getCarAccess(final Edge edge, final String defaultValue)
    {
        final List<Class> carAccessTagsPrecedence = Arrays.asList(MotorcarTag.class,
                MotorVehicleTag.class, VehicleTag.class);
        for (final Class tagClass : carAccessTagsPrecedence)
        {
            if (Validators.hasValuesFor(edge, tagClass))
            {
                return edge.tag(Validators.findTagNameIn(tagClass)).toUpperCase();
            }
        }
        return defaultValue;
    }

    /**
     * Checks if the edge is within or intersects airport polygon. Airport polygon is an atlas area
     * with {@link AerowayTag} tag.
     *
     * @param edge
     *            any edge
     * @return true if the edge is within or intersects airport polygon
     */
    private boolean intersectsAirportOrBuilding(final Edge edge)
    {
        return StreamSupport
                .stream(edge.getAtlas()
                        .areasIntersecting(edge.bounds(),
                                area -> Validators.hasValuesFor(area, AmenityTag.class)
                                        || BuildingTag.isBuilding(area)
                                        || Validators.hasValuesFor(area, AerowayTag.class))
                        .spliterator(), false)
                .anyMatch(area -> area.asPolygon().overlaps(edge.asPolyLine()));
    }

    /**
     * Checks if the edge is publicly accessible. An edge is considered accessible to the public if
     * the {@link AccessTag} is not present or if present, is not one of the values in the
     * PRIVATE_ACCESS set in {@link AccessTag}.
     *
     * @param edge
     *            any Edge
     * @return true if the edge is accessible
     */
    private boolean isAccessible(final Edge edge)
    {
        return !Validators.hasValuesFor(edge, AccessTag.class) || !AccessTag.isPrivate(edge);
    }

    /**
     * Checks if the edge is car navigable. Edge is navigable if it has an accessibility value of
     * yes, designated, or, permissive.
     *
     * @param edge
     *            any Edge
     * @return true if the edge is navigable
     */
    private boolean isCarNavigable(final Edge edge, final String defaultValue)
    {
        final List<String> accessPermittedValues = Arrays.asList(AccessTag.YES.toString(),
                AccessTag.DESIGNATED.toString(), AccessTag.PERMISSIVE.toString());
        return accessPermittedValues.contains(this.getCarAccess(edge, defaultValue));
    }

    /**
     * Checks if an {@link Edge} is connected to any edge that is a pedestrian navigable highway
     *
     * @param edge
     *            any edge
     * @return true if the edge has connection to pedestrian navigable highways
     */
    private boolean isConnectedToPedestrianNavigableHighway(final Edge edge)
    {
        return edge.connectedEdges().stream().anyMatch(HighwayTag::isPedestrianNavigableHighway);
    }

    /**
     * Checks if the edge is fully enclosed within areas that have amenity tags that are in the
     * AMENITY_VALUES_TO_EXCLUDE list
     *
     * @param edge
     *            any edge
     * @return true if the edge is fully enclosed within the area with excluded amenity tag
     */
    private boolean isWithinAreasWithExcludedAmenityTags(final Edge edge)
    {
        return StreamSupport
                .stream(edge.getAtlas()
                        .areasIntersecting(edge.bounds(),
                                area -> Validators.isOfType(area, AmenityTag.class,
                                        AMENITY_VALUES_TO_EXCLUDE))
                        .spliterator(), false)
                .anyMatch(area -> area.asPolygon().fullyGeometricallyEncloses(edge.asPolyLine()));
    }

    /**
     * This function will check various elements of the edge to make sure that we should be looking
     * at it.
     *
     * @param object
     *            the edge to check whether we want to continue looking at it
     * @return {@code true} if is a valid object to look at
     */
    private boolean validEdge(final AtlasObject object)
    {
        return object instanceof Edge
                // Only allow car navigable highways with appropriate tagging for public
                // accessibility
                && HighwayTag.isCarNavigableHighway(object) && this.isAccessible((Edge) object)
                && this.isCarNavigable((Edge) object, AccessTag.YES.toString())
                // Ignore any highways tagged as areas
                && !TagPredicates.IS_AREA.test(object);
    }
}
