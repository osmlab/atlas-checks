package org.openstreetmap.atlas.checks.validation.points;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.base.Check;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * {@link Check} that flags {@link Node}s with too many connections. Connection count can be
 * configured, but otherwise it is 10 by default.
 *
 * @author matthieun
 * @author cuthbertm
 * @author mkalender
 */
public class NodeValenceCheck extends BaseCheck<Long>
{
    // Maximum connection config
    public static final long MAXIMUM_CONNECTIONS_DEFAULT = 10;
    // Instruction format
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Node {0,number,#} has too many connections ({1} connected edges). Ideally a node shouldn't be connected to more than {2} edges.");
    private static final long serialVersionUID = -6518944438651833609L;
    private final long maximumConnections;

    public NodeValenceCheck(final Configuration configuration)
    {
        super(configuration);
        this.maximumConnections = configurationValue(configuration, "connections.maximum",
                MAXIMUM_CONNECTIONS_DEFAULT);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Node;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Node node = (Node) object;

        // Count car navigable connections
        final List<Edge> connectedCarNavigableEdges = node.connectedEdges().stream()
                .filter(edge -> edge.isMainEdge() && HighwayTag.isCarNavigableHighway(edge))
                .collect(Collectors.toList());
        final int valence = connectedCarNavigableEdges.size();
        if (valence > this.maximumConnections)
        {
            final CheckFlag flag = new CheckFlag(String.valueOf(object.getIdentifier()));
            connectedCarNavigableEdges.forEach(flag::addObject);
            flag.addObject(object, this.getLocalizedInstruction(0, node.getOsmIdentifier(), valence,
                    this.maximumConnections));
            return Optional.of(flag);
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
