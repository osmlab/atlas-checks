package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This flags {@link Edge}s that are tagged with highway=motorway and are not connected to any other
 * {@link Edge}s with the same highway tag.
 *
 * @author bbreithaupt
 */
public class SingleSegmentMotorwayCheck extends BaseCheck<Long>
{

    private static final long serialVersionUID = 5874631233752066384L;
    private static final String SINGLE_SEGMENT_INSTRUCTION = "This way, id:{0,number,#}, is a motorway that is disconnected from any other motorways.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(SINGLE_SEGMENT_INSTRUCTION);

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public SingleSegmentMotorwayCheck(final Configuration configuration)
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
        return object instanceof Edge && ((Edge) object).isMainEdge()
                && this.isMotorwayNotRoundabout((Edge) object)
                && !this.isFlagged(object.getOsmIdentifier()) && ((Edge) object).connectedNodes()
                        .stream().noneMatch(SyntheticBoundaryNodeTag::isBoundaryNode);
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
        if (edge.connectedEdges().stream().filter(Edge::isMainEdge)
                .noneMatch(this::isMotorwayNotRoundabout))
        {
            this.markAsFlagged(edge.getOsmIdentifier());
            final CheckFlag flag = this.createFlag(new OsmWayWalker(edge).collectEdges(),
                    this.getLocalizedInstruction(0, edge.getOsmIdentifier()));
            final Optional<String> fixSuggestion = this.getFixSuggestion(edge);
            return fixSuggestion.map(suggestion -> flag.addFixSuggestion(FeatureChange.add(
                    (AtlasEntity) ((CompleteEntity) CompleteEntity
                            .shallowFrom((AtlasEntity) object)).withTags(object.getTags())
                                    .withReplacedTag(HighwayTag.KEY, HighwayTag.KEY, suggestion),
                    object.getAtlas()))).or(() -> Optional.of(flag));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private Optional<String> getFixSuggestion(final Edge edge)
    {
        final Set<String> options = edge.connectedEdges().stream().filter(Edge::isMainEdge)
                .map(HighwayTag::highwayTag).flatMap(Optional::stream).map(Enum::name)
                .map(String::toLowerCase).collect(Collectors.toSet());
        return options.size() == 1 ? Optional.of(options.iterator().next()) : Optional.empty();
    }

    /**
     * Checks if an {@link Edge} is tagged with highway=motorway. Roundabouts are excluded, as they
     * act more like motorway_links than motorway segments in most cases found.
     *
     * @param edge
     *            {@link Edge}
     * @return {@link boolean}
     */
    private boolean isMotorwayNotRoundabout(final Edge edge)
    {
        return Validators.isOfType(edge, HighwayTag.class, HighwayTag.MOTORWAY)
                && !JunctionTag.isRoundabout(edge);
    }
}
