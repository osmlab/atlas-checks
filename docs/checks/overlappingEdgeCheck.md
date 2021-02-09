# Overlapping Edge Check

#### Description
The purpose of this check is to identify Edges which overlap another Edge.  Overlapping Edges share two consecutive Nodes. 

#### Configuration

This check has two configurables that can be changed in the configuration file [config.json](../../config/configuration.json)

Defaults:
- ```"highway.priority.minimum": "residential"``` - Minimum highway type for priority
- ```"pedestrian.areas.filter": true``` - Whether or not to filter out pedestrian areas

#### Live Examples

- Part of [id:851925421](https://www.openstreetmap.org/way/851925421) is overlapping part of [id:55081553](https://www.openstreetmap.org/way/55081553)
- Part of [id:857538513](https://www.openstreetmap.org/way/857538513) is overlapping part of [id:130585687](https://www.openstreetmap.org/way/130585687)
- Part of [id:125452478](https://www.openstreetmap.org/way/125452478) is overlapping part of [id:230343727](https://www.openstreetmap.org/way/230343727)

Please see the source code for OverlappingEdgeCheck here: [OverlappingEdgeCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/OverlappingEdgeCheck.java)