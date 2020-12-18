# AcuteAngleBuildingCheck

#### Description
This check attempts to flag building who have angles between 80 and 89.9 degrees or between 90.1 and 100 degrees that need to be squared.

#### Configurables
angles:
- ***minLowAngleDiff***: minimum angle difference between two building segments below 90 degrees.
- ***maxLowAngleDiff***: maximum angle difference between two building segments below 90 degrees.
- ***minHighAngleDiff***: minimum angle difference between two building segments above 90 degrees.
- ***maxHighAngleDiff***: maximum angle difference between two building segments above 90 degrees.

angleCounts:
- ***min***: minimum amount of nodes the building can have.
- ***max***: maximum amount of nodes the building can have.


#### Live Examples

Building needs to have angles squared
1. The way [id:791199437](https://www.openstreetmap.org/way/791199437) needs to have its angle squared.

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
[ConcerningAngleBuildingCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/areas/ConcerningAngleBuildingCheck.java)
