# AtGradeSignPostCheck

#### Description

This check identifies at-grade intersections that are not modeled into destination_sign relations. 
An at-grade intersection is an intersection with two or more edges at the same level with valid 
highway classifications.

#### Live Examples

1. Node [id:393673917](https://www.openstreetmap.org/node/393673917) forms an at-grade intersection 
with ways [393673917](https://www.openstreetmap.org/way/202447272), 
[34370252](https://www.openstreetmap.org/way/34370252) and 
[41234996](https://www.openstreetmap.org/way/41234996) but is not a member of a "destination_sign" relation.
2. Node [id:5351792253](https://www.openstreetmap.org/node/5351792253) forms an at-grade intersection with its exit road
[554569602](https://www.openstreetmap.org/way/554569602) but is not a member of a "destination_sign" relation.

#### Code Review

The check ensures that the Atlas object being evaluated is a [Node](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java)
with a minimum node valence of 3. The node is a valid candidate for the check if it forms an intersection with edges at the same z-level with highway classification specified in the configurable. 
Once the node is evaluated to be a valid at-grade intersection, check if it is a member of any destination_sign relations.
If the node is not part of a destination_sign relation, then flag the node along with the intersecting edges. Since a node can be part of multiple destination_sign
relations, check if all the valid intersecting edges are members of destination_sign relations. If there are edges missing destination_sign
relations, flag the node and the edges. If all the intersecting edges are members of relations, check if all the relations have destination tags.
If the relations are missing destination_sign tag, flag it. If the node is part of a roundabout, flag all the roundabout edges when flagging the node and its intersecting edges.

To learn more about the code, please look at the comments in the source code for the check.
[AtGradeSignPostCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/intersections/AtGradeSignPostCheck.java)