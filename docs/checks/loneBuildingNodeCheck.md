#  Lone Building Node Check
The purpose of this check is to convert a Node with building tag into Building Footprint polygon (enclosed Way or Relation)  
      
# Reference
Wiki reference for building tag: [Key:building](https://wiki.openstreetmap.org/wiki/Key:building)
Wiki reference for buildings:[Buildings](https://wiki.openstreetmap.org/wiki/Buildings)
Wiki for mapping address as separate node:[Mapping address](https://wiki.openstreetmap.org/wiki/Mapping_addresses_as_separate_nodes_or_by_adding_to_building_polygons) 

#### Live Examples

1. Building Node [id:7157248223](https://www.openstreetmap.org/node/7157248223).
2. Building Node [id:3483463310](https://www.openstreetmap.org/node/3483463310).

#### Fix Suggestions

None

#### Code Review
In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines,
Nodes, Areas & Relations; in our case, we’re working with [Point](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java).

Our first goal is to validate the incoming Atlas Object.
* Must be a Point.
* Must have a building tag.  

Our second goal is to search within 10 meters (default) radius for building Areas or Relations.
* Areas or Relations must have building tag.
* Building Node must not be fully geometrically enclosed in Areas or Relations

#### Configuration 
1. Search Building Area/Relation distance. Default is 10 meters. 
```
"building.search.distance": 10.0
```
2. There is a filter in the configuration file that determines which `building` tag values the point should have to be
considered for this check:
```
"tags.filter":"building->!no&building->!window&building->!roof"
```

To learn more about the code, please look at the comments in the source code for the check:
[LoneBuildingNodeCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/LoneBuildingNodeCheck.java)
