package org.openstreetmap.atlas.checks.validation.relations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
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

    public static final String MULTIPOLYGON_OMR_INSTRUCTIONS = "This relation, {0,number,#}, contains only "
            + "one member. Multi-polygon relations need multiple polygons.";

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(OMR_INSTRUCTIONS,
            MULTIPOLYGON_OMR_INSTRUCTIONS);

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
        return object instanceof Relation;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Relation relation = (Relation) object;
        final RelationMemberList members = relation.members();

        // If the number of members in the relation is 1
        if (members.size() == 1)
        {
            // If the relation is a multi-polygon,
            if (relation.isMultiPolygon())
            {
                return Optional.of(createFlag(
                        relation.members().stream().map(RelationMember::getEntity)
                                .collect(Collectors.toSet()),
                        this.getLocalizedInstruction(1, relation.getOsmIdentifier())));
            }

            return Optional.of(createFlag(
                    relation.members().stream().map(RelationMember::getEntity)
                            .collect(Collectors.toSet()),
                    this.getLocalizedInstruction(0, relation.getOsmIdentifier())));
        }
        return Optional.empty();
    }
}
