package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.LanesTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags {@link Edge}s that have the {@code highway} tag and a {@code lanes} tag with an invalid
 * value. The valid {@code lanes} values are configurable.
 *
 * @author bbreithaupt
 */
public class InvalidLanesTagCheck extends BaseCheck
{

    private static final long serialVersionUID = -1459761692833694715L;

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Way {0,number,#} has an invalid lanes value.");

    private static final String LANES_FILTER_DEFAULT = "Lanes->1,1.5,2,3,4,5,6,7,8,9,10";

    private final TaggableFilter lanesFilter;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public InvalidLanesTagCheck(final Configuration configuration)
    {
        super(configuration);
        this.lanesFilter = (TaggableFilter) configurationValue(configuration, "lanes.filter",
                LANES_FILTER_DEFAULT, value -> new TaggableFilter(value.toString()));
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
        return Validators.hasValuesFor(object, LanesTag.class)
                && HighwayTag.highwayTag(object).isPresent() && object instanceof Edge
                && !this.isFlagged(((Edge) object).getOsmIdentifier());
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
        if (!this.lanesFilter.test(object))
        {
            this.markAsFlagged(((Edge) object).getOsmIdentifier());
            return Optional.of(this.createFlag(object,
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
