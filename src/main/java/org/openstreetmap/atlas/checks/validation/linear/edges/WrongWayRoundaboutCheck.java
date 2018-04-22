package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.OneWayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags roundabouts where the directionality is opposite to what it should be.
 *
 * @author savannahostrowski
 */

public class WrongWayRoundaboutCheck extends BaseCheck
{
    private static final long serialVersionUID = -3018101860747289836L;
    public static final String WRONG_WAY_INSTRUCTIONS = "This roundabout, {0,number,#},"
            + " is going the wrong direction.";
    public static final String MULTIDIRECTIONAL_INSTRUCTIONS = "This roundabout, {0,number,#},"
            + " is going the wrong direction.";
    public static final Set<String> LEFT_DRIVING_COUNTRIES = new HashSet<>(
            Arrays.asList("AIA", "ATG", "AUS", "BHS", "BGD", "BRB", "BMU", "BTN", "BWA", "BRN",
                    "CYM", "CXR", "CCK", "COK", "CYP", "DMA", "FLK", "FJI", "GRD", "GGY", "GUY",
                    "HKG", "IND", "IDN", "IRL", "IMN", "JAM", "JPN", "JEY", "KEN", "KIR", "LSO",
                    "MAC", "MWI", "MYS", "MDV", "MLT", "MUS", "MSR", "MOZ", "NAM", "NRU", "NPL",
                    "NZL", "NIU", "NFK", "PAK", "PNG", "PCN", "SHN", "KNA", "LCA", "VCT", "WSM",
                    "SYC", "SGP", "SLB", "ZAF", "SGS", "LKA", "SUR", "SWZ", "TZA", "THA", "TKL",
                    "TON", "TTO", "TCA", "TUV", "UGA", "GBR", "VGB", "VIR", "ZMB", "ZWE"));
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            WRONG_WAY_INSTRUCTIONS, MULTIDIRECTIONAL_INSTRUCTIONS);

    // An enum to list out all the possibilities for roundabout direction
    public enum RoundaboutDirection
    {
        CLOCKWISE,
        COUNTERCLOCKWISE,
        MULTIDIRECTIONAL,
        UNKNOWN
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    public WrongWayRoundaboutCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // We check that the object is an instance of Edge
        return object instanceof Edge
                // Make sure that the edges are instances of roundabout
                && JunctionTag.isRoundabout(object)
                // Is not two-way
                && !OneWayTag.isExplicitlyTwoWay(object)
                // And that the Edge has not already been marked as flagged
                && !this.isFlagged(object.getIdentifier())
                // Make sure that we are only looking at master edges
                && ((Edge) object).isMasterEdge();
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
        final List<Edge> roundaboutEdges = getAllRoundaboutEdges(edge);

        // Get the direction of the roundabout
        final RoundaboutDirection direction = findRoundaboutDirection(roundaboutEdges);

        // Determine if the roundabout is in a left or right driving country
        final boolean isLeftDriving = LEFT_DRIVING_COUNTRIES.contains(isoCountryCode);

        // If the roundabout traffic is clockwise in a right-driving country, or
        // If the roundabout traffic is counterclockwise in a left-driving country
        if (direction.equals(RoundaboutDirection.CLOCKWISE) && !isLeftDriving
                || direction.equals(RoundaboutDirection.COUNTERCLOCKWISE) && isLeftDriving)
        {
            return Optional.of(this.createFlag(new HashSet<>(roundaboutEdges),
                    this.getLocalizedInstruction(0, edge.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    /**
     * This method gets all edges in a roundabout given one edge in that roundabout, in ascending
     * Edge identifier order.
     *
     * @param edge
     *            An Edge object known to be a roundabout edge
     * @return A list of edges in the roundabout
     */
    private List<Edge> getAllRoundaboutEdges(final Edge edge)
    {
        final List<Edge> roundaboutEdges = new ArrayList<>();

        // Initialize a queue to add yet to be processed connected edges to
        final Queue<Edge> queue = new LinkedList<>();

        // Mark the current Edge as visited and enqueue it
        this.markAsFlagged(edge.getIdentifier());
        queue.add(edge);

        // As long as the queue is not empty
        while (!queue.isEmpty())
        {
            // Dequeue a connected edge and add it to the roundaboutEdges
            final Edge currentEdge = queue.poll();

            roundaboutEdges.add(currentEdge);

            // Get the edges connected to the edge e as an iterator
            final Set<Edge> connectedEdges = currentEdge.connectedEdges();

            for (final Edge connectedEdge : connectedEdges)
            {
                final Long edgeId = connectedEdge.getIdentifier();

                if (JunctionTag.isRoundabout(connectedEdge)
                        && !roundaboutEdges.contains(connectedEdge))

                {
                    this.markAsFlagged(edgeId);
                    queue.add(connectedEdge);
                }
            }
        }
        roundaboutEdges.sort(Edge::compareTo);
        return roundaboutEdges;
    }

    /**
     * This method returns an roundaboutDirection enum which indicates direction of the flow of
     * traffic based on the cross product of two adjacent edges.
     *
     * @see "https://en.wikipedia.org/wiki/Cross_product"
     * @see "https://en.wikipedia.org/wiki/Right-hand_rule"
     * @param roundaboutEdges
     *            A list of Edges in a roundabout
     * @return CLOCKWISE if cross product > 0, COUNTERCLOCKWISE if cross product < 0, and
     *          UNKNOWN if cross product = 0
     */
    private static RoundaboutDirection findRoundaboutDirection (final List<Edge> roundaboutEdges)
    {
        RoundaboutDirection directionSoFar = RoundaboutDirection.UNKNOWN;

        for (int i = 0; i < roundaboutEdges.size(); i++) {
            // Get the Edges to use in the cross product
            final Edge edge1 = roundaboutEdges.get(i);
            final Edge edge2 = roundaboutEdges.get((i + 1) % roundaboutEdges.size());

            double crossProduct = getCrossProduct(edge1, edge2);
            RoundaboutDirection direction = crossProduct < 0 ? RoundaboutDirection.COUNTERCLOCKWISE :
                    (crossProduct > 0) ? RoundaboutDirection.CLOCKWISE : RoundaboutDirection.UNKNOWN;

            if(direction.equals(RoundaboutDirection.UNKNOWN)) {
                continue;
            }

            if (directionSoFar.equals(RoundaboutDirection.UNKNOWN)) {
                directionSoFar = direction;
            }
            else if (!directionSoFar.equals(direction)){
                return RoundaboutDirection.MULTIDIRECTIONAL;
            }
        }
        return directionSoFar;
    }

    private static Double getCrossProduct(Edge edge1, Edge edge2) {

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
}
