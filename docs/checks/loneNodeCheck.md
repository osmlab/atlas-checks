# Lone Node Check

The check will look for nodes that have a highway tag but are not part of a way that has a highway or railway tag.
It will also verify that the highway tag value is correctly associated with a Node element.

Wiki reference for node highway values: [Key:highway](https://wiki.openstreetmap.org/wiki/Key:highway)
#### Live Examples

1. Node [id:4318525793](https://www.openstreetmap.org/node/4318525793) is a lone node with `highway=crossing` and `crossing=zebra` on a `waterway=stream`.
2. Node [id:4547445454](https://www.openstreetmap.org/node/4547445454) is a lone node with `highway=traffic_signals` as part of two ways both with `waterway=river`.
3. Node [id:407609365](https://www.openstreetmap.org/node/407609365) has a `highway=motorway` tag which is not an appropriate value for a node.

#### Fix Suggestions

None

#### Code Review

This check looks at [Node](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Node.java) elements.
For those with a `highway` tag the check validates that the value is appropriately associated with an OSM node and that it has at least one connected Edge with a `highway` or `railway` tag.

To learn more about the code, please look at the comments in the source code for the check:
[LoneNodeCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/LoneNodeCheck.java)
