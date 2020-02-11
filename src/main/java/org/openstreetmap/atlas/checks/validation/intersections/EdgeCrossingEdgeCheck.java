package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.atlas.predicates.TagPredicates;
import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.EdgeWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import scala.Tuple2;

/**
 * Flags edges that are crossing other edges invalidly. If two edges are crossing each other, then
 * they should have an intersection location shared in both edges. Otherwise, their layer tag should
 * tell the difference.
 *
 * @author mkalender, gpogulsky, bbreithaupt
 */
public class EdgeCrossingEdgeCheck extends BaseCheck<Long>
{
    /**
     * A simple {@link EdgeWalker} that filters using nextCandidates.
     */
    private class EdgeCrossingEdgeWalker extends EdgeWalker
    {
        EdgeCrossingEdgeWalker(final Edge startingEdge,
                final Function<Edge, Stream<Edge>> nextCandidates)
        {
            super(startingEdge, nextCandidates);
        }
    }

    private static final String INSTRUCTION_FORMAT = "The road with id {0} has invalid crossings with {1}."
            + " If two roads are crossing each other, then they should have nodes at intersection"
            + " locations unless they are explicitly marked as crossing. Otherwise, crossing roads"
            + " should have different layer tags.";
    private static final String INVALID_EDGE_FORMAT = "Edge {0} is crossing invalidly with {1}.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INSTRUCTION_FORMAT,
            INVALID_EDGE_FORMAT);
    private static final String MINIMUM_HIGHWAY_DEFAULT = HighwayTag.NO.toString();
    private static final Long OSM_LAYER_DEFAULT = 0L;
    private static final long serialVersionUID = 2146863485833228593L;
    private final HighwayTag minimumHighwayType;

    /**
     * Checks whether given {@link PolyLine}s can cross each other.
     *
     * @param edgeAsPolyLine
     *            {@link PolyLine} being crossed
     * @param edgeLayer
     *            {@link Optional} layer value for edge being crossed
     * @param crossingEdgeAsPolyLine
     *            Crossing {@link PolyLine}
     * @param crossingEdgeLayer
     *            {@link Optional} layer value for crossing edge
     * @param intersection
     *            Intersection {@link Location}
     * @return {@code true} if given {@link PolyLine}s can cross each other
     */
    private static boolean canCross(final PolyLine edgeAsPolyLine, final Optional<Long> edgeLayer,
            final PolyLine crossingEdgeAsPolyLine, final Optional<Long> crossingEdgeLayer,
            final Location intersection)
    {
        // If crossing edges have nodes at intersections points, then crossing is valid
        return edgeAsPolyLine.contains(intersection)
                && crossingEdgeAsPolyLine.contains(intersection)
                // Otherwise, if crossing edges has valid, but different tag values
                // Then that is still a valid crossing
                || edgeLayer.isPresent() && crossingEdgeLayer.isPresent()
                        && !edgeLayer.get().equals(crossingEdgeLayer.get());
    }

    public EdgeCrossingEdgeCheck(final Configuration configuration)
    {
        super(configuration);
        this.minimumHighwayType = configurationValue(configuration, "minimum.highway.type",
                MINIMUM_HIGHWAY_DEFAULT, str -> Enum.valueOf(HighwayTag.class, str.toUpperCase()));
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return TypePredicates.IS_EDGE.test(object) && isValidCrossingEdge(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Use EdgeWalker to gather all connected invalid crossings. The use of the walker prevents
        // edges from being flagged more than once.
        final Set<Edge> collectedEdges = new EdgeCrossingEdgeWalker((Edge) object,
                this.getInvalidCrossingEdges()).collectEdges();
        if (collectedEdges.size() > 1)
        {
            final List<Tuple2<Long, Set<Edge>>> edgeCrossPairs = collectedEdges
                    .stream().filter(
                            e -> e.getOsmIdentifier() != object.getOsmIdentifier())
                    .map(e -> new Tuple2<>(e.getOsmIdentifier(),
                            new EdgeCrossingEdgeWalker(e, this.getInvalidCrossingEdges())
                                    .collectEdges()))
                    .collect(Collectors.toList());
            edgeCrossPairs.add(new Tuple2<>(object.getOsmIdentifier(), collectedEdges));
            final Optional<Tuple2<Long, Set<Edge>>> maxPair = edgeCrossPairs.stream().max(
                    (entry1, entry2) -> Integer.compare(entry1._2().size(), entry2._2().size()));
            if (maxPair.isPresent())
            {
                final int maxSize = (maxPair.get()._2()).size();
                if (maxSize == collectedEdges.size())
                {
                    // when the current edge has max edges, of all connected edges, check if another
                    // edge
                    // also have the same set of connected edges. If that is the case flag only
                    // minimum identifier edge.
                    final Optional<Long> minIdentifier = edgeCrossPairs.stream()
                            .filter(entry -> (entry._2()).size() == maxSize)
                            .filter(entry -> (entry._2()).stream()
                                    .allMatch(collectedEdges::contains))
                            .map(Tuple2::_1).min(Comparator.comparingLong(Long::valueOf));
                    if (minIdentifier.isPresent()
                            && minIdentifier.get() == object.getOsmIdentifier())
                    {
                        final CheckFlag newFlag = new CheckFlag(getTaskIdentifier(object));
                        this.markAsFlagged(object.getIdentifier());
                        newFlag.addObject(object);
                        newFlag.addInstruction(this.getLocalizedInstruction(0,
                                object.getOsmIdentifier(), collectedEdges.stream()
                                        .map(AtlasObject::getIdentifier).collect(Collectors.toList())));
                        return Optional.of(newFlag);
                    }
                }
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
     * A {@link Function} for an {@link EdgeWalker} that collects all connected invalid crossings.
     *
     * @return a {@link Set} of invalid crossing {@link Edge}s
     */
    private Function<Edge, Stream<Edge>> getInvalidCrossingEdges()
    {
        return edge ->
        {
            // Prepare the edge being tested for checks
            final PolyLine edgeAsPolyLine = edge.asPolyLine();
            final Rectangle edgeBounds = edge.bounds();
            // If layer tag is present use its value, else use the OSM default
            final Optional<Long> edgeLayer = Validators.hasValuesFor(edge, LayerTag.class)
                    ? LayerTag.getTaggedValue(edge)
                    : Optional.of(OSM_LAYER_DEFAULT);

            // Retrieve crossing edges
            final Atlas atlas = edge.getAtlas();
            return Iterables.asList(atlas.edgesIntersecting(edgeBounds,
                    // filter out the same edge and non-valid crossing edges
                    crossingEdge -> edge.getIdentifier() != crossingEdge.getIdentifier()
                            && this.isValidCrossingEdge(crossingEdge)))
                    .stream()
                    .filter(crossingEdge -> crossingEdge.getOsmIdentifier() != edge.getOsmIdentifier())
                    // Go through crossing items and collect invalid crossings
                    // NOTE: Due to way sectioning same OSM way could be marked multiple times here.
                    // However,
                    // MapRoulette will display way-sectioned edges in case there is an invalid
                    // crossing.
                    // Therefore, if an OSM way crosses another OSM way multiple times in separate
                    // edges,
                    // then each edge will be marked explicitly.
                    .filter(crossingEdge ->
                    {
                        final PolyLine crossingEdgeAsPolyLine = crossingEdge.asPolyLine();
                        final Optional<Long> crossingEdgeLayer = Validators
                                .hasValuesFor(crossingEdge, LayerTag.class)
                                        ? LayerTag.getTaggedValue(crossingEdge)
                                        : Optional.of(OSM_LAYER_DEFAULT);
                        return edgeAsPolyLine.intersections(crossingEdgeAsPolyLine).stream()
                                .anyMatch(intersection -> !canCross(edgeAsPolyLine, edgeLayer,
                                        crossingEdgeAsPolyLine, crossingEdgeLayer, intersection));
                    });
        };
    }

    /**
     * Validates given {@link AtlasObject} (assumed to be an {@link Edge}) whether it is a valid
     * crossing edge or not
     *
     * @param object
     *            {@link AtlasObject} to test
     * @return {@code true} if given {@link AtlasObject} object is a valid crossing edge
     */
    private boolean isValidCrossingEdge(final AtlasObject object)
    {
        if (Edge.isMasterEdgeIdentifier(object.getIdentifier())
                && !TagPredicates.IS_AREA.test(object))
        {
            final Optional<HighwayTag> highway = HighwayTag.highwayTag(object);
            if (highway.isPresent())
            {
                final HighwayTag highwayTag = highway.get();
                return HighwayTag.isCarNavigableHighway(highwayTag)
                        && !HighwayTag.CROSSING.equals(highwayTag)
                        && highwayTag.isMoreImportantThanOrEqualTo(this.minimumHighwayType);
            }
        }
        return false;
    }
}
