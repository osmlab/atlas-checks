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

import groovy.lang.Tuple4;

/**
 * The purpose of this check is to identify invalid building:levels and height tags as well as
 * identify statistically outlying building:levels/height tags.
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
    private final Map<Rectangle, Tuple4<String, Double, Double, Double>> storedAreasWithStatistics = new HashMap<>();
    private final Set<String> invalidHeightCharacters;
    private static final Set<String> INVALID_CHARACTER_DEFAULT = Set.of("~", "`", "!", "@", "#",
            "$", "%", "^", "&", "*", "(", ")", "-", "_", "+", "=", "{", "[", "}", "]", "|", "\\",
            ":", ";", "<", ",", ">", "?", "/");
    private static final int INSTRUCTION_THREE = 3;
    private static final int INSTRUCTION_FOUR = 4;
    private static final double ONE_QUARTER = 0.25;
    private static final double THREE_QUARTERS = 0.75;

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
        this.bufferDistanceMeters = this.configurationValue(configuration, "buffer.distance.meters",
                BUFFER_DISTANCE_DEFAULT);
        this.minDatasetSizeForStatsComputation = this.configurationValue(configuration,
                "min.dataset.size.for.stats.computation", MIN_DATASET_SIZE_DEFAULT);
        this.maxLevelTagValue = this.configurationValue(configuration, "max.level.tag.value",
                MAX_LEVEL_TAG_VALUE_DEFAULT);
        this.outlierMultiplier = this.configurationValue(configuration, "outlier.multiplier",
                OUTLIER_MULTIPLIER_DEFAULT);
        this.invalidHeightCharacters = new HashSet<>(this.configurationValue(configuration,
                "invalid.height.characters", INVALID_CHARACTER_DEFAULT));
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
        return !this.isFlagged(object.getOsmIdentifier())
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
            final Optional<CheckFlag> flag = this.buildingLevelsTagFlagLogic(object, tags);
            if (flag.isPresent())
            {
                return flag;
            }
        }

        // height tag logic
        if (this.hasHeightTag(tags))
        {
            final Optional<CheckFlag> flag = this.heightTagFlagLogic(object, tags);
            if (flag.isPresent())
            {
                return flag;
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
     * Determines which case the building:levels tag falls under.
     * 
     * @param object
     *            building object
     * @param tags
     *            building object tags
     * @return Optional CheckFlag
     */
    private Optional<CheckFlag> buildingLevelsTagFlagLogic(final AtlasObject object,
            final Map<String, String> tags)
    {
        final double buildingLevelsTagValue;
        try
        {
            buildingLevelsTagValue = Double.parseDouble(tags.get(BuildingLevelsTag.KEY));
        }

        // Case 2: Building has an invalid building:levels tag because it cannot be parsed.
        catch (final NumberFormatException e)
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(1, object.getOsmIdentifier())));
        }

        // Case 1: Building:levels tag greater than 100
        if (buildingLevelsTagValue > this.maxLevelTagValue)
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }

        // Don't run statistics if building contains building=apartments tag (apartments seem to
        // be showing up a lot as outliers)
        if (!(tags.containsKey(BuildingTag.KEY)
                && tags.get(BuildingTag.KEY).equalsIgnoreCase(BuildingTag.APARTMENTS.toString())))
        {
            // Check store to see if building intersects stored entry rectangle
            final Optional<Tuple4<String, Double, Double, Double>> rectangleStatsWhichIntersectObjectOptional = this
                    .getRectangleStatsWhichIntersectObject(object, BuildingLevelsTag.KEY);

            // Case 3: Building intersects stored area AND Levels tag is a statistical outlier
            // compared to surrounding buildings with building:levels tag.
            if (rectangleStatsWhichIntersectObjectOptional.isPresent()
                    && this.isOutlier(buildingLevelsTagValue,
                            rectangleStatsWhichIntersectObjectOptional.get().getSecond(),
                            rectangleStatsWhichIntersectObjectOptional.get().getThird(),
                            rectangleStatsWhichIntersectObjectOptional.get().getFourth()))
            {
                return Optional.of(this.createFlag(object,
                        this.getLocalizedInstruction(2, object.getOsmIdentifier())));
            }

            // Case 3: Building does not intersect stored area but levels tag is a statistical
            // outlier compared to surrounding buildings with building:levels tag.
            if (rectangleStatsWhichIntersectObjectOptional.isEmpty())
            {
                return this.getStatisticsWithNewStoreEntry(object, buildingLevelsTagValue,
                        BuildingLevelsTag.KEY);
            }

        }
        return Optional.empty();
    }

    /**
     * get buffer area by expanding object bounds
     * 
     * @param bounds
     *            bounds of object
     * @return expanded bounds of object
     */
    private Rectangle getBufferArea(final Rectangle bounds)
    {
        return bounds.expand(Distance.meters(this.bufferDistanceMeters));
    }

    /**
     * get object bounds
     * 
     * @param object
     *            building
     * @return building bounds
     */
    private Rectangle getBuildingBounds(final AtlasObject object)
    {
        return object.bounds();
    }

    /**
     * calculates inner quartile range (3rd quartile - 1st quartile) from sorted building height
     * list
     * 
     * @param listOfSortedHeights
     *            sorted building heights within buffer area
     * @return inner quartile range from list of sorted height
     */
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

    /**
     * @param object
     *            building
     * @param intersectingItems
     *            all intersecting AtlasItems
     * @param tagIdentifier
     *            identifier to filter either height or building:levels tag
     * @return building ID and relevant tag
     */
    private Set<Map<Long, String>> getIntersectingBuildingsIdAndRelevantTag(
            final AtlasObject object, final Iterable<AtlasItem> intersectingItems,
            final String tagIdentifier)
    {
        final Set<Map<Long, String>> buildingsWithRelevantTagWithinBuffer = new HashSet<>();
        for (final AtlasItem atlasItem : intersectingItems)
        {
            final Map<String, String> tags = atlasItem.getTags();
            if ((this.isBuildingRelationMember(object) || this.isBuildingOrPart(object))
                    && tags.containsKey(tagIdentifier))
            {
                final Map<Long, String> atlasItemProperties = new HashMap<>();
                atlasItemProperties.put(atlasItem.getOsmIdentifier(), tags.get(tagIdentifier));
                buildingsWithRelevantTagWithinBuffer.add(atlasItemProperties);
            }
        }
        return buildingsWithRelevantTagWithinBuffer;
    }

    /**
     * Function to get first quartile value
     * 
     * @param listOfRelevantTags
     *            sorted list of building tags intersecting with buffer area
     * @return first quarter value from list
     */
    private Optional<Double> getLowerQuartile(final List<Double> listOfRelevantTags)
    {
        final int lengthOfList = listOfRelevantTags.size() - 1;
        if (lengthOfList == 0)
        {
            return Optional.empty();
        }
        return Optional.of(listOfRelevantTags.get((int) Math.round(lengthOfList * ONE_QUARTER)));
    }

    /**
     * Function to check if building intersects stored area already with statistical values or if a
     * new store entry needs to be made.
     *
     * @param object
     *            building object
     * @param tagIdentifier
     *            either "building:levels" or "height" to identify category of analysis
     * @return Optional of relevant statistical values to compare object against.
     */
    private Optional<Tuple4<String, Double, Double, Double>> getRectangleStatsWhichIntersectObject(
            final AtlasObject object, final String tagIdentifier)
    {
        if (this.storedAreasWithStatistics.size() == 0)
        {
            return Optional.empty();
        }

        for (final Map.Entry<Rectangle, Tuple4<String, Double, Double, Double>> mapEntry : this.storedAreasWithStatistics
                .entrySet())
        {
            if (mapEntry.getValue().getFirst().equals(tagIdentifier))
            {

                final Rectangle rectangleKey = mapEntry.getKey();
                final Rectangle intersection = rectangleKey.intersection(object.bounds());

                if (intersection != null)
                {
                    return Optional.of(mapEntry.getValue());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Function to get relevant tags from intersecting buildings and to sort them for statistical
     * analysis.
     * 
     * @param buildingsWithRelevantTagWithinBuffer
     *            intersecting buildings with relevant tags
     * @param tagIdentifier
     *            either building:levels or height tag to determine which analysis to conduct.
     * @return sorted list of relevant intersecting building tags.
     */
    private List<Double> getSortedTagValuesWithinBufferRadius(
            final Set<Map<Long, String>> buildingsWithRelevantTagWithinBuffer,
            final String tagIdentifier)
    {
        final List<Double> tagsWithinBuffer = new ArrayList<>();

        for (final Map<Long, String> buildingWithinBuffer : buildingsWithRelevantTagWithinBuffer)
        {
            for (final Map.Entry<Long, String> entry : buildingWithinBuffer.entrySet())
            {
                this.parseAndAddRelativeTagsToTagsWithinBuffer(tagIdentifier, entry,
                        tagsWithinBuffer);
            }
        }

        Collections.sort(tagsWithinBuffer);
        return tagsWithinBuffer;
    }

    /**
     * Function to perform new statistical analysis and create new store entry when building does
     * not intersect an already stored area.
     * 
     * @param object
     *            building object
     * @param tagValue
     *            building tag value either "building:levels" or "height".
     * @param tagIdentifier
     *            distinguish which tag to conduct analysis on.
     * @return CheckFlag if building tag is outlier.
     */
    private Optional<CheckFlag> getStatisticsWithNewStoreEntry(final AtlasObject object,
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
                this.storedAreasWithStatistics.put(bufferArea, new Tuple4<>(tagIdentifier,
                        lowerQuartile.get(), upperQuartile.get(), innerQuartileRange.get()));
                if (this.isOutlier(tagValue, lowerQuartile.get(), upperQuartile.get(),
                        innerQuartileRange.get()) && tagIdentifier.equals(BuildingLevelsTag.KEY))
                {
                    return Optional.of(this.createFlag(object,
                            this.getLocalizedInstruction(2, object.getOsmIdentifier())));
                }

                if (this.isOutlier(tagValue, lowerQuartile.get(), upperQuartile.get(),
                        innerQuartileRange.get()) && tagIdentifier.equals(HeightTag.KEY))
                {
                    return Optional.of(this.createFlag(object, this
                            .getLocalizedInstruction(INSTRUCTION_FOUR, object.getOsmIdentifier())));
                }

            }
        }
        return Optional.empty();
    }

    /**
     * Function to get third quartile value from sorted list of relevant tags.
     * 
     * @param listOfRelevantTags
     *            sorted list of relevant tags
     * @return third quartile value of dataset (tags)
     */
    private Optional<Double> getUpperQuartile(final List<Double> listOfRelevantTags)
    {
        final int lengthOfList = listOfRelevantTags.size() - 1;
        if (lengthOfList == 0)
        {
            return Optional.empty();
        }
        return Optional.of(listOfRelevantTags.get((int) Math.round(lengthOfList * THREE_QUARTERS)));
    }

    /**
     * Function to determine if building has "building:levels" tag.
     * 
     * @param tags
     *            building tags
     * @return boolean if it has specific tag.
     */
    private boolean hasBuildingLevelTag(final Map<String, String> tags)
    {
        return tags.containsKey(BuildingLevelsTag.KEY);
    }

    /**
     * Function to determine if building has "height" tag.
     * 
     * @param tags
     *            building tags
     * @return boolean if it has specific tag.
     */
    private boolean hasHeightTag(final Map<String, String> tags)
    {
        return tags.containsKey(HeightTag.KEY);
    }

    /**
     * Function to determine if height tag is invalid
     * 
     * @param heightTag
     *            building height tag
     * @return boolean if tag is invalid or not
     */
    private boolean hasInvalidHeightTag(final String heightTag)
    {
        return (!heightTag.contains(" ") && heightTag.contains("m"))
                || this.heightTagContainsInvalidCharacter(heightTag).isPresent()
                || !this.stringContainsNumber(heightTag)
                || this.parseHeightTag(heightTag).isEmpty();
    }

    /**
     * Function to determine if "height" tag has invalid characters (in the config)
     * 
     * @param heightTag
     *            building height tag raw value
     * @return Optional string of invalid character
     */
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

    /**
     * Function to flag "height" tag
     * 
     * @param object
     *            building object
     * @param tags
     *            building tags
     * @return CheckFlag based on cases
     */
    private Optional<CheckFlag> heightTagFlagLogic(final AtlasObject object,
            final Map<String, String> tags)
    {
        final String heightTag = tags.get(HeightTag.KEY);

        // Case 4: building has an invalid "height" tag
        if (this.hasInvalidHeightTag(heightTag))
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(INSTRUCTION_FOUR, object.getOsmIdentifier())));
        }

        // Don't run statistics if building contains building=apartments tag (apartments seem to be
        // showing up a lot as outliers)
        if (!(tags.containsKey(BuildingTag.KEY)
                && tags.get(BuildingTag.KEY).equalsIgnoreCase(BuildingTag.APARTMENTS.toString())))
        {
            final Optional<Double> buildingHeightTagValue = this.parseHeightTag(heightTag);

            if (buildingHeightTagValue.isPresent())
            {
                final Optional<Tuple4<String, Double, Double, Double>> rectangleStatsWhichIntersectObjectOptional = this
                        .getRectangleStatsWhichIntersectObject(object, HeightTag.KEY);
                // Case 5: Building intersects stored area AND "height" tag is an outlier
                // compared
                // to surrounding buildings with "height" tag.
                if (rectangleStatsWhichIntersectObjectOptional.isPresent()
                        && this.isOutlier(buildingHeightTagValue.get(),
                                rectangleStatsWhichIntersectObjectOptional.get().getSecond(),
                                rectangleStatsWhichIntersectObjectOptional.get().getThird(),
                                rectangleStatsWhichIntersectObjectOptional.get().getFourth()))
                {
                    return Optional.of(this.createFlag(object, this.getLocalizedInstruction(
                            INSTRUCTION_THREE, object.getOsmIdentifier())));
                }

                // Case 5: Building does not intersect stored area but "height" tag is an
                // outlier
                // compared to surrounding buildings with "height tag.
                if (rectangleStatsWhichIntersectObjectOptional.isEmpty())
                {
                    return this.getStatisticsWithNewStoreEntry(object, buildingHeightTagValue.get(),
                            HeightTag.KEY);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Function to determine if object is a building or part of a building
     * 
     * @param object
     *            Atlas object
     * @return boolean if is building or part of building
     */
    private boolean isBuildingOrPart(final AtlasObject object)
    {
        return BuildingTag.isBuilding(object)
                && (Validators.isNotOfType(object, BuildingPartTag.class, BuildingPartTag.NO)
                        || Validators.isNotOfType(object, BuildingTag.class, BuildingTag.ROOF));
    }

    /**
     * Function to determine if object is a building relation member
     * 
     * @param object
     *            Atlas object
     * @return boolean if object is a building relation member
     */
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

    /**
     * Function determining if relevantTag is a statistical outlier
     * 
     * @param relevantTag
     *            either building "building:levels" or "height" tag
     * @param lowerQuartile
     *            first quarter from sorted relevant tag list.
     * @param upperQuartile
     *            third quartile from sorted relevant tag list.
     * @param innerQuartileRange
     *            upperQuartile - lowerQuartile
     * @return boolean if relevant tag is an outlier determined by statistical outlier analysis
     */
    private boolean isOutlier(final double relevantTag, final double lowerQuartile,
            final double upperQuartile, final double innerQuartileRange)
    {
        double innerQuartileRangeAdjusted = innerQuartileRange;
        if (innerQuartileRange < 1)
        {
            innerQuartileRangeAdjusted = innerQuartileRange + 1.0;
        }
        return relevantTag < lowerQuartile - (innerQuartileRangeAdjusted * this.outlierMultiplier)
                || relevantTag > upperQuartile
                        + (innerQuartileRangeAdjusted * this.outlierMultiplier);
    }

    /**
     * Function to parse and add relevant tag values to list of tags within buffer
     * 
     * @param tagIdentifier
     *            either "building:levels" or "height" tag
     * @param entry
     *            single entry key: osmID, value: relevant tag
     * @param tagsWithinBuffer
     *            list of relevant tags within buffer area
     */
    private void parseAndAddRelativeTagsToTagsWithinBuffer(final String tagIdentifier,
            final Map.Entry<Long, String> entry, final List<Double> tagsWithinBuffer)
    {
        if (tagIdentifier.equals(BuildingLevelsTag.KEY))
        {
            try
            {
                final double levelsTagValue = Double.parseDouble(entry.getValue());
                tagsWithinBuffer.add(levelsTagValue);
            }
            catch (final Exception ignored)
            {
                /* Do Nothing */
            }
        }
        if (tagIdentifier.equals(HeightTag.KEY))
        {
            try
            {
                final Optional<Double> height = this.parseHeightTag(entry.getValue());
                height.ifPresent(tagsWithinBuffer::add);
            }
            catch (final Exception ignored)
            {
                /* Do Nothing */
            }
        }
    }

    /**
     * Function to parse "height" tag
     * 
     * @param heightTagValue
     *            raw height tag value
     * @return height as double
     */
    private Optional<Double> parseHeightTag(final String heightTagValue)
    {
        // Valid height tag if there is a space so we split if there is a space and take the first
        // index to check if it is a number.
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

        // If there isn't a space we just try to parse a number
        try
        {
            return Optional.of(Double.parseDouble(heightTagValue));
        }
        catch (final Exception e)
        {
            return Optional.empty();
        }
    }

    /**
     * Function to determine if string contains numerical value
     * 
     * @param heightTag
     *            raw "height" tag
     * @return boolean if string contains numerical value
     */
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
