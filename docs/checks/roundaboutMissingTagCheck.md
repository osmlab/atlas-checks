# Roundabout Missing Tag Check

#### Description
This check identifies closed navigable ways https://wiki.openstreetmap.org/wiki/Item:Q4669 
that have a round shape and intersecting with at least two navigable ways.    

#### Live Example
Roundabout without `junction=roundabout` tag.
1) [id:592687817](https://www.openstreetmap.org/way/592687817) 
2) [id:548529232](https://www.openstreetmap.org/way/548529232)
3) [id:150664424](https://www.openstreetmap.org/way/150664424)
4) [id:287957529](https://www.openstreetmap.org/way/287957529)

#### Code Review
In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).
In OpenStreetMap, roundabouts are [Ways](https://wiki.openstreetmap.org/wiki/Way) classified with
the `junction=roundabout` tag. We’ll identify ways that are roundabouts without `junction=roundabout` tag.

Our first goal is to validate the incoming Atlas Object.
* Must be a first section of valid main Edge
* Must have not already been flagged
* Must be car navigable
* Must have not `junction` and `area` tag
* Must be a closed way [OSM-wiki:Closed Way](https://wiki.openstreetmap.org/wiki/Item:Q4669)
* Must intersect at minimum with two navigable ways
* Must not be Turning Circle [OSM-wiki:turning_circle](https://wiki.openstreetmap.org/wiki/Tag:highway%3Dturning_circle) nor Turning Loop [OSM-wiki:turning_loop](https://wiki.openstreetmap.org/wiki/Tag:highway%3Dturning_loop)   

Our second goal is to exclude pedestrian, private and under construction edges with "tags.filter" 

After the preliminary filtering of features we need to rebuild original OSM way geometry.
Ones we have rebuilt the geometry, we will measure angles between each [Nodes](https://wiki.openstreetmap.org/wiki/Node)
ensuring that roundabout candidate is round shaped within a threshold.   

We use a
[OsmWayWalker](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/walker/OsmWayWalker.java)
to gather all sectioned Edges that are connected to the original Edge. 


To learn more about the code, please look at the comments in the source code for the check.
[RoundaboutMissingTag.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/RoundaboutMissingTagCheck.java)