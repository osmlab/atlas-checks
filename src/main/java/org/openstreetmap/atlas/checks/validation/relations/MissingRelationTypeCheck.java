package org.openstreetmap.atlas.checks.validation.relations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.CommonMethods;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags {@link Relation}s with missing Type tag. Refer to:
 * https://wiki.openstreetmap.org/wiki/Types_of_relation
 *
 * @author Vladimir Lemberg
 */
public class MissingRelationTypeCheck extends BaseCheck<Object>
{
    public static final String MISSING_TYPE_INSTRUCTIONS = "This relation, {0,number,#}, is missing type tag";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(MISSING_TYPE_INSTRUCTIONS);
    private static final String TAG_FILTER_IGNORE_DEFAULT = "disused:type->!&disabled:type->!";
    private static final long serialVersionUID = 5171171744111206430L;
    private final TaggableFilter tagFilterIgnore;

    public MissingRelationTypeCheck(final Configuration configuration)
    {
        super(configuration);
        this.tagFilterIgnore = this.configurationValue(configuration, "ignore.tags.filter",
                TAG_FILTER_IGNORE_DEFAULT, TaggableFilter::forDefinition);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Relation
                // Missing relation Type tag
                && object.getTag(RelationTypeTag.KEY).isEmpty()
                // Is not "One Member Relation"
                && CommonMethods.getOSMRelationMemberSize((Relation) object) > 1
                // Relation is not disused or disabled
                && this.tagFilterIgnore.test(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        return Optional.of(this.createFlag(object,
                this.getLocalizedInstruction(0, object.getOsmIdentifier())));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
