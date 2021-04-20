package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.PlaceTag;
import org.openstreetmap.atlas.tags.SurfaceTag;
import org.openstreetmap.atlas.tags.WaterwayTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.tags.names.ReferenceTag;
import org.openstreetmap.atlas.tags.oneway.OneWayTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags features which contain the "fixme" or "FIXME" tag as well as other combinations
 * of important tags.
 *
 * @author v-garei
 */
public class FixMeReviewCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -2191776278923026805L;
    private static final String HAS_FIXME_TAG = "Object {0, number, #} has 'fixme' tag and needs to be investigated.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(HAS_FIXME_TAG);
    private static final List<String> fixMeSupplementaryTags = List.of(WaterwayTag.KEY,
            OneWayTag.KEY, BuildingTag.KEY, HighwayTag.KEY, NameTag.KEY, ReferenceTag.KEY,
            PlaceTag.KEY, SurfaceTag.KEY);
    private static final List<String> FIX_ME_SUPPORTED_VALUES_DEFAULT = List.of("verify",
            "position", "resurvey", "Revisar: este punto fue creado por importación directa",
            "continue", "name", "incomplete", "draw geometry and delete this point", "unfinished",
            "recheck");
    private final List<String> fixMeSupportedValues;
    private final HighwayTag minHighwayTag;
    private static final String MIN_HIGHWAY_TAG_DEFAULT = "tertiary";
    private static final String fixMeLowerCase = "fixme";
    private static final String fixMeUpperCase = "FIXME";

    /**
     * instantiate config values
     * @param configuration
     *            the JSON configuration for this check
     */
    public FixMeReviewCheck(final Configuration configuration)
    {

        super(configuration);
        this.fixMeSupportedValues = this.configurationValue(configuration, "fixMe.supported.values",
                FIX_ME_SUPPORTED_VALUES_DEFAULT);
        this.minHighwayTag = Enum.valueOf(HighwayTag.class,
                this.configurationValue(configuration, "min.highway.type", MIN_HIGHWAY_TAG_DEFAULT)
                        .toUpperCase());
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
        final Map<String, String> tags = object.getTags();
        return !isFlagged(object.getOsmIdentifier())
                && (tags.containsKey(fixMeUpperCase) || tags.containsKey(fixMeLowerCase));
    }

    /**
     * This is the function that will check to see whether the object needs to be flagged.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that
     */
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        markAsFlagged(object.getOsmIdentifier());
        final Map<String, String> tags = object.getTags();

        if (this.featureHasSupplementaryTags(tags) && this.featureHasPriorityFixMeValues(tags))
        {
            // If the object is an edge we only care about tertiary (configurable) and above.
            if (object instanceof Edge)
            {
                if (((Edge) object).highwayTag().isMoreImportantThanOrEqualTo(this.minHighwayTag))
                {
                    return Optional
                            .of(this.createFlag(new OsmWayWalker((Edge) object).collectEdges(),
                                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
                }
                return Optional.empty();
            }
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

    /**
     * Function to determining if object's fixme tag contains priority values (configurable values)
     *
     * @param tags
     *            object osm tags
     * @return boolean if the object's fixme tag contains priority values
     */
    private boolean featureHasPriorityFixMeValues(final Map<String, String> tags)
    {
        for (final String priorityTagValue : this.fixMeSupportedValues)
        {
            if ((tags.containsKey(fixMeUpperCase)
                    && tags.get(fixMeUpperCase).equals(priorityTagValue))
                    || (tags.containsKey(fixMeLowerCase)
                            && tags.get(fixMeLowerCase).equals(priorityTagValue)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Function to determine if object tags contain flaggable priority tags (configurable)
     *
     * @param tags
     *            object osm tags
     * @return boolean if the object has flaggable priority tags.
     */
    private boolean featureHasSupplementaryTags(final Map<String, String> tags)
    {
        for (final String supplementaryTag : fixMeSupplementaryTags)
        {
            if (tags.containsKey(supplementaryTag))
            {
                return true;
            }
        }
        return false;
    }

}
