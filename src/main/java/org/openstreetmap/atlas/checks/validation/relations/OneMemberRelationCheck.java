package org.openstreetmap.atlas.checks.validation.relations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags {@link Relation}s which contain only one member.
 *
 * @author savannahostrowski
 */
public class OneMemberRelationCheck extends BaseCheck
{
    public static final String OMR_INSTRUCTIONS = "This relation, {0,number,#}, contains only "
            + "one member.";

    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(OMR_INSTRUCTIONS);

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    public OneMemberRelationCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Relation && !this.isFlagged(object.getOsmIdentifier());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Relation relation = (Relation) object;
        // Initialize a flag that will be flipped if the relation is found to be problematic
        boolean flag = false;

        // If the relation is a multipolygon
        if (relation.isMultiPolygon())
        {
            // Determine if there are members with the role:inner
            final boolean isInner = relation.members().stream().anyMatch(member -> member.getRole()
                    .equalsIgnoreCase(RelationTypeTag.MULTIPOLYGON_ROLE_INNER));

            // If the only member of the relation has the role:inner then we want to flag the
            // relation
            if (isInner && relation.members().size() == 1)
            {
                flag = true;
            }
            // If the relation has only one member and is not a multi-polygon
        }
        else if (relation.members().size() == 1)
        {
            flag = true;
        }

        if (flag)
        {
            this.markAsFlagged(relation.getOsmIdentifier());
            return Optional.of(createFlag(
                    relation.members().stream().map(RelationMember::getEntity)
                            .collect(Collectors.toSet()),
                    this.getLocalizedInstruction(0, relation.getOsmIdentifier())));
        }
        return Optional.empty();
    }
}
