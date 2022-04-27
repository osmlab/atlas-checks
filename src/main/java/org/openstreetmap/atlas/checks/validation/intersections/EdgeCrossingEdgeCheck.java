package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.EdgeWalker;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Flags edges that are crossing other edges invalidly. If two edges are crossing each other, then
 * they should have an intersection location shared in both edges. Otherwise, their layer tag should
 * tell the difference.
 *
 * @author mkalender, gpogulsky, bbreithaupt, vlemberg
 */
public class EdgeCrossingEdgeCheck extends BaseCheck<Long>
{
    /**
     * A simple {@link EdgeWalker} that filters using nextCandidates.
     */
    private class EdgeCrossingEdgeWalker extends EdgeWalker
    {
        EdgeCrossingEdgeWalker(final Edge startingEdge,
                final Function<Edge, Stream<Edge>> nextCandidates)
        {
            super(startingEdge, nextCandidates);
        }
    }

    private static final String INSTRUCTION_FORMAT = "The roads with ids {0} invalidly cross each other."
            + " If two roads are crossing each other, then they should have nodes at intersection"
            + " locations unless they are explicitly marked as crossing. Otherwise, crossing roads"
            + " should have different layer tags.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(INSTRUCTION_FORMAT);
    private static final boolean CAR_NAVIGABLE_DEFAULT = true;
    private static final boolean PEDESTRIAN_NAVIGABLE_DEFAULT = false;
    private static final boolean CROSSING_CAR_NAVIGABLE_DEFAULT = true;
    private static final boolean CROSSING_PEDESTRIAN_NAVIGABLE_DEFAULT = false;
    private static final String MINIMUM_HIGHWAY_DEFAULT = HighwayTag.NO.toString();
    private static final String MAXIMUM_HIGHWAY_DEFAULT = HighwayTag.MOTORWAY.toString();
    private static final Long OSM_LAYER_DEFAULT = 0L;
    private static final Double CLUSTER_DISTANCE_DEFAULT = 500.0;
    private static final long serialVersionUID = 2146863485833228593L;
    private static final String DEFAULT_INDOOR_MAPPING = "indoor->*|highway->corridor,steps|level->*";
    private final boolean carNavigable;
    private final boolean pedestrianNavigable;
    private final boolean crossingCarNavigable;
    private final boolean crossingPedestrianNavigable;
    private final HighwayTag minimumHighwayType;
    private final HighwayTag maximumHighwayType;
    private final TaggableFilter indoorMapping;
    private final Distance clusterDistance;
    private final Map<Long, Tuple<Set<Location>, Set<Edge>>> clusteredIntersections = new HashMap<>();

    /**
     * Checks whether given {@link PolyLine}s can cross each other.
     *
     * @param edgeAsPolyLine
     *            {@link PolyLine} being crossed
     * @param edgeLayer
     *            {@link Optional} layer value for edge being crossed
     * @param crossingEdgeAsPolyLine
     *            Crossing {@link PolyLine}
     * @param crossingEdgeLayer
     *            {@link Optional} layer value for crossing edge
     * @param intersection
     *            Intersection {@link Location}
     * @return {@code true} if given {@link PolyLine}s can cross each other
     */
    private static boolean canCross(final PolyLine edgeAsPolyLine, final Long edgeLayer,
            final PolyLine crossingEdgeAsPolyLine, final Long crossingEdgeLayer,
            final Location intersection)
    {
        // If crossing edges have nodes at intersections points, then crossing is valid
        return edgeAsPolyLine.contains(intersection)
                && crossingEdgeAsPolyLine.contains(intersection)
                // Otherwise, if crossing edges has valid, but different tag values
                // Then that is still a valid crossing
                || !edgeLayer.equals(crossingEdgeLayer);
    }

