# Highway Intersection Check

#### Description

The purpose of this check is to flag highways intersecting power or waterway objects.

*dam* and *weir* waterway objects are not flagged, these type of waterways can intersect with highways.

For waterway objects the intersection with a highway having *leisure=slipway* or *ford=yes* tag is allowed, and they are not flagged.

#### Configuration

There are no configurables for this check.

#### Code Review

#### More Information

Please see the source code for HighwayIntersectionCheck here: [HighwayIntersectionCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/intersections/HighwayIntersectionCheck.java)