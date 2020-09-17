# HighwayToFerryTagCheck

#### Description

The purpose of this check is to flag all Edges with *route=FERRY* and *highway=PATH* (or higher). 
According to [OSM Wiki](https://wiki.openstreetmap.org/wiki/Key:ferry), when *route=FERRY* exists for an object, 
its ferry Tag can take values of highway classification and so, it is recommended to have a ferry Tag
with the highway Tag value instead of a highway Tag for ferry routes.

#### Live Examples

1) The OSM Way [25957980](https://www.openstreetmap.org/way/25957980), ingested as an Edge, 
has *route=FERRY*, *ferry=YES* and *highway=TERTIARY* Tags. The ferry Tag should have the value of 
the highway classification instead of the additional highway Tag.

2) The OSM Way [256442950](https://www.openstreetmap.org/way/256442950), ingested as an Edge, 
has *route=FERRY* and *highway=TERTIARY* Tags. Since this is a ferry route, the Tag should be 
*ferry=TERTIARY* and not *highway=TERTIARY.*

#### Code Review

In Atlas, OSM elements are represented as Points, Lines, Edges, Areas and Relations. In our case, we're
looking at edges. 
The incoming Atlas object must meet the following criteria to be considered for flagging:
* Has to be a Main Edge and its OSM id should not have already been flagged
* Has a *route=FERRY* Tag
* Has a highway Tag  with priority greater than or equal to a minimum highway type. The default 
minimum highway type is PATH which includes MOTORWAY, MOTORWAY_LINK, TRUNK,TRUNK_LINK, PRIMARY, 
PRIMARY_LINK, SECONDARY, SECONDARY_LINK, TERTIARY, TERTIARY_LINK, UNCLASSIFIED, RESIDENTIAL, 
SERVICE, TRACK, LIVING_STREET, PEDESTRIAN, BUS_GUIDEWAY, RACEWAY, ROAD, CYCLEWAY, FOOTWAY, 
BRIDLEWAY, STEPS and PATH. 
The minimum highway type is set as a configurable so that the default minimum type can be overridden.

The object is then flagged with appropriate instructions based on the following conditions:
* If the object has a Ferry Tag and is not of the same classification as the Highway Tag
* If the object has a Ferry Tag and is of the same classification as the Highway Tag
* If the object has no Ferry Tag


###### More Information
For more information, see the source code in 
[HighwayToFerryTagCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/HighwayToFerryTagCheck.java).


