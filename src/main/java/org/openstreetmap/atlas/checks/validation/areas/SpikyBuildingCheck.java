package org.openstreetmap.atlas.checks.validation.areas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Triple;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.tags.BuildingPartTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.tuples.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * First pass at a spikybuilding check.
 *
 * @author nachtm
 */
public class SpikyBuildingCheck extends BaseCheck<Long>
{
    private final Logger logger = LoggerFactory.getLogger(SpikyBuildingCheck.class);

    private static final long DEFAULT_MIN_HEADING_THRESHOLD = 15;
    private static final long DEFAULT_MIN_SIDES_NUMBER = 3;
    private static final long DEFAULT_CIRCULAR_ANGLE_THRESHOLD = 25;
    private static final long DEFAULT_MINIMUM_TOTAL_CIRCULAR_ANGLE_THRESHOLD = 10;
    private static final long DEFAULT_MINIMUM_CIRCULAR_LINE_SEGMENTS = 4;

    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "This building has the following angle measurements under the minimum allowed angle of {0}: {1}");
    private Angle headingThreshold;
    private Angle circularAngleThreshold;
    private long minSidesNumber;
    private Angle minimumTotalCircularAngleThreshold;
    private long minimumCircularLineSegments;

    /**
     * Default constructor
     *
     * @param configuration
     *            {@link Configuration} required to construct any Check
     */
    public SpikyBuildingCheck(final Configuration configuration)
    {
        super(configuration);
        this.headingThreshold = this.configurationValue(configuration, "angle.spiky.maximum",
                DEFAULT_MIN_HEADING_THRESHOLD, threshold -> Angle.degrees((double) threshold));
        this.minSidesNumber = this.configurationValue(configuration, "sides.minimum",
                DEFAULT_MIN_SIDES_NUMBER);
        this.circularAngleThreshold = this.configurationValue(configuration,
                "angle.circular.maximum", DEFAULT_CIRCULAR_ANGLE_THRESHOLD,
                threshold -> Angle.degrees((double) threshold));
        this.minimumTotalCircularAngleThreshold = this.configurationValue(configuration,
                "angle.circular.total.minimum", DEFAULT_MINIMUM_TOTAL_CIRCULAR_ANGLE_THRESHOLD,
                threshold -> Angle.degrees((double) threshold));
        this.minimumCircularLineSegments = this.configurationValue(configuration,
                "sides.circular.minimum", DEFAULT_MINIMUM_CIRCULAR_LINE_SEGMENTS);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return ((object instanceof Area && ((Area) object).asPolygon().size() > minSidesNumber)
                || (object instanceof Relation && ((Relation) object).isMultiPolygon()))
                && (this.isBuildingOrPart(object));
    }

    private boolean isBuildingOrPart(final AtlasObject object)
    {
        return BuildingTag.isBuilding(object)
                || Validators.isNotOfType(object, BuildingPartTag.class, BuildingPartTag.NO);
    }

    private Optional<Polygon> toPolygon(final RelationMember member)
    {
        if (member.getEntity().getType().equals(ItemType.AREA))
        {
            return Optional.of(((Area) member.getEntity()).asPolygon());
        }
        return Optional.empty();
    }

    private Stream<Polygon> getPolylines(final AtlasObject object)
    {
        if (object instanceof Area)
        {
            return Stream.of(((Area) object).asPolygon());
        }
        else if (((Relation) object).isMultiPolygon())
        {
            return ((Relation) object).members().stream().map(this::toPolygon).flatMap(
                    optPoly -> optPoly.isPresent() ? Stream.of(optPoly.get()) : Stream.empty());
        }
        logger.warn("Returning empty stream");
        return Stream.empty();
    }

    private int lengthOfSegment(final Tuple<Integer, Integer> startAndEnd, final int size)
    {
        // start and end are (both!) inclusive, so add 1
        final int start = startAndEnd.getFirst();
        final int end = startAndEnd.getSecond();
        if (start <= end)
        {
            return end - start + 1;
        }
        return (size - start) + end + 1;
    }

    private Set<Integer> getIndicesOfCircularSegments(final List<Segment> segments)
    {
        return convertStartsAndEndsToIndices(
                expandStartsAndEnds(
                        filterCircularSegmentsByOverallHeadingChange(segments,
                                convertToStartAndEnds(getCircularSegmentIndices(
                                        getCircularSegments(segments), segments), segments.size())),
                        segments.size()),
                segments.size());
    }

    private List<Tuple<Integer, Integer>> filterCircularSegmentsByOverallHeadingChange(
            final List<Segment> referenceList, final List<Tuple<Integer, Integer>> startsAndEnds)
    {
        final List<Tuple<Integer, Integer>> result = new ArrayList<>();
        for (final Tuple<Integer, Integer> startAndEnd : startsAndEnds)
        {
            final Segment start = referenceList.get(startAndEnd.getFirst());
            final Segment end = referenceList.get(startAndEnd.getSecond());
            final Angle difference = getDifferenceBetween(start, end);
            if (difference.isGreaterThanOrEqualTo(minimumTotalCircularAngleThreshold))
            {
                result.add(startAndEnd);
            }
        }
        return result;
    }

    private List<Integer> getCircularSegmentIndices(final Set<Segment> circularSegments,
            final List<Segment> allSegments)
    {
        final List<Integer> result = new ArrayList<>();
        for (int i = 0; i < allSegments.size(); i++)
        {
            if (circularSegments.contains(allSegments.get(i)))
            {
                result.add(i);
            }
        }
        return result;
    }

    private Set<Segment> getCircularSegments(final List<Segment> segments)
    {
        return getSegmentAndNeighbors(segments).filter(segmentTriple ->
        {
            final Segment before = segmentTriple.getLeft();
            final Segment middle = segmentTriple.getMiddle();
            final Segment after = segmentTriple.getRight();

            return getDifferenceBetween(before, middle).isLessThan(circularAngleThreshold)
                    || getDifferenceBetween(middle, after).isLessThan(circularAngleThreshold);
        }).map(Triple::getMiddle).collect(Collectors.toSet());
    }

    private Stream<Triple<Segment, Segment, Segment>> getSegmentAndNeighbors(
            final List<Segment> segments)
    {
        final int size = segments.size();
        return Stream.concat(
                // Get the middle elements
                IntStream.range(1, size - 1)
                        .mapToObj(index -> Triple.of(segments.get(index - 1), segments.get(index),
                                segments.get(index + 1))),
                // Get the last and first element
                // Note that, since the segments wrap around, this stream is still in order!
                Stream.of(
                        Triple.of(segments.get(size - 2), segments.get(size - 1), segments.get(0)),
                        Triple.of(segments.get(size - 1), segments.get(0), segments.get(1))));
    }

    private List<Tuple<Integer, Integer>> convertToStartAndEnds(final List<Integer> indices,
            final int size)
    {
        if (indices.isEmpty())
        {
            return Collections.emptyList();
        }

        final List<Tuple<Integer, Integer>> startsAndEnds = new ArrayList<>();
        int start = indices.get(0);
        int previous = indices.get(0);
        for (int currentIndex = 1; currentIndex < indices.size(); currentIndex++)
        {
            final int currentValue = indices.get(currentIndex);
            // if not continuous, we're at the end
            if (currentValue - previous > 1)
            {
                startsAndEnds.add(Tuple.createTuple(start, indices.get(currentIndex - 1)));
                start = currentValue;
            }
            // if right after previous, we're in the middle. don't update anything extra

            // update previous before advancing
            previous = currentValue;
        }

        // add the tuple containing the last index
        startsAndEnds.add(Tuple.createTuple(start, previous));

        // we might need to clean up a circular segment that wraps around 0.
        if (startsAndEnds.get(0).getFirst() == 0
                && startsAndEnds.get(startsAndEnds.size() - 1).getSecond() == size - 1)
        {
            startsAndEnds.set(0,
                    Tuple.createTuple(startsAndEnds.get(startsAndEnds.size() - 1).getFirst(),
                            startsAndEnds.get(0).getSecond()));
            startsAndEnds.remove(startsAndEnds.size() - 1);
        }
        return startsAndEnds.stream().filter(
                segment -> this.lengthOfSegment(segment, size) >= minimumCircularLineSegments)
                .collect(Collectors.toList());
    }

    private List<Tuple<Integer, Integer>> expandStartsAndEnds(
            final List<Tuple<Integer, Integer>> startsAndEnds, final int size)
    {
        return startsAndEnds.stream()
                .map(original -> Tuple.createTuple(Math.floorMod(original.getFirst() - 1, size),
                        Math.floorMod(original.getSecond() + 1, size)))
                .collect(Collectors.toList());
    }

    private Set<Integer> convertStartsAndEndsToIndices(
            final List<Tuple<Integer, Integer>> startsAndEnds, final int size)
    {
        return startsAndEnds.stream().flatMap(startEnd ->
        {
            final int start = startEnd.getFirst();
            final int end = startEnd.getSecond();
            final IntStream valStream;
            // normal case
            if (start <= end)
            {
                valStream = IntStream.rangeClosed(start, end);
            }
            // wrap around
            else
            {
                valStream = IntStream.concat(IntStream.range(start, size),
                        IntStream.rangeClosed(0, end));
            }
            return valStream.boxed();
        }).collect(Collectors.toSet());
    }

    /**
     * Given a polygon, return a stream consisting of all consecutive pairs of segments from this
     * polygon. For example, given a polygon ABCD, returns a stream with: (AB), (BC), (CD), (DA)
     *
     * @param polygon
     *            A polygon to decompose
     * @return A stream containing all of the segment pairs in this polygon.
     */
    private Stream<Tuple<Segment, Segment>> segmentPairsFrom(final Polygon polygon)
    {
        final List<Segment> segments = polygon.segments();
        return Stream.concat(
                // take the first segments
                IntStream.range(1, segments.size())
                        .mapToObj(secondIndex -> Tuple.createTuple(segments.get(secondIndex - 1),
                                segments.get(secondIndex))),
                // dont forget about the closing segment!
                Stream.of(Tuple.createTuple(segments.get(segments.size() - 1), segments.get(0))));
    }

    // this is almost exactly the same as polygon.anglesLessThanOrEqualTo, except we also need to
    // compare the angle between the last segment and the first segment.
    private List<Tuple<Angle, Location>> getSkinnyAngleLocations(final Polygon polygon)
    {
        final List<Tuple<Angle, Location>> results = new ArrayList<>();
        final List<Segment> segments = polygon.segments();
        final Set<Integer> circleIndices = getIndicesOfCircularSegments(segments);

        // comparing segment to previous segment
        for (int i = 1; i < segments.size(); i++)
        {
            if (!circleIndices.contains(i))
            {
                final Angle difference = getDifferenceBetween(segments.get(i - 1),
                        segments.get(i).reversed());
                if (difference.isLessThan(headingThreshold))
                {
                    results.add(Tuple.createTuple(difference, segments.get(i).start()));
                }
            }
        }
        // compare last segment to first
        if (!circleIndices.contains(0))
        {
            final Angle difference = getDifferenceBetween(segments.get(segments.size() - 1),
                    segments.get(0).reversed());
            if (difference.isLessThan(headingThreshold))
            {
                results.add(Tuple.createTuple(difference, segments.get(0).start()));
            }
        }
        return results;

    }

    private Angle getDifferenceBetween(final Segment firstSegment, final Segment secondSegment)
    {
        // TODO resolve get() issues
        final Heading first = firstSegment.heading().get();
        final Heading second = secondSegment.heading().get();
        return first.difference(second);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final List<Tuple<Angle, Location>> allSkinnyAngles = getPolylines(object)
                .map(this::getSkinnyAngleLocations)
                .filter(angleLocations -> !angleLocations.isEmpty()).flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (!allSkinnyAngles.isEmpty())
        {
            final Set<AtlasObject> objectsToFlag;
            if (object instanceof Area)
            {
                objectsToFlag = Collections.singleton(object);
            }
            else
            {
                objectsToFlag = ((Relation) object).flatten();
            }
            return Optional.of(this.createFlag(objectsToFlag,
                    this.getLocalizedInstruction(0, headingThreshold.toString(),
                            allSkinnyAngles.stream().map(Tuple::getFirst).map(Angle::toString)
                                    .collect(Collectors.joining(", "))),
                    allSkinnyAngles.stream().map(Tuple::getSecond).collect(Collectors.toList())));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
