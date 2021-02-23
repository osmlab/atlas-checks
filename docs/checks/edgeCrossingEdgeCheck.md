# EdgeCrossingEdgeCheck

#### Description

The purpose of this check is to identify Edges intersecting another Edge(s) that do not share the same Node (meaning they are not well-connected) nor have proper layer tagging on one of these Edge(s) (meaning there should be a _layer_ Tag for one of the Edges).

#### Live Examples

1. Line [id:245574716](https://www.openstreetmap.org/way/245574716) and [id:245574709](https://www.openstreetmap.org/way/245574709) do not share a node of intersection.
2. Line [id:400798431](https://www.openstreetmap.org/way/400798431) and [id:386147030](https://www.openstreetmap.org/way/386147030) cross invalidly.
3. Line [id:313458620](https://www.openstreetmap.org/way/313458620) and [id:554507231](https://www.openstreetmap.org/way/554507231) are overlapping duplicates.
4. Line [id:486493202](https://www.openstreetmap.org/way/486493202) and [id:172811290](https://www.openstreetmap.org/way/172811290) are duplicates that intersect at [id:172811276](https://www.openstreetmap.org/way/172811276).

#### Check configuration.
1) "minimum.highway.type" and "maximum.highway.type" - specifying corridor of OSM ways that will be checks.
##### Example 
Check OSM ways that fall into following corridor highway=trunk to highway=primary.
"minimum.highway.type": "primary",
"maximum.highway.type": "trunk",
2) "car.navigable", "pedestrian.navigable", "crossing.car.navigable", "crossing.pedestrian.navigable" - grouping highway types for Atlas object and crossing Atlas object. 
Please refer to [Highway Tag](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/HighwayTag.java) for highway grouping.
##### Example 
Check Car-Navigable ways crossing with Car-Navigable ways only (default).
"car.navigable": true,
"pedestrian.navigable": false,
"crossing.car.navigable": true,
"crossing.pedestrian.navigable": false
##### Example
Check Pedestrian-Navigable ways crossing with Car-Navigable and Pedestrian Navigable.
"car.navigable": false,
"pedestrian.navigable": true,
"crossing.car.navigable": true,
"crossing.pedestrian.navigable": true.

#### Code Review

The check ensures that the Atlas object being evaluated is a car-navigable Edge. The check flags Edges that cross each other if they do not share the same node of intersection, or if none of the Edges have a _layer_ Tag. In addition, this check flags all Edges that cross the "candidate edge" Edge that is currently inspected by the check). The check inspects every Edge, creating duplicated flags when there are multiple Edges invalidly crossing each other.

To learn more about the code, please look at the comments in the source code for the check.
[EdgeCrossingEdgeCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/intersections/EdgeCrossingEdgeCheck.java)