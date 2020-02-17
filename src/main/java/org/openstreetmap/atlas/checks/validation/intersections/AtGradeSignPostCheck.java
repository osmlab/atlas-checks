package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.walker.SimpleEdgeWalker;
import org.openstreetmap.atlas.tags.DestinationForwardTag;
import org.openstreetmap.atlas.tags.DestinationTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.LevelTag;
import org.openstreetmap.atlas.tags.OneWayTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.direction.EdgeDirectionComparator;
import org.openstreetmap.atlas.utilities.scalars.Angle;

import com.google.common.collect.ImmutableMap;

/**
 * Check flags at-grade intersections with missing destination_sign tags or destinations_sign
 * relations. An at-grade intersection is an intersection between two edges at the same z-level. In
 * case of roundabout edges, intersection between exit edge and roundabout edge are checked for
 * either destination_sign relation or destination:forward tag, in case of bi-directional exit edge,
 * or destination tag, in case of unidirectional exit edge.
 *
 * @author sayas01
 */
public class AtGradeSignPostCheck extends BaseCheck<String>
{
    /**
     * A class for holding flagged intersection items and corresponding flag instruction index
     */
    private class FlaggedIntersection
    {
        private int instructionIndex;
        private Set<AtlasEntity> setOfFlaggedItems;

        FlaggedIntersection(final int instructionIndex, final Set<AtlasEntity> setOfFlaggedItems)
        {
            this.instructionIndex = instructionIndex;
            this.setOfFlaggedItems = setOfFlaggedItems;
        }

        private Set<AtlasEntity> getFlaggedItems()
        {
            return this.setOfFlaggedItems;
        }

        private Integer getInstructionIndex()
        {
            return this.instructionIndex;
        }
    }

    private static final long serialVersionUID = -7428641176420422187L;
    // Primary road (inEdge) connected to trunk, primary, secondary roads (outEdges) are treated as
    // valid intersection
    private static final List<String> CONNECTIONS_TO_PRIMARY = Arrays.asList("trunk", "primary",
            "secondary");
    // Trunk road (inEdge) connected to primary road (outEdge) is treated as valid intersection
    private static final List<String> CONNECTIONS_TO_TRUNK = Collections.singletonList("primary");
    // Secondary road (inEdge) connected to primary road (outEdge) is treated as valid intersection
    private static final List<String> CONNECTIONS_TO_SECONDARY = Collections
            .singletonList("primary");
    private static final ImmutableMap<String, List<String>> CONNECTED_HIGHWAY_TYPES_MAP = ImmutableMap
            .of("primary", CONNECTIONS_TO_PRIMARY, "trunk", CONNECTIONS_TO_TRUNK, "secondary",
                    CONNECTIONS_TO_SECONDARY);
    private static final String NO_DESTINATION_SIGN_RELATION_INSTRUCTION = "Node {0,number,#} forms an at-grade junction but is not part of "
            + "a destination sign relation. Verify and create a destination sign relation with the node as \"intersection\" member and following "
            + "connected edges {1}, " + "as \"to\" and \"from\" members.";
    private static final String DESTINATION_SIGN_RELATION_MISSING_DESTINATION_TAG_INSTRUCTION = "Node {0,number,#} form an at-grade junction. It is part of destination sign relation(s): "
            + "{1}" + "but the relation(s) are missing \"destination\" tags.";
    private static final String INCOMPLETE_DESTINATION_RELATION_INSTRUCTION = "Node {0,number,#} forms an "
            + "at-grade junction and is part of destination sign relation(s). But the following connected edges {1} "
            + "could also form destination sign relations with this node. Create new destination sign relation "
            + "with these edges and the node.";
    private static final String ROUNDABOUT_EDGE_MISSING_DESTINATION_SIGN_RELATION = "Node {0,number,#} is part of a roundabout and forms an "
            + "at-grade junction with connected edges. Add destination sign relations with the node as \"intersection\" member and following "
            + "connected edges {1}, as \"to\" and \"from\" members and add destination sign tag to the connected edges.";
    private static final String ROUNDABOUT_EDGE_INCOMPLETE_DESTINATION_SIGN_RELATION = "Node {0,number,#} is part of a roundabout and forms an "
            + "at-grade junction. It is part of destination sign relation(s). Either the existing relations are missing destination sign tag or following connected edges {1} "
            + "could also form destination sign relations with this node. Either add destination tags to existing relations or create new destination sign relation "
            + "with these edges and the node.";
    private static final String NON_ROUNDABOUT_INTERSECTION_MAP = "nonRoundaboutMatchingEdgesMap";
    private static final String ROUNDABOUT_INTERSECTION_MAP = "roundaboutMatchingEdgesMap";

