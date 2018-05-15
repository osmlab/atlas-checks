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
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LandUseTag;
import org.openstreetmap.atlas.tags.MotorVehicleTag;
import org.openstreetmap.atlas.tags.MotorcarTag;
import org.openstreetmap.atlas.tags.PublicServiceVehiclesTag;
import org.openstreetmap.atlas.tags.VehicleTag;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags {@link Edge}s and {@link Line}s that include the highway tag and an access tag
 * with a value of no. It checks for locations where this breaks a road network and does not have
 * supporting tags. Supporting tags declare what is or is not included in {@code access=no}. For
 * example a supporting tag of {@code public_transport=yes} would mean only public transport
 * vehicles are allowed.
 *
 * @author bbreithaupt
 */

public class HighwayAccessTagCheck extends BaseCheck
{

    private static final String MINIMUM_HIGHWAY_TYPE_DEFAULT = HighwayTag.RESIDENTIAL.toString();
    private static final List<String> DO_NOT_FLAG_IF_NO_DEFAULT = Arrays.asList(MotorVehicleTag.KEY,
            VehicleTag.KEY, MotorcarTag.KEY);
    // change string keys to tag class references once the classes are created
    private static final List<String> DO_NOT_FLAG_IF_YES_DEFAULT = Arrays.asList("public_transport",
            PublicServiceVehiclesTag.KEY, "bus", "emergency");
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Make proper adjustments to the access tag of way {0,number,#}, and associated tag combinations.");

    private final HighwayTag minimumHighwayType;
    private final List<String> doNotFlagIfNoKeys;
    private final List<String> doNotFlagIfYesKeys;

    private final String stringFirst = "first";
    private final String stringLast = "last";

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

        final String highwayType = (String) this.configurationValue(configuration,
                "minimum.highway.type", MINIMUM_HIGHWAY_TYPE_DEFAULT);
        this.minimumHighwayType = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());

        this.doNotFlagIfNoKeys = (List<String>) this.configurationValue(configuration,
                "do-not-flag.value.no.keys", DO_NOT_FLAG_IF_NO_DEFAULT);

