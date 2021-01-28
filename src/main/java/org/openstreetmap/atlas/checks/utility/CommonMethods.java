package org.openstreetmap.atlas.checks.utility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.oneway.OneWayTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hold common Methods (should be used in more than one check)
 *
 * @author Vladimir Lemberg
 */
public final class CommonMethods
{
    private static final Logger logger = LoggerFactory.getLogger(CommonMethods.class);

    /**
     * Build original (before Atlas sectioning) OSW way geometry from all Main {@link Edge}s
     * sections
     *
     * @param edge
     *            entity to check
     * @return original Way geometry {@link PolyLine}
     */
    public static PolyLine buildOriginalOsmWayGeometry(final Edge edge)
    {
        // Identify and sort by IDs all sections of original OSM way
        final List<Edge> sortedEdges = new ArrayList<>(new OsmWayWalker(edge).collectEdges());

        // Build original OSM polyline.
        PolyLine geometry = null;

        try
        {
            geometry = OneWayTag.isOneWayReversed(edge)
                    ? new PolyLine(sortedEdges.get(0).getRawGeometry()).reversed()
                    : new PolyLine(sortedEdges.get(0).getRawGeometry());

            for (int index = 1; index < sortedEdges.size(); index++)
            {
                geometry = OneWayTag.isOneWayReversed(edge)
                        ? geometry.append(sortedEdges.get(index).asPolyLine().reversed())
                        : geometry.append(sortedEdges.get(index).asPolyLine());
            }
        }
        catch (final CoreException exception)
        {
            logger.warn("Unable to build geometry for edge {}({}): {}", edge.getIdentifier(),
                    edge.getOsmIdentifier(), exception.getMessage());
        }
        return geometry;
    }

    /**
     * Return OSM Relation Members size excluding Atlas reversed and sectioned Edges
     *
     * @param relation
     *            {@link Relation} to get the members of
     * @return A size of relations members as {@link Long}
     */
    public static long getOSMRelationMemberSize(final Relation relation)
    {
        return relation.members().stream().map(RelationMember::getEntity).map(entity ->
        {
            // De-duplicating either Point or Node with same OSM Id
            if (entity.getType().toString().matches("POINT|NODE"))
            {
                return "PointNode"
                        .concat(String.valueOf(entity.getOsmIdentifier()));
            }
            return entity.getType().toString().concat(String.valueOf(entity.getOsmIdentifier()));
        }).distinct().count();
    }

    /**
     * Check if given {@link Edge} is part of Closed Way. OSM wiki:
     * https://wiki.openstreetmap.org/wiki/Item:Q4669
     *
     * @param edge
     *            entity to check
     * @return {@code true} if edge is part of closed way.
     */
    public static boolean isClosedWay(final Edge edge)
    {
        final HashSet<Long> wayIds = new HashSet<>();
        Edge nextEdge = edge;
        // Loop through outgoing edges with the same OSM id
        while (nextEdge != null)
        {
            wayIds.add(nextEdge.getIdentifier());
            final List<Edge> nextEdgeList = Iterables.stream(nextEdge.outEdges())
                    .filter(Edge::isMainEdge)
                    .filter(outEdge -> outEdge.getOsmIdentifier() == edge.getOsmIdentifier())
                    .collectToList();
            nextEdge = nextEdgeList.isEmpty() ? null : nextEdgeList.get(0);
            // If original edge is found, the way is closed
            if (nextEdge != null && wayIds.contains(nextEdge.getIdentifier()))
            {
                return true;
            }
        }
        return false;
    }

    private CommonMethods()
    {
        // constructor
    }
}
