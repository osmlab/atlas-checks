# SuddenHighwayTypeChangeCheck

#### Description

The purpose of this check is to identify ways that have sudden highway tag jumps based on 3 different classes.

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
Gather the ways' in and out edges. Iterate over these edges and determining if they fit within the 3 classifications of this check.

##### Concerning Highway Change Classification
* Way with following classification: **Motorway, Trunk, Primary, Secondary, or Tertiary**
* Above way terminates and connects to following classification: **Unclassified, Residential, Living Street, Track, or Service**

To learn more about the code, please look at the comments in the source code for the check.  
[SuddenHighwayTypeChangeCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/SuddenHighwayTypeChangeCheck.java)
