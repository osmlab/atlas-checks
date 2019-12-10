package org.openstreetmap.atlas.checks.validation;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.atlas.predicates.TagPredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 *
 * @author sugandhimaheshwaram
 */
public class RoadNameGapCheck extends BaseCheck
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public RoadNameGapCheck(final Configuration configuration)
    {
        super(configuration);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && Edge.isMasterEdgeIdentifier(object.getIdentifier())
                && TagPredicates.IS_HIGHWAY_NOT_LINK_TYPE.test(object)
                && TagPredicates.VALID_HIGHWAY_TAG.test(object)
                && HighwayTag.isCarNavigableHighway(object);
        // && TagPredicates.NOT_ROUNDABOUT_JUNCTION.test(object);
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        final Set<Edge> inEdges = edge.inEdges().stream().filter(this::validCheckForObject)
                .collect(Collectors.toSet());
        final Set<Edge> outEdges = edge.outEdges().stream().filter(this::validCheckForObject)
                .collect(Collectors.toSet());

        if (inEdges.isEmpty() || outEdges.isEmpty())
        {
            return Optional.empty();
        }

        final Set<String> edgeNames = findInEdgeOutEdgeMatchingName(inEdges, outEdges);

        // Return empty if there is no pair of in edge and out edge with same name.
        if (edgeNames.isEmpty())
        {
            return Optional.empty();
        }

        // Create flag when we have in edge and out edge with same name but intermediate edge
        // doesn't have a name.
        if (!edge.getName().isPresent())
        {
            final String instruction = "Edge name is empty.";
            return Optional.of(createFlag(object, instruction));
        }

        // Create flag when in edge and out edge name tag matches and intermediate edge has
        // different tag name.
        final Optional<String> edgeName = edge.getName();
        if (edgeName.isPresent() && !edgeNames.contains(edgeName.get()))
        {
            final String instruction = "Edge name is different from in edge name and out edge name.";
            return Optional.of(createFlag(object, instruction));
        }
        return Optional.empty();
    }

    private Set<String> findInEdgeOutEdgeMatchingName(final Set<Edge> inEdges,
            final Set<Edge> outEdges)
    {
        final Set<String> edgeNames = new HashSet<>();

        for (final Edge inEdge : inEdges)
        {
            if (!inEdge.getName().isPresent())
            {
                continue;
            }
            for (final Edge outEdge : outEdges)
            {
                if (!outEdge.getName().isPresent())
                {
                    continue;
                }
                final Optional<String> inEdgeName = inEdge.getName();
                final Optional<String> outEdgeName = outEdge.getName();
                if (inEdgeName.isPresent() && outEdgeName.isPresent()
                        && inEdgeName.get().equals(outEdgeName.get()))
                {
                    edgeNames.add(outEdgeName.get());
                }
            }
        }
        return edgeNames;
    }
}
