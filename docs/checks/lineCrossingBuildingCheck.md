# Line Crossing Building Check

#### Description

The purpose of this check is to flag Lines or Edges that cross buildings invalidly. This integrity check examines the same intersections as [BuildingRoadIntersectionCheck](buildingRoadIntersectionCheck.md), but also inspects intersections between buildings and non-navigable Ways.

#### Configuration

There are no configurables for this check.

#### Live Examples

- Line [id:232392538](https://www.openstreetmap.org/way/232392538) crossing building [id:168913755](https://www.openstreetmap.org/way/168913755)
- Line [id:61241226](https://www.openstreetmap.org/way/61241226) crossing building [id:196979455](https://www.openstreetmap.org/way/196979455)
- Line [id:857309620](https://www.openstreetmap.org/way/857309620) crossing building [id:717915035](https://www.openstreetmap.org/way/717915035)

Please see the source code for LineCrossingBuildingCheck here: [LineCrossingBuildingCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/intersections/LineCrossingBuildingCheck.java)