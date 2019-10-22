# IntersectingBuildingsCheck

#### Description

The purpose of this check is to identify buildings that intersect other buildings.

#### Live Examples

1. Line [id:172116389](https://www.openstreetmap.org/way/172116389) and [id:](https://www.openstreetmap.org/way/172116424) have building=YES Tags and do not fully contain one another, so they are flagged as `intersect`.
2. Line [id:525062338](https://www.openstreetmap.org/way/525062338) fully contains [id:525062342](https://www.openstreetmap.org/way/525062342) and both have building=YES Tags.
3. Line [id:](https://www.openstreetmap.org/way/334111739) with building=COMMERCIAL Tag fully contains [id:](https://www.openstreetmap.org/way/463063324) with building=RESIDENTIAL Tag.

#### Code Review

Each pair of intersecting buildings will only be flagged once (i.e. A intersecting B = B intersecting A).

 An Atlas object must meet the following criteria to be considered a building:

- It must have at least 3 points.
- It must have a building Tag and has a value that's defined in the [BuildingTag.java](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/BuildingTag.java).

Intersecting buildings are either flagged as 'intersect' or 'overlap':

- intersectLowerLimit and overlapLowerLimit are two configurable parameters that determine the flag type.
- The default values are: intersectLowerLimit = 0.01 (1%), overlapLowerLimit = 0.90 (90%).
- Flag types:
    - Intersect flag:
        - intersectLowerLimit <= the proportion of the intersection area compared to the smaller building area < overlapLowerLimit
        - Output instruction = 'building A intersects with building B'
    - Overlap flag:
        - The proportion of the intersection area compared to the smaller building area >= overlapLowerLimit
        - Output instruction = 'building A is overlapped by another building B'

To learn more about the code, please look at the comments in the source code for the check.
[IntersectingBuildingsCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/intersections/IntersectingBuildingsCheck.java)