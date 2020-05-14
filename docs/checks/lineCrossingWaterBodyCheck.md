# LineCrossingWaterBody Check

This check flags Lines, Edges, and optionally buildings that invalidly intersect inland water features. Validity of intersection is determined by the qualities of the intersecting feature. Flags are grouped by waterbody. A flag contains features for a single waterbody and all of its invalidly intersecting features.

A Line should be flagged if it has any tag combination in the configurable "lineItems.offending" and no tag combination in the configurable "lineItems.non_offending". Additionally, the Line must not be a bridge, should be on the same level as the waterbody, and can't have a tag starting with "addr:" or be part of a boundary relation, and should intersect a valid part of the waterbody.

An Edge should be flagged if it has the appropriate highway tag (specified in "highway.minimum" and "highways.exclude"), is not a bridge, is on the same level as the waterbody, has no tag combination from "lineItems.non_offending", has no tag starting with "addr:", intersects a valid part of the waterbody, and either does not have explicit locations for intersections with the waterbody or any explicit location of intersection is not marked by a node with a tag in "nodes.intersecting.non_offending". If any nodes of the street physically in the waterbody do not have a ford tag that is not ford->no, the street should be flagged as well.

A building should be flagged if the "buildings.flag" is set to true, and the building is not a public_transport->station,aerialway=station, is on the same level as the waterbody, and intersects a valid part of the waterbody.

#### Live Examples

1. Street [id:32845241](https://www.openstreetmap.org/way/32845241) and building [id:753774494](https://www.openstreetmap.org/way/753774494) intersect waterbody feature [id:10886166](https://www.openstreetmap.org/relation/10886166) invalidly. Both the street and waterbody share a location (node) at the point of intersection, but this node is not tagged with something from "nodes.intersecting.non_offending".
2. Street [id:738464515](https://www.openstreetmap.org/way/738464515) intersects waterbody feature [id:248766391](https://www.openstreetmap.org/way/248766391) invalidly. Both features share a location (node) at the point of intersection, but this node is not tagged with something from "nodes.intersecting.non_offending".

#### Code Review

The check starts off by validating certain waterbodies (Atlas Areas or Multipolygon relations) as being waterbody features. Then it collects all valid Edges and Lines (and optionally buildings) that intersect the given waterbody feature invalidly. A single flag is created which includes all intersecting land features for the ocean feature. The check repeats this process for every Area and Multipolygon relation in the supplied atlas.

Please see the source code for LineCrossingWaterBodyCheck here: [LineCrossingWaterBodyCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/intersections/LineCrossingWaterBodyCheck.java)
