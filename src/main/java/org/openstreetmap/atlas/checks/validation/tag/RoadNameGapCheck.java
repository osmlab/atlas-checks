package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check identifies Edges with no name Tag that are between two other Edges with the same name
 * Tag, OR the Edge has a name Tag but does not equal the name Tag of the Edges that it is between.
 *
 * @author sugandhimaheshwaram
 */
public class RoadNameGapCheck extends BaseCheck
{

    // You can use serialver to regenerate the serial UID.
    private static final long serialVersionUID = 7104778218412127847L;
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList("Edge name is empty.",
            "Edge name {1} is different from in edge name and out edge name.");

    /**
     * Tests if the {@link AtlasObject} has a highway tag that do contain TERTIARY, SECONDARY,
     * PRIMARY, TRUNK, or MOTORWAY
     */
    private static final Predicate<AtlasObject> VALID_HIGHWAY_TAG = object -> Validators
            .hasValuesFor(object, HighwayTag.class)
            && Validators.isOfType(object, HighwayTag.class, HighwayTag.TERTIARY,
                    HighwayTag.PRIMARY, HighwayTag.SECONDARY, HighwayTag.MOTORWAY,
                    HighwayTag.TRUNK);

    /**
     * The default constructor that must be supplied. The Atlas Checks framework will generate the
     * checks with this constructor, supplying a configuration that can be used to adjust any
     * parameters that the check uses during operation.
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public RoadNameGapCheck(final Configuration configuration)
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
        return object instanceof Edge && Edge.isMasterEdgeIdentifier(object.getIdentifier())
                && !HighwayTag.isLinkHighway(object) && VALID_HIGHWAY_TAG.test(object)
                && !JunctionTag.isRoundabout(object);
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
        final Edge edge = (Edge) object;
        final Set<String> inEdgesNameTags = edge.inEdges().stream().filter(
                inEdge -> this.validCheckForObject(inEdge) && NameTag.getNameOf(inEdge).isPresent())
                .map(inEdge -> NameTag.getNameOf(inEdge).get()).collect(Collectors.toSet());
        final Set<String> outEdgesNameTags = edge.outEdges().stream()
                .filter(outEdge -> this.validCheckForObject(outEdge)
                        && NameTag.getNameOf(outEdge).isPresent())
                .map(inEdge -> NameTag.getNameOf(inEdge).get()).collect(Collectors.toSet());

        if (inEdgesNameTags.isEmpty() || outEdgesNameTags.isEmpty())
        {
            return Optional.empty();
        }

        final Set<String> inEdgeOutEdgeMatchingNames = this
                .findInEdgeOutEdgeMatchingName(inEdgesNameTags, outEdgesNameTags);

        // Return empty if there is no pair of in edge and out edge with same name.
        if (inEdgeOutEdgeMatchingNames.isEmpty())
        {
            return Optional.empty();
        }

        // Create flag when we have in edge and out edge with same name but intermediate edge
        // doesn't have a name.
        if (!edge.getName().isPresent())
        {
            return Optional.of(
                    createFlag(object, this.getLocalizedInstruction(0, edge.getOsmIdentifier())));
        }

        // Create flag when in edge and out edge name tag matches and intermediate edge has
        // different tag name.
        final Optional<String> edgeName = edge.getName();
        if (edgeName.isPresent() && !inEdgeOutEdgeMatchingNames.contains(edgeName.get()))
        {
            return Optional.of(createFlag(object,
                    this.getLocalizedInstruction(1, edge.getOsmIdentifier(), edgeName)));
        }
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    private Set<String> findInEdgeOutEdgeMatchingName(final Set<String> inEdgesNameTags,
            final Set<String> outEdgesNameTags)
    {
        final Set<String> inEdgeOutEdgeMatchingNames = new HashSet<>();

        for (final String inEdgeName : inEdgesNameTags)
        {
            for (final String outEdgeName : outEdgesNameTags)
            {
                if (inEdgeName.equals(outEdgeName))
                {
                    inEdgeOutEdgeMatchingNames.add(outEdgeName);
                }
            }
        }
        return inEdgeOutEdgeMatchingNames;
    }
}
