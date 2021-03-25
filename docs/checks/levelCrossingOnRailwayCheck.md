# Level Crossing on Railway Check

#### Description

The Purpose of this check is to detect and flag object the following scenarios below:

1. When an area or way contain `railway=level_crossing` tag.
2. When a car navigable highway crosses a railway on the same layer and intersection node contains `railway=crossing`.
3. When a car navigable highway crosses a railway on the same layer and intersection node does not contain `railway` tag.
4. When a node contains `railway=level_crossing` but there is no car navigable highway that contains this node.
5. When a node contains `railway=level_crossing` but there is no railway that contains this node.
6. When a node contains `railway=level_crossing` but there is no railway and car navigable way on the same layer.
7. When a car navigable highway crosses a railway on the same layer and intersection node is missing.
8. When a node contains `railway=level_crossing` but there are only pedestrian ways at this intersection.
9. When a node contains `railway=level_crossing` but there are only bicycle ways at this intersection.

#### Auto-Fix Suggestions

For each of the possible issues flagged above these are the fix suggestions proposed.

1. Remove railway tag from object
2. Replace `railway=crossing` with `railway=level_crossing` on node
3. Add `railway=level_crossing` tag to node
4. NA (No suggestion for this case as there are multiple possible fixes)
5. NA (No suggestion for this case as there are multiple possible fixes)
6. NA (No suggestion for this case as there are multiple possible fixes)
7. NA (No suggestion for this case as the cases are too complicated)
8. Replace `railway=level_crossing` with `railway=crossing` on node
9. Replace `railway=level_crossing` with `railway=crossing`, add `foot=no`, and add `bicycle=yes` to node.

#### Live Example

*Case 1: Bridge over Railway on same layer.*

Highway bridge goes over Railway, but layer missing on both railway and highway. The bridge should have a layer=1 tag
https://www.openstreetmap.org/way/80459517

*Case 2: Railway crosses a highway with no level_crossing tag*

The intersection node (OSM ID: 273135212) is missing a railway=level_crossing tag. Add the appropriate tag to the node.
https://www.openstreetmap.org/node/273135212

*Case 3: Railway crosses highway with no intersection node.*

The intersection of railway (OSM ID: 550221984) and highway (OSM ID: 637449204) is missing an intersection node. Add an appropriate intersection node.
https://www.openstreetmap.org/way/550221984 and https://www.openstreetmap.org/way/637449204 overlap without an intersection node.

*Case 4: Node is not a Level_crossing*

The node (OSM ID: 4147274783) is tagged with railway=level_crossing but is not the intersection of a railway and highway. Remove tag or add missing way.
https://www.openstreetmap.org/node/4147274783

*Case 5: Non-node tagged with railway=level_crossing*

Unable to find non-node features tagged with railway=level_crossing in OSM maps. Tested manually.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines,
Nodes & Relations; in our case, we’re working with
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java),
[Nodes](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java), and
[Lines](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Line.java).
In OpenStreetMap, railways are [Ways](https://wiki.openstreetmap.org/wiki/Way) classified with
the `railway=rail`, `railway=tram`, `railway=disused`, `railway=miniature`, or `railway=preserved` tags. Highways are
[Ways](https://wiki.openstreetmap.org/wiki/Way) classified with `highway=*` tags. In this check we are interested in
the intersection of highways and railways. These are represented by [Nodes](https://wiki.openstreetmap.org/wiki/Node).
We’ll use this information to filter our potential flag candidates.

A Car Navigable highway is any highway tagged with any of the following "highway:" tag values: MOTORWAY, TRUNK,
PRIMARY, SECONDARY, TERTIARY, UNCLASSIFIED, RESIDENTIAL, SERVICE, MOTORWAY_LINK, TRUNK_LINK, PRIMARY_LINK,
SECONDARY_LINK, TERTIARY_LINK, LIVING_STREET, TRACK, ROAD.

A valid intersection of a car navigable highway and railway should have a [Node](https://wiki.openstreetmap.org/wiki/Node)
at the intersection of the ways that is classified with a `railway=level_crossing`. Nodes at intersections are only
necessary if the railway and highway ways are on the same layer. It is common for a way that is classified with a
`bridge=yes` or `tunnel=yes` to go over or under another way without an intersection node. In those cases a node at the
intersection is not necessary, and if a node does exist at that intersection then the `railway=level_crossing` tag
should not be used. This check assumes that if a highway or railway has been tagged to indicate that this way is a
bridge or tunnel and the layer tag has not been set, then there is an implied layer of 1 for bridges and -1 for
tunnels.

Note: Railways and highways that are under construction will be ignored by this check. A way is under construction if
any tag key or tag value contains the string "construction".

The code has three major sections to check for all possible types of railway/highway intersections.

1. The first section is specifically designed to look at all [Nodes](https://wiki.openstreetmap.org/wiki/Node). Each
node is examined to count the number of highways and railways that contain this node.
    1. If a node is contained in at least one highway and at least one railway on the same layer and is not tagged
    with `railway=level_crossing` then it is flagged. A fix suggestion exists for this type of issue and will suggest
    to add a railway=level_crossing tag to the Node.
    1. If a node is tagged with `railway=level_crossing` but is not contained in any railways or car navigable highways
or all highways and railways are on separate layers then it is flagged. Two fix suggestions exists for these issues and
will suggest to remove the invalid tag if no pedestrian ways cross at this node. If a pedestrian way crosses at this
node then the suggestion will suggest to change the railway=level_crossing tag to railway=crossing.

2. The second section examines all objects that are not Nodes or Points. If any non-node or point object is tagged
with `railway=level_crossing` then it is flagged. A fix suggestion exists for this issue and it will suggest to remove
the invalid tag.

3. The final section of code examines all railway [Lines](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Line.java).
Each railway is traversed to find any intersection with a car navigable highway. If an intersection is found that is
missing a node then it is flagged. This issue type does not include a fix suggestion.

To learn more about the code, please look at the comments in the source code for the check.
[LevelCrossingOnRailwayCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/intersections/LevelCrossingOnRailwayCheck.java)
