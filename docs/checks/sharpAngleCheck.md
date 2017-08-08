# SharpAngleCheck

#### Description

Flags edges that have an angle that is too sharp within their polyline. Rendering fails once this threshold is 
exceeded. There may be other factors in play here, such as numbers of intersections, type of highway, etc. 
But the main breaking point is any angles that are greater than 149 degrees.

#### Code Review

To learn more about the code, please look at the comments in the source code for the check. 
[SharpAngleCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/SharpAngleCheck.java)
