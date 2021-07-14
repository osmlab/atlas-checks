package org.openstreetmap.atlas.checks.utility.feature_change;

import java.util.Collection;
import java.util.function.BiFunction;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

/**
 * An interface to help modify a complete entity. This is mostly useful to create a composite
 * FeatureChange.
 *
 * @author Taylor Smock
 */
public interface IFeatureChange
{
    /** Implement on IFeatureChange objects that change geometry */
    interface GeometryChange
    {
    }

    /** Implement on IFeatureChange objects that change tags */
    interface TaggedChange
    {
    }

    /**
     * Create a feature change from a collection of changes
     *
     * @param changeCreator
     *            The method to use to create the feature change (e.g.
     *            {@link FeatureChange#add(AtlasEntity, Atlas)})
     * @param object
     *            The object that needs to be changed
     * @param changes
     *            The changes to make to the object
     * @return The FeatureChange to be applied. Currently only applies to tags.
     */
    static FeatureChange createFeatureChange(
            final BiFunction<AtlasEntity, Atlas, FeatureChange> changeCreator,
            final AtlasEntity object, final Collection<IFeatureChange> changes)
    {
        // originally shallowFrom withTags
        CompleteEntity<?> completeEntity = (CompleteEntity<?>) CompleteEntity.shallowFrom(object);
        if (changes.stream().anyMatch(IFeatureChange.TaggedChange.class::isInstance))
        {
            completeEntity = completeEntity.withTags(object.getTags());
        }
        if (object instanceof Iterable<?>
                && changes.stream().anyMatch(IFeatureChange.GeometryChange.class::isInstance))
        {
            var addGeometry = true;
            for (final Object iterable : (Iterable<?>) object)
            {
                if (!(iterable instanceof Location))
                {
                    addGeometry = false;
                    break;
                }
            }

            if (addGeometry)
            {
                // This is checked in the above for loop.
                completeEntity = completeEntity.withGeometry((Iterable<Location>) object);
            }
        }
        for (final IFeatureChange change : changes)
        {
            completeEntity = change.apply(completeEntity);
        }
        return changeCreator.apply((AtlasEntity) completeEntity, object.getAtlas());
    }

    /**
     * Apply a change to a complete entity
     *
     * @param <T>
     *            The type of the complete entity
     * @param entity
     *            The entity that will be changed
     * @return The new CompleteEntity (may be the entity from the parameter)
     */
    <T extends CompleteEntity<T>> CompleteEntity<T> apply(CompleteEntity<T> entity);
}
