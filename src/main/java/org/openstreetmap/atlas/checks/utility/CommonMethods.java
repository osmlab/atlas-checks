package org.openstreetmap.atlas.checks.utility;

import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;

/**
 * Hold common Methods (should be used in more than one check)
 *
 * @author Vladimir Lemnberg
 */
public class CommonMethods
{

    /**
     * Return OSM Relation Members size excluding Atlas reversed and sectioned Edges
     */
    public static long getOSMRelationMemberSize(final Relation relation)
    {
        return relation
                .members().stream().map(RelationMember::getEntity).map(entity -> entity.getType()
                        .toString().concat(String.valueOf(entity.getOsmIdentifier())))
                .distinct().count();
    }
}
