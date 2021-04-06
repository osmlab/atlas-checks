# FixMeReviewCheck

#### Description
The purpose of this check is to flag features that contain the "fixme"/"FIXME" tags with along with a variety of other important tags.

#### Live Examples

Highway contains "fixme" tag with a value of "name" which is a higher priority to address.
1. [Node:551984301](https://www.openstreetmap.org/way/551984301) has a highway tag that is unknown (“highway=priority”)

#### Code Review
This check evaluates [Nodes](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java) and
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java), and [HighwayTags](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/HighwayTag.java),
it attempts to find items that contain unknown highway tags.