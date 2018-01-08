package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check will look for any edges that do not contain any incoming or outgoing edges. The
 * appearance on the map would be that of a road simply floating in the middle of nowhere. No way
 * for any navigation, no ability to enter the {@link Edge} (road) from any point and no way to exit
 * it. To resolve the issue a mapper would either remove the edge as invalid or connect it to a
 * connected set of edges.
 *
 * @author cuthbertm, gpogulsky
 */
public class FloatingEdgeCheck extends BaseCheck<Long>
{
    // The default value for the maximum length in kilometers for something to be considered a
    // floating road, anything larger will be ignored. This can be updated through configuration and
    // set prior to runtime with a custom value.
    public static final double DISTANCE_MAXIMUM_KILOMETERS_DEFAULT = 100;
    // The default value for the minimum length in meters for something to be considered a floating
    // road, anything smaller will be ignored. This can be updated through configuration and set
    // prior to runtime with a custom value.
    public static final double DISTANCE_MINIMUM_METERS_DEFAULT = 100;
    // create a simple instruction stating the Edge with the supplied OSM Identifier is floating.
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Way '{0,number,#}' is floating. Ie. has no incoming or outgoing ways.");
    private static final long serialVersionUID = -6867668561001117411L;
    // class variable to store the maximum distance for the floating road
    private final Distance maximumDistance;
    // class variable to store the minimum distance for the floating road
    private final Distance minimumDistance;

    /**
     * Default constructor defined by the {@link BaseCheck} required to instantiate the Check within
     * the Atlas Checks framework
     *
     * @param configuration
     *            the configuration supplied by the framework containing custom properties for the
     *            floating edge check
     */
    public FloatingEdgeCheck(final Configuration configuration)
    {
        super(configuration);

        // This will retrieve two values, minimum and maximum length. In the JSON configuration it
        // would look like this:
        // "FloatingEdgeCheck": {
        // "length": {
        // "maximum.kilometers": 100.0,
        // "minimum.meters": 100.0
        // }
        // }
        this.minimumDistance = configurationValue(configuration, "length.minimum.meters",
                DISTANCE_MINIMUM_METERS_DEFAULT, Distance::meters);
        this.maximumDistance = configurationValue(configuration, "length.maximum.kilometers",
                DISTANCE_MAXIMUM_KILOMETERS_DEFAULT, Distance::kilometers);
    }

    /**
     * Checks if the supplied object is of {@link ItemType} {@link Edge} and that it is the
     * MasterEdge and whether a car can navigate on the edge. So we would ignore any pedestrian
     * paths in this particular check. An {@link Edge} contains a master edge and a reserve edge,
     * unless it is a oneway edge in which case it will only contain the master edge. Either way we
     * want to ignore the reverse edge so that we don't produce duplicate flags for what is
     * essentially the same feature.
     *
     * @param object
     *            the {@link AtlasObject} you are checking
     * @return {@code true} if matches the restrictions described above
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // Consider navigable master edges
        return TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMasterEdge()
                && HighwayTag.isCarNavigableHighway(object);
    }

    /**
     * The primary function that will check to see if the current edge is a floating edge
     *
     * @param object
     *            the {@link AtlasObject} you are checking
     * @return an Optional {@link CheckFlag} that contains the problem object and instructions on
     *         how to fix it, or the reason the object was flagged
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        // Check the length of the edge and make sure that it is larger then the minimum distance
        // and shorter than the maximum distance. We also want to make sure it doesn't have any
        // connected edges and has not been cut on the border and contains a synthetic boundary tag.
        if (edge.length().isGreaterThanOrEqualTo(this.minimumDistance)
                && edge.length().isLessThanOrEqualTo(this.maximumDistance)
                && this.hasNoConnectedEdges(edge) && this.isNotOnSyntheticBoundary(edge))
        {
            // return a flag created using the object and the flag that was either defined in the
            // configuration or above.
            return Optional.of(this.createFlag(edge,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * This checks to see if the edge has any connected edges. One of the things it has to do is
     * check for reverse edges, as these are considered connected edges however in terms of a
     * floating edge they are considered the same edge.
     *
     * @param edge
     *            the edge you are checking
     * @return {@code true} if there are no connected edges, other than a reverse edge
     */
    private boolean hasNoConnectedEdges(final Edge edge)
    {
        // loop through all edges that are connected to the edge you are looking at
        for (final Edge connectedEdge : edge.connectedEdges())
        {
            // if edge is not null (ie. valid) and not the reverse edge then immediately return
            // false, and it can safely assumed that this particular edge is not a floating edge
            if (connectedEdge != null && !edge.isReversedEdge(connectedEdge))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * A {@link SyntheticBoundaryNodeTag} is a special tag that is placed on any tag that has been
     * cut on the border of the particular Atlas file or shard that you are processing. The cutting
     * process has the potential to create floating edges at the border that are in reality not
     * floating edges.
     *
     * @param edge
     *            the edge that is currently be processed
     * @return {@code true} if the edge contains a synthetic boundary tag
     */
    private boolean isNotOnSyntheticBoundary(final Edge edge)
    {
        return !(SyntheticBoundaryNodeTag.isBoundaryNode(edge.start())
                || SyntheticBoundaryNodeTag.isBoundaryNode(edge.end()));
    }
}
