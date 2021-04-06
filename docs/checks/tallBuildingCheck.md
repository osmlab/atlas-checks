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

Case 1 - Building has a building:levels tag greater than 100.
* Small amount of these flags but this was the ask for the original purpose of this check. 

Case 2 - Building has an invalid building:levels tag.
* The building (house) [id:795395318](https://www.openstreetmap.org/way/795395318) has an invalid building:levels tag. (should only be comprised of numerical values)

Case 3 - Building has an outlying building:levels tag compared to buildings around it.
* The building [id:734691356](https://www.openstreetmap.org/way/734691356) has an outlying height tag compared to it's neighboring homes.

Case 4 - Building has an invalid height tag.
* The building [id:788365480](https://www.openstreetmap.org/way/788365480) has an invalid height tag. (needs space between '2' and 'm')

Case 5 - Building has an outlying height tag compared to buildings surrounding it.
* The building (house) [id:710607756](https://www.openstreetmap.org/way/710607756) has an outlying height tag compared to it's neighboring homes.


#### Code Review
This check evaluates [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java), and
[Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java), validating their buildings:levels tags and height tag statistically and textually.

##### Validating the Object
We first validate that the incoming object is:
* Is a building 

##### Flagging the Edge
##### Five scenarios of TallBuildingCheck
###### Scenario One
* Building has a building:levels tag greater than 100.

###### Scenario Two
* Building has an invalid building:levels tag.
    * tag contains anything other than numerical values.
    
###### Scenario Three
* Building has an outlying building:levels tag compared to buildings around it.
    * Statistical analysis of the building:levels tag determining if it is an outlier or not compared to all buildings within a mile radius. Statistical parameters can be configured in the config.json.

###### Scenario Four
* Building has an invalid height tag.
    * tag contains characters other than numerical, alphabetical, double quotes (""), or apostrophe ('').

###### Scenario Five
* Building has an outlying height tag compared to buildings surrounding it.
    * Statistical analysis of the height tag determining if it is an outlier or not compared to all buildings within a mile radius. Statistical parameters can be configured in the config.json.

To learn more about the code, please look at the comments in the source code for the check.  
[TallBuildingCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/areas/TallBuildingCheck.java)
