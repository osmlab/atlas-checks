# Atlas Checks

[![Build Status](https://travis-ci.org/osmlab/atlas-checks.svg?branch=master)](https://travis-ci.org/osmlab/atlas-checks)
[![quality gate](https://sonarcloud.io/api/project_badges/measure?project=org.openstreetmap.atlas%3Aatlas-checks&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.openstreetmap.atlas%3Aatlas-checks)
[![Maven Central](https://img.shields.io/maven-central/v/org.openstreetmap.atlas/atlas-checks.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.openstreetmap.atlas%22%20AND%20a:%22atlas-checks%22)
[![CircleCI](https://circleci.com/gh/osmlab/atlas-checks/tree/master.svg?style=svg)](https://circleci.com/gh/osmlab/atlas-checks/tree/master)

---

The Atlas Checks framework and standalone application are tools to enable quality assurance of Atlas data files. For more information on the Atlas mapping file format please see the [Atlas project](http://github.com/osmlab/atlas) in Github.

## Starting with Atlas Checks

Please see the [contributing guidelines](https://github.com/osmlab/atlas/blob/dev/CONTRIBUTING.md)!

### Requirements
To run Atlas Checks the following is required:
1. [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
2. [Gradle](https://gradle.org/install)
3. [Git Command Line Tools](https://git-scm.com/downloads)

### Run Atlas Checks
To start working with Atlas Checks follow the steps below:
1. Clone Atlas Checks project using the following command `git clone https://github.com/osmlab/atlas-checks.git`
2. Switch to newly created directory: `cd atlas-checks`
3. Execute `./gradlew run`

This command will build and run Atlas Checks with all the default options against a sample OSM PBF of Belize downloaded from [here](https://apple.box.com/s/3k3wcc0lq1fhqgozxr4mdi0llf95byo3). GeoJSON output will be produced that contains all the results found from the run. Those outputs will be found in `atlas-checks/build/examples/data/output`. For more information on running Atlas Checks as a standalone application click [here](docs/standalone.md).

## Working with Configuration
See [configuration docs](docs/configuration.md) for more information about the configuration files that can be used to define specific details around the Atlas Checks application.

## Running Atlas Checks in Spark Cluster
Atlas Checks have been developed to take advantage of distributed computing by running the checks in Spark. For more information on Spark see [spark.apache.org](http://spark.apache.org/). Running Atlas Checks locally is already executed within a local Spark environment on your machine, so running Spark in a cluster is simply a matter of updating the configuration. For more information see [Running Atlas Checks in a Spark Cluster](docs/cluster.md)

## Developing your own Atlas Checks
See [Development docs](docs/dev.md) for more information about developing and best practices for new Atlas Checks.

## Currently Available Checks

| Check Type | Check Name | Check Description |
| :--------- | :--------- | :---------------- |
| Areas | [PoolSizeCheck](docs/tutorials/tutorial1-PoolSizeCheck.md) | The purpose of this check is to identify pools that are larger than 5,000,000 square meters or smaller than 5 square meters.  This check was created to be used as a tutorial for developing new checks. |
| Areas | [OverlappingAOIPolygonCheck](docs/checks/overlappingAOIPolygonCheck.md) | The purpose of this check is to identify areas of interest (AOIs) that are overlapping one another. |
| Areas | [ShadowDetectionCheck](docs/checks/shadowDetectionCheck.md) | The purpose of this check is to identify floating buildings. |
| Areas | [SpikyBuildingCheck](docs/checks/spikyBuildingCheck.md) | The purpose of this check is to identify buildings with extremely sharp angles in their geometry. |
| Highways | [FloatingEdgeCheck](docs/checks/floatingEdgeCheck.md) | The purpose of this check is to identify Edges that are not accessible or navigable from the rest of the Edge network due to lack of connectivity or access restrictions. |
| Highways | [MalformedRoundaboutCheck](docs/checks/malformedRoundaboutCheck.md) | The purpose of this check is to identify roundabouts mapped in the opposite direction of traffic. The check takes into consideration countries with both left-side and right-side traffic. There are three types of map errors that will be flagged by this check: 1. Wrong-way-roundabouts, 2. Multi-directional roundabouts, and 3. Roundabouts with poor geometry. |
| Highways | [~~RoundaboutClosedLoopCheck~~ (Deprecated)](docs/checks/roundaboutClosedLoopCheck.md) | The purpose of this check is to identify Roundabout Edges that are bi-directional or have an end Node with less than 2 connections.  This check has been deprecated and is no longer active. |
| Highways | [RoundaboutValenceCheck](docs/checks/roundaboutValenceCheck.md) | The purpose of this check is to identify OpenStreetMap (OSM) tagged roundabouts that have an unusual number of edges connected to them. |
| Highways | [SharpAngleCheck](docs/checks/sharpAngleCheck.md) | The purpose of this check is to identify roads with angles that are too sharp. Sharp angles may indicate inaccurate digitization once a certain threshold is exceeded. |
| Highways | [SingleSegmentMotorwayCheck](docs/checks/singleSegmentMotorwayCheck.md) | The purpose of this check is to identify ways tagged with highway=motorway that are not connected to any ways tagged the same. |
| Highways | [SinkIslandCheck](docs/tutorials/tutorial3-SinkIslandCheck.md) | The purpose of this check is to identify whether a network of car-navigable Edges can be exited. |
| Highways | [SnakeRoadCheck](docs/checks/snakeRoadCheck.md) | The purpose of the SnakeRoad check is to identify roads that should be split into two or more roads. |
| Nodes | [AddressPointMatch](dev/docs/checks/addressPointMatch.md) | The purpose of this check is to identify improperly tagged street names (addr:street) on features that already have an addr:housenumber tag. This includes cases where the addr:street tag doesn't exist or is null. |
| Nodes | [AddressStreetNameCheck](docs/checks/addressStreetNameCheck.md) | The purpose of this check is to identify nodes with addr:street names that don't match the surrounding roads. |
| Nodes | [DuplicateNodeCheck](docs/checks/duplicateNodeCheck.md) | The purpose of this check is to identify Nodes that are in the exact same location. |
| Nodes | [InvalidMiniRoundaboutCheck](docs/checks/invalidMiniRoundaboutCheck.md) | The purpose of this check is to identify invalid mini-roundabouts (i.e. roundabouts that share the same rules as other roundabouts, but present as painted circles rather than physical circles). |
| Nodes | [OrphanNodeCheck](docs/tutorials/tutorial2-OrphanNodeCheck.md) | The purpose of this check is to identify untagged and unconnected Nodes in OSM. |
| Points | [DuplicatePointCheck](docs/checks/duplicatePointCheck.md) | The purpose of this check is to identify ﻿Points﻿ in OSM that are in the same location. |
| Relations | [InvalidTurnRestrictionCheck](docs/checks/invalidTurnRestrictionCheck.md) | The purpose of this check is to identify invalid turn restrictions in OSM. Invalid turn restrictions occur in a variety of ways from invalid members, Edge geometry issues, not being routable, or wrong topology. |
| Relations | [OneMemberRelationCheck](docs/checks/oneMemberRelationCheck.md) | The purpose of this check is to identify Relations in OSM which only have one Member. |
| Tags | [ConflictingAreaTagCombination](docs/checks/conflictingAreaTagCombination.md) | The purpose of this check is to identify Areas with invalid tag combinations. |
| Tags | [HighwayToFerryTagCheck](docs/checks/highwayToFerryTagCheck.md) | The purpose of this check is to identify all Edges with route=FERRY and highway=PATH (or higher). |
| Tags | [InvalidAccessTagCheck](docs/checks/invalidAccessTagCheck.md) | The purpose of this check is to identify invalid access tags. |
| Tags | [InvalidLanesTagCheck](docs/checks/invalidLanesTagCheck.md) | The purpose of this check is to identify highways in OSM with an invalid lanes tag value. |
| Tags | [MixedCaseNameCheck](docs/checks/mixedCaseNameCheck.md) | The purpose of this check is to identify names that contain invalid mixed cases so that they can be edited to be the standard format. |
| Tags | [RoadNameSpellingConsistencyCheck](docs/checks/RoadNameSpellingConsistencyCheck.md) | The purpose of this check is to identify road segments that have a name Tag with a different spelling from that of other segments of the same road. This check is primarily meant to catch small errors in spelling, such as a missing letter, letter accent mixups, or capitalization errors. |
| Tags | [StreetNameIntegersOnlyCheck](docs/checks/streetNameIntegersOnlyCheck.md) | The purpose of this check is to identify streets whose names contain integers only. |
| Ways | [BuildingRoadIntersectionCheck](docs/checks/buildingRoadIntersectionCheck.md) | The purpose of this check is to identify buildings that intersect/overlap roads. |
| Ways | [~~DuplicateWaysCheck~~ (Deprecated)](docs/checks/duplicateWaysCheck.md) | The purpose of this check is to identify Ways that have either had their entire length or a segment of their length duplicated or drawn multiple times. This check has been deprecated and is no longer active. |
| Ways | [PedestrianAreaOverlappingEdgeCheck](docs/checks/pedestrianAreaOverlappingEdgeCheck.md) | The purpose of this check is to identify pedestrian areas overlapping with roads that are not snapped to car navigable edges. |
| Ways | [SelfIntersectingPolylineCheck](docs/checks/selfIntersectingPolylineCheck.md) | The purpose of this check is to identify all edges/lines/areas in Atlas that have self-intersecting polylines, or geometries that intersects itself in some form. |
| Ways | [WaterbodyAndIslandSizeCheck](docs/checks/waterbodyAndIslandSizeCheck.md) | The purpose of this check is to identify waterbodies and islands which are either too small or too large in size. |
