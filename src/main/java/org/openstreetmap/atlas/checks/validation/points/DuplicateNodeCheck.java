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
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check looks for two or more {@link Node}s that are in the exact same location.
 *
 * @author cuthbertm
 * @author mgostintsev
 */
public class DuplicateNodeCheck extends BaseCheck<Location>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Nodes {0} are duplicates at {1}.");
    private static final long serialVersionUID = 1055616456230649593L;

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
        this.markAsFlagged(node.getLocation());

        final List<Node> duplicates = Iterables
                .asList(object.getAtlas().nodesAt(node.getLocation()));
        if (duplicates.size() > 1)
        {
            final List<Long> duplicateIdentifiers = duplicates.stream()
                    .map(AtlasEntity::getOsmIdentifier).collect(Collectors.toList());
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, duplicateIdentifiers, node.getLocation())));
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
