# Roundabout Valence Check

#### Description

This check identifies roundabouts that have an unusual valence. By default, unusual valence is
defined as less than 2 or greater than 14. However, these values are configurable, and can be
adjusted by setting the connections.minimum and connections.maximum in the configuration file.

#### Live Example
The following examples illustrate two cases where the roundabout valence has been deemed unusual.
1) This roundabout [id:5794760](https://www.openstreetmap.org/way/30886531) has been incorrectly
tagged as a roundabout. Because the valence of this roundabout is 1, it should be either labelled
as a turning circle or turning loop depending on the traversability of the center.
2) This roundabout [id:530638487](https://www.openstreetmap.org/way/530638487) has a valence of 11
connected edges, which in this case is sensible but should be verified.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).
In OpenStreetMap, roundabouts are [Ways](https://wiki.openstreetmap.org/wiki/Way) classified with
the `junction=roundabout` tag. We’ll use this information to filter our potential flag candidates.

Our first goal is to validate the incoming Atlas Object. We know two things about roundabouts:
* Must be a valid Edge
* Must have not already been flagged
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
* *Set<Edge> roundaboutEdges* - a Set of Edges in the roundabout

```java
    private Set<Edge> getAllRoundaboutEdges(final Edge edge)
        {
            final Set<Edge> roundaboutEdges = new HashSet<>();
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
    
                // Check to make sure that we are only adding master edges into the set
                if (!e.isMasterEdge())
                {
                    continue;
                }
    
                roundaboutEdges.add(e);
    
                // Get the edges connected to the edge e as an iterator
                final Iterator<Edge> iterator = e.connectedEdges().iterator();
                while (iterator.hasNext())
                {
                    final Edge connectedEdge = iterator.next();
                    final Long edgeId = connectedEdge.getIdentifier();
    
                    if (JunctionTag.isRoundabout(connectedEdge)
                            && !roundaboutEdges.contains(connectedEdge))
    
                    {
                        this.markAsFlagged(edgeId);
                        queue.add(connectedEdge);
                    }
                }
            }
            return roundaboutEdges;
        }
```

Once we have retrieved and stored all edges in the roundabout, we iterate over each one to get
each Edge's connected non-roundabout edges. These connected non-roundabout Edges must also be 
car navigable to avoid counting pedestrian walkways as part of the valence. Once the unique connected
edges have been stored, the total valence is calculated by subtracting the number of roundabout
edges from the number of total connected edges (the non-roundabout connected edges and the 
roundabout edges). If that value is less than the minimum number of connections or greater
than or equal to the maximum number of connections specified in the configuration, we throw a flag.


To learn more about the code, please look at the comments in the source code for the check.
[RoundaboutValenceCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/RoundaboutValenceCheck.java)