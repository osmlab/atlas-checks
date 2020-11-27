package org.openstreetmap.atlas.checks.validation.intersections;

import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;

import java.util.List;
import java.util.Set;


public class BoundaryPart {
    
    private final Rectangle bounds;
    private final long osmIdentifier;
    private final String wktGeometry;
    private final Set<String> boundaryTags;
    //TODO?
//    private final Set<AtlasObject> atlasObjects;
    
    public BoundaryPart(long osmIdentifier, Rectangle bounds, String wktGeometry, Set<String> boundaryTags) {
        this.osmIdentifier = osmIdentifier;
        this.bounds = bounds;
        this.wktGeometry = wktGeometry;
        this.boundaryTags = boundaryTags;
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    public long getOsmIdentifier() {
        return osmIdentifier;
    }
    
    public String getWktGeometry() {
        return wktGeometry;
    }
    
    public Set<String> getBoundaryTags() {
        return boundaryTags;
    }
}
