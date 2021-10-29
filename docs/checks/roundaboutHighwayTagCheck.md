# Roundabout Valence Check

#### Description

This check identifies roundabouts that have an unusual valence.
The valence can be for the entire roundabout or any of its intersections.
By default, unusual overall valence is
defined as less than 2. However, this value is configurable, and can be
adjusted by setting connections.minimum in the configuration file.
Each intersection in a roundabout should only ever have one or zero connections to outside roads. 

#### Live Example
1) This roundabout [id:127882850](https://www.openstreetmap.org/way/127882850) has incorrect highway tag.
2) This roundabout [id:56007062](https://www.openstreetmap.org/way/56007062) has incorrect highway tag.

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

Once we have retrieved and stored all edges in the roundabout, we iterate over the connected Nodes to get the set of highway edges connected to these Nodes that are not part of the roundabout. These connected non-roundabout Edges must also be car navigable. 
If the highest level of Highway Tag in these external edges is higher than the highway=* tag in the roundabout, this roundabout is flagged, 

To learn more about the code, please look at the comments in the source code for the check.
[RoundaboutHighwayTagCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/RoundaboutHighwayTagCheck.java)