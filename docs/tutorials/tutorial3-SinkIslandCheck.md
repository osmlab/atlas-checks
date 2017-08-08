# Tutorial 3
### How to build a SinkIsland Atlas Check

For reference around general development see [The Development Document](../dev.md), for this tutorial it is assumed
that you have read through that document.

For reference on complete source code see [SinkIslandCheck](../../src/main/java/org/openstreetmap/altas/checks/validation/linear/edges/SinkIslandCheck.java)

#### What is a SinkIsland

A SinkIsland is a navigable road network that cannot be exited once entered, or it potentially could never be
entered at all. A subset of these problems are roads that are created without any connection to the greater road
network.
 
#### Initializing our SinkIslandCheck
The first thing we need to do for our SinkIslandCheck is to initialize it. This will involve building out the
constructor for the class that is required. We will be initializing a single property from the configuration:
- tree.size - This is the value for how large our edge network can become before we decide that it is not a SinkIsland.

We also define our store size, which is the initial size that we set our maps when looking for the sink island. 

```java
public SinkIslandCheck(final Configuration configuration)
{
    super(configuration);
    this.treeSize = configurationValue(configuration, "tree.size", TREE_SIZE_DEFAULT,
            Math::toIntExact);
    this.storeSize = (int) (this.treeSize / LOAD_FACTOR);
}
```

#### Validate the incoming Object
```java
@Override
public boolean validCheckForObject(final AtlasObject object)
{
    return this.validEdge(object) && !this.isFlagged(object.getIdentifier());
}

private boolean validEdge(final AtlasObject object)
{
    return object instanceof Edge
            // Ignore any airport taxiways and runways, as these often create a sinkisland
            && !Validators.isOfType(object, AerowayTag.class, AerowayTag.TAXIWAY,
                    AerowayTag.RUNWAY)
            // Ignore edges that have been waysectioned at the border, as has high probability
            // of creating a false positive due to the sectioning of the way
            && !(SyntheticBoundaryNodeTag.isBoundaryNode(((Edge) object).end())
                    || SyntheticBoundaryNodeTag.isBoundaryNode(((Edge) object).start()))
            // Only allow car navigable highways and ignore ferries
            && HighwayTag.isCarNavigableHighway(object) && !RouteTag.isFerry(object)
            // Ignore any highways tagged as areas
            && !TagPredicates.IS_AREA.test(object);
}
```

We split out validity function into two, the reason being is that we will use the `validEdge` function again in the 
flag function below when we walk the road network. More information on that below. We define a valid edge as the following:
- Does not contain the "aeroway=taxiway" or "aeroway=runway" tag, it is expected that taxiways and runways at airports would not be 
    connected to the greater road network.
- The Node does not contain the synthetic boundary node tag. These tags are added when Atlas files are created
    to define whether an edge was cut at the border or not. If you do not ignore these tags you could get a high number
    of false positives when in actual fact the sink island does connect to a road network, but outside of the current bounds.
- The edge is navigable by a car which is defined as having the highway tag equal to one of the following:
    - motorway
    - trunk
    - primary
    - secondary
    - tertiary
    - unclassified
    - residential
    - service
    - motorway_link
    - trunk_link
    - primary_link
    - secondary_link
    - tertiary_link
    - living_street
    - track
    - road
- The edge is not part of a ferry route. Ie. does not include the tag "route=ferry"
- The edge is not an area. There are a couple of reasons to include "area=yes" with the highway tag, but they
    are not applicable to this check.
    
The other part of the `validCheckForObject` function is the section '!this.isFlagged(object.getIdentifier())' is used so that 
we don't create duplicate flags. So for each edge we walk in the network will will flag it internally so that it is not processed more than once.

#### Generate the Check

The algorithm takes advantage of the capabilities of the Atlas by exploring the outgoing edges of the currently supplied edge and finding 
out whether within a given number of edges you could navigate your way out of the edge network. 

```java
/**
 * Flags the provided object and surrounding edge network if it is found to be a SinkIsland
 *
 * @param object
 *            an edge that is considered a ValidEdge as per the function validEdge
 * @return returns an Optional {@link CheckFlag}, the {@link CheckFlag} will be empty if the
 *         object is not considered part of a SinkIsland network
 */
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
    // current queue of candidates that we can draw from
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
            : Optional.of(createFlag(explored, "Road is impossible to get out of."));
}
```

#### Setup Configuration

This SinkIslandCheck requires only a single extra configuration field. `tree.size` which is described earlier in this
document. 

First we need to modify this [configuration.json](../../config/configuration.json) file. We want to change the following things:
1. On line 7 we want to change the value from true to false. This will disable all checks by default and require
any checks to explicitly be turned on, which we will do in the next step.
2. Below line 8 we are going to add the following code:
```json
{
  "SinkIslandCheck": {
    "enabled": true,
    "tree.size": 50
  }
}
```

The properties for the new check explicitly set it as enabled, and set the tree.size to 50. 50 is the default value
so if you wish to adjust this higher or lower you can do that here.

#### Run your correction

Now that you have built your correction, the next step is simply to run it. And this is all explained [here](../standalone.md).
