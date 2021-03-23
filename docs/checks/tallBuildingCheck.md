# TallBuildingCheck

#### Description
The purpose of this check is to identify invalid building:levels and height tags as well as identify statistically outlying building:levels/height tags.

#### Configurables
- ***bufferDistanceMeters***: Distance to expand the bounding box of the building in question.
- ***minDatasetSizeForStatsComputation***: Minimum quantity of data points within a boundary to trigger calculating outlier statistics.
- ***maxLevelTagValue***: Maximum threshold for building:levels tag. 
- ***outlierMultiplier***: Multiplier to compute outlier thresholds.
- ***invalidHeightCharacters***: Characters that should not exist within a height tag.
- ***magicNumbers***: Magic numbers to use in the instructions (3 and 4) or statistics (0.25 and 0.75).

#### Live Examples
Way Intersects Toll Feature - Missing toll tag
1. The way [id:824285021](https://www.openstreetmap.org/way/824285021) intersects a toll feature and needs a toll=yes tag.

Inconsistent Toll Tags - way sandwiched between 2 toll=yes ways but does not have a toll tag
1. The way [id:498038529](https://www.openstreetmap.org/way/498038529) is inconsistent with its surrounding toll tags.

Escapable Toll - Way has routes that escape toll feature so toll should be investigated for removal
1. The way [id:2039409](https://www.openstreetmap.org/way/546540482) is deemed "escapable" since on either side of it there are ways with toll=no or no toll tag at all before
   intersecting a toll feature. There needs to be an investigation as to if the way in question or the surrounding ways have been modeled properly.

#### Code Review
This check evaluates [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)
, it attempts to find large jumps in highway classifications.

##### Validating the Object
We first validate that the incoming object is:
* Is a building 

##### Flagging the Edge
##### Three scenarios of TollValidationCheck
###### Scenario 1
* Way intersects toll feature but does not have a toll tag.
###### Scenario 2
* Way has inconsistent toll tagging compared to surrounding ways.
###### Scenario 3
* Toll is escapable so the edge in question should not have toll tag since a toll is not required to drive on the road.

##### AutoFix Suggestion
* AutoFix Suggestion only applied to Way intersecting toll feature, Scenario 1 and inconsistent toll tags, Scenario 2.


To learn more about the code, please look at the comments in the source code for the check.  
[TollValidationCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/TollValidationCheck.java)
