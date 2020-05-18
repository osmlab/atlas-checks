package org.openstreetmap.atlas.checks.validation.areas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.IntersectionUtilities;
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
 * This checks water areas for overlaps and missing waterways (where appropriate)
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

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Waterway area (id={0,number,#}) is missing a waterway way.",
            "Waterway area (id={0,number,#}) has a waterway way, but there are none entering/exiting.",
            "Waterway area (id={0,number,#}) intersects with at least one other waterway area (id={1}).");

    private static final double MINIMUM_PROPORTION_DEFAULT = 0.01;

    private final double minimumIntersect;

    // List of TaggableFilters where each filter represents all tags for water areas
    // that
    // should not overlap
    private final List<TaggableFilter> areaFilters = new ArrayList<>();
    private final List<TaggableFilter> waterRequiringWaterwayFilters = new ArrayList<>();
    private final List<TaggableFilter> waterwayFilters = new ArrayList<>();

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
        final Polygon areaPolygon = area.asPolygon();
        final List<Area> intersections = Iterables
                .stream(area.getAtlas().areasIntersecting(areaPolygon,
                        o -> matchesSameFilter(this.areaFilters, object, o)
                                && IntersectionUtilities.findIntersectionPercentage(areaPolygon,
                                        o.asPolygon()) >= this.minimumIntersect
                                && !object.equals(o)))
                .collectToList();
        final List<Line> waterways = Iterables.stream(area.getAtlas().linesIntersecting(areaPolygon,
                o -> matchesFilter(this.waterwayFilters, o))).collectToList();
        final CheckFlag flag = new CheckFlag(this.getTaskIdentifier(object));
        flag.addObject(object);
        if (waterways.isEmpty() && matchesFilter(this.waterRequiringWaterwayFilters, object))
        {
            // TODO add appropriate instruction
            flag.addInstruction(this.getLocalizedInstruction(0, object.getOsmIdentifier()));
            this.markAsFlagged(object.getOsmIdentifier());
            return Optional.of(flag);
        }
        else if (!intersections.isEmpty())
        {
            intersections.forEach(flag::addObject);
            this.markAsFlagged(object.getOsmIdentifier());
            intersections.stream().map(AtlasObject::getOsmIdentifier).forEach(this::markAsFlagged);
            flag.addInstruction(
                    "Intersections exist: " + object.getOsmIdentifier() + " intersects with "
                            + String.join(", ",
                                    intersections.stream().map(AtlasObject::getOsmIdentifier)
                                            .map(Object::toString).collect(Collectors.toList())));
            return Optional.of(flag);
        }
        else if (!waterways.isEmpty())
        {
            final List<Segment> areaSegments = areaPolygon
                    .segments().stream().filter(s -> waterways.parallelStream()
                            .map(LineItem::asPolyLine).anyMatch(s::intersects))
                    .collect(Collectors.toList());
            if (areaSegments.isEmpty())
            {
                // TODO add appropriate instruction
                flag.addInstruction(this.getLocalizedInstruction(1, object.getOsmIdentifier()));
                this.markAsFlagged(object.getOsmIdentifier());
                return Optional.of(flag);
            }
            else
            {
                final List<Pair<Segment, List<Area>>> possibleAreaIntersections = areaSegments
                        .stream().map(
                                b -> Pair.of(b,
                                        Iterables
                                                .stream(object.getAtlas().areasIntersecting(
                                                        b.bounds(),
                                                        o -> matchesFilter(this.waterwayFilters, o)
                                                                && !object.equals(o)
                                                                && o.asPolygon()
                                                                        .overlaps(areaPolygon)))
                                                .collectToList()))
                        .filter(p -> !p.getRight().isEmpty()).collect(Collectors.toList());

                final List<Area> areaIntersections = possibleAreaIntersections.stream()
                        .flatMap(p -> p.getRight().stream()
                                .filter(a -> a.asPolygon().segments().stream()
                                        .noneMatch(s -> p.getLeft().equals(s)
                                                || (s.start().equals(p.getLeft().end())
                                                        && s.end().equals(p.getLeft().start())))))
                        .collect(Collectors.toList());
                if (!areaIntersections.isEmpty() && !alreadyFlagged(areaIntersections))
                {
                    flag.addPoints(possibleAreaIntersections.stream()
                            .filter(p -> !alreadyFlagged(p.getRight())).map(Pair::getLeft)
                            .map(Segment::middle).collect(Collectors.toList()));
                    // TODO add appropriate instruction
                    flag.addInstruction(this.getLocalizedInstruction(2, object.getOsmIdentifier(),
                            String.join(", ",
                                    areaIntersections.stream().map(AtlasObject::getOsmIdentifier)
                                            .distinct().map(Objects::toString)
                                            .collect(Collectors.toList()))));
                    areaIntersections.forEach(flag::addObject);
                    this.markAsFlagged(object.getOsmIdentifier());
                    return Optional.of(flag);
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

    private boolean alreadyFlagged(final List<? extends AtlasObject> objects)
    {
        return this.getFlaggedIdentifiers().containsAll(objects.parallelStream()
                .map(AtlasObject::getOsmIdentifier).collect(Collectors.toList()));
    }
}
