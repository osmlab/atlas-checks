# Duplicate Ways Check

#### Description

This check identifies Edges which have duplicates. Each Segment in each Edge is checked to identify 
if the Edge is at all duplicated. The Edge is flagged if any Segment duplicated - this includes
partially and completely duplicated Edges.

#### Live Example
The following examples illustrate cases where Edges have duplicates.
1) This edge [id:536929208](https://www.openstreetmap.org/way/536929208) should be flagged as a
result of it being a partially duplicated Edge.
2) This edge [id:24628387](https://www.openstreetmap.org/way/24628387) should be flagged as a result
of it being a partially duplicated Edge.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).
We’ll use this information to filter our potential flag candidates.

Our first goal is to validate the incoming Atlas Object. We know two things about roundabouts:
* Must be a valid Edge
* Must be a master Edge
* Must be car-navigable
* Must not be part of an area
* Must have not already been flagged


```java
   @Override
       public boolean validCheckForObject(final AtlasObject object)
       {
           return object instanceof Edge
                   && ((Edge) object).isMasterEdge()
                   // Check to see that the edge is car navigable
                   && HighwayTag.isCarNavigableHighway(object)
                   // The edge is not part of an area
                   && !object.getTags().containsKey(AREA_KEY)
                   // The edge has not already been seen
                   && !this.isFlagged(((Edge) object).getMasterEdgeIdentifier());
       }
```

After the preliminary filtering of features, we take each Edge and use a series of conditional
statements to validate whether we do in fact want to flag the feature for inspection.

```java
    @Override
       protected Optional<CheckFlag> flag(final AtlasObject object)
       {
           final Set<Segment> allEdgeSegments = new HashSet<>();
           final Edge edge = (Edge) object;
   
           final Rectangle bounds = edge.asPolyLine().bounds();
           // Get Edges which are contained by or intersect the bounds, and then filter
           // Out the non-master Edges as the bounds Edges are not guaranteed to be uni-directional
           final Iterable<Edge> edgesInBounds = Iterables.stream(edge.getAtlas().edgesIntersecting(bounds))
                   .filter(Edge::isMasterEdge);
   
           for (final Edge edgeInBounds : edgesInBounds)
           {
               // If the Edge found in the bounds has an area tag or if the Edge has a length of 0
               // Or if the Edge has already been flagged before then continue because we don't want to
               // Flag area Edges or duplicate Nodes
               if (edgeInBounds.getTags().containsKey(AREA_KEY)
                       || edgeInBounds.asPolyLine().length().equals(Distance.meters(ZERO_LENGTH))
                       || this.isFlagged(edgeInBounds.getMasterEdgeIdentifier()))
               {
                   continue;
               }
   
               // If the Set of Edges does not contain the Edge found in the bounds
               if (!allEdgeSegments.containsAll(edgeInBounds.asPolyLine().segments()))
               {
                   final List<Segment> edgeInBoundsSegments = edgeInBounds.asPolyLine().segments();
   
                   for (final Segment segment : edgeInBoundsSegments)
                   {
                       // If a Segment in the Edge is found (check for partial duplication)
                       if (allEdgeSegments.contains(segment))
                       {
                           this.markAsFlagged(edgeInBounds.getMasterEdgeIdentifier());
                           return Optional.of(this.createFlag(edgeInBounds,
                                   this.getLocalizedInstruction(0, edgeInBounds.getOsmIdentifier())));
                       }
                   }
                   // Add all Segments flattened to the Set of Segments
                   allEdgeSegments.addAll(edgeInBounds.asPolyLine().segments());
               }
               // A full duplicate Edge was found in the Set of allEdgePolyLines
               else
               {
                   this.markAsFlagged(edgeInBounds.getMasterEdgeIdentifier());
                   return Optional.of(this.createFlag(edgeInBounds,
                           this.getLocalizedInstruction(0, edgeInBounds.getOsmIdentifier())));
               }
           }
           return Optional.empty();
       }
```

To learn more about the code, please look at the comments in the source code for the check.
[DuplicateWaysCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/DuplicateWaysCheck.java)
