package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Verify that one end or the other is a fork to/from a road of the same class, that is not a _link
 *
 * @author cuthbertm
 */
public class RoadLinkCheck extends BaseCheck<Long>
{
    public static final double DISTANCE_MILES_DEFAULT = 1;
    private static final String INVALID_LINK_DISTANCE_INSTRUCTION = "Invalid link, distance, {0}, greater than maximum, {1}.";
    private static final String NO_SAME_CLASSIFICATION_INSTRUCTION = "None of the connected edges contain any edges with the same classification [{0}]";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(INVALID_LINK_DISTANCE_INSTRUCTION, NO_SAME_CLASSIFICATION_INSTRUCTION);
    private static final long serialVersionUID = 6828331285027997648L;
    private final Distance maximumLength;

    public RoadLinkCheck(final Configuration configuration)
    {
        super(configuration);
        this.maximumLength = configurationValue(configuration, "length.maximum.miles",
                DISTANCE_MILES_DEFAULT, Distance::miles);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && ((Edge) object).highwayTag().isLink()
                && ((Edge) object).isMainEdge() && !this.isFlagged(object.getOsmIdentifier());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        this.markAsFlagged(edge.getOsmIdentifier());
        if (edge.length().isGreaterThan(this.maximumLength))
        {
            return Optional.of(this.createFlag(new OsmWayWalker(edge).collectEdges(),
                    this.getLocalizedInstruction(0, edge.length(), this.maximumLength)));
        }
        else if (edge.connectedEdges().stream().filter(Edge::isMainEdge).noneMatch(
                connected -> connected.highwayTag().isOfEqualClassification(edge.highwayTag())))
        {
            final Set<AtlasObject> geometry = new HashSet<>();
            geometry.add(edge);
            geometry.addAll(edge.connectedEdges().stream().filter(Edge::isMainEdge)
                    .collect(Collectors.toSet()));
            final Set<Edge> flagEdges = geometry.stream()
                    .flatMap(obj -> new OsmWayWalker((Edge) obj).collectEdges().stream())
                    .collect(Collectors.toSet());
            final CheckFlag flag = this.createFlag(flagEdges,
                    this.getLocalizedInstruction(1, edge.highwayTag().toString()));
            flag.addPoint(edge.start().getLocation().midPoint(edge.end().getLocation()));
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
