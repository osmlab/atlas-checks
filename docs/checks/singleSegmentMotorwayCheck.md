# Single Segment Motorway Check

This check flags Edges that are tagged with highway=motorway and are not connected to any other Edges with the same highway tag.

#### Live Examples

1. Way [id:568803646](https://www.openstreetmap.org/way/568803646) is tagged as motorway, but is only connected to two motorway_links.
2. Way [id:154947124](https://www.openstreetmap.org/way/154947124) is tagged as motorway, and it is connected to a trunk. 
It is also connected to a roundabout tagged as motorway, but this is ignored (see below).

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes, Areas & Relations; in our case, we’re are looking at
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).

This check first validates object by checking that they are main Edges, tagged as motorways, not roundabouts, and their OSM id has not already been flagged.

Next the valid objects have their connected edges checked to see if there are any tagged with motorway that are not roundabouts.
If none are found the edge is flagged.

Roundabouts are excluded from this check because they often act more like highway links. 
Their presence cannot be counted on to indicate the continuation of a highway classification. 

To learn more about the code, please look at the comments in the source code for the check.  
[SingleSegmentMotorwayCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/SingleSegmentMotorwayCheck.java)
