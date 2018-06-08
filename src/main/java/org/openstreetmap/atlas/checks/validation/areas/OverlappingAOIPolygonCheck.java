package org.openstreetmap.atlas.checks.validation.areas;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.clipping.Clip;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import com.vividsolutions.jts.geom.TopologyException;

/**
 * Auto generated Check template
 *
 * @author danielbaah
 */
public class OverlappingAOIPolygonCheck extends BaseCheck
{

    private static final long serialVersionUID = -3286838841854959683L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList("Area (id={0,number,#}) overlaps area (id={1,number,#}) and has the same AOI tag.","Area (id={0,number,#}) overlaps area (id={1,number,#}) and has similar AOI tags.");

    private static final String AOI_FILTER_DEFAULT = "tourism->zoo|sport->golf|"
            + "amenity->festival_grounds,grave_yard|boundary->national_park,protected_area|"
            + "historic->battlefield|landuse->cemetery,forest,recreation_ground,village_green|"
            + "natural->beach,wood|leisure->recreation_ground,garden,golf_course|leisure->park&name->*";

    private static final List<List<String>> AOI_DUPLICATES_DEFAULT = Arrays.asList(
            Arrays.asList("landuse->cemetery", "amenity->grave_yard"),
            Arrays.asList("landuse->forest", "natural->wood"),
            Arrays.asList("landuse->recreation_ground", "leisure->recreation_ground"),
            Arrays.asList("leisure->garden", "leisure->park"),
            Arrays.asList("leisure->golf", "sport->golf"),
            Arrays.asList("leisure->park", "village_green"));

    private static final double MINIMUM_PROPORTION = 0.01;

    private final TaggableFilter aoiFilter;
    private final List<List<TaggableFilter>> aoiDuplicateFilters;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public OverlappingAOIPolygonCheck(final Configuration configuration)
    {
        super(configuration);
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH,
        // Distance::meters);
        this.aoiFilter = (TaggableFilter) configurationValue(configuration, "aoi.tags.filter",
                AOI_FILTER_DEFAULT, value -> new TaggableFilter(value.toString()));
        this.aoiDuplicateFilters = (List<List<TaggableFilter>>) configurationValue(configuration,
                "aoi.tags.duplicates", AOI_DUPLICATES_DEFAULT, value -> ((List) value).stream()
                        .map(subValue -> new TaggableFilter(subValue.toString())));
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
        // by default we will assume all objects as valid
        return object instanceof Area
                && (this.aoiFilter.test(object) || aoiDuplicateFiltersTest(object))
                && !this.isFlagged(object.getIdentifier());
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

        final Area aoi = (Area) object;
        final Polygon aoiPolygon = aoi.asPolygon();
        boolean hasOverlap = false;

        final Set<Area> overlappingArea = Iterables.stream(object.getAtlas().areasIntersecting(
                aoiPolygon.bounds(),
                area -> area.getIdentifier() != aoi.getIdentifier() && area.intersects(aoiPolygon)
                        && this.aoiFilter.test(area) && !this.isFlagged(area.getIdentifier())))
                .collectToSet();

        final CheckFlag flag = new CheckFlag(this.getTaskIdentifier(object));
        flag.addObject(object);

        for (final Area area : overlappingArea)
        {
            if (this.hasMinimumOverlapProportion(aoi.asPolygon(), area.asPolygon()))
            {
                if (this.aoiFilter.test(area)){
                    flag.addObject(area);
                    this.markAsFlagged(area.getIdentifier());
                    hasOverlap = true;
                }
                if (aoiDuplicateFiltersTest(object,area)){
                    flag.addObject(area);
                    this.markAsFlagged(area.getIdentifier());
                    hasOverlap = true;
                }
            }
        }

        if (hasOverlap)
        {
            this.markAsFlagged(object.getIdentifier());
            return Optional.of(flag);
        }

        return Optional.empty();
    }

    /**
     * Uses code from findIntersectionType function in BuildingRoadIntersection
     * 
     * @param polygon
     *            {@link Polygon} to check for intersection
     * @param otherPolygon
     *            Another {@link Polygon} to check against for intersection
     * @return true if the polygon to polygon overlap surface area is greater than minimum
     */

    private boolean hasMinimumOverlapProportion(Polygon polygon, Polygon otherPolygon)
    {

        Clip clip = null;
        try
        {
            clip = polygon.clip(otherPolygon, Clip.ClipType.AND);
        }
        catch (final TopologyException e)
        {
            System.out
                    .println(String.format("Error clipping [%s] and [%s].", polygon, otherPolygon));
        }

        // Skip if nothing is returned
        if (clip == null)
        {
            return false;
        }

        // Sum intersection area
        long intersectionArea = 0;
        for (final PolyLine polyline : clip.getClip())
        {
            if (polyline != null && polyline instanceof Polygon)
            {
                final Polygon clippedPolygon = (Polygon) polyline;
                intersectionArea += clippedPolygon.surface().asDm7Squared();
            }
        }

        // Avoid division by zero
        if (intersectionArea == 0)
        {
            return false;
        }

        // Pick the smaller building's area as baseline
        final long baselineArea = Math.min(polygon.surface().asDm7Squared(),
                otherPolygon.surface().asDm7Squared());
        final double proportion = (double) intersectionArea / baselineArea;

        return proportion >= MINIMUM_PROPORTION;
    }

    private boolean aoiDuplicateFiltersTest(final AtlasObject object)
    {
        for (final List<TaggableFilter> filters : this.aoiDuplicateFilters)
        {
            for (final TaggableFilter filter : filters)
            {
                if (filter.test(object))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean aoiDuplicateFiltersTest(final AtlasObject object, final Area area){
        for (final List<TaggableFilter> filters : this.aoiDuplicateFilters)
        {
            for (final TaggableFilter filter : filters)
            {
                if (filter.test(object))
                {
                    List<TaggableFilter> otherFilters = (List<TaggableFilter>) filters.stream().filter(tagFilter -> !tagFilter.equals(filter));
                    for (TaggableFilter otherFilter : otherFilters) {
                        if (otherFilter.test(area)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
