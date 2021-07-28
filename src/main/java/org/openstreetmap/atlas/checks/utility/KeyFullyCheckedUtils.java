package org.openstreetmap.atlas.checks.utility;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.openstreetmap.atlas.checks.validation.tag.GenericTagCheck;
import org.openstreetmap.atlas.checks.validation.tag.InvalidTagsCheck;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

/**
 * Utils to find fully checked keys (only use this for generic tests, such as
 * {@link GenericTagCheck}).
 *
 * @author Taylor Smock
 */
public final class KeyFullyCheckedUtils
{
    private static final Logger logger = LoggerFactory.getLogger(KeyFullyCheckedUtils.class);

    /**
     * Populate ignore tags using reflection and looking for annotations using
     * {@link KeyFullyChecked} in the current package.
     *
     * @return A collection of predicates to use to determine if a tag should be ignored.
     */
    public static Collection<Predicate<Taggable>> populateIgnoreTags()
    {
        final List<TaggableFilter> filters = new ArrayList<>();
        final List<Predicate<Taggable>> predicateFilters = new ArrayList<>();
        final var classLoader = InvalidTagsCheck.class.getClassLoader();
        Set<ClassInfo> classInfoList;
        try
        {
            classInfoList = ClassPath.from(classLoader)
                    .getTopLevelClassesRecursive(InvalidTagsCheck.class.getPackageName());
        }
        catch (final IOException exception)
        {
            logger.error(exception.getMessage(), exception);
            classInfoList = Collections.emptySet();
        }
        for (final ClassPath.ClassInfo info : classInfoList)
        {
            final Class<?> clazz = info.load();
            final Field[] fields = FieldUtils.getFieldsWithAnnotation(clazz, KeyFullyChecked.class);
            for (final Field field : fields)
            {
                getTagFromField(clazz, field, filters, predicateFilters);
            }
        }
        filters.forEach(filter -> predicateFilters.add(filter::test));
        return predicateFilters;
    }

    /**
     * Get tag checks from fields
     *
     * @param clazz
     *            The class with the field
     * @param field
     *            The field to get (must have the {@link KeyFullyChecked} annotation)
     * @param filters
     *            The filters to add {@link TaggableFilter}s to.
     * @param predicateFilters
     *            The filters to add {@link Predicate}s to
     */
    private static void getTagFromField(final Class<?> clazz, final Field field,
            final List<TaggableFilter> filters, final List<Predicate<Taggable>> predicateFilters)
    {

        final KeyFullyChecked annotation = field.getAnnotation(KeyFullyChecked.class);
        final Object fieldObject;
        try
        {
            field.trySetAccessible();
            fieldObject = field.get(clazz);
            if (fieldObject == null)
            {
                throw new IllegalArgumentException(MessageFormat.format(
                        "@KeyFullyChecked fields must be initialized in a static manner ({0}: {1})",
                        clazz.getSimpleName(), field.getName()));
            }
        }
        catch (IllegalArgumentException | IllegalAccessException e1)
        {
            logger.error(e1.getMessage(), e1);
            return;
        }
        if (KeyFullyChecked.Type.TAGGABLE_FILTER == annotation.value())
        {
            getTagFromFieldTaggable(field, fieldObject, filters, predicateFilters);
        }
        else if (KeyFullyChecked.Type.TAG_KEY == annotation.value())
        {
            if (fieldObject instanceof Class)
            {
                predicateFilters.add(Validators.hasValuesFor((Class<?>) fieldObject));
            }
            else
            {
                predicateFilters.add(Validators.hasValuesFor(fieldObject.getClass()));
            }
        }
        else if (KeyFullyChecked.Type.PARTIAL == annotation.value())
        {
            final var value = fieldObject.toString();
            predicateFilters.add(taggable -> taggable.getTags().entrySet().parallelStream()
                    .anyMatch(e -> e.getKey().contains(value)));
        }
        else if (logger.isErrorEnabled())
        {
            logger.error(
                    MessageFormat.format("We don''t currently handle {0}", annotation.value()));
        }
    }

    /**
     * Get taggable checks from fields
     *
     * @param field
     *            The field to get (must have the {@link KeyFullyChecked} annotation)
     * @param fieldObject
     *            The field object
     * @param filters
     *            The filters to add {@link TaggableFilter}s to.
     * @param predicateFilters
     *            The filters to add {@link Predicate}s to
     */
    private static void getTagFromFieldTaggable(final Field field, final Object fieldObject,
            final List<TaggableFilter> filters, final List<Predicate<Taggable>> predicateFilters)
    {
        if (fieldObject instanceof TaggableFilter)
        {
            filters.add((TaggableFilter) fieldObject);
        }
        else if (fieldObject instanceof String)
        {
            filters.add(TaggableFilter.forDefinition((String) fieldObject));
        }
        else if (fieldObject instanceof Predicate)
        {
            // There isn't really a way to check generics, at least not without a lot of
            // code. :(
            final var type = field.getGenericType();
            if (type instanceof ParameterizedType)
            {
                final Type[] parameters = ((ParameterizedType) type).getActualTypeArguments();
                if (parameters.length == 1 && Taggable.class.equals(parameters[0]))
                {
                    // We've checked for the appropriate class AND the appropriate generic
                    // parameters
                    @SuppressWarnings("unchecked")
                    final Predicate<Taggable> filter = (Predicate<Taggable>) fieldObject;
                    predicateFilters.add(filter);
                }
            }
        }
    }

    private KeyFullyCheckedUtils()
    {
        // Hide constructor
    }
}
