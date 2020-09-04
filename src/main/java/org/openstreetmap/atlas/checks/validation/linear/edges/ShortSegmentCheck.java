package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.BarrierTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Flags short segments/edges (length is less than {@link ShortSegmentCheck#maximumLength}) that
 * have a node with less than or equal to {@link ShortSegmentCheck#minimumValence} connections
 *
 * @author mkalender
 * @author bentley-breithaupt
 */
public class ShortSegmentCheck extends BaseCheck<Long>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "This edge {0,number,#} is short (length < {1} m) and it is connected to node {2,number,#} that has less than {3} connections.");
    // Length for an edge not to be defined as short
    private static final double MAXIMUM_LENGTH_DEFAULT = Distance.ONE_METER.asMeters();
    // Minimum valence for a node to not to flag
    private static final long MINIMUM_VALENCE_DEFAULT = 3;
    // Minimum highway priority for an edge
    private static final String MINIMUM_HIGHWAY_PRIORITY_DEFAULT = HighwayTag.SERVICE.toString();
    private static final long serialVersionUID = 6916628316458828018L;

    private final Distance maximumLength;
    private final long minimumValence;
    private final HighwayTag minimumHighwayPriority;

    /**
     * Checks if any of an {@link Edge}'s {@link Node}s are below a minimum valence. The nodes are
     * ignored if they are at the start/end of a closed way.
     *
     * @param edge
     *            {@link Edge} to check the {@link Node}s of
     * @param valence
     *            the minimum number of main edges for the node to have an acceptable valence
     * @return an {@link Optional} containing the first low valence node found
     */
    private static Optional<Node> getConnectedNodesWithValenceLessThan(final Edge edge,
            final long valence)
    {
        // go through each connected node of given edge
        for (final Node node : edge.connectedNodes())
        {
            // Get the connected main edges
            final List<Edge> mainEdges = node.connectedEdges().stream().filter(Edge::isMainEdge)
                    .collect(Collectors.toList());
            // check if any of them has less than given valence value
            if (mainEdges.size() < valence
                    // Check if the low valence is the result of a closed way being way
                    // sectioned
                    // A short segment at the start or end of a closed way will have a node with
                    // 2 connected main edges, and they will have the same OSM id
                    && !(mainEdges.size() == 2
                            //
                            && mainEdges.stream()
                                    .filter(connectedEdge -> connectedEdge
                                            .getOsmIdentifier() == edge.getOsmIdentifier())
                                    .count() > 1))
            {
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    public ShortSegmentCheck(final Configuration configuration)
    {
        super(configuration);
        this.maximumLength = configurationValue(configuration, "edge.length.maximum.meters",
                MAXIMUM_LENGTH_DEFAULT, Distance::meters);
        this.minimumValence = configurationValue(configuration, "node.valence.minimum",
                MINIMUM_VALENCE_DEFAULT);
        this.minimumHighwayPriority = Enum.valueOf(HighwayTag.class,
                configurationValue(configuration, "highway.priority.minimum",
                        MINIMUM_HIGHWAY_PRIORITY_DEFAULT).toUpperCase());
    }

    /**
     * Validate if given {@link AtlasObject} is actually an {@link Edge}
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge
                && ((Edge) object).highwayTag().isMoreImportantThanOrEqualTo(
                        this.minimumHighwayPriority)
                && ((Edge) object).isMainEdge()
                && ((Edge) object).length().isLessThan(this.maximumLength);
    }

    /**
     * Flag an {@link Edge} if it has a length less than {@link ShortSegmentCheck#maximumLength} and
     * is connected to end nodes whose valence is less than {@link ShortSegmentCheck#minimumValence}
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        final Optional<Node> lowValenceNodes = getConnectedNodesWithValenceLessThan(edge,
                this.minimumValence);
        if (lowValenceNodes.isPresent() && !isGateLike((Edge) object))
        {
            return Optional.of(createFlag(object,
                    this.getLocalizedInstruction(0, object.getIdentifier(),
                            this.maximumLength.asMeters(), lowValenceNodes.get().getIdentifier(),
                            this.minimumValence),
                    Collections.singletonList(lowValenceNodes.get().getLocation())));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks if an {@link Edge} has one node with a barrier tag and another with > 1 valence. This
     * indicates a gate, or similar barrier, has caused a short segment due to way sectioning.
     *
     * @param object
     *            {@link Edge} to check the {@link Node}s of
     * @return true if one node has a barrier tag and the other has > 1 valence
     */
    private boolean isGateLike(final Edge object)
    {
        return (object.start().connectedEdges().stream().filter(Edge::isMainEdge).count() > 1
                && Validators.hasValuesFor(object.end(), BarrierTag.class))
                || (object.end().connectedEdges().stream().filter(Edge::isMainEdge).count() > 1
                        && Validators.hasValuesFor(object.start(), BarrierTag.class));
    }
}
