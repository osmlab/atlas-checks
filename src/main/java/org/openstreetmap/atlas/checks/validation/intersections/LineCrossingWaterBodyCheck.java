package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.checks.atlas.predicates.TagPredicates;
import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.IntersectionUtilities;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.tags.AdministrativeLevelTag;
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
 * Flags line items (edges or lines) that are crossing water bodies invalidly.
 * {@code LineCrossingWaterBodyCheck#canCrossWaterBody(AtlasItem)} and
 * {@code Utilities#haveExplicitLocationsForIntersections(Polygon, AtlasItem)} is used to decide
 * whether a crossing is valid or not.
 *
 * @author mertk
 * @author savannahostrowski
 * @author sayana_saithu
 */
public class LineCrossingWaterBodyCheck extends BaseCheck<Long>
{
    private static final String LINEAR_INSTRUCTION = "Linear item {0,number,#} is crossing water body invalidly.";
    private static final String WATERBODY_INSTRUCTION = "The water body with id {0,number,#} has invalid crossings.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(WATERBODY_INSTRUCTION,
            LINEAR_INSTRUCTION);
    private static final String ADDRESS_PREFIX_KEY = "addr";
    // Whitelist for line tags

    private static final Set<String> VALID_LINE_TAGS = Stream.of(NotesTag.KEY, SourceTag.KEY,
            NaturalTag.KEY, PlaceTag.KEY, AdministrativeLevelTag.KEY).collect(Collectors.toSet());
    // Whitelisted tags filter for multipolygon relations. Multipolygon relations with these tags
    // are
    // expected to cross water bodies.
    private static final TaggableFilter VALID_RELATIONS_TAG_FILTER = TaggableFilter
            .forDefinition("natural->*|place->*|landuse->*|waterway->*|admin_level->*|boundary->*");
    private static final String CAN_CROSS_WATER_BODY_TAGS = "waterway->*|boundary->*|landuse->*|"
            + "bridge->yes,viaduct,aqueduct,boardwalk,covered,low_water_crossing,movable,suspension|tunnel->yes,culvert,building_passage|"
            + "embankment->yes|location->underwater,underground|power->line,minor_line|"
            + "man_made->pier,breakwater,embankment,groyne,dyke,pipeline|route->ferry|highway->proposed,construction|ice_road->yes|ford->yes|winter_road->yes|snowmobile->yes|ski->yes";
    private static final TaggableFilter CAN_CROSS_WATER_BODY_FILTER = TaggableFilter
            .forDefinition(CAN_CROSS_WATER_BODY_TAGS);
    private static final long serialVersionUID = 6048659185833217159L;

    /**
     * Checks if given {@link AtlasItem} can cross a water body
     *
     * @param crossingItem
     *            {@link AtlasItem} crossing
     * @return whether given {@link AtlasItem} can cross a water body
     */
    private static boolean canCrossWaterBody(final AtlasItem crossingItem)
    {
        // In the following cases, given item can cross a water body

        return CAN_CROSS_WATER_BODY_FILTER.test(crossingItem)
                // It has a tag starting with addr
                || crossingItem.containsKeyStartsWith(Collections.singleton(ADDRESS_PREFIX_KEY))
                // If crossing item is a line and meets the conditions for a boundary
                || crossingItem instanceof Line && isBoundary(crossingItem);
    }

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
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return TypePredicates.IS_AREA.test(object) && TagPredicates.IS_WATER_BODY.test(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // First retrieve the crossing edges and lines
        final Area objectAsArea = (Area) object;
        final Polygon areaAsPolygon = objectAsArea.asPolygon();
        final Atlas atlas = object.getAtlas();
        final Iterable<AtlasItem> crossingLinearItems = new MultiIterable<>(
                atlas.edgesIntersecting(areaAsPolygon), atlas.linesIntersecting(areaAsPolygon));

        // Assume there is no invalid crossing
        boolean hasInvalidCrossings = false;

        // Still let's create a flag in case of an invalid crossing
        final CheckFlag newFlag = new CheckFlag(getTaskIdentifier(object));
        newFlag.addObject(object);
        newFlag.addInstruction(this.getLocalizedInstruction(0, object.getOsmIdentifier()));

        // Go through crossing items and collect invalid crossings
        // NOTE: Due to way sectioning same OSM way could be marked multiple times here. However,
        // MapRoulette will display way-sectioned edges in case there is an invalid crossing.
        // Therefore, if an OSM way crosses a water body multiple times in separate edges, then
        // each edge will be marked explicitly.
        for (final AtlasItem crossingLineItem : crossingLinearItems)
        {
            // Check whether crossing linear item can actually cross
            if (!canCrossWaterBody(crossingLineItem)
                    && !IntersectionUtilities.haveExplicitLocationsForIntersections(areaAsPolygon,
                            (LineItem) crossingLineItem))
            {
                // Update the flag
                newFlag.addObject(crossingLineItem);
                newFlag.addInstruction(
                        this.getLocalizedInstruction(1, crossingLineItem.getOsmIdentifier()));
                // Set indicator to make sure we return invalid crossings
                hasInvalidCrossings = true;
            }
        }

        // If there is an invalid crossing, return the previously created flag
        if (hasInvalidCrossings)
        {
            return Optional.of(newFlag);
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
