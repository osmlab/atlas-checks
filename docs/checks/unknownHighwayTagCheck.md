# UnknownHighwayTagCheck

#### Description
This check attempts to flag all highway tags that are unknown to the [osm wiki page](https://wiki.openstreetmap.org/wiki/Key:highway).

#### Live Examples

Highway tag does not exist on the OSM Wiki page.
1. [Node:313559095](https://www.openstreetmap.org/node/313559095) has a highway tag that is unknown (“highway=priority”)

#### Code Review
This check evaluates [Nodes](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java) and
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java), and [HighwayTags](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/HighwayTag.java),
it attempts to find items that contain unknown highway tags. 