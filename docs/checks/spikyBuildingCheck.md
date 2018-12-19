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

###### Curves
Perfectly identifying curved sections of a building's geometry is not possible, especially given the
wide variety of mapping techniques across the world. However, it is possible to use heuristics to
get pretty close. This section will briefly walk through the algorithm used by `SpikyBuildingCheck`
to detect curves.

The first thing to note is that throughout the code, points are stored as the two surrounding segments.
So, for a triangle with points ABC and lines (AB, BC, and CA), we store the point B as <AB, BC>. This
way, we have a little extra context needed later on down the line.

The entry point into this algorithm is `getCurvedLocations`, but we'll start in `getPotentiallyCircularPoints`,
which grabs all points in a polygon where the change in heading between the previous and following
segments is less than some threshold. Once we have that list, we want to combine consecutive points
into a potentially circular segment. We do this in `summarizedCurvedSections`, which takes in an ordered
list of points in a polygon, and finds the start and end of each segment of consecutive points
(points that share a segment between them). It returns the segment before the start point, the segment
after the end point, and the total number of points present in the section. From there, `getCurvedLocations`
filters out all the summarized curved sections that are either too short, or the difference in headings
between the first and last segment in the section is too small. Finally, `sectionsToLocations` takes
all of the curvedLocations and converts them from the metadata tuple with length, start and end, back
to a list of locations. By taking all the sections at once, it is able to complete the entire conversion
in a single pass of the list of all segments, since both are in the same order.

###### More Information
For more information, see the source code in 
[SpikyBuildingCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/areas/SpikyBuildingCheck.java).
