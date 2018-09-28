package org.openstreetmap.atlas.checks.validation.areas;

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
 * Auto generated Check template
 *
 * @author bbreithaupt
 */
public class ShadowDetectionCheck extends BaseCheck
{

    private static final long serialVersionUID = -6968080042879358551L;

    private static final String BUILDING_MIN_HEIGHT_KEY = "building:min_height";
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
        return object instanceof Area && (this.isBuildingPart(object) || this.hasMinKey(object));
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

        if (this.isBuildingPart(area))
        {
            if (!isConnectedPart(area))
            {
                this.markAsFlagged(object.getIdentifier());
            }
        }
        return Optional.empty();
    }

    private boolean isConnectedPart(final Area part)
    {
        if (this.hasMinKey(part))
        {
            final Set<Area> neighboringParts = Iterables.asSet(part.getAtlas().areasIntersecting(
                    part.bounds().expand(Distance.ONE_METER), neighboringPart(part)));
            if (Validators.hasValuesFor(part, MinHeightTag.class))
            {
                return neighboringParts.stream().noneMatch(neighbor -> neighborsHeightContains(part,
                        neighbor, MinHeightTag.KEY, HeightTag.KEY));
            }
            if (Validators.hasValuesFor(part, BuildingMinLevelTag.class))
            {
                return neighboringParts.stream().noneMatch(neighbor -> neighborsHeightContains(part,
                        neighbor, BuildingMinLevelTag.KEY, BuildingLevelsTag.KEY));
            }
        }
        return true;
    }

    private Predicate<Area> neighboringPart(final Area part)
    {
        return area ->
        {
            final Polygon partPolygon = part.asPolygon();
            final Polygon areaPolygon = area.asPolygon();
            return this.isBuildingPart(area) && (partPolygon.fullyGeometricallyEncloses(areaPolygon)
                    || partPolygon.stream().anyMatch(areaPolygon::contains));
        };
    }

    private boolean neighborsHeightContains(final Area part, final Area neighbor,
            final String minKey, final String maxKey)
    {
        final Map<String, String> neighborTags = neighbor.getOsmTags();
        final Map<String, String> partTags = part.getOsmTags();
        if (neighborTags.containsKey(maxKey))
        {
            final int partMinHeight = Integer.parseInt(partTags.get(minKey));
            final int partMaxHeight = Integer
                    .parseInt(partTags.getOrDefault(maxKey, String.valueOf(partMinHeight)));
            final int neighborMaxHeight = Integer.parseInt(neighborTags.get(maxKey));
            final int neighborMinHeight = Integer
                    .parseInt(neighborTags.getOrDefault(minKey, ZERO_STRING));

            return Range.closed(partMinHeight, partMaxHeight)
                    .isConnected(Range.closed(neighborMinHeight, neighborMaxHeight));
        }
        return false;
    }

    private boolean isBuildingPart(final AtlasObject object)
    {
        return Validators.isOfType(object, BuildingPartTag.class, BuildingPartTag.YES);
    }

    private boolean hasMinKey(final AtlasObject object)
    {
        return Validators.hasValuesFor(object, BuildingMinLevelTag.class)
                || Validators.hasValuesFor(object, MinHeightTag.class);
    }
}
