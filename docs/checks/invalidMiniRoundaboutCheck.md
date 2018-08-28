# Invalid Mini Roundabout Check 

In OSM, a mini-roundabout is recorded using the tag `highway=MINI_ROUNDABOUT`. However, many 
intersections are erroneously tagged as mini-roundabouts when they are not truly mini-roundabouts.
This check filters out some of those cases. More specifically, this check flags all Atlas objects that 
meet the following conditions:
 - Is a `Node` object
 - Has the tag `highway=MINI_ROUNDABOUT`
 - Has less than `minimumValence` number of car-navigable connecting edges AND
     - Has exactly 2 connecting edges, and those edges share the same OSM ID, OR
     - The node has neither `direction=CLOCKWISE` nor `direction=ANTICLOCKWISE` values

After performing sensitivity analysis, the best value for `minimumValence` was determined to be 6, 
but this number is configurable in `config/configuration.json`.

#### Live Examples

The node [1092811451](https://www.openstreetmap.org/node/1092811451) is incorrectly flagged as a 
mini-roundabout and should probably be flagged as a `highway=TURNING_LOOP` or 
`highway=TURNING_CIRCLE` instead. 

The node [2367787523](https://www.openstreetmap.org/node/2367787523) has only 5 connecting car-navigable
edges. Satellite imagery confirms that this is not a mini-roundabout and should be reviewed by an editor.

#### Code Review

We take the `minimumValence` value from the configuration file, unless it is not provided. In that 
case, we take `minimumValence=DEFAULT_VALENCE`, which is 6.
```java
public InvalidMiniRoundaboutCheck(final Configuration configuration)
{
    super(configuration);
    this.minimumValence = this.configurationValue(configuration, MINIMUM_VALENCE_KEY,
            DEFAULT_VALENCE);
}
```

In the `validCheckForObject` method, we filter down to only the objects that are `Node`s with the 
`highway=MINI_ROUNDABOUT` tag.

```java
@Override
public boolean validCheckForObject(final AtlasObject object)
{
    return object instanceof Node
            && Validators.isOfType(object, HighwayTag.class, HighwayTag.MINI_ROUNDABOUT);
}
```

Before discussing the `flag` method, there are a couple of helper methods to discuss. The first 
takes a `Node` and returns a `Collection<Edge>` of all edges which are connected to that `Node` and
 are car-navigable.
```java
private Collection<Edge> getCarNavigableEdges(final Node node)
{
    return node.connectedEdges().stream().filter(HighwayTag::isCarNavigableHighway)
            .collect(Collectors.toSet());
}
```

There is also the notion of a turnaround, which is a `Node` that should probably be a `TURNING_LOOP`
or `TURNING_CIRCLE` instead. This is the first case in which we will flag a `Node`
 (exactly two edges that share the same OSM ID). This case is only possible when we have a single 
 bi-directional way connecting to our `Node`, which results in a master edge and its reverse. So,
 this case can only happen when there is one master edge connected to this `Node`, but two edges 
 overall.
```java
private boolean isTurnaround(final Node node, final Collection<Edge> carNavigableEdges)
{
    final long masterEdgeCount = carNavigableEdges.stream().filter(Edge::isMasterEdge).count();
    return masterEdgeCount == 1 && carNavigableEdges.size() == 2;
}
```
`flagNode` is a helper function that flags a node and all of its car-navigable connecting
edges, then returns that flag.
```java
private CheckFlag flagNode(final Node node, final Collection<Edge> edges,
        final String instruction)
{
    final CheckFlag flag = this.createFlag(node, instruction);
    Iterables.stream(edges).forEach(flag::addObject);
    return flag;
}
```

Finally, we can look at `flag` itself. Note that we store `carNavigableEdges` to prevent repeated 
filtering of the set of connecting edges. 
```java
protected Optional<CheckFlag> flag(final AtlasObject object)
{
    final Node node = (Node) object;
    final Collection<Edge> carNavigableEdges = getCarNavigableEdges(node);
    final long valence = carNavigableEdges.size();
    final Optional<CheckFlag> result;
```
We consider the first case, and flag with an appropriate instruction:

```java
    if (isTurnaround(node, carNavigableEdges))
    {
        result = Optional.of(flagNode(node, carNavigableEdges,
                this.getLocalizedInstruction(0, node.getOsmIdentifier(), valence)));
    }
``` 
Then, we consider the second case, and flag with an appropriate instruction:
```java
    else if (!node.containsValue(DIRECTION_KEY, VALID_DIRECTIONS) && valence < minimumValence
            && valence > 0)
    {
        result = Optional.of(flagNode(node, carNavigableEdges,
                this.getLocalizedInstruction(1, node.getOsmIdentifier(), valence)));
    }
``` 
If neither case is met, we don't flag the node.
```java
    else
    {
        result = Optional.empty();
    }
```
Finally, return result.
```java
    return result;
}
```
For more information, see the source code in 
[InvalidMiniRoundaboutCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/InvalidMiniRoundaboutCheck.java).
