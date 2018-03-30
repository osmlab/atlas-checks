package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
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
        final Set<Segment> allEdgeSegments = new HashSet<>();
        final Edge edge = (Edge) object;

        final Rectangle bounds = edge.asPolyLine().bounds();
        final Iterable<Edge> edgesInBounds = edge.getAtlas().edgesIntersecting(bounds);
        edgesInBounds.forEach(edgeInBounds -> edgeInBounds.getMasterEdge());

        for (final Edge edgeInBounds : edgesInBounds)
        {
            // If the Edge found in the bounds has an area tag or if the Edge has a length of 0
            // Or if the Edge has already been flagged before then continue because we don't want to
            // Flag area Edges or duplicate Nodes
            if (edgeInBounds.getTags().containsKey(AREA_KEY)
                    || edgeInBounds.asPolyLine().length().equals(Distance.meters(ZERO_LENGTH))
                    || this.isFlagged(edgeInBounds.getMasterEdgeIdentifier()))
            {
                continue;
            }

            // If the Set of Edges does not contain the Edge found in the bounds
            if (!allEdgeSegments.containsAll(edgeInBounds.asPolyLine().segments()))
            {
                final List<Segment> edgeInBoundsSegments = edgeInBounds.asPolyLine().segments();

                for (final Segment segment : edgeInBoundsSegments)
                {
                    // If a Segment in the Edge is found (check for partial duplication)
                    if (allEdgeSegments.contains(segment))
                    {
                        this.markAsFlagged(edgeInBounds.getMasterEdgeIdentifier());
                        return Optional.of(this.createFlag(edgeInBounds,
                                this.getLocalizedInstruction(0, edgeInBounds.getOsmIdentifier())));
                    }
                }
                // Add all Segments flattened to the Set of Segments
                allEdgeSegments.addAll(edgeInBounds.asPolyLine().segments());
            }
            // A full duplicate Edge was found in the Set of allEdgePolyLines
            else
            {
                this.markAsFlagged(edgeInBounds.getMasterEdgeIdentifier());
                return Optional.of(this.createFlag(edgeInBounds,
                        this.getLocalizedInstruction(0, edgeInBounds.getOsmIdentifier())));
            }
        }
        return Optional.empty();
    }
}
