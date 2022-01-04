package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.TurnLanesBackwardTag;
import org.openstreetmap.atlas.tags.TurnLanesForwardTag;
import org.openstreetmap.atlas.tags.TurnLanesTag;
import org.openstreetmap.atlas.tags.TurnTag;
import org.openstreetmap.atlas.tags.TurnTag.TurnType;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags {@link Edge}s that have the {@code highway} tag and a {@code lanes} tag with an invalid
 * value. The valid {@code lanes} values are configurable.
 *
 * @author mselaineleong
 */
public class InvalidTurnLanesValueCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -1459761692833694715L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Way {0,number,#} has an invalid turn:lanes value.");

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public InvalidTurnLanesValueCheck(final Configuration configuration)
    {
        super(configuration);
    }

    public final String trimKeywords(final String input)
    {
        String result = input.toLowerCase();
        for (final TurnType turnType : TurnTag.TurnType.values())
        {
            if (turnType != TurnTag.TurnType.LEFT && turnType != TurnTag.TurnType.RIGHT)
            {
                result = result.replaceAll(turnType.name().toLowerCase(), "");
            }
        }
        result = result.replaceAll(TurnTag.TurnType.LEFT.name().toLowerCase(), "");
        result = result.replaceAll(TurnTag.TurnType.RIGHT.name().toLowerCase(), "");
        result = result.replaceAll(TurnTag.TURN_LANE_DELIMITER.toLowerCase(), "");
        result = result.replaceAll(TurnTag.TURN_TYPE_DELIMITER.toLowerCase(), "");
        return result.trim();
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
        return TurnLanesTag.hasTurnLane(object) && HighwayTag.isCarNavigableHighway(object)
                && object instanceof Edge && ((Edge) object).isMainEdge()
                && !this.isFlagged(object.getOsmIdentifier());
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
        final String turnLanesTag = object.getTag(TurnLanesTag.KEY).orElse("");
        final String turnLanesForwardTag = object.getTag(TurnLanesForwardTag.KEY).orElse("");
        final String turnLanesBackwardTag = object.getTag(TurnLanesBackwardTag.KEY).orElse("");

        if (!this.trimKeywords(turnLanesTag).isEmpty()
                || !this.trimKeywords(turnLanesForwardTag).isEmpty()
                || !this.trimKeywords(turnLanesBackwardTag).isEmpty())
        {
            this.markAsFlagged(object.getOsmIdentifier());

            return Optional.of(this.createFlag(new OsmWayWalker((Edge) object).collectEdges(),
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

}
