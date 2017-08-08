package org.openstreetmap.atlas.checks.validation.relations;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.TurnRestriction;
import org.openstreetmap.atlas.tags.TurnRestrictionTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Check for invalid turn restrictions
 *
 * @author gpogulsky
 */
public class InvalidTurnRestrictionCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -983698716949386657L;

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public InvalidTurnRestrictionCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Relation && TurnRestrictionTag.isRestriction(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Optional<CheckFlag> result;

        final Relation relation = (Relation) object;
        if (!TurnRestriction.from(relation).isPresent())
        {
            final Set<AtlasObject> members = relation.members().stream()
                    .map(RelationMember::getEntity).collect(Collectors.toSet());
            result = Optional.of(createFlag(members, "Relation ID: " + relation.getOsmIdentifier()
                    + " is marked as turn restriction, but it is not a well-formed relation (i.e. it is missing required members)"));
        }
        else
        {
            result = Optional.empty();
        }

        return result;
    }

}
