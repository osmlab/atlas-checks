package org.openstreetmap.atlas.checks.validation.linear.lines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.ElevationUtilities;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Sets;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check flags waterways that do not have a sink (i.e., are not connected to another waterway),
 * are circular (so first node and last node are the same), or cross another waterway with the same
 * layer. It also looks for ways that may be going uphill (requires elevation data, see
 * {@link ElevationUtilities}
 *
 * @author Taylor Smock
 */
public class WaterWayCheck extends BaseCheck<Long>
{
    /**
     * This comparator takes a polyline and segments, and compares the segments based off of the
     * location of the segment in the line.
     *
     * @author Taylor Smock
     */
    private static class SegmentIndexComparator implements Comparator<Segment>
    {
        private final List<Segment> lineSegments;

        /**
         * Initialize with a specific polyline to check
         *
         * @param line
         *            The line to check the index of segments against
         */
        SegmentIndexComparator(final PolyLine line)
        {
            this.lineSegments = line.segments();
        }

        @Override
        public int compare(final Segment segment1, final Segment segment2)
        {
            final Segment segment1real = this.lineSegments.stream().filter(segment1::equals)
                    .findFirst().orElse(null);
            final Segment segment2real = this.lineSegments.stream().filter(segment2::equals)
                    .findFirst().orElse(null);
            final int segment1index = this.lineSegments.indexOf(segment1real);
            final int segment2index = this.lineSegments.indexOf(segment2real);
            return segment1index - segment2index;
        }
    }

    private static final long serialVersionUID = 2877101774578564205L;
    private static final String WATERWAY_SINK_TAG_FILTER_DEFAULT = "natural->sinkhole|waterway->tidal_channel,drain|manhole->drain";
    private static final String WATERWAY_TAG_FILTER_DEFAULT = "waterway->river,stream,tidal_channel,canal,drain,ditch,pressurised";

    private static final String DEFAULT_VALID_OCEAN_TAGS = "natural->strait,channel,fjord,sound,bay|"
            + "harbour->*&harbour->!no|estuary->*&estuary->!no|bay->*&bay->!no|place->sea|seamark:type->harbour,harbour_basin,sea_area|water->bay,cove,harbour|waterway->artificial,dock";

    private static final String DEFAULT_OCEAN_BOUNDARY_TAGS = "natural->coastline";
    private static final String DOES_NOT_END_IN_SINK = "The waterway {0} does not end in a sink (ocean/sinkhole/waterway/drain)";
    private static final String CIRCULAR_WATERWAY = "The waterway {0} loops back on itself. This is typically impossible.";
    private static final String CROSSES_WATERWAY = "The waterway {0} crosses the waterway {1}.";
    private static final String GOES_UPHILL = "The waterway {0} probably does not go up hill.\nPlease check (source elevation data resolution was about {1} meters).";
    private static final Distance MIN_RESOLUTION_DISTANCE = Distance.ONE_METER;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(DOES_NOT_END_IN_SINK,
            CIRCULAR_WATERWAY, CROSSES_WATERWAY, GOES_UPHILL);

    private final TaggableFilter waterwaySinkTagFilter;
    private final TaggableFilter waterwayTagFilter;

    private final TaggableFilter validOceanTags;

    private final TaggableFilter oceanBoundaryTags;
    private final ElevationUtilities elevationUtils;
    private final Distance minResolutionDistance;
    private final Distance minDistanceStartEndElevationUphill;

    /**
     * Get intersecting segments of two polylines
     *
     * @param left
     *            The polyline whose segments will go in the key/left side of the pair
     * @param right
     *            The polyline whose segments will go in the value/right side of the pair
     * @return A collection of segments intersecting other segments. A segment *may* appear more
     *         than once.
     */
    public static Collection<Pair<Segment, Segment>> getIntersectingSegments(final PolyLine left,
            final PolyLine right)
    {
        final Collection<Pair<Segment, Segment>> intersectingSegments = new HashSet<>();
        for (final Segment leftSegment : left.segments())
        {
            for (final Segment rightSegment : right.segments())
            {
                if (leftSegment.intersects(rightSegment))
                {
                    intersectingSegments.add(Pair.of(leftSegment, rightSegment));
                }
            }
        }
        return intersectingSegments;
    }

    /**
     * Check the atlas object for boundary nodes at its end
     *
     * @param object
     *            The atlas object to check
     * @return {@code true} if the object ends on a boundary
     */
    private static boolean endsWithBoundaryNode(final AtlasObject object)
    {
        if (!(object instanceof LineItem))
        {
            return false;
        }
        final LineItem lineItem = (LineItem) object;
        final Atlas atlas = object.getAtlas();
        final Location last = lineItem.asPolyLine().last();
        final Stream<Point> points = Iterables.asList(atlas.pointsAt(last)).stream();
        final Stream<Node> nodes = Iterables.asList(atlas.nodesAt(last)).stream();
        return Stream.concat(points, nodes).anyMatch(SyntheticBoundaryNodeTag::isBoundaryNode);
    }

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public WaterWayCheck(final Configuration configuration)
    {
        super(configuration);
        this.elevationUtils = new ElevationUtilities(configuration);
        this.waterwaySinkTagFilter = this.configurationValue(configuration,
                "waterway.sink.tags.filters", WATERWAY_SINK_TAG_FILTER_DEFAULT,
                TaggableFilter::forDefinition);
        this.waterwayTagFilter = this.configurationValue(configuration, "waterway.tags.filters",
                WATERWAY_TAG_FILTER_DEFAULT, TaggableFilter::forDefinition);

        /* Ocean data */
        this.validOceanTags = TaggableFilter.forDefinition(
                this.configurationValue(configuration, "ocean.valid", DEFAULT_VALID_OCEAN_TAGS));
        this.oceanBoundaryTags = TaggableFilter.forDefinition(this.configurationValue(configuration,
                "ocean.boundary", DEFAULT_OCEAN_BOUNDARY_TAGS));

        /* End ocean data */
        /* Elevation settings */
        this.minResolutionDistance = this.configurationValue(configuration,
                "waterway.elevation.resolution.min.uphill", MIN_RESOLUTION_DISTANCE.asMeters(),
                Distance::meters);
        this.minDistanceStartEndElevationUphill = this.configurationValue(configuration,
                "waterway.elevation.distance.min.start.end",
                Distance.FIFTEEN_HUNDRED_FEET.asMeters(), Distance::meters);
        /* End elevation settings */

    }

    /**
     * Check if a line ends in an ocean
     *
     * @param line
     *            The line to check
     * @return {@code true} if the line ends in an ocean
     */
    public boolean doesLineEndInOcean(final LineItem line)
    {
        final Atlas atlas = line.getAtlas();
        final PolyLine linePolyline = line.asPolyLine();
        final Location last = linePolyline.last();
        if (atlas.areasCovering(last, this.validOceanTags::test).iterator().hasNext()
                || atlas.areasCovering(last, this.oceanBoundaryTags::test).iterator().hasNext())
        {
            return true;
        }
        final List<LineItem> lines = new ArrayList<>();
        atlas.lineItemsIntersecting(line.asPolyLine().bounds(), this.oceanBoundaryTags::test)
                .forEach(lines::add);
        final LineItem[] intersecting = lines.stream()
                .filter(l -> l.asPolyLine().intersects(linePolyline)).toArray(LineItem[]::new);
        final SegmentIndexComparator segmentComparator = new SegmentIndexComparator(linePolyline);
        for (final LineItem lineItem : intersecting)
        {
            final Collection<Pair<Segment, Segment>> segs = getIntersectingSegments(linePolyline,
                    lineItem.asPolyLine());
            final Segment max = segs.stream().map(Pair::getKey).distinct().max(segmentComparator)
                    .orElse(null);
            final Collection<Segment> crosses = segs.stream().filter(p -> p.getLeft().equals(max))
                    .map(Pair::getValue).collect(Collectors.toSet());
            final PolyLine coast = new PolyLine(crosses.stream()
                    .flatMap(c -> Stream.of(c.first(), c.last())).toArray(Location[]::new));
            if (isRightOf(coast, linePolyline.last())
                    || lineItem.asPolyLine().contains(linePolyline.last()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a line ends in a waterway sink
     *
     * @param lineItem
     *            The LineItem to check
     * @return {@code true} if the waterway can reasonably be expected to end
     */
    public boolean doesLineEndInSink(final LineItem lineItem)
    {
        // If the way is a sink, it obviously ends in a sink...
        if (this.waterwaySinkTagFilter.test(lineItem))
        {
            return true;
        }
        if (lineItem instanceof Line)
        {
            final Line line = (Line) lineItem;
            final Location last = line.asPolyLine().last();
            final Set<LocationItem> one = Iterables.stream(line.getAtlas().nodesAt(last))
                    .filter(LocationItem.class::isInstance).map(LocationItem.class::cast)
                    .collectToSet();
            final Set<LocationItem> two = Iterables.stream(line.getAtlas().pointsAt(last))
                    .filter(LocationItem.class::isInstance).map(LocationItem.class::cast)
                    .collectToSet();
            if (Stream.concat(one.stream(), two.stream())
                    .anyMatch(this.waterwaySinkTagFilter::test))
            {
                return true;
            }
        }
        else if (lineItem instanceof Edge)
        {
            return this.waterwaySinkTagFilter.test(((Edge) lineItem).end());
        }
        return lineItem.getAtlas()
                .areasCovering(lineItem.asPolyLine().last(), this.waterwaySinkTagFilter::test)
                .iterator().hasNext();
    }

    /**
     * Check if the last location on a line also happens to be connected to a waterway.
     *
     * @param line
     *            The line to check
     * @return {@code true} if the line ends on a waterway (the last node is part of a waterway)
     */
    public boolean doesLineEndOnWaterway(final LineItem line)
    {
        final List<LineItem> waterways = new ArrayList<>();
        line.getAtlas()
                .lineItemsIntersecting(line.asPolyLine().last().boxAround(Distance.ONE_METER),
                        this.waterwayTagFilter::test)
                .forEach(waterways::add);

        waterways.removeIf(line::equals);
        final Location last = line.asPolyLine().last();
        return waterways.stream().anyMatch(
                l -> l.asPolyLine().contains(last) && !last.equals(l.asPolyLine().last()));
    }

    /**
     * Check if a location is to the right of a line (e.g., kerbs, cliffs, and oceans)
     *
     * @param line
     *            The line to compare the location to
     * @param location
     *            The location
     * @return {@code true} if the location is to the right of the line. If the location is on the
     *         line, or to the left of the line, we return {@code false}.
     */
    public boolean isRightOf(final PolyLine line, final Location location)
    {
        final PolyLine tLine = new PolyLine(location);
        final Segment closest = line.segments().stream()
                .min(Comparator.comparingDouble(s -> s.shortestDistanceTo(tLine).asMeters()))
                .orElse(null);
        if (closest != null)
        {
            final PolyLine testLine = new PolyLine(closest.first(), closest.last(), location);
            final Angle difference = testLine.headingDifference().orElse(null);
            if (difference != null && difference.asDegrees() > 0)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a location is inside an atlas bounds
     *
     * @param atlas
     *            The atlas
     * @param location
     *            The location to check
     * @return {@code true} if the atlas contains the location inside its bounds
     */
    public boolean isValidEndToCheck(final Atlas atlas, final Location location)
    {
        return atlas.bounds().fullyGeometricallyEncloses(location);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return !isFlagged(object.getOsmIdentifier()) && object instanceof LineItem
                && this.waterwayTagFilter.test(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final LineItem line = (LineItem) object;
        final Location last = line.asPolyLine().last();
        final Location first = line.asPolyLine().first();
        CheckFlag flag = null;
        final double incline = this.elevationUtils.getIncline(first, last);
        final boolean uphill = !Double.isNaN(incline) && incline > 0
                && last.distanceTo(first).isGreaterThan(this.minDistanceStartEndElevationUphill);
        if (line.isClosed())
        {
            flag = createFlag(object,
                    this.getLocalizedInstruction(FALLBACK_INSTRUCTIONS.indexOf(CIRCULAR_WATERWAY),
                            object.getOsmIdentifier()),
                    Collections.singletonList(line.asPolyLine().first()));
        }
        else if (uphill && this.minResolutionDistance
                .isGreaterThanOrEqualTo(this.elevationUtils.getResolution(first)))
        {
            flag = createUphillFlag(object, first);
        }
        else if (isValidEndToCheck(line.getAtlas(), last) && !doesWaterwayEndInSink(line))
        {
            if (uphill)
            {
                flag = createUphillFlag(object, first);
            }
            else if (!endsWithBoundaryNode(object))
            {
                flag = createFlag(object,
                        this.getLocalizedInstruction(
                                FALLBACK_INSTRUCTIONS.indexOf(DOES_NOT_END_IN_SINK),
                                object.getOsmIdentifier()),
                        Collections.singletonList(last));
            }
        }
        final LineItem crossed = intersectsAnotherWaterWay(line);
        if (flag == null && crossed != null)
        {
            final Iterator<Location> intersections = crossed.asPolyLine()
                    .intersections(line.asPolyLine()).iterator();
            if (intersections.hasNext())
            {
                flag = createFlag(Sets.hashSet(object, crossed),
                        this.getLocalizedInstruction(
                                FALLBACK_INSTRUCTIONS.indexOf(CROSSES_WATERWAY),
                                object.getOsmIdentifier(), crossed.getOsmIdentifier()),
                        Arrays.asList(intersections.next()));
            }
        }
        if (flag == null)
        {
            return Optional.empty();
        }
        super.markAsFlagged(object.getOsmIdentifier());
        return Optional.of(flag);
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private CheckFlag createUphillFlag(final AtlasObject object, final Location first)
    {
        return createFlag(object,
                this.getLocalizedInstruction(FALLBACK_INSTRUCTIONS.indexOf(GOES_UPHILL),
                        object.getOsmIdentifier(),
                        this.elevationUtils.getResolution(first).asMeters()));
    }

    private boolean doesWaterwayEndInSink(final LineItem line)
    {
        return doesLineEndOnWaterway(line) || doesLineEndInSink(line) || doesLineEndInOcean(line);
    }

    private LineItem intersectsAnotherWaterWay(final LineItem line)
    {
        final Atlas atlas = line.getAtlas();
        final Iterable<LineItem> intersectingWaterways = atlas.lineItemsIntersecting(line.bounds(),
                this.waterwayTagFilter::test);
        final Set<LineItem> sameLayerWays = Iterables.stream(intersectingWaterways)
                .filter(potential -> LayerTag.areOnSameLayer(line, potential)
                        && !waterwayConnects(line, potential))
                .collectToSet();
        sameLayerWays.removeIf(line::equals);
        if (sameLayerWays.isEmpty())
        {
            return null;
        }
        return sameLayerWays.iterator().next();
    }

    private boolean waterwayConnects(final LineItem line, final LineItem potential)
    {
        final PolyLine linePoly = line.asPolyLine();
        final PolyLine potentialPoly = potential.asPolyLine();
        final Set<Location> locations = linePoly.intersections(potentialPoly);
        for (final Location location : locations)
        {
            if (!linePoly.contains(location) || !potentialPoly.contains(location))
            {
                return false;
            }
        }
        return !locations.isEmpty();
    }

}
