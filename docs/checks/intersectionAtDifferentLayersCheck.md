# IntersectionAtDifferentLayersCheck

#### Description

The purpose of this check is to identify Node when it is a non-terminal intersection node between two ways which have different layer tag values. 
Catching such cases will prevent “jump off the bridge” or “climb up the bridge” scenarios when routing.

#### Live Examples

1. Node [id:6510210704](https://www.openstreetmap.org/node/6510210704)
2. Node [id:3095952757](https://www.openstreetmap.org/node/3095952757)
3. Node [id:2885601598](https://www.openstreetmap.org/node/2885601598)

#### Code Review
In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines,
Nodes & Relations; in our case, we’re working with [Nodes](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java).

Our first goal is to validate the incoming Atlas Object.
* Must be a Node.
* Must be an Intermediate Node.    
* Must have not already been flagged.
* Must not be a Pedestrian Crossing [OSM-wiki:highway=crossing](https://wiki.openstreetmap.org/wiki/Tag:highway%3Dcrossing).
* Must not be a Railway Level Crossing [OSM-wiki:railway=crossing](https://wiki.openstreetmap.org/wiki/Tag:railway%3Dcrossing).  

Our second goal is to identify connected Edges. 
* Must be a Main Edge.
* Must be a Car-navigable or Pedestrian-navigable highway type.
* Must not be an Area.
* Must not match Indoor Mapping filter. 
* Must match Great Separation highway tag filter. Default is empty in configurations.json

#### Great Separation Filter Example 
Flag Nodes that connect only matching highway types: "bridge->yes|tunnel->yes|embankment->yes|cutting->yes|ford->yes"; 

To learn more about the code, please look at the comments in the source code for the check.
[IntersectionAtDifferentLayersCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/intersections/IntersectionAtDifferentLayersCheck.java)