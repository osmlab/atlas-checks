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
* Has a `highway` tag that is car navigable
* Has a `lanes` tag
* Does not have a valid `lanes` tag
* Is not an OSM way that has already been flagged

```java
@Override
public boolean validCheckForObject(final AtlasObject object)
{
    return Validators.hasValuesFor(object, LanesTag.class)
            && HighwayTag.isCarNavigableHighway(object) && object instanceof Edge
            && ((Edge) object).isMainEdge() && !this.lanesFilter.test(object)
            && !this.isFlagged(object.getOsmIdentifier());
}
```

The valid objects are then checked for toll booths and flagged if none are found

```java
@Override
protected Optional<CheckFlag> flag(final AtlasObject object)
{
    if (this.isChecked.contains(object.getIdentifier()) || !partOfTollBooth(object))
    {
        this.markAsFlagged(object.getOsmIdentifier());
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
        final HashSet<Edge> connectedInvalidEdges = connectedInvalidLanes(object);

        // check for toll booths
        for (final Edge edge : connectedInvalidEdges)
        {
            for (final Node node : edge.connectedNodes())
            {
                if (Validators.isOfType(node, BarrierTag.class, BarrierTag.TOLL_BOOTH))
                {
                    // If there is a toll booth, mark them so we don't process
                    // items twice unnecessarily, and return true
                    connectedInvalidEdges
                            .forEach(validEdge -> this.markAsFlagged(validEdge.getOsmIdentifier()));
                    return true;
                }
            }
        }
        // If not a toll booth, mark for flagging so they can skip this toll booth check.
        connectedInvalidEdges
                .forEach(invalidEdge -> this.isChecked.add(invalidEdge.getIdentifier()));
        return false;
    }
```

The connected Edges with invalid `lanes` tag values are gathered by testing each successive invalid edge for connected invalid `lanes` edges until no more are found.

```java
private HashSet<Edge> connectedInvalidLanes(final AtlasObject object)
    {
        // Connected edges with lanes tag values not in the lanesFilter
        final HashSet<Edge> connectedEdges = new HashSet<>();
        // Queue of edges to be processed
        final ArrayDeque<Edge> toProcess = new ArrayDeque<>();
        Edge polledEdge;

        // Add original edge
        connectedEdges.add((Edge) object);
        toProcess.add((Edge) object);

        // Get all connected edges with lanes tag values not in the lanesFilter
        while (!toProcess.isEmpty())
        {
            polledEdge = toProcess.poll();
            for (final Edge edge : polledEdge.connectedEdges())
            {
                if (!connectedEdges.contains(edge) && ((Edge) object).isMainEdge()
                        && Validators.hasValuesFor(edge, LanesTag.class)
                        && HighwayTag.isCarNavigableHighway(edge) && !this.lanesFilter.test(edge))
                {
                    toProcess.add(edge);
                    connectedEdges.add(edge);
                }
            }
        }

        return connectedEdges;
    }
```

To learn more about the code, please look at the comments in the source code for the check.  
[InvalidLanesTagCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/InvalidLanesTagCheck.java)