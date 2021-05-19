# Lone Node Check

The check will look for nodes that have a highway tag but are not part of a way that has a highway or railway tag.


Wiki reference for node highway values: [Key:highway](https://wiki.openstreetmap.org/wiki/Key:highway)
#### Live Examples

1. Node [id:4318525793](https://www.openstreetmap.org/node/4318525793) is a lone node with `highway=crossing` and `crossing=zebra` on a `waterway=stream`.
2. Node [id:4547445454](https://www.openstreetmap.org/node/4547445454) is a lone node with `highway=traffic_signals` as part of two ways both with `waterway=river`.

#### Fix Suggestions

None

#### Code Review

This check looks at [Point](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java) elements.
For those with a `highway` tag the check validates that it has at least one connected Way with a `highway` or `railway` tag.

There is a filter in the configuration file that determines which `highway` tag values the point should have to be 
considered for this check:
```
"valid.highway.tag": ["crossing", "turning_circle", "traffic_signals", "stop", "give_way", 
      "motorway_junction", "mini_roundabout", "passing_place", "turning_loop"],
```

To learn more about the code, please look at the comments in the source code for the check:
[LoneNodeCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/LoneNodeCheck.java)
