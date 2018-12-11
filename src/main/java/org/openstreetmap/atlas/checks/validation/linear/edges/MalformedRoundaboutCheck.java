package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.walker.SimpleEdgeWalker;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.TunnelTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags roundabouts where the directionality is opposite to what it should be, the
 * roundabout is multi-directional, the roundabout is incomplete, part of the roundabout is not car
 * navigable, or the roundabout has car navigable edges inside it.
 *
 * @author savannahostrowski
 * @author bbreithaupt
 */

public class MalformedRoundaboutCheck extends BaseCheck
{
    private static final long serialVersionUID = -3018101860747289836L;
    private static final String WRONG_WAY_INSTRUCTIONS = "This roundabout is going the"
            + " wrong direction, or has been improperly tagged as a roundabout.";
    private static final String INCOMPLETE_ROUTE_INSTRUCTIONS = "This roundabout does not form a single, one-way, complete, car navigable route.";
    private static final String ENCLOSED_ROADS_INSTRUCTIONS = "This roundabout has car navigable ways inside it.";
    private static final List<String> LEFT_DRIVING_COUNTRIES_DEFAULT = Arrays.asList("AIA", "ATG",
            "AUS", "BGD", "BHS", "BMU", "BRB", "BRN", "BTN", "BWA", "CCK", "COK", "CXR", "CYM",
            "CYP", "DMA", "FJI", "FLK", "GBR", "GGY", "GRD", "GUY", "HKG", "IDN", "IMN", "IND",
            "IRL", "JAM", "JEY", "JPN", "KEN", "KIR", "KNA", "LCA", "LKA", "LSO", "MAC", "MDV",
            "MLT", "MOZ", "MSR", "MUS", "MWI", "MYS", "NAM", "NFK", "NIU", "NPL", "NRU", "NZL",
            "PAK", "PCN", "PNG", "SGP", "SGS", "SHN", "SLB", "SUR", "SWZ", "SYC", "TCA", "THA",
            "TKL", "TLS", "TON", "TTO", "TUV", "TZA", "UGA", "VCT", "VGB", "VIR", "WSM", "ZAF",
            "ZMB", "ZWE");
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(WRONG_WAY_INSTRUCTIONS,
            INCOMPLETE_ROUTE_INSTRUCTIONS, ENCLOSED_ROADS_INSTRUCTIONS);

    private List<String> leftDrivingCountries;

    /**
     * An enum of RoundaboutDirections
     */
    public enum RoundaboutDirection
    {
        CLOCKWISE,
        COUNTERCLOCKWISE,
        // Handles the case where we were unable to get any information about the roundabout's
        // Direction.
        UNKNOWN
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    public MalformedRoundaboutCheck(final Configuration configuration)
    {
        super(configuration);
        this.leftDrivingCountries = (List<String>) configurationValue(configuration,
                "traffic.countries.left", LEFT_DRIVING_COUNTRIES_DEFAULT);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // We check that the object is an instance of Edge
        return object instanceof Edge
                // Make sure that the object has an iso_country_code
                && object.getTag(ISOCountryTag.KEY).isPresent()
                // Make sure that the edges are instances of roundabout
                && JunctionTag.isRoundabout(object)
                // And that the Edge has not already been marked as flagged
                && !this.isFlagged(object.getIdentifier())
                // Make sure that we are only looking at master edges
                && ((Edge) object).isMasterEdge()
                // Check for excluded highway types
                && !this.isExcludedHighway(object);
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        final String isoCountryCode = edge.tag(ISOCountryTag.KEY).toUpperCase();

        // Get all edges in the roundabout
        final Set<Edge> roundaboutEdgeSet = new SimpleEdgeWalker(edge, this.isRoundaboutEdge())
                .collectEdges();
        roundaboutEdgeSet
                .forEach(roundaboutEdge -> this.markAsFlagged(roundaboutEdge.getIdentifier()));
        final Route roundaboutEdges;
        // Flag if any of them are not car navigable or master Edges
        if (roundaboutEdgeSet.stream()
                .anyMatch(roundaboutEdge -> !HighwayTag.isCarNavigableHighway(roundaboutEdge)
                        || !roundaboutEdge.isMasterEdge()))
        {
            return Optional.of(this.createFlag(roundaboutEdgeSet, this.getLocalizedInstruction(1)));
        }
        // Try to build a Route from the edges
        try
        {
            roundaboutEdges = Route.fromNonArrangedEdgeSet(roundaboutEdgeSet, false);
            if (!roundaboutEdges.start().inEdges().contains(roundaboutEdges.end()))
            {
                return Optional
                        .of(this.createFlag(roundaboutEdgeSet, this.getLocalizedInstruction(1)));
            }
        }
        // If a Route cannot be formed, flag the edges as an incomplete roundabout.
        catch (final CoreException badRoundabout)
        {
            return Optional.of(this.createFlag(roundaboutEdgeSet, this.getLocalizedInstruction(1)));
        }

        // Get the direction of the roundabout
        final RoundaboutDirection direction = findRoundaboutDirection(roundaboutEdges);

        // Determine if the roundabout is in a left or right driving country
        final boolean isLeftDriving = leftDrivingCountries.contains(isoCountryCode);

        // If the roundabout traffic is clockwise in a right-driving country, or
        // If the roundabout traffic is counterclockwise in a left-driving country
        if (direction.equals(RoundaboutDirection.CLOCKWISE) && !isLeftDriving
                || direction.equals(RoundaboutDirection.COUNTERCLOCKWISE) && isLeftDriving)
        {
            return Optional.of(this.createFlag(roundaboutEdgeSet, this.getLocalizedInstruction(0)));
        }

        // If there are car navigable edges inside the roundabout flag it, as it is probably
        // malformed or a throughabout
        if (roundaboutEdgeSet.stream().noneMatch(this::ignoreCrossings)
                && this.roundaboutEnclosesRoads(roundaboutEdges))
        {
            return Optional.of(this.createFlag(roundaboutEdgeSet, this.getLocalizedInstruction(2)));
        }

        return Optional.empty();
    }

