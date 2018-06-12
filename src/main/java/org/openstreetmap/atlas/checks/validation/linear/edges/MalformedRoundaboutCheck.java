package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags roundabouts where the directionality is opposite to what it should be, where the
 * roundabout is multi-directional, or where the roundabout has incorrect geometry (concave).
 *
 * @author savannahostrowski
 */

public class MalformedRoundaboutCheck extends BaseCheck
{
    private static final long serialVersionUID = -3018101860747289836L;
    private static final String WRONG_WAY_INSTRUCTIONS = "This roundabout, {0,number,#}, is going the"
            + " wrong direction, or has been improperly tagged as a roundabout.";
    private static final String MULTIDIRECTIONAL_INSTRUCTIONS = "This roundabout, {0,number,#}, is"
            + " multi-directional, or the roundabout has improper angle geometry.";
    private static final List<String> LEFT_DRIVING_COUNTRIES_DEFAULT = Arrays.asList("AIA", "ATG",
            "AUS", "BGD", "BHS", "BMU", "BRB", "BRN", "BTN", "BWA", "CCK", "COK", "CXR", "CYM",
            "CYP", "DMA", "FJI", "FLK", "GBR", "GGY", "GRD", "GUY", "HKG", "IDN", "IMN", "IND",
            "IRL", "JAM", "JEY", "JPN", "KEN", "KIR", "KNA", "LCA", "LKA", "LSO", "MAC", "MDV",
            "MLT", "MOZ", "MSR", "MUS", "MWI", "MYS", "NAM", "NFK", "NIU", "NPL", "NRU", "NZL",
            "PAK", "PCN", "PNG", "SGP", "SGS", "SHN", "SLB", "SUR", "SWZ", "SYC", "TCA", "THA",
            "TKL", "TLS", "TON", "TTO", "TUV", "TZA", "UGA", "VCT", "VGB", "VIR", "WSM", "ZAF",
            "ZMB", "ZWE");
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(WRONG_WAY_INSTRUCTIONS,
            MULTIDIRECTIONAL_INSTRUCTIONS);

    private List<String> leftDrivingCountries;

    /**
     * An enum of RoundaboutDirections
     */
    public enum RoundaboutDirection
    {
        CLOCKWISE,
        COUNTERCLOCKWISE,
        // Handles the case where multiple directions were found in the roundabout
        MULTIDIRECTIONAL,
        // Handles the case where we were unable to get any information about the roundabout's
        // Direction or if the roundabout's geometry was malformed (concave).
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

        // If the roundabout is found to be going in multiple directions
        if (direction.equals(RoundaboutDirection.MULTIDIRECTIONAL))
        {
            return Optional.of(this.createFlag(new HashSet<>(roundaboutEdges),
                    this.getLocalizedInstruction(1, edge.getOsmIdentifier())));
        }

        // Determine if the roundabout is in a left or right driving country
        final boolean isLeftDriving = leftDrivingCountries.contains(isoCountryCode);

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

            // Get the edges connected to the edge as an iterator
            final Set<Edge> connectedEdges = currentEdge.connectedEdges();

            for (final Edge connectedEdge : connectedEdges)
            {
                if (JunctionTag.isRoundabout(connectedEdge)
                        && !roundaboutEdges.contains(connectedEdge))
                {
                    this.markAsFlagged(connectedEdge.getIdentifier());
                    queue.add(connectedEdge);
                }
            }
        }
        roundaboutEdges.sort(Edge::compareTo);
        return roundaboutEdges;
    }

    /**
     * This method returns a RoundaboutDirection enum which indicates direction of the flow of
     * traffic based on the cross product of two adjacent edges. This method leverages the
     * right-hand rule as it relates to the directionality of two vectors.
     *
     * @see "https://en.wikipedia.org/wiki/Right-hand_rule"
     * @param roundaboutEdges
     *            A list of Edges in a roundabout
     * @return CLOCKWISE or COUNTERCLOCKWISE if all the edges have positive or negative cross
     *         products respectively, MULTIDIRECTIONAL if multiple directions are found in the same
     *         roundabout, and UNKNOWN if all edge cross products are 0 or if the roundabout's
     *         geometry is malformed
     */
    private static RoundaboutDirection findRoundaboutDirection(final List<Edge> roundaboutEdges)
    {
        // Initialize the directionSoFar to UNKNOWN as we have no directional information yet
        RoundaboutDirection directionSoFar = RoundaboutDirection.UNKNOWN;

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

            // If the directionSoFar is UNKNOWN, and the direction derived from the current pair
            // Of edges is not UNKNOWN, make the directionSoFar equal to the current pair direction
            if (directionSoFar.equals(RoundaboutDirection.UNKNOWN))
            {
                directionSoFar = direction;
            }
            // Otherwise, if the directionSoFar and the direction are not equal, we know that the
            // Roundabout has segments going in different directions
            else if (!directionSoFar.equals(direction))
            {
                return RoundaboutDirection.MULTIDIRECTIONAL;
            }
        }
        return directionSoFar;
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
}
