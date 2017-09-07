package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.HashSet;
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
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.CoveredTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.TunnelTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags buildings that intersect/touch centerlines of roads. This doesn't address cases where
 * buildings get really close to roads, but don't overlap them.
 *
 * @author mgostintsev
 */
public class BuildingRoadIntersectionCheck extends BaseCheck
{
    private static final long serialVersionUID = 5986017212661374165L;

    private static Predicate<Edge> ignoreTags()
    {
        return edge -> !(Validators.isOfType(edge, CoveredTag.class, CoveredTag.YES)
                || Validators.isOfType(edge, TunnelTag.class, TunnelTag.BUILDING_PASSAGE));
    }

    private static Predicate<Edge> intersectsCoreWayInvalidly(final Area building)
    {
        return edge -> HighwayTag.isCoreWay(edge)
                && edge.asPolyLine().intersects(building.asPolygon())
                && !isValidIntersection(building, edge);
    }

    /**
     * An edge intersecting with a building that doesn't have the proper tags is only valid iff it
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
        if (intersections.size() == 1
                && !building.asPolygon().fullyGeometricallyEncloses(edge.asPolyLine()))
        {
            if (intersections.contains(edgeStart.getLocation())
                    && edge.inEdges().stream().anyMatch(ignoreTags().negate()))
            {
                return true;
            }
            if (intersections.contains(edgeEnd.getLocation())
                    && edge.outEdges().stream().anyMatch(ignoreTags().negate()))
            {
                return true;
            }
        }

        return false;
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
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // We could go about this a couple of ways. Either check all buildings, all roads, or both.
        // Since intersections will be flagged for any feature, it makes sense to loop over the
        // smallest of the three sets - buildings (for most countries). This may change over time.
        return object instanceof Area && BuildingTag.isBuilding(object)
                && !HighwayTag.isHighwayArea(object)
                && !Validators.isOfType(object, BuildingTag.class, BuildingTag.ROOF);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Area building = (Area) object;
        final Iterable<Edge> intersectingEdges = Iterables.filter(building.getAtlas()
                .edgesIntersecting(building.bounds(), intersectsCoreWayInvalidly(building)),
                ignoreTags());
        final CheckFlag flag = new CheckFlag(getTaskIdentifier(building));
        flag.addObject(building);
        this.handleIntersections(intersectingEdges, flag, building);

        if (flag.getPolyLines().size() > 1)
        {
            return Optional.of(flag);
        }

        return Optional.empty();
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
                flag.addObject(edge, "Building (id " + building.getOsmIdentifier()
                        + ") intersects road (id " + edge.getOsmIdentifier() + ")");
                knownIntersections.add(edge);
                if (edge.hasReverseEdge())
                {
                    knownIntersections.add(edge.reversed().get());
                }
            }
        }
    }
}
