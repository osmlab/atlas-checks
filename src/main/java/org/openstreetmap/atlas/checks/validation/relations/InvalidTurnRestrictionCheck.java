package org.openstreetmap.atlas.checks.validation.relations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.TurnRestriction;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.TurnRestrictionTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;

/**
 * Check for invalid turn restrictions
 *
 * @author gpogulsky
 * @author bbreithaupt
 */
public class InvalidTurnRestrictionCheck extends BaseCheck<Long>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "Relation ID: {0,number,#} is marked as turn restriction, but it is not well-formed: {1}");
    private static final String MISSING_TO_FROM_VIA_INSTRUCTION = "Missing a FROM and/or TO member and/or VIA member";
    private static final String INVALID_MEMBER_TYPE_INSTRUCTION = "Invalid member type";
    private static final String TOPOLOGY_NOT_MATCH_RESTRICTION_INSTRUCTION = "Restriction doesn't match topology";
    private static final String UNKNOWN_ISSUE = "Unable to specify issue";
    private static final Map<String, String> INVALID_REASON_INSTRUCTION_MAP = new HashMap<>();
    private static final long serialVersionUID = -983698716949386657L;
    public static final double STRAIGHT_ROUTE_ANGLE_THRESHOLD_DEFAULT = 60.0;
    public static final double UTURN_ROUTE_ANGLE_THRESHOLD_DEFAULT = 100.0;
    private static final int MAXIMUM_ANGLE = 180;
    static
    {
        final String routeInstruction = "There is not a single navigable route to restrict, this restriction may be redundant or need to be split in to multiple relations";
        INVALID_REASON_INSTRUCTION_MAP.put("Cannot have a route with no members", routeInstruction);
        INVALID_REASON_INSTRUCTION_MAP.put(
                "Restriction relation should not have more than 1 via node.",
                "A Turn Restriction should only have 1 via Node");
        INVALID_REASON_INSTRUCTION_MAP.put(
                "has same members in from and to, but has no via members to disambiguate.",
                "Via member is required for restrictions with the same to and from members");
        INVALID_REASON_INSTRUCTION_MAP.put("Can't build route from", routeInstruction);
        INVALID_REASON_INSTRUCTION_MAP.put("Unable to build a route from edges", routeInstruction);
        INVALID_REASON_INSTRUCTION_MAP.put(
                "A route was found from start to end, but not every unique edge was used",
                routeInstruction);
        INVALID_REASON_INSTRUCTION_MAP.put("No edge that connects to the current route",
                routeInstruction);
    }
    private final Angle straightOnAngleThreshold;
    private final Angle uturnAngleThreshold;

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public InvalidTurnRestrictionCheck(final Configuration configuration)
    {
        super(configuration);
        this.straightOnAngleThreshold = this.configurationValue(configuration,
                "straight.on.angle.threshold", STRAIGHT_ROUTE_ANGLE_THRESHOLD_DEFAULT,
                Angle::degrees);
        this.uturnAngleThreshold = this.configurationValue(configuration, "uturn.angle.threshold",
                UTURN_ROUTE_ANGLE_THRESHOLD_DEFAULT, Angle::degrees);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Relation && TurnRestrictionTag.isRestriction(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Relation relation = (Relation) object;
        final Set<AtlasObject> members = relation.members().stream().map(RelationMember::getEntity)
                .collect(Collectors.toSet());

        if (relation.members().stream()
                .noneMatch(member -> member.getRole().equals(RelationTypeTag.RESTRICTION_ROLE_FROM))
                || relation.members().stream().noneMatch(
                        member -> member.getRole().equals(RelationTypeTag.RESTRICTION_ROLE_TO))
                || relation.members().stream().noneMatch(
                        member -> member.getRole().equals(RelationTypeTag.RESTRICTION_ROLE_VIA)))
        {
            return Optional.of(createFlag(members, this.getLocalizedInstruction(0,
                    relation.getOsmIdentifier(), MISSING_TO_FROM_VIA_INSTRUCTION)));
        }

        // Restriction relation, bad member type
        if (relation.members().stream().anyMatch(member -> member.getEntity() instanceof Line))
        {
            return Optional.of(createFlag(members, this.getLocalizedInstruction(0,
                    relation.getOsmIdentifier(), INVALID_MEMBER_TYPE_INSTRUCTION)));
        }

        // Build a turn restriction
        final TurnRestriction turnRestriction = new TurnRestriction(relation);
        // If it is not valid map the reason to an instruction
        if (!turnRestriction.isValid())
        {
            return Optional.of(createFlag(members, this.getLocalizedInstruction(0,
                    relation.getOsmIdentifier(),
                    this.getInstructionFromInvalidReason(turnRestriction.getInvalidReason()))));
        }

        // Restriction doesn't match topology
        if (!this.isValidTopology(relation))
        {
            return Optional.of(createFlag(members, this.getLocalizedInstruction(0,
                    relation.getOsmIdentifier(), TOPOLOGY_NOT_MATCH_RESTRICTION_INSTRUCTION)));
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Map {@link TurnRestriction} invalid reasons to instructions
     *
     * @param invalidReason
     *            invalid reason from {@link TurnRestriction}
     * @return {@link String} instruction
     */
    private String getInstructionFromInvalidReason(final String invalidReason)
    {
        String instruction = UNKNOWN_ISSUE;

        for (final Map.Entry<String, String> entry : INVALID_REASON_INSTRUCTION_MAP.entrySet())
        {
            if (invalidReason.contains(entry.getKey()))
            {
                instruction = entry.getValue();
                break;
            }
        }

        return instruction;
    }

    /**
     * Return true if the turn angle makes a straight path within the threshold angle
     *
     * @param Angle angle
     *
     * @return true if the turn angle makes a straight path within the threshold angle
     */
    private boolean isHeadingStraight(final Angle angle)
    {
        return Math.abs(angle.asDegrees()) < this.straightOnAngleThreshold.asDegrees();
    }

    /**
     * Return true if the turn angle makes a left turn
     *
     * @param Angle angle
     *
     * @return true if the turn angle makes a left turn
     */
    private boolean isLeftTurn(final Angle angle)
    {
        return angle.asDegrees() < 0 && angle.asDegrees() > -MAXIMUM_ANGLE;
    }

    /**
     * Return true if the LEFT_TURN restriction doesn't match the topology
     *
     * @param TurnRestrictionTag turnRestrictionTag
     * 
     * @param Angle turnAngle
     *
     * @return true if the LEFT_TURN restriction doesn't match the topology
     */
    private boolean isLeftTurnTopologyViolated(final TurnRestrictionTag turnRestrictionTag, final Angle turnAngle)
    {
        return (TurnRestrictionTag.ONLY_LEFT_TURN == turnRestrictionTag
                || TurnRestrictionTag.NO_LEFT_TURN == turnRestrictionTag)
                && this.isRightTurn(turnAngle);
    }    

    /**
     * Return true if the turn angle makes a right turn
     *
     * @param Angle angle
     *
     * @return true if the turn angle makes a right turn
     */
    private boolean isRightTurn(final Angle angle)
    {
        return angle.asDegrees() > 0 && angle.asDegrees() < MAXIMUM_ANGLE;
    }

    /**
     * Return true if the RIGHT_TURN restriction doesn't match the topology
     *
     * @param TurnRestrictionTag turnRestrictionTag
     * 
     * @param Angle turnAngle
     *
     * @return true if the RIGHT_TURN restriction doesn't match the topology
     */
    private boolean isRightTurnTopologyViolated(final TurnRestrictionTag turnRestrictionTag, final Angle turnAngle)
    {
        return (TurnRestrictionTag.ONLY_RIGHT_TURN == turnRestrictionTag
                || TurnRestrictionTag.NO_RIGHT_TURN == turnRestrictionTag)
                && this.isLeftTurn(turnAngle);
    }    

    /**
     * Return true if the STRAIGHT_ON restriction doesn't match the topology
     *
     * @param TurnRestrictionTag turnRestrictionTag
     * 
     * @param Angle turnAngle
     *
     * @return true if the STRAIGHT_ON restriction doesn't match the topology
     */
    private boolean isStaightOnTopologyViolated(final TurnRestrictionTag turnRestrictionTag, final Angle turnAngle)
    {
        return (TurnRestrictionTag.ONLY_STRAIGHT_ON == turnRestrictionTag
                || TurnRestrictionTag.NO_STRAIGHT_ON == turnRestrictionTag)
                && !this.isHeadingStraight(turnAngle);
    }   

    /**
     * Return true if the turn angle makes a U-turn
     *
     * @param Angle angle
     *
     * @return true if the turn angle makes a U-turn
     */
    private boolean isUTurn(final Angle angle)
    {
        return Math.abs(angle.asDegrees()) > this.uturnAngleThreshold.asDegrees();
    }

    /**
     * Return true if the U_TURN restriction doesn't match the topology
     *
     * @param TurnRestrictionTag turnRestrictionTag
     * 
     * @param Angle turnAngle
     *
     * @return true if the U_TURN restriction doesn't match the topology
     */
    private boolean isUTurnTopologyViolated(final TurnRestrictionTag turnRestrictionTag, final Angle turnAngle)
    {
        return TurnRestrictionTag.NO_U_TURN == turnRestrictionTag 
                && !this.isUTurn(turnAngle);
    }    

    /**
     * Return true if the turn restriction tag satisfies the topology
     *
     * @param Relation relation
     *
     * @return true if the turn restriction tag satisfies the topology
     */
    private boolean isValidTopology(final Relation relation)
    {
        final TurnRestriction turnRestriction = new TurnRestriction(relation);
      
        final Angle turnAngle = turnRestriction.getTo().asPolyLine().initialHeading().get()
                .subtract(turnRestriction.getFrom().asPolyLine().finalHeading().get());

        final TurnRestrictionTag turnRestrictionTag = Validators
                .from(TurnRestrictionTag.class, relation).get();

        return  !this.isStaightOnTopologyViolated(turnRestrictionTag, turnAngle) &&
                !this.isLeftTurnTopologyViolated(turnRestrictionTag, turnAngle) &&
                !this.isRightTurnTopologyViolated(turnRestrictionTag, turnAngle) &&
                !this.isUTurnTopologyViolated(turnRestrictionTag, turnAngle);
    }

}
