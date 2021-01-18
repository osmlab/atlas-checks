package org.openstreetmap.atlas.checks.utility;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;

/**
 * Hold common Methods (should be used in more than one check)
 *
 * @author Vladimir Lemberg
 */
public final class CommonMethods
{
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
        // Build original OSM polyline
        PolyLine geometry = new PolyLine(sortedEdges.get(0).getRawGeometry());
        for (int index = 1; index < sortedEdges.size(); index++)
        {
            geometry = geometry.append(sortedEdges.get(index).asPolyLine());
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
                return String.valueOf("PointNode")
                        .concat(String.valueOf(entity.getOsmIdentifier()));
            }
            return entity.getType().toString().concat(String.valueOf(entity.getOsmIdentifier()));
        }).distinct().count();
    }

    private CommonMethods()
    {
        // constructor
    }
}
