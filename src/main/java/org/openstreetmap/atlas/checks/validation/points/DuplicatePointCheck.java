package org.openstreetmap.atlas.checks.validation.points;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check looks for two or more {@link Point}s that are in the exact same location.
 *
 * @author savannahostrowski
 */
public class DuplicatePointCheck extends BaseCheck<Location>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Nodes {0} are duplicates at {1}");
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

        return object instanceof Point && !this.isFlagged(((Point) object).getLocation());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Point point = (Point) object;

        final List<Point> duplicates = Iterables
                .asList(object.getAtlas().pointsAt(point.getLocation()));
        if (duplicates.size() > 1)
        {
            this.markAsFlagged(point.getLocation());
            final List<Long> duplicateIdentifiers = duplicates.stream()
                    .map(AtlasEntity::getOsmIdentifier).collect(Collectors.toList());
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, duplicateIdentifiers, point.getLocation())));
        }

        return Optional.empty();
    }
}
