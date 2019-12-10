# Available Checks
This document is a list of tables with a description and link to documentation for each check.  Each table is organized by check type.

## Areas
| Check Name | Check Description |
| :--------- | :---------------- |
| [AreasWithHighwayTagCheck](checks/areasWithHighwayTagCheck.md) | The purpose of this check is to identify Areas attributed with highway tags. |
| [PoolSizeCheck](tutorials/tutorial1-PoolSizeCheck.md) | The purpose of this check is to identify pools that are larger than 5,000,000 square meters or smaller than 5 square meters.  This check was created to be used as a tutorial for developing new checks. |
| [OverlappingAOIPolygonCheck](checks/overlappingAOIPolygonCheck.md) | The purpose of this check is to identify areas of interest (AOIs) that are overlapping one another. |
| [PedestrianAreaOverlappingEdgeCheck](checks/pedestrianAreaOverlappingEdgeCheck.md) | The purpose of this check is to identify pedestrian areas overlapping with roads that are not snapped to car navigable edges. |
| [ShadowDetectionCheck](checks/shadowDetectionCheck.md) | The purpose of this check is to identify floating buildings. |
| [SpikyBuildingCheck](checks/spikyBuildingCheck.md) | The purpose of this check is to identify buildings with extremely sharp angles in their geometry. |
| [WaterbodyAndIslandSizeCheck](checks/waterbodyAndIslandSizeCheck.md) | The purpose of this check is to identify waterbodies and islands which are either too small or too large in size. |

## Highways
| Check Name | Check Description |
| :--------- | :---------------- |
| [EdgeCrossingEdgeCheck](checks/edgeCrossingCheck.md) | The purpose of this check is to identify Edges intersecting another Edge(s) that do not share the same Node (meaning they are not well-connected) nor have proper layer tagging on one of these Edge(s) (meaning there should be a layer tag for one of the Edges). |
| [FloatingEdgeCheck](checks/floatingEdgeCheck.md) | The purpose of this check is to identify Edges that are not accessible or navigable from the rest of the Edge network due to lack of connectivity or access restrictions. |
| [MalformedRoundaboutCheck](checks/malformedRoundaboutCheck.md) | The purpose of this check is to identify roundabouts mapped in the opposite direction of traffic. The check takes into consideration countries with both left-side and right-side traffic. There are three types of map errors that will be flagged by this check: 1. Wrong-way-roundabouts, 2. Multi-directional roundabouts, and 3. Roundabouts with poor geometry. |
| [~~RoundaboutClosedLoopCheck~~ (Deprecated)](checks/roundaboutClosedLoopCheck.md) | The purpose of this check is to identify Roundabout Edges that are bi-directional or have an end Node with less than 2 connections.  **This check has been deprecated and is no longer active.** |
| [RoundaboutConnectorCheck](checks/roundaboutConnectorCheck.md) | The purpose of this check is to identify roads that connect to a roundabout at too sharp an angle |
| [RoundaboutValenceCheck](checks/roundaboutValenceCheck.md) | The purpose of this check is to identify OpenStreetMap (OSM) tagged roundabouts that have an unusual number of edges connected to them. |
| [SharpAngleCheck](checks/sharpAngleCheck.md) | The purpose of this check is to identify roads with angles that are too sharp. Sharp angles may indicate inaccurate digitization once a certain threshold is exceeded. |
| [SignPostCheck](checks/signPostCheck.md) | The purpose of this check is to identify On-/Off-Ramps in motorways and trunk highways that are not relaying information from their respective sign posts. |
| [SingleSegmentMotorwayCheck](checks/singleSegmentMotorwayCheck.md) | The purpose of this check is to identify ways tagged with highway=motorway that are not connected to any ways tagged the same. |
| [SinkIslandCheck](tutorials/tutorial3-SinkIslandCheck.md) | The purpose of this check is to identify whether a network of car-navigable Edges can be exited. |
| [SnakeRoadCheck](checks/snakeRoadCheck.md) | The purpose of the SnakeRoad check is to identify roads that should be split into two or more roads. |
| [InvalidPiersCheck](checks/invalidPiersCheck.md) | The purpose of this check is to identify piers(OSM Ways with man_made=pier tag) that are ingested in Atlas as edges with linear or polygonal geometry without an area=yes tag |

