# Shadow Detection Check

This check flags buildings that are floating in 3D, casting abnormal shadows on the base map when rendered.

In OSM, 3D buildings are achieved by assigning `height` and/or `level` tags to ways or relations that have a `building` or `building:part` tag.
The [Simple 3D Buildings](https://wiki.openstreetmap.org/wiki/Simple_3D_buildings) osm wiki page documents much of this tagging scheme.
Important to this check is the use of `building:min_level` and `min_height` tags to define the bottom z level of a part of a building.
When improperly used these tags will cause building parts to become disconnected and float.  

#### Live Examples

1. Way [id:260580125](https://www.openstreetmap.org/way/260580125) has a `building:min_level` tag that is > 0, but their is nothing below it to connect it to the ground.
2. Way [id:462577211](https://www.openstreetmap.org/way/462577211) has a `building:min_level` tag of 5, but the part below it, 
way [id:462577208](https://www.openstreetmap.org/way/462577208), has a `level` tag of 4. This causes half the building to float one level above the other half.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes, Areas & Relations; in our case, we’re are looking at
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java), and
[Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java).

The first process in this check is to validate incoming objects, to see if they have the potential to be floating building parts.  
This is done by testing that each object:

* is an Area or multipolygon Relation
* has a `building` or `building:part` tag
* has a `min_height` or `building:min_level` tag

Once an object has been validated it has its `min_height` or `building:min_level` tag checked to see if it is > 0, indicating it is off the ground.
If it is, a BFS walker is used to find connected building parts that form a path to the ground (see the `getFloatingParts` method).
The walker uses geographic indices and polygon overlap calculations to locate pats that overlap in 2D (see the `neighboringPart` method), 
and then checks height and level tags to determine 3D overlap (see the `neighborsHeightContains` method). 
When the walker is unable to find a 3D path to the ground, all the collected building parts are flagged as floating. 

To learn more about the code, please look at the comments in the source code for the check.  
[ShadowDetectionCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/areas/ShadowDetectionCheck.java)

