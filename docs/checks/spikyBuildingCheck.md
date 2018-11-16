# SpikyBuildingCheck

The purpose of this check is to identify buildings with extremely sharp angles in their geometry. 
These angles, or “spikes” are formed by two or more poly-lines and a peak node. These spikes are often 
hard to visually inspect as the poly-lines that create them can be very small. Spikes in building 
geometry are almost always errors and should be corrected. They can be inadvertently created in the 
data creation process by incorrectly closing a poly-line to create a polygon.

The original issue was raised in an [OpenStreetMap Help forum](https://help.openstreetmap.org/questions/66104/is-there-a-way-to-detect-very-sharp-angles-of-buildings). 
OSM user “sanser” included [a paper](https://drive.google.com/file/d/1MaLdnSnc454xKjn3eL95vDQKeoIW8zGU/view)
with their answer which described the methodology for identifying and correcting “spiky buildings” in the UK.

This check flags all Atlas objects for which the following criteria are true:
 * The object is a building or a building part
 * The angle created by the intersection of any two line segments within the building's geometry 
 should be less than 15˚ (configurable value).
 * An angle less than 15˚ should not be flagged if it occurs at the beginning or end of a curve 
 within the building's geometry. There should be three configurable values that control this behavior: 
 minimum number of points needed to comprise a curve, angle threshold between a curve and a non-curve, 
 and minimum total change in heading for the curve.

#### Live Examples

The way [34550963](https://www.openstreetmap.org/way/34550963) is tagged as a building, and has a 
spiky protrusion less than 15 degrees that was likely accidental.

The way [60485431](https://www.openstreetmap.org/way/60485431) is tagged as a building, but looks like it was
poorly digitized when compared to satellite imagery. 

The way [503863867](https://www.openstreetmap.org/way/503863867) is tagged as a building and has an
angle less than 15 degrees, but it is part of an intentionally mapped circle. This is a correct 
digitization and should not be flagged.

#### Code Review

For more information, see the source code in 
[SpikyBuildingCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/areas/SpikyBuildingCheck.java).
