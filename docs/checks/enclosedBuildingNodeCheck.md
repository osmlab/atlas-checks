#  Enclosed Building Node Check
The purpose of this check is to remove building tag from the Node that is fully geometrically enclosed into Building Area or Relation.  

# Reference
Wiki reference for building tag: [Key:building](https://wiki.openstreetmap.org/wiki/Key:building)
Wiki reference for buildings:[Buildings](https://wiki.openstreetmap.org/wiki/Buildings)
Wiki for mapping address as separate node:[Mapping address](https://wiki.openstreetmap.org/wiki/Mapping_addresses_as_separate_nodes_or_by_adding_to_building_polygons)

#### Live Examples

1. Building Node [id:982746946](https://www.openstreetmap.org/node/982746946).
2. Building Node [id:2078552526](https://www.openstreetmap.org/node/2078552526).

#### Fix Suggestions

None

#### Code Review
In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines,
Nodes & Relations; in our case, we’re working with [Point](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java).

Our first goal is to validate the incoming Atlas Object.
* Must be a Point.
* Must have a building tag.

Our second goal is to search within 10 meters (default) radius for building areas.
* Area must have building tag.
* Building Node must be fully geometrically enclosed in Area.

#### Configuration
1. Search Building Polygon/Relation distance. Default is 10 meters.
```
"building.search.distance": 10.0
```
2. There is a filter in the configuration file that determines which `building` tag values the point should have to be
   considered for this check:
```
"tags.filter":"building->!no&building->!window&building->!roof"
```

To learn more about the code, please look at the comments in the source code for the check:
[EnclosedBuildingNodeCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/EnclosedBuildingNodeCheck.java)
