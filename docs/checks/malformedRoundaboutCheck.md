# Malformed Roundabout Check

#### Description
This check flags roundabouts where:
1. The directionality is opposite to what it should be (for example, a counterclockwise roundabout in a right-driving country)
2. The segments are multi-directional
3. The roundabout is incomplete or has overlapping segments
4. Part of it is not car navigable
5. It intersects with car navigable edges on the same level as itself and inside itself  

#### Live Example
1) This roundabout [id:242413354](https://www.openstreetmap.org/way/242413354) is multi-directional and
has some segment going the wrong way. This is incorrect and should be flagged.
2) Way [id:37908511](https://www.openstreetmap.org/way/37908511) is an incomplete roundabout.
3) Roundabout [id:243934233](https://www.openstreetmap.org/way/243934233) is two way, thus it flags for overlapping itself.
4) Roundabout [id:336145577](https://www.openstreetmap.org/way/336145577) is not car navigable.
5) The roundabout made of ways 
[id:468374854](https://www.openstreetmap.org/way/468374854), 
[id:27750560](https://www.openstreetmap.org/way/27750560), and
[id:468374855](https://www.openstreetmap.org/way/468374855) has the car navigable way 
[id:523227557](https://www.openstreetmap.org/way/523227557) intersecting and inside it. 

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).
In OpenStreetMap, roundabouts are [Ways](https://wiki.openstreetmap.org/wiki/Way) classified with
the `junction=roundabout` tag. We’ll use this information to filter our potential flag candidates.

Our first goal is to validate the incoming Atlas Object. We know two things about roundabouts:
* Must be a valid Edge
* Must have an `iso_country_code` tag
* Must have not already been flagged
* Must have `junction=roundabout` tag
* Must be main edge
* Must not have a highway tag value of `cycleway`, `footway`, or `pedestrian`

After the preliminary filtering of features, we need to get all the roundabout's edges. We use a
[SimpleEdgeWalker](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/walker/SimpleEdgeWalker.java)
to gather all roundabout Edges that are connected to the original Edge. 

This set of edges is then checked for non-car navigable and reverse Edges. If any are found the roundabout is flagged.

The set is then converted to a [Route](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Route.java).
If this fails the roundabout is flagged for not forming a single complete route. 
This could be caused by overlapping segments or multi-directionality.  
At this time it is also checked that the roundabout is a closed polyline. 

At this point we can check the direction. 
To find the direction of the roundabout, we get the cross product of every set of adjacent edges in the
roundabout. Using the cross product and the [right-hand rule](https://en.wikipedia.org/wiki/Right-hand_rule),
we get the vector which is orthogonal to each pair of adjacent vectors. 

A positive cross product indicates that the roundabout is going clockwise, and a negative cross-product 
indicates that the roundabout is going counterclockwise. This is calculated for each set of adjacent edges, and the most frequent direction is used for the roundabout. 
We return a RoundaboutDirection enum from the findRoundaboutDirection method and handle it in the flag method.

Using the roundabout's direction and the iso_country_code tag on a given feature, we can determine
whether the roundabout is moving in the incorrect direction. If the roundabout is in a right-driving
country and the traffic is moving in the clockwise direction or if the roundabout is in a left-driving
country and the traffic is moving in the counterclockwise direction then a flag is thrown.

Finally, we check for car navigable Edges that both intersect the roundabout on the same z level, and are at least partially inside it. 
This finds items that are generally to0 large or complex to be considered roundabouts, roundabouts where the wrong ways/edges have been tagged as `junction=roundabout`, 
and [throughabouts](https://wiki.openstreetmap.org/wiki/Throughabout) that have been tagged as roundabouts.

To learn more about the code, please look at the comments in the source code for the check.
[MalformedRoundaboutCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/MalformedRoundaboutCheck.java)