package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check is used to help identify segments that are missing the proper tagging for sign posts.
 * The basic logic of the check is to first find all Edges that are the start of an on or off ramp.
 * Once Edges are identified and filtered, a flag is thrown if one or both of the following
 * conditions are met.
 * <p>
 * 1) The starting node for an off ramp is missing the highway=motorway_junction tag<br>
 * 2) The ramp road is missing the destination tag<br>
 * <p>
 * If either of these cases is true and ramp is over a certain length then a flag is created.
 * Optionally, branching Edges of on and off ramps can be checked for destination tags. This is
 * configurable to accommodate varying standards in different countries.
 *
 * @author ericgodwin
 * @author mkalender
 * @author bbreithaupt
 */
public class SignPostCheck extends BaseCheck<String>
{
    private static final long serialVersionUID = 8042255121118115024L;

    // Instruction
    private static final String JUNCTION_NODE_INSTRUCTION = "Junction node {0,number,#} is missing a highway=motorway_junction tag.";
    private static final String DESTINATION_TAG_INSTRUCTION = "Way {0,number,#} is missing a destination tag.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(JUNCTION_NODE_INSTRUCTION, DESTINATION_TAG_INSTRUCTION);

    // Default values for configurable settings
    private static final double DISTANCE_MINIMUM_METERS_DEFAULT = 50;
    private static final String SOURCE_EDGE_FILTER_DEFAULT = "highway->motorway,trunk";
    private static final String RAMP_FILTER_DEFAULT = "highway->motorway_link,trunk_link";
    private static final String DESTINATION_TAG_FILTER_DEFAULT = "destination->*|destination:ref->*|destination:street->*|destination:backward->*|destination:forwards->*";

    // The minimum link length to examine.
    private final Distance minimumLinkLength;

    // A filter to filter source edges for flagging
    private final TaggableFilter sourceEdgeFilter;

    // A filter to filter ramp edges
    private final TaggableFilter rampEdgeFilter;

    // A filter for the variations of the destination tag
    private final TaggableFilter destinationTagFilter;

    // Whether to check link branches for destination tags.
    private final boolean checkLinkBranches;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public SignPostCheck(final Configuration configuration)
    {
        super(configuration);

        this.minimumLinkLength = configurationValue(configuration, "link.length.minimum.meters",
                DISTANCE_MINIMUM_METERS_DEFAULT, Distance::meters);
        this.sourceEdgeFilter = configurationValue(configuration, "source.filter",
                SOURCE_EDGE_FILTER_DEFAULT, TaggableFilter::forDefinition);
        this.rampEdgeFilter = configurationValue(configuration, "ramp.filter", RAMP_FILTER_DEFAULT,
                TaggableFilter::forDefinition);
        this.destinationTagFilter = configurationValue(configuration, "destination_tag.filter",
                DESTINATION_TAG_FILTER_DEFAULT, TaggableFilter::forDefinition);
        this.checkLinkBranches = configurationValue(configuration, "link.branch.check", true);
    }

    /**
     * Validates if the supplied {@link AtlasObject} is valid for the check.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // Must be a main Edge, and must not have been flagged already
        return TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMainEdge()
                && !this.isFlagged(String.valueOf(object.getOsmIdentifier()))
                // Must be a link defined in the rampEdgeFilter
                && this.rampEdgeFilter.test(object) && ((Edge) object).highwayTag().isLink()
                // Must be longer than the configurable limit
                && ((Edge) object).length().isGreaterThan(this.minimumLinkLength);
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
        final HighwayTag highwayTag = edge.highwayTag();
        final CheckFlag flag = new CheckFlag(String.valueOf(object.getOsmIdentifier()));
        final Set<Node> junctionNodes = new HashSet<>();
        boolean checkDestination = false;

        // Get in edges from edge and its reverse, if it has one
        final Set<Edge> inEdges = new HashSet<>(edge.inEdges());
        edge.reversed().ifPresent(reverseEdge -> inEdges.addAll(reverseEdge.inEdges()));

        // Check all in edges to find all unmarked junction nodes, and check if a destination tag is
        // required
        for (final Edge inEdge : inEdges)
        {
            // Check if inEdge is a source edge. If so this is an off ramp and needs a junction node
            // and destination tag.
            if (this.sourceEdgeFilter.test(inEdge))
            {
                checkDestination = true;
                junctionNodes.add(inEdge.end());
            }
            // Check if inEdge is not a link. If so it is an on ramp and needs a destination tag.
            else if (!inEdge.highwayTag().equals(highwayTag))
            {
                checkDestination = true;
            }
            // If currently configured to check branches, check if inEdge is a stem. If so this is a
            // branch
            else if (this.checkLinkBranches && this.isLinkStem(inEdge))
            {
                checkDestination = true;
            }
        }

        // Check to see if nodes are missing junction tags
        junctionNodes.forEach(node ->
        {
            if (!Validators.isOfType(node, HighwayTag.class, HighwayTag.MOTORWAY_JUNCTION))
            {
                flag.addInstruction(this.getLocalizedInstruction(0, node.getOsmIdentifier()));
                flag.addObject(node);
            }
        });

        // Check to see if the edge is missing a destination tag or relation
        if (checkDestination && !this.destinationTagFilter.test(edge)
                && edge.relations().stream().noneMatch(relation -> Validators.isOfType(relation,
                        RelationTypeTag.class, RelationTypeTag.DESTINATION_SIGN)))
        {
            flag.addInstruction(this.getLocalizedInstruction(1, edge.getOsmIdentifier()));
            flag.addObjects(new OsmWayWalker(edge).collectEdges());
        }

        // Return the flag if it has any flagged objects in it
        if (!flag.getFlaggedObjects().isEmpty())
        {
            this.markAsFlagged(String.valueOf(object.getOsmIdentifier()));
            return Optional.of(flag);
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Checks if an {@link Edge} has 2 or more out Edges of the same highway classification as
     * itself, excluding its reverse edge.
     *
     * @param edge
     *            {@link Edge} to check
     * @return true if the road branches from this {@link Edge}, with the same highway class
     */
    private boolean isLinkStem(final Edge edge)
    {
        return edge.outEdges().stream()
                .filter(outEdge -> outEdge.highwayTag().equals(edge.highwayTag())
                        && outEdge.getIdentifier() != edge.getIdentifier())
                .count() >= 2;
    }
}
