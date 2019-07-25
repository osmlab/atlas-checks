# PedestrianAreaOverlappingEdgeCheck

#### Description

The purpose of this check is to flag pedestrian areas that are not properly snapped to its 
intersecting/overlapping edges. Pedestrian areas are defined as *Ways* with highway=PEDESTRIAN and
area=YES tags. Overlapping roads are *Edges* with any highway=** except highway=path, 
highway=steps, highway=footway, and highway=pedestrian tag, which 
are too narrow to allow vehicle access.  This check only applies to pedestrian areas 
where all points of intersection with roads are not snapped.  To be considered overlapping,
pedestrian areas and roads must be at the same elevation, meaning, both the area and edge should have
the same *LayerTag* and same *LocationTag*, if any. If these conditions are true, 
this check will flag pedestrian areas overlapping with unsnapped roads along with the unsnapped roads.

#### Live Examples

1. Line [id:223986287](https://www.openstreetmap.org/way/223986287) is not snapped to the road with which it overlaps.
2. Line [id:575511688](https://www.openstreetmap.org/way/575511688) is not snapped to the road with which it overlaps.

#### Code Review

The check ensures that the Atlas object being evaluated is an [Area](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java).
The roads that intersects the area is then verified to be properly snapped to the area. If not, the area, intersecting edge and any connected edges within the area are marked as 
flagged.

To learn more about the code, please look at the comments in the source code for the check.
[PedestrianAreaOverlappingEdgeCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/area/PedestrianAreaOverlappingEdgeCheck.java)