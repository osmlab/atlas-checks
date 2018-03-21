package org.openstreetmap.atlas.checks.validation.points;

import com.google.common.collect.Iterables;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    private static final double BOUNDS_SIZE_DEFAULT = 25.0;

    private final Distance boundsSize;

    @Override protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    public AddressPointMatchCheck(final Configuration configuration)
    {
        super(configuration);
        this.boundsSize = Distance.meters((Double) configurationValue(configuration, "bounds.size",
                BOUNDS_SIZE_DEFAULT));
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
        // Get a bounding box around the Node of interest
        final Rectangle box = node.getLocation().boxAround(boundsSize);
        double closestNodeDistance = Integer.MAX_VALUE;
        Node closestNode = null;

        // Try and find the closest Node to the Node of interest
        getClosestNode(node, box, closestNode, closestNodeDistance);




        // If we have found the closest Node to the Node of interest (so it exists)
        if (closestNode != null)
        {
            // Flag the node with the proper street name in the MapRoulette instructions
            return Optional.of(this.createFlag(node, this.getLocalizedInstruction(0,
                    node.getOsmIdentifier(), closestNode.getTag(ADDRESS_STREET_KEY))));
        }
        // If we haven't found a closest Node, then we need to check for streets that intersect
        // With our bounding box


        // If we haven't found an any Edges (or any Nodes), then flag the Node but state that we
        // are not certain of the proper street name for that Node. The proper street name should
        // be decided at the discretion of the editor
        return Optional.of(this.createFlag(node, this.getLocalizedInstruction(1,
                node.getOsmIdentifier())));
    }

    private Node getClosestNode(Node node, Rectangle boundingBox, Node closestNodeObj, double closestNodeDistanceVal) {
        final Iterable<Node> interiorNodes = node.getAtlas().nodesWithin(boundingBox);

        // If there are nodes within the bounding box (aside from the Node of interest
        if (!Iterables.isEmpty(interiorNodes))
        {
            // For each node in the bounding box
            for (Node interiorNode : interiorNodes)
            {
                // If node in the bounding box has a street name
                if (interiorNode.getTags().containsKey(ADDRESS_STREET_KEY))
                {
                    PolyLine nodesAsPolyline = new PolyLine(node.getLocation(),
                            interiorNode.getLocation());
                    double betweenNodeDistance = nodesAsPolyline.length().asMeters();

                    // If a interior Node has not yet been compared to the Node of interest
                    // Or if the interior Node to the Node of interest distance is shorter
                    // Than the closestNodeDistance
                    if (closestNodeObj == null || closestNodeDistanceVal > betweenNodeDistance)
                    {
                        closestNodeObj = interiorNode;
                        closestNodeDistanceVal = betweenNodeDistance;

                    }
                }
            }
        }
    }

}