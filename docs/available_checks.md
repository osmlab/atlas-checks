# Available Checks
This document is a list of tables with a description and link to documentation for each check.  Each table is organized by check type.

## Areas
| Check Name | Check Description |
| :--------- | :---------------- |
| [AreasWithHighwayTagCheck](checks/areasWithHighwayTagCheck.md) | The purpose of this check is to identify Areas attributed with highway tags. |
| [OceanBleedingCheck](checks/oceanBleedingCheck.md) | The purpose of this check is to identify streets, buildings, and railways that bleed into (intersect) an ocean feature. |
| [OverlappingAOIPolygonCheck](checks/overlappingAOIPolygonCheck.md) | The purpose of this check is to identify areas of interest (AOIs) that are overlapping one another. |
| [PedestrianAreaOverlappingEdgeCheck](checks/pedestrianAreaOverlappingEdgeCheck.md) | The purpose of this check is to identify pedestrian areas overlapping with roads that are not snapped to car navigable edges. |
| [PoolSizeCheck](tutorials/tutorial1-PoolSizeCheck.md) | The purpose of this check is to identify pools that are larger than 5,000,000 square meters or smaller than 5 square meters.  This check was created to be used as a tutorial for developing new checks. |
| [ShadowDetectionCheck](checks/shadowDetectionCheck.md) | The purpose of this check is to identify floating buildings. |
| [SpikyBuildingCheck](checks/spikyBuildingCheck.md) | The purpose of this check is to identify buildings with extremely sharp angles in their geometry. |
| [WaterAreaCheck](checks/waterAreaCheck.md) | Find overlapping water areas and water areas that should have a waterway. |
| [WaterbodyAndIslandSizeCheck](checks/waterbodyAndIslandSizeCheck.md) | The purpose of this check is to identify waterbodies and islands which are either too small or too large in size. |

## Highways
| Check Name | Check Description |
| :--------- | :---------------- |
| [AtGradeSignPostCheck](checks/atGradeSignPostCheck.md) | The purpose of this check is to identify at-grade intersections that are not part of destination sign relations. |
| [EdgeCrossingEdgeCheck](checks/edgeCrossingCheck.md) | The purpose of this check is to identify Edges intersecting another Edge(s) that do not share the same Node (meaning they are not well-connected) nor have proper layer tagging on one of these Edge(s) (meaning there should be a layer tag for one of the Edges). |
| [FloatingEdgeCheck](checks/floatingEdgeCheck.md) | The purpose of this check is to identify Edges that are not accessible or navigable from the rest of the Edge network due to lack of connectivity or access restrictions. |
| InconsistentRoadClassificationCheck | The purpose of this check is to identify roads, that transition from one classification to another and then back to the original classification. |
| [InvalidPiersCheck](checks/invalidPiersCheck.md) | The purpose of this check is to identify piers(OSM Ways with man_made=pier tag) that are ingested in Atlas as edges with linear or polygonal geometry without an area=yes tag |
| LongSegmentCheck | This check identifies long segments/edges (length is more than minimumLength). |
| [MalformedRoundaboutCheck](checks/malformedRoundaboutCheck.md) | The purpose of this check is to identify roundabouts mapped in the opposite direction of traffic. The check takes into consideration countries with both left-side and right-side traffic. There are three types of map errors that will be flagged by this check: 1. Wrong-way-roundabouts, 2. Multi-directional roundabouts, and 3. Roundabouts with poor geometry. |
| OverlappingEdgeCheck | The purpose of this check is to identify edges that share the same two consecutive geometry points. |
| RoadLinkCheck |  Verifies that one end or the other of an edge is a fork to/from a road of the same class, that is not a _link. |
| [~~RoundaboutClosedLoopCheck~~ (Deprecated)](checks/roundaboutClosedLoopCheck.md) | The purpose of this check is to identify Roundabout Edges that are bi-directional or have an end Node with less than 2 connections.  **This check has been deprecated and is no longer active.** |
| [RoundaboutConnectorCheck](checks/roundaboutConnectorCheck.md) | The purpose of this check is to identify roads that connect to a roundabout at too sharp an angle |
| [RoundaboutValenceCheck](checks/roundaboutValenceCheck.md) | The purpose of this check is to identify OpenStreetMap (OSM) tagged roundabouts that have an unusual number of edges connected to them. |
| [RoundaboutMissingTag](checks/roundaboutMissingTagCheck.md) | The purpose of this check is to identify Roundabouts with missing junction=roundabout tag. Candidate must be navigable, closed and round shape OSM Way that intersects with at least two navigable roads. |
| [SharpAngleCheck](checks/sharpAngleCheck.md) | The purpose of this check is to identify roads with angles that are too sharp. Sharp angles may indicate inaccurate digitization once a certain threshold is exceeded. |
| ShortSegmentCheck |  The purpose of this check is to identify short segments/edges (length is less than a configured minimum length) that have a node with less than or equal to a configured node valence connections. |
| [SignPostCheck](checks/signPostCheck.md) | The purpose of this check is to identify On-/Off-Ramps in motorways and trunk highways that are not relaying information from their respective sign posts. |
| [SingleSegmentMotorwayCheck](checks/singleSegmentMotorwayCheck.md) | The purpose of this check is to identify ways tagged with highway=motorway that are not connected to any ways tagged the same. |
| [SinkIslandCheck](tutorials/tutorial3-SinkIslandCheck.md) | The purpose of this check is to identify whether a network of car-navigable Edges can be exited. |
| [SnakeRoadCheck](checks/snakeRoadCheck.md) | The purpose of the SnakeRoad check is to identify roads that should be split into two or more roads. |
| [SuddenHighwayTypeChangeCheck](checks/suddenHighwayTypeChangeCheck.md) | The purpose of this check is to identify roads that jump to much different highway classifications. |
| UnwalkableWaysCheck | The purpose of this check is to identify any non-motorway single carriageway edges with no foot tags that cross any high-priority roads that are dual carriageways. |
| ValenceOneImportantRoadCheck | This check identifies important roads that either start or end with valance-1 nodes. |

