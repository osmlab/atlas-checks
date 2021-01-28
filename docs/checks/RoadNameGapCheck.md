# Road name gap check

This is a new check that's designed to flag edges whose name tag is either different to the edges connecting to it with same name tag 
or if there is no name tag itself for the edge which is connected between two edges with same name tag.
The goal is to flag the above mentioned edges so that editors can either add the similar tag name to it or correct the existing tag name.

i) This check should flag the edges who has NO name tag with below conditions:
   1. Is an Edge
   2. Has the Tag,
      a. highway=TERTIARY, SECONDARY, PRIMARY, TRUNK, or MOTORWAY
   3. Does NOT have the Tag,
      a. name=*
      b. highway=*_LINK
         1. TERTIARY_LINK, SECONDARY_LINK, PRIMARY_LINK, TRUNK_LINK, or MOTORWAY_LINK
      c. junction=ROUNDABOUT
   
   For this case a fix suggestion to add the name tag with the same value as the connected edges is issued.
   
ii) This check should flag the edges which has different Name Tag:
    1. Is an Edge
    2. Has the Tag,
        1. highway=TERTIARY, SECONDARY, PRIMARY, TRUNK, or MOTORWAY.
    3. name=* but is not the same as the other Edges it is connected to
    4. Does NOT have the Tag,
        a. highway=*_LINK
        b. junction=ROUNDABOUT

For this case a fix suggestion to modify the name tag with the value from the connected edges is issued.  
    
### Live Examples

Case 1: No name tag between two Edges with name tag (HKG)*
a. Way 548524582 (https://www.openstreetmap.org/way/548524582) does not have the Tag, name=* and is 
connected to Way 447864651 (https://www.openstreetmap.org/way/447864651) and Way 528677149 (https://www.openstreetmap.org/way/528677149) 
that have the Tag, name=連翔道 Lin Cheung Road https://www.openstreetmap.org/way/548524582
b. Edge name: Tai. Incoming edge names: Tai , Shai. Outgoing edge name: Shai.
c. Edge name: Tai. Incoming edge names: Shai. Outgoing edge name: Tai, Shai.
d.: Edge name: Tai. Incoming edges names: Tai, Shai. Outgoing edge names: Shai, Pendler.

Case 2: Different name tag between two Edges with same name tag (ISO)
 
### Code Review

For the source code for this check, please refer to [RoadNameGapCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/RoadNameGapCheck.java)
