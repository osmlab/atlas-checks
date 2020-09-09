package org.openstreetmap.atlas.checks.validation.areas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.IntersectionUtilities;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This checks water areas for overlaps and missing waterways (where appropriate).
 *
 * @author Taylor Smock
 */
public class WaterAreaCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -2567398383133412329L;

    private static final List<String> WATER_FILTERS = Arrays
            .asList("natural->water&water->*|waterway->riverbank");
    private static final List<String> WATER_FILTERS_WATERWAY = Arrays
            .asList("natural->water&water->river,stream_pool,canal,lock|waterway->riverbank");
    private static final List<String> WATERWAY_FILTERS = Arrays.asList("waterway->*");

    // https://wiki.openstreetmap.org/wiki/Tag:waterway%3Ddam specifies that dams
    // may cross other waterways
    // Probably should not cross other waterways with the same tag though.
    private static final List<String> WATERWAY_CROSSING_IGNORE = Arrays.asList("waterway->dam");

    private static final String INSTRUCTION_MISSING_WATERWAY = "Waterway area (id={0,number,#}) is missing a waterway way.";
    private static final String INSTRUCTION_NO_EXITING_WATERWAY = "Waterway area (id={0,number,#}) has a waterway way, but there are none entering/exiting.";
    private static final String INSTRUCTION_WATERWAY_INTERSECTION = "Waterway area (id={0,number,#}) intersects with at least one other waterway area (id={1}).";

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            INSTRUCTION_MISSING_WATERWAY, INSTRUCTION_NO_EXITING_WATERWAY,
            INSTRUCTION_WATERWAY_INTERSECTION);

    private static final double MINIMUM_PROPORTION_DEFAULT = 0.01;

    private final double minimumIntersect;

    // List of TaggableFilters where each filter represents all tags for water areas
    // that should not overlap
    private final List<TaggableFilter> areaFilters = new ArrayList<>();
    private final List<TaggableFilter> waterRequiringWaterwayFilters = new ArrayList<>();
    private final List<TaggableFilter> waterwayFilters = new ArrayList<>();
    private final List<TaggableFilter> waterwayCrossingIgnore = new ArrayList<>();

    /**
     * Check if an object matches a filter
     * 
     * @param filters
     *            The filters to check against
     * @param object
     *            The object to check
     * @return {@code true} if the object matches *any* filter
     */
    public static boolean matchesFilter(final List<TaggableFilter> filters,
            final AtlasObject object)
    {
        return filters.parallelStream().anyMatch(filter -> filter.test(object));
    }

    /**
     * Check if two objects match the same filter
     * 
     * @param filters
     *            The filters to check
     * @param object1
     *            An AtlasObject to check
     * @param object2
     *            Another AtlasObject to check
     * @return {@code true} if both objects match the same filter
     */
    public static boolean matchesSameFilter(final List<TaggableFilter> filters,
            final AtlasObject object1, final AtlasObject object2)
    {
        return filters.parallelStream()
                .anyMatch(filter -> filter.test(object1) && filter.test(object2));
    }

    /**
     * Create a new WaterAreaCheck
     * 
     * @param configuration
     *            The configuration for the new Check
     */
    public WaterAreaCheck(final Configuration configuration)
    {
        super(configuration);
        this.minimumIntersect = this.configurationValue(configuration, "intersect.minimum.limit",
                MINIMUM_PROPORTION_DEFAULT);
        List<String> filtersString = configurationValue(configuration, "water.tags.filters",
                WATER_FILTERS);
        filtersString.forEach(string -> this.areaFilters.add(TaggableFilter.forDefinition(string)));
        filtersString = configurationValue(configuration, "waterway.tags.filters",
                WATERWAY_FILTERS);
        filtersString
                .forEach(string -> this.waterwayFilters.add(TaggableFilter.forDefinition(string)));
        filtersString = configurationValue(configuration, "water.tags.filtersrequireswaterway",
                WATER_FILTERS_WATERWAY);
        filtersString.forEach(string -> this.waterRequiringWaterwayFilters
                .add(TaggableFilter.forDefinition(string)));
        filtersString = configurationValue(configuration, "water.tags.crossing.ignore",
                WATERWAY_CROSSING_IGNORE);
        filtersString.forEach(
                string -> this.waterwayCrossingIgnore.add(TaggableFilter.forDefinition(string)));
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Area && (!isFlagged(object.getOsmIdentifier()))
                && matchesFilter(this.areaFilters, object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Area area = (Area) object;
        final Polygon areaPolygon = area.getClosedGeometry();
        final List<Line> waterways = Iterables
                .stream(area.getAtlas().linesIntersecting(areaPolygon,
                        atlasObject -> matchesFilter(this.waterwayFilters, atlasObject)))
                .collectToList();
        CheckFlag flag = checkForMissingWaterway(null, area, waterways);
        flag = checkForNoExitingWays(flag, areaPolygon, area, waterways);
        flag = checkForOverlappingWaterways(flag, area);

        if (flag != null)
        {
            super.markAsFlagged(object.getOsmIdentifier());
        }
        return Optional.ofNullable(flag);
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Check if an object is already flagged
     * 
     * @param objects
     *            The objects to check
     * @return {@code true} if *all* objects are flagged
     */
    private boolean alreadyFlagged(final List<? extends AtlasObject> objects)
    {
        return this.getFlaggedIdentifiers().containsAll(objects.parallelStream()
                .map(AtlasObject::getOsmIdentifier).collect(Collectors.toList()));
    }

    /**
     * Check a waterway area for a missing waterway way
     * 
     * @param flag
     *            The flag to add data to. May be null.
     * @param area
     *            The area to check
     * @param waterways
     *            The waterways intersecting with the waterway area
     * @return The modified CheckFlag (or new CheckFlag, if the passed CheckFlag was null)
     */
    private CheckFlag checkForMissingWaterway(final CheckFlag flag, final Area area,
            final List<Line> waterways)
    {
        CheckFlag returnFlag = flag;
        if (waterways.isEmpty() && matchesFilter(this.waterRequiringWaterwayFilters, area))
        {
            if (returnFlag == null)
            {
                returnFlag = new CheckFlag(this.getTaskIdentifier(area));
            }
            returnFlag.addInstruction(this.getLocalizedInstruction(
                    FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_MISSING_WATERWAY),
                    area.getOsmIdentifier()));
        }
        return returnFlag;
    }

    /**
     * Check a waterway area for exiting and entering waterways (only checks for one or the other)
     * 
     * @param flag
     *            The flag to add data to. May be null.
     * @param areaPolygon
     *            The area polygon (needed to avoid recalculating the area polygon)
     * @param area
     *            The area to check for exiting waterways
     * @param waterways
     *            The waterways that intersect with the waterway area
     * @return The modified CheckFlag (or new CheckFlag, if the passed CheckFlag was null)
     */
    private CheckFlag checkForNoExitingWays(final CheckFlag flag, final Polygon areaPolygon,
            final Area area, final List<Line> waterways)
    {
        CheckFlag returnFlag = flag;
        if (!waterways.isEmpty())
        {
            final List<Segment> areaSegments = areaPolygon
                    .segments().stream().filter(segment -> waterways.parallelStream()
                            .map(LineItem::asPolyLine).anyMatch(segment::intersects))
                    .collect(Collectors.toList());
            if (areaSegments.isEmpty())
            {
                if (returnFlag == null)
                {
                    returnFlag = new CheckFlag(this.getTaskIdentifier(area));
                }
                returnFlag.addInstruction(this.getLocalizedInstruction(
                        FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_NO_EXITING_WATERWAY),
                        area.getOsmIdentifier()));
            }
        }
        return returnFlag;
    }

    /**
     * Check for overlapping waterways
     * 
     * @param flag
     *            The flag to add data to. May be null.
     * @param area
     *            The area to check for overlapping waterways
     * @return The modified CheckFlag (or new CheckFlag, if the passed CheckFlag was null)
     */
    private CheckFlag checkForOverlappingWaterways(final CheckFlag flag, final Area area)
    {
        CheckFlag returnFlag = flag;

        final List<Pair<Segment, List<Area>>> possibleAreaIntersections = area.getClosedGeometry()
                .segments().stream()
                .map(segment -> Pair.of(segment,
                        Iterables
                                .stream(area.getAtlas().areasIntersecting(segment.bounds(),
                                        atlasObject -> matchesFilter(this.areaFilters, atlasObject)
                                                && !area.equals(atlasObject)
                                                && area.getClosedGeometry().intersects(
                                                        atlasObject.getClosedGeometry())))
                                .collectToList()))
                .filter(pair -> !pair.getRight().isEmpty()).collect(Collectors.toList());

        final List<Area> areaIntersections = possibleAreaIntersections.stream()
                .flatMap(pair -> pair.getRight().stream()).distinct()
                .filter(tArea -> !intersections(area.getClosedGeometry(), tArea.getClosedGeometry())
                        .isEmpty())
                .filter(tArea -> matchesFilter(this.waterwayCrossingIgnore, tArea)
                        && matchesFilter(this.waterwayCrossingIgnore, area)
                        || !matchesFilter(this.waterwayCrossingIgnore, tArea)
                                && !matchesFilter(this.waterwayCrossingIgnore, area))
                .collect(Collectors.toList());
        if (!areaIntersections.isEmpty() && !alreadyFlagged(areaIntersections))
        {
            if (returnFlag == null)
            {
                returnFlag = new CheckFlag(this.getTaskIdentifier(area));
            }
            returnFlag.addPoints(possibleAreaIntersections.stream()
                    .filter(pair -> !alreadyFlagged(pair.getRight())).map(Pair::getLeft)
                    .map(Segment::middle).collect(Collectors.toList()));
            returnFlag.addInstruction(this.getLocalizedInstruction(
                    FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_WATERWAY_INTERSECTION),
                    area.getOsmIdentifier(),
                    areaIntersections.stream().map(AtlasObject::getOsmIdentifier).distinct()
                            .map(Objects::toString).collect(Collectors.joining(", "))));
            areaIntersections.forEach(returnFlag::addObject);
            areaIntersections.stream().map(AtlasObject::getOsmIdentifier)
                    .forEach(super::markAsFlagged);
        }

        // Sometimes there will be two waterways that share every exterior intersection,
        // but one or the other cuts a corner somewhere.
        final List<Area> areaOverlaps = possibleAreaIntersections.stream()
                .flatMap(pair -> pair.getRight().stream()).distinct()
                .filter(tArea -> IntersectionUtilities.findIntersectionPercentage(
                        tArea.getClosedGeometry(),
                        area.getClosedGeometry()) >= this.minimumIntersect)
                .collect(Collectors.toList());
        if (!areaOverlaps.isEmpty())
        {
            if (returnFlag == null)
            {
                returnFlag = new CheckFlag(this.getTaskIdentifier(area));
            }
            returnFlag.addInstruction(this.getLocalizedInstruction(
                    FALLBACK_INSTRUCTIONS.indexOf(INSTRUCTION_WATERWAY_INTERSECTION),
                    area.getOsmIdentifier(),
                    areaOverlaps.stream().map(AtlasObject::getOsmIdentifier).distinct()
                            .map(Objects::toString).collect(Collectors.joining(", "))));
            areaOverlaps.forEach(returnFlag::addObject);
        }
        return returnFlag;
    }

    /**
     * Get the intersections between two polylines. Unlike {@link PolyLine#intersections}, this does
     * not include points that are shared between the two lines.
     *
     * @param line1
     *            A line to check for intersections
     * @param line2
     *            A line to check for intersections
     * @return The intersections of line1 and line2 where line1 and line2 are not connected.
     */
    private Set<Location> intersections(final PolyLine line1, final PolyLine line2)
    {
        // An intersection is any shared point OR overlap
        if (line1.intersects(line2))
        {
            final Set<Location> intersections = line1.intersections(line2);
            // Remove intersections that are points on both lines
            intersections.removeIf(
                    intersection -> line1.contains(intersection) && line2.contains(intersection));
            if (!intersections.isEmpty())
            {
                return intersections;
            }
        }
        return Collections.emptySet();
    }
}
