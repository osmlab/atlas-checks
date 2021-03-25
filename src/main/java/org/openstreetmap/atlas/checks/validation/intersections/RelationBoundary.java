package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Contains BoundaryParts and map of relations. It is a main object for analysing
 * BoundaryIntersectionCheck.
 *
 * @author srachanski
 */
public class RelationBoundary
{

    private final Map<String, Relation> tagToRelation;
    private final Set<AtlasEntity> atlasEntities;

    public RelationBoundary(final Map<String, Relation> tagToRelation,
            final Set<AtlasEntity> atlasEntities)
    {
        this.tagToRelation = tagToRelation;
        this.atlasEntities = atlasEntities;
    }

    public boolean containsRelationId(final long osmIdentifier)
    {
        return this.tagToRelation.values().stream().map(Relation::getOsmIdentifier)
                .anyMatch(id -> id == osmIdentifier);
    }

    public Set<AtlasEntity> getAtlasEntities()
    {
        return this.atlasEntities;
    }

    public Set<Relation> getRelationsByBoundaryTags(final Set<String> tags)
    {
        return this.tagToRelation.keySet().stream().filter(tags::contains)
                .map(this.tagToRelation::get).collect(Collectors.toSet());
    }

    public Map<String, Relation> getTagToRelation()
    {
        return this.tagToRelation;
    }
}
