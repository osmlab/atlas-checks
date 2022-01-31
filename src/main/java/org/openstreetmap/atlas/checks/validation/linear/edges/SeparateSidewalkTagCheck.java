package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.FootTag;
import org.openstreetmap.atlas.tags.FootwayTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.SidewalkLeftTag;
import org.openstreetmap.atlas.tags.SidewalkRightTag;
import org.openstreetmap.atlas.tags.SidewalkTag;
import org.openstreetmap.atlas.tags.oneway.OneWayTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import avro.shaded.com.google.common.collect.Range;
import avro.shaded.com.google.common.collect.Ranges;

/**
 * This check flags when sidewalk=* tags are used on a highway that any separately mapped
 * sidewalk(s) are consistent with the highway’s sidewalk tags.
 *
 * @author Vladimir Lemberg
 */
public class SeparateSidewalkTagCheck extends BaseCheck<Long>
{
    private static final String INSTRUCTION_FORMAT = "Way {0,number,#} is tagged as sidewalk={1} but separately mapped sidewalks were detected that are not consistent with the way's sidewalk tag. Verify that the sidewalk tag for this way is correct and consistent with separately mapped ways for the entirety of the way.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(INSTRUCTION_FORMAT);
    private static final Double SIDEWALK_SEARCH_DISTANCE_DEFAULT = 15.0;
    private static final Double EDGE_LENGTH_DEFAULT = 20.0;
    private static final Range<Double> HEADING_DEGREE_RANGE = Ranges.closed(-20.0, 20.0);
    private static final String MAXIMUM_HIGHWAY_DEFAULT = HighwayTag.PRIMARY.toString();
    private final Distance searchDistance;
    private final Distance defaultEdgeLength;
    private final HighwayTag maximumHighwayType;

