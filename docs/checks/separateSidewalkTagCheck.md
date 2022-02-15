# Separate Sidewalk Tag Check

#### Description
A check to validate that when sidewalk=* tags are used on a highway that any separately mapped sidewalk(s) are consistent with the highway’s sidewalk tags.

#### Live Example
Please note that examples below might be fixed olready.  
1) [id:42023472](https://www.openstreetmap.org/way/42023472)
2) [id:415202408](https://www.openstreetmap.org/way/415202408)
3) [id:370064399](https://www.openstreetmap.org/way/370064399)
4) [id:684661218](https://www.openstreetmap.org/way/684661218)


#### Code Review
In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines,
Nodes & Relations; in our case, we’re working with [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java) that have Sidewalk tags.
In OpenStreetMap, [Sidewalks](https://wiki.openstreetmap.org/wiki/Key:sidewalk) ma

Our first goal is to validate the incoming Atlas Object.
* Must be a valid main Edge
* Must have not already been flagged
* Must be car navigable
* Must have [Sidewalks](https://wiki.openstreetmap.org/wiki/Key:sidewalk) tags
* Must not be a closed way [OSM-wiki:Closed Way](https://wiki.openstreetmap.org/wiki/Item:Q4669)
* Must not be a [dual carriageway](https://wiki.openstreetmap.org/wiki/Tag:dual_carriageway%3Dyes).
* Must have a certain length (default 20 meters)

Our second goal is to search for [separately mapped Sidewalks](https://wiki.openstreetmap.org/wiki/Tag:highway%3Dfootway) around the Edge midpoint within certain search distance (default 15 meters). We use a [boxAround](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/Location.java#L211)
to gather all sidewalks.

After we found separately mapped sidewalks, we ensure that
* Sidewalk is on the same layer with the highway
* Sidewalk is not crossing the highway
* Sidewalk is not sharing a location with the highway
* Sidewalk is more or less parallel with the highway

Our third goal is to identify the side (left|right) for every separately mapped sidewalk.

After we identified the side, we ensure that tagging of highway sidewalk is consistent with separately mapped sidewalk.      


To learn more about the code, please look at the comments in the source code for the check.
[SeparateSidewalkTagCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/SeparateSidewalkTagCheck.java)