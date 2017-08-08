package org.openstreetmap.atlas.checks.atlas.predicates;

import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;

/**
 * Collection of predicates for determining {@link AtlasItem} types
 *
 * @author brian_l_davis
 */
public interface TypePredicates
{
    /**
     * Tests if the {@link AtlasObject} is tagged as an {@link Area}
     */
    Predicate<AtlasObject> IS_AREA = object -> object instanceof Area;

    /**
     * Tests if the {@link AtlasObject} is an {@link Edge}
     */
    Predicate<AtlasObject> IS_EDGE = object -> object instanceof Edge;
}
