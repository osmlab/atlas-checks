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
* Must be car-navigable
* Must not be part of an area
* Must have not already been flagged


```java
   public boolean validCheckForObject(final AtlasObject object)
       {
           return object instanceof Edge
                   // Check to see that the edge is car navigable
                   && HighwayTag.isCarNavigableHighway(object)
                   // The edge is not part of an area
                   && !object.getTags().containsKey(AREA_KEY)
                   // The edge has not already been seen
                   && !this.isFlagged(object.getIdentifier());
       }

```

After the preliminary filtering of features, we take each Edge and use a series of conditional
statements to validate whether we do in fact want to flag the feature for inspection.

```java
     @Override
        protected Optional<CheckFlag> flag(final AtlasObject object)
        {
            // Get current edge object
            final Edge edge = (Edge) object;
    
            // Get the edge identifier
            final long identifier = edge.getIdentifier();
    
            // Get all Segments in Edge
            final List<Segment> edgeSegments = edge.asPolyLine().segments();
    
            // For each Segment in the Edge
            for (final Segment segment : edgeSegments)
            {
                // Make sure that we aren't flagging duplicate nodes
                if (!segment.length().isGreaterThan(Distance.meters(ZERO_LENGTH))) {
                    continue;
                }
    
                // Check if the Segment is in globalSegments
                if (globalSegments.containsKey(segment))
                {
                    // add identifier to the list of identifiers with that segment
                    globalSegments.get(segment).add(identifier);
    
                    if (!this.isFlagged(edge.getMasterEdgeIdentifier()))
                    {
                        this.markAsFlagged(edge.getMasterEdgeIdentifier());
                        return Optional.of(this.createFlag(edge, this.getLocalizedInstruction(0,
                                edge.getOsmIdentifier())));
                    }
                }
                else
                {
                    // if it doesn't already exist, then add the segment and list with one identifier
                    final Set<Long> identifiers = new HashSet<>();
                    identifiers.add(identifier);
                    globalSegments.put(segment, identifiers);
                }
            }
    
            return Optional.empty();
        }
```

Within the check body, we first check that the Segment is greater than zero meters in length to
ensure that we are not just looking at a duplicated node. Next, we get all Segments in each Edge,
store each unique Segment as a key in a HashMap, and store each Edge which contains that Segment as 
the value. If that Segment already exists in the HashMap (so the list of Edges is greater than 1) 
and the Edge identifier has not already been flagged, we flag the Edge. If that Segment does not 
already contain that key, we add the Segment, Edge identifier key value pair into the HashMap.



To learn more about the code, please look at the comments in the source code for the check.
[DuplicateWaysCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/DuplicateWaysCheck.java)
