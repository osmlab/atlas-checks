# Invalid Lanes Tag Check 

This check flags roads that have invalid `lanes` tag values.

Valid values are configurable. The defaults are:  
`1,1.5,2,3,4,5,6,7,8,9,10`

In OSM, generally, lanes values greater than 10 are incorrect, and no lanes values should have special characters with the exception of 1.5. The lanes value 1.5 is valid, due to people often using this value to signify a narrow two lane road.  
One exception, that is handled, is when there is a toll booth. 

#### Live Examples

1. The way [id:313769043](https://www.openstreetmap.org/way/313769043) has an invalid `lanes` tag value of `2;1`. `lanes` tag values must be numeric. 
2. The way [id:58693335](https://www.openstreetmap.org/way/58693335) has an invalid `lanes` tag value of `20`. Satellite imagery shows this to be a misrepresentation of reality.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes & Relations; in our case, we’re are looking at [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).

Our first goal is to validate the incoming Atlas object. Valid features for this check will satisfy the following conditions:

* Is an Edge
* Has a `highway` tag
* Has a `lanes` tag

```java
@Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return Validators.hasValuesFor(object, LanesTag.class)
                && HighwayTag.highwayTag(object).isPresent() && object instanceof Edge
                && !this.isFlagged(((Edge) object).getOsmIdentifier());
    }
```

The valid objects are then tested against the list of valid `lanes` tag values, and checked for toll booths (see below).  They are flagged if both these items are false.

```java
@Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        if (!this.lanesFilter.test(object)  && !partOfTollBooth(object))
        {
            this.markAsFlagged(((Edge) object).getOsmIdentifier());
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }
```

The test for toll booths checks all connected Edges with invalid `lanes` tag values. This is to ensure the whole toll plaza is properly evaluated.  
If a toll booth is found, all the connected Edges are marked as checked. This prevents unnecessary rechecking of the same toll plaza later. 

```java
private boolean partOfTollBooth(final AtlasObject object)
    {
        // Connected edges with lanes tag values not in the lanesFilter
        final HashSet<Long> connectedEdges = new HashSet<>();
        // Que of edges to be processed
        final ArrayDeque<Edge> toProcess = new ArrayDeque<>();
        boolean tollBooth = false;
        Edge polledEdge;

        // Add original edge
        connectedEdges.add(object.getIdentifier());
        toProcess.add((Edge) object);

        // Get all connected edges with lanes tag values not in the lanesFilter and check for toll
        // booths
        while (!toProcess.isEmpty())
        {
            polledEdge = toProcess.poll();
            for (final Node node : polledEdge.connectedNodes())
            {
                if (Validators.isOfType(node, BarrierTag.class, BarrierTag.TOLL_BOOTH))
                {
                    tollBooth = true;
                }
            }
            for (final Edge edge : polledEdge.connectedEdges())
            {
                if (!connectedEdges.contains(edge.getIdentifier()) && !lanesFilter.test(edge))
                {
                    toProcess.add(edge);
                    connectedEdges.add(edge.getIdentifier());
                }
            }
        }

        // Add to the global set so we don't process items twice unnecessarily
        if (tollBooth)
        {
            this.isChecked.addAll(connectedEdges);
        }

        return tollBooth;
    }
```

To learn more about the code, please look at the comments in the source code for the check.  
[InvalidLanesTagCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/InvalidLanesTagCheck.java)