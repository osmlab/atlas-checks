package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.CommonTagFilters;
import org.openstreetmap.atlas.checks.utility.IntersectionUtilities;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.tags.AmenityTag;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags railways (configurable), streets (configurable), buildings that bleed into an ocean. An
 * ocean is defined by a set of ocean tags, and can be an {@link Area} or {@link LineItem}. Differs
 * from {@link LineCrossingWaterBodyCheck} in that that check has a different set of tags/geometries
 * to define waterbodies.
 *
 * @author seancoulter
 */
public class OceanBleedingCheck extends BaseCheck<Long>
{
    private final TaggableFilter validOceanTags;
    private static final String DEFAULT_INVALID_OCEAN_TAGS = "man_made->breakwater,pier"
            + "|natural->beach,marsh,swamp" + "|water->marsh"
            + "|wetland->bog,fen,mangrove,marsh,saltern,saltmarsh,string_bog,swamp,wet_meadow"
            + "|landuse->*";
    private final TaggableFilter invalidOceanTags;
    private final TaggableFilter oceanBoundaryTags;
    private static final String DEFAULT_OFFENDING_MISCELLANEOUS_LINEITEMS = "railway->rail,narrow_gauge,preserved,subway,disused,monorail,tram,light_rail,funicular,construction,miniature";
    private final TaggableFilter defaultOffendingLineitems;
    private static final String DEFAULT_HIGHWAY_MINIMUM = "TOLL_GANTRY";
    private final HighwayTag highwayMinimum;
    private static final List<String> DEFAULT_HIGHWAYS_EXCLUDE = Collections.emptyList();
    private final List<HighwayTag> highwaysExclude;
    private static final String OCEAN_INSTRUCTION = "Ocean feature {0,number,#} has invalid intersections. ";
    private static final String BLEEDING_BUILDING_INSTRUCTION = "Building {0,number,#} intersects the ocean feature. ";
    private static final String BLEEDING_LINEITEM_INSTRUCTION = "Way {0,number,#} intersects the ocean feature. ";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            BLEEDING_BUILDING_INSTRUCTION, BLEEDING_LINEITEM_INSTRUCTION, OCEAN_INSTRUCTION);
    private static final long serialVersionUID = -2229281211747728380L;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public OceanBleedingCheck(final Configuration configuration)
    {
        super(configuration);
        this.validOceanTags = TaggableFilter.forDefinition(this.configurationValue(configuration,
                "ocean.valid", CommonTagFilters.DEFAULT_VALID_OCEAN_TAGS));
        this.invalidOceanTags = TaggableFilter.forDefinition(this.configurationValue(configuration,
                "ocean.invalid", DEFAULT_INVALID_OCEAN_TAGS));
        this.defaultOffendingLineitems = TaggableFilter.forDefinition(this.configurationValue(
                configuration, "lineItems.offending", DEFAULT_OFFENDING_MISCELLANEOUS_LINEITEMS));
        this.highwayMinimum = Enum.valueOf(HighwayTag.class,
                this.configurationValue(configuration, "highway.minimum", DEFAULT_HIGHWAY_MINIMUM)
                        .toUpperCase());
        this.highwaysExclude = this
                .configurationValue(configuration, "highway.exclude", DEFAULT_HIGHWAYS_EXCLUDE)
                .stream().map(element -> Enum.valueOf(HighwayTag.class, element.toUpperCase()))
                .collect(Collectors.toList());
        this.oceanBoundaryTags = TaggableFilter.forDefinition(this.configurationValue(configuration,
                "ocean.boundary", CommonTagFilters.DEFAULT_OCEAN_BOUNDARY_TAGS));
    }

    /**
     * This function will validate ocean features that are tagged appropriately.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return (object instanceof Area || object instanceof LineItem)
                && (this.validOceanTags.test(object) && !this.invalidOceanTags.test(object)
                        || this.oceanBoundaryTags.test(object));
    }

    /**
     * We flag railways, streets, and buildings that intersect or are within certain ocean features,
     * so each flag is a collection of all invalid interactions for a given ocean feature.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that flags the objects that invalidly intersect
     *         the ocean feature
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Use this flag to see if we need to check for actual intersection (not just intersection
        // on the closed surface representation of the PolyLine) when we query the Atlas looking for
        // offending land features
        final boolean oceanIsArea = object instanceof Area;

        // Ocean boundary, make it a closed polygon
        final Polygon oceanBoundary = oceanIsArea ? ((Area) object).asPolygon()
                : new Polygon(((LineItem) object).asPolyLine());

        // Differentiate between a coastline area (sometimes seen as islands) and a waterbody area
        final boolean oceanFeatureIsAWaterBody = this.validOceanTags.test(object);

        final ArrayList<LineItem> offendingLineItems = new ArrayList<>();
        final ArrayList<Area> offendingBuildings = new ArrayList<>();

        // Check if a land feature (building or line item) interacts with the ocean feature
        // invalidly. The land feature is assumed to be an Area or LineItem. Interactions that
        // should be flagged are as follows: -- the ocean feature is a waterbody Area and the land
        // feature is within or intersects the ocean feature -- the ocean feature is a LineItem or a
        // coastline Area (with natural=coastline) and the land feature intersects the ocean feature
        // Interactions that should not be flagged: -- the ocean feature is a coastline area or
        // lineitem, or a waterbody LineItem, and the land feature is within the ocean feature

        if (oceanIsArea && oceanFeatureIsAWaterBody)
        {
            // Collect invalid line items contained within and intersecting with the waterbody ocean
            // feature
            final Iterable<LineItem> intersectingLinearFeatures = object.getAtlas()
                    .lineItemsIntersecting(oceanBoundary,
                            isInvalidlyInteractingWithOcean(oceanBoundary));
            final Iterable<Area> intersectingBuildingFeatures = object.getAtlas()
                    .areasIntersecting(oceanBoundary, BuildingTag::isBuilding);
            intersectingLinearFeatures.forEach(offendingLineItems::add);
            intersectingBuildingFeatures.forEach(offendingBuildings::add);
        }
        else
        {
            // Collect invalid buildings items intersecting the ocean feature, which is either a
            // coastline landmass or linear waterbody
            final Iterable<LineItem> intersectingLinearFeatures = object.getAtlas()
                    .lineItemsIntersecting(oceanBoundary, lineItem -> (oceanIsArea
                            && !oceanBoundary.fullyGeometricallyEncloses(lineItem.asPolyLine())
                            || object instanceof LineItem && ((LineItem) object).asPolyLine()
                                    .intersects(lineItem.asPolyLine()))
                            && isInvalidlyInteractingWithOcean(
                                    oceanIsArea ? oceanBoundary : ((LineItem) object).asPolyLine())
                                            .test(lineItem));
            final Iterable<Area> intersectingBuildingFeatures = object.getAtlas().areasIntersecting(
                    oceanBoundary,
                    area -> (oceanIsArea
                            && !oceanBoundary.fullyGeometricallyEncloses(area.asPolygon())
                            || object instanceof LineItem && ((LineItem) object).asPolyLine()
                                    .intersects(area.asPolygon()))
                            && BuildingTag.isBuilding(area));
            intersectingLinearFeatures.forEach(offendingLineItems::add);
            intersectingBuildingFeatures.forEach(offendingBuildings::add);
        }
        return this.generateFlag(object, offendingLineItems, offendingBuildings);
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Generate and return flag for this ocean feature if there were offending items
     * 
     * @param object
     *            the ocean feature
     * @param offendingLineItems
     *            offending streets/railways
     * @param offendingBuildings
     *            offending buildings
     * @return the flag for this ocean feature if flaggable items were found
     */
    private Optional<CheckFlag> generateFlag(final AtlasObject object,
            final ArrayList<LineItem> offendingLineItems, final ArrayList<Area> offendingBuildings)
    {
        // Unify all offenders in storage so the flag id is generated from a single set of flagged
        // objects
        final HashSet<AtlasObject> flaggedObjects = new HashSet<>();
        final StringBuilder instructions = new StringBuilder();
        instructions.append(this.getLocalizedInstruction(2, object.getOsmIdentifier()));
        offendingBuildings.forEach(building ->
        {
            flaggedObjects.add(building);
            instructions.append(this.getLocalizedInstruction(0, building.getOsmIdentifier()));
        });
        final Set<Long> seenLineItems = new HashSet<>();
        offendingLineItems.forEach(lineItem ->
        {
            flaggedObjects.add(lineItem);
            if (!seenLineItems.contains(lineItem.getOsmIdentifier()))
            {
                instructions.append(this.getLocalizedInstruction(1, lineItem.getOsmIdentifier()));
                seenLineItems.add(lineItem.getOsmIdentifier());
            }
        });
        return flaggedObjects.isEmpty() ? Optional.empty()
                : Optional.of(this.createFlag(flaggedObjects, instructions.toString()));
    }

    /**
     * Checks if the provided {@link LineItem} should be flagged. It should be flagged if it's not a
     * bridge and is either an edge with the correct highway type that is not explicitly connected
     * to a ferry terminal, or a Line that has at least one of the configurable offending tags
     *
     * @return true if lineItem should be flagged, false otherwise
     */
    private Predicate<LineItem> isInvalidlyInteractingWithOcean(final PolyLine oceanFeature)
    {
        return lineItem ->
        {
            if (BridgeTag.isBridge(lineItem))
            {
                return false;
            }
            if (!(lineItem instanceof Edge))
            {
                return this.defaultOffendingLineitems.test(lineItem);
            }
            if (!this.validHighwayType().test((Edge) lineItem))
            {
                return false;
            }
            if (IntersectionUtilities.haveExplicitLocationsForIntersections(oceanFeature, lineItem))
            {
                // All intersections are explicit (or there are none -> full containment), so make
                // sure they're marked as ferry terminals
                final Set<Location> intersections = oceanFeature
                        .intersections(lineItem.asPolyLine());

                if (!intersections.contains(((Edge) lineItem).start().getLocation())
                        && !intersections.contains(((Edge) lineItem).end().getLocation()))
                {
                    // The point of intersection was at an intermediate position along the edge, or
                    // there were no intersections (full containment in the waterbody)
                    return true;
                }
                return ((Edge) lineItem).connectedNodes().stream()
                        .filter(node -> intersections.contains(node.getLocation()))
                        .anyMatch(node -> !node.getTag(AmenityTag.KEY).orElse("")
                                .equalsIgnoreCase(AmenityTag.FERRY_TERMINAL.name()));
            }
            return true;
        };
    }

    /**
     * Validates the supplied street {@link Edge}
     *
     * @return true if the Edge is a valid street, false otherwise
     */
    private Predicate<Edge> validHighwayType()
    {
        return edge -> edge.highwayTag().isMoreImportantThanOrEqualTo(this.highwayMinimum)
                && !this.highwaysExclude.contains(edge.highwayTag());
    }

}
