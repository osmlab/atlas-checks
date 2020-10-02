# Tunnel/Bridge Height Limit Check

#### Description

This check identifies roads of limited vertical clearance (tunnels, covered roads, and roads crossed by bridges) that do not have 'maxheight' or 'maxheight:physical' tags set. 
The valid highway classes for this check (configurable) are:
MOTORWAY_LINK, TRUNK_LINK, PRIMARY, PRIMARY_LINK, SECONDARY, SECONDARY_LINK

This is a port of Osmose check #7130.

#### Live Examples

1. Way [id:17659494](https://www.openstreetmap.org/way/17659494) is a tunnel without maxheight tag.
2. Way [id:601626442](https://www.openstreetmap.org/way/601626442) is a covered road without maxheight tag.
3. Way [id:174319379](https://www.openstreetmap.org/way/174319379) passes under a pair of bridges:
[id:11778321](https://www.openstreetmap.org/way/11778321) and [id:11778325](https://www.openstreetmap.org/way/11778325).
But it does not have a maxheight tag.

#### Code Review

The check ensures that the Atlas object being evaluated is an [Edge](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)
which is a _master_ edge (positive ID) and its related OSM Way ID has not yet been flagged. This is done in the validation step.
Once an edge is found to be valid, the verification step first checks (using standard Validators) if the edge is a _tunnel_ or a _covered_ road,
it has an expected _highway_ class and no _maxheight_ tags are present. The edge is then flagged.
If the edge has not been flagged, but it is tagged as a _bridge_, then any edges that intersect the bridge's bounding box are retrieved.
For each of those new target objects, the algorithm verifies the same criteria (the edge is a master edge, with OSM ID that has not yet been flagged,
it is of expected highway class and does not have maxheight tags). Additionally, it checks if the edge's polyline properly crosses the bridge's polyline,
to exclude any edges that might have been caught in the bounding box but do not actually intersect with the bridge, or only touch it at either end.
For each edge which passed through all these filters, a separate fixing instruction is added and a single flag is created. 

To learn more about the code, please look at the comments in the source code for the check.
[TunnelBridgeHeightLimitCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/TunnelBridgeHeightLimitCheck.java)
