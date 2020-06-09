package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.configuration.Configuration;


/**
 * Flags conditional restriction tags that do not follow the scheme provided on the OSM wiki.
 * {@literal <restriction-type>[:<transportation mode>][:<direction>]:conditional
  = <restriction-value> @ <condition>[;<restriction-value> @ <condition>]}
 *
 * @author laura
 * @see <a href="https://wiki.openstreetmap.org/wiki/Conditional_restrictions">wiki</a>
 * for more information.
 */
public class ConditionalRestrictionCheck extends BaseCheck {

    private static final long serialVersionUID = 6726352951073801440L;

    public static final String CONDITIONAL = ":conditional";
    private static final List<String> RESTRICTION_TYPES =
            List.of("access", "restriction", "maxspeed", "minspeed", "maxweight", "maxaxleload", "maxheight",
                    "maxlength", "maxstay", "maxgcweight", "maxgcweightrating", "interval", "duration", "overtaking",
                    "oneway", "fee", "toll", "noexit");
    private static final List<String> TRANSPORTATION_MODE =
            List.of("foot", "ski", "inline_skates", "horse", "vehicle", "bicycle", "carriage", "trailer", "caravan",
                    "motor_vehicle", "motorcycle", "moped", "mofa", "motorcar", "motorhome", "tourist_bus", "coach",
                    "goods", "hgv", "hgv_articulated", "agricultural", "golf_cart", "atv", "snowmobile", "psv", "bus",
                    "minibus", "share_taxi", "taxi", "hov", "hazmat", "emergency");
    private static final List<String> DIRECTION = List.of("forward", "backward");
    private static final List<String> ACCESS_RESTRICTION_VALUE =
            List.of("yes", "no", "private", "permissive", "destination", "delivery", "customers", "designated",
                    "use_sidepath", "dismount", "agricultural", "forestry", "discouraged", "official");

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "The conditional key {0} does not respect the \"<restriction-type>[:<transportation mode>][:<direction>]:conditional\" format",
            "The conditional value does not respect the format \"<restriction-value> @ <condition>[;<restriction-value> @ <condition>]\" ");

    public ConditionalRestrictionCheck(final Configuration configuration) {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object) {
        //all elements that have a tag containing ":conditional"
        return object.getOsmTags().keySet().stream().anyMatch(key -> key.contains(CONDITIONAL));
    }

    /**
     * Checks if the conditional restrictions respects the format
     *
     * @param object the atlas object containing a conditional tag
     * @return an optional {@link CheckFlag} object
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object) {
        final List<String> conditionalKeys =
                object.getOsmTags().keySet().stream().filter(key -> key.contains(CONDITIONAL))
                        .collect(Collectors.toList());
        for (String key : conditionalKeys) {
            if (!isKeyValid(key)) {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(0, key)));
            }
            final String value = object.getOsmTags().get(key);
            if (!isValueValid(value)) {
                return Optional.of(this.createFlag(object, this.getLocalizedInstruction(1, value)));
            }
        }
        return Optional.empty();
    }

    private boolean isKeyValid(final String key) {
        final String[] parts = key.split(":");
        switch (parts.length) {
            //starts with 2 because they all contain the conditional part
            case 2:
                return isRestrictionType(parts[0]) || isTransportationMode(parts[0]);
            case 3:
                return (isRestrictionType(parts[0]) && (isTransportationMode(parts[1]) || isDirection(parts[1]))) || (
                        isTransportationMode(parts[0]) && isDirection(parts[1]));
            case 4:
                return isRestrictionType(parts[0]) && isTransportationMode(parts[1]) && isDirection(parts[2]);
            default:
                return false;
        }
    }

    private boolean isValueValid(final String value) {
        //TODO implement value check
        return true;
    }

    private boolean isRestrictionType(final String value) {
        return RESTRICTION_TYPES.contains(value);
    }

    private boolean isTransportationMode(final String value) {
        return TRANSPORTATION_MODE.contains(value);
    }

    private boolean isDirection(final String value) {
        return DIRECTION.contains(value);
    }

    private boolean isAccessValue(final String value) {
        return ACCESS_RESTRICTION_VALUE.contains(value);
    }

    @Override
    protected List<String> getFallbackInstructions() {
        return FALLBACK_INSTRUCTIONS;
    }
}
