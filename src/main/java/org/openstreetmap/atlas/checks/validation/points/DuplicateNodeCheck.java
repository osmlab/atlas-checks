package org.openstreetmap.atlas.checks.validation.points;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This check looks for two or more {@link Node}s that are in the exact same location.
 *
 * @author cuthbertm
 * @author mgostintsev
 */
public class DuplicateNodeCheck extends BaseCheck<Location>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Duplicate Node {0,number,#} at {1}");
    private static final long serialVersionUID = 1055616456230649593L;

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
    public DuplicateNodeCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Node;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Node node = (Node) object;
        if (!this.isFlagged(node.getLocation()))
        {
            final Rectangle box = node.getLocation().boxAround(Distance.meters(0));
            for (final Node dupe : object.getAtlas().nodesWithin(box))
            {
                if (object.getIdentifier() != dupe.getIdentifier()
                        && dupe.getLocation().equals(node.getLocation()))
                {
                    this.markAsFlagged(node.getLocation());
                    return Optional.of(createFlag(object, this.getLocalizedInstruction(0,
                            object.getOsmIdentifier(), node.getLocation())));
                }
            }
        }
        return Optional.empty();
    }
}
