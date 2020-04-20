package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags railways (configurable), streets (configurable), buildings that bleed into an ocean. An
 * ocean is defined by a set of ocean tags, and can be an {@link Area} or {@link LineItem}.
 *
 * @author seancoulter
 */
public class OceanBleedingCheck extends BaseCheck<Long>
{
    private static final String DEFAULT_VALID_OCEAN_TAGS = "natural->strait,channel,fjord,sound,bay|"
            + "harbour->*&harbour->!no|estuary->*&estuary->!no|bay->*&bay->!no|place->sea|seamark:type->harbour,harbour_basin,sea_area|water->bay,cove,harbour|waterway->artificial,dock";
    private final TaggableFilter validOceanTags;
    private static final String DEFAULT_INVALID_OCEAN_TAGS = "man_made->breakwater,pier"
            + "|natural->beach,marsh,swamp" + "|water->marsh"
            + "|wetland->bog,fen,mangrove,marsh,saltern,saltmarsh,string_bog,swamp,wet_meadow"
            + "|landuse->*";
    private final TaggableFilter invalidOceanTags;
    private static final String DEFAULT_OCEAN_BOUNDARY_TAGS = "natural->coastline";
    private final TaggableFilter oceanBoundaryTags;
    private static final String DEFAULT_OFFENDING_MISCELLANEOUS_LINEITEMS = "railway->rail,narrow_gauge,preserved,subway,disused,monorail,tram,light_rail,funicular,construction,miniature";
    private final TaggableFilter defaultOffendingLineitems;
    private static final String DEFAULT_HIGHWAY_MINIMUM = "TOLL_GANTRY";
    private final HighwayTag highwayMinimum;
    private static final List<String> DEFAULT_HIGHWAYS_EXCLUDE = Collections.emptyList();
    private final List<HighwayTag> highwaysExclude;
    private static final String OCEAN_INSTRUCTION = "Ocean feature {0,number,#} has invalid intersections.";
    private static final String BLEEDING_BUILDING_INSTRUCTION = "Building {0,number,#} intersects the ocean feature.";
    private static final String BLEEDING_LINEITEM_INSTRUCTION = "Way {0,number,#} intersects the ocean feature.";
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
        this.validOceanTags = TaggableFilter.forDefinition(
                this.configurationValue(configuration, "ocean.valid", DEFAULT_VALID_OCEAN_TAGS));
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
                "ocean.boundary", DEFAULT_OCEAN_BOUNDARY_TAGS));
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
        return this.validOceanTags.test(object) && !this.invalidOceanTags.test(object)
                && (object instanceof Area || object instanceof LineItem)
                || object instanceof LineItem && this.oceanBoundaryTags.test(object);
    }

    /**
     * We flag railways, streets, and buildings that intersect the ocean feature, so each flag is a
     * collection of all intersections for a given ocean feature.
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
        // intersecting features
        final boolean oceanIsArea = object instanceof Area;

        // Ocean boundary, make it a closed polygon
        final Polygon oceanBoundary = oceanIsArea ? ((Area) object).asPolygon()
                : new Polygon(((LineItem) object).asPolyLine());

        // Collect offending line items (non-bridges) and buildings
        // We do a second check in the predicate for actual intersection on the ocean boundary if
        // the ocean boundary is a LineItem. Or else we just use the area polygon.
        final Iterable<LineItem> intersectingRoads = object.getAtlas().lineItemsIntersecting(
                oceanBoundary,
                lineItem -> (oceanIsArea
                        || ((LineItem) object).asPolyLine().intersects(lineItem.asPolyLine()))
                        && !BridgeTag.isBridge(lineItem)
                        && (lineItem instanceof Edge
                                && this.validHighwayType().test((Edge) lineItem)
                                || this.defaultOffendingLineitems.test(lineItem)));
        final Iterable<Area> intersectingBuildings = object.getAtlas().areasIntersecting(
                oceanBoundary,
                area -> (oceanIsArea
                        || ((LineItem) object).asPolyLine().intersects(area.asPolygon()))
                        && BuildingTag.isBuilding(area));

        // Unify all offenders in storage so the flag id is generated from a single set of flagged
        // objects
        final HashSet<AtlasObject> flaggedObjects = new HashSet<>();
        final StringBuilder instructions = new StringBuilder();
        instructions.append(this.getLocalizedInstruction(2, object.getOsmIdentifier()));
        intersectingBuildings.forEach(building ->
        {
            flaggedObjects.add(building);
            instructions.append(this.getLocalizedInstruction(0, building.getOsmIdentifier(),
                    object.getOsmIdentifier()));
        });
        intersectingRoads.forEach(road ->
        {
            flaggedObjects.add(road);
            instructions.append(this.getLocalizedInstruction(1, road.getOsmIdentifier(),
                    object.getOsmIdentifier()));
        });
        return flaggedObjects.isEmpty() ? Optional.empty()
                : Optional.of(this.createFlag(flaggedObjects, instructions.toString()));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
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
