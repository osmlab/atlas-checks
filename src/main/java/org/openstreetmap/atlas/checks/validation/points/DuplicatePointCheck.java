package org.openstreetmap.atlas.checks.validation.points;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;

import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check looks for two or more {@link Point}s that are in the exact same location.
 *
 * @author savannahostrowski
 */
public class DuplicatePointCheck extends BaseCheck<Location>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Duplicate Node {0,number,#} at {1}");
    private static final Distance ZERO_DISTANCE = Distance.ZERO;
    private static final long serialVersionUID = 8624313405718452123L;

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public DuplicatePointCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Point;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Point point = (Point) object;
        if (!this.isFlagged(point.getLocation()))
        {
            final Rectangle box = point.getLocation().boxAround(ZERO_DISTANCE);
            for (final Point dupe : object.getAtlas().pointsWithin(box))
            {
                if (object.getIdentifier() != dupe.getIdentifier()
                        && dupe.getLocation().equals(point.getLocation()))
                {
                    this.markAsFlagged(point.getLocation());
                    return Optional.of(createFlag(object, this.getLocalizedInstruction(0,
                            object.getOsmIdentifier(), point.getLocation())));
                }
            }
        }
        return Optional.empty();
    }
}