        this.doNotFlagIfYesKeys = (List<String>) this.configurationValue(configuration,
                "do-not-flag.value.yes.keys", DO_NOT_FLAG_IF_YES_DEFAULT);
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
        return ((object instanceof Edge) || (object instanceof Line))
                && Edge.isMasterEdgeIdentifier(object.getIdentifier())
                && !this.isFlagged(object.getOsmIdentifier()) && AccessTag.isNo(object)
                && isMinimumHighway(object)
                && !hasKeyValueMatch(object, this.doNotFlagIfNoKeys, "NO", "yes")
                && !hasKeyValueMatch(object, this.doNotFlagIfYesKeys, "YES", "no");
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
        final HashMap<String, ArrayList<LineItem>> connectedHighways = getConnectedHighways(
                lineItem);
        if (connectedHighways.get(this.stringFirst).size() > 0
                && connectedHighways.get(this.stringLast).size() > 0
                && !isInMilitaryArea((LineItem) object))
        {
            this.markAsFlagged(object.getOsmIdentifier());
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    /**
     * This finds LineItems with the highway tag, that are connected to the original OSM way of the
     * input {@link LineItem}. These connected {@link LineItem}s are returned as a {@link HashMap}
     * that splits them based on which end of the OSM way they are connected to.
     *
     * @param object
     *            a {@link LineItem} for which the connected highways ar to be found
     * @return a {@link HashMap} containing keys first and last, each containing an
     *         {@link ArrayList} of LineItems that are the connected highways for the associated
     *         side of the original {@link LineItem}
     */
    private HashMap<String, ArrayList<LineItem>> getConnectedHighways(final LineItem object)
    {
        final Location first = object.asPolyLine().first();
        final Location last = object.asPolyLine().last();

        final Iterable<LineItem> lineItemArrays = new MultiIterable<>(
                object.getAtlas().lineItemsContaining(first),
                object.getAtlas().lineItemsContaining(last));

        final HashMap<String, ArrayList<LineItem>> connectedLineItems = new HashMap<>();
        connectedLineItems.put(this.stringFirst, new ArrayList<>());
        connectedLineItems.put(this.stringLast, new ArrayList<>());

        for (final LineItem lineItem : lineItemArrays)
        {
            if (Edge.isMasterEdgeIdentifier(object.getIdentifier())
                    && !(lineItem.getIdentifier() == object.getIdentifier())
                    && this.isMinimumHighway(lineItem)
                    && (lineItem.asPolyLine().first().equals(first)
                            || lineItem.asPolyLine().last().equals(first)))
            {
                if (getOsmId(lineItem.getIdentifier()) == getOsmId(object.getIdentifier()))
                {
                    final ArrayList<LineItem> haveRunList = new ArrayList<>();
                    haveRunList.add(object);
                    connectedLineItems.get(this.stringFirst)
                            .addAll(getConnectedHighways(lineItem, haveRunList)
                                    .get(this.stringFirst).contains(lineItem)
                                            ? getConnectedHighways(lineItem, haveRunList)
                                                    .get(this.stringLast)
                                            : getConnectedHighways(lineItem, haveRunList)
                                                    .get(this.stringFirst));
                }
                else
                {
                    connectedLineItems.get(this.stringFirst).add(lineItem);
                }
            }
            else if (Edge.isMasterEdgeIdentifier(object.getIdentifier())
                    && !(lineItem.getIdentifier() == object.getIdentifier())
                    && this.isMinimumHighway(lineItem)
                    && (lineItem.asPolyLine().first().equals(last)
                            || lineItem.asPolyLine().last().equals(last)))
            {
                connectedLineItems.get(this.stringLast).add(lineItem);
            }
        }

        return connectedLineItems;
    }

    /**
     * This finds LineItems with the highway tag, that are connected to the original OSM way of the
     * input {@link LineItem}. These connected {@link LineItem}s are returned as a {@link HashMap}
     * that splits them based on which end of the OSM way they are connected to. Items in the
     * {@code ignoreLineItems} {@link ArrayList} are not returned as connections. This is useful for
     * preventing infinite loops when recursing.
     *
     * @param object
     *            a {@link LineItem} for which the connected highways ar to be found
     * @param ignoreLineItems
     *            a list of {@link LineItem}s that are ignored if found.
     * @return a {@link HashMap} containing keys first and last, each containing an
     *         {@link ArrayList} of LineItems that are the connected highways for the associated
     *         side of the original {@link LineItem}
     */
    private HashMap<String, ArrayList<LineItem>> getConnectedHighways(final LineItem object,
            final ArrayList<LineItem> ignoreLineItems)
    {
        final Location first = object.asPolyLine().first();
        final Location last = object.asPolyLine().last();

        final Iterable<LineItem> lineItemArrays = new MultiIterable<>(
                object.getAtlas().lineItemsContaining(first),
                object.getAtlas().lineItemsContaining(last));

        final HashMap<String, ArrayList<LineItem>> connectedLineItems = new HashMap<>();
        connectedLineItems.put(this.stringFirst, new ArrayList<>());
        connectedLineItems.put(this.stringLast, new ArrayList<>());

        for (final LineItem lineItem : lineItemArrays)
        {
            if (!(lineItem.getIdentifier() == object.getIdentifier())
                    && this.isMinimumHighway(lineItem)
                    && (lineItem.asPolyLine().first().equals(first)
                            || lineItem.asPolyLine().last().equals(first)))
            {
                if (getOsmId(lineItem.getIdentifier()) == getOsmId(object.getIdentifier())
                        && !ignoreLineItems.contains(object))
                {
                    ignoreLineItems.add(lineItem);
                    connectedLineItems.get(this.stringFirst)
                            .addAll(getConnectedHighways(lineItem, ignoreLineItems)
                                    .get(this.stringFirst).contains(lineItem)
                                            ? getConnectedHighways(lineItem, ignoreLineItems)
                                                    .get(this.stringLast)
                                            : getConnectedHighways(lineItem, ignoreLineItems)
                                                    .get(this.stringFirst));
                }
                else
                {
                    connectedLineItems.get(this.stringFirst).add(lineItem);
                }
            }
            else if (!(lineItem.getIdentifier() == object.getIdentifier())
                    && this.isMinimumHighway(lineItem)
                    && (lineItem.asPolyLine().first().equals(last)
                            || lineItem.asPolyLine().last().equals(last)))
            {
                connectedLineItems.get(this.stringLast).add(lineItem);
            }
        }

        return connectedLineItems;
    }

    /**
     * Checks if {@link LineItem} is inside an {@link Area} with tag {@code landuse=MILITARY}.
     *
     * @param object
     *            {@link LineItem} to check
     * @return {@code true} if input {@link LineItem} is in an {@link Area} with tag
     *         {@code landuse=MILITARY}
     */
    private boolean isInMilitaryArea(final LineItem object)
    {
        for (final Node node : getLineItemNodes(object))
        {
            if (object.getAtlas()
                    .areasCovering(node.getLocation(),
                            area -> area.getOsmTags().getOrDefault(LandUseTag.KEY, "na")
                                    .toUpperCase().equals(LandUseTag.MILITARY.toString()))
                    .iterator().hasNext())
            {
                return true;
            }
        }
        for (final Relation relation : object.getAtlas().relations())
        {
            if (relation.intersects(new Polygon(
                    new MultiIterable<>(object.asPolyLine(), object.asPolyLine().reversed()))))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all {@link Node}s in a {@link LineItem} as an {@link ArrayList}.
     *
     * @param object
     *            {@link LineItem} to get the nodes from
     * @return {@link ArrayList} of nodes from the input {@link LineItem}
     */
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

    /**
     * Checks if an {@link AtlasObject} is of an equal or greater priority than the minimum. The
     * minimum is supplied as a configuration parameter, the default is {@code "tertiary"}.
     *
     * @param object
     *            an {@link AtlasObject}
     * @return {@code true} if this object is >= the minimum
     */
    private boolean isMinimumHighway(final AtlasObject object)
    {
        final Optional<HighwayTag> result = HighwayTag.highwayTag(object);
        return result.isPresent()
                && result.get().isMoreImportantThanOrEqualTo(this.minimumHighwayType);
    }

    /**
     * Gets the original OSM id from an atlas id.
     *
     * @param atlasId
     *            an atlas id as a {@code long}
     * @return the original OSM id
     */
    private long getOsmId(final long atlasId)
    {
        final long mill = 1000000;
        return atlasId / mill;
    }

    /**
     * Checks if any of the keys in the input list have a value that matches the input
     * {@code matchValue} for the input {@code object}.
     *
     * @param object
     *            the {@link LineItem} to be evaluated
     * @param keys
     *            a {@link List} of {@link String} keys to check the values of
     * @param matchValue
     *            a {@link String} to check the {@code keys} against
     * @param defaultValue
     *            a value to pass if a key does not exist for the input {@code object}, usually the
     *            inverse of {@code matchValue}
     * @return {@code true} if any key's value, in {@code keys}, is equal to {@code matchValue}
     */
    private boolean hasKeyValueMatch(final AtlasObject object, final List<String> keys,
            final String matchValue, final String defaultValue)
    {
        for (final String key : keys)
        {
            if (object.getOsmTags().getOrDefault(key, defaultValue).toUpperCase()
                    .equals(matchValue))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