    /**
     * Function for {@link SimpleEdgeWalker} that gathers connected edges that are part of a
     * roundabout.
     *
     * @return {@link Function} for {@link SimpleEdgeWalker}
     */
    private Function<Edge, Stream<Edge>> isRoundaboutEdge()
    {
        return edge -> edge.connectedEdges().stream()
                .filter(connected -> JunctionTag.isRoundabout(connected)
                        && !this.isExcludedHighway(connected));
    }

    /**
     * This method returns a RoundaboutDirection enum which indicates direction of the flow of
     * traffic based on the cross product of two adjacent edges. This method leverages the
     * right-hand rule as it relates to the directionality of two vectors.
     *
     * @see "https://en.wikipedia.org/wiki/Right-hand_rule"
     * @param roundaboutEdges
     *            A list of Edges in a roundabout
     * @return CLOCKWISE or COUNTERCLOCKWISE if the majority of the edges have positive or negative
     *         cross products respectively, and UNKNOWN if all edge cross products are 0 or if the
     *         roundabout's geometry is malformed
     */
    private static RoundaboutDirection findRoundaboutDirection(final Route roundaboutEdges)
    {
        int clockwiseCount = 0;
        int counterClockwiseCount = 0;

        for (int idx = 0; idx < roundaboutEdges.size(); idx++)
        {
            // Get the Edges to use in the cross product
            final Edge edge1 = roundaboutEdges.get(idx);
            // We mod the roundabout edges here so that we can get the last pair of edges in the
            // Roundabout correctly
            final Edge edge2 = roundaboutEdges.get((idx + 1) % roundaboutEdges.size());
            // Get the cross product and then the direction of the roundabout
            final double crossProduct = getCrossProduct(edge1, edge2);
            final RoundaboutDirection direction = crossProduct < 0
                    ? RoundaboutDirection.COUNTERCLOCKWISE
                    : (crossProduct > 0) ? RoundaboutDirection.CLOCKWISE
                            : RoundaboutDirection.UNKNOWN;

            // If the direction is UNKNOWN then we continue to the next iteration because we do not
            // Have any new information about the roundabout's direction
            if (direction.equals(RoundaboutDirection.UNKNOWN))
            {
                continue;
            }
            if (direction.equals(RoundaboutDirection.CLOCKWISE))
            {
                clockwiseCount += 1;
            }
            if (direction.equals(RoundaboutDirection.COUNTERCLOCKWISE))
            {
                counterClockwiseCount += 1;
            }
        }
        // Return the Enum for whatever has the highest count
        return clockwiseCount > counterClockwiseCount ? RoundaboutDirection.CLOCKWISE
                : clockwiseCount < counterClockwiseCount ? RoundaboutDirection.COUNTERCLOCKWISE
                        : RoundaboutDirection.UNKNOWN;
    }

