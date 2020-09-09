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
import org.openstreetmap.atlas.checks.utility.CommonTagFilters;
import org.openstreetmap.atlas.checks.utility.ElevationUtilities;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
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

    private static final String DOES_NOT_END_IN_SINK = "The waterway {0} does not end in a sink (ocean/sinkhole/waterway/drain).";
    private static final String DOES_NOT_END_IN_SINK_BUT_CROSSING_OCEAN = DOES_NOT_END_IN_SINK
            + "\nThe waterway crosses a coastline, which means it is possible for the coastline to have an incorrect direction.\nLand should be to the LEFT of the coastline and the ocean should be to the RIGHT of the coastline (for more information, see https://wiki.osm.org/Tag:natural=coastline).";
    private static final String CIRCULAR_WATERWAY = "The waterway {0} loops back on itself. This is typically impossible.";
    private static final String CROSSES_WATERWAY = "The waterway {0} crosses the waterway {1}.";
    private static final String GOES_UPHILL = "The waterway {0} probably does not go up hill.\nPlease check (source elevation data resolution was about {1} meters).";
    private static final Distance MIN_RESOLUTION_DISTANCE = Distance.ONE_METER;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(DOES_NOT_END_IN_SINK,
            CIRCULAR_WATERWAY, CROSSES_WATERWAY, GOES_UPHILL,
            DOES_NOT_END_IN_SINK_BUT_CROSSING_OCEAN);

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
     * @param atlas
     *            The atlas of the object
     * @param object
     *            The atlas object to check
     * @return {@code true} if the object ends on a boundary
     */
    private static boolean endsWithBoundaryNode(final Atlas atlas, final AtlasObject object)
    {
        if (!(object instanceof LineItem))
        {
            return false;
        }
        final LineItem lineItem = (LineItem) object;
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
        this.validOceanTags = TaggableFilter.forDefinition(this.configurationValue(configuration,
                "ocean.valid", CommonTagFilters.DEFAULT_VALID_OCEAN_TAGS));
        this.oceanBoundaryTags = TaggableFilter.forDefinition(this.configurationValue(configuration,
                "ocean.boundary", CommonTagFilters.DEFAULT_OCEAN_BOUNDARY_TAGS));

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
     * @param atlas
     *            The atlas of the object
     * @param line
     *            The line to check
     * @return {@code true} if the line ends in an ocean
     */
    public boolean doesLineEndInOcean(final Atlas atlas, final LineItem line)
    {
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
            // Get way segments from the two lines that intersect
            final Collection<Pair<Segment, Segment>> segs = getIntersectingSegments(linePolyline,
                    lineItem.asPolyLine());
            // Get the *latest* crossing segment of the waterway
            // (just in case the waterway crosses a coastline multiple times)
            final Segment max = segs.stream().map(Pair::getKey).distinct().max(segmentComparator)
                    .orElse(null);
            // Get the crossing segments of the coastline (probably just one)
            final Collection<Segment> crosses = segs.stream()
                    .filter(pair -> pair.getLeft().equals(max)).map(Pair::getValue)
                    .collect(Collectors.toSet());
            // Create a shortened coastline to use to check if the waterway ends inside the ocean
            final PolyLine coast = new PolyLine(
                    crosses.stream().flatMap(pCoast -> Stream.of(pCoast.first(), pCoast.last()))
                            .toArray(Location[]::new));
            // If the waterway ends to the right of the coastline, it ended in an ocean.
            // If the waterway ends on the coastline, it ended in an ocean.
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
     * @param atlas
     *            The atlas of the object
     * @param lineItem
     *            The LineItem to check
     * @return {@code true} if the waterway can reasonably be expected to end
     */
    public boolean doesLineEndInSink(final Atlas atlas, final LineItem lineItem)
    {
        // If the way is a sink, it obviously ends in a sink...
        if (this.waterwaySinkTagFilter.test(lineItem))
        {
            return true;
        }
        final Location last = lineItem.asPolyLine().last();
        final Set<LocationItem> nodes = Iterables.stream(atlas.nodesAt(last))
                .filter(LocationItem.class::isInstance).map(LocationItem.class::cast)
                .collectToSet();
        final Set<LocationItem> points = Iterables.stream(atlas.pointsAt(last))
                .filter(LocationItem.class::isInstance).map(LocationItem.class::cast)
                .collectToSet();
        return Stream.concat(nodes.stream(), points.stream())
                .anyMatch(this.waterwaySinkTagFilter::test)
                || atlas.areasCovering(lineItem.asPolyLine().last(),
                        this.waterwaySinkTagFilter::test).iterator().hasNext();
    }

    /**
     * Check if the last location on a line also happens to be connected to a waterway.
     *
     * @param atlas
     *            The atlas of the object
     * @param line
     *            The line to check
     * @return {@code true} if the line ends on a waterway (the last node is part of a waterway)
     */
    public boolean doesLineEndOnWaterway(final Atlas atlas, final LineItem line)
    {
        final List<LineItem> waterways = new ArrayList<>();
        atlas.lineItemsContaining(line.asPolyLine().last(), this.waterwayTagFilter::test)
                .forEach(waterways::add);

        waterways.removeIf(line::equals);
        final Location last = line.asPolyLine().last();
        return waterways.stream().anyMatch(tLine -> tLine.asPolyLine().contains(last)
                && !last.equals(tLine.asPolyLine().last()));
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
            return difference != null && difference.asDegrees() > 0;
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
        final Atlas atlas = line.getAtlas();
        CheckFlag flag = null;
        flag = flagCircularWaterway(flag, line);
        flag = flagIncline(flag, line, first, last);
        flag = flagNoSink(flag, atlas, line, last);
        flag = flagCrossingWays(flag, atlas, line);
        if (flag != null)
        {
            super.markAsFlagged(object.getOsmIdentifier());
        }
        return Optional.ofNullable(flag);
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Check if a line crosses an coastline line
     *
     * @param line
     *            The line to check
     * @return {@code true} if the line crosses a coastline
     */
    private boolean doesLineCrossCoast(final Atlas atlas, final LineItem line)
    {
        final List<LineItem> lines = new ArrayList<>();
        atlas.lineItemsIntersecting(line.asPolyLine().bounds(), this.oceanBoundaryTags::test)
                .forEach(lines::add);
        return !lines.isEmpty();
    }

    /**
     * Check if the waterway ends in a sink (i.e., a location that can reasonably expected to have
     * no outflow).
     *
     * @param atlas
     *            The atlas of the object
     * @param line
     *            The waterway item to check
     * @return {@code true} if the waterway ends in a sink.
     */
    private boolean doesWaterwayEndInSink(final Atlas atlas, final LineItem line)
    {
        return this.doesLineEndOnWaterway(atlas, line) || this.doesLineEndInSink(atlas, line)
                || this.doesLineEndInOcean(atlas, line);
    }

    /**
     * Flag circular waterways (how do they have circular flow?)
     *
     * @param flag
     *            The pre-existing flag (or {@code null})
     * @param line
     *            The line to check
     * @return The pre-existing check flag, or a new check flag, or {@code null}
     */
    private CheckFlag flagCircularWaterway(final CheckFlag flag, final LineItem line)
    {
        if (line.isClosed())
        {
            CheckFlag returnFlag = flag;
            final String instructions = this.getLocalizedInstruction(
                    FALLBACK_INSTRUCTIONS.indexOf(CIRCULAR_WATERWAY), line.getOsmIdentifier());
            if (returnFlag == null)
            {
                returnFlag = createFlag(line, instructions,
                        Collections.singletonList(line.asPolyLine().first()));
            }
            else
            {
                returnFlag.addObject(line, instructions);
            }
            return returnFlag;
        }
        return flag;
    }

    /**
     * Flag crossing ways
     *
     * @param flag
     *            The pre-existing flag (or {@code null})
     * @param atlas
     *            The atlas to check
     * @param line
     *            The line to check
     * @return The pre-existing check flag, or a new check flag, or {@code null}
     */
    private CheckFlag flagCrossingWays(final CheckFlag flag, final Atlas atlas, final LineItem line)
    {
        CheckFlag returnFlag = flag;
        final Collection<LineItem> crossed = getIntersectingWaterways(atlas, line);
        for (final LineItem lineItemCrossed : crossed)
        {
            final Iterator<Location> intersections = lineItemCrossed.asPolyLine()
                    .intersections(line.asPolyLine()).iterator();
            if (intersections.hasNext())
            {
                final String instruction = this.getLocalizedInstruction(
                        FALLBACK_INSTRUCTIONS.indexOf(CROSSES_WATERWAY), line.getOsmIdentifier(),
                        lineItemCrossed.getOsmIdentifier());
                if (returnFlag == null)
                {
                    returnFlag = createFlag(Sets.hashSet(line, lineItemCrossed), instruction,
                            Arrays.asList(intersections.next()));
                }
                else
                {
                    returnFlag.addObject(lineItemCrossed, intersections.next(), instruction);
                }
            }
        }
        return returnFlag;
    }

    /**
     * Flag the waterway if it goes uphill
     *
     * @param flag
     *            The pre-existing flag (or {@code null})
     * @param line
     *            The line to check
     * @param first
     *            The location of the first node of the waterway
     * @param last
     *            The location of the last node of the waterway
     * @return The pre-existing check flag, or a new check flag, or {@code null}
     */
    private CheckFlag flagIncline(final CheckFlag flag, final LineItem line, final Location first,
            final Location last)
    {
        /*
         * While this could be modified to check the incline between each node in the way, it
         * currently isn't realistic (the NASA SRTM mission has a max resolution of ~30m). If better
         * resolution datasets become available, it may be worthwhile to check each node in the way.
         * I would expect the resolution of the dataset to be ~1m, but good results may be had with
         * more than 1m resolution.
         */
        final double incline = this.elevationUtils.getIncline(first, last);
        final boolean uphill = !Double.isNaN(incline) && incline > 0
                && last.distanceTo(first).isGreaterThan(this.minDistanceStartEndElevationUphill);
        if (uphill && this.minResolutionDistance
                .isGreaterThanOrEqualTo(this.elevationUtils.getResolution(first)))
        {
            final CheckFlag returnFlag = flag;
            final String instruction = this.getLocalizedInstruction(
                    FALLBACK_INSTRUCTIONS.indexOf(GOES_UPHILL), line.getOsmIdentifier(),
                    this.elevationUtils.getResolution(first).asMeters());
            if (returnFlag == null)
            {
                return createFlag(line, instruction);
            }
            returnFlag.addInstruction(instruction);
            return returnFlag;
        }
        return flag;
    }

    /**
     * Flag if the way has no sink
     *
     * @param flag
     *            The pre-existing flag (or {@code null})
     * @param atlas
     *            The atlas to check
     * @param line
     *            The line to check
     * @param last
     *            The location of the last node in the line
     * @return The pre-existing check flag, or a new check flag, or {@code null}
     */
    private CheckFlag flagNoSink(final CheckFlag flag, final Atlas atlas, final LineItem line,
            final Location last)
    {
        if (isValidEndToCheck(atlas, last) && !doesWaterwayEndInSink(atlas, line)
                && !endsWithBoundaryNode(atlas, line))
        {
            CheckFlag returnFlag = flag;
            final String instruction = this.getLocalizedInstruction(FALLBACK_INSTRUCTIONS.indexOf(
                    this.doesLineCrossCoast(atlas, line) ? DOES_NOT_END_IN_SINK_BUT_CROSSING_OCEAN
                            : DOES_NOT_END_IN_SINK),
                    line.getOsmIdentifier());
            if (returnFlag == null)
            {
                returnFlag = createFlag(line, instruction, Collections.singletonList(last));
            }
            else
            {
                returnFlag.addObject(line, last, instruction);
            }
            return returnFlag;

        }
        return flag;
    }

    /**
     * Get an intersecting waterway, if one exists.
     *
     * @param line
     *            The waterway to look for intersections for.
     * @return The crossing waterways.
     */
    private Collection<LineItem> getIntersectingWaterways(final Atlas atlas, final LineItem line)
    {
        final PolyLine linePoly = line.asPolyLine();
        final Iterable<LineItem> intersectingWaterways = atlas.lineItemsIntersecting(line.bounds(),
                lineItem -> this.waterwayTagFilter.test(lineItem)
                        && lineItem.asPolyLine().intersects(linePoly));
        final Set<LineItem> sameLayerWays = Iterables.stream(intersectingWaterways)
                .filter(potential -> LayerTag.areOnSameLayer(line, potential)
                        && !waterwayConnects(line, potential))
                .collectToSet();
        sameLayerWays.removeIf(line::equals);
        return sameLayerWays;
    }

    /**
     * Check if two waterways are connected
     *
     * @param line
     *            A waterway
     * @param potential
     *            Another waterway which may connect
     * @return {@code true} if both waterways share a location
     */
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
