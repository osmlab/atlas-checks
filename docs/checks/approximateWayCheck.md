# ApproximateWayCheck

#### Description

The purpose of this check is to identify ways that are crudely drawn, there is a discrepancy between the drawing 
and the real way, especially for curves.

#### Live Examples

Crudely drawn ways
1. The way [id:168490775](https://www.openstreetmap.org/way/168490775) is crudely drawn and could use more nodes 
and some rearrangement of current nodes.

#### Code Review

This check evaluates [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)
, it attempts to find crudely drawn edges especially those that are curves.

##### Validating the Object
We first validate that the incoming object is: 
* An Edge
* A Master edge
* The Edge is Car Navigable
* The Edge is of a specified minimum highway type

##### Flagging the Edge
We split the edge into it's segments. If there is less than two segments we don't flag the edge. 
Otherwise, we will go through adjacent pairs of segments, first checking that the angle between 
them are within a configurable angle. We get the minimum distance that the middle point is from a bezier curve using 
points from the segments. We then check if the distance is greater than the minDeviationLength and if the ratio between 
the distance and the length of both segments is greater than the configured maxDeviationRatio.

If the angle, and the distance/deviation are both found as valid, the Edge will be flagged.  

##### How the quadratic bezier curve is built and distance/deviation is found
We build a quadratic bezier curve using the first node of the first segment, the node that joins both segments (anchor), 
and the last node of the second segment. During the creation of the curve we calculate at each step how close the 
curve gets to the anchor, the closest value is returned as the distance (or deviation).

To learn more about the code, please look at the comments in the source code for the check.  
[ApproximateWayCheck.java﻿](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/ApproximateWayCheck.java)
