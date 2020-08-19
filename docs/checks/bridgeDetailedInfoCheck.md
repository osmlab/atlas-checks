# Bridge Detailed Info Check

#### Description

This check flags railway and major highway bridges longer than X meters (configurable) which only have a generic 'bridge=yes' tag without any details (bridge type or structure).

The minimum bridge length to qualify for this check is configurable. The default is 500 meters.

This is a port of Osmose check #7012.

#### Live examples

Way [id:4407849](https://www.openstreetmap.org/way/4407849) represents a prominent bridge - it is a section of a motorway and its length is greater than 500m - but it only has a generic `bridge=yes` tag.  

#### Code review

The validation section ensures that the Atlas object being evaluated is an [Edge](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java) with the following tags:
* `bridge=yes`
* `railway` -or- `highway` with one of the values: `motorway`, `trunk`, `primary`, `secondary`.
Only _master_ Edges are valid candidates for this check, to avoid duplicate flags on a single bi-directional Way.
 
The check section flags a valid candidate Edge if it is longer than the configurable minimum but does not have a `bridge:structure` tag.
 
To learn more, please look at the source code for the check:
[BridgeDetailedInfoCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/BridgeDetailedInfoCheck.java)
