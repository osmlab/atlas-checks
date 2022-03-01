package org.openstreetmap.atlas.checks.validation.linear.edges;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.complex.roundabout.ComplexRoundabout;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.ServiceTag;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author elaineleong
 * @author brianjor
 */
public class RoundaboutHighwayTagCheck extends BaseCheck<Long>
{

    private static final long serialVersionUID = 1L;
    private static final String TAG_FILTER_IGNORE_DEFAULT = "junction->roundabout|highway->*_link|service->driveway";
    private static final String ROUNDABOUT_HIGHWAY_LEVEL_INSTRUCTION = "The way, id:{0,number,#}, should have the highway tag that matches the highest classification of road that passes through. Current: {1}. Expected: {2}.";
    private static final List<String> FALLBACK_INSTRUCTION = List.of(ROUNDABOUT_HIGHWAY_LEVEL_INSTRUCTION);
    private final TaggableFilter tagFilterIgnore;

    public RoundaboutHighwayTagCheck(final Configuration configuration)
    {
        super(configuration);
        this.tagFilterIgnore = this.configurationValue(configuration, "ignore.tags.filter",
                TAG_FILTER_IGNORE_DEFAULT, TaggableFilter::forDefinition);
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
        return object instanceof Edge && JunctionTag.isRoundabout(object)
                && HighwayTag.isCarNavigableHighway(object) && ((Edge) object).isMainEdge()
                && !this.isFlagged(object.getIdentifier());
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *          the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;

        final HighwayTag currentHighwayTag = edge.highwayTag();
        final Set<Edge> roundaboutEdges = new ComplexRoundabout(edge).getRoundaboutEdgeSet();
        roundaboutEdges.stream().map(AtlasObject::getIdentifier).forEach(this::markAsFlagged);

        final HighwayTag highestHighwayTag = this.getConnectEdgesMaxHighwayTagClassification(roundaboutEdges);

        if (highestHighwayTag.isMoreImportantThan(currentHighwayTag))
        {
            final CheckFlag flag = this.createFlag(roundaboutEdges, this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                    currentHighwayTag, highestHighwayTag));
            return Optional.of(flag);
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTION;
    }

    /**
     * Get edges that are external to the roundabout.
     * @param roundaboutNodes
     *          {@link Node}s that comprise the roundabout
     * @return {@link Edge}s that are connected to but outside the roundabout
     */
    private Set<Edge> externalEdges(Set<Node> roundaboutNodes)
    {
        return roundaboutNodes.stream()
                .flatMap(node -> node.connectedEdges().stream()
                        .filter(Edge::isMainEdge)
                        .filter(Predicate.not(this.tagFilterIgnore))
                ).collect(Collectors.toSet());
    }

    /**
     * Finds the highest highway tag classification of all edges connected to the roundabout.
     * @param roundaboutEdges
     *          set of {@link Edge} that comprise the roundabout
     * @return the {@link HighwayTag} with the highest classification
     */
    private HighwayTag getConnectEdgesMaxHighwayTagClassification(final Set<Edge> roundaboutEdges)
    {
        final Set<Node> roundaboutNodes = roundaboutEdges.stream().map(Edge::start)
                .collect(Collectors.toSet());

        final Set<Edge> connectedHighwayEdges = this.externalEdges(roundaboutNodes);

        final List<HighwayTag> validHighwayTags = this.highwayTagsOnMultipleEdges(connectedHighwayEdges);
        return this.maxHighwayTag(validHighwayTags);
    }

    /**
     * Get highway tags that appear on multiple edges.
     * @param connectedHighwayEdges
     *          list of {@link Edge} to check.
     * @return list of {@link HighwayTag} that are on multiple edges
     */
    private List<HighwayTag> highwayTagsOnMultipleEdges(final Set<Edge> connectedHighwayEdges)
    {
        final Map<HighwayTag, Integer> highwayTagCount = new HashMap<>();
        connectedHighwayEdges.stream()
                .map(Edge::highwayTag)
                .forEach(tag -> highwayTagCount.put(tag, highwayTagCount.getOrDefault(tag, 0) + 1));

        return highwayTagCount.entrySet()
                .stream()
                .filter(entrySet -> (entrySet.getValue() / 2) > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Finds the highest classification of highway tag from a list of highway tags.
     * @param highwayTags
     *          List of {@link HighwayTag}s to compare
     * @return
     *          {@link HighwayTag} of highest classification
     */
    private HighwayTag maxHighwayTag(final List<HighwayTag> highwayTags)
    {
        return highwayTags.stream()
                .reduce(HighwayTag.NO, (prev, cur) -> cur.isMoreImportantThan(prev) ? cur : prev);
    }
}