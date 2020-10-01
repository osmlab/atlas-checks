# Roundabout Valence Check

#### Description

This check identifies roundabouts that have an unusual valence.
The valence can be for the entire roundabout or any of its intersections.
By default, unusual overall valence is
defined as less than 2. However, this value is configurable, and can be
adjusted by setting connections.minimum in the configuration file.
Each intersection in a roundabout should only ever have one or zero connections to outside roads. 

#### Live Example
1) This roundabout [id:5794760](https://www.openstreetmap.org/way/30886531) has been incorrectly
tagged as a roundabout. Because the valence of this roundabout is 1, it should be either labelled
as a turning circle or turning loop depending on the traversability of the center.
2) This roundabout [id:182781989](https://www.openstreetmap.org/way/182781989) has multiple connections at single nodes.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).
In OpenStreetMap, roundabouts are [Ways](https://wiki.openstreetmap.org/wiki/Way) classified with
the `junction=roundabout` tag. We’ll use this information to filter our potential flag candidates.

Our first goal is to validate the incoming Atlas Object. We know two things about roundabouts:
* Must be a valid main Edge
* Must have not already been flagged
* Must have `junction=roundabout` tag
* Must be car navigable

After the preliminary filtering of features, we need to get all the roundabout's edges. Sometimes
roundabouts are drawn as multiple edges so it's important to get all the edges before getting its
valence. This will also prevent us from creating a MapRoulette Challenge for each individual Edge.

We use a
[SimpleEdgeWalker](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/walker/SimpleEdgeWalker.java)
to gather all roundabout Edges that are connected to the original Edge. 

Once we have retrieved and stored all edges in the roundabout, we iterate over the connected Nodes to get the count of
each Node's connected non-roundabout edges. These connected non-roundabout Edges must also be 
car navigable to avoid counting pedestrian walkways as part of the valence. 
If any Node has more than one connection the roundabout is flagged, 
as this can affect routing (see the second to last bullet under 'How to Map': [OSM-wiki:junction=Rrundabout](https://wiki.openstreetmap.org/wiki/Tag:junction%3Droundabout)).
Otherwise the valence of each Node is summed to get the total. If the total is less than the configured minimum (default 2), it is flagged. 


To learn more about the code, please look at the comments in the source code for the check.
[RoundaboutValenceCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/RoundaboutValenceCheck.java)