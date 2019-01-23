package org.openstreetmap.atlas.checks.validation.areas;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.EdgeWalker;

/**
 * Simple walker to grab all connected edges which have a bad combination of area=yes and some
 * highway tag.
 *
 * @author nachtm
 */
public class AreasWithHighwayTagCheckWalker extends EdgeWalker
{

    private static final Predicate<Edge> IS_BAD_EDGE_WITH_AREA_TAG = edge -> AreasWithHighwayTagCheck
            .isUnacceptableAreaHighwayTagCombination(edge, edge.highwayTag());

    private static final Function<Edge, Stream<Edge>> GET_NEIGHBOR_STREAM = edge -> edge
            .connectedEdges().stream();

    public AreasWithHighwayTagCheckWalker(final Edge startingEdge)
    {
        super(startingEdge, IS_BAD_EDGE_WITH_AREA_TAG, GET_NEIGHBOR_STREAM);
    }
}
