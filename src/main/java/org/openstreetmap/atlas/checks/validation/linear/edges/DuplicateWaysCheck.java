package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
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
    private static final String DUPLICATE_EDGE_INSTRUCTIONS = "This way, {0,number,#}, "
            + "has at least one duplicate segment.";

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(DUPLICATE_EDGE_INSTRUCTIONS);
    private static final Distance ZERO_DISTANCE = Distance.ZERO;

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
                // Check to see that the Edge is a master Edge
                && Edge.isMasterEdgeIdentifier(object.getIdentifier())
                // Check to see that the edge has not already been seen
                && !this.isFlagged(object.getIdentifier())
                // Check to see that the edge is car navigable
                && HighwayTag.isCarNavigableHighway(object)
                // The edge is not part of an area
                && !object.getTag(AreaTag.KEY).isPresent();
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        final PolyLine edgePoly = edge.asPolyLine();

        final Rectangle bounds = edge.asPolyLine().bounds();
        // Get Edges which are contained by or intersect the bounds, and then filter
        // Out the non-master Edges as the bounds Edges are not guaranteed to be uni-directional
        final Iterable<Edge> edgesInBounds = edge.getAtlas().edgesIntersecting(bounds,
                Edge::isMasterEdge);

        for (final Edge edgeInBounds : edgesInBounds)
        {
            // If the Edge found in the bounds has an area tag or if the Edge has a length of 0
            // Or if the Edge has already been flagged before or if the Edge in bounds is the
            // Edge that the bounds was drawn with then continue because we don't want to
            // Flag area Edges, duplicate Nodes, already flagged Edges, or the Edge of interest
            if (edgeInBounds.getTag(AreaTag.KEY).isPresent()
                    || edgeInBounds.asPolyLine().length().equals(ZERO_DISTANCE)
                    || this.isFlagged(edgeInBounds.getIdentifier())
                    || edge.getIdentifier() == edgeInBounds.getIdentifier())
            {
                continue;
            }

            final PolyLine edgeInBoundsPoly = edgeInBounds.asPolyLine();

            // Getting the longerEdge and subsetEdge are necessary as .overlapShapeOf() checks that
            // the longerEdge is at least the same shape as the subsetEdge. If we were to call this
            // method on the subsetEdge with the longerEdge as the parameter, it would return False.
            final PolyLine longerEdge = (edgePoly.length().isGreaterThan(edgeInBoundsPoly.length()))
                    ? edgePoly : edgeInBoundsPoly;
            final PolyLine subsetEdge = (longerEdge == edgePoly) ? edgeInBoundsPoly : edgePoly;

            // Checks that either the edgeInBounds PolyLine overlaps the edgePoly or
            // That the edgePoly overlaps the edgeInBoundsPoly
            if (longerEdge.overlapsShapeOf(subsetEdge))
            {
                this.markAsFlagged(edgeInBounds.getIdentifier());
                return Optional.of(this.createFlag(edgeInBounds,
                        this.getLocalizedInstruction(0, edgeInBounds.getOsmIdentifier())));
            }
        }
        return Optional.empty();
    }
}
