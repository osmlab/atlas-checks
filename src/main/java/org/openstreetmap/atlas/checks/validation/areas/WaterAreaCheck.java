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

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Waterway area (id={0,number,#}) is missing a waterway way.",
            "Waterway area (id={0,number,#}) has a waterway way, but there are none entering/exiting.",
            "Waterway area (id={0,number,#}) intersects with at least one other waterway area (id={1}).");

    private static final double MINIMUM_PROPORTION_DEFAULT = 0.01;

    private final double minimumIntersect;

    // List of TaggableFilters where each filter represents all tags for water areas
    // that should not overlap
    private final List<TaggableFilter> areaFilters = new ArrayList<>();
    private final List<TaggableFilter> waterRequiringWaterwayFilters = new ArrayList<>();
    private final List<TaggableFilter> waterwayFilters = new ArrayList<>();
    private final List<TaggableFilter> waterwayCrossingIgnore = new ArrayList<>();

    public static boolean matchesFilter(final List<TaggableFilter> filters,
            final AtlasObject object)
    {
        return filters.parallelStream().anyMatch(f -> f.test(object));
    }

    public static boolean matchesSameFilter(final List<TaggableFilter> filters,
            final AtlasObject object1, final AtlasObject object2)
    {
        return filters.parallelStream().anyMatch(f -> f.test(object1) && f.test(object2));
    }

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
        filtersString = configurationValue(configuration, "water.tags.filters.requireswaterway",
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
        final List<Line> waterways = Iterables.stream(area.getAtlas().linesIntersecting(areaPolygon,
                o -> matchesFilter(this.waterwayFilters, o))).collectToList();
        CheckFlag flag = null;
        if (waterways.isEmpty() && matchesFilter(this.waterRequiringWaterwayFilters, object))
        {
            flag = new CheckFlag(this.getTaskIdentifier(object));
            flag.addInstruction(this.getLocalizedInstruction(0, object.getOsmIdentifier()));
        }
        if (!waterways.isEmpty())
        {
            final List<Segment> areaSegments = areaPolygon
                    .segments().stream().filter(s -> waterways.parallelStream()
                            .map(LineItem::asPolyLine).anyMatch(s::intersects))
                    .collect(Collectors.toList());
            if (areaSegments.isEmpty())
            {
                if (flag == null)
                {
                    flag = new CheckFlag(this.getTaskIdentifier(object));
                }
                flag.addInstruction(this.getLocalizedInstruction(1, object.getOsmIdentifier()));
            }
        }
        final List<Pair<Segment, List<Area>>> possibleAreaIntersections = area.getClosedGeometry()
                .segments().stream()
                .map(b -> Pair.of(b,
                        Iterables
                                .stream(object.getAtlas().areasIntersecting(b.bounds(),
                                        o -> matchesFilter(this.areaFilters, o) && !object.equals(o)
                                                && area.getClosedGeometry()
                                                        .intersects(o.getClosedGeometry())))
                                .collectToList()))
                .filter(p -> !p.getRight().isEmpty()).collect(Collectors.toList());

        final List<Area> areaIntersections = possibleAreaIntersections.stream()
                .flatMap(p -> p.getRight().stream()).distinct()
                .filter(a -> !intersections(area.getClosedGeometry(), a.getClosedGeometry())
                        .isEmpty())
                .filter(a -> matchesFilter(this.waterwayCrossingIgnore, a)
                        && matchesFilter(this.waterwayCrossingIgnore, area)
                        || !matchesFilter(this.waterwayCrossingIgnore, a)
                                && !matchesFilter(this.waterwayCrossingIgnore, area))
                .collect(Collectors.toList());
        if (!areaIntersections.isEmpty() && !alreadyFlagged(areaIntersections))
        {
            if (flag == null)
            {
                flag = new CheckFlag(this.getTaskIdentifier(object));
            }
            flag.addPoints(
                    possibleAreaIntersections.stream().filter(p -> !alreadyFlagged(p.getRight()))
                            .map(Pair::getLeft).map(Segment::middle).collect(Collectors.toList()));
            flag.addInstruction(this.getLocalizedInstruction(2, object.getOsmIdentifier(),
                    areaIntersections.stream().map(AtlasObject::getOsmIdentifier).distinct()
                            .map(Objects::toString).collect(Collectors.joining(", "))));
            areaIntersections.forEach(flag::addObject);
            areaIntersections.stream().map(AtlasObject::getOsmIdentifier)
                    .forEach(super::markAsFlagged);
        }

        // Sometimes there will be two waterways that share every exterior intersection,
        // but one or the other cuts a corner somewhere.
        final List<Area> areaOverlaps = possibleAreaIntersections.stream()
                .flatMap(p -> p.getRight().stream()).distinct()
                .filter(a -> IntersectionUtilities.findIntersectionPercentage(a.getClosedGeometry(),
                        area.getClosedGeometry()) >= this.minimumIntersect)
                .collect(Collectors.toList());
        if (!areaOverlaps.isEmpty())
        {
            if (flag == null)
            {
                flag = new CheckFlag(this.getTaskIdentifier(object));
            }
            flag.addInstruction(this.getLocalizedInstruction(2, object.getOsmIdentifier(),
                    areaOverlaps.stream().map(AtlasObject::getOsmIdentifier).distinct()
                            .map(Objects::toString).collect(Collectors.joining(", "))));
            areaOverlaps.forEach(flag::addObject);
        }
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

    private boolean alreadyFlagged(final List<? extends AtlasObject> objects)
    {
        return this.getFlaggedIdentifiers().containsAll(objects.parallelStream()
                .map(AtlasObject::getOsmIdentifier).collect(Collectors.toList()));
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
            intersections.removeIf(l -> line1.contains(l) && line2.contains(l));
            if (!intersections.isEmpty())
            {
                return intersections;
            }
        }
        return Collections.emptySet();
    }
}
