package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check identifies Edges with no name Tag that are between two other Edges with the same name
 * Tag, OR the Edge has a name Tag but does not equal the name Tag of the Edges that it is between.
 *
 * @author sugandhimaheshwaram
 */
public class RoadNameGapCheck extends BaseCheck
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 7104778218412127847L;
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList("Edge name is empty.",
            "Edge name {1} is different from in edge name and out edge name.");

    /**
     * Tests if the {@link AtlasObject} has a highway tag that do contain TERTIARY, SECONDARY,
     * PRIMARY, TRUNK, or MOTORWAY
     */
    private static final Predicate<AtlasObject> VALID_HIGHWAY_TAG = object -> Validators
            .hasValuesFor(object, HighwayTag.class)
            && Validators.isOfType(object, HighwayTag.class, HighwayTag.TERTIARY,
                    HighwayTag.PRIMARY, HighwayTag.SECONDARY, HighwayTag.MOTORWAY,
                    HighwayTag.TRUNK);

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
                && !HighwayTag.isLinkHighway(object) && VALID_HIGHWAY_TAG.test(object)
                && !JunctionTag.isRoundabout(object);
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

        final Set<String> matchingInAndOutEdgeNames = getMatchingInAndOutEdgeNames(inEdges,
                outEdges);

        if (matchingInAndOutEdgeNames.isEmpty())
        {
            // There is no pair of inedge and out edge with same name.
            return Optional.empty();
        }

        // Create flag when we have in edge and out edge with same name but intermediate edge
        // doesn't have a name.
        if (!edge.getName().isPresent())
        {
            return Optional.of(
                    createFlag(object, this.getLocalizedInstruction(0, edge.getOsmIdentifier())));
        }
        final Optional<String> edgeName = edge.getName();

        if (edgeName.isPresent() && !matchingInAndOutEdgeNames.contains(edgeName.get()))
        {
            // Case 1: Edge name: Tai. Incoming edge names: Tai , Shai. Outgoing edge name: Shai.
            // Case 2: Edge name: Tai. Incoming edge names: Shai. Outgoing edge name: Tai, Shai.
            // Case 3: Edge name: Tai. Incoming edges names: Tai, Shai. Outgoing edge names: Shai,
            // Pendler.
            if (findMatchingEdgeNameWithConnectedEdges(edge.connectedEdges(), edgeName.get()))
            {
                return Optional.empty();
            }
            return Optional.of(createFlag(object,
                    this.getLocalizedInstruction(1, edge.getOsmIdentifier(), edgeName.get())));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Compare edge name with individual in and out edges names to see if there is a match. If there
     * is a match no flag or else flag the edge.
     *
     * @param connectedEdges
     *            connected edges for a given edge
     * @param edgeName
     *            Edge name
     * @return True if there is matching edge name with connected edge or else false.
     */
    private boolean findMatchingEdgeNameWithConnectedEdges(final Set<Edge> connectedEdges,
            final String edgeName)
    {
        for (final Edge connectedEdge : connectedEdges)
        {
            final Optional<String> connectedEdgeName = connectedEdge.getName();
            if (connectedEdgeName.isPresent() && connectedEdgeName.get().equals(edgeName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Find matching in and out edge names by comparing every name of the edge.
     *
     * @param inEdges
     *            incoming edges
     * @param outEdges
     *            outgoing edges
     * @return Set of matching names for both incoming and outgoing edges.
     */
    private Set<String> getMatchingInAndOutEdgeNames(final Set<Edge> inEdges,
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
                        && inEdgeName.get().equals(outEdgeName.get())
                        && inEdge.getOsmIdentifier() != outEdge.getOsmIdentifier())
                {
                    edgeNames.add(outEdgeName.get());
                }
            }
        }
        return edgeNames;
    }
}
