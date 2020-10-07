# SuddenHighwayTypeChangeCheck

#### Description

The purpose of this check is to identify ways that have sudden highway tag jumps that are at least 2 classifications a part.

#### Live Examples
Sudden Highway Type Changes
1. The way [id:698449634](https://www.openstreetmap.org/way/698449634) jumps from primary road to tertiary road. Flagged edge even needs to be a link.

#### Code Review

This check evaluates [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)
, it attempts to find large jumps in highway classifications.

##### Validating the Object
We first validate that the incoming object is: 
* An Edge
* A Main edge
* The Edge is Car Navigable
* The Edge is of a specified minimum highway type (tertiary)
* The Edge is not a roundabout or circular

##### Flagging the Edge
Gather the Edge' in and out edges. Iterate over these edges set a numeric value for the highway tag. (anything equal or above residential)
FInd the tag difference between the edge being validated and the in and out edges. If the difference is greater than or 
equal to 2 the edge is flagged. 

##### Assigning doubles based on highway tag
* Residential and Unclassified --> 0.0
* Tertiary/Tertiary_link --> 2.0
* Secondary/Secondary_link --> 3.0
* Primary/Primary_link --> 4.0
* Trunk/Trunk_link --> 5.0
* Motorway/Motorway_link --> 6.0
* All others --> -1.0

To learn more about the code, please look at the comments in the source code for the check.  
[SuddenHighwayTypeChangeCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/SuddenHighwayTypeChangeCheck.java)
