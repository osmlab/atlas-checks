# HighwayMissingNameAndRefTagCheck

#### Description
This check detects highways that are missing a name and ref tag. At least one of them is required.

#### Configurables
- ***min.contiguous.angle*** - Minimum angle defining contiguous ways.

#### Live Examples

Primary road [way:174686797](https://www.openstreetmap.org/way/174686797) is missing both name and ref tag.

#### Code Review
This check evaluates [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java), flagging them if they are missing
both name and ref tags.

##### Validating the Object
We first validate that the incoming object is:
* Is an `Edge`
* Is MainEdge
* HighwayTag greater than or equal to 'tertiary'
* Not a roundabout
* Not circular

##### Flagging the Edge
Flag edge if it is missing both name and ref tag.

##### AutoFix Suggestion
NA


To learn more about the code, please look at the comments in the source code for the check.  
[HighwayMissingNameAndRefTagCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/HighwayMissingNameAndRefTagCheck.java)
