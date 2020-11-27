package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RelationBoundary {
    
    private final Map<String, Relation> tagToRelation;
    private final Set<BoundaryPart> boundaryParts;
//    private final Set<AtlasObject> boundaryObjects;
    
    public RelationBoundary(Map<String, Relation> tagToRelation, Set<BoundaryPart> boundaryParts) {
        this.tagToRelation = tagToRelation;
        this.boundaryParts = boundaryParts;
//        this.boundaryObjects = boundaryParts.stream()
//                .map(boundaryPart -> boundaryPart.get)
    }
    
    public Map<String, Relation> getTagToRelation() {
        return tagToRelation;
    }
    
    public Set<BoundaryPart> getBoundaryParts() {
        return boundaryParts;
    }
    
    public Set<Relation> getRelationsByBoundaryTags(Set<String> tags){
        return tagToRelation.keySet()
                .stream()
                .filter(tags::contains)
                .map(tagToRelation::get)
                .collect(Collectors.toSet());
    }
}
