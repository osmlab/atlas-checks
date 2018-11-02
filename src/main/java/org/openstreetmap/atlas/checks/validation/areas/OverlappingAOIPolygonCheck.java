package org.openstreetmap.atlas.checks.validation.areas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.IntersectionUtilities;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags {@link Area}s that overlap and represent the same Area of Interest (AOI) AOIs
 * are defined by {@link TaggableFilter}s, which are grouped in a {@link List}.
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
            "landuse->RECREATION_GROUND|leisure->RECREATION_GROUND",
            "landuse->VILLAGE_GREEN|leisure->PARK", "leisure->GARDEN",
            "leisure->GOLF_COURSE|sport->GOLF", "leisure->PARK&name->*", "natural->BEACH",
            "tourism->ZOO");

    private static final double MINIMUM_PROPORTION_DEFAULT = 0.01;

    private final double minimumIntersect;

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
        final List<String> aoiFiltersString = (List<String>) configurationValue(configuration,
                "aoi.tags.filters", AOI_FILTERS_DEFAULT);
        aoiFiltersString
                .forEach(string -> this.aoiFilters.add(TaggableFilter.forDefinition(string)));
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
        // Checks for areas, that are not flagged, and pass any of the TaggableFilters in
        // aoiFilters. aoiFiltersTest() and aoiFilters is used in place of a single TaggableFilter
        // so that each filter may be tested separately later.
        return object instanceof Area && !this.isFlagged(object.getIdentifier())
                && aoiFiltersTest(object);
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
        final Rectangle aoiBounds = aoiPolygon.bounds();
        boolean hasOverlap = false;

        // Set of overlapping area AOIs
        final Set<Area> overlappingAreas = Iterables
                .stream(object.getAtlas().areasIntersecting(aoiBounds,
                        area -> area.getIdentifier() != aoi.getIdentifier()
                                && !this.isFlagged(area.getIdentifier())
                                && area.intersects(aoiPolygon) && aoiFiltersTest(area)))
                .collectToSet();

        final CheckFlag flag = new CheckFlag(this.getTaskIdentifier(object));
        flag.addObject(object);

        // Test each overlapping AOI to see if it overlaps enough and passes the same AOI filter as
        // the object
        for (final Area area : overlappingAreas)
        {
            if (IntersectionUtilities.findIntersectionPercentage(aoiPolygon,
                    area.asPolygon()) >= this.minimumIntersect && aoiFiltersTest(object, area))
            {
                flag.addObject(area);
                flag.addInstruction(this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                        area.getOsmIdentifier()));
                this.markAsFlagged(area.getIdentifier());
                hasOverlap = true;
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
     * Tests the input {@link AtlasObject} against the list of {@link TaggableFilter}s.
     *
     * @param object
     *            {@link AtlasObject} to be tested
     * @return true if any of the filters return true
     */
    private boolean aoiFiltersTest(final AtlasObject object)
    {
        return this.aoiFilters.stream().anyMatch(filter -> filter.test(object));
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
        return this.aoiFilters.stream()
                .anyMatch(filter -> filter.test(object) && filter.test(area));
    }
}
