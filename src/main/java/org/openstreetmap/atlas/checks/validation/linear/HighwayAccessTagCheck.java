package org.openstreetmap.atlas.checks.validation.linear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 *
 * @author bbreithaupt
 */

public class HighwayAccessTagCheck extends BaseCheck
{

    private static final String MINIMUM_HIGHWAY_PRIORITY_DEFAULT = HighwayTag.SERVICE.toString();
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Check if the access tag of way {0,number,#} needs to be removed.");

    private final HighwayTag minimumHighwayPriority;

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public HighwayAccessTagCheck(final Configuration configuration)
    {
        super(configuration);
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH,
        // Distance::meters);
        final String highwayType = (String) this.configurationValue(configuration,
                "minimum.highway.type", MINIMUM_HIGHWAY_PRIORITY_DEFAULT);
        this.minimumHighwayPriority = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        if ((object instanceof Edge) || (object instanceof Line) && this.isMinimumHighway(object)
                && (AccessTag.isNo(object)) || (AccessTag.isPrivate(object)))
        {
            return true;
        }
        return false;
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
        final LineItem lineItem = (LineItem) object;
        final HashMap<String, ArrayList<LineItem>> connectedHighways = this
                .getConnectedHighways(lineItem);
        if (AccessTag.isNo(object) && connectedHighways.get("first").size() > 0
                && connectedHighways.get("last").size() > 0)
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        else if (AccessTag.isPrivate(object) && connectedHighways.get("first").size() > 0
                && connectedHighways.get("last").size() > 0 && !hasGate((LineItem) object))
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    private HashMap<String, ArrayList<LineItem>> getConnectedHighways(final LineItem object)
    {
        final Location first = object.asPolyLine().first();
        final Location last = object.asPolyLine().last();

        /*
         * final HashMap<String, Iterable<LineItem>> lineItemArrays = new HashMap<>();
         * lineItemArrays.put("first", object.getAtlas().lineItemsContaining(first));
         * lineItemArrays.put("last", object.getAtlas().lineItemsContaining(last));
         */

        final Iterable<LineItem> lineItemArrays = new MultiIterable<>(
                object.getAtlas().lineItemsContaining(first),
                object.getAtlas().lineItemsContaining(last));

        final HashMap<String, ArrayList<LineItem>> connectedLineItems = new HashMap<>();
        connectedLineItems.put("first", new ArrayList<>());
        connectedLineItems.put("last", new ArrayList<>());

        for (final LineItem lineItem : lineItemArrays)
        {
            if (!lineItem.equals(object) && this.isMinimumHighway(lineItem)
                    && (lineItem.asPolyLine().first().equals(first)
                            || lineItem.asPolyLine().last().equals(first)))
            {
                connectedLineItems.get("first").add(lineItem);
            }
            else if (!lineItem.equals(object) && (lineItem.asPolyLine().first().equals(last)
                    || lineItem.asPolyLine().last().equals(last)))
            {
                connectedLineItems.get("last").add(lineItem);
            }
        }

        return connectedLineItems;
    }

    private boolean hasGate(final LineItem object)
    {
        for (final Node node : getLineItemNodes(object))
        {
            if (node.getOsmTags().containsValue("gate"))
            {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Node> getLineItemNodes(final LineItem object)
    {
        final ArrayList<Node> lineItemNodes = new ArrayList<>();

        for (final Object location : object.asPolyLine().toArray())
        {
            lineItemNodes.addAll(
                    (Collection<? extends Node>) object.getAtlas().nodesAt((Location) location));
        }

        return lineItemNodes;
    }

    private boolean isMinimumHighway(final AtlasObject object)
    {
        final Optional<HighwayTag> result = HighwayTag.highwayTag(object);
        if (result.isPresent())
        {
            return result.get().isMoreImportantThanOrEqualTo(this.minimumHighwayPriority);
        }
        else
        {
            return false;
        }
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
