package org.openstreetmap.atlas.checks.validation.tag;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.configuration.Configuration;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Flags road segments that have a {@link NameTag} with a different spelling from that of other
 * segments of the same road. This check is primarily meant to catch small errors in spelling, such
 * as a missing letter, letter accent mixups, or capitalization errors.
 *
 * @author seancoulter
 */
public class RoadNameSpellingConsistencyCheck extends BaseCheck<Long>
{
    private static final long serialVersionUID = 1L;

    private static final HighwayTag MINIMUM_NAME_PRIORITY_DEFAULT = HighwayTag.SERVICE;
    private static final double MAXIMUM_SEARCH_DISTANCE_DEFAULT = 500.0;
    private static final List<String> FALLBACK_INSTRUCTIONS = Collections.singletonList(
            "These road segments have spelling inconsistencies. Spellings are: {0}. Examine all flagged road segments to determine the best spelling, and apply this spelling to all of those segments.");
    private final Distance maximumSearchDistance;

    public RoadNameSpellingConsistencyCheck(final Configuration configuration)
    {
        super(configuration);
        this.maximumSearchDistance = this.configurationValue(configuration,
                "distance.search.maximum", MAXIMUM_SEARCH_DISTANCE_DEFAULT, Distance::meters);
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
        return object instanceof Edge
                && ((Edge) object).highwayTag().isMoreImportantThanOrEqualTo(
                        MINIMUM_NAME_PRIORITY_DEFAULT)
                && ((Edge) object).isMainEdge() && !this.isFlagged(object.getIdentifier())
                && ((Edge) object).getName().isPresent();
    }

    /**
     * This is the actual function that will check to see whether the object needs to be flagged.
     * squid:S3655 is suppressed because edges without names are filtered out by the walker.
     *
     * @param object
     *            the atlas object supplied by the Atlas-Checks framework for evaluation
     * @return an optional {@link CheckFlag} object that flags an OSM way for spelling
     *         inconsistencies.
     */
    @Override
    @SuppressWarnings("squid:S3655")
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        // Collect all of the Edges within a search distance, then filter those that have NameTags
        // that are slightly different than edge
        final Set<Edge> inconsistentEdgeSet = new RoadNameSpellingConsistencyCheckWalker(edge,
                this.maximumSearchDistance).collectEdges().stream()
                        .filter(incomingEdge -> !this.isFlagged(incomingEdge.getIdentifier()))
                        .filter(RoadNameSpellingConsistencyCheckWalker
                                .isEdgeWithInconsistentSpelling(edge))
                        .collect(Collectors.toSet());

        // If the Walker found any inconsistent NameTag spellings
        if (!inconsistentEdgeSet.isEmpty())
        {
            inconsistentEdgeSet.add(edge);
            final String startingEdgeName = edge.getName().get();
            final StringBuilder inconsistentSpellingsCommaDelimited = new StringBuilder("\"")
                    .append(startingEdgeName).append("\"");

            // Holds unique flagged edge names
            final HashMap<String, Boolean> nameMap = new HashMap<>();
            nameMap.put(startingEdgeName, true);

            inconsistentEdgeSet.forEach(inconsistentEdge ->
            {
                this.markAsFlagged(inconsistentEdge.getIdentifier());
                final String flaggedInconsistentName = inconsistentEdge.getName().get();
                if (nameMap.get(flaggedInconsistentName) == null)
                {
                    nameMap.put(flaggedInconsistentName, true);
                    inconsistentSpellingsCommaDelimited.append(" , \"")
                            .append(flaggedInconsistentName).append("\"");
                }
            });
            // Mark beginning of flagged edges
            final List<Location> inconsistencyLocations = inconsistentEdgeSet.stream()
                    .map(Edge::start).map(Node::getLocation).collect(Collectors.toList());
            return Optional.of(this.createFlag(inconsistentEdgeSet,
                    this.getLocalizedInstruction(0, inconsistentSpellingsCommaDelimited.toString()),
                    inconsistencyLocations));
        }

        // There are no spelling inconsistencies among the road's segments
        return Optional.empty();
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

}
