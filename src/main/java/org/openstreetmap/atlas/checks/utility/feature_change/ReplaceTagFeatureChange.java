package org.openstreetmap.atlas.checks.utility.feature_change;

import java.util.Map;

import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;

/**
 * Add a tag using another tag as the base. For example if the original tag is "highway=residential"
 * and the replacement is "motorway", the new tag will be "motorway=residential". If the replacement
 * is a full tag, that will be used. For example, "motorway=yes", the replacement will be
 * "motorway=yes" regardless of the original tag. If the original tag should be removed, use
 * {@link RemoveTagFeatureChange} as well.
 *
 * @author Taylor Smock
 */
public class ReplaceTagFeatureChange implements IFeatureChange, IFeatureChange.TaggedChange
{
    private final Map.Entry<String, String> tag;
    private final Map.Entry<String, String> newTag;

    /**
     * Create a new ReplacementFeatureChange
     *
     * @param tag
     *            The original tag
     * @param newTag
     *            The tag to replace the original tag with. If only a key, the original tag value
     *            will be used.
     */
    public ReplaceTagFeatureChange(final Map.Entry<String, String> tag,
            final Map.Entry<String, String> newTag)
    {
        this.tag = tag;
        this.newTag = newTag;
    }

    @Override
    public <T extends CompleteEntity<T>> CompleteEntity<T> apply(
            final CompleteEntity<T> completeEntity)
    {
        return completeEntity.withAddedTag(this.newTag.getKey(),
                this.newTag.getValue().isBlank() ? this.tag.getValue() : this.newTag.getValue());
    }
}
