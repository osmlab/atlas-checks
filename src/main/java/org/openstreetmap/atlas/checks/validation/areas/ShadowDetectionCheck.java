package org.openstreetmap.atlas.checks.validation.areas;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.BuildingLevelsTag;
import org.openstreetmap.atlas.tags.BuildingMinLevelTag;
import org.openstreetmap.atlas.tags.BuildingPartTag;
import org.openstreetmap.atlas.tags.HeightTag;
import org.openstreetmap.atlas.tags.MinHeightTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import com.google.common.collect.Range;

/**
 * This flags buildings and building parts that are floating, casting strange shadows when rendered
 * in 3D.
 *
 * @author bbreithaupt
 */
public class ShadowDetectionCheck extends BaseCheck
{

    private static final long serialVersionUID = -6968080042879358551L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Floating building {0,number,#} has a min_level/min_height above ground level. Determine ground truth and edit accordingly.",
            "Building Part {0,number,#} is not connected to any other building parts. Determine ground truth and edit accordingly.");

    private static final double LEVEL_TO_METERS_CONVERSION = 3.5;
    private static final String ZERO_STRING = "0";

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public ShadowDetectionCheck(final Configuration configuration)
    {
        super(configuration);
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
        return object instanceof Area && this.hasMinKey(object);
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
        final Area area = (Area) object;

        // Check building parts for connection to other building parts
        if (this.isBuildingPart(area) && !isConnectedPart(area))
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(1, object.getOsmIdentifier())));
        }
        // Flag buildings (not parts) that are floating.
        else if (!this.isBuildingPart(area) && (!object.getOsmTags()
                .getOrDefault(MinHeightTag.KEY, ZERO_STRING).equals(ZERO_STRING)
                || !object.getOsmTags().getOrDefault(BuildingMinLevelTag.KEY, ZERO_STRING)
                        .equals(ZERO_STRING)))
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    /**
     * Gathers the intersecting or touching building parts for an {@link Area}, and checks that they
     * are vertically connected.
     *
     * @param part
     *            an {@link Area} with tag {@code building:part=yes}, and either
     *            {@code min_height=*} or {@code building:min_level=*}
     * @return true if {@code part} is vertically connected to another building part
     */
    private boolean isConnectedPart(final Area part)
    {
        // Get intersecting or touching building parts
        final Set<Area> neighboringParts = Iterables.asSet(part.getAtlas().areasIntersecting(
                part.bounds().expand(Distance.ONE_METER), neighboringPart(part)));
        // Look for any vertical connection
        return neighboringParts.stream()
                .anyMatch(neighbor -> neighborsHeightContains(part, neighbor));
    }

    /**
     * Checks if two areas are building parts and overlap or touch each other.
     *
     * @param part
     *            a known building part to check against
     * @return true if the predicate {@link Area} is a building part and intersects or touches
     *         {@code part}
     */
    private Predicate<Area> neighboringPart(final Area part)
    {
        return area ->
        {
            final Polygon partPolygon = part.asPolygon();
            final Polygon areaPolygon = area.asPolygon();
            // Check if it is a building part, and either intersects or touches.
            return this.isBuildingPart(area) && (partPolygon.fullyGeometricallyEncloses(areaPolygon)
                    || partPolygon.stream().anyMatch(areaPolygon::contains));
        };
    }

    /**
     * Given two {@link Area}s, checks that they have any intersecting or touching height values.
     * The range of height values for the {@link Area}s are calculated using height and layer tags.
     * A {@code min_height} or {@code building:min_layer} tag must exist for {@code part}. All other
     * tags will use defaults if not found.
     *
     * @param part
     *            {@link Area} being checked
     * @param neighbor
     *            {@lnik Area} being checked against
     * @return true if {@code part} intersects or touches {@code neighbor}, by default neighbor is
     *         flat on the ground.
     */
    private boolean neighborsHeightContains(final Area part, final Area neighbor)
    {
        final Map<String, String> neighborTags = neighbor.getOsmTags();
        final Map<String, String> partTags = part.getOsmTags();
        final double partMinHeight;
        final double partMaxHeight;
        double neighborMinHeight = 0;
        double neighborMaxHeight = 0;

        // Set partMinHeight
        partMinHeight = partTags.containsKey(MinHeightTag.KEY)
                ? Integer.parseInt(partTags.get(MinHeightTag.KEY))
                : Integer.parseInt(partTags.get(BuildingMinLevelTag.KEY))
                        * LEVEL_TO_METERS_CONVERSION;
        // Set partMaxHeight
        if (partTags.containsKey(HeightTag.KEY))
        {
            partMaxHeight = Integer.parseInt(partTags.get(HeightTag.KEY));
        }
        else if (partTags.containsKey(BuildingLevelsTag.KEY))
        {
            partMaxHeight = Integer.parseInt(partTags.get(BuildingLevelsTag.KEY))
                    * LEVEL_TO_METERS_CONVERSION;
        }
        else
        {
            partMaxHeight = partMinHeight;
        }
        // Set neighborMinHeight
        if (neighborTags.containsKey(MinHeightTag.KEY))
        {
            neighborMinHeight = Integer.parseInt(neighborTags.get(MinHeightTag.KEY));
        }
        else if (neighborTags.containsKey(BuildingMinLevelTag.KEY))
        {
            neighborMinHeight = Integer.parseInt(neighborTags.get(BuildingMinLevelTag.KEY))
                    * LEVEL_TO_METERS_CONVERSION;
        }
        // Set neighborMaxHeight
        if (neighborTags.containsKey(HeightTag.KEY))
        {
            neighborMaxHeight = Integer.parseInt(neighborTags.get(HeightTag.KEY));
        }
        else if (neighborTags.containsKey(BuildingLevelsTag.KEY))
        {
            neighborMaxHeight = Integer.parseInt(neighborTags.get(BuildingLevelsTag.KEY))
                    * LEVEL_TO_METERS_CONVERSION;
        }

        // Check the range of heights for overlap.
        return Range.closed(partMinHeight, partMaxHeight)
                .isConnected(Range.closed(neighborMinHeight, neighborMaxHeight));
    }

    /**
     * Checks if an {@link AtlasObject} is a building part.
     *
     * @param object
     *            {@link AtlasObject} to check
     * @return true if {@code object} has a {@code building:part=yes} tag
     */
    private boolean isBuildingPart(final AtlasObject object)
    {
        return Validators.isOfType(object, BuildingPartTag.class, BuildingPartTag.YES);
    }

    /**
     * Checks if an {@link AtlasObject} has a tag defining the minimum height of a building.
     *
     * @param object
     *            {@link AtlasObject} to check
     * @return true if {@code object} has a tag defining the minimum height of a building
     */
    private boolean hasMinKey(final AtlasObject object)
    {
        return Validators.hasValuesFor(object, BuildingMinLevelTag.class)
                || Validators.hasValuesFor(object, MinHeightTag.class);
    }
}