    private static final int INSTRUCTION_INDEX_ZERO = 0;
    private static final int INSTRUCTION_INDEX_ONE = 1;
    private static final int INSTRUCTION_INDEX_TWO = 2;
    private static final int INSTRUCTION_INDEX_THREE = 3;
    private static final int INSTRUCTION_INDEX_FOUR = 4;

    private static final int MINIMUM_NODE_VALENCE = 3;
    private static final Angle DEFAULT_OPPOSITE_DIRECTION_LOWER_LIMIT = Angle.degrees(171);
    private static final Angle DEFAULT_OPPOSITE_DIRECTION_UPPER_LIMIT = Angle.degrees(-171);
    private static final Angle DEFAULT_SAME_DIRECTION_LOWER_LIMIT = Angle.degrees(-100);
    private static final Angle DEFAULT_SAME_DIRECTION_UPPER_LIMIT = Angle.degrees(9);
    private static final EdgeDirectionComparator EDGE_DIRECTION_COMPARATOR = new EdgeDirectionComparator(
            DEFAULT_OPPOSITE_DIRECTION_LOWER_LIMIT, DEFAULT_OPPOSITE_DIRECTION_UPPER_LIMIT,
            DEFAULT_SAME_DIRECTION_LOWER_LIMIT, DEFAULT_SAME_DIRECTION_UPPER_LIMIT);

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            NO_DESTINATION_SIGN_RELATION_INSTRUCTION,
            DESTINATION_SIGN_RELATION_MISSING_DESTINATION_TAG_INSTRUCTION,
            INCOMPLETE_DESTINATION_RELATION_INSTRUCTION,
            ROUNDABOUT_EDGE_MISSING_DESTINATION_SIGN_RELATION,
            ROUNDABOUT_EDGE_INCOMPLETE_DESTINATION_SIGN_RELATION);

    private final Set<String> highwayFilter;
    private Map<String, List<String>> connectedHighwayTypes;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public AtGradeSignPostCheck(final Configuration configuration)
    {
        super(configuration);
        this.connectedHighwayTypes = this.configurationValue(configuration,
                "connected.highway.types", CONNECTED_HIGHWAY_TYPES_MAP);
        this.highwayFilter = new HashSet<>(this.connectedHighwayTypes.keySet());
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Node && ((Node) object).valence() >= MINIMUM_NODE_VALENCE
                && !this.isFlagged(String.valueOf(object.getIdentifier()));
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Node intersectingNode = (Node) object;
        // Filter and sort all in edges that have valid highway types
        final List<Edge> inEdges = intersectingNode.inEdges().stream()
                .filter(this::isValidIntersectingEdge)
                .sorted(Comparator.comparingLong(AtlasObject::getIdentifier))
                .collect(Collectors.toList());
        // Filter all out edges that have valid highway types
        final Set<Edge> outEdges = intersectingNode.outEdges().stream()
                .filter(this::isValidIntersectingEdge).collect(Collectors.toSet());
        // Terminate if there isn't at least one inEdge or two out edges with valid highway types
        if (inEdges.isEmpty() || outEdges.size() < 2)
        {
            return Optional.empty();
        }
        // For each inEdge, get the list of potentially matching out edges
        // Matching out edges are based on z level and highway type.
        // For each inEdge, store the inEdge and corresponding outEdges in inEdgeToOutEdgeMap
        // If any of the out edge is a roundabout edge, store the roundabout edges and the inEdge in
        // roundAboutInEdgeToOutEdgeMap
        final Map<String, Map<AtlasEntity, Set<AtlasEntity>>> mapOfMatchingInAndOutEdges = this
                .populateInEdgeToOutEdgeMaps(inEdges, outEdges);
        final Map<AtlasEntity, Set<AtlasEntity>> inEdgeToOutEdgeMap = mapOfMatchingInAndOutEdges
                .getOrDefault(NON_ROUNDABOUT_INTERSECTION_MAP, null);
        final Map<AtlasEntity, Set<AtlasEntity>> roundAboutInEdgeToOutEdgeMap = mapOfMatchingInAndOutEdges
                .getOrDefault(ROUNDABOUT_INTERSECTION_MAP, null);
        // If there are no valid intersection, return Optional.empty()
        if ((inEdgeToOutEdgeMap == null || inEdgeToOutEdgeMap.isEmpty())
                && (roundAboutInEdgeToOutEdgeMap == null || roundAboutInEdgeToOutEdgeMap.isEmpty()))
        {
            return Optional.empty();
        }
        // Collect all destination sign relations, the node is a member of
        final Optional<Set<Relation>> destinationSignRelations = this
                .getParentDestinationSignRelations(intersectingNode);
        final FlaggedIntersection flaggedIntersection = destinationSignRelations.isEmpty()
                ? this.getFlaggedIntersection(roundAboutInEdgeToOutEdgeMap,
                        inEdgeToOutEdgeMap)
                : this.getIntersectionsWithIncompleteDestinationSignRelation(
                        roundAboutInEdgeToOutEdgeMap, inEdgeToOutEdgeMap, intersectingNode,
                        destinationSignRelations.get());
        final int instructionIndex = flaggedIntersection.getInstructionIndex();
        final Set<AtlasEntity> entitiesToBeFlagged = flaggedIntersection.getFlaggedItems();
        if (instructionIndex == -1)
        {
            return Optional.empty();
        }
        final List<String> identifiers = this.getIdentifiers(entitiesToBeFlagged);
        entitiesToBeFlagged.add(intersectingNode);
        this.markAsFlagged(String.valueOf(intersectingNode.getIdentifier()));
        return Optional.of(
                this.createFlag(entitiesToBeFlagged, this.getLocalizedInstruction(instructionIndex,
                        intersectingNode.getIdentifier(), new StringList(identifiers).join(", "))));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Collects all edges that are not part of a relation
     *
     * @param fromEdge
     *            inEdge
     * @param toEdges
     *            set of out edges
     * @param destinationSignRelations
     *            set of existing destination sign relations
     * @return Optional<Set<AtlasEntity>> if there are edges that are not part of destination sign
     *         relations
     */
    private Optional<Set<AtlasEntity>> connectedEdgesNotPartOfRelation(final AtlasEntity fromEdge,
            final Set<AtlasEntity> toEdges, final Set<Relation> destinationSignRelations)
    {
        final Set<AtlasEntity> outEdges = new HashSet<>(toEdges);
        final Set<AtlasEntity> filteredOutEdges = outEdges.stream()
                .filter(outEdge -> this.isMissingDestinationTag((Edge) outEdge))
                .collect(Collectors.toSet());
        final Set<AtlasEntity> allRelationMembers = destinationSignRelations.stream()
                .flatMap(destinationSignRelation ->
                {
                    final RelationMemberList relationMembers = destinationSignRelation
                            .allKnownOsmMembers();
                    return relationMembers.stream().map(RelationMember::getEntity);
                }).collect(Collectors.toSet());
        if (!allRelationMembers.contains(fromEdge))
        {
            filteredOutEdges.add(fromEdge);
            return Optional.of(filteredOutEdges);
        }
        allRelationMembers.forEach(filteredOutEdges::remove);
        if (!filteredOutEdges.isEmpty())
        {
            filteredOutEdges.add(fromEdge);
            return Optional.of(filteredOutEdges);
        }
        return Optional.empty();
    }

    /**
     * Collect all roundabout edges and exit edges that are missing destination sign relations or
     * missing destination sign tags
     *
     * @param roundAboutInEdgeToOutEdgeMap
     *            Map<AtlasEntity, Set<AtlasEntity>> where key is inEdge and value is set of
     *            outEdges of the rounabout
     * @param destinationSignRelations
     *            set of existing destoination sign relations
     * @return set of edges that meet the above criteria
     */
    private Set<AtlasEntity> getAllRoundaboutEdgesMissingTagsOrRelations(
            final Map<AtlasEntity, Set<AtlasEntity>> roundAboutInEdgeToOutEdgeMap,
            final Set<Relation> destinationSignRelations)
    {
        final Set<AtlasEntity> entitiesToBeFlagged = new HashSet<>();
        roundAboutInEdgeToOutEdgeMap.forEach((inEdge, setOfOutEdge) ->
        {
            final Optional<AtlasEntity> roundaboutEdge = setOfOutEdge.stream()
                    .filter(JunctionTag::isRoundabout).findFirst();
            final Optional<AtlasEntity> roundaboutExitEdge = setOfOutEdge.stream()
                    .filter(outEdge -> !JunctionTag.isRoundabout(outEdge)).findFirst();
            if (roundaboutEdge.isPresent() && roundaboutExitEdge.isPresent())
            {
                final Set<AtlasEntity> roundaboutEdges = this
                        .getRoundaboutEdges((Edge) roundaboutEdge.get());
                final AtlasEntity exitEdge = roundaboutExitEdge.get();
                // If the destination sign relation is missing destination tag, flag it
                if (this.isMissingDestinationTag((Edge) exitEdge))
                {
                    entitiesToBeFlagged.addAll(roundaboutEdges);
                    entitiesToBeFlagged.add(roundaboutExitEdge.get());
                }
                final Optional<Set<AtlasEntity>> roundAboutEdgesNotPartOfRelations = this
                        .connectedEdgesNotPartOfRelation(exitEdge, roundaboutEdges,
                                destinationSignRelations);
                // If there are missing destination sign relations, flag it
                roundAboutEdgesNotPartOfRelations.ifPresent(entitiesToBeFlagged::addAll);
            }
        });
        return entitiesToBeFlagged;
    }

    /**
     * @param inEdgeToOutEdgeMap
     *            inEdge to outEdge map
     * @param destinationSignRelations
     *            destinationSignRelations
     * @return
     */
    private Set<AtlasEntity> getConnectedEdgesNotFormDestinationRelation(
            final Map<AtlasEntity, Set<AtlasEntity>> inEdgeToOutEdgeMap,
            final Set<Relation> destinationSignRelations)
    {
        return inEdgeToOutEdgeMap.entrySet().stream().flatMap(atlasEntitySetEntry ->
        {
            final AtlasEntity key = atlasEntitySetEntry.getKey();
            return this.connectedEdgesNotPartOfRelation(key, inEdgeToOutEdgeMap.get(key),
                    destinationSignRelations).stream();
        }).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * Collects all atlas identifiers of given set of {@link AtlasObject}s
     *
     * @param objects
     *            set of {@link AtlasObject}s
     * @return {@link Iterable<String>} containing the atlas identifiers of input objects
     */
    private List<String> getIdentifiers(final Set<AtlasEntity> objects)
    {
        return Iterables.stream(objects).map(AtlasEntity::getIdentifier).map(String::valueOf)
                .collectToList();
    }

    /**
     * Node could be part of multiple destination_sign relations. This method collect the
     * intersecting items that are not part of existing destination_sign relation of the node or if
     * the relations are missing a destination_sign tag.
     *
     * @param roundAboutInEdgeToOutEdgeMap
     *            map with roundabout inEdge and outEdges
     * @param inEdgeToOutEdgeMap
     *            map with non-roundabout inEdge and outEdges
     * @param intersectingNode
     *            {@link Node}
     * @param destinationSignRelations
     *            set of relations the node is a member of
     * @return FlaggedIntersection with instruction index and set of flagged items with incomplete
     *         destination_sign relation
     */
    private FlaggedIntersection getIntersectionsWithIncompleteDestinationSignRelation(
            final Map<AtlasEntity, Set<AtlasEntity>> roundAboutInEdgeToOutEdgeMap,
            final Map<AtlasEntity, Set<AtlasEntity>> inEdgeToOutEdgeMap,
            final Node intersectingNode, final Set<Relation> destinationSignRelations)
    {
        // If the node is part of destination sign relation, check if destination tag of the
        // relation is missing or if there are any missing relations that the node could be part of
        // based on from and to edges
        final Set<AtlasEntity> entitiesToBeFlagged = new HashSet<>();
        int instructionIndex = -1;
        // Flag all roundabout edges that are missing destination sign relations or missing
        // destination sign tags for existing relations
        if (!roundAboutInEdgeToOutEdgeMap.isEmpty())
        {
            final Set<AtlasEntity> allRoundaboutEdgesMissingTagsOrRelations = this
                    .getAllRoundaboutEdgesMissingTagsOrRelations(roundAboutInEdgeToOutEdgeMap,
                            destinationSignRelations);
            if (!allRoundaboutEdgesMissingTagsOrRelations.isEmpty())
            {
                entitiesToBeFlagged.addAll(allRoundaboutEdgesMissingTagsOrRelations);
                instructionIndex = INSTRUCTION_INDEX_FOUR;
            }
        }
        else
        {
            final Set<Relation> destinationSignRelationsMissingTag = this
                    .getRelationsWithMissingDestinationTag(destinationSignRelations);
            // Flag if destination sign tag is missing
            if (!destinationSignRelationsMissingTag.isEmpty())
            {
                this.markAsFlagged(String.valueOf(intersectingNode.getIdentifier()));
                instructionIndex = INSTRUCTION_INDEX_ONE;
                entitiesToBeFlagged.addAll(destinationSignRelationsMissingTag);
            }
            else if (inEdgeToOutEdgeMap != null)
            {
                // If there are any missing destination sign relation that the node should be
                // part of, flag it
                final Set<AtlasEntity> connectedEdgesNotFormDestinationRelation = this
                        .getConnectedEdgesNotFormDestinationRelation(inEdgeToOutEdgeMap,
                                destinationSignRelations);
                if (!connectedEdgesNotFormDestinationRelation.isEmpty())
                {
                    instructionIndex = INSTRUCTION_INDEX_TWO;
                    entitiesToBeFlagged.addAll(connectedEdgesNotFormDestinationRelation);
                }
            }
        }
        return new FlaggedIntersection(instructionIndex, entitiesToBeFlagged);
    }

    /**
     * Return a FlaggedIntersection with the items in the input params and appropriate instruction
     * index based on the input params.
     *
     * @param roundAboutInEdgeToOutEdgeMap
     *            map with roundabout inEdge and outEdges
     * @param inEdgeToOutEdgeMap
     *            map with non-roundabout inEdge and outEdges
     * @return FlaggedIntersection with instruction index and set of flagged items based on the
     * input params
     */
    private FlaggedIntersection getFlaggedIntersection(
            final Map<AtlasEntity, Set<AtlasEntity>> roundAboutInEdgeToOutEdgeMap,
            final Map<AtlasEntity, Set<AtlasEntity>> inEdgeToOutEdgeMap)
    {
        final Set<AtlasEntity> entitiesToBeFlagged = new HashSet<>();
        int instructionIndex = -1;
        // Flag all in and out edges
        if (roundAboutInEdgeToOutEdgeMap.isEmpty() && inEdgeToOutEdgeMap != null)
        {
            inEdgeToOutEdgeMap.forEach((inEdge, setOfOutEdge) ->
            {
                entitiesToBeFlagged.add(inEdge);
                entitiesToBeFlagged.addAll(setOfOutEdge);
            });
            if (!entitiesToBeFlagged.isEmpty())
            {
                //
                instructionIndex = INSTRUCTION_INDEX_ZERO;
            }
        }
        // Flag all roundabout edges
        else
        {
            roundAboutInEdgeToOutEdgeMap.forEach((inEdge, setOfOutEdge) ->
            {
                // Ideally there would only be one roundabout edge and one exit edge per node
                final Optional<AtlasEntity> roundaboutEdge = JunctionTag.isRoundabout(inEdge)
                        ? Optional.of(inEdge)
                        : setOfOutEdge.stream().filter(JunctionTag::isRoundabout).findFirst();
                final Optional<AtlasEntity> roundaboutExitEdge = setOfOutEdge.stream()
                        .filter(outEdge -> !JunctionTag.isRoundabout(outEdge)).findFirst();
                if (roundaboutEdge.isPresent() && roundaboutExitEdge.isPresent())
                {
                    entitiesToBeFlagged
                            .addAll(this.getRoundaboutEdges((Edge) roundaboutEdge.get()));
                    entitiesToBeFlagged.add(roundaboutExitEdge.get());
                }
            });
            if (!entitiesToBeFlagged.isEmpty())
            {
                instructionIndex = INSTRUCTION_INDEX_THREE;
            }
        }
        return new FlaggedIntersection(instructionIndex, entitiesToBeFlagged);
    }

    /**
     * Collect all destination sign relations that the input atlas entity is member of.
     *
     * @param atlasEntity
     *            any {@link AtlasEntity}
     * @return Optional<Set<Relation>> that the atlasEntity is member of
     */
    private Optional<Set<Relation>> getParentDestinationSignRelations(final AtlasEntity atlasEntity)
    {
        final Set<Relation> setOfDestinationSignRelations = atlasEntity.relations().stream()
                .filter(relation -> RelationTypeTag.DESTINATION_SIGN.toString()
                        .equalsIgnoreCase(relation.tag(RelationTypeTag.KEY)))
                .collect(Collectors.toSet());
        return setOfDestinationSignRelations.isEmpty() ? Optional.empty()
                : Optional.of(setOfDestinationSignRelations);
    }

    /**
     * Collects all destination sign relations with missing destination sign tag
     *
     * @param destinationSignRelations
     *            set of destination sign relations
     * @return set of relations with missing destination sign tag
     */
    private Set<Relation> getRelationsWithMissingDestinationTag(
            final Set<Relation> destinationSignRelations)
    {
        return destinationSignRelations.stream()
                .filter(relation -> relation.tag(DestinationTag.KEY) == null)
                .collect(Collectors.toSet());
    }

    /**
     * Collects all roundabout edges starting with the given given
     *
     * @param startEdge
     *            {@link Edge}
     * @return Set of roundabout edges
     */
    private Set<AtlasEntity> getRoundaboutEdges(final Edge startEdge)
    {
        return new SimpleEdgeWalker(startEdge, this.isRoundaboutEdge()).collectEdges().stream()
                .map(AtlasEntity.class::cast).collect(Collectors.toSet());
    }

    /**
     * Checks if given outEdge is at the same z level and in the same direction as that of the
     * inEdge
     *
     * @param inEdge
     *            inEdge
     * @param outEdge
     *            outEdge
     * @return true if the outEdge matches the above criteria for the given inEdge
     */
    private boolean isMatchingOutEdge(final Edge inEdge, final Edge outEdge)
    {
        return LevelTag.areOnSameLevel(inEdge, outEdge) && LayerTag.areOnSameLayer(inEdge, outEdge)
                && EDGE_DIRECTION_COMPARATOR.isSameDirection(inEdge, outEdge, true);
    }

    /**
     * Checks if the edge is missing DestinationForwardTag if two way or is missing a destination
     * tag if one way
     *
     * @param edge
     *            any edge
     * @return true if the edge is missing the destination tags
     */
    private boolean isMissingDestinationTag(final Edge edge)
    {
        return (OneWayTag.isExplicitlyTwoWay(edge) && edge.tag(DestinationForwardTag.KEY) == null)
                || (!OneWayTag.isExplicitlyTwoWay(edge) && edge.tag(DestinationTag.KEY) == null);
    }

    /**
     * Function for {@link SimpleEdgeWalker} that gathers connected edges that are part of a
     * roundabout.
     *
     * @return {@link Function} for {@link SimpleEdgeWalker}
     */
    private Function<Edge, Stream<Edge>> isRoundaboutEdge()
    {
        return edge -> edge.connectedEdges().stream()
                .filter(connected -> JunctionTag.isRoundabout(connected)
                        && HighwayTag.isCarNavigableHighway(connected));
    }

    /**
     * Checks if given edge is a valid intersecting edge for an at-grade intersection
     *
     * @param edge
     *            edge
     * @return true if the edge is valid intersecting edge
     */
    private boolean isValidIntersectingEdge(final Edge edge)
    {
        return edge.isMasterEdge() && HighwayTag.highwayTag(edge).isPresent()
                && this.highwayFilter.contains(edge.highwayTag().getTagValue());
    }

    /**
     * Collect matching out edges and corresponding in edge in a map. Store the roundabout edges and
     * non roundabout edges in separate maps.
     *
     * @param inEdges
     *            List<Edge> inEdges
     * @param outEdges
     *            Set<Edge> outEdges
     * @return Map<String, Map<AtlasEntity, Set<AtlasEntity>>> with Map of inEdge to outEdges for
     *         roundabout and non roundabout edges
     */
    private Map<String, Map<AtlasEntity, Set<AtlasEntity>>> populateInEdgeToOutEdgeMaps(
            final List<Edge> inEdges, final Set<Edge> outEdges)
    {
        final Map<AtlasEntity, Set<AtlasEntity>> inEdgeToOutEdgeMap = new HashMap<>();
        final Map<AtlasEntity, Set<AtlasEntity>> roundAboutInEdgeToOutEdgeMap = new HashMap<>();
        inEdges.forEach(inEdge ->
        {
            final Optional<HighwayTag> highwayTag = HighwayTag.highwayTag(inEdge);
            if (highwayTag.isPresent()
                    && this.connectedHighwayTypes.containsKey(highwayTag.get().getTagValue()))
            {
                // Filter out edges based on level and layer tags and valid highway types
                final Set<AtlasEntity> filteredOutEdges = outEdges.stream()
                        .filter(outEdge -> this.isMatchingOutEdge(inEdge, outEdge))
                        .collect(Collectors.toSet());
                // There should be at least 2 valid outEdges
                if (filteredOutEdges.size() >= 2)
                {
                    final String inEdgeHighwayType = highwayTag.get().getTagValue();
                    final List<String> validHighwayTypesOfOutEdge = this.connectedHighwayTypes
                            .get(inEdgeHighwayType);
                    final Set<AtlasEntity> filteredByHighways = filteredOutEdges.stream()
                            .filter(atlasEntity ->
                            {
                                final Optional<HighwayTag> atlasEntityHighway = HighwayTag
                                        .highwayTag(atlasEntity);
                                return atlasEntityHighway.isPresent() && validHighwayTypesOfOutEdge
                                        .contains(atlasEntityHighway.get().getTagValue());
                            }).collect(Collectors.toSet());
                    // If any of the edges is a roundabout, add it to roundabout map
                    if (filteredByHighways.stream().anyMatch(JunctionTag::isRoundabout)
                            || JunctionTag.isRoundabout(inEdge))
                    {
                        roundAboutInEdgeToOutEdgeMap.put(inEdge, filteredByHighways);
                    }
                    else if (!filteredByHighways.isEmpty())
                    {
                        inEdgeToOutEdgeMap.put(inEdge, filteredByHighways);
                    }
                }

            }
        });
        final Map<String, Map<AtlasEntity, Set<AtlasEntity>>> mapOfMatchingInAndOutEdges = new HashMap<>();
        mapOfMatchingInAndOutEdges.put(NON_ROUNDABOUT_INTERSECTION_MAP, inEdgeToOutEdgeMap);
        mapOfMatchingInAndOutEdges.put(ROUNDABOUT_INTERSECTION_MAP, roundAboutInEdgeToOutEdgeMap);
        return mapOfMatchingInAndOutEdges;
    }
}