## Nodes
| Check Name | Check Description |
| :--------- | :---------------- |
| [DuplicateNodeCheck](docs/checks/duplicateNodeCheck.md) | The purpose of this check is to identify Nodes that are in the exact same location. |
| [InvalidMiniRoundaboutCheck](checks/invalidMiniRoundaboutCheck.md) | The purpose of this check is to identify invalid mini-roundabouts (i.e. roundabouts that share the same rules as other roundabouts, but present as painted circles rather than physical circles). |
| [OrphanNodeCheck](tutorials/tutorial2-OrphanNodeCheck.md) | The purpose of this check is to identify untagged and unconnected Nodes in OSM. |

## Points
| Check Name | Check Description |
| :--------- | :---------------- |
| [AddressPointMatch](checks/addressPointMatch.md) | The purpose of this check is to identify improperly tagged street names (addr:street) on features that already have an addr:housenumber tag. This includes cases where the addr:street tag doesn't exist or is null. |
| [AddressStreetNameCheck](checks/addressStreetNameCheck.md) | The purpose of this check is to identify nodes with addr:street names that don't match the surrounding roads. |
| [DuplicatePointCheck](checks/duplicatePointCheck.md) | The purpose of this check is to identify Points in OSM that are in the same location. |


## Relations
| Check Name | Check Description |
| :--------- | :---------------- |
| [InvalidTurnRestrictionCheck](checks/invalidTurnRestrictionCheck.md) | The purpose of this check is to identify invalid turn restrictions in OSM. Invalid turn restrictions occur in a variety of ways from invalid members, Edge geometry issues, not being routable, or wrong topology. |
| [OneMemberRelationCheck](checks/oneMemberRelationCheck.md) | The purpose of this check is to identify Relations in OSM which only have one Member. |

## Tags
| Check Name | Check Description |
| :--------- | :---------------- |
| [ConflictingAreaTagCombination](checks/conflictingAreaTagCombination.md) | The purpose of this check is to identify Areas with invalid tag combinations. |
| [HighwayToFerryTagCheck](checks/highwayToFerryTagCheck.md) | The purpose of this check is to identify all Edges with route=FERRY and highway=PATH (or higher). |
| [InvalidAccessTagCheck](checks/invalidAccessTagCheck.md) | The purpose of this check is to identify invalid access tags. |
| [InvalidLanesTagCheck](docs/checks/invalidLanesTagCheck.md) | The purpose of this check is to identify highways in OSM with an invalid lanes tag value. |
| [MixedCaseNameCheck](checks/mixedCaseNameCheck.md) | The purpose of this check is to identify names that contain invalid mixed cases so that they can be edited to be the standard format. |
| [RoadNameSpellingConsistencyCheck](checks/RoadNameSpellingConsistencyCheck.md) | The purpose of this check is to identify road segments that have a name Tag with a different spelling from that of other segments of the same road. This check is primarily meant to catch small errors in spelling, such as a missing letter, letter accent mixups, or capitalization errors. |
| [StreetNameIntegersOnlyCheck](checks/streetNameIntegersOnlyCheck.md) | The purpose of this check is to identify streets whose names contain integers only. |
| [UnusualLayerTagsCheck](checks/unusualLayerTagsCheck.md) | The purpose of this check is to identify layer tag values when accompanied by invalid tunnel and bridge tags. |

## Ways
| Check Name | Check Description |
| :--------- | :---------------- |
| [BuildingRoadIntersectionCheck](checks/buildingRoadIntersectionCheck.md) | The purpose of this check is to identify buildings that intersect/overlap roads. |
| [~~DuplicateWaysCheck~~ (Deprecated)](checks/duplicateWaysCheck.md) | The purpose of this check is to identify Ways that have either had their entire length or a segment of their length duplicated or drawn multiple times. **This check has been deprecated and is no longer active.** |
| [GeneralizedCoastlineCheck](checks/generalizedCoastlineCheck.md) | The purpose of this check is to identify coastlines whose nodes are too far apart, have angles that are too sharp, and/or have _source=PGS_ Tag values. |
| [IntersectingBuildingsCheck](checks/intersectingBuildingsCheck.md) | The purpose of this check is to identify buildings that intersect other buildings. |
| [SelfIntersectingPolylineCheck](checks/selfIntersectingPolylineCheck.md) | The purpose of this check is to identify all edges/lines/areas in Atlas that have self-intersecting polylines, or geometries that intersects itself in some form. |
