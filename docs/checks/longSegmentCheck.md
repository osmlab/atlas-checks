# Long Segment Check

#### Description
Checks for Edges that have segments longer than the given configurable length, excluding ferry routes.

#### Configuration

This check has three configurables that can be changed in the configuration file [config.json](../../config/configuration.json)

Defaults:
- ```"length.minimum.kilometers": 10.0``` - Minimum length in kilometers for a flagged way

#### Live Examples

- [id:183926818](https://www.openstreetmap.org/way/183926818)
- [id:279683900](https://www.openstreetmap.org/way/279683900)
- [id:555861516](https://www.openstreetmap.org/way/555861516)

Please see the source code for LongSegmentCheck here: [LongSegmentCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/LongSegmentCheck.java)