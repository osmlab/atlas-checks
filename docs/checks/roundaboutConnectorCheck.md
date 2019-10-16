# Roundabout Connector Check

This check flags roads that connect to a roundabout at too sharp an angle. 

This is usually indicative of one way roads that have been drawn backwards, or two way roads that should be one way.   
Other map errors found by this check are poorly digitized connector roads (poorly drawn but don't brake routing), 
incorrectly labeled or formed roundabouts, and missing roads or networks.

The sharpness of flagged angles can be altered using the two configurable values:  
`threshold.one_way`: maximum degrees of intersection for a one way road; defaults to 100°  
`threshold.two_way`: maximum degrees of intersection for a two way road; defaults to 130°  
<br>
```
 1| \____/
 2|   \ ∠A
 3|    \
```
The threshold angles are based on the headings of the connected roads, thus the angle may not be what would normally be 
considered the angle of intersection. In the diagram above line 1 is part of a roundabout and lines 2 and 3 are a 
connecting road. Normally the angle of intersection would be considered angle A, but because we are using headings it is 
actually ∠A's supplement. So if ∠A is 80° then the value to flag this intersection would need to be at least 100°.

#### Live Examples

1. The way [id:296416348](https://www.openstreetmap.org/way/296416348) is digitized backwards.
2. The way [id:606815435](https://www.openstreetmap.org/way/606815435) should be made one way.
3. The way [id:256474193](https://www.openstreetmap.org/way/256474193) is one way in the correct direction, but connects 
at a sharp angle due to poor digitization.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes & Relations; in 
our case, we’re are looking at [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).

Our first goal is to validate the incoming Atlas object. Valid features for this check will satisfy the following conditions:

* Is an Edge
* Is not an OSM way that has already been flagged
* Is not tagged as a roundabout (`junction=roundabout`)
* Has a `highway` tag greater than `service`

Next we check that the Edge we are working with is connected to a roundabout. This is done by checking the in and out 
Edges of our current edge for `junction=roundabout` tags. If no connection to a roundabout can be found the current Edge
is not flagged.

Once we establish that our current Edge is a roundabout connector, we gather and compare the headings of the connector 
and the roundabout. The headings are gathered from both the connector and the roundabout Edge it is connected to. The 
headings are those that form the intersection, the initial heading of one Edge and the final heading of the other. 
Depending on whether the connector is a one or two way road, the difference in the angles is compared to one of the two 
thresholds. If it meets or exceeds that threshold the Edge is flagged (along with all other Edges that made up the 
original way). 

To learn more about the code, please look at the comments in the source code for the check:  
[RoundaboutConnectorCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/RoundaboutConnectorCheck.java)
