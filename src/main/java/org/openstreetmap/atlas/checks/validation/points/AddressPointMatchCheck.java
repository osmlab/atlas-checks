package org.openstreetmap.atlas.checks.validation.points;


import com.google.common.collect.Iterables;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import java.util.*;

public class AddressPointMatchCheck extends BaseCheck
{
    private static final long serialVersionUID = 1L;
    public static final String NO_STREET_NAME_INSTRUCTIONS = "This node, {0,number,#}, has "
            + "no street name specified in the address. The street name should likely "
            + "be one of {1}.";
    public static final String NO_SUGGESTED_NAMES_INSTRUCTIONS = "This node, {0,number,#}, has "
            + "no street name specified in the address. No suggestions names were found.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(NO_STREET_NAME_INSTRUCTIONS, NO_SUGGESTED_NAMES_INSTRUCTIONS);
    private static final String ADDRESS_STREET_NUMBER_KEY = "addr:housenumber";
    private static final String STREET_NAME_KEY = "addr:street";
    private static final double BOUNDS_SIZE_DEFAULT = 25.0;

    private final Distance boundsSize;

    @Override protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    public AddressPointMatchCheck(final Configuration configuration)
    {
        super(configuration);
        this.boundsSize = Distance.meters((Double) configurationValue(configuration,
                "bounds.size", BOUNDS_SIZE_DEFAULT));
    }

    @Override public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Point
                && !object.getTags().containsKey(ADDRESS_STREET_NUMBER_KEY);
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

        Iterable<Node> interiorNodes = node.getAtlas().nodesWithin(box);
        Iterable<Edge> interiorEdges = node.getAtlas().edgesIntersecting(box);
        List<String> streetNameList = new ArrayList<>();

        // If there are no nodes or edges in the bounding box
        if (Iterables.isEmpty(interiorEdges) && Iterables.isEmpty(interiorEdges)) {
            // Flag node with instruction indicating that there are are no suggestions
            return Optional.of(this.createFlag(node, this.getLocalizedInstruction(1,
                    node.getOsmIdentifier()));
        }
        else if (!Iterables.isEmpty(interiorNodes))
        {
            interiorNodes.forEach(interiorNode ->
                    streetNameList.add((interiorNode.getTags().get(STREET_NAME_KEY))));

        }
        else if (!Iterables.isEmpty(interiorEdges)){
            interiorEdges.forEach(interiorEdge ->
                    streetNameList.add((interiorEdge.getTags().get(STREET_NAME_KEY))));
        }


            return Optional.of(this.createFlag(node, this.getLocalizedInstruction(0,
                    node.getOsmIdentifier(), streetNameList)));
        }
    }
}