package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check looks for partially or completely duplicated Ways via Edges.
 *
 * @author savannahostrowski
 */
public class DuplicateWaysCheck extends BaseCheck
{
    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;
    public static final String DUPLICATE_EDGE_INSTRUCTIONS = "This way, {0,number,#}, "
            + "has at least one duplicate segment. ";

    public static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(DUPLICATE_EDGE_INSTRUCTIONS);
    public static final int ZERO_LENGTH = 0;
    public static final String AREA_KEY = AreaTag.KEY;

    // A map of segments and list of Edge identifiers
    private final Map<Segment, Set<Long>> globalSegments = new HashMap<>();

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    public DuplicateWaysCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge
                // Check to see that the edge is car navigable
                && HighwayTag.isCarNavigableHighway(object)
                // The edge is not part of an area
                && !object.getTags().containsKey(AREA_KEY)
                // The edge has not already been seen
                && !this.isFlagged(((Edge) object).getMasterEdgeIdentifier());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {

        // Get current edge object
        final Edge edge = (Edge) object;

        // Get the edge identifier
        final long identifier = edge.getIdentifier();

        // Get all Segments in Edge
        final List<Segment> edgeSegments = edge.asPolyLine().segments();

        // For each Segment in the Edge
        for (final Segment segment : edgeSegments)
        {
            // Make sure that we aren't flagging duplicate nodes
            if (!segment.length().isGreaterThan(Distance.meters(ZERO_LENGTH)))
            {
                continue;
            }

            // Check if the Segment is in globalSegments
            if (globalSegments.containsKey(segment))
            {
                // Add identifier to the list of identifiers with that segment
                globalSegments.get(segment).add(identifier);

                if (!this.isFlagged(edge.getMasterEdgeIdentifier()))
                {
                    this.markAsFlagged(edge.getMasterEdgeIdentifier());
                    return Optional.of(this.createFlag(edge,
                            this.getLocalizedInstruction(0, edge.getOsmIdentifier())));
                }
            }
            else
            {
                // If it doesn't already exist, then add the segment and list with one identifier
                final Set<Long> identifiers = new HashSet<>();
                identifiers.add(identifier);
                globalSegments.put(segment, identifiers);
            }
        }
        return Optional.empty();
    }
}
