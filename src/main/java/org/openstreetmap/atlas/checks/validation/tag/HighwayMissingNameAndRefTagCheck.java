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
 * This check flags ways that have neither a name or ref tag but should have at least one.
 *
 * @author v-garei - msft
 */
public class HighwayMissingNameAndRefTagCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 8198312161814763037L;
    private static final String MISSING_BOTH_NAME_AND_REF_TAG_INSTRUCTIONS = "Way {0, number, #} is missing both name and ref tag. Way must contain either one.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(MISSING_BOTH_NAME_AND_REF_TAG_INSTRUCTIONS);
    private final double minAngleForNonContiguousWays;
    private final HighwayTag minHighwayTag;
    private static final String MIN_HIGHWAY_TAG_DEFAULT = "tertiary";
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
        this.minHighwayTag = Enum.valueOf(HighwayTag.class,
                this.configurationValue(configuration, "min.highway.type", MIN_HIGHWAY_TAG_DEFAULT)
                        .toUpperCase());
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
                && !isFlagged(object.getOsmIdentifier())
                && ((Edge) object).highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayTag)
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
        markAsFlagged(object.getOsmIdentifier());
        final Map<String, String> tags = object.getTags();
        if (this.highwayMissingBothNameAndRefTag(tags)
                && !this.isConnectorWayToIgnore(((Edge) object).getMainEdge()))
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

    /**
     * Function calculate angle between Edges
     * 
     * @param edge1
     *            an edge
     * @param edge2
     *            another edge
     * @return Angle between edges
     */
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

    /**
     * Function to collect osm name tags
     * 
     * @param edges
     *            a set of edges (connected edges from edge in question)
     * @return set of strings representing name tags
     */
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

    /**
     * Function to collect osm reference tags
     * 
     * @param edges
     *            a set of edges (connected edges from edge in question)
     * @return Set of strings representing reference tags
     */
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
     * Function to determine if there are common tags between edges (helper function to ignore small
     * connector road)
     *
     * @param tagSet1
     *            start/end node connected edges set of tags
     * @param tagSet2
     *            start/end node connected edges set of tags
     * @return boolean if there are common tags between edges.
     */
    private boolean edgesShareTags(final Set<String> tagSet1, final Set<String> tagSet2)
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

    /**
     * Function to determine if Edge object is missing both name and ref tag
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

    /**
     * function to determine which inEdge's name/ref tags to add to respective sets if applicable
     * based on difference in angle between edge in question and inEdge.
     *
     * @param edge
     *            edge in question
     * @param inEdges
     *            edge in question's inEdges
     * @param inEdgeNames
     *            Set of inEdges' names
     * @param inEdgeRefs
     *            Set of inEdges' refs
     */
    private void inEdgeLogic(final Edge edge, final Set<Edge> inEdges,
            final Set<String> inEdgeNames, final Set<String> inEdgeRefs)
    {
        // inEdge logic to add name/ref tag to name/ref sets respectively
        for (final Edge inEdge : inEdges)
        {
            final Map<String, String> inEdgeTags = inEdge.getTags();

            // angle between inEdge and edge in question < 30 (configurable)
            // inEdge contains either name or ref tag
            if (this.angleDiffBetweenEdges(inEdge, edge)
                    .asDegrees() <= this.minAngleForNonContiguousWays
                    && (inEdgeTags.containsKey(NameTag.KEY)
                            || inEdgeTags.containsKey(ReferenceTag.KEY)))
            {
                if (inEdgeTags.containsKey(NameTag.KEY))
                {
                    inEdgeNames.add(inEdgeTags.get(NameTag.KEY));
                }

                if (inEdgeTags.containsKey(ReferenceTag.KEY))
                {
                    inEdgeRefs.add(inEdgeTags.get(ReferenceTag.KEY));
                }
            }
        }
    }

    /**
     * This function determines whether the edge in question has in and out edges that are
     * "contiguous" (angle difference is less than 30 degrees) and have the same name/ref tag
     * 
     * @param edge
     *            edge in question
     * @param inEdges
     *            edge's inEdges
     * @param outEdges
     *            edge's outEdges
     * @return boolean if edge in question has inconsistent tags with contiguous inEdges/outEdges.
     */
    private boolean inconsistentTagsWithContiguousEdges(final Edge edge, final Set<Edge> inEdges,
            final Set<Edge> outEdges)
    {
        final Set<String> inEdgeNames = new HashSet<>();
        final Set<String> outEdgeNames = new HashSet<>();

        final Set<String> inEdgeRefs = new HashSet<>();
        final Set<String> outEdgeRefs = new HashSet<>();

        // inEdge logic to add name/ref tag to name/ref sets respectively
        // angle between inEdge and edge in question < 30 (configurable)
        // inEdge contains either name or ref tag
        this.inEdgeLogic(edge, inEdges, inEdgeNames, inEdgeRefs);

        // outEdge logic to add name/ref tag to name/ref sets respectively
        // angle between edge in question and outEdge < 30 (configurable)
        // outEdge contains either name or ref tag
        this.outEdgeLogic(edge, outEdges, outEdgeNames, outEdgeRefs);

        // Check to see if inEde and outEdge share either the same name or the same ref tag.
        return this.edgesShareTags(inEdgeNames, outEdgeNames)
                || this.edgesShareTags(inEdgeRefs, outEdgeRefs);
    }

    /**
     * Function to determine if edge is a small connector edge which doesn't require a name or ref
     * tag. Will be ignored.
     *
     * @param edge
     *            edge in question
     * @return boolean if edge is a small connector road that doesn't require a name or ref tag.
     */
    private boolean isConnectorWayToIgnore(final Edge edge)
    {
        final Set<Edge> completeWay = new OsmWayWalker(edge).collectEdges();

        // Typical single edges scenario through more basic intersections
        if (completeWay.size() == 1)
        {
            final Node startNode = edge.start();
            final Node endNode = edge.end();

            final Set<Edge> inEdges = edge.inEdges();
            final Set<Edge> outEdges = edge.outEdges();

            // Scenario captures an edge in between two contiguous edges, one on the the upstream
            // and one on the downstream side that both share the same name/ref tag.
            if (this.inconsistentTagsWithContiguousEdges(edge, inEdges, outEdges))
            {
                return false;
            }

            // Keep connected edges if osmIdentifier doesn't match original edge and if edge is not
            // contiguous with original way.
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

            // Collect edge name tags from connected edges
            final Set<String> startNodeConnectedEdgeNames = this
                    .collectEdgeNameTags(startNodeConnectedEdges);
            final Set<String> endNodeConnectedEdgeNames = this
                    .collectEdgeNameTags(endNodeConnectedEdges);

            // Collect edge ref tags from connected edges
            final Set<String> startNodeConnectedEdgeRefs = this
                    .collectEdgeRefTags(startNodeConnectedEdges);
            final Set<String> endNodeConnectedEdgeRefs = this
                    .collectEdgeRefTags(endNodeConnectedEdges);

            return this.edgesShareTags(startNodeConnectedEdgeNames, endNodeConnectedEdgeNames)
                    || this.edgesShareTags(startNodeConnectedEdgeRefs, endNodeConnectedEdgeRefs);
        }
        return false;
    }

    /**
     * function to determine which edge's name/ref tags to add to respective sets if applicable
     * based on difference in angle between edge in question and outEdge
     * 
     * @param edge
     *            edge in question
     * @param outEdges
     *            edge in question's outEdges
     * @param outEdgeNames
     *            Set of outEdges' names
     * @param outEdgeRefs
     *            Set of outEdges' refs
     */
    private void outEdgeLogic(final Edge edge, final Set<Edge> outEdges,
            final Set<String> outEdgeNames, final Set<String> outEdgeRefs)
    {
        // outEdge logic to add name/ref tag to name/ref sets respectively
        for (final Edge outEdge : outEdges)
        {
            final Map<String, String> outEdgeTags = outEdge.getTags();

            // angle between edge in question and outEdge < 30 (configurable)
            // outEdge contains either name or ref tag
            if (this.angleDiffBetweenEdges(outEdge, edge)
                    .asDegrees() <= this.minAngleForNonContiguousWays
                    && (outEdgeTags.containsKey(NameTag.KEY)
                            || outEdgeTags.containsKey(ReferenceTag.KEY)))
            {
                if (outEdgeTags.containsKey(NameTag.KEY))
                {
                    outEdgeNames.add(outEdgeTags.get(NameTag.KEY));
                }

                if (outEdgeTags.containsKey(ReferenceTag.KEY))
                {
                    outEdgeRefs.add(outEdgeTags.get(ReferenceTag.KEY));
                }
            }
        }
    }
}
