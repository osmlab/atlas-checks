package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.FootTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.OneWayTag;
import org.openstreetmap.atlas.tags.SidewalkTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.direction.EdgeDirectionComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This check looks for any non-motorway single carriageway edges with no foot tags that cross any
 * high-priority roads that are dual carriageways. These intersections can cause issues with
 * walkable routing.
 *
 * @author cameron_frenette
 * @author savannahostrowski
 */
public class UnwalkableWaysCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -2894765496856223796L;
    private static final Logger logger = LoggerFactory.getLogger(UnwalkableWaysCheck.class);
    // Instructions
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList("Way {0,number,#} may be unwalkable and needs verification if safe "
                    + "pedestrian crossing exists. If it does, it should be marked ‘foot=yes’. If "
                    + "safe pedestrian crossing does not exist, the Way should be marked ‘foot=no'.");
    // The minimum highway type considered for potential dual carriageways
    private static final HighwayTag MINIMUM_HIGHWAY_TYPE_DEFAULT = HighwayTag.PRIMARY;
    private static final List<String> DEFAULT_WALKWAY_TAGS = Arrays.asList(
            HighwayTag.CYCLEWAY.toString(), HighwayTag.FOOTWAY.toString(),
            HighwayTag.PATH.toString(), HighwayTag.STEPS.toString(),
            HighwayTag.PEDESTRIAN.toString(), HighwayTag.LIVING_STREET.toString());
    private static final EdgeDirectionComparator EDGE_DIRECTION_COMPARATOR = new EdgeDirectionComparator();
    // Config options
    private final Boolean includeDualCrossingDualCarriageways;
    private final HighwayTag minimumHighwayType;
    // Configurable set of tags: one for highways and other for walkways
    private final HighwayTag[] walkwayTags;

    public UnwalkableWaysCheck(final Configuration configuration)
    {
        super(configuration);

        this.minimumHighwayType = configurationValue(configuration, "minimum.highway.type",
                MINIMUM_HIGHWAY_TYPE_DEFAULT.toString(),
                tag -> HighwayTag.valueOf(tag.toUpperCase()));

        this.walkwayTags = configurationValue(configuration, "walkway.tags", DEFAULT_WALKWAY_TAGS)
                .stream().map(value -> HighwayTag.valueOf(value.toUpperCase()))
                .toArray(HighwayTag[]::new);

        this.includeDualCrossingDualCarriageways = configurationValue(configuration,
                "includeDualCrossingDualCarriageways", false);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        if (!(object instanceof Edge))
        {
            return false;
        }
        final Edge edge = (Edge) object;

        // Check that the Edge is a main edge
        return edge.isMainEdge()
                // For all connected nodes to the Edge, check that none of them have
                // Highway=crossing
                && edge.connectedNodes().stream().noneMatch(
                        node -> Validators.isOfType(node, HighwayTag.class, HighwayTag.CROSSING))
                // Check that the Edge does not have highway=motorway or highway=motorway_link
                && !Validators.isOfType(edge, HighwayTag.class, HighwayTag.MOTORWAY,
                        HighwayTag.MOTORWAY_LINK)
                // Check that the Edge does not have a foot tag
                && !Validators.isOfType(edge, FootTag.class, FootTag.YES, FootTag.NO)
                // Check that the Edge does not have a highway tag equal to one of the walkway
                // Values
                && !(Validators.isOfType(edge, HighwayTag.class, this.walkwayTags)
                        // Or that the Edge does not have a sidewalk tag
                        || edge.getTag(SidewalkTag.KEY).isPresent());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge crossingEdge = (Edge) object;

        // Filter the connected edges on either end of this edge to narrow down to potential
        // dual carriageways.
        final Set<Edge> startEdges = filterConnectedEdgesToCandidates(
                crossingEdge.start().connectedEdges(), crossingEdge);
        final Set<Edge> endEdges = filterConnectedEdgesToCandidates(
                crossingEdge.end().connectedEdges(), crossingEdge);

        // used for comparing directions of candidate edges to find ones in opposite directions
        // for the edges from the "start" edge
        for (final Edge matchingStartEdge : startEdges)
        {
            final Optional<String> matchingStartEdgeName = NameTag.getNameOf(matchingStartEdge);
            final Optional<HighwayTag> matchingStartEdgeHighwayTag = HighwayTag
                    .highwayTag(matchingStartEdge);

            // Find any matching "end" edges with
            // - same name (must exist)
            // - different OSM id
            // - same highway type
            // - opposite direction
            final Optional<Edge> matchingEndEdge = endEdges.stream()
                    .filter(potentialMatchingEndEdge ->
                    {
                        final Boolean tagsAndIdsMatch = matchingStartEdge
                                .getIdentifier() != potentialMatchingEndEdge.getIdentifier()
                                && matchingStartEdgeName.isPresent()
                                && matchingStartEdgeName
                                        .equals(NameTag.getNameOf(potentialMatchingEndEdge))
                                && matchingStartEdgeHighwayTag
                                        .equals(HighwayTag.highwayTag(potentialMatchingEndEdge));
                        final Boolean oppositeDirection = EDGE_DIRECTION_COMPARATOR
                                .isOppositeDirection(matchingStartEdge, potentialMatchingEndEdge,
                                        false);
                        return tagsAndIdsMatch && oppositeDirection;
                    }

                    ).findFirst();

            if (matchingEndEdge.isPresent())
            {

                // Now we think our original edge is connected to both sides of a dual carriageway.
                // Based on that, we can then do a basic check if the original edge is also a dual
                // carriageway.
                if (!this.includeDualCrossingDualCarriageways
                        && hasReverseCarriageway(crossingEdge))
                {
                    logger.trace("Skipping {} as possible dual carriageway.",
                            matchingEndEdge.get().getOsmIdentifier());
                    return Optional.empty();
                }

                // create the flag for the original edge.
                logger.info("Flagging {}", crossingEdge.getOsmIdentifier());
                final CheckFlag flag = createFlag(crossingEdge,
                        this.getLocalizedInstruction(0, crossingEdge.getOsmIdentifier()));
                return Optional.of(flag);
            }
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Filter a set of candidate edges down edges that are potential dual carriageways and are not
     * the same name as the original edge.
     *
     * @param edges
     *            the set of candidate edges (from one end of original edge)
     * @param originalEdge
     *            the original edge
     * @return a set of filtered potential dual carriageway edges
     */
    private Set<Edge> filterConnectedEdgesToCandidates(final Set<Edge> edges,
            final Edge originalEdge)
    {
        final Optional<String> originalEdgeName = NameTag.getNameOf(originalEdge);

        return edges.stream().filter(candidate ->
        {
            final Optional<String> candidateEdgeName = NameTag.getNameOf(candidate);

            return !originalEdgeName.equals(candidateEdgeName) && !OneWayTag.isTwoWay(candidate)
                    && candidate.highwayTag().isMoreImportantThanOrEqualTo(this.minimumHighwayType);

        }).collect(Collectors.toSet());
    }

    /**
     * Attempt to check if the given edge is a dual carriageway once we think that edge crosses a
     * dual carriageway. This method takes the edge, traverses to 2 levels deep on connected edges,
     * then checks for one that could be the reverse edge (different id, same name, opposite
     * heading). The assumption is this works for a typical crossing dual-carriageway, where roadA
     * (possible dual carriageway currently being checked) crosses roadB (known dual carriageway)
     *
     * @param originalEdge
     *            the possible dual carriageway.
     * @return true if we think there is a reverse carriage way
     */
    private boolean hasReverseCarriageway(final Edge originalEdge)
    {
        // first level deep edges
        final Set<Edge> firstLevel = originalEdge.connectedEdges();

        // second level deep edges
        final Set<Edge> secondLevel = new HashSet<>();
        firstLevel.forEach(firstLevelEdge -> secondLevel.addAll(firstLevelEdge.connectedEdges()));

        // collect visited edges
        final Set<Long> visitedEdgeIds = firstLevel.stream().map(Edge::getIdentifier)
                .collect(Collectors.toSet());
        visitedEdgeIds.add(originalEdge.getIdentifier());

        // filter visited edges
        final Set<Edge> filteredSecondLevel = secondLevel.stream().filter(
                secondLevelEdge -> !visitedEdgeIds.contains(secondLevelEdge.getIdentifier()))
                .collect(Collectors.toSet());

        // check for any edges with same name and road classification but different osmId as edge
        // and opposite direction
        return filteredSecondLevel.stream()
                .anyMatch(filteredSecondLevelEdge -> NameTag.getNameOf(filteredSecondLevelEdge)
                        .equals(NameTag.getNameOf(originalEdge))
                        && filteredSecondLevelEdge.getTag(HighwayTag.KEY)
                                .equals(originalEdge.getTag(HighwayTag.KEY))
                        && filteredSecondLevelEdge.getIdentifier() != originalEdge.getIdentifier()
                        && EDGE_DIRECTION_COMPARATOR.isOppositeDirection(filteredSecondLevelEdge,
                                originalEdge, false));
    }
}
