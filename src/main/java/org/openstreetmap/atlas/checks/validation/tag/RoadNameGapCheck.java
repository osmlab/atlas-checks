package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.direction.EdgeDirectionComparator;

/**
 * This check identifies Edges with no name Tag that are between two other Edges with the same name
 * Tag, OR the Edge has a name Tag but does not equal the name Tag of the Edges that it is between.
 *
 * @author sugandhimaheshwaram
 */
public class RoadNameGapCheck extends BaseCheck<Long>
{
    /**
     * This checks if the connected edges have same direction or not.
     */
    static class EdgePredicate
    {
        private final Edge edge;
        private final EdgeDirectionComparator edgeDirectionComparator = new EdgeDirectionComparator();

        EdgePredicate(final Edge edge)
        {
            this.edge = edge;
        }

        boolean isSameHeading(final AtlasObject object)
        {
            final Edge otherEdge = (Edge) object;
            return object != null
                    && this.edgeDirectionComparator.isSameDirection(this.edge, otherEdge, false);
        }
    }

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 7104778218412127847L;
    private static final List<String> VALID_HIGHWAY_TAG_DEFAULT = Arrays.asList("primary",
            "secondary", "tertiary", "trunk", "motorway");
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList("Edge name is empty.",
            "Edge name {1} is different from in edge name and out edge name.");

    private final List<String> validHighwayTag;

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
        this.validHighwayTag = configurationValue(configuration, "valid.highway.tag",
                VALID_HIGHWAY_TAG_DEFAULT).stream().map(String::toLowerCase)
                        .collect(Collectors.toList());
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
        return object instanceof Edge && Edge.isMainEdgeIdentifier(object.getIdentifier())
                && !JunctionTag.isRoundabout(object)
                && Validators.hasValuesFor(object, HighwayTag.class)
                && this.validHighwayTag.contains(object.tag(HighwayTag.KEY));
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
        final EdgePredicate edgePredicate = new EdgePredicate(edge);
        final Set<Edge> inEdges = edge.inEdges().stream()
                .filter(obj -> validCheckForObject(obj) && edgePredicate.isSameHeading(obj))
                .collect(Collectors.toSet());
        final Set<Edge> outEdges = edge.outEdges().stream()
                .filter(obj -> validCheckForObject(obj) && edgePredicate.isSameHeading(obj))
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
            final Set<Edge> connectedEdges = edge.connectedEdges().stream()
                    .filter(this::validCheckForObject).collect(Collectors.toSet());
            return findMatchingEdgeNameWithConnectedEdges(connectedEdges, edgeName.get())
                    ? Optional.empty()
                    : Optional.of(createFlag(object, this.getLocalizedInstruction(1,
                            edge.getOsmIdentifier(), edgeName.get())));

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
        return connectedEdges.stream().anyMatch(connectedEdge -> connectedEdge.getName().isPresent()
                && connectedEdge.getName().get().equals(edgeName));
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
        return inEdges.stream().filter(inEdge -> outEdges.stream()
                .anyMatch(outEdge -> outEdge.getName().isPresent() && inEdge.getName().isPresent()
                        && outEdge.getName().get().equals(inEdge.getName().get())
                        && inEdge.getOsmIdentifier() != outEdge.getOsmIdentifier()))
                .map(edge -> edge.getName().get()).collect(Collectors.toSet());
    }
}
