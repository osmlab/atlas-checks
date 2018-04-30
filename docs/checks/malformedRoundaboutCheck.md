# Malformed Roundabout Check

#### Description
This check flags roundabouts where:
1. the directionality is opposite to what it should be (for example, a counterclockwise roundabout in a right-driving country), where
2. the segments are  multi-directional, or
3. the incorrect geometry (concave)

#### Live Example
1) This roundabout [id:242413354](https://www.openstreetmap.org/way/242413354) is multi-directional and
has some segment going the wrong way. This is incorrect and should be flagged.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).
In OpenStreetMap, roundabouts are [Ways](https://wiki.openstreetmap.org/wiki/Way) classified with
the `junction=roundabout` tag. We’ll use this information to filter our potential flag candidates.

Our first goal is to validate the incoming Atlas Object. We know two things about roundabouts:
* Must be a valid Edge
* Must have an `iso_country_code` tag
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
                     // Make sure that the object has an iso_country_code
                     && object.getTag(ISOCountryTag.KEY).isPresent()
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
function, we loop through each Edge's connected Edges until each have been either marked as flagged,
or added to our roundAboutEdges Set.

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
find the direction of the roundabout, we get the cross product of every set of adjacent edges in the
roundabout. Using the cross product and the [right-hand rule](https://en.wikipedia.org/wiki/Right-hand_rule),
we get the vector which is orthogonal to each pair of adjacent vectors. 
```java
    private static Double getCrossProduct(final Edge edge1, final Edge edge2)
        {
    
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
            return (vector1X * vector2Y) - (vector1Y * vector2X);
        }
```

A positive cross product carried over all edge pairs indicates that the roundabout is going clockwise, and a negative cross-product carried over all edge pairs
indicates that the roundabout is going counterclockwise. However, because we are comparing the directionality
over all edge pairs, we are also able to check for a multi-directional roundabout (meaning that there are
both clockwise and counterclockwise segments). We return a RoundaboutDirection enum from this
findRoundaboutDirection method and handle it in the flag method.

```java
    private static RoundaboutDirection findRoundaboutDirection (final List<Edge> roundaboutEdges)
        {
            // Initialize the directionSoFar to UNKNOWN as we have no directional information yet
            RoundaboutDirection directionSoFar = RoundaboutDirection.UNKNOWN;
    
            for (int i = 0; i < roundaboutEdges.size(); i++) {
                // Get the Edges to use in the cross product
                final Edge edge1 = roundaboutEdges.get(i);
                // We mod the roundabout edges here so that we can get the last pair of edges in the
                // Roundabout correctly
                final Edge edge2 = roundaboutEdges.get((i + 1) % roundaboutEdges.size());
    
                // Get the cross product and then the direction of the roundabout
                double crossProduct = getCrossProduct(edge1, edge2);
                RoundaboutDirection direction = crossProduct < 0 ? RoundaboutDirection.COUNTERCLOCKWISE :
                        (crossProduct > 0) ? RoundaboutDirection.CLOCKWISE : RoundaboutDirection.UNKNOWN;
    
                // If the direction is UNKNOWN then we continue to the next iteration because we do not
                // Have any new information about the roundabout's direction
                if(direction.equals(RoundaboutDirection.UNKNOWN)) {
                    continue;
                }
    
                // If the directionSoFar is UNKNOWN, and the direction derived from the current pair
                // Of edges is not UNKNOWN, make the directionSoFar equal to the current pair direction
                if (directionSoFar.equals(RoundaboutDirection.UNKNOWN)) {
                    directionSoFar = direction;
                }
                // Otherwise, if the directionSoFar and the direction are not equal, we know that the
                // Roundabout has segments going in different directions
                else if (!directionSoFar.equals(direction)){
                    return RoundaboutDirection.MULTIDIRECTIONAL;
                }
            }
            return directionSoFar;
        }
```java

    public enum roundaboutDirection
        {
            CLOCKWISE,
            COUNTERCLOCKWISE,
            MULTIDIRECTIONAL,
            UNKNOWN
        }
```

Using the roundabout's direction and the iso_country_code tag on a given feature, we can determine
whether the roundabout is moving in the incorrect direction. If the roundabout is in a right-driving
country and the traffic is moving in the clockwise direction or if the roundabout is in a left-driving
country and the traffic is moving in the counterclockwise direction then a flag is thrown.
If the findRoundaboutDirection method found that the roundabout was multi-directional, then we flag that
as well.

```java
 @Override
     protected Optional<CheckFlag> flag(final AtlasObject object)
     {
         final Edge edge = (Edge) object;
         final String isoCountryCode = edge.tag(ISOCountryTag.KEY).toUpperCase();
 
         // Get all edges in the roundabout
         final List<Edge> roundaboutEdges = getAllRoundaboutEdges(edge);
 
         // Get the direction of the roundabout
         final RoundaboutDirection direction = findRoundaboutDirection(roundaboutEdges);
 
         // Determine if the roundabout is in a left or right driving country
         final boolean isLeftDriving = LEFT_DRIVING_COUNTRIES.contains(isoCountryCode);
 
         // If the roundabout is found to be going in multiple directions
         if (direction.equals(RoundaboutDirection.MULTIDIRECTIONAL)) {
             return Optional.of(this.createFlag(new HashSet<>(roundaboutEdges),
                     this.getLocalizedInstruction(1, edge.getOsmIdentifier())));
         }
         // If the roundabout traffic is clockwise in a right-driving country, or
         // If the roundabout traffic is counterclockwise in a left-driving country
         if (direction.equals(RoundaboutDirection.CLOCKWISE) && !isLeftDriving
                 || direction.equals(RoundaboutDirection.COUNTERCLOCKWISE) && isLeftDriving)
         {
             return Optional.of(this.createFlag(new HashSet<>(roundaboutEdges),
                     this.getLocalizedInstruction(0, edge.getOsmIdentifier())));
         }
 
         return Optional.empty();
     }

```

To learn more about the code, please look at the comments in the source code for the check.
[MalformedRoundaboutCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/MalformedRoundaboutCheck.java)