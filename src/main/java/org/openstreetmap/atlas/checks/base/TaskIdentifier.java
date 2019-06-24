package org.openstreetmap.atlas.checks.base;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.checks.constants.CommonConstants;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.collections.EnhancedCollectors;

/**
 * Extracted the TaskIdentifier into a separate class so it can be reused outside of
 * {@link BaseCheck} or in separate unit tests
 *
 * @author cstaylor
 */
public class TaskIdentifier
{
    private final String identifier;

    /**
     * Constructor for a single {@link AtlasObject}
     *
     * @param object
     *            the {@link AtlasObject} used to form the {@code identifier}
     */
    public TaskIdentifier(final AtlasObject object)
    {
        this(Collections.singleton(object));
    }

    /**
     * Constructor for multiple {@link AtlasObject}s
     *
     * @param objects
     *            a {@code Set} of {@link AtlasObject}s used to form the {@code identifier}
     */
    public TaskIdentifier(final Set<? extends AtlasObject> objects)
    {
        if (objects == null || objects.isEmpty())
        {
            throw new CoreException("items can't be empty");
        }
        final Set<String> orderedSet = new TreeSet<>(
                objects.stream().map(item -> String.valueOf(item.getIdentifier()))
                        .collect(EnhancedCollectors.toUnmodifiableSortedSet()));
        this.identifier = StringUtils.join(orderedSet, CommonConstants.EMPTY_STRING);
    }

    @Override
    public String toString()
    {
        return this.identifier;
    }
}
