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

        final Iterable<Node> interiorNodes = node.getAtlas().nodesWithin(box);

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
                    if (closestNode == null || closestNodeDistance > betweenNodeDistance)
                    {
                        closestNode = interiorNode;
                        closestNodeDistance = betweenNodeDistance;

                    }
                }
            }
        }
        if (closestNode == null)
        {

        }
    }

    protected Optional<String> getStreetName(AtlasObject closestFeature)
    {
        return closestFeature.getTag(ADDRESS_STREET_KEY);
    }

}