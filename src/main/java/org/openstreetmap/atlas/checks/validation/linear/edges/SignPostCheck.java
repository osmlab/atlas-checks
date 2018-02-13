package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.DestinationTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * The SignPostCheck is used to help identify segments that are missing the proper tagging for sign
 * posts. The basic logic of the check is to first find all motorway and trunk edges which have link
 * edges departing them. Once you have identified these candidates a flag is thrown if one or both
 * of the following conditions are met.
 * <p>
 * 1) The shared node is missing the highway=motorway_junction tag<br>
 * 2) The exiting link edge is missing the destination tag<br>
 * <p>
 * If either of these cases is true and the link edge is over a certain length then a flag is
 * signalled.
 *
 * @author ericgodwin
 */
public class SignPostCheck extends BaseCheck<String>
{
    private static final long serialVersionUID = 8042255121118115024L;

    // Instruction
    private static final String INSTRUCTION = "Junction node is missing the following tags: {0}.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(INSTRUCTION);

    // Predicate defining the type of outbound links we are looking for.
    private static final Predicate<Edge> LINK_PREDICATE = edge -> edge
            .highwayTag() == HighwayTag.MOTORWAY_LINK || edge.highwayTag() == HighwayTag.TRUNK_LINK;

    // The default minimum link length which will trigger the check.
    public static final double DISTANCE_MINIMUM_METERS_DEFAULT = 50;

    // The minimum link length to examine.
    private final Distance minimumLinkLength;

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
        return TypePredicates.IS_EDGE.test(object) && Validators.isOfType(object, HighwayTag.class,
                HighwayTag.MOTORWAY, HighwayTag.TRUNK, HighwayTag.PRIMARY, HighwayTag.PRIMARY_LINK);
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
        final Set<String> missingTags = new HashSet<String>();
        final Set<AtlasObject> offendingObjects = new HashSet<AtlasObject>();

        // First find any motorway_link edges going out of the edge
        edge.outEdges().stream().filter(LINK_PREDICATE).forEach(outEdge ->
        {
            // Let's ignore really short segments as they are often connectors
            // between doubly digitized trunk roads.
            if (outEdge.length().isGreaterThan(this.minimumLinkLength))
            {
                // Check to see if the node leaving the mainline is missing the
                // junction tag
                if (!Validators.isOfType(outEdge.start(), HighwayTag.class,
                        HighwayTag.MOTORWAY_JUNCTION))
                {
                    missingTags.add(String.format("%s=%s", HighwayTag.KEY,
                            HighwayTag.MOTORWAY_JUNCTION.getTagValue()));
                    offendingObjects.add(outEdge.start());
                }

                // See if the out edge has the destination tag.
                if (!outEdge.getTag(DestinationTag.KEY).isPresent())
                {
                    missingTags.add(DestinationTag.KEY);

                    // Instead of adding the edge we are adding the node so
                    // that a single point in the task is highlighted.
                    offendingObjects.add(outEdge.start());
                }
            }
        });

        // If it has an out link edge make sure that the end node has the appropriate
        // highway=motorway_junction tag.
        if (!offendingObjects.isEmpty())
        {
            final String instruction = this.getLocalizedInstruction(0,
                    String.join(", ", missingTags));
            return Optional.of(this.createFlag(offendingObjects, instruction));
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
