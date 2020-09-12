package org.openstreetmap.atlas.checks.validation.relations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.walker.EdgeWalker;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.DestinationTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

import com.google.common.collect.Iterables;

/**
 * Finds signboard relations that don't meet the following requirements: Has at least one role=from
 * member, which is an edge. Has exactly one role=to member, which is an edge. The from and to edges
 * meet. All role=FROM edges are connected to each other. Has a destination= tag.
 *
 * @author micah-nacht
 */
public class InvalidSignBoardRelationCheck extends BaseCheck<Long>
{
    private static final String MISSING_DESTINATION_INSTRUCTION = "All type=destination_sign relations must have a destination= tag, but {0,number,#} is missing this tag. Please add this tag.";
    private static final String MISSING_MEMBER_INSTRUCTION = "All type=destination_sign relations must have a role={0} member, but {1,number,#} is missing this member.";
    private static final String EXTRA_MEMBER_INSTRUCTION = "All type=destination_sign relations must have exactly one role={0} member, but {1,number,#} has more than one.";
    private static final String WRONG_TYPE_INSTRUCTION = "In a type=destination_sign relation, the role={0} member must be a way, but {1,number,#} is a {2}.";
    private static final String FROM_TO_NO_MEETING_INSTRUCTION = "In a type=destination_sign relation, the role=from member must meet the role=to member, but they do not here.";
    private static final String DISCONNECTED_FROM_INSTRUCTION = "In a type=destination_sign relation, the role=from members must be connected.";
    private static final String TEMP_RELATION_ID_INSTRUCTION = "The relation with ID={0,number,#} is problematic.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            MISSING_DESTINATION_INSTRUCTION, MISSING_MEMBER_INSTRUCTION, EXTRA_MEMBER_INSTRUCTION,
            WRONG_TYPE_INSTRUCTION, FROM_TO_NO_MEETING_INSTRUCTION, TEMP_RELATION_ID_INSTRUCTION,
            DISCONNECTED_FROM_INSTRUCTION);
    private static final String FROM = "from";
    private static final String TO = "to";
    private static final int MISSING_DESTINATION_INDEX = 0;
    private static final int MISSING_MEMBER_INDEX = 1;
    private static final int EXTRA_MEMBER_INDEX = 2;
    private static final int WRONG_TYPE_INDEX = 3;
    private static final int FROM_TO_NO_MEETING_INDEX = 4;
    private static final int TEMP_RELATION_ID_INDEX = 5;
    private static final int DISCONNECTED_FROM_INDEX = 6;
    private static final long serialVersionUID = 7761409062471623430L;

    public InvalidSignBoardRelationCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Relation && Validators.isOfType(object, RelationTypeTag.class,
                RelationTypeTag.DESTINATION_SIGN);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Relation signBoard = (Relation) object;
        final List<String> instructions = new ArrayList<>();
        instructions.add(
                this.getLocalizedInstruction(TEMP_RELATION_ID_INDEX, signBoard.getOsmIdentifier()));

        // Must have a destination= tag
        if (!Validators.hasValuesFor(signBoard, DestinationTag.class))
        {
            instructions.add(this.getLocalizedInstruction(MISSING_DESTINATION_INDEX,
                    signBoard.getOsmIdentifier()));
        }

        // Check the relation members
        final Map<String, List<RelationMember>> membersByRole = signBoard.members().stream()
                .collect(Collectors.groupingBy(RelationMember::getRole));
        final Tuple<Optional<String>, Optional<Route>> fromRouteAndReasons = this.fromRoute(
                membersByRole.getOrDefault(FROM, Collections.emptyList()),
                signBoard.getOsmIdentifier());
        final Tuple<Optional<String>, Optional<Route>> toRouteAndReasons = this.toRoute(
                membersByRole.getOrDefault(TO, Collections.emptyList()),
                signBoard.getOsmIdentifier());
        fromRouteAndReasons.getFirst().ifPresent(instructions::add);
        toRouteAndReasons.getFirst().ifPresent(instructions::add);

        // If the from route doesn't meet the to way, this is invalid
        if (!fromRouteAndReasons.getSecond().flatMap(fromRoute -> toRouteAndReasons.getSecond()
                .map(toRoute -> fromAndToMeet(fromRoute, toRoute))).orElse(false))
        {
            instructions.add(this.getLocalizedInstruction(FROM_TO_NO_MEETING_INDEX));
        }

        return instructions.size() == 1 ? Optional.empty()
                : Optional.of(this.createFlag(signBoard.flatten(), instructions));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Return true if fromEdge and toEdge meet
     *
     * @param fromEdge
     *            The starting edge
     * @param toEdge
     *            The destination edge
     * @return true if the last node in fromEdge equals the first node in toEdge
     */
    private boolean edgesMeet(final Edge fromEdge, final Edge toEdge)
    {
        return fromEdge.end().equals(toEdge.start());
    }

    /**
     * Do the fromRoute and the toRoute meet at any point?
     *
     * @param fromRoute
     *            a route consisting of role=FROM members
     * @param toRoute
     *            a route consisting of role=TO members
     * @return True if the from and to routes (or their reverses, if they exist) meet.
     */
    private boolean fromAndToMeet(final Route fromRoute, final Route toRoute)
    {
        final Optional<Edge> fromReversedOptional = fromRoute.reverse().map(Route::end);
        final Optional<Edge> toReversedOptional = toRoute.reverse().map(Route::start);

        // Both mains meet
        return this.edgesMeet(fromRoute.end(), toRoute.start())
                // From main meets to reversed
                || toReversedOptional.map(toReversed -> this.edgesMeet(fromRoute.end(), toReversed))
                        .orElse(false)
                // From reverse meets to main
                || fromReversedOptional
                        .map(fromReversed -> this.edgesMeet(fromReversed, toRoute.start()))
                        .orElse(false)
                // Both reverses meet
                || fromReversedOptional
                        .flatMap(fromReversed -> toReversedOptional
                                .map(toReversed -> this.edgesMeet(fromReversed, toReversed)))
                        .orElse(false);
    }

    /**
     * Validate that all members with role=FROM tags are edges that form a continuous route.
     *
     * @param members
     *            All members of a signboard relation with that are role=FROM.
     * @param signboardOsmIdentifier
     *            The OSM identifier of the original relation.
     * @return Either (Optional[Reason fromRoute is invalid], empty) or (empty, route consisting of
     *         all members).
     */
    private Tuple<Optional<String>, Optional<Route>> fromRoute(final List<RelationMember> members,
            final long signboardOsmIdentifier)
    {
        // All need to be edges
        final Optional<String> nonEdgeReason = this.getNonEdgeReasonFromMembers(members, FROM);
        if (nonEdgeReason.isPresent())
        {
            return Tuple.createTuple(nonEdgeReason, Optional.empty());
        }

        // Get all edges and turn them into a single route
        final Set<Edge> allEdges = members.stream().map(RelationMember::getEntity)
                .map(member -> (Edge) member).map(this::getOsmEdges).flatMap(Set::stream)
                .collect(Collectors.toSet());

        // Need to have at least one edge
        if (allEdges.isEmpty())
        {
            return Tuple.createTuple(Optional.of(this.getLocalizedInstruction(MISSING_MEMBER_INDEX,
                    FROM, signboardOsmIdentifier)), Optional.empty());
        }

        // Try building a route. If it fails, then the edges are disconnected, and this is invalid.
        try
        {
            return Tuple.createTuple(Optional.empty(),
                    Optional.of(Route.fromNonArrangedEdgeSet(allEdges, false)));
        }
        catch (final CoreException error)
        {
            return Tuple.createTuple(
                    Optional.of(this.getLocalizedInstruction(DISCONNECTED_FROM_INDEX)),
                    Optional.empty());
        }
    }

    /**
     * If members has a non-edge, returns an instruction specifying that the edge is incorrect.
     * Otherwise, returns an empty {@link Optional}.
     *
     * @param members
     *            a list of all members of a particular {@link Relation}.
     * @param role
     *            the role which all of these members are
     * @return either an instruction explaining that all members must be edges, or an empty
     *         optional.
     */
    private Optional<String> getNonEdgeReasonFromMembers(final List<RelationMember> members,
            final String role)
    {
        return members.stream().map(RelationMember::getEntity)
                .filter(member -> !member.getType().equals(ItemType.EDGE)).findAny()
                .map(nonEdge -> this.getLocalizedInstruction(WRONG_TYPE_INDEX, role,
                        nonEdge.getOsmIdentifier(), this.osmTypeFromAtlasType(nonEdge.getType())));
    }

    /**
     * Get all edges with the same OSM identifier.
     *
     * @param edge
     *            Any edge
     * @return All edges that have the same OSM identifier as edge.
     */
    private Set<Edge> getOsmEdges(final Edge edge)
    {
        final EdgeWalker walker = new OsmWayWalker(edge);
        return walker.collectEdges().stream().filter(Edge::isMainEdge).collect(Collectors.toSet());
    }

    /**
     * Given an Atlas type, return a string representing an OSM type.
     *
     * @param itemType
     *            The type of an atlas object
     * @return A string representing the corresponding OSM object
     */
    private String osmTypeFromAtlasType(final ItemType itemType)
    {
        switch (itemType)
        {
            case NODE:
            case POINT:
                return "node";
            case EDGE:
            case LINE:
            case AREA:
                return "way";
            case RELATION:
                return "relation";
            default:
                throw new CoreException("ItemType had an unexpected value");
        }
    }

    /**
     * Ensure that all members with role=TO are edges, are a part of a single OSM way, and form a
     * continuous route.
     *
     * @param members
     *            All members of a relation that have a role=TO.
     * @param osmIdentifier
     *            The OSM identifier of the original relation.
     * @return Either (Optional[Reason toRoute is invalid], empty) or (empty, route consisting of
     *         all members)
     */
    private Tuple<Optional<String>, Optional<Route>> toRoute(final List<RelationMember> members,
            final long osmIdentifier)
    {
        // All need to be edges
        final Optional<String> nonEdgeReason = this.getNonEdgeReasonFromMembers(members, TO);
        if (nonEdgeReason.isPresent())
        {
            return Tuple.createTuple(nonEdgeReason, Optional.empty());
        }

        // Turn each OSM way into a route
        final Set<Route> ways = members.stream().map(RelationMember::getEntity)
                .map(member -> (Edge) member).map(this::getOsmEdges)
                // reversed edges will sometimes produce empty sets here
                .filter(edges -> !edges.isEmpty())
                .map(edges -> Route.fromNonArrangedEdgeSet(edges, false))
                .collect(Collectors.toSet());

        // There should only be one route
        if (ways.size() > 1)
        {
            return Tuple.createTuple(
                    Optional.of(
                            this.getLocalizedInstruction(EXTRA_MEMBER_INDEX, TO, osmIdentifier)),
                    Optional.empty());
        }
        else if (ways.isEmpty())
        {
            return Tuple.createTuple(
                    Optional.of(
                            this.getLocalizedInstruction(MISSING_MEMBER_INDEX, TO, osmIdentifier)),
                    Optional.empty());
        }

        return Tuple.createTuple(Optional.empty(), Optional.of(Iterables.getOnlyElement(ways)));
    }
}
