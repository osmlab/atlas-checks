# HighwayAccessCheck

#### Description
This check flags all the objects of type Way that contain the access tag equal to "yes" or "permissive"
and the highway tag equal to one of the following: ["footway", "bridleway", "steps", "path", "cycleway", "pedestrian",
"track", "bus_guideway", "busway", "raceway", "motorway", "trunk"]
[OSMOSE](http://osmose.openstreetmap.fr/en/issues/open?item=3220).

#### Configurables
No configurable variables for HighwayAccessCheck


#### Live Example

[Way:705630999](https://www.openstreetmap.org/way/705630999) has the tags: access=yes, highway=cycleway.

#### Code Review
This check evaluates [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).
It flags the objects that contains the proper access and highway tags.

##### Validating the Object
We first validate that the incoming object is:
* Hasn't been flagged previously
* A Way (Main Edge)


##### Flagging the Object
###### Scenario 
* The object is a way, has an access tag "yes" or "permissive" and a highway tag from the following list: ["footway", "bridleway", "steps", "path", "cycleway", "pedestrian",
  "track", "bus_guideway", "busway", "raceway", "motorway", "trunk"].

##### Not flagging the Object
* Any other scenario than the one described above.


To learn more about the code, please look at the comments in the source code for the check: [HighwayAccessCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/HighwayAccessCheck.java)
