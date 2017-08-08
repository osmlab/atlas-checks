package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.CoveredTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import com.google.common.collect.Iterables;

/**
 * Flags buildings that intersect/touch centerlines of roads. This doesn't address cases where
 * buildings get really close to roads, but don't overlap them.
 *
 * @author mgostintsev
 */
public class BuildingRoadIntersectionCheck extends BaseCheck
{
    private static final long serialVersionUID = 5986017212661374165L;

    private static Predicate<Edge> intersectsCoreWay(final Area building)
    {
        return edge -> HighwayTag.isCoreWay(edge)
                && edge.asPolyLine().intersects(building.asPolygon());
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
                && !HighwayTag.isHighwayArea(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Area building = (Area) object;
        final Iterable<Edge> intersectingEdges = Iterables.filter(
                object.getAtlas().edgesIntersecting(object.bounds(), intersectsCoreWay(building)),
                edge -> !Validators.isOfType(edge, CoveredTag.class, CoveredTag.YES));
        final CheckFlag flag = new CheckFlag(getTaskIdentifier(object));
        flag.addObject(object);
        handleIntersections(intersectingEdges, flag, object);

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
            final AtlasObject building)
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
