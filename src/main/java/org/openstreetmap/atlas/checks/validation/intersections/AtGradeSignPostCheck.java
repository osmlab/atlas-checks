package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import com.google.common.collect.ImmutableMap;

/**
 * Auto generated Check template
 *
 * @author sayas01
 */
public class AtGradeSignPostCheck extends BaseCheck<String>
{
    private static final long serialVersionUID = -7428641176420422187L;
    private static final String HIGHWAY_FILTER_DEFAULT = "highway->trunk,primary,secondary";
    private static final List<String> CONNECTIONS_TO_PRIMARY = Arrays.asList("trunk", "primary",
            "secondary");
    private static final List<String> CONNECTIONS_TO_TRUNK = Collections.singletonList("primary");
    private static final List<String> CONNECTIONS_TO_SECONDARY = Collections
            .singletonList("primary");
    private static final ImmutableMap<String, List<String>> CONNECTED_HIGHWAY_TYPES_MAP = ImmutableMap
            .of("primary", CONNECTIONS_TO_PRIMARY, "trunk", CONNECTIONS_TO_TRUNK, "secondary",
                    CONNECTIONS_TO_SECONDARY);
    private static final String NO_DESTINATION_SIGN_RELATION_INSTRUCTION = "Node {0,number,#} forms an at-grade junction but is not part of"
            + "a destination sign relation. Verify and create a destination sign relation with the node as \"intersection\" member and following"
            + "connected edges {1}, " + "as \"to\" and \"from\" members.";
    private static final String DEFECTIVE_DESTINATION_SIGN_RELATION_INSTRUCTION = "Node {0,number,#} form an at-grade junction. It is part of destination sign relation(s):"
            + "{1}" + "but the relation(s) are missing \"destination\" tags.";
    private static final String INCOMPLETE_DESTINATION_RELATION_INSTRUCTION = "Node {0,number,#} forms an "
            + "at-grade junction and is part of destination sign relation(s). Following connected edges {1}"
            + "could form destination sign relations with this node. Either add these to existing destination relations or create new destination sign relation"
            + "with these edges and the node";
    private static final String MISSING_DESTINATION_SIGN_INSTRUCTION = "Node {0,number,#} is part of a roundabout and forms an "
            + "at-grade junction with connected edges. Add a destination sign tag to {1}";

