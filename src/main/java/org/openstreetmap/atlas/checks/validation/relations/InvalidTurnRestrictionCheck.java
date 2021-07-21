package org.openstreetmap.atlas.checks.validation.relations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
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
    private static final String MISSING_TO_FROM_VIA_INSTRUCTION = "Class 1: Missing a FROM and/or TO member and/or VIA member";
    private static final String INVALID_MEMBER_TYPE_INSTRUCTION = "Class 2: Invalid member type";
    private static final String TOPOLOGY_NOT_MATCH_RESTRICTION_INSTRUCTION = "Class 5: Restriction doesn't match topology";
    private static final String UNKNOWN_ISSUE = "Unable to specify issue";
    private static final Map<String, String> INVALID_REASON_INSTRUCTION_MAP = new HashMap<>();
    private static final long serialVersionUID = -983698716949386657L;
    public static final double STRAIGHT_ROUTE_ANGLE_THRESHOLD_DEFAULT = 60.0;
    public static final double UTURN_ROUTE_ANGLE_THRESHOLD_DEFAULT = 100.0;
    private static final int MAXIMUM_ANGLE = 180;
    static
    {
        final String routeInstruction = "Class 3: There is not a single navigable route to restrict, this restriction may be redundant or need to be split in to multiple relations";
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

        // Class 1: "Restriction relation, wrong number of members" : some required member are
        // missing,
        // eg. there is a "from" and "via" role, but missing the "to" role.
        // Live example: https://www.openstreetmap.org/relation/7070408
        // same link in Osmose 3180:
        // http://osmose.openstreetmap.fr/en/map/#item=3180&zoom=17&lat=-34.881034&lon=-55.048703&level=2&fixable=online&issue_uuid=a647103a-7c01-9a50-fc2b-dd517f6e409e
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

        // Class 2: "Restriction relation, bad member type"
        // Invalid member type (e.g. "disused:highway") for TO or FROM
        // Live example: https://www.openstreetmap.org/relation/3658651
        // same link in Osmose 3180:
        // http://osmose.openstreetmap.fr/en/map/#item=3180&zoom=17&lat=-33.703765&lon=-53.457542&level=2&fixable=online&issue_uuid=7aece9a1-aa7f-172e-d022-8679ec3499eb
        if (relation.members().stream().anyMatch(member -> member.getEntity() instanceof Line))
        {
            return Optional.of(createFlag(members, this.getLocalizedInstruction(0,
                    relation.getOsmIdentifier(), INVALID_MEMBER_TYPE_INSTRUCTION)));
        }

        // Class 3: "Unconnected restriction relation ways" :
        // Build a turn restriction
        // Live example: https://www.openstreetmap.org/relation/8221967
        // same link in Osmose 3180:
        // http://osmose.openstreetmap.fr/en/map/#item=3180&zoom=17&lat=-27.526051&lon=153.055579&level=2&fixable=online&issue_uuid=38ef3192-5827-8899-0024-577812d848c8
        final TurnRestriction turnRestriction = new TurnRestriction(relation);
        // If it is not valid map the reason to an instruction
        if (!turnRestriction.isValid())
        {
            return Optional.of(createFlag(members, this.getLocalizedInstruction(0,
                    relation.getOsmIdentifier(),
                    this.getInstructionFromInvalidReason(turnRestriction.getInvalidReason()))));
        }

        // Class 4: "Restriction relation, bad oneway direction on "from" or "to" member" :
        // impossible to reach the restriction by respecting the oneway.
        // Live example: https://www.openstreetmap.org/relation/2741062
        // same link in Osmose 3180:
        // http://osmose.openstreetmap.fr/en/map/#item=3180&zoom=17&lat=-34.880444&lon=-56.149815&level=2&fixable=online&issue_uuid=e73991dc-9c90-3e49-f1fc-06076844f568
        // NOT IMPLEMENTED after verifying with editorial team.
        // while we don't need a turn restriction when there's already a oneway, its existence is
        // not actually breaking anything.

        // Class 5: Restriction doesn't match topology
        // Live example: https://www.openstreetmap.org/relation/5451368
        // same link in Osmose 3180:
        // http://osmose.openstreetmap.fr/en/map/#item=3180&zoom=17&lat=-33.236907&lon=-54.379256&level=2&fixable=online&issue_uuid=2b04fd37-d2aa-091a-c005-688cd62b5f28
        if (!this.isValidTopology(relation))
        {
            return Optional.of(createFlag(members, this.getLocalizedInstruction(0,
                    relation.getOsmIdentifier(), TOPOLOGY_NOT_MATCH_RESTRICTION_INSTRUCTION)));
        }

        return Optional.empty();

        // Class 31801 "Useless non u-turn restriction, it's forbidden by local law" :
        // Not implemented because it is no longer an OSMOSE 3180 issue
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
     * Return true if the turn restriction tag satisfies the topology
     *
     * @param Relation relation
     *
     * @return true if the turn restriction tag satisfies the topology
     */
    private boolean isValidTopology(final Relation relation)
    {
        // Build the via members
        final Set<AtlasItem> viaMembers = relation.members().stream()
                .filter(member -> member.getRole().equals(RelationTypeTag.RESTRICTION_ROLE_VIA))
                .filter(member -> member.getEntity() instanceof Node
                        || member.getEntity() instanceof Edge)
                .map(RelationMember::getEntity).map(entity -> (AtlasItem) entity)
                .collect(Collectors.toSet());

        // Filter the members to extract only the "from" members that are connected at the end
        // to VIA members.
        final Set<Edge> fromMembers = new TreeSet<>();
        relation.members().stream()
                .filter(member -> member.getRole().equals(RelationTypeTag.RESTRICTION_ROLE_FROM)
                        && member.getEntity() instanceof Edge
                        && (!viaMembers.isEmpty()
                                && ((Edge) member.getEntity()).isConnectedAtEndTo(viaMembers)))
                .forEach(member -> fromMembers.add((Edge) member.getEntity()));

        // Filter the members to extract only the "TO" members that are connected at the
        // beginning to VIA members
        final Set<Edge> toMembers = new TreeSet<>();
        relation.members().stream()
                .filter(member -> member.getRole().equals(RelationTypeTag.RESTRICTION_ROLE_TO)
                        && member.getEntity() instanceof Edge
                        && (!viaMembers.isEmpty()
                                && ((Edge) member.getEntity()).isConnectedAtStartTo(viaMembers)))
                .forEach(member -> toMembers.add((Edge) member.getEntity()));

        // find the turnAngle between TO and FROM using angle subtraction
        // the resulting turnAngle will be between -180 to 180 degrees
        // 
        //    from
        //      \
        //       \
        //        \------> to
        //

        final Angle turnAngle = toMembers.iterator().next().asPolyLine().initialHeading().get()
                .subtract(fromMembers.iterator().next().asPolyLine().finalHeading().get());

        final TurnRestrictionTag turnRestrictionTag = Validators
                .from(TurnRestrictionTag.class, relation).get();

        return ((TurnRestrictionTag.ONLY_STRAIGHT_ON == turnRestrictionTag
                || TurnRestrictionTag.NO_STRAIGHT_ON == turnRestrictionTag)
                && !this.isHeadingStraight(turnAngle)) &&
                ((TurnRestrictionTag.ONLY_LEFT_TURN == turnRestrictionTag
                || TurnRestrictionTag.NO_LEFT_TURN == turnRestrictionTag)
                && this.isRightTurn(turnAngle)) &&
                ((TurnRestrictionTag.ONLY_RIGHT_TURN == turnRestrictionTag
                || TurnRestrictionTag.NO_RIGHT_TURN == turnRestrictionTag)
                && this.isLeftTurn(turnAngle)) &&
                (TurnRestrictionTag.NO_U_TURN == turnRestrictionTag && !this.isUTurn(turnAngle));
    }
}
