package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.RelationTypeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check is used to help identify segments that are missing the proper tagging for sign posts.
 * The basic logic of the check is to first find all edges with given filter with on and off ramps.
 * Once ramps are identified and filtered, a flag is thrown if one or both of the following
 * conditions are met.
 * <p>
 * 1) The starting node for an off ramp is missing the highway=motorway_junction tag<br>
 * 2) The ramp road is missing the destination tag<br>
 * <p>
 * If either of these cases is true and ramp is over a certain length then a flag is created.
 *
 * @author ericgodwin
 * @author mkalender
 * @author bbreithaupt
 */
public class SignPostCheck extends BaseCheck<String>
{
    private static final long serialVersionUID = 8042255121118115024L;

    // Instruction
    private static final String JUNCTION_NODE_INSTRUCTION = "Junction node {0,number,#} is missing a motorway_junction tag.";
    private static final String DESTINATION_TAG_INSTRUCTION = "Way {0,number,#} is missing a destination tag.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList(JUNCTION_NODE_INSTRUCTION, DESTINATION_TAG_INSTRUCTION);

    // Default values for configurable settings
    private static final String RAMP_FILTER_DEFAULT = "highway->motorway_link,trunk_link";
    private static final String DESTINATION_TAG_FILTER_DEFAULT = "destination->*|destination:ref->*|destination:street->*";

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
        return TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMasterEdge()
                && this.rampEdgeFilter.test(object) && ((Edge) object).highwayTag().isLink();
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
        final CheckFlag flag = new CheckFlag(this.getTaskIdentifier(object));

        final HighwayTag targetType = highwayTag.getHighwayFromLink().get();

        boolean checkDestination = false;
        boolean checkJunctionNode = false;

        for (final Edge inEdge : edge.inEdges())
        {
            final HighwayTag inHighwayTag = inEdge.highwayTag();
            if (inHighwayTag.equals(targetType))
            {
                checkDestination = true;
                checkJunctionNode = true;
                break;
            }
            else if (!inHighwayTag.equals(highwayTag))
            {
                checkDestination = true;
            }
            else if (this.checkLinkBranches && inEdge.outEdges().stream().anyMatch(
                    outEdge -> outEdge.highwayTag().equals(highwayTag) && !outEdge.equals(edge)))
            {
                checkDestination = true;
            }
        }

        // Check to see if start node is missing junction tag
        final Node start = edge.start();
        if (checkJunctionNode
                && !Validators.isOfType(start, HighwayTag.class, HighwayTag.MOTORWAY_JUNCTION))
        {
            flag.addInstruction(this.getLocalizedInstruction(0, start.getOsmIdentifier()));
            flag.addObject(start);
        }

        if (checkDestination && !destinationTagFilter.test(edge)
                && edge.relations().stream().noneMatch(relation -> Validators.isOfType(relation,
                        RelationTypeTag.class, RelationTypeTag.DESTINATION_SIGN)))
        {
            flag.addInstruction(this.getLocalizedInstruction(1, edge.getOsmIdentifier()));
            flag.addObject(edge);
        }

        // Return the flag if it has any flagged objects in it
        if (!flag.getFlaggedObjects().isEmpty())
        {
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
