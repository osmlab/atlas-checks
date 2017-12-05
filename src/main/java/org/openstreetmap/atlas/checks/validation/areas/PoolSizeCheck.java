package org.openstreetmap.atlas.checks.validation.areas;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.tags.LeisureTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags swimming pools that are larger or smaller than expected
 *
 * @author mcuthbert
 */
public class PoolSizeCheck extends BaseCheck<Long>
{
    // The worlds largest swimming pool is at the San Alfonso del Mar resort in Algarrobo and
    // measures 1,013 meters in length, which is 4,856,227.71 square meters. So we can use a even
    // 5,000,000 and assume that it won't find any valid pools. In saying that we can modify
    // configuration later in countries outside Chile to get more of a standard norm, and if need be
    // we can also filter the pool out of our equation.
    public static final double MAXIMUM_SIZE_DEFAULT = 5000000;
    // A 5 meter squared pool if a circle would only be roughly 2 meters in diameter.
    public static final double MINIMUM_SIZE_DEFAULT = 5;
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "The swimming pool with OSM ID {0} with a surface area of {1,number,#.##} meters squared is greater than the expected maximum of {2} meters squared.",
            "The swimming pool with OSM ID {0} with a surface area of {1,number,#.##} meters squared is smaller than the expected minimum of {2} meters squared.");
    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;
    // Create maximum and minimum size variables to be used later in our flag function.
    private final double maximumSize;
    private final double minimumSize;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public PoolSizeCheck(final Configuration configuration)
    {
        super(configuration);
        // Retrieve the maximum and minimum sizes from configuration
        this.maximumSize = (double) this.configurationValue(configuration, "surface.maximum",
                MAXIMUM_SIZE_DEFAULT);
        this.minimumSize = (double) this.configurationValue(configuration, "surface.minimum",
                MINIMUM_SIZE_DEFAULT);
    }

    /**
     * This function will validate if the supplied atlas object is valid for the check
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return {@code true} if this object should be checked
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // by default we will assume all objects as valid
        return object instanceof Area;
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Area area = (Area) object;
        // this could be done in the ValidCheckForObject as well, doesn't change things too much
        if (Validators.isOfType(object, LeisureTag.class, LeisureTag.SWIMMING_POOL))
        {
            final double surfaceArea = area.asPolygon().surface().asMeterSquared();
            // we are purposefully separating our if statements to have more specific instructions
            if (surfaceArea > this.maximumSize)
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0,
                        object.getOsmIdentifier(), surfaceArea, this.maximumSize)));
            }
            else if (surfaceArea < this.minimumSize)
            {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(1,
                        object.getOsmIdentifier(), surfaceArea, this.minimumSize)));
            }
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
