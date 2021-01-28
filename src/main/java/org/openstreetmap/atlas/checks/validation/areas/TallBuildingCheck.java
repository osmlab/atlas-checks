package org.openstreetmap.atlas.checks.validation.areas;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.BuildingLevelsTag;
import org.openstreetmap.atlas.tags.BuildingPartTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HeightTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Auto generated Check template
 *
 * @author v-garei
 */
public class TallBuildingCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -6979838256518757743L;
    private static final String TALL_BUILDING_INSTRUCTIONS = "Building {0, number, #} has a 'levels' tag over 100 or an outlying 'height' tag, please adjust them as needed.";
    private static final String TALL_BUILDING_OVER_HUNDRED_INSTRUCTIONS = "Building {0, number, #} has a levels tag above 100, please adjust levels as needed.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(TALL_BUILDING_INSTRUCTIONS);
    private static final double BUFFER_DISTANCE_DEFAULT = 1600.0;
    private final double bufferDistanceMeters;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public TallBuildingCheck(final Configuration configuration)
    {
        super(configuration);
        this.bufferDistanceMeters = this.configurationValue(configuration, "bufferDistanceMeters",
                BUFFER_DISTANCE_DEFAULT);

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
        return !this.isFlagged(object.getIdentifier())
                && (object instanceof Area
                || (object instanceof Relation && ((Relation) object).isMultiPolygon()))
                && (this.isBuildingOrPart(object) || this.isBuildingRelationMember(object));
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
        Map<String, String> tags = object.getOsmTags();
        Set<AtlasObject> flags = new HashSet<>();

        try
        {
            double levelQuantity = Double.parseDouble(tags.get(BuildingLevelsTag.KEY));
            if (hasBuildingLevelTag(tags) && levelQuantity > 100)
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(1,
                        object.getOsmIdentifier())));
            }
        }
        catch (Exception exception)
        {
            try
            {
                List<Double> sortedBuildingHeightList = getSortedBuildingHeightsWithinBufferRadius(object);
                Set<AtlasObject> intersectingBuildings = getIntersectingBuildings(object);

                double lowerQuartile = getLowerQuartile(sortedBuildingHeightList);
                double upperQuartile = getUpperQuartile(sortedBuildingHeightList);
                double innerQuartileRange = getInnerQuartileRange(sortedBuildingHeightList);
                double buildingHeight = Double.parseDouble(tags.get(HeightTag.KEY));

                if (hasBuildingHeightTag(tags) && lowerQuartile != -100.0
                        && upperQuartile != -100.0
                        && isStrongOutlier(buildingHeight, lowerQuartile, upperQuartile, innerQuartileRange))
                {
                    for (AtlasObject intersectingBuilding : intersectingBuildings)
                    {
                        Map<String, String> intersectingBuildingTags = intersectingBuilding.getTags();
                        if (intersectingBuildingTags.containsKey(HeightTag.KEY) && !intersectingBuildingTags.get(HeightTag.KEY).isEmpty())
                        {
                            try
                            {
                                double intersectingBuildingHeight = Double.parseDouble(intersectingBuildingTags.get(HeightTag.KEY));
                                if (!isFlagged(intersectingBuilding.getOsmIdentifier())
                                        && isStrongOutlier(intersectingBuildingHeight, lowerQuartile, upperQuartile, innerQuartileRange))
                                {
                                    markAsFlagged(intersectingBuilding.getOsmIdentifier());
                                    flags.add(intersectingBuilding);
                                }
                            }
                            catch (Exception ignored)
                            {

                            }
                        }
                    }
                    flags.add(object);
                    return Optional.of(this.createFlag(flags, this.getLocalizedInstruction(0,
                            object.getOsmIdentifier())));
                }
            }
            catch (Exception exception2)
            {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private Rectangle getBuildingBounds(AtlasObject  object)
    {
        return object.bounds();
    }

    private double getLowerQuartile(List<Double> listOfBuildingHeights)
    {
        int lengthOfList = listOfBuildingHeights.size();
        if (lengthOfList == 0)
        {
            return -100.0;
        }
        return listOfBuildingHeights.get((int) Math.round(lengthOfList / 4.0));
    }

    private double getUpperQuartile(List<Double> listOfBuildingHeights)
    {
        int lengthOfList = listOfBuildingHeights.size();
        if (lengthOfList == 0)
        {
            return -100.0;
        }
        return listOfBuildingHeights.get((int) Math.round((lengthOfList / 4.0) * 2.0));
    }

    private double getInnerQuartileRange(List<Double> listOfSortedBuildingHeights)
    {
        double lowerQuartile = getLowerQuartile(listOfSortedBuildingHeights);
        double upperQuartile = getUpperQuartile(listOfSortedBuildingHeights);

        return upperQuartile - lowerQuartile;
    }

    private Rectangle getBufferArea(Rectangle bounds)
    {
        return bounds.expand(Distance.meters(this.bufferDistanceMeters));
    }

    private Set<AtlasObject> getIntersectingBuildings(AtlasObject object)
    {
        Set<AtlasObject> buildingsWithinBuffer = new HashSet<>();
        Rectangle bufferArea = this.getBufferArea(this.getBuildingBounds(object));
        Iterable<AtlasItem> intersectingItems = object.getAtlas().itemsIntersecting(bufferArea);
        for (AtlasItem atlasItem : intersectingItems)
        {
            Map<String, String> tags = atlasItem.getTags();
            if ((this.isBuildingRelationMember(object) || this.isBuildingOrPart(object)) && this.hasBuildingHeightTag(tags))
            {
                buildingsWithinBuffer.add(atlasItem);
            }
        }
        return buildingsWithinBuffer;
    }

    private List<Double> getSortedBuildingHeightsWithinBufferRadius(AtlasObject object)
    {
        List<Double> buildingHeightsWithinBuffer = new ArrayList<>();
        Rectangle bufferArea = this.getBufferArea(this.getBuildingBounds(object));
        Iterable<AtlasItem> intersectingItems = object.getAtlas().itemsIntersecting(bufferArea);
        for (AtlasItem atlasItem : intersectingItems)
        {
            Map<String, String> tags = atlasItem.getTags();
            if ((this.isBuildingRelationMember(object) || this.isBuildingOrPart(object)) && this.hasBuildingHeightTag(tags))
            {
                try
                {
                    double buildingHeight = Double.parseDouble(tags.get(HeightTag.KEY));
                    buildingHeightsWithinBuffer.add(buildingHeight);
                }
                catch(Exception ignored)
                {

                }
            }
        }
        Collections.sort(buildingHeightsWithinBuffer);
        return buildingHeightsWithinBuffer;
    }

    private boolean isStrongOutlier(double buildingHeight, double lowerQuartile, double upperQuartile, double innerQuartileRange)
    {
        return buildingHeight < lowerQuartile - (innerQuartileRange * 3) || buildingHeight > upperQuartile + (upperQuartile * 3);
    }

    private boolean hasBuildingLevelTag(final Map<String, String> tags)
    {
        return tags.containsKey(BuildingLevelsTag.KEY);
    }

    private boolean hasBuildingHeightTag(Map<String, String> tags)
    {
        return tags.containsKey(HeightTag.KEY);
    }

    private boolean isBuildingOrPart(final AtlasObject object)
    {
        return BuildingTag.isBuilding(object)
                && (Validators.isNotOfType(object, BuildingPartTag.class, BuildingPartTag.NO)
                || Validators.isNotOfType(object, BuildingTag.class, BuildingTag.ROOF));
    }

    private boolean isBuildingRelationMember(final AtlasObject object)
    {
        return object instanceof AtlasEntity && ((AtlasEntity) object).relations().stream()
                .anyMatch(relation -> Validators.isOfType(relation, RelationTypeTag.class,
                        RelationTypeTag.BUILDING)
                        && relation.members().stream()
                        .anyMatch(member -> member.getEntity().equals(object)
                                && (member.getRole().equals("outline"))
                                || member.getRole().equals("part")));
    }
}

