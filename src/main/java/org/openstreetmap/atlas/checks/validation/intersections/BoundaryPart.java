package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

import java.util.Set;


public class BoundaryPart {

    private final Set<String> boundaryTags;
    private final AtlasEntity atlasEntity;

    public BoundaryPart(AtlasEntity entity, Set<String> boundaryTags) {
        this.atlasEntity = entity;
        this.boundaryTags = boundaryTags;
    }

    public AtlasEntity getAtlasEntity() {
        return atlasEntity;
    }

    public Rectangle getBounds() {
        return atlasEntity.bounds();
    }
    
    public long getOsmIdentifier() {
        return atlasEntity.getOsmIdentifier();
    }
    
    public String getWktGeometry() {
        return atlasEntity.toWkt();
    }
    
    public Set<String> getBoundaryTags() {
        return boundaryTags;
    }
}
