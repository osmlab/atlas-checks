package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.RailwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
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

    private static final Predicate<AtlasObject> IS_MAJOR_HIGHWAY = object -> Validators.isOfType(
            object, HighwayTag.class, HighwayTag.MOTORWAY, HighwayTag.TRUNK, HighwayTag.PRIMARY,
            HighwayTag.SECONDARY);
    private static final Double MINIMUM_LENGTH = 500.0;
    private static final String BRIDGE_STRUCTURE_TAG = "bridge:structure";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
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
        return object instanceof Edge && ((Edge) object).isMasterEdge()
                && Validators.isOfType(object, BridgeTag.class, BridgeTag.YES)
                && (RailwayTag.isRailway(object) || IS_MAJOR_HIGHWAY.test(object));
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
                && bridgeStructureTag.isEmpty())
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
}
