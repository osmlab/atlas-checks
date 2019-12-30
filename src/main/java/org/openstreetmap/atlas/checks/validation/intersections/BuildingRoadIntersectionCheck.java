package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.AccessTag;
import org.openstreetmap.atlas.tags.AmenityTag;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.BarrierTag;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.CoveredTag;
import org.openstreetmap.atlas.tags.EntranceTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.ServiceTag;
import org.openstreetmap.atlas.tags.TunnelTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags buildings that intersect/touch centerlines of roads. This doesn't address cases where
 * buildings get really close to roads, but don't overlap them. The configurable value
 * "car.navigable" can be set to true or false, depending on which the validity of the intersecting
 * highways will be checked. The default value of "car.navigable" is set to true. If set to true,
 * the intersecting highways will be checked for tags in the enum set "CAR_NAVIGABLE_HIGHWAYS", else
 * checked for tags in "CORE_WAYS" enum set in the {@link HighwayTag} class.
 *
 * @author mgostintsev
 * @author sayas01
 */
public class BuildingRoadIntersectionCheck extends BaseCheck<Long>
{
    private static final String BUILDING_ROAD_INTERSECTION_INSTRUCTION = "Building (id-{0,number,#}) intersects road (id-{1,number,#})";
    private static final String SERVICE_ROAD_INTERSECTION_INSTRUCTION = "Building (id-{0,number,#}) intersects road (id-{1,number,#}), which is a SERVICE road. Please verify whether the intersection is valid or not.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(BUILDING_ROAD_INTERSECTION_INSTRUCTION, SERVICE_ROAD_INTERSECTION_INSTRUCTION);
    private static final String INDOOR_KEY = "indoor";
    private static final String YES_VALUE = "yes";
    private static final Predicate<Edge> HIGHWAY_SERVICE_TAG = edge -> Validators.isOfType(edge,
            HighwayTag.class, HighwayTag.SERVICE);
    private static final long serialVersionUID = 5986017212661374165L;
    private final Boolean carNavigableEdgesOnly;

    private static Predicate<Edge> ignoreTags()
    {
        return edge -> !(Validators.isOfType(edge, CoveredTag.class, CoveredTag.YES)
                || Validators.isOfType(edge, TunnelTag.class, TunnelTag.BUILDING_PASSAGE,
                        TunnelTag.YES)
                || Validators.isOfType(edge, AreaTag.class, AreaTag.YES)
                || YES_VALUE.equals(edge.tag(INDOOR_KEY))
                || HIGHWAY_SERVICE_TAG.test(edge)
                        && Validators.isOfType(edge, ServiceTag.class, ServiceTag.DRIVEWAY)
                || edge.connectedNodes().stream().anyMatch(node -> Validators.isOfType(node,
                        EntranceTag.class, EntranceTag.YES)
                        || Validators.isOfType(node, AmenityTag.class, AmenityTag.PARKING_ENTRANCE)
                        // Ignore edges with nodes containing Barrier tags
                        || Validators.hasValuesFor(node, BarrierTag.class)));
    }

    /**
     * An edge intersecting with a building that doesn't have the proper tags is only valid if it
     * intersects at one single node and that node is shared with an edge that has the proper tags
     * and it is not enclosed in the building
     *
     * @param building
     *            the building being processed
     * @param edge
     *            the edge being examined
     * @return true if the intersection is valid, false otherwise
     */
    private static boolean isValidIntersection(final Area building, final Edge edge)
    {
        final Node edgeStart = edge.start();
        final Node edgeEnd = edge.end();
        final Set<Location> intersections = building.asPolygon().intersections(edge.asPolyLine());
        return intersections.size() == 1
                && !building.asPolygon().fullyGeometricallyEncloses(edge.asPolyLine())
                && (intersections.contains(edgeStart.getLocation())
                        || intersections.contains(edgeEnd.getLocation()));
    }

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public BuildingRoadIntersectionCheck(final Configuration configuration)
    {
        super(configuration);
        this.carNavigableEdgesOnly = this.configurationValue(configuration, "car.navigable", true);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // We could go about this a couple of ways. Either check all buildings, all roads, or both.
        // Since intersections will be flagged for any feature, it makes sense to loop over the
        // smallest of the three sets - buildings (for most countries). This may change over time.
        return object instanceof Area && BuildingTag.isBuilding(object)
                && !HighwayTag.isHighwayArea(object)
                && !Validators.isOfType(object, AmenityTag.class, AmenityTag.PARKING)
                && !Validators.isOfType(object, BuildingTag.class, BuildingTag.ROOF)
                // Ignore buildings that have points withing it with Ameniity=Fuel
                && !object.getAtlas().pointsWithin(((Area) object).asPolygon(),
                        point -> Validators.isOfType(point, AmenityTag.class, AmenityTag.FUEL))
                        .iterator().hasNext();
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Area building = (Area) object;
        final Iterable<Edge> intersectingEdges = Iterables.filter(building.getAtlas()
                .edgesIntersecting(building.bounds(), this.intersectsCoreWayInvalidly(building)),
                ignoreTags());
        final CheckFlag flag = new CheckFlag(getTaskIdentifier(building));
        flag.addObject(building);
        this.handleIntersections(intersectingEdges, flag, building);
        return flag.getPolyLines().size() > 1
                ? Optional.of(flag.setObjectIdentifiersAsFlagIdentifier())
                : Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Loops through all intersecting {@link Edge}s, and keeps track of reverse and already seen
     * intersections
     *
     * @param intersectingEdges
     *            all intersecting {@link Edge}s for given building
     * @param flag
     *            the {@link CheckFlag} we're updating
     * @param building
     *            the building being processed
     */
    private void handleIntersections(final Iterable<Edge> intersectingEdges, final CheckFlag flag,
            final Area building)
    {
        final Set<Edge> knownIntersections = new HashSet<>();
        for (final Edge edge : intersectingEdges)
        {
            if (!knownIntersections.contains(edge))
            {
                final int instructionIndex = HIGHWAY_SERVICE_TAG.test(edge) ? 1 : 0;
                flag.addObject(edge, this.getLocalizedInstruction(instructionIndex,
                        building.getOsmIdentifier(), edge.getOsmIdentifier()));
                knownIntersections.add(edge);
                final Optional<Edge> reversedEdge = edge.reversed();
                if (reversedEdge.isPresent())
                {
                    knownIntersections.add(reversedEdge.get());
                }
            }
        }
    }

    private Predicate<Edge> intersectsCoreWayInvalidly(final Area building)
    {
        // An invalid intersection is determined by checking that its highway tag is car navigable
        // or core way based on the configuration value
        return edge -> (this.carNavigableEdgesOnly ? HighwayTag.isCarNavigableHighway(edge)
                : HighwayTag.isCoreWay(edge))
                // And if the edge intersects the building polygon
                && edge.asPolyLine().intersects(building.asPolygon())
                // And ignore intersections where edge has highway=service and building has
                // Amenity=fuel
                && !(HIGHWAY_SERVICE_TAG.test(edge)
                        && Validators.isOfType(building, AmenityTag.class, AmenityTag.FUEL))
                // And if the layers have the same layer value
                && LayerTag.getTaggedValue(edge).orElse(0L)
                        .equals(LayerTag.getTaggedValue(building).orElse(0L))
                // And if the building/edge intersection is not valid
                && !isValidIntersection(building, edge)
                // And if the edge has no Access = Private tag
                && !Validators.isOfType(edge, AccessTag.class, AccessTag.PRIVATE);
    }
}
