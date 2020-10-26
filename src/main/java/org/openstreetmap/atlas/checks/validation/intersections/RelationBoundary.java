package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RelationBoundary {
    
    private final Map<String, Relation> tagToRelation;
    private final Set<LineItem> lineItems;
    
    public RelationBoundary(Map<String, Relation> tagToRelation, Set<LineItem> lineItems) {
        this.tagToRelation = tagToRelation;
        this.lineItems = lineItems;
    }
    
    public Map<String, Relation> getTagToRelation() {
        return tagToRelation;
    }
    
    public Set<LineItem> getLineItems() {
        return lineItems;
    }
    
    public Set<Relation> getRelationsByBoundaryTags(Set<String> tags){
        return tagToRelation.keySet()
                .stream()
                .filter(tags::contains)
                .map(tagToRelation::get)
                .collect(Collectors.toSet());
    }
}
