package org.openstreetmap.atlas.checks.utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author srachanski
 */
public class RelationIntersections
{
    
    private final Map<Relation, Map<Long, Set<LineItem>>> intersections = new HashMap<>();
    
    public Set<Relation> getRelations()
    {
        return this.intersections.keySet();
    }
    
    public Map<Long, Set<LineItem>> getLineItemMap(final Relation relation)
    {
        return this.intersections.get(relation);
    }
    
    public void addIntersection(final Relation relation, final LineItem lineItem)
    {
        this.intersections.computeIfAbsent(relation, k -> new HashMap<>());
        final long osmIdentifier = lineItem.getOsmIdentifier();
        this.intersections.get(relation).computeIfAbsent(osmIdentifier, k -> new HashSet<>());
        this.intersections.get(relation).get(osmIdentifier).add(lineItem);
    }
    
}
