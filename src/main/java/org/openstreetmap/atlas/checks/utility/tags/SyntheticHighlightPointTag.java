package org.openstreetmap.atlas.checks.constants.tags;

import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.TagKey;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Tag identifying synthetic Points added to CheckFlags. These Points are used to highlight
 * locations with a FlaggedObject that need to be examined.
 *
 * @author danielbaah
 */
@Tag(synthetic = true)
public enum SyntheticHighlightPointTag
{
    YES;

    @TagKey
    public static final String KEY = "synthetic_highlight_point";

    public static boolean isYes(final Taggable taggable)
    {
        return Validators.isOfType(taggable, SyntheticHighlightPointTag.class, YES);
    }
}
