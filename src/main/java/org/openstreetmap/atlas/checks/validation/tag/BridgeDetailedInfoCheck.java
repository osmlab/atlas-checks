package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Flags railway and major highway bridges which are longer than configured minimum and have
 * unspecified structure. This is a port of Osmose check 7012.
 * 
 * @author ladwlo
 */
public class BridgeDetailedInfoCheck extends BaseCheck<Long>
{

    private static final long serialVersionUID = -8915653487119336836L;

    private static final EnumSet<HighwayTag> MAJOR_HIGHWAYS = EnumSet.of(HighwayTag.MOTORWAY,
            HighwayTag.TRUNK, HighwayTag.PRIMARY, HighwayTag.SECONDARY);
    private static final Double MINIMUM_LENGTH = 500.0;
    public static final String RAILWAY_TAG = "railway";
    public static final String BRIDGE_TAG = "bridge";
    public static final String BRIDGE_STRUCTURE_TAG = "bridge:structure";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "The length of this bridge (OSM ID: {0,number,#}) makes it deserve more details than just 'bridge=yes'. Add an appropriate 'bridge=*' or 'bridge:structure=*' tag.");
    private final Distance minimumLength;

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public BridgeDetailedInfoCheck(final Configuration configuration)
    {
        super(configuration);
        this.minimumLength = configurationValue(configuration, "bridge.length.minimum.meters",
                MINIMUM_LENGTH, Distance::meters);
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
        // only master edges shall be flagged to avoid duplicate flags on the same OSM Way
        return object instanceof Edge && ((Edge) object).isMasterEdge() && isGenericBridge(object)
                && (isRailway(object) || isMajorHighway(object));
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
        final Optional<String> bridgeStructureTag = object.getTag(BRIDGE_STRUCTURE_TAG);
        if (((Edge) object).length().isGreaterThan(this.minimumLength)
                && !bridgeStructureTag.isPresent())
        {
            return Optional
                    .of(createFlag(object, getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private boolean isGenericBridge(final AtlasObject object)
    {
        final Optional<String> bridgeTag = object.getTag(BRIDGE_TAG);
        return bridgeTag.isPresent() && "yes".equals(bridgeTag.get());
    }

    private boolean isMajorHighway(final AtlasObject object)
    {
        return MAJOR_HIGHWAYS.contains(((Edge) object).highwayTag());
    }

    private boolean isRailway(final AtlasObject object)
    {
        return object.getTag(RAILWAY_TAG).isPresent();
    }
}
