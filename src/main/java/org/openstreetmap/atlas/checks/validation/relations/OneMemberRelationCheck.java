package org.openstreetmap.atlas.checks.validation.relations;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags {@link Relation}s which contain only one member.
 *
 * @author savannahostrowski
 */
public class OneMemberRelationCheck extends BaseCheck<Object>
{
    public static final String OMR_INSTRUCTIONS = "This relation, {0,number,#}, contains only "
            + "one member.";

    public static final String MULTIPOLYGON_OMR_INSTRUCTIONS = "This relation, {0,number,#}, contains only "
            + "one member. Multi-polygon relations need multiple polygons.";

    public static final String MEMBER_RELATION_INSTRUCTIONS = "This relation, {0,number,#}, contains only relation {1,number,#}.";

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(OMR_INSTRUCTIONS,
            MULTIPOLYGON_OMR_INSTRUCTIONS, MEMBER_RELATION_INSTRUCTIONS);
    private static final long serialVersionUID = -4435597321329026191L;

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
            if (members.get(0).getEntity().getType().equals(ItemType.RELATION))
            {
                return Optional.of(createFlag(getRelationMembers((Relation) object),
                        this.getLocalizedInstruction(2, relation.getOsmIdentifier(),
                                members.get(0).getEntity().getOsmIdentifier())));
            }
            // If the relation is a multi-polygon,
            if (relation.isMultiPolygon())
            {
                return Optional.of(createFlag(getRelationMembers((Relation) object),
                        this.getLocalizedInstruction(1, relation.getOsmIdentifier())));
            }
            return Optional.of(createFlag(getRelationMembers((Relation) object),
                    this.getLocalizedInstruction(0, relation.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Gathers all the members of a relation, replacing child relations with their members.
     *
     * @param relation
     *            {@link Relation} to get the members of
     * @return A {@link Set} of the relations members as {@link AtlasObject}s
     */
    private Set<AtlasObject> getRelationMembers(final Relation relation)
    {
        final Set<AtlasObject> relationMembers = new HashSet<>();
        final ArrayDeque<AtlasObject> toProcess = new ArrayDeque<>();
        AtlasObject polledMember;

        toProcess.add(relation);
        while (!toProcess.isEmpty())
        {
            polledMember = toProcess.poll();
            // If a member is a relation do not add it to the set and instead add its members to the
            // queue for processing
            if (polledMember instanceof Relation)
            {
                ((Relation) polledMember).members()
                        .forEach(member -> toProcess.add(member.getEntity()));
            }
            // Otherwise add the member to the returned set
            else
            {
                relationMembers.add(polledMember);
            }
        }
        return relationMembers;
    }
}
