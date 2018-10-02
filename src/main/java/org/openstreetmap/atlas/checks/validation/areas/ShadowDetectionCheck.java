package org.openstreetmap.atlas.checks.validation.areas;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
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
        return !this.isFlagged(object.getIdentifier()) && object instanceof Area
                && this.hasMinKey(object);
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
        if (this.isBuildingPart(area))
        {
            final Set<Area> floatingParts = this.getFloatingParts(area);
            if (!floatingParts.isEmpty())
            {
                final CheckFlag flag = this.createFlag(object,
                        this.getLocalizedInstruction(1, object.getOsmIdentifier()));
                for (final Area part : floatingParts)
                {
                    this.markAsFlagged(part.getIdentifier());
                    if (!part.equals(area))
                    {
                        flag.addObject(part);
                    }
                }
                return Optional.of(flag);
            }
        }
        // Flag buildings (not parts) that are floating.
        else if (this.isOffGround(area))
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Uses a BFS to gather all connected building parts. If a part is found that is on the ground,
     * an empty {@link Set} is returned because the parts are not floating.
     *
     * @param startingPart
     *            {@link Area} to start the walker from
     * @return a {@link Set} of {@link Area}s that are all floating
     */
    private Set<Area> getFloatingParts(final Area startingPart)
    {
        final Set<Area> connectedParts = new HashSet<>();
        final ArrayDeque<Area> toCheck = new ArrayDeque<>();
        connectedParts.add(startingPart);
        toCheck.add(startingPart);

        while (!toCheck.isEmpty())
        {
            final Area checking = toCheck.poll();
            // If a connection to the ground is found the parts are not floating
            if (!isOffGround(checking) && connectedParts.size() > 1)
            {
                return new HashSet<>();
            }
            // Get parts connected in 3D
            final Set<Area> neighboringParts = Iterables.asSet(checking.getAtlas()
                    .areasIntersecting(checking.bounds().expand(Distance.ONE_METER),
                            neighboringPart(checking, connectedParts)));
            connectedParts.addAll(neighboringParts);
            toCheck.addAll(neighboringParts);
        }
        return connectedParts;
    }

    /**
     * Checks if two areas are building parts and overlap or touch each other.
     *
     * @param part
     *            a known building part to check against
     * @return true if the predicate {@link Area} is a building part and intersects or touches
     *         {@code part}
     */
    private Predicate<Area> neighboringPart(final Area part, final Set<Area> checked)
    {
        return area ->
        {
            final Polygon partPolygon = part.asPolygon();
            final Polygon areaPolygon = area.asPolygon();
            // Check if it is a building part, and either intersects or touches.
            return !checked.contains(area) && this.isBuildingPart(area)
                    && (partPolygon.intersects(areaPolygon)
                            || partPolygon.stream().anyMatch(areaPolygon::contains)
                            || partPolygon.fullyGeometricallyEncloses(areaPolygon)
                            || areaPolygon.fullyGeometricallyEncloses(partPolygon))
                    && neighborsHeightContains(part, area);
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

    /**
     * Checks if an {@link Area} has tags indicating it is off the ground.
     *
     * @param area
     *            {@link Area} to check
     * @return true if the area is off the ground
     */
    private boolean isOffGround(final Area area)
    {
        return !area.getOsmTags().getOrDefault(MinHeightTag.KEY, ZERO_STRING).equals(ZERO_STRING)
                || !area.getOsmTags().getOrDefault(BuildingMinLevelTag.KEY, ZERO_STRING)
                        .equals(ZERO_STRING);
    }
}
