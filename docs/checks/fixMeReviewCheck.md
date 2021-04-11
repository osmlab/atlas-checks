# FixMeReviewCheck

#### Description
The purpose of this check is to flag features that contain the "fixme"/"FIXME" tags with along with a variety of other important tags.

#### Configurables
* ***minHighwayTag:*** Minimum highway tag value for object.
* ***fixMeSupportedValues:*** Supported fixme tag values
#### Live Examples

1. [Way:177401829](https://www.openstreetmap.org/way/177401829) has "fixme" tag with priority value (name) along with important supplementary tag(s).
2. [Way:328136807](https://www.openstreetmap.org/way/328136807) has "fixme" tag with priority value (continue) along with important supplementary tag(s).

#### Code Review
This check evaluates [Nodes](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java),
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java),
[Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java),
[Points](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java),
[Areas](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java), and
[Lines](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Line.java) 
determining if they contain the fixme tag and priority fixme tag values along with other important tags.