package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.atlas.predicates.TagPredicates;
import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.IntersectionUtilities;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.tags.AddressInterpolationTag;
import org.openstreetmap.atlas.tags.AerialWayTag;
import org.openstreetmap.atlas.tags.BarrierTag;
import org.openstreetmap.atlas.tags.BoundaryTag;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.CoveredTag;
import org.openstreetmap.atlas.tags.LandUseTag;
import org.openstreetmap.atlas.tags.RailwayTag;
import org.openstreetmap.atlas.tags.ServiceTag;
import org.openstreetmap.atlas.tags.TunnelTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags line items (edges or lines) that are crossing buildings invalidly.
 * {@code LineCrossingBuildingCheck#canCrossBuilding(AtlasItem)} and
 * {@code IntersectionUtilities#haveExplicitLocationsForIntersections(Polygon, AtlasItem)} is used
 * to decide whether a crossing is valid or not.
 *
 * @author mkalender
 * @author gpogulsky - performance tune-up
 */
public class LineCrossingBuildingCheck extends BaseCheck<Long>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "The building with id {0,number,#} has invalid crossings.",
            "The Line with id {0,number,#} has invalid crossings.");
    private static final long serialVersionUID = 6048659185833217159L;

    /**
     * Checks if given {@link AtlasItem} can cross a building
     *
     * @param crossingItem
     *            {@link AtlasItem} crossing
     * @return whether given {@link AtlasItem} can cross a building
     */
    private static boolean canCrossBuilding(final AtlasItem crossingItem)
    {
        // In the following cases, given item can cross a building
        // It is a boundary
        return crossingItem.getTag(BoundaryTag.KEY).isPresent()
                // Item is referring to a land use
                || crossingItem.getTag(LandUseTag.KEY).isPresent()
                // It goes underground
                || TagPredicates.GOES_UNDERGROUND.test(crossingItem)
                // It is a tunnel
                || TunnelTag.isTunnel(crossingItem)
                // It is a power line
                || TagPredicates.IS_POWER_LINE.test(crossingItem)
                // It is an address interpolation
                || crossingItem.getTag(AddressInterpolationTag.KEY).isPresent()
                // It is a bridge
                || Validators.isNotOfType(crossingItem, BridgeTag.class, BridgeTag.NO)
                // It is an aerialway
                || Validators.hasValuesFor(crossingItem, AerialWayTag.class)
                // It is a subway
                || Validators.isOfType(crossingItem, RailwayTag.class, RailwayTag.SUBWAY)
                // It is a driveway
                || Validators.isOfType(crossingItem, ServiceTag.class, ServiceTag.DRIVEWAY)
                // It is covered
                || Validators.isOfType(crossingItem, CoveredTag.class, CoveredTag.YES);
    }

    public LineCrossingBuildingCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return TypePredicates.IS_AREA.test(object)
                // Only check buildings
                && Validators.isNotOfType(object, BuildingTag.class, BuildingTag.NO)
                // Roofs can have crossings
                && !TagPredicates.IS_ROOF.test(object)
                // Open areas for pedestrians can have crossings too
                && !TagPredicates.IS_HIGHWAY_FOR_PEDESTRIANS.test(object)
                // Toll booths have crossings
                && !Validators.isOfType(object, BarrierTag.class, BarrierTag.TOLL_BOOTH)
                // Buildings can be crossed as an entrance to a compound
                && !Validators.isOfType(object, BuildingTag.class, BuildingTag.ENTRANCE);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Area objectAsArea = (Area) object;
        final Polygon areaAsPolygon = objectAsArea.asPolygon();
        final Atlas atlas = object.getAtlas();

        // First retrieve the crossing edges and lines
        // Go through crossing items and collect invalid crossings
        // NOTE: Due to way sectioning same OSM way could be marked multiple times here. However,
        // MapRoulette will display way-sectioned edges in case there is an invalid crossing.
        // Therefore, if an OSM way crosses a building multiple times in separate edges, then
        // each edge will be marked explicitly.
        final List<Edge> edges = this.findCrossingItems(atlas.edgesIntersecting(areaAsPolygon),
                areaAsPolygon);
        final List<Line> lines = this.findCrossingItems(atlas.linesIntersecting(areaAsPolygon),
                areaAsPolygon);

        if (edges != null || lines != null)
        {
            final CheckFlag flag = new CheckFlag(this.getTaskIdentifier(object));
            flag.addObject(object);
            flag.addInstruction(this.getLocalizedInstruction(0, object.getOsmIdentifier()));

            if (edges != null)
            {
                edges.forEach(edge ->
                {
                    flag.addObject(edge);
                    flag.addInstruction(this.getLocalizedInstruction(1, edge.getOsmIdentifier()));
                });
            }

            if (lines != null)
            {
                lines.forEach(line ->
                {
                    flag.addObject(line);
                    flag.addInstruction(this.getLocalizedInstruction(1, line.getOsmIdentifier()));
                });
            }

            return Optional.of(flag);
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private <T extends LineItem> List<T> findCrossingItems(final Iterable<T> collection,
            final Polygon areaAsPolygon)
    {
        List<T> list = null;

        for (final T item : collection)
        {
            if (canCrossBuilding(item) || IntersectionUtilities
                    .haveExplicitLocationsForIntersections(areaAsPolygon, item))
            {
                continue;
            }

            if (list == null)
            {
                list = new ArrayList<>();
            }

            list.add(item);
        }

        return list;
    }
}
