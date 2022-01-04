package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.IntersectionUtilities;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Surface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flags the buildings that intersect/overlap with other buildings.
 *
 * @author sid
 * @author mkalender
 */
public class IntersectingBuildingsCheck extends BaseCheck<String>
{
    /**
     * Differentiate intersection and overlap and create separate descriptions for MapRoulette
     * challenges. Overlapping buildings are ones where intersection of two buildings covers at
     * least one of team.
     */
    public enum IntersectionType
    {
        NONE,
        INTERSECT,
        OVERLAP;
    }

    private static final Logger logger = LoggerFactory.getLogger(IntersectingBuildingsCheck.class);
    private static final long serialVersionUID = 5796448445672515517L;

    // Instructions
    private static final String INTERSECT_INSTRUCTION = "Building (id={0,number,#}) intersects with another building (id={1,number,#}).";
    private static final String OVERLAP_INSTRUCTION = "Building (id={0,number,#}) is overlapped by another building (id={1,number,#}).";
    private static final String CONTAINS_INSTRUCTION = "Building (id={0,number,#}) contains building (id={1,number,#}).";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(OVERLAP_INSTRUCTION,
            INTERSECT_INSTRUCTION, CONTAINS_INSTRUCTION);

    // Default values for configurable settings
    private static final double INTERSECTION_LOWER_LIMIT_DEFAULT = 0.01;

    // Minimum number of points for a polygon
    private static final int MINIMUM_POINT_COUNT_FOR_POLYGON = 3;

    // Format to use to generate unique identifier tuples from areas
    private static final String UNIQUE_IDENTIFIER_FORMAT = "%s,%s";

    // Minimum intersection to be contained
    private static final double OVERLAP_LOWER_LIMIT = 1.0;

    // Overlap below this limit is not considered to be intersecting
    private final double intersectionLowerLimit;

    /**
     * Generates a unique identifier tuple as a {@link String} for given {@link Area}s identifiers
     *
     * @param area
     *            First {@link Area} instance
     * @param otherArea
     *            Another {@link Area} instance
     * @return A unique identifier tuple in {@link String}
     */
    private static String getIdentifierTuple(final Area area, final Area otherArea)
    {
        return area.getIdentifier() < otherArea.getIdentifier()
                ? String.format(UNIQUE_IDENTIFIER_FORMAT, area.getIdentifier(),
                        otherArea.getIdentifier())
                : String.format(UNIQUE_IDENTIFIER_FORMAT, otherArea.getIdentifier(),
                        area.getIdentifier());
    }

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public IntersectingBuildingsCheck(final Configuration configuration)
    {
        super(configuration);

        this.intersectionLowerLimit = this.configurationValue(configuration,
                "intersection.lower.limit", INTERSECTION_LOWER_LIMIT_DEFAULT, Double::valueOf);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Area && BuildingTag.isBuilding(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Area building = (Area) object;

        // Fetch building's area as polygon and make sure it has at least 3 points
        final Polygon buildingPolygon = building.asPolygon();
        if (buildingPolygon.size() < MINIMUM_POINT_COUNT_FOR_POLYGON)
        {
            return Optional.empty();
        }

        // Fetch possibly intersecting buildings
        final Iterable<Area> possiblyIntersectingBuildings = object.getAtlas().areasIntersecting(
                building.bounds(),
                area -> BuildingTag.isBuilding(area)
                        && building.getIdentifier() != area.getIdentifier()
                        && area.intersects(buildingPolygon));

        // Assuming that we'd find intersections/overlaps below, create a flag
        final CheckFlag flag = new CheckFlag(this.getTaskIdentifier(object));
        flag.addObject(object);
        boolean hadIntersection = false;

        // Go over possible intersections
        for (final Area otherBuilding : possiblyIntersectingBuildings)
        {
            // Fetch other building's area as polygon and make sure it has at least 3 points
            final Polygon otherBuildingsPolygon = otherBuilding.asPolygon();
            if (otherBuildingsPolygon.size() < MINIMUM_POINT_COUNT_FOR_POLYGON)
            {
                continue;
            }

            // Create a unique identifier for building tuple to avoid processing same buildings more
            // than once
            final String uniqueIdentifier = getIdentifierTuple(building, otherBuilding);
            if (this.isFlagged(getIdentifierTuple(building, otherBuilding)))
            {
                continue;
            }

            // Find intersection type
            final IntersectionType resultType = this.findIntersectionType(buildingPolygon,
                    otherBuildingsPolygon);

            // Flag based on intersection type
            if (resultType == IntersectionType.OVERLAP)
            {
                // Get object and otherBuilding as a Surfaces
                final Surface objectAsSurface = ((Area) object).asPolygon().surface();
                final Surface otherBuildingAsSurface = otherBuilding.asPolygon().surface();
                final FeatureChange featureChange;
                // If object is larger than otherBuilding, the instruction states object contains
                // otherBuilding
                if (objectAsSurface.isLargerThan(otherBuildingAsSurface))
                {
                    featureChange = FeatureChange.remove(CompleteEntity.shallowFrom(otherBuilding));
                    flag.addObject(otherBuilding, this.getLocalizedInstruction(2,
                            object.getOsmIdentifier(), otherBuilding.getOsmIdentifier()));
                }
                // If object is smaller than otherBuilding, the instruction states otherBuilding
                // contains object
                else if (objectAsSurface.isLessThan(otherBuildingAsSurface))
                {
                    featureChange = FeatureChange
                            .remove(CompleteEntity.shallowFrom((AtlasEntity) object));
                    flag.addObject(otherBuilding, this.getLocalizedInstruction(2,
                            otherBuilding.getOsmIdentifier(), object.getOsmIdentifier()));
                }
                // If object and otherBuilding are equal, the instruction states that they overlap
                else
                {
                    featureChange = FeatureChange.remove(CompleteEntity.shallowFrom(otherBuilding));
                    flag.addObject(otherBuilding, this.getLocalizedInstruction(0,
                            object.getOsmIdentifier(), otherBuilding.getOsmIdentifier()));
                }
                flag.addFixSuggestion(featureChange);
                this.markAsFlagged(uniqueIdentifier);
                hadIntersection = true;
            }
            else if (resultType == IntersectionType.INTERSECT)
            {
                flag.addObject(otherBuilding, this.getLocalizedInstruction(1,
                        object.getOsmIdentifier(), otherBuilding.getOsmIdentifier()));
                this.markAsFlagged(uniqueIdentifier);
                hadIntersection = true;
            }
        }

        if (hadIntersection)
        {
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
     * Find {@link IntersectionType} for given {@link Polygon}s. There are some edge cases where
     * there are minor boundary intersections. So we do additional area check to filter off the
     * false positives.
     *
     * @param polygon
     *            {@link Polygon} to check for intersection
     * @param otherPolygon
     *            Another {@link Polygon} to check against for intersection
     * @return {@link IntersectionType} between given {@link Polygon}s
     */
    private IntersectionType findIntersectionType(final Polygon polygon, final Polygon otherPolygon)
    {
        final double proportion = IntersectionUtilities.findIntersectionPercentage(polygon,
                otherPolygon);
        if (proportion >= OVERLAP_LOWER_LIMIT)
        {
            return IntersectionType.OVERLAP;
        }
        else if (proportion >= this.intersectionLowerLimit)
        {
            return IntersectionType.INTERSECT;
        }

        return IntersectionType.NONE;
    }
}
