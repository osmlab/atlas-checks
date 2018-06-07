package org.openstreetmap.atlas.checks.validation.areas;

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

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;

    private static final String AOI_FILTER_DEFAULT = "tourism->zoo|sport->golf|"
            + "amenity->festival_grounds,grave_yard|boundary->national_park,protected_area|"
            + "historic->battlefield|landuse->cemetery,forest,recreation_ground,village_green|"
            + "natural->beach,wood|leisure->recreation_ground,garden,golf_course|leisure->park&name->*";

    private static final String AOI_DUPLICATES_DEFAULT = "landuse->cemetery&amenity->grave_yard|"
            + "landuse->forest&natural->wood|landuse->recreation_ground&leisure->recreation_ground|"
            + "leisure->garden&leisure->park|leisure->golf&sport->golf|leisure->park&landuse->village_green";

    private static final double MINIMUM_PROPORTION = 0.01;

    private final TaggableFilter aoiFilter;
    private final TaggableFilter aoiDuplicateFilter;

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
        this.aoiDuplicateFilter = (TaggableFilter) configurationValue(configuration,
                "aoi.tags.duplicates", AOI_DUPLICATES_DEFAULT,
                value -> new TaggableFilter(value.toString()));
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
                && (this.aoiFilter.test(object) || this.aoiDuplicateFilter.test(object))
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
                flag.addObject(area);
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

}
