# Source Maxspeed Check

This check flags features that contain a `source:maxspeed` tag that is not constructed following the rules set in the 
OSM wiki: [Key:source:maxspeed](https://wiki.openstreetmap.org/wiki/Key:source:maxspeed).

#### Live Examples

1. Way [id:24358163](https://www.openstreetmap.org/way/24358163) has the value `implicit` instead of `RO:urban` as expected
for a default maxspeed on an urban road in Romania.
2. Way [id:31362404](https://www.openstreetmap.org/way/31362404) has a url instead of one of the following expected: 
`sign`, `markings` or `<country_code>:<context>`.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes, Areas & Relations; 
in our case, we’re are looking at
[Points](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java) and 
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).
 
 The check verifies that all the values for `source:maxspeed` tags respect the following rules:
 * = `sign` where the speed limit is defined by a numeric sign
 * = `markings` where the speed limit is defined by painted road markings
 * = `<country_code>:<context>` where the speed limit is defined by a particular context, for example urban/rural/motorway/etc., 
 and no maxspeed is signposted
 
 To learn more about the code, please look at the comments in the source code for the check:  
 [SourceMaxspeedCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/SourceMaxspeedCheck.java)
 