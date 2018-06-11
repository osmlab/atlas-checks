package org.openstreetmap.atlas.checks.validation.areas;

import java.util.ArrayList;
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
 * This check flags {@link Area}s that overlap and represent the same Area of Interest (AOI)
 *
 * @author danielbaah
 * @author bbreithaupt
 */
public class OverlappingAOIPolygonCheck extends BaseCheck
{

    private static final long serialVersionUID = -3286838841854959683L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Area (id={0,number,#}) overlaps area (id={1,number,#}) and represent the same AOI.");

    private static final List<String> AOI_FILTERS_DEFAULT = Arrays.asList(
            "amenity->FESTIVAL_GROUNDS", "amenity->GRAVE_YARD|landuse->CEMETERY",
            "boundary->NATIONAL_PARK,PROTECTED_AREA|leisure->NATURE_RESERVE,PARK",
            "historic->BATTLEFIELD", "landuse->FOREST|natural->WOOD",
            "landuse->RECREATION_GROUND|leisure->RECREATION_GROUND,PARK",
            "landuse->VILLAGE_GREEN|leisure->PARK", "leisure->GARDEN,PARK",
            "leisure->GOLF_COURSE|sport->GOLF", "leisure->PARK&name->*", "natural->BEACH",
            "tourism->ZOO");

    private static final double MINIMUM_PROPORTION_DEFAULT = 0.01;

    private final double minimumIntersect;

    private final List<String> aoiFiltersString;

    // List of TaggableFilters where each filter represents all tags for AOIs that should not
    // overlap
    private final List<TaggableFilter> aoiFilters = new ArrayList<>();

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
        this.minimumIntersect = (Double) this.configurationValue(configuration,
                "intersect.minimum.limit", MINIMUM_PROPORTION_DEFAULT);
        this.aoiFiltersString = (List<String>) configurationValue(configuration, "aoi.tags.filter",
                AOI_FILTERS_DEFAULT);
        this.aoiFiltersString.stream()
                .forEach(string -> this.aoiFilters.add(new TaggableFilter(string)));
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
        return object instanceof Area && (aoiFiltersTest(object))
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

        // Set of overlapping area AOIs
        final Set<Area> overlappingArea = Iterables.stream(object.getAtlas().areasIntersecting(
                aoiPolygon.bounds(),
                area -> area.getIdentifier() != aoi.getIdentifier() && area.intersects(aoiPolygon)
                        && aoiFiltersTest(area) && !this.isFlagged(area.getIdentifier())))
                .collectToSet();

        final CheckFlag flag = new CheckFlag(this.getTaskIdentifier(object));
        flag.addObject(object);

        // Test each overlapping AOI to see if it overlaps enough and passes the same AOI filter as
        // the object
        for (final Area area : overlappingArea)
        {
            if (this.hasMinimumOverlapProportion(aoi.asPolygon(), area.asPolygon()))
            {
                if (aoiFiltersTest(object, area))
                {
                    flag.addObject(area);
                    flag.addInstruction(this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                            area.getOsmIdentifier()));
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

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
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

    private boolean hasMinimumOverlapProportion(final Polygon polygon, final Polygon otherPolygon)
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

        return proportion >= this.minimumIntersect;
    }

    /**
     * Tests the input {@link AtlasObject} against the list of {@link TaggableFilter}s.
     *
     * @param object
     *            {@link AtlasObject} to be tested
     * @return true if any of the filters return true
     */
    private boolean aoiFiltersTest(final AtlasObject object)
    {
        for (final TaggableFilter filter : this.aoiFilters)
        {
            if (filter.test(object))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests the input {@link AtlasObject} and {@link Area} against the list of
     * {@link TaggableFilter}s and each other.
     *
     * @param object
     *            {@link AtlasObject} to be tested
     * @param area
     *            {@link Area} to be tested against {@code object}
     * @return true if any of the filters return true for both {@code object} and {@code area}
     */
    private boolean aoiFiltersTest(final AtlasObject object, final Area area)
    {
        for (final TaggableFilter filter : this.aoiFilters)
        {
            if (filter.test(object) && filter.test(area))
            {
                return true;
            }
        }
        return false;
    }

}
