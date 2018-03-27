package org.openstreetmap.atlas.checks.validation.areas;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.AreaTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flag the {@link Area}s that have a highway tag
 *
 * @author matthieun
 * @author cuthbertm
 */
public class AreasWithHighwayTagCheck extends BaseCheck<Long>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "Area with OSM ID {0,number,#} contains invalid highway tag.",
            "Area with OSM ID {0,number,#} is missing area tag.");
    private static final long serialVersionUID = 3638306611072651348L;

    public AreasWithHighwayTagCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Area;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // See configuration for initial check filter; highway=pedestrian & area=yes is a valid
        // Area/Highway tag combination.
        final Area area = (Area) object;
        final Optional<String> highwayTag = area.getTag(HighwayTag.KEY);
        // First check if highway tag exists
        if (highwayTag.isPresent())
        {
            // Send special instructions for pedestrian highways missing area=yes tag
            final int localizedInstruction = Validators.isOfType(area, HighwayTag.class,
                    HighwayTag.PEDESTRIAN) && !Validators.isOfType(area, AreaTag.class, AreaTag.YES)
                            ? 1 : 0;

            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(localizedInstruction, object.getOsmIdentifier())));

        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
