package org.openstreetmap.atlas.checks.validation.points;

import com.google.common.collect.Iterables;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import java.util.*;
import java.util.stream.Collectors;

public class AddressPointMatchCheck extends BaseCheck
{
    private static final long serialVersionUID = 1L;
    public static final String WRONG_STREET_NAME_INSTRUCTIONS = "This node, {0,number,#}, has the "
            + "incorrect street name tagged in the address. The street name should be {1}.";
    public static final String NO_STREET_NAME_INSTRUCTIONS = "This node, {0,number,#}, has no "
            + "street name tagged in the address. The street name should be {1}.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(WRONG_STREET_NAME_INSTRUCTIONS, NO_STREET_NAME_INSTRUCTIONS);
    private static final String ADDRESS_STREET_KEY = "addr:street";
    private static final int BOUNDS_DISTANCE_DEFAULT = 5;

    private final double boundsDistance;

    @Override protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    public AddressPointMatchCheck(final Configuration configuration)
    {
        super(configuration);
        this.boundsDistance = (double) configurationValue(configuration, "bounds.distance",
                BOUNDS_DISTANCE_DEFAULT);
    }

    @Override public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Node;
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Node node = (Node) object;
        final Rectangle box = node.getLocation().boxAround(Distance.meters(this.boundsDistance));
        Node closestNode;
        Edge closestEdge;

        if (!Iterables.isEmpty(node.getAtlas().nodesWithin(box))) {
            closestNode = getClosestNode(node, box);
        }

        if (!Iterables.isEmpty(node.getAtlas().edgesIntersecting(box))) {
            closestEdge = getClosestEdge(node, box);
        }




    }

    protected String getStreetName(Node node)
    {

    }

    protected Node getClosestNode(Node node, Rectangle bounds)
    {
        final Map<Node, Double> nodeDistances = new HashMap<>();

        // Gets all nodes within the bounds
        final Iterable<Node> interiorNodes = node.getAtlas().nodesWithin(bounds);

        //
        if (Iterables.isEmpty(interiorNodes))
        {
            for (Node interiorNode : interiorNodes)
            {
                if (!interiorNode.getTags().containsKey(ADDRESS_STREET_KEY)) {
                    continue;
                }
                PolyLine nodesAsPoly = new PolyLine(node.getLocation(), interiorNode.getLocation());
                double distance = nodesAsPoly.length().asMeters();

                nodeDistances.put(interiorNode, distance);
            }
        }

        // return the key with minimum value in map
    }

    protected Edge getClosestEdge(Node node, Rectangle bounds) {

    }
}