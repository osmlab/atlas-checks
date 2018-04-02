package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.directory.api.util.Strings;
import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.DestinationTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check is used to help identify segments that are missing the proper tagging for sign posts.
 * The basic logic of the check is to first find all edges with given filter with on and off ramps.
 * Once ramps are identified and filtered, a flag is thrown if one or both of the following
 * conditions are met.
 * <p>
 * 1) The starting node for the ramp is missing the highway=motorway_junction tag<br>
 * 2) The ramp road is missing the destination tag<br>
 * <p>
 * If either of these cases is true and ramp is over a certain length then a flag is created.
 *
 * @author ericgodwin
 * @author mkalender
 */
public class SignPostCheck extends BaseCheck<String>
{
    private static final long serialVersionUID = 8042255121118115024L;

    // Instruction
    private static final String NODE_INSTRUCTION = "Junction node {0,number,#} is missing the following tags: {1}.";
    private static final String EDGE_INSTRUCTION = "Way {0,number,#} ({1}) is missing the following tags: {2}.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(NODE_INSTRUCTION,
            EDGE_INSTRUCTION);
    private static final String OFF_RAMP_KEY = "off-ramp";
    private static final String ON_RAMP_KEY = "on-ramp";

    // Default values for configurable settings
    private static final double DISTANCE_MINIMUM_METERS_DEFAULT = 50;
    private static final String SOURCE_EDGE_FILTER_DEFAULT = "highway->motorway,trunk";
    private static final String RAMP_FILTER_DEFAULT = "highway->motorway,motorway_link,trunk,trunk_link";
    private static final double RAMP_ANGLE_DIFFERENCE_DEFAULT = 30;

    // Maximum number of hops while collecting ramp edges
    private static final int MAX_EDGE_COUNT_FOR_RAMP = 5;

    // The minimum link length to examine.
    private final Distance minimumLinkLength;

    // A filter to filter source edges for flagging
    private final TaggableFilter sourceEdgeFilter;

    // A filter to filter ramp edges
    private final TaggableFilter rampEdgeFilter;

    // A tag to differentiate ramps from roads with the same classification
    private final String rampDifferentiatorTag;

    // Maximum angle difference between ramp edges
    private final Angle rampAngleDifference;

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

        this.minimumLinkLength = configurationValue(configuration, "linkLength.minimum.meters",
                DISTANCE_MINIMUM_METERS_DEFAULT, Distance::meters);
        this.sourceEdgeFilter = configurationValue(configuration, "source.filter",
                SOURCE_EDGE_FILTER_DEFAULT, value -> new TaggableFilter(value));
        this.rampEdgeFilter = configurationValue(configuration, "ramp.filter", RAMP_FILTER_DEFAULT,
                value -> new TaggableFilter(value));
        this.rampDifferentiatorTag = configurationValue(configuration, "ramp.differentiator.tag",
                null);
        this.rampAngleDifference = configurationValue(configuration,
                "ramp.angle.difference.degrees", RAMP_ANGLE_DIFFERENCE_DEFAULT, Angle::degrees);
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
        return TypePredicates.IS_EDGE.test(object) && this.sourceEdgeFilter.test(object);
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

        // First find off ramps
        edge.end().outEdges().stream()
                .filter(connectedEdge -> isPossiblyRamp(edge, highwayTag, connectedEdge))
                .forEach(outEdge ->
                {
                    // Check to see if start node is missing junction tag
                    final Node start = outEdge.start();
                    if (!Validators.isOfType(start, HighwayTag.class, HighwayTag.MOTORWAY_JUNCTION))
                    {
                        flag.addInstruction(this.getLocalizedInstruction(0,
                                start.getOsmIdentifier(), String.format("%s=%s", HighwayTag.KEY,
                                        HighwayTag.MOTORWAY_JUNCTION.getTagValue())));
                        flag.addObject(start);
                    }

                    // Check if edge is missing destination tag
                    if (!outEdge.getTag(DestinationTag.KEY).isPresent())
                    {
                        flag.addInstruction(this.getLocalizedInstruction(1,
                                outEdge.getOsmIdentifier(), OFF_RAMP_KEY, DestinationTag.KEY));
                        flag.addObject(outEdge);
                    }
                });

        // Then repeat the work for on ramps
        edge.start().inEdges().stream()
                .filter(connectedEdge -> isPossiblyRamp(edge, highwayTag, connectedEdge))
                .forEach(inEdge ->
                {
                    // Find the source of in-ramp
                    final Edge rampEdge = findFirstRampEdge(inEdge);

                    // Check to see if start node is missing junction tag
                    final Node start = rampEdge.start();
                    if (!Validators.isOfType(start, HighwayTag.class, HighwayTag.MOTORWAY_JUNCTION))
                    {
                        flag.addInstruction(this.getLocalizedInstruction(0,
                                start.getOsmIdentifier(), String.format("%s=%s", HighwayTag.KEY,
                                        HighwayTag.MOTORWAY_JUNCTION.getTagValue())));
                        flag.addObject(start);
                    }

                    // Check if edge is missing destination tag
                    if (!rampEdge.getTag(DestinationTag.KEY).isPresent())
                    {
                        flag.addInstruction(this.getLocalizedInstruction(1,
                                rampEdge.getOsmIdentifier(), ON_RAMP_KEY, DestinationTag.KEY));
                        flag.addObject(rampEdge);
                    }
                });

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

    /**
     * Given a final {@link Edge}, hops back and finds the first edge that was forming the ramp.
     *
     * @param finalEdge
     *            {@link Edge} that is the last/final piece of the ramp
     * @return {@link Edge} that possibly is the first {@link Edge} forming the ramp
     */
    private Edge findFirstRampEdge(final Edge finalEdge)
    {
        int count = 0;

        // Go back and collect edges
        Edge nextEdge = finalEdge;
        while (count < MAX_EDGE_COUNT_FOR_RAMP)
        {
            count++;

            // Break if edge has no heading
            final Optional<Heading> initialHeading = nextEdge.asPolyLine().initialHeading();
            if (!initialHeading.isPresent())
            {
                break;
            }

            // Collect in edges and make sure there is not more than one
            final Set<Edge> inEdges = nextEdge.inEdges();
            if (inEdges.isEmpty() || inEdges.size() > 1)
            {
                break;
            }

            // Find the edge that precedes nextEdge
            final HighwayTag highwayTag = nextEdge.highwayTag();
            final Edge precedingEdge = inEdges.iterator().next();

            // Ignore if edge has a different classification
            if (!highwayTag.isIdenticalClassification(precedingEdge.highwayTag()))
            {
                break;
            }

            // Break if given edge has no heading
            final Optional<Heading> finalHeading = precedingEdge.asPolyLine().finalHeading();
            if (!finalHeading.isPresent() || initialHeading.get().asPositiveAngle()
                    .difference(finalHeading.get().asPositiveAngle())
                    .isGreaterThan(this.rampAngleDifference))
            {
                break;
            }

            nextEdge = precedingEdge;
        }

        return nextEdge;
    }

    /**
     * Returns if given a connected {@link Edge} is possibly a ramp entering or leaving source
     * {@link Edge}.
     *
     * @param sourceEdge
     *            Source {@link Edge}
     * @param sourceHighwayTag
     *            {@link HighwayTag} of the source {@link Edge}
     * @param connectedEdge
     *            Connected {@link Edge} to source {@link Edge}
     * @return true if connected {@link Edge} is a ramp
     */
    private boolean isPossiblyRamp(final Edge sourceEdge, final HighwayTag sourceHighwayTag,
            final Edge connectedEdge)
    {
        // Ignore if edge is failing to pass ramp filter and also ignore if it is short
        if (!this.rampEdgeFilter.test(connectedEdge)
                || !connectedEdge.length().isGreaterThan(this.minimumLinkLength))
        {
            return false;
        }

        // Different classification is a good indication for a ramp
        if (!sourceHighwayTag.isIdenticalClassification(connectedEdge.highwayTag()))
        {
            return true;
        }

        // If ramp differentiator tag is supplied, then use it to differentiate same classification
        // edges
        if (this.rampDifferentiatorTag == null)
        {
            return true;
        }

        // Look for certain tag differences if edges have same highway tag
        final String sourceTag = sourceEdge.tag(this.rampDifferentiatorTag);
        final String rampTag = connectedEdge.tag(this.rampDifferentiatorTag);
        return !(sourceTag == null && rampTag == null || Strings.equals(sourceTag, rampTag));
    }
}
