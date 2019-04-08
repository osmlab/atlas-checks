# Address Street Name Check

This check flags Points that have an `addr:street` value that does not match any of the names of surrounding streets.  
The search distance for surrounding streets is based on a configurable value that has a default of 100 meters. 

#### Live Examples

1. Node [id:847673678](https://www.openstreetmap.org/node/847673678) has an `addr:street` value of Stykkishólmsvegur that does not match any name of surrounding streets.
2. Node [id:2416844306](https://www.openstreetmap.org/node/2416844306) has an `addr:street` value of Vitatorg that is a typo of the nearby street V**í**tatorg.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes, Areas & Relations; 
in our case, we’re are looking at
[Points](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java) and 
[Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).

This check first validates objects by checking that they are Points with an `addr:street` tag.

If an object is valid, the check gathers all name tag values from surrounding roads (Edges). The surrounding roads are defined by a 
configurable search distance that is 100m by default. All name tag values are collected (`name` and localized name tags).

The `addr:street` is then compared against the collected list of street names. If no match is found the Point is flagged. 

To learn more about the code, please look at the comments in the source code for the check.  
[AddressStreetNameCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/AddressStreetNameCheck.java)
