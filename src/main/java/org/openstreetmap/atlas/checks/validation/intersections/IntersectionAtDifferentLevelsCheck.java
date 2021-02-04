package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.checks.utility.CommonMethods;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.RailwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags Nodes that connect Ways at different level.
 *
 * @author Vladimir Lemberg
 */
public class IntersectionAtDifferentLevelsCheck extends BaseCheck<Long>
{
    // Instructions
    private static final String INSTRUCTION_FORMAT = "The Node id {0,number,#} connecting two Ways with different layers.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(INSTRUCTION_FORMAT);
    private static final long serialVersionUID = 5171171744111206429L;

    /**
     * Default constructor
     *
     * @param configuration
     *            {@link Configuration} required to construct any Check
     */
    public IntersectionAtDifferentLevelsCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return !this.isFlagged(object.getOsmIdentifier()) && object instanceof Node
                && !HighwayTag.isPedestrianCrossing(object)
                && !RailwayTag.isRailwayCrossing(object);
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Node node = (Node) object;
        final List<Edge> connectedEdges = node.connectedEdges().stream().filter(Edge::isMainEdge)
                .filter(obj -> HighwayTag.isCarNavigableHighway(obj)
                        || HighwayTag.isPedestrianNavigableHighway(obj))
                .filter(obj -> obj.getTag(AreaTag.KEY).isEmpty()).collect(Collectors.toList());

        if (connectedEdges.stream()
                .anyMatch(edge1 -> connectedEdges.stream()
                        .anyMatch(edge2 -> edge1.getOsmIdentifier() != edge2.getOsmIdentifier()
                                && !LayerTag.areOnSameLayer(edge1, edge2)
                                // must be an intermediate node for both ways.
                                && (this.isInterLocationNode(edge1, node)
                                        && this.isInterLocationNode(edge2, node)))))
        {
            this.markAsFlagged(object.getOsmIdentifier());
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * This function returns set of intersections locations for given params.
     *
     * @param edge
     *            Atlas object
     * @param node
     *            crossing edge
     * @return set of intersection locations.
     */
    private boolean isInterLocationNode(final Edge edge, final Node node)
    {
        final List<Location> interLocations = StreamSupport.stream(
                CommonMethods.buildOriginalOsmWayGeometry(edge).innerLocations().spliterator(),
                false).collect(Collectors.toList());

        return interLocations.contains(node.getLocation());
    }
}
