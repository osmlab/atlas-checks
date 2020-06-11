package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.*;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Flags edges that are closed shaped and have minimum of two intersections with navigable roads.
 * {@link RoundaboutMissingTagCheck#MINIMUM_INTERSECTION} connections. See
 * https://wiki.openstreetmap.org/wiki/Tag:junction%3Droundabout for more information about roundabouts
 *
 * @author vladlemberg
 */

public class RoundaboutMissingTagCheck extends BaseCheck<Long>
{
    // Instructions
    public static final String MISSING_JUNCTION_TAG_INSTRUCTION = "This edge might be a roundabout";
    // Minimum intersection with Navigable Roads
    private static final int MINIMUM_INTERSECTION = 2;
    public static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(MISSING_JUNCTION_TAG_INSTRUCTION);
    private static final double MAX_THRESHOLD_DEGREES_DEFAULT = 40.0;
    private static final double MIN_THRESHOLD_DEGREES_DEFAULT = 10.0;
    private final Angle maxAngleThreshold;
    private final Angle minAngleThreshold;

    private static boolean aConnectedEdgeValenceMoreThan(final Edge edge)
    {
        //Identify all sections of original OSM way
        final Set<Edge> edgesFormingOSMWay = new OsmWayWalker(edge).collectEdges().stream()
                .filter(Edge::isMasterEdge).collect(Collectors.toSet());
        Set<Long> connectedEdges = new HashSet<>();
        edgesFormingOSMWay.stream()
                .forEach(obj -> obj.connectedEdges()
                        .stream()
                        .filter(Edge::isMasterEdge)
                        //intersection with navigable roads
                        .filter(HighwayTag::isCarNavigableHighway)
                        .filter(e -> e.getTag(ServiceTag.KEY).isEmpty())
                        //de-duplication sectioned edges
                        .forEach(wayId -> connectedEdges.add(wayId.getOsmIdentifier())));
        return (connectedEdges.size() > MINIMUM_INTERSECTION);
    }

    //Re-build original OSM way geometry polyline.
    private PolyLine buildOriginalOsmWayGeometry(final Edge edge) {

        //Identify all sections of original OSM way
        final Set<Edge> edgesFormingOSMWay = new OsmWayWalker(edge).collectEdges().stream()
                .filter(Edge::isMasterEdge).collect(Collectors.toSet());

        //Sort sectioned Edges by Ids
        final List<Edge> sortedEdges = new ArrayList<>(edgesFormingOSMWay);
        sortedEdges.sort(Comparator.comparingLong(AtlasObject::getIdentifier));

        //Build original OSM polyline
        PolyLine geometry = new PolyLine(sortedEdges.get(0).getRawGeometry());
        for (int i = 0; i < sortedEdges.size(); i++) {
            if (i > 0) {
                geometry = geometry.append(sortedEdges.get(i).asPolyLine());
            }
        }
        return geometry;
    }

    //Master edge is part of closed way
    private boolean isPartOfClosedWay(final Edge object)
    {
        final HashSet<Long> wayIds = new HashSet<>();
        Edge nextEdge = object;
        // Loop through out going edges with the same OSM id
        while (nextEdge != null)
        {
            wayIds.add(nextEdge.getIdentifier());
            final List<Edge> nextEdgeList = Iterables.stream(nextEdge.outEdges())
                    .filter(Edge::isMasterEdge)
                    .filter(outEdge -> outEdge.getOsmIdentifier() == object.getOsmIdentifier())
                    .collectToList();
            nextEdge = nextEdgeList.isEmpty() ? null : nextEdgeList.get(0);
            // If original edge is found, the way is closed
            if (nextEdge != null && wayIds.contains(nextEdge.getIdentifier()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public RoundaboutMissingTagCheck(final Configuration configuration)
    {
        super(configuration);
        this.maxAngleThreshold = configurationValue(configuration, "threshold.degrees",
                MAX_THRESHOLD_DEGREES_DEFAULT, Angle::degrees);
        this.minAngleThreshold = configurationValue(configuration, "threshold.degrees",
                MIN_THRESHOLD_DEGREES_DEFAULT, Angle::degrees);
    }

    /**
     * Validates if given {@link AtlasObject} is actually an {@link Edge} and is a potential roundabout and
     * also corresponding OSM identifier shouldn't be flagged before (this is for avoiding duplicate
     * flags)
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && !this.isFlagged(object.getOsmIdentifier());
    }

    /**
     * Flags an {@link Edge} is its circular shape and connected to at
     * least {@link RoundaboutMissingTagCheck#MINIMUM_INTERSECTION} navigable roads.
     * {@link Edge} doesn't have junction=roundabout or area=yes tags.
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;

        //process only first sectioned Edge
        if(edge.getIdentifier() % 10 == 1 && edge.isMasterEdge()
                && HighwayTag.isCarNavigableHighway(edge)
                    && isPartOfClosedWay(edge))
        {
            if (!edge.getTag(JunctionTag.KEY).isPresent()  && !edge.getTag(AreaTag.KEY).isPresent()
                    && aConnectedEdgeValenceMoreThan(edge))
            {
                //rebuild original OSM Way geometry
                PolyLine originalGeom = buildOriginalOsmWayGeometry(edge);
                //check maximum angle
                final List<Tuple<Angle, Location>> maxOffendingAngles = (originalGeom
                        .anglesGreaterThanOrEqualTo(this.maxAngleThreshold));
                //check minimum angle
                final List<Tuple<Angle, Location>> minOffendingAngles = (originalGeom
                        .anglesLessThanOrEqualTo(this.minAngleThreshold));

                if (maxOffendingAngles.isEmpty() && minOffendingAngles.isEmpty())
                {
                    System.out.println(edge.getOsmIdentifier());
                    this.markAsFlagged(object.getOsmIdentifier());
                    return Optional.of(createFlag(new OsmWayWalker(edge).collectEdges(),
                            this.getLocalizedInstruction(0)));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
