package org.openstreetmap.atlas.checks.validation.tag;

import static org.openstreetmap.atlas.checks.constants.CommonConstants.COLON;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flags conditional restriction tags that do not follow the scheme provided on the OSM wiki.
 * {@literal <restriction-type>[:<transportation mode>][:<direction>]:conditional
 * = <restriction-value> @ <condition>[;<restriction-value> @ <condition>]}
 *
 * @author laura
 * @see <a href="https://wiki.openstreetmap.org/wiki/Conditional_restrictions">wiki</a> for more
 *      information.
 */
public class ConditionalRestrictionCheck extends BaseCheck<String>
{

    private static final long serialVersionUID = 6726352951073801440L;

    public static final String CONDITIONAL = ":conditional";
    private static final List<String> RESTRICTION_TYPES = List.of("access", "restriction",
            "maxspeed", "minspeed", "maxweight", "maxaxleload", "maxheight", "maxlength", "maxstay",
            "maxgcweight", "maxgcweightrating", "interval", "duration", "overtaking", "oneway",
            "fee", "toll", "noexit", "snowplowing", "disabled", "lanes", "parking");
    private static final List<String> TRANSPORTATION_MODE = List.of("foot", "ski", "inline_skates",
            "horse", "vehicle", "bicycle", "carriage", "trailer", "caravan", "motor_vehicle",
            "motorcycle", "moped", "mofa", "motorcar", "motorhome", "tourist_bus", "coach", "goods",
            "hgv", "hgv_articulated", "agricultural", "golf_cart", "atv", "snowmobile", "psv",
            "bus", "minibus", "share_taxi", "taxi", "hov", "hazmat", "emergency", "canoe",
            "electric_vehicle", "cycleway", "busway");
    private static final List<String> DIRECTION = List.of("forward", "backward", "left", "right",
            "both");
    private static final List<String> ACCESS_RESTRICTION_VALUE = List.of("yes", "no", "private",
            "permissive", "destination", "delivery", "customers", "designated", "use_sidepath",
            "dismount", "agricultural", "forestry", "discouraged", "official", "lane",
            "share_busway", "opposite_share_busway");

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "The conditional key \"{0}\" does not respect the \"<restriction-type>[:<transportation mode>][:<direction>]:conditional\" format",
            "The conditional value \"{0}\" does not respect the format \"<restriction-value> @ <condition>[;<restriction-value> @ <condition>]\" ",
            "The element with id {0,number,#} does not follow the conditional restriction pattern.");
    private static final int TWO_PARTS = 2;
    private static final int THREE_PARTS = 3;
    private static final int FOUR_PARTS = 4;

    private static final Pattern VALUE_PATTERN = Pattern.compile(
            "([a-zA-Z0-9_.-|\\s]*?)\\s@\\s(\\([^)\\s][^)]+?\\)|[^();\\s][^();]*)\\s*(;\\s*([^@\\s][^@]*?)\\s*@\\s*(\\([^)\\s][^)]+?\\)|[^();\\s][^();]*?)\\s*)*");

    public ConditionalRestrictionCheck(final Configuration configuration)
    {
        super(configuration);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        // all elements that have a tag containing ":conditional"
        return object.getOsmTags().keySet().stream().anyMatch(key -> key.contains(CONDITIONAL));
    }

    /**
     * Checks if the conditional restrictions respects the format
     *
     * @param object
     *            the atlas object containing a conditional tag
     * @return an optional {@link CheckFlag} object
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Set<String> instructions = new HashSet<>();

        final List<String> conditionalKeys = object.getOsmTags().keySet().stream()
                .filter(key -> key.contains(CONDITIONAL)).collect(Collectors.toList());
        for (final String key : conditionalKeys)
        {
            if (!this.isKeyValid(key))
            {
                instructions.add(this.getLocalizedInstruction(0, key));
            }
            final String value = object.getOsmTags().get(key);
            if (!this.isValueValid(value, key))
            {
                instructions.add(this.getLocalizedInstruction(1, value));
            }
        }
        if (!instructions.isEmpty())
        {
            final CheckFlag flag = this.createFlag(object,
                    this.getLocalizedInstruction(2, object.getOsmIdentifier()));
            instructions.forEach(flag::addInstruction);
            return Optional.of(flag);
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private boolean containsTransportationMode(final String key)
    {
        final String[] parts = key.split(COLON);
        for (final String part : parts)
        {
            if (this.isTransportationMode(part))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isAccessType(final String value)
    {
        return "access".equals(value);
    }

    private boolean isAccessValue(final String value)
    {
        return ACCESS_RESTRICTION_VALUE.contains(value);
    }

    private boolean isDirection(final String value)
    {
        return DIRECTION.contains(value);
    }

    private boolean isKeyValid(final String key)
    {
        final String[] parts = key.split(COLON);
        // access:lanes is a valid exception for lanes on second position
        final boolean isAccessLanes = parts.length > 2
                && (this.isAccessType(parts[0]) && this.isLanes(parts[1]));
        switch (parts.length)
        {
            // starts with 2 because they all contain the conditional part
            case TWO_PARTS:
                return this.isRestrictionType(parts[0]) || this.isTransportationMode(parts[0]);
            case THREE_PARTS:
                final boolean isRestrictionTypeFormat = this.isRestrictionType(parts[0])
                        && (this.isTransportationMode(parts[1]) || this.isDirection(parts[1]));
                final boolean isTransportTypeFormat = this.isTransportationMode(parts[0])
                        && (this.isDirection(parts[1]) || this.isLanes(parts[1]));
                return isRestrictionTypeFormat || isTransportTypeFormat || isAccessLanes;
            case FOUR_PARTS:
                final boolean isRestrictionTransport = this.isRestrictionType(parts[0])
                        && this.isTransportationMode(parts[1]);
                final boolean isTransportLanes = this.isTransportationMode(parts[0])
                        && this.isLanes(parts[1]);
                return (isRestrictionTransport || isTransportLanes || isAccessLanes)
                        && this.isDirection(parts[2]);
            default:
                return false;
        }
    }

    private boolean isLanes(final String value)
    {
        return "lanes".equals(value);
    }

    private boolean isNotLanesType(final String key)
    {
        final String[] parts = key.split(COLON);
        return !this.isLanes(parts[0]);
    }

    private boolean isRestrictionType(final String value)
    {
        return RESTRICTION_TYPES.contains(value);
    }

    private boolean isTransportationMode(final String value)
    {
        return TRANSPORTATION_MODE.contains(value);
    }

    private boolean isValueValid(final String value, final String key)
    {
        final Matcher matcher = VALUE_PATTERN.matcher(value);
        if (matcher.matches())
        {
            if (this.containsTransportationMode(key) && this.isNotLanesType(key))
            {
                final String[] parts = value.split("@");
                for (int i = 0; i < parts.length - 1; i += 2)
                {
                    // the character | is used to display lanes and can accompany a access value in
                    // cases of lanes
                    final String[] subParts = parts[i].split("\\|");
                    for (final String part : subParts)
                    {
                        final String trimmedPart = part.trim();
                        if (!trimmedPart.isEmpty() && !this.isAccessValue(trimmedPart))
                        {
                            return false;
                        }
                    }
                }
            }
        }
        else
        {
            return false;
        }
        return true;
    }
}
