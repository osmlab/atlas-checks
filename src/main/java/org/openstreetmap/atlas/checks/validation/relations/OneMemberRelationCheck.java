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
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
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
        return object instanceof Relation && !this.isFlagged(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Relation relation = (Relation) object;
        final RelationMemberList members = relation.members();

        // If the number of members in the relation is 1, and the relation is either not a
        // Multipolygon relation or the sole member is role:inner
        if (members.size() == 1 && (!relation.isMultiPolygon() || members.iterator().next()
                .getRole().equalsIgnoreCase(RelationTypeTag.MULTIPOLYGON_ROLE_INNER)))
        {

            this.markAsFlagged(relation);
            return Optional.of(createFlag(
                    relation.members().stream().map(RelationMember::getEntity)
                            .collect(Collectors.toSet()),
                    this.getLocalizedInstruction(0, relation.getOsmIdentifier())));
        }
        return Optional.empty();
    }
}