## Nodes
| Check Name | Check Description |
| :--------- | :---------------- |
| BigNodeBadDataCheck | The purpose of this check is to flag any BigNodes that have may have some bad data. |
| ConnectivityCheck | This check identifies nodes that should be connected to nearby nodes or edges. |
| [DuplicateNodeCheck](checks/duplicateNodeCheck.md) | The purpose of this check is to identify Nodes that are in the exact same location. |
| [InvalidMiniRoundaboutCheck](checks/invalidMiniRoundaboutCheck.md) | The purpose of this check is to identify invalid mini-roundabouts (i.e. roundabouts that share the same rules as other roundabouts, but present as painted circles rather than physical circles). |
| [LevelCrossingOnRailwayCheck](checks/LevelCrossingOnRailwayCheck.md) | This check identifies incorrectly tagged or missing nodes at railway/highway intersections. |
| NodeValenceCheck | This check identifies nodes with too many connections. |
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
| InvalidMultiPolygonRelationCheck |  This check is designed to scan through MultiPolygon relations and flag them for invalid geometry. |
| [InvalidTurnRestrictionCheck](checks/invalidTurnRestrictionCheck.md) | The purpose of this check is to identify invalid turn restrictions in OSM. Invalid turn restrictions occur in a variety of ways from invalid members, Edge geometry issues, not being routable, or wrong topology. |
| InvalidSignBoardRelationCheck | Identifies signboard relations that do not meet all the requirements for a signboard relation. |
| [OneMemberRelationCheck](checks/oneMemberRelationCheck.md) | The purpose of this check is to identify Relations in OSM which only have one Member. |

