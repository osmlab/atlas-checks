# InvalidPiersCheck

#### Description

The purpose of this check is to identify piers (OSM ways with man_made=Pier tag) that: (1) have a 
linear geometry but _DO NOT_ have an area=Yes Tag, or (2) have a polygonal geometry but _DO NOT_ 
have an area=Yes Tag.  A pier must: (A) have a highway Tag or (B) have an overlapping highway at the
same level and same layer as the pier or (C) be connected to  a ferry route at the same layer and 
same level as the pier or (D) be connected to or overlapping a building/amenity=Ferry_Terminal at 
the same layer and same level as the pier.  In addition, if a pier is polygonal, then it will also 
be considered a valid check candidate if it has building and/or amenity=Ferry_Terminal Tags.  
If a pier does not meet one or more of these criteria, this check will 
not flag the pier. 

#### Live Examples

1. Edge [id:691592715](https://www.openstreetmap.org/way/691592715) is a linear pier with a highway 
tag and does not have an area=yes tag
2. Edge [id:106157819](https://www.openstreetmap.org/way/106157819) is a polygonal pier with an 
overlapping highway and no area=yes tag.

#### Code Review

The check ensures that the Atlas object being evaluated is an [Edge](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)
with man_made = pier tag and no area = yes tag. All the way sectioned edges of the OSM way are first collected. 
Using all the collected edges, the geometry formed is verified to be either linear or polygonal. We 
can flag the edge if it has a highway tag with priority greater than or equal to minimum type set in
the configurable or if the pier is polygonal with a building tag or is polygonal pier with amenity=ferry_terminal. 
If the above conditions are not met, then check if the way formed from the edges overlaps a highway 
or building or is connected to a ferry route or amenity=ferry_terminal. If any of these conditions 
are met, we can flag the edges.

To learn more about the code, please look at the comments in the source code for the check.
[InvalidPiersCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/InvalidPiersCheck.java)