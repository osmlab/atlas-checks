package org.openstreetmap.atlas.checks.validation.areas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Triple;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
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

/**
 * This check flags all buildings with angles less than some threshold value as part of their
 * geometry. The purpose is to catch buildings that were automatically closed incorrectly, or
 * buildings that are likely to have been poorly digitized. In order to avoid flagging most
 * buildings with curved geometry, this check uses a configurable heuristic to detect curves and
 * does not flag potential spikes at the ends of a curve.
 *
 * @author nachtm
 */
public class SpikyBuildingCheck extends BaseCheck<Long>
{
    private static final long DEFAULT_MIN_HEADING_THRESHOLD = 15;
    private static final long DEFAULT_CIRCULAR_ANGLE_THRESHOLD = 25;
    private static final long DEFAULT_MINIMUM_TOTAL_CIRCULAR_ANGLE_THRESHOLD = 10;
    private static final long DEFAULT_MINIMUM_CIRCULAR_LINE_SEGMENTS = 4;
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "This building has the following angle measurements under the minimum allowed angle of {0}: {1}");
    private Angle headingThreshold;
    private Angle circularAngleThreshold;
    private Angle minimumTotalCircularAngleThreshold;
    private long minimumCircularPointsInCurve;

    /**
     * Default constructor
     *
     * @param configuration
     *            {@link Configuration} required to construct any Check
     */
    public SpikyBuildingCheck(final Configuration configuration)
    {
        super(configuration);
        this.headingThreshold = this.configurationValue(configuration, "spiky.angle.maximum",
                DEFAULT_MIN_HEADING_THRESHOLD, threshold -> Angle.degrees((double) threshold));
        this.circularAngleThreshold = this.configurationValue(configuration, "curve.angle.maximum",
                DEFAULT_CIRCULAR_ANGLE_THRESHOLD, threshold -> Angle.degrees((double) threshold));
        this.minimumTotalCircularAngleThreshold = this.configurationValue(configuration,
                "curve.angle.total.minimum", DEFAULT_MINIMUM_TOTAL_CIRCULAR_ANGLE_THRESHOLD,
                threshold -> Angle.degrees((double) threshold));
        this.minimumCircularPointsInCurve = this.configurationValue(configuration,
                "curve.points.minimum", DEFAULT_MINIMUM_CIRCULAR_LINE_SEGMENTS);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return (object instanceof Area
                || (object instanceof Relation && ((Relation) object).isMultiPolygon()))
                && (this.isBuildingOrPart(object));
    }

    /**
     * Given an object, returns true if that object has a building tag or building part tag
     * indicating that it is either a building or a building part.
     * 
     * @param object
     *            any AtlasObject
     * @return true if object is a building or a building part, false otherwise
     */
    private boolean isBuildingOrPart(final AtlasObject object)
    {
        return BuildingTag.isBuilding(object)
                || Validators.isNotOfType(object, BuildingPartTag.class, BuildingPartTag.NO);
    }

    /**
     * Converts a RelationMember to a polygon if that member is an area.
     * 
     * @param member
     *            any RelationMember object
     * @return an polygon containing the geometry of member if it is an area, otherwise an empty
     *         optional.
     */
    private Optional<Polygon> toPolygon(final RelationMember member)
    {
        if (member.getEntity().getType().equals(ItemType.AREA))
        {
            return Optional.of(((Area) member.getEntity()).asPolygon());
        }
        return Optional.empty();
    }

    /**
     * Gets all of the polygons contained in this object, if this object has any.
     * 
     * @param object
     *            any atlas object
     * @return A singleton stream if object is an Area, a stream if object is a Multipolygon, or an
     *         empty stream if object is neither
     */
    private Stream<Polygon> getPolygons(final AtlasObject object)
    {
        if (object instanceof Area)
        {
            return Stream.of(((Area) object).asPolygon());
        }
        else if (((Relation) object).isMultiPolygon())
        {
            return ((Relation) object).members().stream().map(this::toPolygon)
                    .flatMap(optPoly -> optPoly.map(Stream::of).orElse(Stream.empty()));
        }
        return Stream.empty();
    }

    /**
     * Returns a set of locations which correspond to interior angles of curved portions of a
     * polygons' geometry, using some heuristics to define curved.
     * 
     * @param segments
     *            the cached results of a call to Polyline.segments()
     * @return A set of all locations for which the heuristics hold true.
     */
    private Set<Location> getCurvedLocations(final List<Segment> segments)
    {
        final List<Triple<Integer, Segment, Segment>> curvedSections = this
                .summarizeCurvedSections(this.getPotentiallyCircularPoints(segments)).stream()
                // Has at least minimumCircularPointsInCurve
                .filter(segment -> segment.getLeft() >= minimumCircularPointsInCurve)
                // Changes heading by at least minimumTotalCircularAngleThreshold
                .filter(segment -> this
                        .getDifferenceInHeadings(segment.getMiddle(), segment.getRight(),
                                Angle.MINIMUM)
                        .isGreaterThanOrEqualTo(minimumTotalCircularAngleThreshold))
                .collect(Collectors.toList());
        return this.sectionsToLocations(curvedSections, segments);
    }

    /**
     * Given a polygon, return a list of all points which have a change in heading less than
     * circularAngleThreshold.
     * 
     * @param segments
     *            the cached results of a call to Polyline.segments()
     * @return A List of Tuples containing two consecutive segments. We use this to refer to the
     *         point between them, since other methods further down the pipeline need the
     *         information about the segment.
     */
    private List<Tuple<Segment, Segment>> getPotentiallyCircularPoints(final List<Segment> segments)
    {
        return this.segmentPairsFrom(segments)
                .filter(segmentTuple -> this.getDifferenceInHeadings(segmentTuple.getFirst(),
                        segmentTuple.getSecond(), Angle.MAXIMUM).isLessThan(circularAngleThreshold))
                .collect(Collectors.toList());
    }

    /**
     * Given a list of potentially circular points, summarize each section into a triple containing
     * the segment before the first point, the segment after the last point, and the number of
     * points contained inside.
     * 
     * @param curvedLocations
     *            a list of points defined by the two segments connected to those points, as
     *            generated by getPotentiallyCircularPoints.
     * @return a list of summary stats for each curved segment, containing the number of points, the
     *         segment before the first point, and the segment after the last point, in that order.
     */
    private List<Triple<Integer, Segment, Segment>> summarizeCurvedSections(
            final List<Tuple<Segment, Segment>> curvedLocations)
    {
        if (curvedLocations.isEmpty())
        {
            return Collections.emptyList();
        }
        final List<Triple<Integer, Segment, Segment>> summaryStats = new ArrayList<>();
        Tuple<Segment, Segment> start = curvedLocations.get(0);
        Tuple<Segment, Segment> previous = curvedLocations.get(0);
        int numPoints = 1;
        for (final Tuple<Segment, Segment> location : curvedLocations.subList(1,
                curvedLocations.size()))
        {
            // If this location doesn't share a segment with the previous location, we just finished
            // a segment
            if (!previous.getSecond().equals(location.getFirst()))
            {
                summaryStats.add(Triple.of(numPoints, start.getFirst(), previous.getSecond()));
                numPoints = 1;
                start = location;
            }
            // Otherwise, we're still part of the same curved section, so just increment numPoints
            else
            {
                numPoints++;
            }
            // Always update previous
            previous = location;
        }
        // Add the last triple
        summaryStats.add(Triple.of(numPoints, start.getFirst(), previous.getSecond()));
        // We might need to clean up a circular segment that wraps around 0.
        if (summaryStats.get(0).getMiddle()
                .equals(summaryStats.get(summaryStats.size() - 1).getRight()))
        {
            final Triple<Integer, Segment, Segment> first = summaryStats.get(0);
            final Triple<Integer, Segment, Segment> last = summaryStats.get(0);

            summaryStats.set(0, Triple.of(first.getLeft() + last.getLeft(), last.getMiddle(),
                    first.getRight()));
        }
        return summaryStats;
    }

    /**
     * Given an order list of summary stats for curved sections, and a list of all the segments in a
     * polygon, traverse the polygon and return a set of all curved locations. Effectively a
     * reversal of summarizeCurvedSections. Note that every segment listed in curvedSections should
     * exist somewhere in allSegments!
     * 
     * @param curvedSections
     *            a list of summary stats for a polygon generated by summarizeCurvedSections
     * @param allSegments
     *            the cached results of a call to Polyline.segments()
     * @return a set of all locations represented by the curvedSections data structure
     */
    private Set<Location> sectionsToLocations(
            final List<Triple<Integer, Segment, Segment>> curvedSections,
            final List<Segment> allSegments)
    {
        if (curvedSections.isEmpty())
        {
            return Collections.emptySet();
        }
        final Set<Location> locations = new HashSet<>();
        boolean inMiddleOfSegment = false;
        int curvedSectionIndex = 0;
        Segment curvedSectionStart = curvedSections.get(curvedSectionIndex).getMiddle();
        Segment curvedSectionEnd = curvedSections.get(curvedSectionIndex).getRight();
        for (final Tuple<Segment, Segment> beforeAndAfter : this.segmentPairsFrom(allSegments)
                .collect(Collectors.toList()))
        {
            if (inMiddleOfSegment)
            {
                // Is this the end of the curved segment?
                if (curvedSectionEnd.equals(beforeAndAfter.getSecond()))
                {
                    inMiddleOfSegment = false;
                    locations.add(curvedSectionEnd.start());
                    curvedSectionIndex++;
                    if (curvedSectionIndex >= curvedSections.size())
                    {
                        break;
                    }
                    curvedSectionStart = curvedSections.get(curvedSectionIndex).getMiddle();
                    curvedSectionEnd = curvedSections.get(curvedSectionIndex).getRight();
                }
                else
                {
                    locations.add(beforeAndAfter.getFirst().end());
                }
            }
            else
            {
                // Did we come across a new curved segment?
                if (curvedSectionStart.equals(beforeAndAfter.getFirst()))
                {
                    inMiddleOfSegment = true;
                    locations.add(curvedSectionStart.end());
                }
                // If not, do nothing
            }
        }
        return locations;
    }

    /**
     * Given a polygon, return a stream consisting of all consecutive pairs of segments from this
     * polygon. For example, given a polygon ABCD, returns a stream with: (AB), (BC), (CD), (DA)
     *
     * @param segments
     *            The cached results of a call to Polyline.segments() for a polygon to decompose
     * @return A stream containing all of the segment pairs in this polygon
     */
    private Stream<Tuple<Segment, Segment>> segmentPairsFrom(final List<Segment> segments)
    {
        return Stream.concat(
                // Take the first segments
                IntStream.range(1, segments.size())
                        .mapToObj(secondIndex -> Tuple.createTuple(segments.get(secondIndex - 1),
                                segments.get(secondIndex))),
                // Don't forget about the closing segment!
                Stream.of(Tuple.createTuple(segments.get(segments.size() - 1), segments.get(0))));
    }

    /**
     * Finds curved sections of a polygon, then gets the location of all spiky angles inside of the
     * polygon and composes them into a list.
     * 
     * @param polygon
     *            any Polygon to analyze
     * @return a list of tuples representing spiky angles. The first value is the calculated angle
     *         of a particular point, and the second is its location in the world.
     */
    private List<Tuple<Angle, Location>> getSpikyAngleLocations(final Polygon polygon)
    {
        final List<Segment> segments = polygon.segments();
        final Set<Location> curvedLocations = this.getCurvedLocations(segments);
        return this.segmentPairsFrom(segments)
                .map(segmentPair -> this.getSpikyAngleLocation(segmentPair.getFirst(),
                        segmentPair.getSecond(), curvedLocations))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    /**
     * For an point defined by the two surrounding segments, return the angle and location of that
     * point if that point is not part of a curve, and the angle between the two segments is less
     * than headingThreshold.
     * 
     * @param beforeAngle
     *            the segment directly before the point in question
     * @param afterAngle
     *            the segment directly after the point in question
     * @param curvedLocations
     *            the locations of all curved segments in the polygon
     * @return an empty optional if the point is part of a curve, or if the angle is greater than or
     *         equal to headingThreshold. Otherwise, a tuple containing the location of the point
     *         and the angle between beforeAnge and afterAngle
     */
    private Optional<Tuple<Angle, Location>> getSpikyAngleLocation(final Segment beforeAngle,
            final Segment afterAngle, final Set<Location> curvedLocations)
    {
        if (!curvedLocations.contains(afterAngle.end())
                && !curvedLocations.contains(beforeAngle.start()))
        {
            final Angle difference = this.getDifferenceInHeadings(beforeAngle,
                    afterAngle.reversed(), Angle.MAXIMUM);
            if (difference.isLessThan(headingThreshold))
            {
                return Optional.of(Tuple.createTuple(difference, afterAngle.start()));
            }
        }
        return Optional.empty();
    }

    /**
     * Gets the difference in headings between firstSegment and secondSegment, returning
     * defaultAngle if either segments are a point.
     * 
     * @param firstSegment
     *            the first segment to compare
     * @param secondSegment
     *            the second segment to compare
     * @param defaultAngle
     *            the default value to return
     * @return the difference between firstSegment.heading() and secondSegment.heading() if neither
     *         segment is a single point (same start and end nodes), or defaultAngle if either
     *         segment is a single point
     */
    private Angle getDifferenceInHeadings(final Segment firstSegment, final Segment secondSegment,
            final Angle defaultAngle)
    {
        return firstSegment.heading()
                .flatMap(first -> secondSegment.heading().map(first::difference))
                .orElse(defaultAngle);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final List<Tuple<Angle, Location>> allSpikyAngles = this.getPolygons(object)
                .map(this::getSpikyAngleLocations)
                .filter(angleLocations -> !angleLocations.isEmpty()).flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (!allSpikyAngles.isEmpty())
        {
            final String instruction = this.getLocalizedInstruction(0, headingThreshold.toString(),
                    allSpikyAngles.stream().map(Tuple::getFirst).map(Angle::toString)
                            .collect(Collectors.joining(", ")));
            final List<Location> markers = allSpikyAngles.stream().map(Tuple::getSecond)
                    .collect(Collectors.toList());
            final CheckFlag flag;
            if (object instanceof Area)
            {
                flag = this.createFlag(object, instruction, markers);
            }
            else
            {
                flag = this.createFlag(((Relation) object).flatten(), instruction, markers);
            }
            return Optional.of(flag);
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
