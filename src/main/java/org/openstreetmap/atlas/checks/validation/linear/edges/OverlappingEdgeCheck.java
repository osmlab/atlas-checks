package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LevelTag;
import org.openstreetmap.atlas.tags.ManMadeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Checks that two Edges do not share the same two consecutive geometry points. Each segment of a
 * main Edge is checked for overlapping segments of other main edges. If an Edge has previously been
 * found to overlap some Edge, it will not be flagged unless it is overlapped by an Edge not flagged
 * yet. It ignores edges that are part of pedestrian areas.
 *
 * @author brian_l_davis
 * @author sayana_saithu
 * @author bbreithaupt
 */
public class OverlappingEdgeCheck extends BaseCheck<Long>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Duplicate Highway found for way id {0,number,#}. Overlapping way id(s) {1}.",
            "Way {0,number,#} wraps back on itself.");
    private static final long serialVersionUID = 606963380071388809L;
    // Minimum highway priority for an edge to not be allowed to overlap a pedestrian area
    private static final String HIGHWAY_MINIMUM_PRIORITY_DEFAULT = HighwayTag.RESIDENTIAL
            .toString();
    private static final boolean FILTER_PEDESTRIAN_AREAS_DEFAULT = true;
    private static final String ZERO = "0";
    private static final Predicate<Edge> AREA_YES_TAG = edge -> Validators.isOfType(edge,
            AreaTag.class, AreaTag.YES);

    private final HighwayTag highwayMinimumPriority;
    private final boolean filterPedestrianAreas;

    private static Predicate<Edge> notEqual(final AtlasObject object)
    {
        return edge -> !object.equals(edge);
    }

    public OverlappingEdgeCheck(final Configuration configuration)
    {
        super(configuration);
        this.highwayMinimumPriority = Enum.valueOf(HighwayTag.class,
                this.configurationValue(configuration, "highway.priority.minimum",
                        HIGHWAY_MINIMUM_PRIORITY_DEFAULT).toUpperCase());
        this.filterPedestrianAreas = this.configurationValue(configuration,
                "pedestrian.areas.filter", FILTER_PEDESTRIAN_AREAS_DEFAULT);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && ((Edge) object).isMainEdge()
                && !(this.filterPedestrianAreas && this.edgeIsArea((Edge) object));
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        if (!this.isFlagged(object.getIdentifier()))
        {
            final Atlas atlas = object.getAtlas();
            final Set<AtlasObject> overlappingItems = new HashSet<>();
            Location start = null;
            for (final Location end : ((Edge) object).asPolyLine())
            {
                if (start != null)
                {
                    // we only have to check one end for intersecting edges
                    final Rectangle box = start.boxAround(Distance.meters(0));
                    // add all overlapping edges not yet flagged and not pedestrian areas
                    overlappingItems.addAll(Iterables
                            .stream(atlas.edgesIntersecting(box, Edge::isMainEdge))
                            .filter(notEqual(object).and(notIn(object))
                                    .and(this.overlapsSegment(start, end))
                                    .and(this.filterPedestrianAreas ? edge -> !this.edgeIsArea(edge)
                                            : this.notPedestrianAreas((Edge) object))
                                    .and(this.haveSameLevels(object)))
                            .collectToSet());
                }
                start = end;
            }
            if (!overlappingItems.isEmpty())
            {
                this.markAsFlagged(object.getIdentifier());
                // Mark overlapping objects as flagged
                overlappingItems
                        .forEach(overlapEdge -> this.markAsFlagged(overlapEdge.getIdentifier()));
                final CheckFlag flag = this.createFlag(overlappingItems,
                        this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                                new StringList(osmIdentifiers(overlappingItems)).join(", ")));
                // If the edges are part of the same way, give special instructions
                if (overlappingItems.stream().anyMatch(
                        overlapEdge -> overlapEdge.getOsmIdentifier() == object.getOsmIdentifier()))
                {
                    flag.addInstruction(this.getLocalizedInstruction(1, object.getOsmIdentifier()));
                }
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
     * Checks if an {@link Edge} is a pedestrian area or service highway with area=yes tag. A
     * pedestrian area is defined here by a closed way or area=yes tag combined with
     * highway=pedestrian or manmade=pier.
     *
     * @param edge
     *            {@link Edge} to check
     * @return true if the edges is a closed way or has area=yes tag combined with
     *         highway=pedestrian or manmade=pier or if edge has area=yes tag combined with
     *         highway=service.
     */
    private boolean edgeIsArea(final Edge edge)
    {
        return (Validators.isOfType(edge, HighwayTag.class, HighwayTag.PEDESTRIAN)
                || Validators.isOfType(edge, ManMadeTag.class, ManMadeTag.PIER))
                && (AREA_YES_TAG.test(edge) || isPartOfClosedWay(edge))
                || (Validators.isOfType(edge, HighwayTag.class, HighwayTag.SERVICE)
                        && AREA_YES_TAG.test(edge));
    }

    /**
     * Evaluates if the {@link AtlasObject} and the overlapping items have same level. If they have
     * the same level, then it needs to be flagged.
     *
     * @param object
     * @return {@code true} if the {@link AtlasObject} and overlapping items have same level.
     */
    private Predicate<Edge> haveSameLevels(final AtlasObject object)
    {
        // If an object has no level tag, then it is interpreted to be level = 0
        final String objectLevel = LevelTag.getTaggedOrImpliedValue(object, ZERO);
        return edge ->
        {
            // If an edge has a no level tag then it is interpreted as level = 0
            final String edgeLevel = LevelTag.getTaggedOrImpliedValue(edge, ZERO);
            // Return true if both edge level and object level are equal
            return objectLevel.equals(edgeLevel);
        };
    }

    /**
     * Checks if an edge is part of a closed OSM way.
     *
     * @param object
     *            {@link Edge} to check
     * @return true if it is part of a closed OSM way
     */
    private boolean isPartOfClosedWay(final Edge object)
    {
        final HashSet<Long> wayIds = new HashSet<>();
        Edge nextEdge = object;
        // Loop through out going edges with the same OSM id
        while (nextEdge != null)
        {
            wayIds.add(nextEdge.getIdentifier());
            final List<Edge> nextEdgeList = Iterables.stream(nextEdge.outEdges())
                    .filter(Edge::isMainEdge)
                    .filter(outEdge -> outEdge.getOsmIdentifier() == object.getOsmIdentifier())
                    .collectToList();
            nextEdge = nextEdgeList.isEmpty() ? null : nextEdgeList.get(0);
            // If original edge is found, the way is closed
            if (nextEdge != null && wayIds.contains(nextEdge.getIdentifier()))
            {
                return true;
            }
        }
        return false;
    }

    private Predicate<Edge> notIn(final AtlasObject object)
    {
        return edge -> !this.isFlagged(object.getIdentifier());
    }

    /**
     * Checks that if {@code object} or {@code edge} is a pedestrian area, the other is not also a
     * pedestrian area or tagged as a highway with priority under {@code highwayMinimumPriority}.
     *
     * @param object
     *            {@link Edge} that edge overlaps
     * @return true if {@code edge} or {@code object} are not pedestrian areas, or one is a
     *         pedestrian area but the other has a highway priority greater than
     *         {@code highwayMinimumPriority}
     */
    private Predicate<Edge> notPedestrianAreas(final Edge object)
    {
        return edge ->
        {
            // Check if the edge is a pedestrian area
            final boolean edgeIsPedArea = edgeIsArea(edge);
            // Check if the object is a pedestrian area
            final boolean objectIsPedArea = edgeIsArea(object);
            // If both are pedestrian areas, or one is a pedestrian area and the other is a lower
            // priority highway than the configurable return false
            return !((edgeIsPedArea && objectIsPedArea)
                    || (edgeIsPedArea
                            && object.highwayTag().isLessImportantThan(this.highwayMinimumPriority))
                    || (objectIsPedArea
                            && edge.highwayTag().isLessImportantThan(this.highwayMinimumPriority)));
        };
    }

    private Iterable<String> osmIdentifiers(final Iterable<AtlasObject> objects)
    {
        return Iterables.stream(objects).map(AtlasObject::getOsmIdentifier).map(String::valueOf)
                .collectToList();
    }

    private Predicate<Edge> overlapsSegment(final Location start, final Location end)
    {
        return edge ->
        {
            final PolyLine polyLine = edge.asPolyLine();
            return polyLine.contains(new Segment(start, end))
                    || polyLine.contains(new Segment(end, start));
        };
    }
}
