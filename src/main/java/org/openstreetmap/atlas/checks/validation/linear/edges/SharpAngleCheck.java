package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Angle;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * Flags edges that have an angle that is too sharp within their {@link PolyLine}. Sharp angles may
 * indicate inaccurate digitization once this threshold is exceeded. There may be other factors in
 * play here, such as number of intersections, type of highway, etc. But the main breaking point is
 * any angles that are less than 83 degrees.
 *
 * @author mgostintsev
 */
public class SharpAngleCheck extends BaseCheck<Long>
{
    private static final double THRESHOLD_DEGREES_DEFAULT = 97.0;
    private static final String TOO_SHARP_INSTRUCTION_1 = "Way {0,number,#} has {1} node(s) with angle(s) that are too sharp.";
    private static final String TOO_SHARP_INSTRUCTION_2 = "The node at {0} is too sharp.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(TOO_SHARP_INSTRUCTION_1,
            TOO_SHARP_INSTRUCTION_2);
    private static final long serialVersionUID = 285618700794811828L;
    private final Angle threshold;

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public SharpAngleCheck(final Configuration configuration)
    {
        super(configuration);
        this.threshold = this.configurationValue(configuration, "threshold.degrees",
                THRESHOLD_DEGREES_DEFAULT, Angle::degrees);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;

        // Ignore all types less significant than Tertiary
        if (edge.highwayTag().isLessImportantThan(HighwayTag.TERTIARY))
        {
            return Optional.empty();
        }

        final List<Tuple<Angle, Location>> offendingAngles = edge.asPolyLine()
                .anglesGreaterThanOrEqualTo(this.threshold);
        if (!offendingAngles.isEmpty() && !this.hasBeenFlagged(edge))
        {
            this.flagEdge(edge);

            final List<Location> offendingLocations = this.buildLocationList(offendingAngles);
            final CheckFlag newFlag = this.createFlag(object, this.getLocalizedInstruction(0,
                    object.getOsmIdentifier(), offendingAngles.size()), offendingLocations);

            for (final Location sharpAngle : offendingLocations)
            {
                newFlag.addInstruction(this.getLocalizedInstruction(1, sharpAngle.toString()));
            }
            return Optional.of(newFlag);
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private List<Location> buildLocationList(final List<Tuple<Angle, Location>> angleTuples)
    {
        final List<Location> resultList = new ArrayList<>();
        angleTuples.forEach(tuple -> resultList.add(tuple.getSecond()));

        return resultList;
    }

    /**
     * Flags the given edge and its reverse edge
     *
     * @param edge
     *            The edge to flag
     */
    private void flagEdge(final Edge edge)
    {
        this.markAsFlagged(edge.getIdentifier());

        if (edge.hasReverseEdge())
        {
            this.markAsFlagged(edge.reversed().get().getIdentifier());
        }
    }

    /**
     * Checks if the supplied edge or its reverse edge has already been flagged
     *
     * @param edge
     *            edge to check
     * @return {@code true} if the reverse edge has already been flagged
     */
    private boolean hasBeenFlagged(final Edge edge)
    {
        if (edge.hasReverseEdge())
        {
            return this.isFlagged(edge.getIdentifier())
                    || this.isFlagged(edge.reversed().get().getIdentifier());
        }
        else
        {
            return this.isFlagged(edge.getIdentifier());
        }
    }

}
