package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.openstreetmap.atlas.tags.FordTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LeisureTag;
import org.openstreetmap.atlas.tags.WaterwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags waterway and power line edge items that are crossed by navigable edges (having way specific
 * highway tag). If the way is a waterway and the crossing way has {@code ford=yes} or
 * {@code leisure=slipway} tags, then the crossing is accepted. {@code dam} and {@code weir}
 * waterways are not checked, those type of ways can cross other highways.
 *
 * @author pako.todea
 */
public class HighwayIntersectionCheck extends BaseCheck<Long>
{

    private static final long serialVersionUID = 1L;
    private static final String INSTRUCTION_FORMAT = "The water/powerline with id {0,number,#} has invalid crossings "
            + "with {1}. A navigable way can not cross a power line or a water.";
    private static final String INVALID_EDGE_FORMAT = "Edge {0,number,#} is crossing invalidly with {1}.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INSTRUCTION_FORMAT,
            INVALID_EDGE_FORMAT);

    /**
     * Checks whether the given {@link Edge}s cross each other.
     *
     * @param edge
     *            {@link Edge} being crossed
     * @param crossingEdge
     *            Crossing {@link Edge}
     * @param intersection
     *            Intersection {@link Location}
     * @return {@code true} if given {@link Edge}s cross each other
     */
    private static boolean isCross(final Edge edge, final Edge crossingEdge,
            final Location intersection)
    {
        final PolyLine edgeAsPolyLine = edge.asPolyLine();
        final PolyLine crossingEdgeAsPolyLine = crossingEdge.asPolyLine();
        return edgeAsPolyLine.contains(intersection)
                && crossingEdgeAsPolyLine.contains(intersection);
    }

    public HighwayIntersectionCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return TypePredicates.IS_EDGE.test(object)
                && (TagPredicates.IS_POWER_LINE.test(object) || this.isWaterwayToCheck(object));
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        final Atlas atlas = edge.getAtlas();
        final Rectangle edgeBounds = edge.bounds();

        final Set<Edge> invalidIntersectingEdges = Iterables
                .asList(atlas.edgesIntersecting(edgeBounds, HighwayTag::isWayOnlyTag)).stream()
                .filter(crossingEdge -> TagPredicates.IS_POWER_LINE.test(edge)
                        || !FordTag.isYes(crossingEdge))
                .filter(crossingEdge -> TagPredicates.IS_POWER_LINE.test(edge)
                        || !Validators.isOfType(crossingEdge, LeisureTag.class, LeisureTag.SLIPWAY))
                .filter(crossingEdge -> crossingEdge.getIdentifier() != edge.getIdentifier())
                .filter(crossingEdge -> !crossingEdge.isReversedEdge(edge))
                .filter(crossingEdge -> this.getIntersection(edge, crossingEdge).stream()
                        .anyMatch(intersection -> isCross(edge, crossingEdge, intersection)))
                .collect(Collectors.toSet());

        if (!invalidIntersectingEdges.isEmpty())
        {
            return this.createHighwayIntersectionCheckFlag(edge, invalidIntersectingEdges);
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Function that creates highway intersection check flag.
     *
     * @param edge
     *            Atlas object.
     * @param crossingEdges
     *            collected edges for a given atlas object.
     * @return newly created highway intersection check flag including crossing edges locations.
     */
    private Optional<CheckFlag> createHighwayIntersectionCheckFlag(final Edge edge,
            final Set<Edge> crossingEdges)
    {
        final CheckFlag newFlag = new CheckFlag(this.getTaskIdentifier(edge));
        this.markAsFlagged(edge.getIdentifier());
        final Set<Location> points = crossingEdges.stream()
                .filter(crossEdge -> crossEdge.getIdentifier() != edge.getIdentifier())
                .flatMap(crossEdge -> this.getIntersection(edge, crossEdge).stream())
                .collect(Collectors.toSet());
        newFlag.addInstruction(
                this.getLocalizedInstruction(0, edge.getOsmIdentifier(), crossingEdges.stream()
                        .map(AtlasObject::getOsmIdentifier).collect(Collectors.toSet())));
        newFlag.addPoints(points);
        newFlag.addObject(edge);
        return Optional.of(newFlag);
    }

    /**
     * This function returns the set of intersection locations for the given edges.
     *
     * @param firstEdge
     *            the first Edge
     * @param secondEdge
     *            the second edge
     * @return set of intersection locations.
     */
    private Set<Location> getIntersection(final Edge firstEdge, final Edge secondEdge)
    {
        final PolyLine firstEdgeAsPolyLine = firstEdge.asPolyLine();
        final PolyLine secondEdgeAsPolyLine = secondEdge.asPolyLine();
        return firstEdgeAsPolyLine.intersections(secondEdgeAsPolyLine);
    }

    /**
     * Checks whether the given {@link AtlasObject} is a waterway to check.
     *
     * @param edge
     *            the {@link AtlasObject} to check
     * @return true if the given {@link AtlasObject} should be ckecked
     */
    private boolean isWaterwayToCheck(final AtlasObject edge)
    {
        boolean validForCheck = false;
        if (Validators.hasValuesFor(edge, WaterwayTag.class))
        {
            final Optional<WaterwayTag> waterwayTagValue = WaterwayTag.get(edge);
            if (waterwayTagValue.isPresent())
            {
                validForCheck = !(HighwayTag.highwayTag(edge).isPresent()
                        && (waterwayTagValue.get().name().equalsIgnoreCase("dam")
                                || waterwayTagValue.get().name().equalsIgnoreCase("weir")));
            }
        }
        return validForCheck;
    }
}
