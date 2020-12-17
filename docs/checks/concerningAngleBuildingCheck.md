# AcuteAngleBuildingCheck

#### Description
This check attempts to flag building who have angles between 80 and 89.9 degrees or between 90.1 and 100 degrees.

#### Configurables
- ***minangleDiff***: minimum angle difference between two building segments
- ***maxAngleDiff***: miximum angle difference between two building segments


#### Live Examples
## NOT DONE
Way Intersects Toll Feature - Missing toll tag
1. The way [id:824285021](https://www.openstreetmap.org/way/824285021) intersects a toll feature and needs a toll=yes tag.

#### Code Review
This check evaluates [Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java) and
[Areas](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java), 
it attempts to find buildings that have acute angles. 

##### Validating the Object
We first validate that the incoming object:
* Is an area or relation AND is a building. 
  
or
  
* Is part of a building.

##### Flagging the Edge
* Iterate through all Area/Relation based polygons associated with a building and check each angle.

To learn more about the code, please look at the comments in the source code for the check.  
[AcuteAngleBuildingCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/areas/AcuteAngleBuildingCheck.java)
