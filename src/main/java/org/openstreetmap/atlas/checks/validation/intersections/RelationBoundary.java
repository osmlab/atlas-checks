package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author srachanski
 */
public class RelationBoundary
{
    
    private final Map<String, Relation> tagToRelation;
    private final Set<BoundaryPart> boundaryParts;
    
    public RelationBoundary(final Map<String, Relation> tagToRelation, final Set<BoundaryPart> boundaryParts)
    {
        this.tagToRelation = tagToRelation;
        this.boundaryParts = boundaryParts;
    }

    public boolean containsRelationId(final long osmIdentifier)
    {
        return this.tagToRelation.values()
                .stream()
                .map(Relation::getOsmIdentifier)
                .anyMatch(id -> id == osmIdentifier);
    }

    public Set<BoundaryPart> getBoundaryParts()
    {
        return this.boundaryParts;
    }
    
    public Set<Relation> getRelationsByBoundaryTags(final Set<String> tags)
    {
        return this.tagToRelation.keySet()
                .stream()
                .filter(tags::contains)
                .map(this.tagToRelation::get)
                .collect(Collectors.toSet());
    }

    public Map<String, Relation> getTagToRelation()
    {
        return this.tagToRelation;
    }
}
