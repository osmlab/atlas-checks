package org.openstreetmap.atlas.checks.validation.linear.edges;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.checks.atlas.predicates.TagPredicates;
import org.openstreetmap.atlas.checks.base.BaseCheck;
import org.openstreetmap.atlas.checks.flag.CheckFlag;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.tags.AerowayTag;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.SyntheticBoundaryNodeTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.configuration.Configuration;

/**
 * This check flags islands of roads where it is impossible to get out. The simplest is a one-way
 * that dead-ends; that would be a one-edge island.
 *
 * @author matthieun
 * @author cuthbertm
 * @author gpogulsky
 */
public class SinkIslandCheck extends BaseCheck<Long>
{
    public static final float LOAD_FACTOR = 0.8f;
    public static final long TREE_SIZE_DEFAULT = 50;
    private static final List<String> FALLBACK_INSTRUCTIONS = Arrays
            .asList("Road is impossible to get out of.");
    private static final long serialVersionUID = -1432150496331502258L;
    private final int storeSize;
    private final int treeSize;

    /**
     * Default constructor
     *
     * @param configuration
     *            the JSON configuration for this check
     */
    public SinkIslandCheck(final Configuration configuration)
    {
        super(configuration);
        this.treeSize = configurationValue(configuration, "tree.size", TREE_SIZE_DEFAULT,
                Math::toIntExact);

        // LOAD_FACTOR 0.8 gives us default initial capacity 50 / 0.8 = 62.5
        // map & queue will allocate 64 (the nearest power of 2) for that initial capacity
        // Our algorithm does not allow neither explored set nor candidates queue exceed
        // this.treeSize
        // Therefore underlying map/queue we will never re-double the capacity
        this.storeSize = (int) (this.treeSize / LOAD_FACTOR);
    }

    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return this.validEdge(object) && !this.isFlagged(object.getIdentifier());
    }

    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        // Flag to keep track of whether we found an issue or not
        boolean emptyFlag = false;

        // The current edge to be explored
        Edge candidate = (Edge) object;

        // A set of all edges that we have already explored
        final Set<AtlasObject> explored = new HashSet<>(this.storeSize, LOAD_FACTOR);
        // A set of all edges that we explore that have no outgoing edges
        final Set<AtlasObject> terminal = new HashSet<>();
        // Current queue of candidates that we can draw from
        final Queue<Edge> candidates = new ArrayDeque<>(this.storeSize);

        // Start edge always explored
        explored.add(candidate);

        // Keep looping while we still have a valid candidate to explore
        while (candidate != null)
        {
            // If the edge has already been flagged by another process then we can break out of the
            // loop and assume that whether the check was a flag or not was handled by the other
            // process
            if (this.isFlagged(candidate.getIdentifier()))
            {
                emptyFlag = true;
                break;
            }

            // Retrieve all the valid outgoing edges to explore
            final Set<Edge> outEdges = candidate.outEdges().stream().filter(this::validEdge)
                    .collect(Collectors.toSet());

            if (outEdges.isEmpty())
            {
                // Sink edge. Don't mark the edge explored until we know how big the tree is
                terminal.add(candidate);
            }
            else
            {
                // Add the current candidate to the set of already explored edges
                explored.add(candidate);

                // From the list of outgoing edges from the current candidate filter out any edges
                // that have already been explored and add all the rest to the queue of possible
                // candidates
                outEdges.stream().filter(outEdge -> !explored.contains(outEdge))
                        .forEach(candidates::add);

                // If the number of available candidates and the size of the currently explored
                // items is larger then the configurable tree size, then we can break out of the
                // loop and assume that this is not a SinkIsland
                if (candidates.size() + explored.size() > this.treeSize)
                {
                    emptyFlag = true;
                    break;
                }
            }

            // Get the next candidate
            candidate = candidates.poll();
        }

        // If we exit due to tree size (emptyFlag == true) and there are terminal edges we could
        // cache them and check on entry to this method. However it seems to happen too rare in
        // practice. So these edges (if any) will be processed as all others. Even though they would
        // not generate any candidates. Otherwise if we covered the whole tree, there is no need to
        // delay processing of terminal edges. We should add them to the geometry we are going to
        // flag.
        if (!emptyFlag)
        {
            // Include all touched edges
            explored.addAll(terminal);
        }

        // Set every explored edge as flagged for any other processes to know that we have already
        // process all those edges
        explored.forEach(marked -> this.markAsFlagged(marked.getIdentifier()));

        // Create the flag if and only if the empty flag value is not set to false
        return emptyFlag ? Optional.empty()
                : Optional.of(createFlag(explored, this.getLocalizedInstruction(0)));
    }

    @Override
    protected List<String> getFallbackInstructions()
    {
        return FALLBACK_INSTRUCTIONS;
    }

    /**
     * This function will check various elements of the edge to make sure that we should be looking
     * at it.
     *
     * @param object
     *            the edge to check whether we want to continue looking at it
     * @return {@code true} if is a valid object to look at
     */
    private boolean validEdge(final AtlasObject object)
    {
        return object instanceof Edge
                // Ignore any airport taxiways and runways, as these often create a sink island
                && !Validators.isOfType(object, AerowayTag.class, AerowayTag.TAXIWAY,
                        AerowayTag.RUNWAY)
                // Ignore edges that have been way sectioned at the border, as has high probability
                // of creating a false positive due to the sectioning of the way
                && !(SyntheticBoundaryNodeTag.isBoundaryNode(((Edge) object).end())
                        || SyntheticBoundaryNodeTag.isBoundaryNode(((Edge) object).start()))
                // Only allow car navigable highways and ignore ferries
                && HighwayTag.isCarNavigableHighway(object) && !RouteTag.isFerry(object)
                // Ignore any highways tagged as areas
                && !TagPredicates.IS_AREA.test(object);
    }
}
