# SuddenHighwayChangeCheck

#### Description

The purpose of this check is to identify edges that should be tagged as a link but are not.

#### Live Examples

Not tagged as a link
1. The way [id:7709337218](https://www.openstreetmap.org/way/7709337218) Edge bypasses intersection and is separated by physical triangle barrier 
but is not tagged as link.
2. The way [id:187664819](https://www.openstreetmap.org/way/187664819) Edge bypasses intersection from Barrabool Road to Mount Pleasant Road but is
not tagged as a link.

#### Code Review

This check evaluates Edge Atlas objects: 
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)

##### Validating the Object
We first validate that the incoming object is: 
* not already flagged
* Object is an Edge
* Edge is not a link
* Edge must be navigable by car
* Edge length must fit between config lengths
* Edge is not a roundabout or circular
* Edge has at least 2 connected edges from the start and end node
* Edge highway class is equal or greater than tertiary

##### Flagging the Object
There are lots of parameters that go into identifying certain scenarios to flag an edge requiring a link tag:
1. Angle in and out of main edge must meet config values which are above 90 degrees. Typically links aren't sharp turns off
of the main roads.
2. flagged edge must have a different highway tag from the in or out edge.
3. In and out edges must not be roundabouts.
4. Removing edges that have roundabout or traffic_signal associated with start or end node.
5. in and out edge must not share same name or number.
6. Main edge must not have a split road counterpart which is checked by checking the connected edges of the connected
edges. If any of those edges' osm ids match a connected edge on the opposite end of the main edge the main edge will not be flagged.

To learn more about the code, please look at the comments in the source code for the check.  
[SuddenHighwayChangeCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/SuddenHighwayChangeCheck.java)