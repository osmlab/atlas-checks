# InvalidPiersCheck

#### Description

The purpose of this check is to identify piers (OSM ways with man_made=Pier tag) that: (1) have a 
linear geometry but _DO NOT_ have an area=Yes Tag, or (2) have a polygonal geometry but _DO NOT_ 
have an area=Yes Tag.  A pier must: (A) have a highway Tag or (B) have an overlapping highway at the same
level and same layer as the pier or (C) be connected to  a ferry route at the same layer 
and same level as the pier or (D) be connected to or overlapping a 
building/amenity=Ferry_Terminal at the same layer and same level as the pier.  In addition, if a pier is 
polygonal, then it will also be considered a valid correction candidate if it has building and/or 
amenity=Ferry_Terminal Tags.  If a pier does not meet one or more of these criteria, this check will 
not flag the pier. 

#### Live Examples

1. Edge [id:691592715](https://www.openstreetmap.org/way/691592715) is a linear pier with a highway tag and does not have an area=yes tag
2. Edge [id:106157819](https://www.openstreetmap.org/way/106157819) is a polygonal pier with an overlapping highway and no area=yes tag.

#### Code Review

The check ensures that the Atlas object being evaluated is an [Edge](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)
with man_made=pier tag. First, using OSM way walker, all the edges that form the OSM way are collected. Then check if the
geometry formed from all the connected edges is linear or not. Then check if the pier has a highway tag or a highway
overlaps the pier or is connected to a ferry route/building or overlaps a building. All these connected items should also
be on the same level/layer as the pier. If any of the above condition is met, flag all the edges forming the OSM way with



To learn more about the code, please look at the comments in the source code for the check.
[InvalidPiersCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/area/PedestrianAreaOverlappingEdgeCheck.java)