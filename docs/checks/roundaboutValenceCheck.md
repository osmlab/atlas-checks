# Roundabout Valence Check

#### Description

This check identifies roundabouts that have an unusual valence. The goal of this check is to flag 
each roundabout with a valence less than 2 or greater than or equal to 10 for further editor inspection.

#### Live Example
TODO
#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).
In OpenStreetMap, roundabouts are [Ways](https://wiki.openstreetmap.org/wiki/Way) classified with
the `junction=roundabout` tag. We’ll use this information to filter our potential flag candidates.

Our first goal is to validate the incoming Atlas Object. We know two things about roundabouts:
* Must be a valid Edge
* Must have `junction=roundabout` tag

```java
    public boolean validCheckForObject(final AtlasObject object)
        {
            // We check that the object is an instance of Edge
            return object instanceof Edge
                    // and that the Edge has not already been marked as flagged
                    && !this.isFlagged(object.getIdentifier())
                    // make sure that the edges are instances of roundabout
                    && JunctionTag.isRoundabout(object);
        }

```

After the preliminary filtering of features, we need to get all the roundabout's edges. Sometimes
roundabouts are drawn as multiple edges so it's important to get all the edges before getting its
valence. This will also prevent us from creating a MapRoulette Challenge for each individual Edge.

Using the [`connectedEdges()`](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java#L55)
function, we can recursively loop through each Edge's connected Edges until each have been either 
marked as flagged, or added to our roundAboutEdges Set.

* *Edge edge* - current edge being analyzed
* *Map<Long, Edge> roundaboutEdges* - Map of Edge ID and associated Edge data

```java
     private void getAllRoundaboutEdges(final Edge edge, final Map<Long, Edge> roundaboutEdges)
        {
            // Initialize a queue to add yet to be processed connected edges to
            final Queue<Edge> queue = new LinkedList<>();
    
            // Mark the current node as visited and enqueue it
            this.markAsFlagged(edge.getIdentifier());
            queue.add(edge);
    
            // As long as the queue is not empty
            while (queue.size() != 0)
            {
                // Dequeue a connected edge and add it to the roundaboutEdges
                final Edge e = queue.poll();
                roundaboutEdges.put(e.getIdentifier(), e);
    
                // Get the edges connected to the edge e as an iterator
                final Iterator<Edge> iterator = e.connectedEdges().iterator();
                while (iterator.hasNext())
                {
                    final Edge connectedEdge = iterator.next();
                    final Long edgeId = connectedEdge.getIdentifier();
    
                    if (JunctionTag.isRoundabout(connectedEdge))
                    {
                        if (!roundaboutEdges.containsKey(edgeId)
                            && edgeId != edge.getIdentifier()
                            && !this.isFlagged(edgeId))
    
                        {
                            this.markAsFlagged(edgeId);
                            queue.add(connectedEdge);
                        }
                    }
                }
            }
        }
```



To learn more about the code, please look at the comments in the source code for the check.
[RoundaboutValenceCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/ultiFeatureRoundaboutCheck.jMava)