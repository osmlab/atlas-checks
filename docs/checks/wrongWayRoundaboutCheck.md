# Wrong Way Roundabout Check

#### Description
This check flags roundabouts (Edges with tag junction=ROUNDABOUT) where the flow of traffic is in 
the wrong direction. These roundabouts can cause issues by directing vehicles into oncoming traffic.

#### Live Example
1) This roundabout [id:242413354](https://www.openstreetmap.org/way/242413354) has half of its
edges going clockwise. This is incorrect in a right-driving country, and thus should be flagged.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).
In OpenStreetMap, roundabouts are [Ways](https://wiki.openstreetmap.org/wiki/Way) classified with
the `junction=roundabout` tag. We’ll use this information to filter our potential flag candidates.

Our first goal is to validate the incoming Atlas Object. We know two things about roundabouts:
* Must be a valid Edge
* Must have not already been flagged
* Must have `junction=roundabout` tag
* Must not be two way
* Must be master edge

```java
     @Override
         public boolean validCheckForObject(final AtlasObject object)
         {
             // We check that the object is an instance of Edge
             return object instanceof Edge
                     // Make sure that the edges are instances of roundabout
                     && JunctionTag.isRoundabout(object)
                     // Is not two-way
                     && !OneWayTag.isExplicitlyTwoWay(object)
                     // And that the Edge has not already been marked as flagged
                     && !this.isFlagged(object.getIdentifier())
                     // Make sure that we are only looking at master edges
                     && ((Edge) object).isMasterEdge();
         }
```

After the preliminary filtering of features, we need to get all the roundabout's edges in sorted
order because the order of the edges in a roundabout dictate the directionality.

Using the [`connectedEdges()`](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java#L55)
function, we can recursively loop through each Edge's connected Edges until each have been either 
marked as flagged, or added to our roundAboutEdges Set.

```java
    private List<Edge> getAllRoundaboutEdges(final Edge edge)
        {
            final List<Edge> roundaboutEdges = new ArrayList<>();
    
            // Initialize a queue to add yet to be processed connected edges to
            final Queue<Edge> queue = new LinkedList<>();
    
            // Mark the current Edge as visited and enqueue it
            this.markAsFlagged(edge.getIdentifier());
            queue.add(edge);
    
            // As long as the queue is not empty
            while (!queue.isEmpty())
            {
                // Dequeue a connected edge and add it to the roundaboutEdges
                final Edge currentEdge = queue.poll();
    
                roundaboutEdges.add(currentEdge);
    
                // Get the edges connected to the edge e as an iterator
                final Set<Edge> connectedEdges = currentEdge.connectedEdges();
    
                for (final Edge connectedEdge : connectedEdges)
                {
                    final Long edgeId = connectedEdge.getIdentifier();
    
                    if (JunctionTag.isRoundabout(connectedEdge)
                            && !roundaboutEdges.contains(connectedEdge))
    
                    {
                        this.markAsFlagged(edgeId);
                        queue.add(connectedEdge);
                    }
                }
            }
            roundaboutEdges.sort(Edge::compareTo);
            return roundaboutEdges;
        }
```

Once we have all the roundabout's edges in ascending identifier order, we can get the direction. To
get the direction of the roundabout, we make use of the properties of the cross product of the
vectors of two adjacent edges. We calculate the cross product iteratively over all pairs of
adjacent edges until we find one that that yields a cross product not equal to 0. Using the 
right-hand rule, we know that the cross product of two adjacent vectors an orthogonal vector. This
resulting vector will be positive when the roundabout is moving in a clockwise direction, and 
negative when the roundabout is moving in a counterclockwise direction. Iteratively calculating this
cross product until it's non-zero is important because a 0 value cross product does not tell us anything
about directionality. Finally, we return enums that correspond to this logic.

```java
    private static roundaboutDirection findRoundaboutDirection (final List<Edge> roundaboutEdges)
        {
            double crossProduct = 0;
            int firstEdgeIndex = 0;
    
            while (crossProduct == 0 && firstEdgeIndex + 1 < roundaboutEdges.size())
            {
                // Get the Edges to use in the cross product
                final Edge edge1 = roundaboutEdges.get(firstEdgeIndex);
                final Edge edge2 = roundaboutEdges.get(firstEdgeIndex + 1);
    
                // Get the nodes' latitudes and longitudes to use in deriving the vectors
                final double node1Y = edge1.start().getLocation().getLatitude().asDegrees();
                final double node1X = edge1.start().getLocation().getLongitude().asDegrees();
                final double node2Y = edge1.end().getLocation().getLatitude().asDegrees();
                final double node2X = edge1.end().getLocation().getLongitude().asDegrees();
                final double node3Y = edge2.end().getLocation().getLatitude().asDegrees();
                final double node3X = edge2.end().getLocation().getLongitude().asDegrees();
    
                // Get the vectors from node 2 to 1, and node 2 to 3
                final double vector1X = node2X - node1X;
                final double vector1Y = node2Y - node1Y;
                final double vector2X = node2X - node3X;
                final double vector2Y = node2Y - node3Y;
    
                // The cross product tells us the direction of the orthogonal vector, which is
                // Directly related to the direction of rotation/traffic
                crossProduct = (vector1X * vector2Y) - (vector1Y * vector2X);
    
                firstEdgeIndex += 1;
            }
    
            return crossProduct < 0 ? roundaboutDirection.COUNTERCLOCKWISE :
                    (crossProduct > 0) ? roundaboutDirection.CLOCKWISE : roundaboutDirection.UNKNOWN;
        }
```
```java

    public enum roundaboutDirection
        {
            UNKNOWN,
            CLOCKWISE,
            COUNTERCLOCKWISE
        }

```

Using the roundabout's direction and the iso_country_code tag on a given feature, we can determine
whether the roundabout is moving in the incorrect direction. If the roundabout is in a right-driving
country and the traffic is moving in the clockwise direction or if the roundabout is in a left-driving
country and the traffic is moving in the counterclockwise direction then a flag is thrown.

```java
 @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Edge edge = (Edge) object;
        final String isoCountryCode = edge.tag(ISOCountryTag.KEY).toUpperCase();

        // Get all edges in the roundabout
        final List<Edge> roundaboutEdges = getAllRoundaboutEdges(edge);

        // Get the direction of the roundabout
        final roundaboutDirection direction = findRoundaboutDirection(roundaboutEdges);

        // Determine if the roundabout is in a left or right driving country
        final boolean isLeftDriving = LEFT_DRIVING_COUNTRIES.contains(isoCountryCode);

        // If the roundabout traffic is clockwise in a right-driving country, or
        // If the roundabout traffic is counterclockwise in a left-driving country
        if (direction.equals(roundaboutDirection.CLOCKWISE) && !isLeftDriving
                || direction.equals(roundaboutDirection.COUNTERCLOCKWISE) && isLeftDriving)
        {
            return Optional.of(this.createFlag(new HashSet<>(roundaboutEdges),
                    this.getLocalizedInstruction(0, edge.getOsmIdentifier())));
        }
        return Optional.empty();
    }

```

To learn more about the code, please look at the comments in the source code for the check.
[WrongWayRoundaboutCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/WrongWayRoundaboutCheck.java)