package org.openstreetmap.atlas.checks.validation.areas;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Heading;
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

/**
 * Auto generated Check template
 *
 * @author v-garei
 */
public class ConcerningAngleBuildingCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 8586559001979110697L;
    private static final String CONCERNING_ANGLE_INSTRUCTIONS = "Area {0, number, #} has concerning angles, please make all angles right angles.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(CONCERNING_ANGLE_INSTRUCTIONS);
    private static final double ANGLE_DEFAULT = 90.0;
    private static final double MIN_LOW_ANGLE_DIFF_DEFAULT = 80.0;
    private static final double MAX_LOW_ANGLE_DIFF_DEFAULT = 89.9;
    private static final double MIN_HIGH_ANGLE_DIFF_DEFAULT = 90.1;
    private static final double MAX_HIGH_ANGLE_DIFF_DEFAULT = 100.0;
    private static final double MIN_ANGLE_COUNT_DEFAULT = 4.0;
    private static final double MAX_ANGLE_COUNT_DEFAULT = 16.0;
    private final double minLowAngleDiff;
    private final double maxLowAngleDiff;
    private final double minHighAngleDiff;
    private final double maxHighAngleDiff;
    private final double minAngleCount;
    private final double maxAngleCount;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public ConcerningAngleBuildingCheck(final Configuration configuration)
    {
        super(configuration);
        this.minLowAngleDiff = this.configurationValue(configuration, "angles.minLowAngleDiff",
                MIN_LOW_ANGLE_DIFF_DEFAULT);
        this.maxLowAngleDiff = this.configurationValue(configuration, "angles.maxLowAngleDiff",
                MAX_LOW_ANGLE_DIFF_DEFAULT);
        this.minHighAngleDiff = this.configurationValue(configuration, "angles.minHighAngleDiff",
                MIN_HIGH_ANGLE_DIFF_DEFAULT);
        this.maxHighAngleDiff = this.configurationValue(configuration, "angles.maxHighAngleDiff",
                MAX_HIGH_ANGLE_DIFF_DEFAULT);
        this.minAngleCount = this.configurationValue(configuration, "angleCounts.min",
                MIN_ANGLE_COUNT_DEFAULT);
        this.maxAngleCount = this.configurationValue(configuration, "angleCounts.max",
                MAX_ANGLE_COUNT_DEFAULT);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return !isFlagged(object.getOsmIdentifier()) && this.isBuildingOrPart(object)
                && (object instanceof Area
                        || (object instanceof Relation && ((Relation) object).isMultiPolygon()));
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        markAsFlagged(object.getOsmIdentifier());
        final Set<Polygon> buildingPolygons = this.getPolygons(object).collect(Collectors.toSet());
        if (!buildingPolygons.isEmpty())
        {
            for (final Polygon polygon : buildingPolygons)
            {
                if (this.buildingAngleCountWithinValidRange(polygon)
                        && this.hasConcerningAngles(polygon))
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(0, object.getOsmIdentifier())));
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
     * checks to make sure building node count fits within desired range
     * 
     * @param polygon
     *            building being checked
     * @return boolean ensuring that the node count fits within desired range
     */
    private boolean buildingAngleCountWithinValidRange(final Polygon polygon)
    {
        final List<Segment> polygonSegments = polygon.segments();
        return polygonSegments.size() >= this.minAngleCount
                && polygonSegments.size() <= this.maxAngleCount;
    }

    /**
     * Get angle diff between 2 segments
     * 
     * @param segment1
     *            a segment
     * @param segment2
     *            a connecting segment
     * @return angle difference between headings of segments
     */
    private double getAngleDiff(final Segment segment1, final Segment segment2)
    {
        final Optional<Heading> segmentOneHeading = segment1.heading();
        final Optional<Heading> segmentTwoHeading = segment2.heading();
        if (segmentOneHeading.isPresent() && segmentTwoHeading.isPresent())
        {
            return segmentOneHeading.get().difference(segmentTwoHeading.get()).asDegrees();
        }
        return ANGLE_DEFAULT;
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
                    .flatMap(Optional::stream);
        }
        return Stream.empty();
    }

    /**
     * Checks if angle fits within concerning range set in config.
     * 
     * @param polygon
     *            building
     * @return boolean if the angle fits within concerning ranges
     */
    private boolean hasConcerningAngles(final Polygon polygon)
    {
        final List<Segment> segments = polygon.segments();
        final int segmentSize = segments.size();
        for (final Segment segment : segments)
        {
            if (segments.indexOf(segment) < segmentSize - 2)
            {
                final Segment nextSegment = segments.get(segments.indexOf(segment) + 1);
                return (this.getAngleDiff(segment, nextSegment) < this.maxLowAngleDiff
                        && this.getAngleDiff(segment, nextSegment) > this.minLowAngleDiff)
                        || (this.getAngleDiff(segment, nextSegment) < this.maxHighAngleDiff
                                && this.getAngleDiff(segment, nextSegment) > this.minHighAngleDiff);
            }
        }
        return false;
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
}
