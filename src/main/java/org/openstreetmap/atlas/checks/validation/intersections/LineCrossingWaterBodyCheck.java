package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.IntersectionUtilities;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.RelationOrAreaToMultiPolygonConverter;
import org.openstreetmap.atlas.tags.AdministrativeLevelTag;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LevelTag;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.NotesTag;
import org.openstreetmap.atlas.tags.PlaceTag;
import org.openstreetmap.atlas.tags.SourceTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags line items (edges or lines) and optionally buildings that are crossing water bodies
 * invalidly. Configurable values and
 * {@code LineCrossingWaterBodyCheck#canCrossWaterBody(AtlasItem)} and
 * {@code Utilities#haveExplicitLocationsForIntersections(Polygon, AtlasItem)} are used to decide
 * whether a crossing is valid or not.
 *
 * @author mertk
 * @author savannahostrowski
 * @author sayana_saithu
 * @author seancoulter
 */
public class LineCrossingWaterBodyCheck extends BaseCheck<Long>
{
    private static final String LINEAR_INSTRUCTION = "Linear item {0,number,#} is crossing water body invalidly.";
    private static final String BUILDING_INSTRUCTION = "Building item {0,number,#} is intersecting water body invalidly.";
    private static final String WATERBODY_INSTRUCTION = "The water body with id {0,number,#} has invalid crossings.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(WATERBODY_INSTRUCTION,
            LINEAR_INSTRUCTION, BUILDING_INSTRUCTION);
    private static final String ADDRESS_PREFIX_KEY = "addr";
    // Whitelist for line tags
    private static final Set<String> VALID_LINE_TAGS = Stream.of(NotesTag.KEY, SourceTag.KEY,
            NaturalTag.KEY, PlaceTag.KEY, AdministrativeLevelTag.KEY).collect(Collectors.toSet());
    // Whitelisted tags filter for multipolygon relations. Multipolygon relations with these tags
    // are expected to cross water bodies.
    private static final TaggableFilter VALID_RELATIONS_TAG_FILTER = TaggableFilter
            .forDefinition("natural->*|place->*|landuse->*|waterway->*|admin_level->*|boundary->*");
    private static final String DEFAULT_CAN_CROSS_WATER_BODY_TAGS = "waterway->*|boundary->*|landuse->*|"
            + "bridge->yes,viaduct,aqueduct,boardwalk,covered,low_water_crossing,movable,suspension|tunnel->yes,culvert,building_passage|"
            + "embankment->yes|location->underwater,underground|power->line,minor_line|"
            + "man_made->pier,breakwater,embankment,groyne,dyke,pipeline|route->ferry|highway->proposed,construction|ice_road->yes|ford->yes|winter_road->yes|snowmobile->yes|ski->yes";
    private TaggableFilter canCrossWaterBodyFilter;
    private TaggableFilter lineItemsOffending;
    private boolean flagBuildings;
    private static final String DEFAULT_HIGHWAY_MINIMUM = "TOLL_GANTRY";
    private HighwayTag highwayMinimum;
    private static final List<String> DEFAULT_HIGHWAYS_EXCLUDE = Collections.emptyList();
    private List<HighwayTag> highwaysExclude;
    private static final String BUILDING_TAGS_DO_NOT_FLAG = "public_transport->station,aerialway=station";
    private static final TaggableFilter NONOFFENDING_BUILDINGS = TaggableFilter
            .forDefinition(BUILDING_TAGS_DO_NOT_FLAG);

    private static final String WATER_BODY_TAGS =
            // Lakes
            "natural->spring,hot_spring&name->*" + "|natural->lake,pond" + "|water:type->lake"
                    + "|landuse->pond" + "|water->lake,pond,oxbow,salt_lake" +

                    // Rivers
                    "|natural->stream"
                    + "|water->canal,river,lock,moat,riverbank,creek,stream,stream_pool"
                    + "|waterway->river,riverbank,brook,ditch,stream,creek,canal,derelict_canal"
                    + "|stream->*" + "|waterway->drain&name->*" + "|water->drain&name->*" +

                    // Reservoirs
                    "|water->reservoir" + "|water->dam&natural->water" + "|landuse->reservoir"
                    + "|natural->reservoir" + "|seamark:type->dam&natural->water" +

                    // Miscellaneous
                    "|natural->water" + "|waterway->water" + "|water->water,perennial"
                    + "|landuse->water" +

                    // Wetlands
                    "|wetland->tidalflat,reedbed" + "|water->tidalflat,reedbed"
                    + "|natural->tidalflat,reedbed" +

                    // Lagoons
                    "|natural->lagoon" + "|water->lagoon" + "|waterway->lagoon" +

                    // Intermittent/Dry lakes
                    "|intermittent->dry" + "|seasonal->dry" + "|natural->dry_lake" +

                    // Unique
                    "|waterway->billabong,navigablechannel,river;stream,reservoir";
    private static final TaggableFilter VALID_WATER_BODY_TAGS = TaggableFilter
            .forDefinition(WATER_BODY_TAGS);

    private static final String WATER_BODY_EXCLUDE_TAGS = "natural->dock,water_point,floodway,spillway,wastewater,waterhole"
            + "|waterway->lock_gate,dock,water_point,floodway,spillway,wastewater,waterhole,culvert,dam,waterfall,fish_pass,dry_dock,construction,boat_lift,weir,breakwater,boatyard"
            + "|water->lock_gate,dock,water_point,floodway,spillway,wastewater,waterhole,pool,reflecting_pool,swimming_pool,salt_pool,fountain,tank,fish_pass"
            + "|tunnel->culvert" + "|waterway->drain&name->!" + "|water->drain&name->!"
            + "|wetland->tidalflat,reedbed&seasonal->yes"
            + "|water->tidalflat,reedbed&seasonal->yes"
            + "|natural->tidalflat,reedbed&seasonal->yes" + "|covered->yes" + "|highway->*"
            + "|natural->strait,channel,fjord,sound,bay" + "|harbour->*&harbour->!no"
            + "|estuary->*&estuary->!no" + "|bay->*&bay->!no"
            + "|seamark:type->harbour,harbour_basin,sea_area" + "|place->sea"
            + "|water->bay,cove,harbour" + "|waterway->artificial,dock"
            + "|man_made->breakwater,pier" + "|natural->beach,marsh,swamp" + "|water->marsh"
            + "|wetland->bog,fen,mangrove,marsh,saltern,saltmarsh,string_bog,swamp,wet_meadow"
            + "|waterway->drainage_channel,glacier,Minnow Falls, pumping_station"
            + "|water->tank,Earth_Tank_,_Off_Stream_Flow_Dam,treatment_pond,re#,swamp_-_occasional,trough,Trough,waste_water";
    private static final TaggableFilter INVALID_WATER_BODY_TAGS = TaggableFilter
            .forDefinition(WATER_BODY_EXCLUDE_TAGS);

    private static final long serialVersionUID = 6048659185833217159L;

    /**
     * Checks if the relation has whitelisted tags that makes its members cross water bodies
     * validly.
     *
     * @param multipolygonRelations
     * @return {@code true} if the multipolygon has valid tags which makes its members (lines) cross
     *         water body validly.
     */
    private static boolean canRelationCrossWaterBody(final Set<Relation> multipolygonRelations)
    {
        return multipolygonRelations.stream().anyMatch(VALID_RELATIONS_TAG_FILTER);
    }

    /**
     * Checks if the crossing item has "only" certain valid tags and no other valid tags related to
     * it. The VALID_LINE_TAGS allow the line to cross the waterbody.
     *
     * @param osmTags
     * @return {@code true} if the crossing item has only the valid tags and no other tags.
     */
    private static boolean hasOnlyValidCrossingTags(final Map<String, String> osmTags)
    {
        final Validators validators = new Validators(Taggable.class);
        final Set<String> keySet = osmTags.keySet();
        return keySet.stream().filter(validators::canValidate).count() == keySet.stream()
                .filter(VALID_LINE_TAGS::contains).count();
    }

    /**
     * Checks if the crossing item can be classified as a boundary. Since boundary relations are not
     * ingested into atlas, crossing line that is member of boundary relations is determined by
     * checking if any of the following three conditions are met: 1) If the line has no osm tags and
     * has no relations - if a line meets this criteria, it is essentially a part of boundary
     * relation and the boundary relation was not ingested into atlas. 2) If it is part of a
     * multipolygon relation and the relation has any of the whitelisted tags for multipolygon
     * relations. 3) If the crossing line has only the white listed tags in the VALID_LINE_TAGS list
     * and has no other tags. Whitelisted tags for relations and lines make the line valid to cross
     * water body.
     *
     * @param crossingLine
     * @return {@code true} if any of the three conditions are met.
     */
    private static boolean isBoundary(final AtlasEntity crossingLine)
    {
        final Map<String, String> osmTags = crossingLine.getOsmTags();
        final Set<Relation> relations = crossingLine.relations();
        final Set<Relation> multipolygonRelations = relations.stream()
                .filter(Relation::isMultiPolygon).collect(Collectors.toSet());
        // Crossing item is not part of any relation and has no tags, then infer it as part of a
        // boundary/coastline relation that is not ingested in the atlas.
        return osmTags.isEmpty() && relations.isEmpty()
                // Certain multipolygon relations can cross water body.
                || canRelationCrossWaterBody(multipolygonRelations)
                // If crossing item has only certain tags and is not part of multipolygon relations
                || multipolygonRelations.isEmpty() && hasOnlyValidCrossingTags(osmTags);
    }

    public LineCrossingWaterBodyCheck(final Configuration configuration)
    {
        super(configuration);
        this.lineItemsOffending = TaggableFilter
                .forDefinition(this.configurationValue(configuration, "lineItems.offending", ""));
        this.flagBuildings = this.configurationValue(configuration, "buildings.flag", false);
        this.canCrossWaterBodyFilter = TaggableFilter.forDefinition(this.configurationValue(
                configuration, "lineItems.non_offending", DEFAULT_CAN_CROSS_WATER_BODY_TAGS));
        this.highwayMinimum = Enum.valueOf(HighwayTag.class,
                this.configurationValue(configuration, "highway.minimum", DEFAULT_HIGHWAY_MINIMUM)
                        .toUpperCase());
        this.highwaysExclude = this
                .configurationValue(configuration, "highways.exclude", DEFAULT_HIGHWAYS_EXCLUDE)
                .stream().map(element -> Enum.valueOf(HighwayTag.class, element.toUpperCase()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // We only consider water body areas or multipolygon relations, not linear water bodies
        return (TypePredicates.IS_AREA.test(object)
                || (object instanceof Relation && ((Relation) object).isMultiPolygon()))
                && !INVALID_WATER_BODY_TAGS.test(object) && VALID_WATER_BODY_TAGS.test(object);
    }

    @Override
    @SuppressWarnings("S2293")
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // First convert the waterbody to a GeometricSurface for use in querying
        final GeometricSurface waterbody = object instanceof Area ? ((Area) object).asPolygon()
                : new RelationOrAreaToMultiPolygonConverter().convert((Relation) object);
        // Then retrieve the invalid crossing edges, lines, buildings
        final Atlas atlas = object.getAtlas();
        final Iterable<AtlasItem> invalidCrossingItems = this.flagBuildings
                ? new MultiIterable<>(atlas.lineItemsIntersecting(waterbody,
                        lineItem -> isOffendingLineItem(object, waterbody).test(lineItem)
                                && !this.canCrossWaterBody(lineItem)
                                && !this.intersectionsAreExplicit(waterbody, lineItem)),
                        atlas.areasIntersecting(waterbody,
                                area -> BuildingTag.isBuilding(area)
                                        && !NONOFFENDING_BUILDINGS.test(area)
                                        && LevelTag.areOnSameLevel(object, area)))
                : new MultiIterable<AtlasItem>(atlas.lineItemsIntersecting(waterbody,
                        lineItem -> isOffendingLineItem(object, waterbody).test(lineItem)
                                && !this.canCrossWaterBody(lineItem)
                                && !this.intersectionsAreExplicit(waterbody, lineItem)));

        // This waterbody has no invalid crossings
        if (!invalidCrossingItems.iterator().hasNext())
        {
            return Optional.empty();
        }

        final CheckFlag newFlag = new CheckFlag(getTaskIdentifier(object));
        newFlag.addObject(object);
        newFlag.addInstruction(this.getLocalizedInstruction(0, object.getOsmIdentifier()));

        // Only record an OSM id as crossing once in the instruction
        final Set<Long> recordedOsmIds = new HashSet<>();
        // Go through crossing items and collect invalid crossings
        // NOTE: Due to way sectioning same OSM way could be marked multiple times here. However,
        // MapRoulette will display way-sectioned edges in case there is an invalid crossing.
        // Therefore, if an OSM way crosses a water body multiple times in separate edges, then
        // each edge will be marked explicitly.
        for (final AtlasItem crossingItem : invalidCrossingItems)
        {
            // Update the flag
            newFlag.addObject(crossingItem);
            if (!recordedOsmIds.contains(crossingItem.getOsmIdentifier()))
            {
                newFlag.addInstruction(this.getLocalizedInstruction(
                        crossingItem instanceof Area ? 2 : 1, crossingItem.getOsmIdentifier()));
                recordedOsmIds.add(crossingItem.getOsmIdentifier());
            }
        }

        return Optional.of(newFlag);
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks if given {@link AtlasItem} can cross a water body
     *
     * @param crossingItem
     *            {@link AtlasItem} crossing
     * @return whether given {@link AtlasItem} can cross a water body
     */
    private boolean canCrossWaterBody(final AtlasItem crossingItem)
    {
        // In the following cases, given item can cross a water body

        return this.canCrossWaterBodyFilter.test(crossingItem)
                // It has a tag starting with addr
                || crossingItem.containsKeyStartsWith(Collections.singleton(ADDRESS_PREFIX_KEY))
                // If crossing item is a line and meets the conditions for a boundary
                || crossingItem instanceof Line && isBoundary(crossingItem);
    }

    /**
     * Returns true if the intersections between the parameter waterbody and the lineItem are marked
     * by Locations in the Atlas. In the case of a waterbody MultiPolygon, iterate through all inner
     * and outer members and make sure that if there is an intersection, there is a corresponding
     * Location
     *
     * @param waterbody
     *            the waterbody
     * @param lineItem
     *            the intersecting lineitem
     * @return true if all intersections are explicitly marked, false otherwise
     */
    private boolean intersectionsAreExplicit(final GeometricSurface waterbody,
            final LineItem lineItem)
    {
        if (waterbody instanceof Polygon)
        {
            return IntersectionUtilities.haveExplicitLocationsForIntersections((Polygon) waterbody,
                    lineItem);
        }
        final MultiPolygon waterbodyMultiPolygon = (MultiPolygon) waterbody;
        return waterbodyMultiPolygon.outers().stream().allMatch(polygon -> IntersectionUtilities
                .haveExplicitLocationsForIntersections(polygon, lineItem));
    }

    /**
     * True if the incoming LineItem is either an {@link Edge} with the correct highway type or
     * {@link LineItem} that has the correct tag combination, AND the lineItem is not a bridge, is
     * on the same level as the parameter waterbody, and if the waterbody is a MultiPolygon, the
     * LineItem intersects any outer polygon of the waterbody
     * 
     * @param object
     *            The waterbody
     * @return True if the incoming AtlasObject is an {@link Edge} with the correct highway type OR
     *         {@link LineItem} that has the correct tag combination, is not a bridge, and is on the
     *         same level as the parameter waterbody.
     */
    private Predicate<LineItem> isOffendingLineItem(final AtlasObject object,
            final GeometricSurface waterbody)
    {
        return lineItem -> (lineItem instanceof Edge
                && ((Edge) lineItem).highwayTag().isMoreImportantThanOrEqualTo(this.highwayMinimum)
                && !this.highwaysExclude.contains(((Edge) lineItem).highwayTag())
                || this.lineItemsOffending.test(lineItem))
                && (!Validators.hasValuesFor(lineItem, BridgeTag.class)
                        || Validators.isOfType(lineItem, BridgeTag.class, BridgeTag.NO))
                && LevelTag.areOnSameLevel(object, lineItem)
                && (!(waterbody instanceof MultiPolygon) || ((MultiPolygon) waterbody).outers()
                        .stream().anyMatch(lineItem::intersects));
    }
}
