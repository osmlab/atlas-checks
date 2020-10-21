package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openstreetmap.atlas.checks.atlas.predicates.TypePredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.*;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Auto generated Check template
 *
 * @author greichenberger
 */
public class TollEscapeCheck extends BaseCheck<Long>
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 1L;
    private static final String EDGE_DEVIATION_INSTRUCTION = "Way {0,number,#} is crude. Please add more nodes/rearrange current nodes to more closely match the road from imagery";
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections
            .singletonList(EDGE_DEVIATION_INSTRUCTION);

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public TollEscapeCheck(final Configuration configuration)
    {
        super(configuration);
        // any internal variables can be set here from configuration
        // eg. MAX_LENGTH could be defined as "public static final double MAX_LENGTH = 100;"
        // this.maxLength = configurationValue(configuration, "length.max", MAX_LENGTH,
        // Distance::meters);
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
        if (TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMainEdge()
                && HighwayTag.isCarNavigableHighway(object)
                && !isFlagged(object.getOsmIdentifier()))
        {
            //
            Edge edgeInQuestion = ((Edge) object).getMainEdge();
            final Map<String, String> keySet = edgeInQuestion.getOsmTags();
            return hasTollTag(keySet) && !isPrivateAccess(keySet);
        }
        return false;
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
        markAsFlagged(object.getOsmIdentifier());
        Edge egdeInQuestion = ((Edge) object).getMainEdge();

        System.out.println("osm ID: " + egdeInQuestion.getOsmIdentifier());
        System.out.println("object osm identifier: " + object.getOsmIdentifier());
        System.out.println("Tags: " + egdeInQuestion.getOsmTags());
//        return Optional.of(this.createFlag(object,
//                this.getLocalizedInstruction(0, object.getOsmIdentifier())));


        return Optional.empty();
    }

    private boolean isPrivateAccess(final Map<String, String> tags)
    {
        return tags.get("access").equals("private");
    }

    private boolean hasTollTag(final Map<String, String> tags)
    {
        return tags.keySet().stream()
                .anyMatch(tag -> tag.equals(TollTag.KEY));
    }
}
