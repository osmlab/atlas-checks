package org.openstreetmap.atlas.checks.validation.points;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.Snapper;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.BarrierTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.LevelTag;
import org.openstreetmap.atlas.tags.NoExitTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Checks for {@link Node}s that should be connected to nearby {@link Node}s or {@link Edge}s. The
 * {@link Node}s and {@link Edge}s must be car navigable and not in the denylisted highway filter in
 * the config.
 *
 * @author matthieun
 * @author cuthbertm
 * @author mgostintsev
 * @author bbreithaupt
 */
public class ConnectivityCheck extends BaseCheck<Long>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Node {0,number,#} is likely supposed to be connected to: {1}");
    private static final double NEARBY_EDGE_THRESHOLD_DISTANCE_METERS_DEFAULT = 2.0;
    private static final double THRESHOLD_SCALE = 1.5;
    private static final int MAXIMUM_ANGLE = 180;
    // Highways to be ignored
    private static final String DEFAULT_DENYLISTED_HIGHWAYS_TAG_FILTER = "highway->no";
    private static final long serialVersionUID = -380675222726130708L;
    private final TaggableFilter denylistedHighwaysTaggableFilter;
    private final Distance threshold;

    public ConnectivityCheck(final Configuration configuration)
    {
        super(configuration);
        this.threshold = configurationValue(configuration, "nearby.edge.distance.meters",
                NEARBY_EDGE_THRESHOLD_DISTANCE_METERS_DEFAULT, Distance::meters);
        this.denylistedHighwaysTaggableFilter = TaggableFilter
                .forDefinition(configurationValue(configuration, "denylisted.highway.filter",
                        DEFAULT_DENYLISTED_HIGHWAYS_TAG_FILTER));
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Node && !SyntheticBoundaryNodeTag.isSyntheticBoundaryNode(object)
                && !this.isFlagged(object.getOsmIdentifier()) && !BarrierTag.isBarrier(object)
                && !Validators.isOfType(object, NoExitTag.class, NoExitTag.YES)
                && !this.connectedEdgesHaveLevelTags((Node) object)
                // Node is part of a valid road, for this check
                && ((Node) object).connectedEdges().stream().anyMatch(this::validEdgeFilter);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Node node = (Node) object;
        final Rectangle box = node.getLocation().boxAround(this.threshold);
        final CheckFlag connectivityFlag = new CheckFlag(getTaskIdentifier(object));
        final ArrayList<String> disconnectedObjects = new ArrayList<>();
        final Map<Long, Set<Edge>> nodeLayerMap = this.getLayerMap(node);
        // Get nearby Edges, ignoring ones that have a level tag or for whom themselves or their
        // connected edges cross over or under a connected Edge of the node
        final Iterable<Edge> nearbyEdges = object.getAtlas().edgesIntersecting(box,
                edge -> !LevelTag.getTaggedValue(edge).isPresent()
                        && nodeLayerMap
                                .containsKey(LayerTag.getTaggedOrImpliedValue(edge, LayerTag.ZERO))
                        && !(this.differentLayersIntersect(nodeLayerMap, edge) || this
                                .differentLayersIntersect(nodeLayerMap, edge.connectedEdges())));
        final Set<Edge> connectedEdges = node.connectedEdges();

        connectivityFlag.addObject(object);
        // Check nearby Nodes, ignoring ones that have connected Edges that have level tags or cross
        // over or under connected edges of the object node
        for (final Node nodeNearby : object.getAtlas().nodesWithin(box,
                nearbyNode -> !this.connectedEdgesHaveLevelTags(nearbyNode)
                        && !this.differentLayersIntersect(nodeLayerMap, nearbyNode.connectedEdges())
                        && this.getLayerMap(nearbyNode).keySet().stream()
                                .anyMatch(nodeLayerMap::containsKey)))
        {
            // Flag nearby nodes if they are neither synthetic boundary node nor a start node, are
            // not a
            // barrier, have a valid
            // connected edge, and there is not a valid route to the start node
            if (!SyntheticBoundaryNodeTag.isSyntheticBoundaryNode(nodeNearby)
                    && !node.equals(nodeNearby) && !BarrierTag.isBarrier(nodeNearby)
                    && !Validators.isOfType(nodeNearby, NoExitTag.class, NoExitTag.YES)
                    && nodeNearby.connectedEdges().stream().anyMatch(this::validEdgeFilter)
                    && !hasValidConnection(node, connectedEdges, nodeNearby))
            {
                this.markAsFlagged(nodeNearby.getOsmIdentifier());
                connectivityFlag.addObject(nodeNearby);
                disconnectedObjects.add("node " + nodeNearby.getOsmIdentifier());
            }
        }
        for (final Edge edgeNearby : nearbyEdges)
        {
            // There is a valid main edge close by that is not validly connected to the start
            // node.
            if (edgeNearby.isMainEdge() && validEdgeFilter(edgeNearby)
                    && !connectedEdges.contains(edgeNearby)
                    && !hasValidConnection(node, connectedEdges, edgeNearby)
                    // Make sure that the spatial index did not over estimate.
                    && node.snapTo(edgeNearby).getDistance()
                            .isLessThanOrEqualTo(this.threshold.scaleBy(THRESHOLD_SCALE)))
            {
                connectivityFlag.addObject(edgeNearby);
                disconnectedObjects.add("way " + edgeNearby.getOsmIdentifier());
            }
        }
        if (connectivityFlag.getFlaggedObjects().size() > 1)
        {
            this.markAsFlagged(object.getOsmIdentifier());
            connectivityFlag.addInstruction(this.getLocalizedInstruction(0,
                    object.getOsmIdentifier(), String.join(", ", disconnectedObjects)));
            return Optional.of(connectivityFlag);
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Check if any of a {@link Node}s connected {@link Edge}s have {@link LevelTag}s.
     *
     * @param node
     *            to check the {@link Edge}s of
     * @return true if any of the connected {@link Edge}s have a {@link LevelTag}
     */
    private boolean connectedEdgesHaveLevelTags(final Node node)
    {
        return node.connectedEdges().stream()
                .anyMatch(connectedEdge -> LevelTag.getTaggedValue(connectedEdge).isPresent());
    }

    /**
     * Check if the given {@link Edge} intersects any {@link Edge} from the node layer map that is
     * not on the same level as itself.
     *
     * @param nodeLayerMap
     *            a {@link Map} layer value keys and values of {@link Set}s of {@link Edge}s
     * @param edge
     *            the {@link Edge} to check
     * @return boolean
     */
    private boolean differentLayersIntersect(final Map<Long, Set<Edge>> nodeLayerMap,
            final Edge edge)
    {
        final long edgeLayerValue = LayerTag.getTaggedOrImpliedValue(edge, LayerTag.ZERO);
        return nodeLayerMap.entrySet().stream().anyMatch(entry -> !entry.getKey()
                .equals(edgeLayerValue)
                && entry.getValue().stream().anyMatch(
                        connectedEdge -> connectedEdge.asPolyLine().intersects(edge.asPolyLine())));
    }

    /**
     * Check if any {@link Edge}s from a node layer map intersect {@link Edge}s of a different level
     * from a given set.
     *
     * @param nodeLayerMap
     *            a {@link Map} layer value keys and values of {@link Set}s of {@link Edge}s
     * @param edges
     *            the {@link Edge}s to check
     * @return boolean
     */
    private boolean differentLayersIntersect(final Map<Long, Set<Edge>> nodeLayerMap,
            final Set<Edge> edges)
    {
        return edges.stream().anyMatch(edge -> this.differentLayersIntersect(nodeLayerMap, edge));
    }

    /**
     * Gets a heading from one {@link Location} to another by creating a {@link PolyLine}.
     *
     * @param firstLocation
     *            {@link Location} where the heading starts
     * @param lastLocation
     *            {@link Location} that the heading points towards
     * @return {@link Optional} of the {@link Heading}
     */
    private Optional<Heading> getConnectedHeading(final Location firstLocation,
            final Location lastLocation)
    {
        return new PolyLine(firstLocation, lastLocation).overallHeading();
    }

    /**
     * Gets the initial heading from a {@link Node} back along the {@link Edge} it is an end of.
     *
     * @param firstEdge
     *            {@link Edge} containing {@code node}
     * @param node
     *            {@link Node} the heading starts from
     * @return {@link Optional} of the {@link Heading}
     */
    private Optional<Heading> getEdgeHeadingFromNode(final Edge firstEdge, final Node node)
    {
        return firstEdge.end().equals(node) ? firstEdge.asPolyLine().reversed().initialHeading()
                : firstEdge.asPolyLine().initialHeading();
    }

    /**
     * Helper function for {@link #isConverging(Node, Edge, Edge, Edge)}. Gets the heading from the
     * snapped location backwards along the connection route.
     *
     * @param snappedLocation
     *            {@link Location} to get a heading from
     * @param previousEdge
     *            {@link Edge} that preceded {@code snappedLocation} on the connection route
     * @param lastEdge
     *            {@link Edge} that contains {@code snappedLocation}
     * @return an {@link Optional} of a {@link Heading} from {@code snappedLocation} backwards along
     *         the connection route
     */
    private Optional<Heading> getHeadingFromSnap(final Location snappedLocation,
            final Edge previousEdge, final Edge lastEdge)
    {
        // Get the location in previousEdge closest to snappedLocation on the connecting route.
        final Location lastPreviousEdgeLocation = lastEdge.connectedNodes()
                .contains(previousEdge.end()) ? previousEdge.end().getLocation()
                        : previousEdge.start().getLocation();
        // Get the heading from snappedLocation along the route
        // If snappedLocation and lastPreviousEdgeLocation are the same: use a previousEdge heading

        if (lastPreviousEdgeLocation.equals(snappedLocation))
        {
            if (previousEdge.start().getLocation().equals(lastPreviousEdgeLocation))
            {
                return previousEdge.asPolyLine().initialHeading();
            }
            else
            {
                return previousEdge.asPolyLine().reversed().initialHeading();
            }
        }
        else
        {
            return new PolyLine(snappedLocation, lastPreviousEdgeLocation).overallHeading();
        }
    }

    /**
     * Get a {@link Map} of connected {@link Edge}s for their layer values.
     *
     * @param node
     *            {@link Node} to get the connected {@link Edge}s for
     * @return a {@link Map} layer value keys and values of {@link Set}s of {@link Edge}s
     */
    private Map<Long, Set<Edge>> getLayerMap(final Node node)
    {
        final Map<Long, Set<Edge>> layerMap = new HashMap<>();

        node.connectedEdges().forEach(edge ->
        {
            final long edgeLayerValue = LayerTag.getTaggedOrImpliedValue(edge, LayerTag.ZERO);
            layerMap.putIfAbsent(edgeLayerValue, new HashSet<>());
            layerMap.get(edgeLayerValue).add(edge);
        });

        return layerMap;
    }

    /**
     * Checks for a valid connection between 2 {@link Node}s using a depth first search. A valid
     * connection will have the first and last {@link Edge}s converging. Diverging Edges are
     * generally a sign that the nodes should be merged, and thus should be flagged regardless of
     * their connection.
     *
     * @param node
     *            the {@link Node} to try to find the connection from
     * @param firstEdges
     *            a {@link Set} of {@link Edge}s connected to {@code node}
     * @param toFind
     *            the {@link Node} to try to find a connection to
     * @return true if a valid connection is found
     */
    private boolean hasValidConnection(final Node node, final Set<Edge> firstEdges,
            final Node toFind)
    {
        final Set<Edge> visitedEdges = new HashSet<>();
        final Set<Edge> secondEdges = new HashSet<>();
        final Deque<Edge> edgesToCheck = new ArrayDeque<>(firstEdges);
        Edge rootEdge = edgesToCheck.peek();

        while (!edgesToCheck.isEmpty())
        {
            final Edge checking = edgesToCheck.pop();
            final boolean firstEdge = firstEdges.contains(checking);
            if (firstEdge)
            {
                // If it is part of a root it is always a valid connection.
                if (checking.connectedNodes().contains(toFind))
                {
                    return true;
                }
                // Keep track of the current route, and allow multiple checks of child edges but
                // from different roots.
                rootEdge = checking;
                visitedEdges.clear();
                secondEdges.clear();
            }
            // If toFind is found, check the connecting route for convergence
            else if (checking.connectedNodes().contains(toFind)
                    && isConverging(node, rootEdge, toFind, checking))
            {
                return true;
            }
            // Mark visited edges, and gather new edges up to a depth of 3
            if (firstEdge || secondEdges.contains(checking))
            {
                checking.connectedEdges().forEach(edge ->
                {
                    if (!visitedEdges.contains(edge) && !firstEdges.contains(edge)
                            && edge.isMainEdge())
                    {
                        edgesToCheck.push(edge);
                        visitedEdges.add(edge);
                    }
                    if (firstEdge)
                    {
                        secondEdges.add(edge);
                    }
                });
            }
        }
        return false;
    }

    /**
     * Checks for a valid connection between a {@link Node} and an {@link Edge} using a depth first
     * search. A valid connection will have the first and last {@link Edge}s converging. Diverging
     * Edges are generally a sign that the node should be part of the edge, and thus should be
     * flagged regardless of their connection.
     *
     * @param node
     *            the {@link Node} to try to find the connection from
     * @param firstEdges
     *            a {@link Set} of {@link Edge}s connected to {@code node}
     * @param toFind
     *            the {@link Edge} to try to find a connection to
     * @return true if a valid connection is found
     */
    private boolean hasValidConnection(final Node node, final Set<Edge> firstEdges,
            final Edge toFind)
    {
        final Set<Edge> visitedEdges = new HashSet<>();
        final Set<Edge> secondEdges = new HashSet<>();
        final Deque<Edge> edgesToCheck = new ArrayDeque<>(firstEdges);
        Edge rootEdge = edgesToCheck.peek();
        Edge previousEdge = rootEdge;

        while (!edgesToCheck.isEmpty())
        {
            final Edge checking = edgesToCheck.pop();
            final boolean firstEdge = firstEdges.contains(checking);
            final boolean secondEdge = secondEdges.contains(checking);
            if (firstEdge)
            {
                // Keep track of the current route, and allow multiple checks of child edges but
                // from different roots.
                rootEdge = checking;
                visitedEdges.clear();
                secondEdges.clear();
            }
            // If toFind is found, check the connecting route for convergence
            else if (checking.equals(toFind)
                    && isConverging(node, rootEdge, secondEdge ? rootEdge : previousEdge, checking))
            {
                return true;
            }
            // Mark visited edges, and gather new edges up to a depth of 3
            if (firstEdge || secondEdge)
            {
                previousEdge = checking;
                checking.connectedEdges().forEach(edge ->
                {
                    if (!visitedEdges.contains(edge) && !firstEdges.contains(edge)
                            && edge.isMainEdge())
                    {
                        edgesToCheck.push(edge);
                        visitedEdges.add(edge);
                    }
                    if (firstEdge)
                    {
                        secondEdges.add(edge);
                    }
                });
            }
        }
        return false;
    }

    /**
     * If all headings exist, get the sum of the angles formed by the headings, as rays from their
     * originating locations. If this sum is less than 180 the rays form a triangle.
     *
     * @param firstHeading
     *            {@link Optional} of a {@link Heading} originating from the start and pointing
     *            along the route
     * @param connectionHeading
     *            {@link Optional} of a {@link Heading} originating from the start and pointing
     *            towards the end
     * @param lastHeading
     *            {@link Optional} of a {@link Heading} originating from the end and pointing along
     *            the route
     * @return true if the headings form a triangle
     */
    private boolean headingsFormTriangle(final Optional<Heading> firstHeading,
            final Optional<Heading> connectionHeading, final Optional<Heading> lastHeading)
    {
        return firstHeading.isPresent() && lastHeading.isPresent() && connectionHeading.isPresent()
                && firstHeading.get().difference(connectionHeading.get()).asDegrees()
                        + connectionHeading.get().add(Angle.degrees(MAXIMUM_ANGLE))
                                .difference(lastHeading.get()).asDegrees() < MAXIMUM_ANGLE;
    }

    /**
     * Checks if the connecting route between 2 {@link Node}s has converging start and end
     * {@link Edge}s. This is achieved by using headings to form 2 connected angles. The angles are
     * the difference between the headings. If the sum of the angles is less than 180 degrees the
     * unconnected headings will eventually meet and form a triangle, thus making them converging.
     * This is used in place of established cross product vector intersection formula due to
     * floating point precision issues at the < 2 meter scale of this check.
     *
     * @param node
     *            {@link Node} that starts the route
     * @param firstEdge
     *            {@link Edge} that contains {@code node} and starts the route
     * @param endNode
     *            {@link Node} that ends the route
     * @param lastEdge
     *            {@link Edge} that contains {@code endNode} and ends the route
     * @return
     */
    private boolean isConverging(final Node node, final Edge firstEdge, final Node endNode,
            final Edge lastEdge)
    {
        // Get the heading from node along the connection route
        final Optional<Heading> firstHeading = getEdgeHeadingFromNode(firstEdge, node);
        // Get the heading from endNode along the connection route
        final Optional<Heading> lastHeading = lastEdge.start().equals(endNode)
                ? lastEdge.asPolyLine().initialHeading()
                : lastEdge.asPolyLine().reversed().initialHeading();
        // Get the heading from node to endNode
        final Optional<Heading> connectionHeading = getConnectedHeading(node.getLocation(),
                endNode.getLocation());
        // Return true if the headings form a triangle
        return headingsFormTriangle(firstHeading, connectionHeading, lastHeading);
    }

    /**
     * Checks if the connecting route between a {@link Node} and an {@link Edge} has converging
     * start and end {@link Edge}s. This is achieved by using headings to form 2 connected angles.
     * The angles are the difference between the headings. If the sum of the angles is less than 180
     * degrees the unconnected headings will eventually meet and form a triangle, thus making them
     * converging. This is used in place of established cross product vector intersection formula
     * due to floating point precision issues at the < 2 meter scale of this check.
     *
     * @param node
     *            {@link Node} that starts the route
     * @param firstEdge
     *            {@link Edge} that contains {@code node} and starts the route
     * @param previousEdge
     *            {@link Edge} that precedes {@code lastEdge} in the route
     * @param lastEdge
     *            {@link Edge} that ends the route
     * @return
     */
    private boolean isConverging(final Node node, final Edge firstEdge, final Edge previousEdge,
            final Edge lastEdge)
    {
        // Get the location where node should passably be connected to lastEdge
        final Snapper.SnappedLocation snappedLocation = node.snapTo(lastEdge);
        // If node and snappedLocation are on top of each other, convergence cannot be properly
        // calculated and node should probably be part of lastEdge
        if (!node.getLocation().equals(snappedLocation))
        {
            // Get the heading from node along the connection route
            final Optional<Heading> firstHeading = getEdgeHeadingFromNode(firstEdge, node);
            // Get the heading from snappedLocation along the route
            final Optional<Heading> lastHeading = getHeadingFromSnap(snappedLocation, previousEdge,
                    lastEdge);
            // Get the heading from node to snappedLocation
            final Optional<Heading> connectionHeading = getConnectedHeading(node.getLocation(),
                    snappedLocation);
            // Return true if the headings form a triangle
            return headingsFormTriangle(firstHeading, connectionHeading, lastHeading);
        }
        return false;
    }

    /**
     * Helper function for filtering {@link Edge}s. This is to detect {@link Edge}s that are too
     * complex to currently be handled by this check.
     *
     * @param edge
     *            - Edge to examine
     * @return true if Edge is car navigable and does not have a barrier tag
     */
    private boolean validEdgeFilter(final Edge edge)
    {
        return HighwayTag.isCarNavigableHighway(edge)
                && !this.denylistedHighwaysTaggableFilter.test(edge) && !BarrierTag.isBarrier(edge);
    }
}
