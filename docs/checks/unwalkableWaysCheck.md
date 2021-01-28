# Unwalkable Ways Check

#### Description

The purpose of this check is to look for any non-motorway single carriageway Edges with no foot tags that cross any high-priority roads that are dual carriageways. _Currently this check is only valid in Singapore_.

#### Configuration

This check has two configurables that can be changed in the configuration file [config.json](../../config/configuration.json)

Defaults:
- ```"countries": ["SGP"]``` - List of enabled countries for this check.
- ```includeDualCrossingDualCarriageWays: false``` - Whether or not to include dual crossing dual carriage ways.

#### Live Examples

- Line [id:707530351](https://www.openstreetmap.org/way/707530351)
- Line [id:173767762](https://www.openstreetmap.org/way/173767762)
- Line [id:701196053](https://www.openstreetmap.org/way/701196053)

Please see the source code for UnwalkableWaysCheck here: [UnwalkableWaysCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/intersections/UnwalkableWaysCheck.java)