    public SeparateSidewalkTagCheck(final Configuration configuration)
    {
        super(configuration);
        this.searchDistance = configurationValue(configuration, "sidewalk.search.distance",
                SIDEWALK_SEARCH_DISTANCE_DEFAULT, Distance::meters);
        this.defaultEdgeLength = configurationValue(configuration, "edge.length",
                EDGE_LENGTH_DEFAULT, Distance::meters);
        this.maximumHighwayType = this.configurationValue(configuration, "maximum.highway.type",
                MAXIMUM_HIGHWAY_DEFAULT, str -> Enum.valueOf(HighwayTag.class, str.toUpperCase()));
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && !this.isFlagged(object.getOsmIdentifier())
                && ((Edge) object).isMainEdge() && HighwayTag.isCarNavigableHighway(object)
                && this.validSidewalkFilter((Edge) object) && !((Edge) object).isClosed();
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        final Location edgeMidPoint = edge.start().getLocation().midPoint(edge.end().getLocation());
        final String sidewalkTagValue = edge.getTag(SidewalkTag.KEY).orElse(null);
        final String sidewalkTagLeftValue = edge.getTag(SidewalkLeftTag.KEY).orElse(null);
        final String sidewalkTagRightValue = edge.getTag(SidewalkRightTag.KEY).orElse(null);
        final Set<Edge> separatedSidewalks = Iterables.stream(edge.getAtlas()
                .edgesIntersecting(this.closestSegmentToPoint(edge.asPolyLine(), edgeMidPoint)
                        .middle().boxAround(this.searchDistance)))
                .filter(this::validFootwayFilter).collectToSet();

        if (!separatedSidewalks.isEmpty())
        {
            for (final Edge sidewalk : separatedSidewalks)
            {
                final Segment closestSidewalkSegment = this
                        .closestSegmentToPoint(sidewalk.asPolyLine(), edgeMidPoint);

                if (this.isCrossing(edge.asPolyLine(), sidewalk.asPolyLine())
                        || !LayerTag.areOnSameLayer(edge, sidewalk))
                {
                    continue;
                }

                if (("right".equals(sidewalkTagValue) || ("separate".equals(sidewalkTagRightValue)))
                        && !this.isRightOf(edge.asPolyLine(), closestSidewalkSegment.middle()))
                {
                    return this.generateFlag(edge, sidewalkTagValue);
                }

                if (("left".equals(sidewalkTagValue) || ("separate".equals(sidewalkTagLeftValue)))
                        && this.isRightOf(edge.asPolyLine(), closestSidewalkSegment.middle()))
                {
                    return this.generateFlag(edge, sidewalkTagValue);
                }

                if ("both".equals(sidewalkTagValue) && separatedSidewalks.size() < 2)
                {
                    return this.generateFlag(edge, sidewalkTagValue);
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
     * Find the closest point between line (
     *
     * @param line
     *            the ocean feature offending streets/railways offending buildings
     * @return the flag for this ocean feature if flaggable items were found
     */
    private Segment closestSegmentToPoint(final PolyLine line, final Location location)
    {
        final PolyLine tLine = new PolyLine(location);
        return line.segments().stream()
                .min(Comparator.comparingDouble(s -> s.shortestDistanceTo(tLine).asMeters()))
                .orElse(null);
    }

    /**
     * Generate and return flag for separately mapped sidewalk if tags are not consistent with the
     * highway’s sidewalk tags.
     *
     * @param edge
     *            edge to flag
     * @param sidewalkTagValue
     *            separate sidewalk tag value
     * @return an optional {@link CheckFlag} object
     */
    private Optional<CheckFlag> generateFlag(final Edge edge, final String sidewalkTagValue)
    {
        super.markAsFlagged(edge.getOsmIdentifier());
        return Optional.of(this.createFlag(new OsmWayWalker(edge).collectEdges(),
                this.getLocalizedInstruction(0, edge.getOsmIdentifier(), sidewalkTagValue)));
    }

    /**
     * Verifies that separate sidewalk and highway are within acceptable heading degree range
     *
     * @param headingOne
     *            {@link Heading} one to check
     * @param headingTwo
     *            {@link Heading} to check.
     * @return {@code true} if both heading are within acceptable heading degree range
     */
    private boolean isAcceptableHeading(final Heading headingOne, final Heading headingTwo)
    {
        int primeMeridian = 180;
        return HEADING_DEGREE_RANGE.contains(headingOne.asDegrees() - headingTwo.asDegrees())
                || HEADING_DEGREE_RANGE.contains(
                        headingOne.asDegrees() - (primeMeridian - headingTwo.asDegrees()));
    }

    /**
     * Verifies if given {@link PolyLine} and {@link PolyLine} are crossing
     *
     * @param lineCrossed
     *            {@link PolyLine} being crossed
     * @param crossingItem
     *            {@link PolyLine} crossing
     * @return whether given {@link PolyLine} and {@link PolyLine}'s intersections are actual
     *         {@link Location}s for both items
     */
    private boolean isCrossing(final PolyLine lineCrossed, final PolyLine crossingItem)
    {
        return !lineCrossed.intersections(crossingItem).isEmpty()
                || lineCrossed.overlapsShapeOf(crossingItem)
                || !this.isAcceptableHeading(Objects.requireNonNull(lineCrossed.overallHeading().orElse(null)),
                Objects.requireNonNull(crossingItem.overallHeading().orElse(null)));
    }

    /**
     * Check if {@link Edge} is a dual carriageway road.
     *
     * @param edge
     *            {@link Edge} to be checked
     * @return {@code true} if {@link Edge} is dual carriageway.
     */
    private boolean isDualCarriageWay(final Edge edge)
    {
        final Optional<HighwayTag> highwayTag = HighwayTag.highwayTag(edge);
        final Optional<OneWayTag> onewayTag = OneWayTag.tag(edge);
        return highwayTag.isPresent() && onewayTag.isPresent()
                && highwayTag.get().isMoreImportantThanOrEqualTo(this.maximumHighwayType)
                && "YES".equals(onewayTag.get().toString());
    }

    /**
     * Check if a location is to the right of a line.
     *
     * @param line
     *            The line to compare the location to
     * @param location
     *            The location
     * @return {@code true} if the location is to the right of the line.
     */
    private boolean isRightOf(final PolyLine line, final Location location)
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
     * Helper function for filtering {@link Edge}s. This is detecting that {@link Edge} is
     * separately mapped sidewalk https://wiki.openstreetmap.org/wiki/Sidewalks or designated
     * pedestrian road https://wiki.openstreetmap.org/wiki/Key:foot.
     *
     * @param edge
     *            Edge to examine
     * @return true if {@link Edge} is passed validation.
     */
    private boolean validFootwayFilter(final Edge edge)
    {
        return edge.isMainEdge()
                && (edge.getTag(HighwayTag.KEY).isPresent()
                        && "footway".equals(edge.getTag(HighwayTag.KEY).get())
                        && edge.getTag(FootwayTag.KEY).isPresent()
                        && "sidewalk".equals(edge.getTag(FootwayTag.KEY).get()))
                || (edge.getTag(FootTag.KEY).isPresent()
                        && edge.getTag(FootTag.KEY).get().matches("yes|designated"));
    }

    /**
     * Helper function for filtering {@link Edge}s. This is to detect {@link Edge} sidewalk tag
     * existence with other
     *
     * @param edge
     *            Edge to examine
     * @return {@code true} if {@link Edge} sidewalk tag meet the criteria.
     */
    private boolean validSidewalkFilter(final Edge edge)
    {
        return edge.getTag(SidewalkTag.KEY).isPresent() && !this.isDualCarriageWay(edge)
                && edge.asPolyLine().length().asMeters() >= this.defaultEdgeLength.asMeters()
                && !edge.isClosed()
                && (edge.getTag(SidewalkTag.KEY).get().matches("left|right|both")
                        || Objects.equals(edge.getTag(SidewalkLeftTag.KEY).orElse(null), "separate")
                        || Objects.equals(edge.getTag(SidewalkRightTag.KEY).orElse(null), "separate"));
    }
}