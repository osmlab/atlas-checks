package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.FerryTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Based on the osm wiki, when route=ferry exists, there should be a ferry=* tag where the values of
 * ferry are highway values. This check flags all edges that have both {@link HighwayTag} and
 * {@link RouteTag} value equal to "FERRY".
 *
 * @author sayas01
 */
public class HighwayToFerryTagCheck extends BaseCheck
{
    private static final String MINIMUM_HIGHWAY_TYPE_DEFAULT = HighwayTag.PATH.toString();
    private static final String FERRY_TAG_IF_SAME_AS_HIGHWAY_INSTRUCTION = "This way {0,number,#} has a Ferry and a Highway tag for a ferry route. "
            + "Please verify and remove the highway tag.";
    private static final String FERRY_TAG_IF_ABSENT_INSTRUCTION = "This way {0,number,#} has a Highway tag for a ferry route instead of a Ferry tag. "
            + "Please verify and add a Ferry tag with the Highway tag value and remove the highway tag.";
    private static final String FERRY_TAG_IF_DIFFERENT_FROM_HIGHWAY_INSTRUCTION = "This way {0,number,#} has a Ferry and a Highway tag for a ferry route. "
            + "Please verify and update the Ferry tag with the Highway tag value and remove the highway tag.";
    private static final Predicate<Taggable> HAS_FERRY_TAG = object -> Validators
            .hasValuesFor(object, FerryTag.class);

    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            FERRY_TAG_IF_DIFFERENT_FROM_HIGHWAY_INSTRUCTION,
            FERRY_TAG_IF_SAME_AS_HIGHWAY_INSTRUCTION, FERRY_TAG_IF_ABSENT_INSTRUCTION);

    private final HighwayTag minimumHighwayType;

    /**
     * Default constructor
     *
     * @param configuration
     *            {@link Configuration} required to construct any Check
     */
    public HighwayToFerryTagCheck(final Configuration configuration)
    {
        super(configuration);
        final String highwayType = (String) this.configurationValue(configuration,
                "highway.type.minimum", MINIMUM_HIGHWAY_TYPE_DEFAULT);
        this.minimumHighwayType = Enum.valueOf(HighwayTag.class, highwayType.toUpperCase());
    }

    /**
     * Checks to see whether the supplied object class type is valid for this particular check
     *
     * @param object
     *            The {@link AtlasObject} you are checking
     * @return true if it is
     */
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMasterEdge()
                && Validators.isOfType(object, RouteTag.class, RouteTag.FERRY)
                && this.isMinimumHighwayType(object) && !this.isFlagged(object.getOsmIdentifier());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Mark OSM id as flagged
        this.markAsFlagged(object.getOsmIdentifier());
        final boolean hasHighwayClassification = this.hasSameClassificationAsHighwayTag(object);
        final boolean hasFerryTag = HAS_FERRY_TAG.test(object);

        // If the object has a Ferry Tag and is not of the same classification as the Highway Tag
        if (hasFerryTag && !hasHighwayClassification)
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        // If the object has a Ferry Tag and is of the same classification as the Highway Tag
        else if (hasFerryTag && hasHighwayClassification)
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(1, object.getOsmIdentifier())));
        }
        // If the object has no Ferry Tag
        else
        {
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(2, object.getOsmIdentifier())));
        }
    }

    /**
     * Verifies that the priority of the {@link HighwayTag} of the {@link AtlasObject} is greater
     * than or equal to the minimum highway type.
     *
     * @param object
     *            {@link AtlasObject} whose {@link HighwayTag} is verified for minimum highway value
     * @return true if the highway tag of the object is greater or equal to the minimum highway type
     */
    private boolean isMinimumHighwayType(final AtlasObject object)
    {
        final Optional<HighwayTag> highwayTagOfObject = HighwayTag.highwayTag(object);

        return highwayTagOfObject.isPresent()
                && highwayTagOfObject.get().isMoreImportantThanOrEqualTo(this.minimumHighwayType);
    }

    /**
     * Verifies if the {@link FerryTag} value is the same as the {@link HighwayTag} value
     *
     * @param object
     *            {@link AtlasObject} whose tag values need to be verified
     * @return true if the {@link FerryTag} value is the same as the {@link HighwayTag} value
     */
    private boolean hasSameClassificationAsHighwayTag(final AtlasObject object)
    {
        return object.getTag(FerryTag.KEY).equals(object.getTag(HighwayTag.KEY));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }
}