    /**
     * This method returns the cross product between two adjacent edges.
     *
     * @see "https://en.wikipedia.org/wiki/Cross_product"
     * @param edge1
     *            An Edge entity in the roundabout
     * @param edge2
     *            An Edge entity in the roundabout adjacent to edge1
     * @return A double corresponding to the cross product between two edges
     */
    private static Double getCrossProduct(final Edge edge1, final Edge edge2)
    {
        // Get the nodes' latitudes and longitudes to use in deriving the vectors
        final double node1Y = edge1.start().getLocation().getLatitude().asDegrees();
        final double node1X = edge1.start().getLocation().getLongitude().asDegrees();
        final double node2Y = edge1.end().getLocation().getLatitude().asDegrees();
        final double node2X = edge1.end().getLocation().getLongitude().asDegrees();
        final double node3Y = edge2.end().getLocation().getLatitude().asDegrees();
        final double node3X = edge2.end().getLocation().getLongitude().asDegrees();

        // Get the vectors from node 2 to 1, and node 2 to 3
        final double vector1X = node2X - node1X;
        final double vector1Y = node2Y - node1Y;
        final double vector2X = node2X - node3X;
        final double vector2Y = node2Y - node3Y;

        // The cross product tells us the direction of the orthogonal vector, which is
        // Directly related to the direction of rotation/traffic
        return (vector1X * vector2Y) - (vector1Y * vector2X);
    }

    /**
     * Checks if an {@link Edge} has values that indicate it is crossable by another edge.
     *
     * @param edge
     *            {@link Edge} to be checked
     * @return true if it is a bridge or tunnel, or has a layer tag
     */
    private boolean ignoreCrossings(final Edge edge)
    {
        return Validators.hasValuesFor(edge, LayerTag.class) || BridgeTag.isBridge(edge)
                || TunnelTag.isTunnel(edge);
    }

    /**
     * Checks for roads that should not be inside a roundabout. Such roads are car navigable, not a
     * roundabout, bridge, or tunnel, don't have a layer tag, and have geometry inside the
     * roundabout.
     *
     * @param roundabout
     *            A roundabout as a {@link Route}
     * @return true if there is a road that is crossing and has geometry enclosed by the roundabout
     */
    private boolean roundaboutEnclosesRoads(final Route roundabout)
    {
        final Polygon roundaboutPoly = new Polygon(roundabout.asPolyLine());
        return roundabout.start().getAtlas()
                .edgesIntersecting(roundaboutPoly,
                        edge -> HighwayTag.isCarNavigableHighway(edge)
                                && !JunctionTag.isRoundabout(edge) && !this.ignoreCrossings(edge)
                                && this.intersectsWithEnclosedGeometry(roundaboutPoly, edge))
                .iterator().hasNext();
    }

    /**
     * Checks if an {@link Edge} intersects a {@link Polygon} and is either fully inside it or
     * intersecting at a point other than one of its {@link Node}s.
     *
     * @param polygon
     *            {@link Polygon} to check against
     * @param edge
     *            the {@link Edge} to check
     * @return true if some part of the {@link Edge} is inside the {@link Polygon} and doesn't just
     *         have touching {@link Node}s.
     */
    private boolean intersectsWithEnclosedGeometry(final Polygon polygon, final Edge edge)
    {
        final PolyLine polyline = edge.asPolyLine();
        return polygon.intersections(polyline).stream()
                .anyMatch(intersection -> !(edge.start().getLocation().equals(intersection)
                        || edge.end().getLocation().equals(intersection))
                        || polygon.fullyGeometricallyEncloses(polyline));
    }

    /**
     * Checks if an {@link AtlasObject} has a highway value that excludes it from this check. These
     * have been excluded because they commonly act differently from car navigable roundabouts.
     *
     * @param object
     * @return
     */
    private boolean isExcludedHighway(final AtlasObject object)
    {
        return Validators.isOfType(object, HighwayTag.class, HighwayTag.CYCLEWAY,
                HighwayTag.PEDESTRIAN, HighwayTag.FOOTWAY);
    }
}
