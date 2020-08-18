package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Segment;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

import static java.lang.Math.pow;

/**
 * Auto generated Check template
 *
 * @author v-garei
 */
public class SuddenHighwayChange extends BaseCheck
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public SuddenHighwayChange(final Configuration configuration)
    {
        super(configuration);
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH,
        // Distance::meters);
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
        return TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMasterEdge()
                && HighwayTag.isCarNavigableHighway(object);
    }

    /**
     * Calculates the angle between the two segments. Uses the Law of Cosines to find the angle.
     * Assumes that the segments connect at an end point.
     *
     * @return the angle between the two segments <= 180 degrees
     */
    private double findAngle(final Edge baseEdge, final Edge outEdge)
    {
        final double aLength = baseEdge.length().asMeters();
        final double bLength = outEdge.length().asMeters();
        final double cLength = new Segment(baseEdge.start().getLocation(), outEdge.end().getLocation()).length().asMeters();
        return Math.toDegrees(Math.acos(
                (pow(aLength, 2) + pow(bLength, 2) - pow(cLength, 2)) / (2 * aLength * bLength)));
    }

    private boolean isOutEdgeDiffHighwayTag(final Edge baseEdge, final Edge outEdge) {
        return baseEdge.highwayTag() == outEdge.highwayTag();
    }

    private boolean isOutEdgeLink(final Edge outEdge) {
        return outEdge.highwayTag().isLink();
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
        // insert algorithmic check to see whether object needs to be flagged.
        // Example of flagging an object
        // return Optional.of(this.createFlag(object, "Instruction how to fix issue or reason behind
        // flagging the object");

        /**
         * Consecutive segment angle must be greater than 90 degrees
         * touching segments must be different highway tag
         * out edge must not be *_link
         */
        Edge baseEdge = ((Edge) object);
        Set<Edge> outEdges = ((Edge) object).outEdges();

        for (Edge edge : outEdges) {
            if (findAngle(baseEdge, edge) > 90 && isOutEdgeDiffHighwayTag(baseEdge, edge) && isOutEdgeLink(edge)) {
                return Optional.of(
                        createFlag(object, this.getLocalizedInstruction(0, object.getOsmIdentifier())));
            }

//            System.out.println("edge name: " + edge.getName());
//            System.out.println("edge highwaytag: " + edge.highwayTag());
        }



        return Optional.empty();
    }
}
