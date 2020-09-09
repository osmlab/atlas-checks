package org.openstreetmap.atlas.checks.validation.intersections;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNode;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNodeFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.RestrictedPath;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * Flag any BigNodes that have may have some bad data. Approach: Flag any BigNodes where either -
 * the number of paths through the big node are over a threshold - the number of junction nodes are
 * over a threshold. The BigNode is first filtered based on the highway types of the connected
 * edges. The highway types of the connected edges should be within the minimum amd maximum highway
 * types set in the configuration.
 *
 * @author cameronfrenette
 */
public class BigNodeBadDataCheck extends BaseCheck<Long>
{
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays.asList(
            "This complex intersection has too many junction edges ({0}).",
            "This complex intersection has too many paths ({0}).  "
                    + "There are {1,number,#} restricted paths and their OSM ids are: {2} ");
    private static final long MAX_NUMBER_JUNCTION_EDGES_THRESHOLD_DEFAULT = 12;
    private static final long MAX_NUMBER_PATHS_THRESHOLD_DEFAULT = 1000;
    private static final String MINIMUM_HIGHWAY_TYPE_DEFAULT = HighwayTag.TOLL_GANTRY.toString();
    private static final String MAXIMUM_HIGHWAY_TYPE_DEFAULT = HighwayTag.MOTORWAY.toString();
    private static final long serialVersionUID = 6311899117562612121L;
    private final long maxNumberJunctionEdgesThreshold;
    private final long maxNumberPathsThreshold;
    private final HighwayTag minimumHighwayType;
    private final HighwayTag maximumHighwayType;

    public BigNodeBadDataCheck(final Configuration configuration)
    {
        super(configuration);
        this.maxNumberPathsThreshold = configurationValue(configuration,
                "max.number.paths.threshold", MAX_NUMBER_PATHS_THRESHOLD_DEFAULT);
        this.maxNumberJunctionEdgesThreshold = configurationValue(configuration,
                "max.number.junction.edges.threshold", MAX_NUMBER_JUNCTION_EDGES_THRESHOLD_DEFAULT);
        this.minimumHighwayType = this.configurationValue(configuration, "highway.type.minimum",
                MINIMUM_HIGHWAY_TYPE_DEFAULT,
                configValue -> HighwayTag.valueOf(configValue.toUpperCase()));
        this.maximumHighwayType = this.configurationValue(configuration, "highway.type.maximum",
                MAXIMUM_HIGHWAY_TYPE_DEFAULT,
                configValue -> HighwayTag.valueOf(configValue.toUpperCase()));
    }

    @Override
    public Optional<BigNodeFinder> finder()
    {
        return Optional.of(new BigNodeFinder());
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof BigNode;
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final BigNode bigNode = (BigNode) object;
        if (this.containsNonPriorityHighways(bigNode))
        {
            return Optional.empty();
        }

        final int junctionEdgesCount = bigNode.junctionEdges().size();
        if (junctionEdgesCount > maxNumberJunctionEdgesThreshold)
        {
            final CheckFlag flag = createFlag(bigNode,
                    this.getLocalizedInstruction(0, junctionEdgesCount));

            bigNode.edges().forEach(flag::addObject);
            // Add map pin at each BigNode Node
            bigNode.nodes().forEach(b -> flag.addPoint(b.getLocation()));

            return Optional.of(flag);
        }
        else
        {
            final int allPathsCount = bigNode.allPaths().size();
            final Set<RestrictedPath> turnRestrictions = bigNode.turnRestrictions();
            final int turnRestrictionCount = turnRestrictions.size();
            // Get list of restricted path OSM ids
            final List<Long> restrictedPathOsmIds = turnRestrictions.stream()
                    .flatMap(restrictedPath -> Iterables.asList(restrictedPath.getRoute()).stream()
                            .map(Edge::getOsmIdentifier))
                    .collect(Collectors.toList());

            if (allPathsCount > maxNumberPathsThreshold)
            {
                final CheckFlag flag = createFlag(bigNode, this.getLocalizedInstruction(1,
                        allPathsCount, turnRestrictionCount, restrictedPathOsmIds.toString()));

                bigNode.edges().forEach(flag::addObject);
                // Add map pin at each BigNode Node
                bigNode.nodes().forEach(b -> flag.addPoint(b.getLocation()));

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

    /**
     * This method checks if the {@link HighwayTag} of the edges forming the {@link BigNode} are
     * within configured minimum and maximum highway types
     *
     * @param bigNode
     *            any {@link BigNode}
     * @return true if the {@link HighwayTag} of the edges of the {@link BigNode} is within the
     *         minimum and maximum highway types
     */
    private boolean containsNonPriorityHighways(final BigNode bigNode)
    {
        return bigNode.edges().stream()
                .anyMatch(edge -> !(HighwayTag.highwayTag(edge).isPresent()
                        && HighwayTag.highwayTag(edge).get()
                                .isMoreImportantThanOrEqualTo(this.minimumHighwayType)
                        && HighwayTag.highwayTag(edge).get()
                                .isLessImportantThanOrEqualTo(this.maximumHighwayType)));
    }
}
