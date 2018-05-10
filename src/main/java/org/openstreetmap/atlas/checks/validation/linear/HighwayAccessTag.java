package org.openstreetmap.atlas.checks.validation.linear;

import java.util.*;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 *
 * @author bbreithaupt
 */

/*bsb - test for edges and lines where it is a highway with a higher priority than service, access=no, and it has connecting edges or lines that are also highway.
* Do the same test for access=private, but also must not have node with gate tag.*/

public class HighwayAccessTag extends BaseCheck
{

    private static final HighwayTag MINIMUM_HIGHWAY_PRIORITY_DEFAULT = HighwayTag.SERVICES;
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Check if the access tag needs to be removed."
    );

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
    public HighwayAccessTag(final Configuration configuration)
    {
        super(configuration);
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH,
        // Distance::meters);
        this.minimumHighwayPriority = (HighwayTag) this.configurationValue(configuration, "highwayPriority.minimum",MINIMUM_HIGHWAY_PRIORITY_DEFAULT);
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
        if ((object instanceof Edge) || (object instanceof Line)){
            final Optional<HighwayTag> result = HighwayTag.highwayTag(object);
            if (result.isPresent())
            {
                return (AccessTag.isNo(object)) || (AccessTag.isPrivate(object));
            }
            else
            {
                return false;
            }
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
        // insert algorithmic check to see whether object needs to be flagged.
        // Example of flagging an object
        // return Optional.of(this.createFlag(object, "Instruction how to fix issue or reason behind
        // flagging the object");


        //get start and end nodes, as Location, of object (maybe use asPolyline to get Location)
        //get all lineItemsContaining for each of those locations.
        //check if start and end of each lineString is the same as one of objects'.
        //if so, return as connected lineString

        //check if there are > 1 connected LineItems using getConnectedLineItems(), to see if not at end of a road network.
        //handle edge case where object has > 1 connected LineItems all at one end.

        getConnectedLineItems((LineItem) object);

    }

    private static Iterable<LineItem> getConnectedLineItems(LineItem object){
        final Location first = object.asPolyLine().first();
        final Location last = object.asPolyLine().last();

        final Iterable<LineItem> lineItems = new MultiIterable<LineItem>(object.getAtlas().lineItemsContaining(first),object.getAtlas().lineItemsContaining(last));
        Iterable<LineItem> connectedLineItems = new ArrayList<LineItem>();

        for (LineItem lineItem: lineItems){
            if (lineItem.asPolyLine().first().equals(first) || lineItem.asPolyLine().first().equals(last) || lineItem.asPolyLine().last().equals(first) || lineItem.asPolyLine().last().equals(last)){
                ((ArrayList<LineItem>) connectedLineItems).add(lineItem);
            }
        }

        return connectedLineItems;
    }
}
