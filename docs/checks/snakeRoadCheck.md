# SnakeRoadCheck

#### Description

Flags edges that form Snake Roads. A Snake Road is defined as a road that should be split into two or more roads. 
An example of this is a residential road that weaves in and out of a neighborhood or multiple neighborhoods, 
where the correct behavior should have each road in a neighborhood be it's own separate way. 
The criteria we use to identify a snake road is:

- At some point, two consecutive edges making up the Snake road must have a heading difference of at least 60 degrees.
- The snake road must have at least 3 edges
- At least one edge making up the snake road should have a valence greater than 4
- To be considered a snake road, you should either have no road name or have any connected ways that share the 
same name that you do. This is done to prevent flagging strange portions of highways that may exhibit the behavior of a snake road.

#### Code Review

To learn more about the code, please look at the comments in the source code for the check.
[SnakeRoadCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/SnakeRoadCheck.java)
