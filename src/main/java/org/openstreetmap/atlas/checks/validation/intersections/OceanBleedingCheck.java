package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

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
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags railways, streets, buildings that bleed into an ocean. An ocean is defined by a set of
 * ocean tags, and can be an {@link Area} or {@link LineItem}.
 *
 * @author seancoulter
 */
public class OceanBleedingCheck extends BaseCheck<Long>
{
    private static final String VALID_OCEAN_TAGS = "natural->strait,channel,fjord,sound,bay|"
            + "harbour->*&harbour->!no|estuary->*&estuary->!no|bay->*&bay->!no|place->sea|seamark:type->harbour,harbour_basin,sea_area|water->bay,cove,harbour|waterway->artificial,dock";
    private static final TaggableFilter VALID_OCEAN_DEFINITIONS = TaggableFilter
            .forDefinition(VALID_OCEAN_TAGS);
    private static final String INVALID_OCEAN_TAGS = "man_made->breakwater,pier"
            + "|natural->beach,marsh,swamp" + "|water->marsh"
            + "|wetland->bog,fen,mangrove,marsh,saltern,saltmarsh,string_bog,swamp,wet_meadow";
    private static final TaggableFilter INVALID_OCEAN_DEFINITIONS = TaggableFilter
            .forDefinition(INVALID_OCEAN_TAGS);
    private static final Predicate<Area> IS_BUILDING = area -> Validators.isNotOfType(area,
            BuildingTag.class, BuildingTag.NO);
    private static final Predicate<Edge> VALID_HIGHWAY_TYPE = object -> object.highwayTag()
            .isMoreImportantThanOrEqualTo(HighwayTag.PATH)
            && Validators.isNotOfType(object, HighwayTag.class, HighwayTag.BUS_GUIDEWAY);
    private static final String VALID_OFFENDING_MISCELLANEOUS_LINEITEMS = "railway->rail,narrow_gauge,preserved,subway,disused,monorail,tram,light_rail,funicular,construction,miniature";
    private static final TaggableFilter VALID_OFFENDING_LINEITEM = TaggableFilter
            .forDefinition(VALID_OFFENDING_MISCELLANEOUS_LINEITEMS);
    private static final String BLEEDING_BUILDING_INSTRUCTION = "Building {0,number,#} intersects ocean feature {1,number,#}.";
    private static final String BLEEDING_LINEITEM_INSTRUCTION = "Way {0,number,#} intersects ocean feature {1,number,#}.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(BLEEDING_BUILDING_INSTRUCTION, BLEEDING_LINEITEM_INSTRUCTION);
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
        return VALID_OCEAN_DEFINITIONS.test(object) && !INVALID_OCEAN_DEFINITIONS.test(object)
                && (object instanceof Area || object instanceof LineItem);
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
        final Iterable<LineItem> intersectingRoads = object.getAtlas()
                .lineItemsIntersecting(oceanBoundary, lineItem -> (oceanIsArea
                        || (((LineItem) object).asPolyLine()).intersects(lineItem.asPolyLine()))
                        && (!Validators.hasValuesFor(lineItem, BridgeTag.class)
                                || Validators.isOfType(lineItem, BridgeTag.class, BridgeTag.NO))
                        && (lineItem instanceof Edge && VALID_HIGHWAY_TYPE.test((Edge) lineItem)
                                || VALID_OFFENDING_LINEITEM.test(lineItem)));
        final Iterable<Area> intersectingBuildings = object.getAtlas().areasIntersecting(
                oceanBoundary,
                area -> (oceanIsArea
                        || ((LineItem) object).asPolyLine().intersects(area.asPolygon()))
                        && IS_BUILDING.test(area));

        // Unify all offenders in storage so the flag id is generated from a single set of flagged
        // objects
        final HashSet<AtlasObject> flaggedObjects = new HashSet<>();
        final StringBuilder instructions = new StringBuilder();
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
}
