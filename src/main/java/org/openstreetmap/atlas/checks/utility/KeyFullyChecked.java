/**
 *
 */
package org.openstreetmap.atlas.checks.utility;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Use this when a check considers a key to be "fully" checked. This means that there should be no
 * unintended values left over. Any keys that do *not* have this annotation will be checked by the
 * fallback tag checker, which can be an expensive process. This is mostly useful for tags that can
 * have *any* number of arbitrary values (e.g., "name" or "addr:street").
 *
 * @author Taylor Smock
 */
@Retention(RUNTIME)
@Target(FIELD)
@Documented
public @interface KeyFullyChecked
{
    /**
     * The method used to determine whether or not a key is fully checked.
     */
    enum Type
    {
        /**
         * Used when any key with the partial string is found.
         */
        PARTIAL,
        /**
         * @see org.openstreetmap.atlas.tags.filters.TaggableFilter
         */
        TAGGABLE_FILTER,
        /**
         * @see org.openstreetmap.atlas.tags.annotations.TagKey
         */
        TAG_KEY
    }

    Type value() default Type.TAG_KEY;
}
