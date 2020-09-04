package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.BridgeTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.SurfaceTag;
import org.openstreetmap.atlas.tags.names.NameFinder;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags improper road name values. Road names are considered improper if they match a
 * road type, or a road surface material, as defined in the Highway, Bridge, and Surface Tags.
 * Additionally, improper roads names are listed in the names.improper configuration value.
 *
 * @author mgostintsev
 * @author mcuthbert
 */
public class ImproperAndUnknownRoadNameCheck extends BaseCheck<Long>
{
    private static final String TAG_INSTRUCTION = "Road (Osm identifier: {0,number,#}) contains {1} tag with value {2} that is equivalent to a value equal to a common {3} tag value.\n";
    private static final String IMPROPER_INSTRUCTION = "Road (Osm identifier: {0,number,#}) contains {1} tag with improper value {2}.\n";
    private static final String UNKNOWN_TAG_VALUE = "unknown";
    private static final String UNKNOWN_INSTRUCTION = "Road (Osm identifier: {0,number,#}) contains {1} tag with an 'unknown' value.\n";
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(UNKNOWN_INSTRUCTION,
            TAG_INSTRUCTION, IMPROPER_INSTRUCTION);
    private static final List<String> IMPROPER_NAMES_DEFAULT = Arrays.asList("street", "express",
            "avenue", "drive");
    private static final long serialVersionUID = -9218913713192411024L;

    // List of words that are not allowed to be road names
    private final List<String> improperNames;

    public ImproperAndUnknownRoadNameCheck(final Configuration configuration)
    {
        super(configuration);
        this.improperNames = configurationValue(configuration, "names.improper",
                IMPROPER_NAMES_DEFAULT);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Edge && ((Edge) object).isMainEdge();
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        if (!this.isFlagged(object.getOsmIdentifier()))
        {
            final Set<String> instructions = new HashSet<>();
            getAllNameTags(object).entrySet().forEach(
                    entry -> updateInstructions(entry, instructions, object.getOsmIdentifier()));

            if (!instructions.isEmpty())
            {
                this.markAsFlagged(object.getOsmIdentifier());
                final CheckFlag flag = createFlag(new OsmWayWalker((Edge) object).collectEdges(),
                        "");
                flag.addInstructions(instructions);
                return Optional.of(flag);
            }
        }

        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private Map<String, String> getAllNameTags(final AtlasObject object)
    {
        final Map<String, String> relevantTags = new HashMap<>();
        for (final String key : object.getTags().keySet())
        {
            for (final String desiredKey : NameFinder.STANDARD_TAG_KEYS)
            {
                if (key.startsWith(desiredKey))
                {
                    object.getTag(key).ifPresent(tag -> relevantTags.put(key, tag));
                }
            }
        }
        return relevantTags;
    }

    /**
     * Checks for unknown name and then loops over the Surface, Bridge and Highway Tags to see if
     * the name value is one these
     *
     * @param entry
     *            The key value par from the map being updated
     * @return true if the name is improper, false otherwise
     */
    private void updateInstructions(final Entry<String, String> entry,
            final Set<String> instructions, final long osmIdentifier)
    {
        final String tagKey = entry.getKey();
        final String tagValue = entry.getValue();
        if (tagValue.equalsIgnoreCase(UNKNOWN_TAG_VALUE))
        {
            instructions.add(this.getLocalizedInstruction(0, osmIdentifier, tagKey, tagValue));
        }
        else if (Arrays.stream(SurfaceTag.values())
                .anyMatch((final SurfaceTag surface) -> StringUtils
                        .equalsIgnoreCase(surface.toString(), tagValue)))
        {
            instructions.add(
                    this.getLocalizedInstruction(1, osmIdentifier, tagKey, tagValue, "Surface"));
        }
        else if (Arrays.stream(BridgeTag.values()).anyMatch((final BridgeTag bridge) -> StringUtils
                .equalsIgnoreCase(bridge.toString(), tagValue)))
        {
            instructions.add(
                    this.getLocalizedInstruction(1, osmIdentifier, tagKey, tagValue, "Bridge"));
        }
        else if (Arrays.stream(HighwayTag.values())
                .anyMatch((final HighwayTag highway) -> StringUtils
                        .equalsIgnoreCase(highway.toString(), tagValue)))
        {
            instructions.add(
                    this.getLocalizedInstruction(1, osmIdentifier, tagKey, tagValue, "Highway"));
        }
        else if (this.improperNames.stream().anyMatch(name -> name.equalsIgnoreCase(tagValue)))
        {
            instructions.add(this.getLocalizedInstruction(2, osmIdentifier, tagKey, tagValue));
        }
    }

}
