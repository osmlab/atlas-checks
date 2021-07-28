package org.openstreetmap.atlas.checks.utility.feature_change;

import java.util.Map;

import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;

/**
 * Remove a tag from a feature
 *
 * @author Taylor Smock
 */
public class RemoveTagFeatureChange implements IFeatureChange, IFeatureChange.TaggedChange
{
    private final Map.Entry<String, String> tag;

    /**
     * Create a new RemoveFeatureChange
     *
     * @param tag
     *            The tag to remove from the feature
     */
    public RemoveTagFeatureChange(final Map.Entry<String, String> tag)
    {
        this.tag = tag;
    }

    @Override
    public <T extends CompleteEntity<T>> CompleteEntity<T> apply(
            final CompleteEntity<T> completeEntity)
    {
        return completeEntity.withRemovedTag(this.tag.getKey());
    }
}