    private static final int THREE = 3;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            NO_DESTINATION_SIGN_RELATION_INSTRUCTION,
            DEFECTIVE_DESTINATION_SIGN_RELATION_INSTRUCTION,
            INCOMPLETE_DESTINATION_RELATION_INSTRUCTION, MISSING_DESTINATION_SIGN_INSTRUCTION);

    private final TaggableFilter highwayFilter;
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
        this.highwayFilter = configurationValue(configuration, "highway.filter",
                HIGHWAY_FILTER_DEFAULT, TaggableFilter::forDefinition);
        this.connectedHighwayTypes = this.configurationValue(configuration,
                "connected.highway.types", CONNECTED_HIGHWAY_TYPES_MAP);
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
        return object instanceof Node && this.isConnectedToValidHighways((Node) object)
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
        // Filter all in edges that have valid highway types
        final Set<Edge> inEdges = intersectingNode.inEdges().stream()
                .filter(inEdge -> inEdge.isMasterEdge() && HighwayTag.highwayTag(inEdge).isPresent()
                        && this.highwayFilter.test(inEdge))
                .collect(Collectors.toSet());
        final Set<Edge> outEdges = intersectingNode.outEdges().stream()
                .filter(outEdge -> outEdge.isMasterEdge()
                        && HighwayTag.highwayTag(outEdge).isPresent()
                        && this.highwayFilter.test(outEdge))
                .collect(Collectors.toSet());
        if (inEdges.isEmpty() || outEdges.size() < 2)
        {
            return Optional.empty();
        }
        // For each inEdge, get the list of potentially matching out edges
        // Matching out edges are based on z level and highway type
        final Map<AtlasEntity, Set<AtlasEntity>> inEdgeToOutEdgeMap = new HashMap<>();
        final Map<AtlasEntity, Set<AtlasEntity>> roundAboutInEdgeToOutEdgeMap = new HashMap<>();
        inEdges.forEach(inEdge ->
        {

            final String highwayTypeOfCurrentEdge = HighwayTag.highwayTag(inEdge).get()
                    .getTagValue();
            if (this.connectedHighwayTypes.containsKey(highwayTypeOfCurrentEdge))
            {
                final List<String> listOfValidConnectedHighways = this.connectedHighwayTypes
                        .get(highwayTypeOfCurrentEdge);
                // Filter out edges based on level and layer tags and valid highway types
                final Set<AtlasEntity> filteredOutEdges = outEdges.stream()
                        .filter(outEdge -> LevelTag.areOnSameLevel(inEdge, outEdge)
                                && LayerTag.areOnSameLayer(inEdge, outEdge))
                        .collect(Collectors.toSet());
                // If there are at least two out edges, filter these edges based on their highway
                // types.
                // Check if the highway type of out edge is one of the valid connected highway types
                // based on
                // the incoming edgeg
                if (filteredOutEdges.size() >= 2)
                {
                    final Set<AtlasEntity> filteredOutEdgesBasedOnHighwayTypes = filteredOutEdges
                            .stream().filter(atlasEntity ->
                            {
                                final Optional<HighwayTag> highwayTag = HighwayTag
                                        .highwayTag(atlasEntity);
                                return highwayTag.isPresent() && listOfValidConnectedHighways
                                        .contains(highwayTag.get().getTagValue());
                            }).collect(Collectors.toSet());

                    if (filteredOutEdgesBasedOnHighwayTypes.stream()
                            .anyMatch(JunctionTag::isRoundabout))
                    {
                        roundAboutInEdgeToOutEdgeMap.put(inEdge,
                                filteredOutEdgesBasedOnHighwayTypes);
                    }
                    else if (!filteredOutEdgesBasedOnHighwayTypes.isEmpty())
                    {

                        inEdgeToOutEdgeMap.put(inEdge, filteredOutEdgesBasedOnHighwayTypes);
                    }
                }
            }
        });
        // If there are no valid intersection, return Optional.empty()
        if (inEdgeToOutEdgeMap.isEmpty() && roundAboutInEdgeToOutEdgeMap.isEmpty())
        {
            return Optional.empty();
        }
        int instructionIndex = -1;
        // If the node is not part of any destination sign relations, flag it
        final Set<AtlasEntity> entitiesToBeFlagged = new HashSet<>();
        // Collect all destination sign relations, the node is member of
        final Optional<Set<Relation>> destinationSignRelations = this
                .getParentDestinationRelations(intersectingNode);

        // If the node is part of destination sign relation, check if destination tag of the
        // relation is missing or
        // if there are any missing relations that the node could be part of based on from and to
        // edges
        if (destinationSignRelations.isPresent())
        {
            final Set<Relation> defectiveDestinationSignRelations = this
                    .getRelationsWithMissingDestinationTag(destinationSignRelations.get());
            if (!defectiveDestinationSignRelations.isEmpty())
            {
                this.markAsFlagged(String.valueOf(intersectingNode.getIdentifier()));
                instructionIndex = 1;
                entitiesToBeFlagged.addAll(defectiveDestinationSignRelations);
            }
            else if (!roundAboutInEdgeToOutEdgeMap.isEmpty())
            {
                roundAboutInEdgeToOutEdgeMap.forEach((inEdge, setOfOutEdge) ->
                {
                    final Optional<AtlasEntity> roundaboutEdge = setOfOutEdge.stream()
                            .filter(JunctionTag::isRoundabout).findAny();
                    final Optional<AtlasEntity> roundaboutExitEdge = setOfOutEdge.stream()
                            .filter(outEdge -> !JunctionTag.isRoundabout(outEdge)).findAny();
                    if (roundaboutEdge.isPresent() && roundaboutExitEdge.isPresent())
                    {
                        final Set<AtlasEntity> roundaboutEdges = new SimpleEdgeWalker(
                                (Edge) roundaboutEdge.get(), this.isRoundaboutEdge()).collectEdges()
                                        .stream().map(AtlasEntity.class::cast)
                                        .collect(Collectors.toSet());
                        final AtlasEntity exitEdge = roundaboutExitEdge.get();
                        final Optional<Set<AtlasEntity>> roundAboutEdgesNotPartOfRelations = this
                                .connectedEdgesNotPartOfRelation(exitEdge, roundaboutEdges,
                                        destinationSignRelations.get());
                        roundAboutEdgesNotPartOfRelations.ifPresent(entitiesToBeFlagged::addAll);
                    }

                });

                if (!entitiesToBeFlagged.isEmpty())
                {
                    instructionIndex = 2;
                    this.markAsFlagged(String.valueOf(intersectingNode.getIdentifier()));
                }
            }
            else
            {
                // If there are any missing destination sign relation that the node should be part
                // of, flag it
                final Set<AtlasEntity> connectedEdgesNotFormDestinationRelation = this
                        .getConnectedEdgesNotFormDestinationRelation(inEdgeToOutEdgeMap,
                                destinationSignRelations.get());
                if (!connectedEdgesNotFormDestinationRelation.isEmpty())
                {
                    this.markAsFlagged(String.valueOf(intersectingNode.getIdentifier()));
                    instructionIndex = 2;
                    entitiesToBeFlagged.addAll(connectedEdgesNotFormDestinationRelation);
                }
            }
        }
        else if (roundAboutInEdgeToOutEdgeMap.isEmpty())
        {
            this.markAsFlagged(String.valueOf(intersectingNode.getIdentifier()));
            inEdgeToOutEdgeMap.forEach((inEdge, setOfOutEdge) ->
            {
                entitiesToBeFlagged.add(inEdge);
                entitiesToBeFlagged.addAll(setOfOutEdge);
            });
            instructionIndex = 0;

        }
        else
        {
            roundAboutInEdgeToOutEdgeMap.forEach((inEdge, setOfOutEdge) ->
            {
                final Optional<AtlasEntity> roundaboutEdge = setOfOutEdge.stream()
                        .filter(JunctionTag::isRoundabout).findAny();
                final Optional<AtlasEntity> roundaboutExitEdge = setOfOutEdge.stream()
                        .filter(outEdge -> !JunctionTag.isRoundabout(outEdge)).findAny();
                if (roundaboutEdge.isPresent() && roundaboutExitEdge.isPresent())
                {
                    final AtlasEntity exitEdge = roundaboutExitEdge.get();
                    if ((OneWayTag.isExplicitlyTwoWay(exitEdge)
                            && exitEdge.tag(DestinationForwardTag.KEY) == null)
                            || (!OneWayTag.isExplicitlyTwoWay(exitEdge)
                                    && exitEdge.tag(DestinationTag.KEY) == null))
                    {
                        entitiesToBeFlagged.add(roundaboutEdge.get());
                        entitiesToBeFlagged.add(roundaboutExitEdge.get());
                    }
                }

            });
            if (!entitiesToBeFlagged.isEmpty())
            {
                instructionIndex = THREE;
                this.markAsFlagged(String.valueOf(intersectingNode.getIdentifier()));
            }

        }
        final List<String> identifiers = this.getIdentifiers(entitiesToBeFlagged);
        entitiesToBeFlagged.add(intersectingNode);
        return instructionIndex == -1 ? Optional.empty()
                : Optional.of(this.createFlag(entitiesToBeFlagged,
                        this.getLocalizedInstruction(instructionIndex,
                                intersectingNode.getIdentifier(),
                                new StringList(identifiers).join(", "))));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private Optional<Set<AtlasEntity>> connectedEdgesNotPartOfRelation(final AtlasEntity fromEdge,
            final Set<AtlasEntity> toEdges, final Set<Relation> destinationSignRelations)
    {
        final Set<AtlasEntity> outEdges = new HashSet<>(toEdges);
        final Set<AtlasEntity> allRelationMembers = destinationSignRelations.stream()
                .flatMap(destinationSignRelation ->
                {
                    final RelationMemberList relationMembers = destinationSignRelation
                            .allKnownOsmMembers();
                    return relationMembers.stream().map(RelationMember::getEntity);
                }).collect(Collectors.toSet());
        if (!allRelationMembers.contains(fromEdge))
        {
            outEdges.add(fromEdge);
            return Optional.of(outEdges);
        }
        allRelationMembers.forEach(outEdges::remove);
        if (!outEdges.isEmpty())
        {
            outEdges.add(fromEdge);
            return Optional.of(outEdges);
        }
        return Optional.empty();
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
            final Optional<Set<AtlasEntity>> atlasEntities = this.connectedEdgesNotPartOfRelation(
                    key, inEdgeToOutEdgeMap.get(key), destinationSignRelations);
            return atlasEntities.map(Stream::of).orElseGet(Stream::empty);
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
     * Collect all destination sign relations that the input atlas entity is member of.
     *
     * @param atlasEntity
     *            any {@link AtlasEntity}
     * @return Optional<Set<Relation>> that the atlasEntity is member of
     */
    private Optional<Set<Relation>> getParentDestinationRelations(final AtlasEntity atlasEntity)
    {
        return Optional.of(atlasEntity.relations().stream()
                .filter(relation -> RelationTypeTag.DESTINATION_SIGN.toString()
                        .equalsIgnoreCase(relation.tag(RelationTypeTag.KEY)))
                .collect(Collectors.toSet()));
    }

    private Set<Relation> getRelationsWithMissingDestinationTag(
            final Set<Relation> destinationSignRelations)
    {
        return destinationSignRelations.stream()
                .filter(relation -> relation.tag(DestinationTag.KEY) == null)
                .collect(Collectors.toSet());
    }

    private boolean isConnectedToValidHighways(final Node node)
    {
        return node.valence() >= THREE && node.inEdges().stream().anyMatch(this.highwayFilter)
                && node.outEdges().stream().filter(this.highwayFilter).count() >= 2;
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
}
