package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.tags.names.ReferenceTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;

/**
 * Auto generated Check template
 *
 * @author v-garei
 */
public class HighwayMissingNameAndRefTagCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 8198312161814763037L;
    private static final String MISSING_BOTH_NAME_AND_REF_TAG_INSTRUCTIONS = "Way {0, number, #} is missing both name and ref tag. Way must contain either one.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(MISSING_BOTH_NAME_AND_REF_TAG_INSTRUCTIONS);
    private final double minAngleForNonContiguousWays;
    private static final double MIN_ANGLE_NON_CONTIGUOUS_WAYS = 30;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public HighwayMissingNameAndRefTagCheck(final Configuration configuration)
    {
        super(configuration);
        this.minAngleForNonContiguousWays = configurationValue(configuration,
                "min.contiguous.angle", MIN_ANGLE_NON_CONTIGUOUS_WAYS);
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

        // by default we will assume all objects as valid
        return object instanceof Edge && ((Edge) object).isMainEdge()
                && !isFlagged(object.getOsmIdentifier())
                && ((Edge) object).highwayTag().isMoreImportantThanOrEqualTo(HighwayTag.TERTIARY)
                && !HighwayTag.isLinkHighway(((Edge) object).highwayTag())
                && !JunctionTag.isRoundabout(object) && !JunctionTag.isCircular(object);
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
        final Map<String, String> tags = object.getTags();
        if (!this.isConnectorWayToIgnore(((Edge) object).getMainEdge())
                && this.highwayMissingBothNameAndRefTag(tags))
        {
            return Optional.of(
                    createFlag(object, this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private Angle angleDiffBetweenEdges(final Edge edge1, final Edge edge2)
    {
        final Optional<Heading> edge1heading = edge1.asPolyLine().finalHeading();
        final Optional<Heading> edge2heading = edge2.asPolyLine().initialHeading();
        if (edge1heading.isPresent() && edge2heading.isPresent())
        {
            return edge1heading.get().difference(edge2heading.get());
        }
        return Angle.NONE;
    }

    private Set<String> collectEdgeNameTags(final Set<Edge> edges)
    {
        final Set<String> edgeNames = new HashSet<>();
        for (final Edge edge : edges)
        {
            final Map<String, String> tags = edge.getTags();
            if (tags.containsKey(NameTag.KEY))
            {
                edgeNames.add(tags.get(NameTag.KEY));
            }
        }
        return edgeNames;
    }

    private Set<String> collectEdgeRefTags(final Set<Edge> edges)
    {
        final Set<String> edgeRefs = new HashSet<>();
        for (final Edge edge : edges)
        {
            final Map<String, String> tags = edge.getTags();
            if (tags.containsKey(ReferenceTag.KEY))
            {
                edgeRefs.add(tags.get(ReferenceTag.KEY));
            }
        }
        return edgeRefs;
    }

    /**
     * Determines if Edge object is missing both name and ref tag
     * 
     * @param tags
     *            object tags
     * @return boolean if Edge does not contain either name or ref tag.
     */
    private boolean highwayMissingBothNameAndRefTag(final Map<String, String> tags)
    {
        return !tags.containsKey(NameTag.KEY.toLowerCase())
                && !tags.containsKey(ReferenceTag.KEY.toLowerCase());
    }

    private boolean isConnectorWayToIgnore(final Edge edge)
    {
        final Set<Edge> completeWay = new OsmWayWalker(edge).collectEdges();

        if (completeWay.size() == 1)
        {
            final Node startNode = edge.start();
            final Node endNode = edge.end();

            final Set<Edge> startNodeConnectedEdges = startNode.connectedEdges().stream()
                    .filter(someEdge -> someEdge.getOsmIdentifier() != edge.getOsmIdentifier()
                            && this.angleDiffBetweenEdges(someEdge, edge)
                                    .asDegrees() >= this.minAngleForNonContiguousWays)
                    .collect(Collectors.toSet());
            final Set<Edge> endNodeConnectedEdges = endNode.connectedEdges().stream()
                    .filter(someEdge -> someEdge.getOsmIdentifier() != edge.getOsmIdentifier()
                            && this.angleDiffBetweenEdges(edge, someEdge)
                                    .asDegrees() >= this.minAngleForNonContiguousWays)
                    .collect(Collectors.toSet());
            final Set<String> startNodeConnectedEdgeNames = this
                    .collectEdgeNameTags(startNodeConnectedEdges);
            final Set<String> endNodeConnectedEdgeNames = this
                    .collectEdgeNameTags(endNodeConnectedEdges);

            final Set<String> startNodeConnectedEdgeRefs = this
                    .collectEdgeRefTags(startNodeConnectedEdges);
            final Set<String> endNodeConnectedEdgeRefs = this
                    .collectEdgeRefTags(endNodeConnectedEdges);

            return this.startAndEndNodeShareEdgeWithSameTags(startNodeConnectedEdgeNames,
                    endNodeConnectedEdgeNames)
                    || this.startAndEndNodeShareEdgeWithSameTags(startNodeConnectedEdgeRefs,
                            endNodeConnectedEdgeRefs);
        }
        return false;
    }

    private boolean startAndEndNodeShareEdgeWithSameTags(final Set<String> tagSet1,
            final Set<String> tagSet2)
    {
        for (final String tag : tagSet1)
        {
            if (tagSet2.contains(tag))
            {
                return true;
            }
        }
        return false;
    }
}
