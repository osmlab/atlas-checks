package org.openstreetmap.atlas.checks.validation.linear;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.WaterTag;
import org.openstreetmap.atlas.tags.WaterwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Flag lines that have only one point, or none, and the ones that are too long.
 *
 * @author matthieun
 * @author cuthbertm
 */
public class MalformedPolyLineCheck extends BaseCheck<Long>
{
    private static final Distance MAXIMUM_LENGTH = Distance.kilometers(100);
    private static final int MAXIMUM_POINTS = 500;
    private static final String MAX_LENGTH_INSTRUCTION = "Line is {0}, which is longer than the maximum of {1}";
    private static final String MAX_POINTS_INSTRUCTION = "Line contains {0} points more than maximum of {1}";
    private static final String MAX_POINTS_MAX_LENGTH_INSTRUCTION = "Line contains {0} points more than maximum of {1} and line is {2}, which is longer than the maximum of {3}";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(MAX_POINTS_INSTRUCTION,
            MAX_LENGTH_INSTRUCTION, MAX_POINTS_MAX_LENGTH_INSTRUCTION);
    private static final long serialVersionUID = -6190296606600063334L;
    private static final WaterwayTag[] WATERWAY_TAGS = { WaterwayTag.CANAL, WaterwayTag.STREAM,
            WaterwayTag.RIVER, WaterwayTag.RIVERBANK };

    public MalformedPolyLineCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && ((Edge) object).isMainEdge() || object instanceof Line;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final LineItem line = (LineItem) object;
        final int numberPoints = Iterables.asList(line.getRawGeometry()).size();
        final Distance length = line.asPolyLine().length();
        // We exclude certain complex PolyLines from the check.
        if (this.isComplexPolyLine(line) || this.isMemberOfRelationWithWaterTag(line))
        {
            return Optional.empty();
        }
        if (numberPoints > MAXIMUM_POINTS && length.isGreaterThan(MAXIMUM_LENGTH))
        {
            return Optional.of(createFlag(object, this.getLocalizedInstruction(2, numberPoints,
                    MAXIMUM_POINTS, length, MAXIMUM_LENGTH)));
        }
        if (numberPoints < 1 || numberPoints > MAXIMUM_POINTS)
        {
            return Optional.of(createFlag(object,
                    this.getLocalizedInstruction(0, numberPoints, MAXIMUM_POINTS)));
        }
        if (length.isGreaterThan(MAXIMUM_LENGTH))
        {
            return Optional.of(
                    createFlag(object, this.getLocalizedInstruction(1, length, MAXIMUM_LENGTH)));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Certain polylines like river, coastline, stream etc should be excluded from the check, as
     * they can be irregular and long, but still valid polylines. This method checks if a polyline
     * is part of such entities.
     *
     * @param line
     * @return {@code true} if this object meets the conditions for complex polyline
     */
    private boolean isComplexPolyLine(final LineItem line)
    {
        return Validators.isOfType(line, WaterwayTag.class, WATERWAY_TAGS)
                || Validators.isOfType(line, NaturalTag.class, NaturalTag.COASTLINE)
                || Validators.isOfType(line, NaturalTag.class, NaturalTag.WATER)
                        && Validators.isOfType(line, WaterTag.class, WaterTag.RIVER);
    }

    /**
     * Checks if {@link LineItem} is part of relation having WaterTag associated with it
     *
     * @param line
     * @return {@code true} if the LineItem is part of relation with WaterTag
     */
    private boolean isMemberOfRelationWithWaterTag(final LineItem line)
    {
        return line.relations().stream().anyMatch(
                relation -> Validators.isOfType(relation, NaturalTag.class, NaturalTag.WATER));
    }
}
