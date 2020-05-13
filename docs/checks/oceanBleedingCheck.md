# Ocean Bleeding Check

This check aims to flag streets, railways, and buildings that bleed into (intersect) ocean features. Intersection includes any geometrical interaction between the ocean feature and the land feature. The only exception to this rule is streets that end at ocean boundaries and are tagged with amenity->ferry_terminal; such streets are not flagged. The definition of streets and railways can be changed in the configuration for the check ("lineItems.offending" for railways, "highway.minimum" and "highway.exclude" for streets). Additionally, tags that describe ocean features are configurable. A valid ocean feature (that is considered for the check) must conform to "ocean.valid" and must not conform to "ocean.invalid", OR must conform to "ocean.boundary". The latter is by default natural->coastline.

#### Live Examples

1. Building [id:355294262](https://www.openstreetmap.org/way/355294262#map=19/22.36138/114.09546&layers=C) extends into ocean feature [id:243872591](https://www.openstreetmap.org/way/243872591#map=16/22.3630/114.0932&layers=C) invalidly.
2. Street [id:327223335](https://www.openstreetmap.org/way/327223335#map=17/25.21095/55.24491&layers=C) extends into ocean feature [id:87287185](https://www.openstreetmap.org/way/87287185#map=17/25.21143/55.24443&layers=C) invalidly.

#### Code Review

The check starts off by validating certain waterbodies (Atlas Areas or LineItems) as being ocean features. Then it collects all valid buildings, streets, and railways that intersect the given ocean feature. A single flag is created which includes all intersecting land features for the ocean feature. The check repeats this process for every Area and LineItem in the supplied atlas.

Please see the source code for OceanBleedingCheck here: [OceanBleedingCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/intersections/OceanBleedingCheck.java)
