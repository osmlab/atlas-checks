# Abbreviated Street Name Check

This check flags Edges, Points, Lines, Nodes, Areas & Relations that have an `addr:street` tag containing abbreviated Road Type. 
According to [OSM Good Practice](https://wiki.openstreetmap.org/wiki/Names#Abbreviation_.28don.27t_do_it.29), address name 
abbreviations must be spelled out in full. Check is currently supporting English road types only. 

### Resources 

Official USPS [street suffix abbreviation](https://pe.usps.com/text/pub28/28apc_002.htm)
Street Suffix [wiki](https://en.wikipedia.org/wiki/Street_suffix)

#### Live Examples
Please note that examples below might be fixed already.
1. Way [id:157966536](https://www.openstreetmap.org/way/157966536) has an `addr:street` value of 3rd Ave NW with abbreviated road type Avenue.
2. Way [id:344801116](https://www.openstreetmap.org/way/344801116) has an `addr:street` value of Division St with abbreviated road type Street.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes, Areas & Relations;

Our first goal is to validate the incoming Atlas Object.
* Must be a valid Atlas Entity
* Must have `addr:street`

Our second goal is to analyze the `addr:street` to ensure that road type is not abbreviated and fully spelled out.   

To learn more about the code, please look at the comments in the source code for the check.  
[AbbreviatedAddressStreetCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/AbbreviatedAddressStreetCheck.java)