## Tags
| Check Name | Check Description |
| :--------- | :---------------- |
| AbbreviatedNameCheck |  The purpose of this check is to flag names that have abbreviations in them. |
| [BridgeDetailedInfoCheck](checks/bridgeDetailedInfoCheck.md) | The purpose of this check is to identify prominent bridges with unspecified type or structure. |
| [ConflictingAreaTagCombination](checks/conflictingAreaTagCombination.md) | The purpose of this check is to identify Areas with invalid tag combinations. |
| ConflictingTagCombinationCheck | This check verifies whether an atlas object has a conflicting tag combination or not. |
| [ConstructionCheck](checks/constructionCheck.md) | The purpose of this check is to identify construction tags where the construction hasn't been checked on recently, or the expected finish date has been passed. |
| [HighwayToFerryTagCheck](checks/highwayToFerryTagCheck.md) | The purpose of this check is to identify all Edges with route=FERRY and highway=PATH (or higher). |
| ImproperAndUnknownRoadNameCheck | This check flags improper road name values. |
| [InvalidAccessTagCheck](checks/invalidAccessTagCheck.md) | The purpose of this check is to identify invalid access tags. |
| [InvalidLanesTagCheck](docs/checks/invalidLanesTagCheck.md) | The purpose of this check is to identify highways in OSM with an invalid lanes tag value. |
| InvalidTagsCheck | This flags features based on configurable filters. Each filter passed contains the atlas entity classes to check and a taggable filter to test objects against. If a feature is of one of the given classes and passes the associated TaggableFilter, it is flagged. |
| [MixedCaseNameCheck](checks/mixedCaseNameCheck.md) | The purpose of this check is to identify names that contain invalid mixed cases so that they can be edited to be the standard format. |
| [RoadNameGapCheck](checks/RoadNameGapCheck.md) | The purpose of this check is to identify edge connected between two edges whose name tag is same. Flag the edge if the edge has a name tag different to name tag of edges connected to it or if there is no name tag itself.
| [RoadNameSpellingConsistencyCheck](checks/RoadNameSpellingConsistencyCheck.md) | The purpose of this check is to identify road segments that have a name Tag with a different spelling from that of other segments of the same road. This check is primarily meant to catch small errors in spelling, such as a missing letter, letter accent mixups, or capitalization errors. |
| ShortNameCheck | The short name check will validate that any and all names contain at least 2 letters in the name. |
| [StreetNameIntegersOnlyCheck](checks/streetNameIntegersOnlyCheck.md) | The purpose of this check is to identify streets whose names contain integers only. |
| [TunnelBridgeHeightLimitCheck](checks/tunnelBridgeHeightLimitCheck.md) | The purpose of this check is to identify roads with limited vertical clearance which do not have a maxheight tag. |
| [UnusualLayerTagsCheck](checks/unusualLayerTagsCheck.md) | The purpose of this check is to identify layer tag values when accompanied by invalid tunnel and bridge tags. |
| [ConditionalRestrictionCheck](checks/conditionalRestrictionCheck.md) | The purpose of this check is to identify elements that have a :conditional tag that does not respect the established format. |

## Ways
| Check Name | Check Description |
| :--------- | :---------------- |
| [ApproximateWayCheck](checks/approximateWayCheck.md) | The purpose of this check is to identify ways that are crudely drawn, there is a discrepancy between the drawing and the real way, especially for curves. |
| [BuildingRoadIntersectionCheck](checks/buildingRoadIntersectionCheck.md) | The purpose of this check is to identify buildings that intersect/overlap roads. |
| DuplicateLocationInPolyLineCheck |  Identifies OSM ways with repeating locations. |
| [~~DuplicateWaysCheck~~ (Deprecated)](checks/duplicateWaysCheck.md) | The purpose of this check is to identify Ways that have either had their entire length or a segment of their length duplicated or drawn multiple times. **This check has been deprecated and is no longer active.** |
| [GeneralizedCoastlineCheck](checks/generalizedCoastlineCheck.md) | The purpose of this check is to identify coastlines whose nodes are too far apart, have angles that are too sharp, and/or have _source=PGS_ Tag values. |
| [IntersectingBuildingsCheck](checks/intersectingBuildingsCheck.md) | The purpose of this check is to identify buildings that intersect other buildings. |
| [InvalidGeometryCheck](checks/invalidGeometryCheck.md) | This check flags invalid polyline and polygon geometries. |
| LineCrossingBuildingCheck | The purpose of this check is to identify line items (edges or lines) that are crossing buildings invalidly. |
| LineCrossingWaterBodyCheck | The purpose of this check is to identify line items (edges or lines) and optionally buildings, that cross water bodies invalidly. |
| MalformedPolyLineCheck | This check identifies lines that have only one point, or none, and the ones that are too long. |
| [SelfIntersectingPolylineCheck](checks/selfIntersectingPolylineCheck.md) | The purpose of this check is to identify all edges/lines/areas in Atlas that have self-intersecting polylines, or geometries that intersects itself in some form. |
| [WaterWayCheck](checks/waterWayCheck.md) | This check finds closed waterways (circular water motion), waterways without a place for water to go (a "sink"), crossing waterways, and waterways that go uphill (requires elevation data). |
