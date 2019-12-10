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
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH,
        // Distance::meters);
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
        final Set<Edge> inEdges = edge.inEdges().stream()
                .filter(edge1 -> validCheckForObject(edge1)).collect(Collectors.toSet());
        final Set<Edge> outEdges = edge.outEdges().stream()
                .filter(edge1 -> validCheckForObject(edge1)).collect(Collectors.toSet());

        if (inEdges == null || inEdges.size() < 1)
        {
            return Optional.empty();
        }

        if (outEdges == null || outEdges.size() < 1)
        {
            return Optional.empty();
        }
        final Set<String> edgeNames = new HashSet();

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
                if (inEdge.getName().isPresent() && inEdge.getName().get().equals(outEdge.getName().get()))
                {
                    edgeNames.add(outEdge.getName().get());
                }
            }
        }

        if (edgeNames.isEmpty())
        {
            // There is no pair of inedge and out edge with same name.
            return Optional.empty();
        }

        if (!edge.getName().isPresent())
        {
            // create flag. We have inedge and outedge with same name but this edge doesnt have a
            // name.
            final String instruction = "Edge name is empty.";
            return Optional.of(createFlag(object, instruction));
        }
        if (!edgeNames.contains(edge.getName().get()))
        {
            final String instruction = "Edge name is different from in edge name and out edge name.";
            return Optional.of(createFlag(object, instruction));
        }
        return Optional.empty();
    }
}
