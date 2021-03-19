package org.openstreetmap.atlas.checks.validation.areas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import groovy.lang.Tuple2;
import groovy.lang.Tuple4;

/**
 * Auto generated Check template
 *
 * @author v-garei
 */
public class TallBuildingCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -6979838256518757743L;
    private static final String LEVEL_OVER_HUNDRED_INSTRUCTIONS = "Building {0, number, #} has a 'levels' tag over 100 please investigate.";
    private static final String NON_PARSABLE_LEVELS_TAG_INSTRUCTIONS = "Building {0, number, #} has improper building:levels syntax, should be a number. Please refer to https://wiki.openstreetmap.org/wiki/Key:building:levels for proper modeling.";
    private static final String BUILDING_IS_LEVELS_OUTLIER_INSTRUCTIONS = "Building {0, number, #} has an outlying levels tag compared to buildings nearby. Please investigate concerning outlier.";
    private static final String BUILDING_IS_HEIGHT_OUTLIER_INSTRUCTIONS = "Building {0, number, #} has an outlying height tag compared to buildings nearby. Please investigate concerning outlier.";
    private static final String INVALID_HEIGHT_TAG_INSTRUCTIONS = "Building {0, number, #} invalid height tag syntax. Please refer to https://wiki.openstreetmap.org/wiki/Key:height for modeling/syntax details.";
    private static final List<String> FALLBACK_INSTRUCTIONS = List.of(
            LEVEL_OVER_HUNDRED_INSTRUCTIONS, NON_PARSABLE_LEVELS_TAG_INSTRUCTIONS,
            BUILDING_IS_LEVELS_OUTLIER_INSTRUCTIONS, BUILDING_IS_HEIGHT_OUTLIER_INSTRUCTIONS,
            INVALID_HEIGHT_TAG_INSTRUCTIONS);
    private static final double BUFFER_DISTANCE_DEFAULT = 1600.0;
    private final double bufferDistanceMeters;
    private static final double MIN_DATASET_SIZE_DEFAULT = 50;
    private final double minDatasetSizeForStatsComputation;
    private final double maxLevelTagValue;
    private static final double MAX_LEVEL_TAG_VALUE_DEFAULT = 100;
    private static final double OUTLIER_MULTIPLIER_DEFAULT = 3;
    private final double outlierMultiplier;
    private final Cache<Tuple2<Rectangle, Integer>, Tuple4<String, Double, Double, Double>> cache = CacheBuilder
            .newBuilder().build();
    private final Set<String> invalidHeightCharacters;
    private static final Set<String> INVALID_CHARACTER_DEFAULT = Set.of("~", "`", "!", "@", "#",
            "$", "%", "^", "&", "*", "(", ")", "-", "_", "+", "=", "{", "[", "}", "]", "|", "\\",
            ":", ";", "<", ",", ">", "?", "/");
    private final Map<Rectangle, Integer> checkedBuildingsInArea = new HashMap<>();
    private static final int magicNumber3 = 3;
    private static final int magicNumber4 = 4;
    private static final double magicNumberOneQuarter = 0.25;
    private static final double magicNumberThreeQuarters = 0.75;

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
        this.minDatasetSizeForStatsComputation = this.configurationValue(configuration,
                "minDatasetSizeForStatsComputation", MIN_DATASET_SIZE_DEFAULT);
        this.maxLevelTagValue = this.configurationValue(configuration, "maxLevelTagValue",
                MAX_LEVEL_TAG_VALUE_DEFAULT);
        this.outlierMultiplier = this.configurationValue(configuration, "outlierMultiplier",
                OUTLIER_MULTIPLIER_DEFAULT);
        this.invalidHeightCharacters = new HashSet<>(this.configurationValue(configuration,
                "invalidHeightCharacters", INVALID_CHARACTER_DEFAULT));
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
        final Map<String, String> tags = object.getTags();
        return !this.isFlagged(object.getIdentifier())
                && ((object instanceof Area)
                        || (object instanceof Relation && ((Relation) object).isMultiPolygon()))
                && (this.isBuildingOrPart(object) || this.isBuildingRelationMember(object))
                && (this.hasHeightTag(tags) || this.hasBuildingLevelTag(tags));
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
        final Map<String, String> tags = object.getOsmTags();

        // level tag logic
        if (this.hasBuildingLevelTag(tags))
        {
            try
            {
                final double buildingLevelsTagValue = Double
                        .parseDouble(tags.get(BuildingLevelsTag.KEY));
                if (buildingLevelsTagValue > this.maxLevelTagValue)
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(0, object.getOsmIdentifier())));
                }

                final Optional<Tuple4<String, Double, Double, Double>> rectangleStatsWhichIntersectObjectOptional = this
                        .getRectangleStatsWhichIntersectObject(this.cache, object,
                                BuildingLevelsTag.KEY);

                if (rectangleStatsWhichIntersectObjectOptional.isPresent())
                {
                    final Tuple4<String, Double, Double, Double> objectIntersectsCachedRectangleStats = rectangleStatsWhichIntersectObjectOptional
                            .get();
                    if (this.isOutlier(buildingLevelsTagValue,
                            objectIntersectsCachedRectangleStats.getSecond(),
                            objectIntersectsCachedRectangleStats.getThird(),
                            objectIntersectsCachedRectangleStats.getFourth()))
                    {

                        return Optional.of(this.createFlag(object,
                                this.getLocalizedInstruction(2, object.getOsmIdentifier())));
                    }
                }

                if (rectangleStatsWhichIntersectObjectOptional.isEmpty())
                {
                    return this.getStatisticsWithNewCacheEntry(object, buildingLevelsTagValue,
                            BuildingLevelsTag.KEY);
                }

            }
            catch (final Exception e)
            {
                return Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(1, object.getOsmIdentifier())));
            }

        }

        // height tag logic
        if (this.hasHeightTag(tags))
        {
            final String heightTag = tags.get(HeightTag.KEY);
            if ((!heightTag.contains(" ") && heightTag.contains("m"))
                    || this.heightTagContainsInvalidCharacter(heightTag).isPresent()
                    || !this.stringContainsNumber(heightTag))
            {
                return Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(magicNumber4, object.getOsmIdentifier())));
            }
            try
            {

                final Optional<Double> buildingHeightTagValue = this.parseHeightTag(heightTag);
                if (buildingHeightTagValue.isPresent())
                {
                    final Optional<Tuple4<String, Double, Double, Double>> rectangleStatsWhichIntersectObjectOptional = this
                            .getRectangleStatsWhichIntersectObject(this.cache, object,
                                    HeightTag.KEY);

                    if (rectangleStatsWhichIntersectObjectOptional.isPresent())
                    {
                        final Tuple4<String, Double, Double, Double> objectIntersectsCachedRectangleStats = rectangleStatsWhichIntersectObjectOptional
                                .get();
                        if (this.isOutlier(buildingHeightTagValue.get(),
                                objectIntersectsCachedRectangleStats.getSecond(),
                                objectIntersectsCachedRectangleStats.getThird(),
                                objectIntersectsCachedRectangleStats.getFourth()))
                        {
                            return Optional.of(this.createFlag(object, this.getLocalizedInstruction(
                                    magicNumber3, object.getOsmIdentifier())));
                        }
                    }

                    if (rectangleStatsWhichIntersectObjectOptional.isEmpty())
                    {
                        return this.getStatisticsWithNewCacheEntry(object,
                                buildingHeightTagValue.get(), HeightTag.KEY);
                    }
                }

            }
            catch (final Exception e)
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

    private Rectangle getBufferArea(final Rectangle bounds)
    {
        return bounds.expand(Distance.meters(this.bufferDistanceMeters));
    }

    private Rectangle getBuildingBounds(final AtlasObject object)
    {
        return object.bounds();
    }

    private Optional<Double> getInnerQuartileRange(final List<Double> listOfSortedHeights)
    {
        final Optional<Double> lowerQuartile = this.getLowerQuartile(listOfSortedHeights);
        final Optional<Double> upperQuartile = this.getUpperQuartile(listOfSortedHeights);
        if (lowerQuartile.isPresent() && upperQuartile.isPresent())
        {
            return Optional.of(upperQuartile.get() - lowerQuartile.get());
        }
        return Optional.empty();
    }

    private Set<Map<Long, String>> getIntersectingBuildingsIdAndRelevantTag(
            final AtlasObject object, final Iterable<AtlasItem> intersectingItems,
            final String tagIdentifier)
    {
        final Set<Map<Long, String>> buildingsWithHeightTagWithinBuffer = new HashSet<>();
        for (final AtlasItem atlasItem : intersectingItems)
        {
            final Map<String, String> tags = atlasItem.getTags();
            if ((this.isBuildingRelationMember(object) || this.isBuildingOrPart(object))
                    && tags.containsKey(tagIdentifier))
            {
                final Map<Long, String> atlasItemProperties = new HashMap<>();
                atlasItemProperties.put(atlasItem.getOsmIdentifier(), tags.get(tagIdentifier));
                buildingsWithHeightTagWithinBuffer.add(atlasItemProperties);
            }
        }
        return buildingsWithHeightTagWithinBuffer;
    }

    private Optional<Double> getLowerQuartile(final List<Double> listOfHeights)
    {
        final int lengthOfList = listOfHeights.size() - 1;
        if (lengthOfList == 0)
        {
            return Optional.empty();
        }
        return Optional
                .of(listOfHeights.get((int) Math.round(lengthOfList * magicNumberOneQuarter)));
    }

    private Optional<Tuple4<String, Double, Double, Double>> getRectangleStatsWhichIntersectObject(
            final Cache<Tuple2<Rectangle, Integer>, Tuple4<String, Double, Double, Double>> cache,
            final AtlasObject object, final String tagIdentifier)
    {
        if (cache.size() == 0)
        {
            return Optional.empty();
        }

        final Map<Tuple2<Rectangle, Integer>, Tuple4<String, Double, Double, Double>> map = cache
                .asMap();

        for (final Map.Entry<Tuple2<Rectangle, Integer>, Tuple4<String, Double, Double, Double>> mapEntry : map
                .entrySet())
        {
            if (mapEntry.getValue().getFirst().equals(tagIdentifier))
            {

                final Rectangle rectangleKey = mapEntry.getKey().getFirst();
                final Rectangle intersection = rectangleKey.intersection(object.bounds());

                final int numberOfIntersectingBuildingsFromCache = mapEntry.getKey().getSecond();

                if (intersection != null)
                {
                    this.checkedBuildingsInArea.put(rectangleKey,
                            this.checkedBuildingsInArea.get(rectangleKey) + 1);
                    final int currentlyCheckedIntersectingBuildingsFromArea = this.checkedBuildingsInArea
                            .get(rectangleKey);

                    if (currentlyCheckedIntersectingBuildingsFromArea == numberOfIntersectingBuildingsFromCache)
                    {
                        final Tuple2<Rectangle, Integer> keyToInvalidate = new Tuple2<>(
                                rectangleKey, numberOfIntersectingBuildingsFromCache);
                        cache.invalidate(keyToInvalidate);
                    }
                    return Optional.of(mapEntry.getValue());
                }
            }
        }
        return Optional.empty();
    }

    private List<Double> getSortedTagValuesWithinBufferRadius(
            final Set<Map<Long, String>> buildingsWithRelevantTagWithinBuffer,
            final String tagIdentifier)
    {
        final List<Double> tagsWithinBuffer = new ArrayList<>();

        for (final Map<Long, String> buildingWithinBuffer : buildingsWithRelevantTagWithinBuffer)
        {
            for (final Map.Entry<Long, String> entry : buildingWithinBuffer.entrySet())
            {
                if (tagIdentifier.equals(BuildingLevelsTag.KEY))
                {
                    try
                    {
                        final double levelsTagValue = Double.parseDouble(entry.getValue());
                        tagsWithinBuffer.add(levelsTagValue);
                    }
                    catch (final Exception e)
                    {
                        continue;
                    }
                }
                if (tagIdentifier.equals(HeightTag.KEY))
                {
                    try
                    {
                        final Optional<Double> height = this.parseHeightTag(entry.getValue());
                        height.ifPresent(tagsWithinBuffer::add);
                    }
                    catch (final Exception e)
                    {
                        continue;
                    }

                }
            }
        }

        Collections.sort(tagsWithinBuffer);
        return tagsWithinBuffer;
    }

    private Optional<CheckFlag> getStatisticsWithNewCacheEntry(final AtlasObject object,
            final double tagValue, final String tagIdentifier)
    {
        final Rectangle bufferArea = this.getBufferArea(this.getBuildingBounds(object));
        final Iterable<AtlasItem> intersectingItems = object.getAtlas()
                .itemsIntersecting(bufferArea);
        final Set<Map<Long, String>> intersectingBuildingsWithRelevantTags = this
                .getIntersectingBuildingsIdAndRelevantTag(object, intersectingItems, tagIdentifier);
        final List<Double> sortedTagValueList = this.getSortedTagValuesWithinBufferRadius(
                intersectingBuildingsWithRelevantTags, tagIdentifier);

        if (sortedTagValueList.size() >= this.minDatasetSizeForStatsComputation)
        {
            final Optional<Double> lowerQuartile = this.getLowerQuartile(sortedTagValueList);
            final Optional<Double> upperQuartile = this.getUpperQuartile(sortedTagValueList);
            final Optional<Double> innerQuartileRange = this
                    .getInnerQuartileRange(sortedTagValueList);

            if (lowerQuartile.isPresent() && upperQuartile.isPresent()
                    && innerQuartileRange.isPresent())
            {
                this.cache.put(new Tuple2<>(bufferArea, sortedTagValueList.size()),
                        new Tuple4<>(tagIdentifier, lowerQuartile.get(), upperQuartile.get(),
                                innerQuartileRange.get()));
                this.checkedBuildingsInArea.put(bufferArea, 1);
                if (this.isOutlier(tagValue, lowerQuartile.get(), upperQuartile.get(),
                        innerQuartileRange.get()) && tagIdentifier.equals(BuildingLevelsTag.KEY))
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(2, object.getOsmIdentifier())));
                }

                if (this.isOutlier(tagValue, lowerQuartile.get(), upperQuartile.get(),
                        innerQuartileRange.get()) && tagIdentifier.equals(HeightTag.KEY))
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(magicNumber3, object.getOsmIdentifier())));
                }

            }
        }
        return Optional.empty();
    }

    private Optional<Double> getUpperQuartile(final List<Double> listOfHeights)
    {
        final int lengthOfList = listOfHeights.size() - 1;
        if (lengthOfList == 0)
        {
            return Optional.empty();
        }
        return Optional
                .of(listOfHeights.get((int) Math.round(lengthOfList * magicNumberThreeQuarters)));
    }

    private boolean hasBuildingLevelTag(final Map<String, String> tags)
    {
        return tags.containsKey(BuildingLevelsTag.KEY);
    }

    private boolean hasHeightTag(final Map<String, String> tags)
    {
        return tags.containsKey(HeightTag.KEY);
    }

    private Optional<String> heightTagContainsInvalidCharacter(final String heightTag)
    {
        for (final String invalidHeightTagCharacter : this.invalidHeightCharacters)
        {
            if (heightTag.contains(invalidHeightTagCharacter))
            {
                return Optional.of(invalidHeightTagCharacter);
            }
        }

        return Optional.empty();
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

    private boolean isOutlier(final double height, final double lowerQuartile,
            final double upperQuartile, final double innerQuartileRange)
    {
        double innerQuartileRangeAdjusted = innerQuartileRange;
        if (innerQuartileRange < 1)
        {
            innerQuartileRangeAdjusted = innerQuartileRange + 1.0;
        }
        return height < lowerQuartile - (innerQuartileRangeAdjusted * this.outlierMultiplier)
                || height > upperQuartile + (innerQuartileRangeAdjusted * this.outlierMultiplier);
    }

    private Optional<Double> parseHeightTag(final String heightTagValue)
    {
        if (heightTagValue.contains(" "))
        {
            try
            {
                final String[] splitString = heightTagValue.split(" ");
                if (splitString.length == 2)
                {
                    return Optional.of(Double.parseDouble(splitString[0]));
                }
            }
            catch (final Exception e)
            {
                return Optional.empty();
            }
        }
        try
        {
            return Optional.of(Double.parseDouble(heightTagValue));
        }
        catch (final Exception e)
        {
            return Optional.empty();
        }
    }

    private boolean stringContainsNumber(final String heightTag)
    {
        final char[] stringArray = heightTag.toCharArray();
        for (final char stringCharacter : stringArray)
        {
            if (Character.isDigit(stringCharacter))
            {
                return true;
            }
        }
        return false;
    }
}
