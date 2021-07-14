package org.openstreetmap.atlas.checks.validation.relations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check looks for two or more {@link Relation}s duplicate with same OSM tags and same members
 * with same roles.
 *
 * @author Xiaohong Tang
 */
public class DuplicateRelationCheck extends BaseCheck<Object>
{
    public static final String DUPLICATE_RELATION_INSTRUCTIONS = "Relation {0} and {1} are duplicates with same OSM tags and same members with same roles.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(DUPLICATE_RELATION_INSTRUCTIONS);
    private static final long serialVersionUID = 2723186269280026809L;

    public DuplicateRelationCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Relation && !isFlagged(object.getOsmIdentifier());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Relation obj = (Relation) object;
        final Set<Relation> relations = new HashSet<>();
        final List<RelationMember> members = obj.members().stream().collect(Collectors.toList());

        for (final RelationMember member : members)
        {
            relations.addAll(member.getEntity().relations());
        }

        relations.remove(obj);

        final List<Long> duplicates = new ArrayList<Long>();

        for (final Relation rel : relations)
        {
            if (obj.getOsmTags().equals(rel.getOsmTags())
                    && this.areSameMembers(obj.members(), rel.members()))
            {
                duplicates.add(rel.getOsmIdentifier());
                this.markAsFlagged(rel.getOsmIdentifier());
            }
        }

        this.markAsFlagged(obj.getOsmIdentifier());

        final String dup = duplicates.toString().replace("[", "").replace("]", "");

        if (duplicates.size() > 0)
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, Long.toString(obj.getOsmIdentifier()), dup)));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks if two {@link RelationMemberList}s have same members with same roles
     *
     * @param first
     *            the first {@link RelationMemberList} to check
     * @param second
     *            the second {@link RelationMemberList} to check
     * @return true if the two RelationMemberList have same members with same roles.
     */
    private boolean areSameMembers(final RelationMemberList first, final RelationMemberList second)
    {
        final List<RelationMember> firstMembers = first.stream().collect(Collectors.toList());
        final List<RelationMember> secondMembers = second.stream().collect(Collectors.toList());

        if (firstMembers.size() != secondMembers.size())
        {
            return false;
        }

        for (final RelationMember rel : first)
        {
            final Optional<RelationMember> secMember = this.containsMember(secondMembers, rel);
            if (secMember.isPresent())
            {
                secondMembers.remove(secMember.get());
            }
            else
            {
                return false;
            }
        }
        return secondMembers.isEmpty();
    }

    /**
     * Checks if the {@link RelationMemberList} contains the {@link RelationMember}.
     *
     * @param membersList
     *            {@link RelationMemberList} to check
     * @param member
     *            {@link RelationMember} to check
     * @return the RelationMember if the RelationMemberList contains the member or an empty
     *         optional.
     */
    private Optional<RelationMember> containsMember(final List<RelationMember> membersList,
            final RelationMember member)
    {
        for (final RelationMember rel : membersList)
        {
            if (rel.compareTo(member) == 0)
            {
                return Optional.of(rel);
            }
        }
        return Optional.empty();
    }
}
