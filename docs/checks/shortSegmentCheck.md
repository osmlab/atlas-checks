# Short Segment Check

#### Description
The purpose of this check is to flag Edges which are shorter than the given configurable length, and are also connected to a Node with a connection valence less than given configurable minimum value.

#### Configuration

This check has three configurables that can be changed in the configuration file [config.json](../../config/configuration.json)

Defaults:
- ```"edge.length.maximum.meters": 1.0``` - Maximum length (meters) of edge
- ```"node.valence.minimum": 3``` - Minimum number of valence nodes
- ```"highway.priority.minimum": "service"``` - Minimum highway type for priorities 

#### Live Examples

- Edge [id:186939108](https://www.openstreetmap.org/way/186939108) is less than 1 meter and it is connected to Node [id:1922609289](https://www.openstreetmap.org/node/1922609289) that has less than 3 connections.
- Edge [id:54358738](https://www.openstreetmap.org/way/54358738) is less than 1 meter and it is connected to Node [id:685072915](https://www.openstreetmap.org/node/685072915) that has less than 3 connections.
- Edge [id:859429484](https://www.openstreetmap.org/way/859429484) is less than 1 meter and it is connected to Node [id:8010983331](https://www.openstreetmap.org/node/8010983331) that has less than 3 connections.


Please see the source code for ShortSegmentCheck here: [ShortSegmentCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/ShortSegmentCheck.java)