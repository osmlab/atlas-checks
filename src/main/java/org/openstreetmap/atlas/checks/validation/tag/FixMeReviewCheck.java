package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.walker.OsmWayWalker;
import org.openstreetmap.atlas.tags.BuildingTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.PlaceTag;
import org.openstreetmap.atlas.tags.SurfaceTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.tags.names.ReferenceTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.testing.TestAtlas;

/**
 * Auto generated Check template
 *
 * @author v-garei
 */
public class FixMeReviewCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = -2191776278923026805L;
    private static final String HAS_FIXME_TAG = "Object {0, number, #} has 'fixme' tag and needs to be investigated.";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(HAS_FIXME_TAG);
    private static final List<String> fixMeSupplementaryKeys = List.of(BuildingTag.KEY, HighwayTag.KEY, NameTag.KEY, ReferenceTag.KEY, PlaceTag.KEY, SurfaceTag.KEY);

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public FixMeReviewCheck(final Configuration configuration)
    {
        super(configuration);
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
        // by default we will assume all objects as valid
        Map<String,String> tags = object.getTags();

        return tags.containsKey("FIXME") || tags.containsKey("fixme");
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
        Map<String,String> tags = object.getTags();

        if (this.featureHasSupplementaryTags(tags))
        {
//            System.out.println("object tags: " + object.getTags());
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
     * Function to determine if object tags contain flaggable priority tags
     * @param tags object osm tags
     * @return boolean if the object has flaggable priority tags.
     */
    private boolean featureHasSupplementaryTags(final Map<String,String> tags)
    {
        for (final String supplementaryTag : fixMeSupplementaryKeys)
        {
            if (tags.containsKey(supplementaryTag))
            {
                return true;
            }
        }
        return false;
    }
}
