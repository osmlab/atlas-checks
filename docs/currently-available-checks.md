# Currently Available Checks
This document is a list of tables with a description and link to documentation for each check.  Each table is organized by check type.

## Areas
| Check Name | Check Description |
| :--------- | :---------------- |
| [PoolSizeCheck](docs/tutorials/tutorial1-PoolSizeCheck.md) | The purpose of this check is to identify pools that are larger than 5,000,000 square meters or smaller than 5 square meters.  This check was created to be used as a tutorial for developing new checks. |
| [OverlappingAOIPolygonCheck](docs/checks/overlappingAOIPolygonCheck.md) | The purpose of this check is to identify areas of interest (AOIs) that are overlapping one another. |
| [ShadowDetectionCheck](docs/checks/shadowDetectionCheck.md) | The purpose of this check is to identify floating buildings. |
| [SpikyBuildingCheck](docs/checks/spikyBuildingCheck.md) | The purpose of this check is to identify buildings with extremely sharp angles in their geometry. |

## Highways
| Check Name | Check Description |
| :--------- | :---------------- |
| [FloatingEdgeCheck](docs/checks/floatingEdgeCheck.md) | The purpose of this check is to identify Edges that are not accessible or navigable from the rest of the Edge network due to lack of connectivity or access restrictions. |
| [MalformedRoundaboutCheck](docs/checks/malformedRoundaboutCheck.md) | The purpose of this check is to identify roundabouts mapped in the opposite direction of traffic. The check takes into consideration countries with both left-side and right-side traffic. There are three types of map errors that will be flagged by this check: 1. Wrong-way-roundabouts, 2. Multi-directional roundabouts, and 3. Roundabouts with poor geometry. |
| [~~RoundaboutClosedLoopCheck~~ (Deprecated)](docs/checks/roundaboutClosedLoopCheck.md) | The purpose of this check is to identify Roundabout Edges that are bi-directional or have an end Node with less than 2 connections.  **This check has been deprecated and is no longer active.** |
| [RoundaboutValenceCheck](docs/checks/roundaboutValenceCheck.md) | The purpose of this check is to identify OpenStreetMap (OSM) tagged roundabouts that have an unusual number of edges connected to them. |
| [SharpAngleCheck](docs/checks/sharpAngleCheck.md) | The purpose of this check is to identify roads with angles that are too sharp. Sharp angles may indicate inaccurate digitization once a certain threshold is exceeded. |
| [SingleSegmentMotorwayCheck](docs/checks/singleSegmentMotorwayCheck.md) | The purpose of this check is to identify ways tagged with highway=motorway that are not connected to any ways tagged the same. |
| [SinkIslandCheck](docs/tutorials/tutorial3-SinkIslandCheck.md) | The purpose of this check is to identify whether a network of car-navigable Edges can be exited. |
| [SnakeRoadCheck](docs/checks/snakeRoadCheck.md) | The purpose of the SnakeRoad check is to identify roads that should be split into two or more roads. |

## Nodes
| Check Name | Check Description |
| :--------- | :---------------- |
| [AddressPointMatch](dev/docs/checks/addressPointMatch.md) | The purpose of this check is to identify improperly tagged street names (addr:street) on features that already have an addr:housenumber tag. This includes cases where the addr:street tag doesn't exist or is null. |
| [AddressStreetNameCheck](docs/checks/addressStreetNameCheck.md) | The purpose of this check is to identify nodes with addr:street names that don't match the surrounding roads. |
| [DuplicateNodeCheck](docs/checks/duplicateNodeCheck.md) | The purpose of this check is to identify Nodes that are in the exact same location. |
| [InvalidMiniRoundaboutCheck](docs/checks/invalidMiniRoundaboutCheck.md) | The purpose of this check is to identify invalid mini-roundabouts (i.e. roundabouts that share the same rules as other roundabouts, but present as painted circles rather than physical circles). |
| [OrphanNodeCheck](docs/tutorials/tutorial2-OrphanNodeCheck.md) | The purpose of this check is to identify untagged and unconnected Nodes in OSM. |

## Points
| Check Name | Check Description |
| :--------- | :---------------- |
| [DuplicatePointCheck](docs/checks/duplicatePointCheck.md) | The purpose of this check is to identify ﻿Points﻿ in OSM that are in the same location. |


## Relations
| Check Name | Check Description |
| :--------- | :---------------- |
| [InvalidTurnRestrictionCheck](docs/checks/invalidTurnRestrictionCheck.md) | The purpose of this check is to identify invalid turn restrictions in OSM. Invalid turn restrictions occur in a variety of ways from invalid members, Edge geometry issues, not being routable, or wrong topology. |
| [OneMemberRelationCheck](docs/checks/oneMemberRelationCheck.md) | The purpose of this check is to identify Relations in OSM which only have one Member. |

## Tags
| Check Name | Check Description |
| :--------- | :---------------- |
| [ConflictingAreaTagCombination](docs/checks/conflictingAreaTagCombination.md) | The purpose of this check is to identify Areas with invalid tag combinations. |
| [HighwayToFerryTagCheck](docs/checks/highwayToFerryTagCheck.md) | The purpose of this check is to identify all Edges with route=FERRY and highway=PATH (or higher). |
| [InvalidAccessTagCheck](docs/checks/invalidAccessTagCheck.md) | The purpose of this check is to identify invalid access tags. |
| [InvalidLanesTagCheck](docs/checks/invalidLanesTagCheck.md) | The purpose of this check is to identify highways in OSM with an invalid lanes tag value. |
| [MixedCaseNameCheck](docs/checks/mixedCaseNameCheck.md) | The purpose of this check is to identify names that contain invalid mixed cases so that they can be edited to be the standard format. |
| [RoadNameSpellingConsistencyCheck](docs/checks/RoadNameSpellingConsistencyCheck.md) | The purpose of this check is to identify road segments that have a name Tag with a different spelling from that of other segments of the same road. This check is primarily meant to catch small errors in spelling, such as a missing letter, letter accent mixups, or capitalization errors. |
| [StreetNameIntegersOnlyCheck](docs/checks/streetNameIntegersOnlyCheck.md) | The purpose of this check is to identify streets whose names contain integers only. |

## Ways
| Check Name | Check Description |
| :--------- | :---------------- |
| [BuildingRoadIntersectionCheck](docs/checks/buildingRoadIntersectionCheck.md) | The purpose of this check is to identify buildings that intersect/overlap roads. |
| [~~DuplicateWaysCheck~~ (Deprecated)](docs/checks/duplicateWaysCheck.md) | The purpose of this check is to identify Ways that have either had their entire length or a segment of their length duplicated or drawn multiple times. **This check has been deprecated and is no longer active.** |
| [GeneralizedCoastlineCheck](https://github.com/osmlab/atlas-checks/blob/dev/docs/checks/generalizedCoastlineCheck.md) | The purpose of this check is to identify coastlines whose nodes are too far apart. |
| [PedestrianAreaOverlappingEdgeCheck](docs/checks/pedestrianAreaOverlappingEdgeCheck.md) | The purpose of this check is to identify pedestrian areas overlapping with roads that are not snapped to car navigable edges. |
| [SelfIntersectingPolylineCheck](docs/checks/selfIntersectingPolylineCheck.md) | The purpose of this check is to identify all edges/lines/areas in Atlas that have self-intersecting polylines, or geometries that intersects itself in some form. |
| [WaterbodyAndIslandSizeCheck](docs/checks/waterbodyAndIslandSizeCheck.md) | The purpose of this check is to identify waterbodies and islands which are either too small or too large in size. |
