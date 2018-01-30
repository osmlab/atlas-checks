package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.atlas.predicates.TagPredicates;
import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.constants.CommonConstants;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags edges that are crossing other edges invalidly. If two edges are crossing each other, then
 * they should have an intersection location shared in both edges. Otherwise, their layer tag should
 * tell the difference.
 *
 * @author mkalender, gpogulsky
 */
public class EdgeCrossingEdgeCheck extends BaseCheck<String>
{
    private static final String INSTRUCTION_FORMAT = "The road with id {0,number,#} has invalid crossings."
            + " If two roads are crossing each other, then they should have nodes at intersection"
            + " locations unless they are explicitly marked as crossing. Otherwise, crossing roads"
            + " should have different layer tags.";
    private static final String INVALID_EDGE_FORMAT = "Edge {0,number,#} is crossing invalidly.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INSTRUCTION_FORMAT,
            INVALID_EDGE_FORMAT);
    private static final long serialVersionUID = 2146863485833228593L;

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
                        && !edgeLayer.equals(crossingEdgeLayer);
    }

    private static String generateAtlasObjectPairIdentifier(final AtlasObject thisObject,
            final AtlasObject thatObject)
    {
        if (thisObject.getIdentifier() < thatObject.getIdentifier())
        {
            return thisObject.getIdentifier() + CommonConstants.DASH + thatObject.getIdentifier();
        }

        return thatObject.getIdentifier() + CommonConstants.DASH + thisObject.getIdentifier();
    }

    /**
     * Validates given {@link AtlasObject} (assumed to be an {@link Edge}) whether it is a valid
     * crossing edge or not
     *
     * @param object
     *            {@link AtlasObject} to test
     * @return {@code true} if given {@link AtlasObject} object is a valid crossing edge
     */
    private static boolean isValidCrossingEdge(final AtlasObject object)
    {
        if (Edge.isMasterEdgeIdentifier(object.getIdentifier())
                && !TagPredicates.IS_AREA.test(object))
        {
            final Optional<HighwayTag> highway = HighwayTag.highwayTag(object);
            if (highway.isPresent())
            {
                return HighwayTag.isCarNavigableHighway(highway.get())
                        && !HighwayTag.CROSSING.equals(highway.get());
            }
        }

        return false;
    }

    public EdgeCrossingEdgeCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return TypePredicates.IS_EDGE.test(object) && isValidCrossingEdge(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Prepare the edge being tested for checks
        final Edge edge = (Edge) object;
        final PolyLine edgeAsPolyLine = edge.asPolyLine();
        final Rectangle edgeBounds = edge.bounds();
        final Optional<Long> edgeLayer = LayerTag.getTaggedOrImpliedValue(object, 0L);

        // Retrieve crossing edges
        final Atlas atlas = object.getAtlas();
        final Iterable<Edge> crossingEdges = atlas.edgesIntersecting(edgeBounds,
                // filter out the same edge, non-valid crossing edges and already flagged ones
                crossingEdge -> edge.getIdentifier() != crossingEdge.getIdentifier()
                        && isValidCrossingEdge(crossingEdge)
                        && !this.isFlagged(generateAtlasObjectPairIdentifier(edge, crossingEdge)));

        List<Edge> invalidEdges = null;

        // Go through crossing items and collect invalid crossings
        // NOTE: Due to way sectioning same OSM way could be marked multiple times here. However,
        // MapRoulette will display way-sectioned edges in case there is an invalid crossing.
        // Therefore, if an OSM way crosses another OSM way multiple times in separate edges, then
        // each edge will be marked explicitly.
        for (final Edge crossingEdge : crossingEdges)
        {
            final PolyLine crossingEdgeAsPolyLine = crossingEdge.asPolyLine();
            final Optional<Long> crossingEdgeLayer = LayerTag.getTaggedOrImpliedValue(crossingEdge,
                    0L);
            final Set<Location> intersections = edgeAsPolyLine
                    .intersections(crossingEdgeAsPolyLine);

            // Check whether crossing edge can actually cross
            for (final Location intersection : intersections)
            {
                // Add this set to flagged pairs to skip it next time
                this.markAsFlagged(generateAtlasObjectPairIdentifier(edge, crossingEdge));

                // Check if crossing is valid or not
                if (canCross(edgeAsPolyLine, edgeLayer, crossingEdgeAsPolyLine, crossingEdgeLayer,
                        intersection))
                {
                    continue;
                }

                if (invalidEdges == null)
                {
                    // Normally we expect 1 or 2 edges in the list
                    invalidEdges = new LinkedList<>();
                }

                invalidEdges.add(crossingEdge);
            }
        }

        if (invalidEdges != null)
        {
            final CheckFlag newFlag = new CheckFlag(getTaskIdentifier(object));
            newFlag.addObject(object);
            newFlag.addInstruction(this.getLocalizedInstruction(0, object.getOsmIdentifier()));

            invalidEdges.forEach(invalidEdge ->
            {
                newFlag.addObject(invalidEdge);
                newFlag.addInstruction(
                        this.getLocalizedInstruction(1, invalidEdge.getOsmIdentifier()));
            });

            return Optional.of(newFlag);
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
