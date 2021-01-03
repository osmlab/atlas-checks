package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Set;

import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

/**
 * @author srachanski
 */
public class BoundaryPart
{

    private final Set<String> boundaryTags;
    private final AtlasEntity atlasEntity;

    public BoundaryPart(final AtlasEntity entity, final Set<String> boundaryTags)
    {
        this.atlasEntity = entity;
        this.boundaryTags = boundaryTags;
    }

    public AtlasEntity getAtlasEntity()
    {
        return this.atlasEntity;
    }

    public Set<String> getBoundaryTags()
    {
        return this.boundaryTags;
    }

    public Rectangle getBounds()
    {
        return this.atlasEntity.bounds();
    }

    public long getOsmIdentifier()
    {
        return this.atlasEntity.getOsmIdentifier();
    }

    public String getWktGeometry()
    {
        return this.atlasEntity.toWkt();
    }
}