    public EdgeCrossingEdgeCheck(final Configuration configuration)
    {
        super(configuration);
        this.minimumHighwayType = this.configurationValue(configuration, "minimum.highway.type",
                MINIMUM_HIGHWAY_DEFAULT, str -> Enum.valueOf(HighwayTag.class, str.toUpperCase()));
        this.maximumHighwayType = this.configurationValue(configuration, "maximum.highway.type",
                MAXIMUM_HIGHWAY_DEFAULT, str -> Enum.valueOf(HighwayTag.class, str.toUpperCase()));
        this.carNavigable = this.configurationValue(configuration, "car.navigable",
                CAR_NAVIGABLE_DEFAULT);
        this.pedestrianNavigable = this.configurationValue(configuration, "pedestrian.navigable",
                PEDESTRIAN_NAVIGABLE_DEFAULT);
        this.crossingCarNavigable = this.configurationValue(configuration, "crossing.car.navigable",
                CROSSING_CAR_NAVIGABLE_DEFAULT);
        this.crossingPedestrianNavigable = this.configurationValue(configuration,
                "crossing.pedestrian.navigable", CROSSING_PEDESTRIAN_NAVIGABLE_DEFAULT);
        this.indoorMapping = TaggableFilter.forDefinition(
                this.configurationValue(configuration, "indoor.mapping", DEFAULT_INDOOR_MAPPING));
        this.clusterDistance = this.configurationValue(configuration, "cluster.distance",
                CLUSTER_DISTANCE_DEFAULT, Distance::meters);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return TypePredicates.IS_EDGE.test(object)
                && this.isValidCrossingEdge(object, this.carNavigable, this.pedestrianNavigable);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        if (!this.clusteredIntersections.containsKey(object.getIdentifier()))
        {
            // Use EdgeWalker to gather all connected invalid crossings. The use of the walker
            // prevents
            // edges from being flagged more than once.
            final Set<Edge> collectedEdges = new EdgeCrossingEdgeWalker((Edge) object,
                    this.getInvalidCrossingEdges()).collectEdges();
            if (collectedEdges.size() > 1)
            {
                this.clusterIntersections(this.getIntersectionPairs(collectedEdges));
            }
            else
            {
                return Optional.empty();
            }
        }

        // Get the cluster of intersections and edges to flag and remove the entries from the map
        final Tuple<Set<Location>, Set<Edge>> cluster = this.clusteredIntersections
                .get(object.getIdentifier());
        cluster.getSecond()
                .forEach(edge -> this.clusteredIntersections.remove(edge.getIdentifier()));
        return this.createEdgeCrossCheckFlag(cluster);
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Group intersection pairs using a BFS search for points whith in {@link #clusterDistance}, and
     * add them to the {@link #clusteredIntersections} map.
     *
     * @param intersectionPairs
     *            {@link List} of {@link Tuple}s containing an intersection {@link Location} and
     *            {@link Edge}
     */
    private void clusterIntersections(final List<Tuple<Location, Edge>> intersectionPairs)
    {
        // Go until all the intersections have been removed from the list
        while (!intersectionPairs.isEmpty())
        {
            // Create a queue and add the first intersection
            final ArrayDeque<Tuple<Location, Edge>> queue = new ArrayDeque<>();
            queue.add(intersectionPairs.get(0));
            final Set<Location> locations = new HashSet<>();
            final Set<Edge> edges = new HashSet<>();

            // BFS search for nearby intersections
            while (!queue.isEmpty())
            {
                final Tuple<Location, Edge> intersectionPair = queue.pop();
                locations.add(intersectionPair.getFirst());
                edges.add(intersectionPair.getSecond());
                // Remove intersections from the original list
                intersectionPairs.remove(intersectionPair);

                queue.addAll(intersectionPairs.stream()
                        .filter(pair -> !queue.contains(pair)
                                && intersectionPair.getFirst().distanceTo(pair.getFirst())
                                        .isLessThanOrEqualTo(this.clusterDistance))
                        .collect(Collectors.toList()));
            }

            // Add cluster to the map
            edges.forEach(edge -> this.clusteredIntersections.put(edge.getIdentifier(),
                    Tuple.createTuple(locations, edges)));
        }
    }

    /**
     * Function creates edge cross check flag.
     *
     * @param cluster
     *            clustered {@link Set}s of {@link Location}s and {@link Edge}s to flag
     * @return newly created edge cross check flag including crossing edges locations.
     */
    private Optional<CheckFlag> createEdgeCrossCheckFlag(
            final Tuple<Set<Location>, Set<Edge>> cluster)
    {
        // Mark edges as flagged
        cluster.getSecond().forEach(edge -> this.markAsFlagged(edge.getIdentifier()));
        // Create flag
        return Optional.of(this.createFlag(cluster.getSecond(),
                this.getLocalizedInstruction(0,
                        cluster.getSecond().stream().map(AtlasObject::getOsmIdentifier)
                                .collect(Collectors.toSet())),
                new ArrayList<>(cluster.getFirst())));
    }

    /**
     * This function returns set of invalid intersections locations for given params.
     *
     * @param edge1
     *            Atlas object
     * @param edge2
     *            crossing edge
     * @return set of intersection locations.
     */
    private Set<Location> getIntersection(final Edge edge1, final Edge edge2)
    {
        final PolyLine edge1AsPolyLine = edge1.asPolyLine();
        final PolyLine edge2AsPolyLine = edge2.asPolyLine();
        final Long edge1LayerTag = LayerTag.getTaggedOrImpliedValue(edge1, OSM_LAYER_DEFAULT);
        final Long edge2LayerTag = LayerTag.getTaggedOrImpliedValue(edge2, OSM_LAYER_DEFAULT);
        return edge1AsPolyLine.intersections(edge2AsPolyLine).stream()
                .filter(intersection -> !canCross(edge1AsPolyLine, edge1LayerTag, edge2AsPolyLine,
                        edge2LayerTag, intersection))
                .collect(Collectors.toSet());
    }

    /**
     * Find the intersections between a set of edges. Intersection pairs are comprised of the
     * {@link Location} and one {@link Edge}. This means each real intersections will have two
     * intersections pairs, one for each edge involved.
     *
     * @param edges
     *            {@link Set} of {@link Edge}s
     * @return {@link List} of intersections as {@link Tuple}s with the intersection
     *         {@link Location} and {@link Edge}
     */
    private List<Tuple<Location, Edge>> getIntersectionPairs(final Set<Edge> edges)
    {
        final List<Tuple<Location, Edge>> intersections = new ArrayList<>();
        edges.forEach(edge -> edges.stream().filter(otherEdge -> !otherEdge.equals(edge))
                .forEach(otherEdge -> this.getIntersection(edge, otherEdge).forEach(
                        location -> intersections.add(Tuple.createTuple(location, edge)))));
        return intersections;
    }

    /**
     * A {@link Function} for an {@link EdgeWalker} that collects all connected invalid crossings.
     *
     * @return a {@link Set} of invalid crossing {@link Edge}s
     */
    private Function<Edge, Stream<Edge>> getInvalidCrossingEdges()
    {
        return edge ->
        {
            // Prepare the edge being tested for checks
            final Rectangle edgeBounds = edge.bounds();

            // Retrieve crossing edges
            final Atlas atlas = edge.getAtlas();
            return Iterables.asList(atlas.edgesIntersecting(edgeBounds,
                    // filter out the same edge and non-valid crossing edges
                    crossingEdge -> edge.getIdentifier() != crossingEdge.getIdentifier()
                            && this.isValidCrossingEdge(crossingEdge, this.crossingCarNavigable,
                                    this.crossingPedestrianNavigable)))
                    .stream()
                    .filter(crossingEdge -> crossingEdge.getOsmIdentifier() != edge
                            .getOsmIdentifier())
                    // Go through crossing items and collect invalid crossings
                    .filter(crossingEdge -> !this.getIntersection(edge, crossingEdge).isEmpty());
        };
    }

    /**
     * Check if {@link Edge} highway tag is vehicle or pedestrian navigable.
     *
     * @param edge
     *            Atlas object.
     * @param carNavigable
     *            Car Navigable Highway Type.
     * @param pedestrianNavigable
     *            Pedestrian Navigable Highway Type.
     * @return {@code true} if {@link Edge} is vehicle or pedestrian navigable.
     */
    private boolean isCrossingHighwayType(final Edge edge, final boolean carNavigable,
            final boolean pedestrianNavigable)
    {
        return (carNavigable && pedestrianNavigable
                && (HighwayTag.isCarNavigableHighway(edge)
                        || HighwayTag.isPedestrianNavigableHighway(edge)))
                || (carNavigable && HighwayTag.isCarNavigableHighway(edge)
                        || (pedestrianNavigable && HighwayTag.isPedestrianNavigableHighway(edge)))
                || (!carNavigable && !pedestrianNavigable
                        && (!HighwayTag.isCarNavigableHighway(edge)
                                && !HighwayTag.isPedestrianNavigableHighway(edge)));
    }

    /**
     * Validates given {@link AtlasObject} (assumed to be an {@link Edge}) whether it is a valid
     * crossing edge or not
     *
     * @param object
     *            {@link AtlasObject} to test
     * @return {@code true} if given {@link AtlasObject} object is a valid crossing edge
     */
    private boolean isValidCrossingEdge(final AtlasObject object, final boolean carNavigable,
            final boolean pedestrianNavigable)
    {
        if (((Edge) object).isMainEdge() && !this.isFlagged(object.getIdentifier())
                && object.getTag(AreaTag.KEY).isEmpty() && !this.indoorMapping.test(object))
        {
            final Optional<HighwayTag> highway = HighwayTag.highwayTag(object);
            if (highway.isPresent())
            {
                final HighwayTag highwayTag = highway.get();
                return this.isCrossingHighwayType((Edge) object, carNavigable, pedestrianNavigable)
                        && !HighwayTag.CROSSING.equals(highwayTag)
                        && highwayTag.isMoreImportantThanOrEqualTo(this.minimumHighwayType)
                        && highwayTag.isLessImportantThanOrEqualTo(this.maximumHighwayType);
            }
        }
        return false;
    }
}
