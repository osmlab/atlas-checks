# FloatingEdgeCheck

#### Description

This check will look for any edges (Roads) outside of airports that do not contain any incoming or outgoing edges.
The appearance on the map would be that of a road simply floating in the middle of nowhere. 
No one for any navigation, no ability to enter the edge (road) from any point and no way to exit it. 
To resolve the issue a mapper would either remove the edge as invalid or connect it to the surrounding
road network.

#### Code Review

To learn more about the code, please look at the comments in the source code for the check.
[FloatingEdgeCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/FloatingEdgeCheck.java)
