package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.ArrayDeque;
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
 */
public class SinkIslandCheck extends BaseCheck<Long>
{
    private static final AmenityTag[] AMENITY_VALUES_TO_EXCLUDE = { AmenityTag.PARKING,
            AmenityTag.PARKING_SPACE, AmenityTag.MOTORCYCLE_PARKING, AmenityTag.PARKING_ENTRANCE };
    private static final String DEFAULT_MINIMUM_HIGHWAY_TYPE = "SERVICE";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList("Road is impossible to get out of.");
    private static final float LOAD_FACTOR = 0.8f;
    private static final Predicate<AtlasObject> NAVIGABLE_HIGHWAYS = object -> Validators
            .isOfType(object, MotorVehicleTag.class, MotorVehicleTag.YES)
            || Validators.isOfType(object, MotorcarTag.class, MotorcarTag.YES)
            || Validators.isOfType(object, VehicleTag.class, VehicleTag.YES);
    private static final Predicate<AtlasObject> SERVICE_ROAD = object -> Validators.isOfType(object,
            HighwayTag.class, HighwayTag.SERVICE);
    private static final long TREE_SIZE_DEFAULT = 50;
    private static final long serialVersionUID = -1432150496331502258L;
    private final HighwayTag minimumHighwayType;
    private final int storeSize;
    private final int treeSize;

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
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return this.validEdge(object) && !this.isFlagged(object.getIdentifier())
                && ((Edge) object).highwayTag()
                        .isMoreImportantThanOrEqualTo(this.minimumHighwayType)
                && !(SERVICE_ROAD.test(object)
                        && (this.isWithinAreasWithExcludedAmenityTags((Edge) object)
                                || this.intersectsAirportOrBuilding((Edge) object)));
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Flag to keep track of whether we found an issue or not
        boolean emptyFlag = false;

        // The current edge to be explored
        Edge candidate = (Edge) object;

        // A set of all edges that we have already explored
        final Set<AtlasObject> explored = new HashSet<>(this.storeSize, LOAD_FACTOR);
        // A set of all edges that we explore that have no outgoing edges
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
                emptyFlag = true;
                break;
            }

            // Retrieve all the valid outgoing edges to explore
            final Set<Edge> outEdges = candidate.outEdges().stream().filter(this::validEdge)
                    .collect(Collectors.toSet());

            if (outEdges.isEmpty())
            {
                // Sink edge. Don't mark the edge explored until we know how big the tree is
                terminal.add(candidate);
            }
            else
            {
                // Add the current candidate to the set of already explored edges
                explored.add(candidate);

                // From the list of outgoing edges from the current candidate filter out any edges
                // that have already been explored and add all the rest to the queue of possible
                // candidates
                outEdges.stream().filter(outEdge -> !explored.contains(outEdge))
                        .forEach(candidates::add);

                // If the number of available candidates and the size of the currently explored
                // items is larger then the configurable tree size, then we can break out of the
                // loop and assume that this is not a SinkIsland
                if (candidates.size() + explored.size() > this.treeSize)
                {
                    emptyFlag = true;
                    break;
                }
            }

            // Get the next candidate
            candidate = candidates.poll();
        }

        // If we exit due to tree size (emptyFlag == true) and there are terminal edges we could
        // cache them and check on entry to this method. However it seems to happen too rare in
        // practice. So these edges (if any) will be processed as all others. Even though they would
        // not generate any candidates. Otherwise if we covered the whole tree, there is no need to
        // delay processing of terminal edges. We should add them to the geometry we are going to
        // flag.
        if (!emptyFlag)
        {
            // Include all touched edges
            explored.addAll(terminal);
        }

        // Set every explored edge as flagged for any other processes to know that we have already
        // process all those edges
        explored.forEach(marked -> this.markAsFlagged(marked.getIdentifier()));

        // Create the flag if and only if the empty flag value is not set to false
        return emptyFlag ? Optional.empty()
                : Optional.of(createFlag(explored, this.getLocalizedInstruction(0)));
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
                // Ignore edges that are of type service and is connected to pedestrian navigable
                // ways or that ends in a building or is within an airport polygon
                || SERVICE_ROAD.test(edge) && (this.isConnectedToPedestrianNavigableHighway(edge)
                        || this.intersectsAirportOrBuilding(edge));
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
     * Checks if the edge is car navigable in terms of {@link MotorVehicleTag}, {@link MotorcarTag}
     * and {@link VehicleTag}. Edge is navigable if 1) MotorVehicleTag, MotorcarTag and VehicleTag
     * is absent or 2) If present, its value equals YES.
     *
     * @param edge
     *            any Edge
     * @return true if the edge is navigable
     */
    private boolean isNavigable(final Edge edge)
    {
        return !Validators.hasValuesFor(edge, MotorVehicleTag.class)
                && !Validators.hasValuesFor(edge, MotorcarTag.class)
                && !Validators.hasValuesFor(edge, VehicleTag.class)
                || NAVIGABLE_HIGHWAYS.test(edge);
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
                // Only allow car navigable highways (access = yes and
                // motor_vehicle/motorcar/vehicle = yes)
                // and ignore ferries
                && HighwayTag.isCarNavigableHighway(object) && this.isAccessible((Edge) object)
                && this.isNavigable((Edge) object) && !RouteTag.isFerry(object)
                // Ignore any highways tagged as areas
                && !TagPredicates.IS_AREA.test(object);
    }
}
