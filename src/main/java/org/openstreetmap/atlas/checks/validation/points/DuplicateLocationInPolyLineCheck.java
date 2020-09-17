package org.openstreetmap.atlas.checks.validation.points;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Checks for repeating locations in a {@link PolyLine}.
 *
 * @author mgostintsev
 */
public class DuplicateLocationInPolyLineCheck extends BaseCheck<Long>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Repeated location found at {0} for feature id {1,number,#} ");
    private static final long serialVersionUID = 7403488805532662065L;

    public DuplicateLocationInPolyLineCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge || object instanceof Area || object instanceof Line;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Set<Location> visitedLocations = new HashSet<>();
        final Iterator<Location> locations = ((AtlasItem) object).getRawGeometry().iterator();
        while (locations.hasNext())
        {
            final Location currentLocation = locations.next();
            if (visitedLocations.contains(currentLocation)
                    && !this.isFlagged(object.getOsmIdentifier()))
            {
                this.markAsFlagged(object.getOsmIdentifier());
                return Optional.of(createFlag(object, this.getLocalizedInstruction(0,
                        currentLocation.toString(), object.getOsmIdentifier())));
            }
            else
            {
                visitedLocations.add(currentLocation);
            }
        }

        // If we reach here, all locations for this PolyLine are unique
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

}
