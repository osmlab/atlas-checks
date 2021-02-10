# Valence One Important Road Check

#### Description
The purpose of this check is to flag high priority roads that have a start or end Node with a valence of one. This has some overlap with [FloatingEdgeCheck](floatingEdgeCheck.md), though it does not have the same segment length requirements and targets high priority road segments only.

#### Configuration

This check has no configurables.

#### Live Examples

- Edge [id:868979533](https://www.openstreetmap.org/way/868979533) dead ends at Node [id:8099729922](https://www.openstreetmap.org/node/8099729922).
- Edge [id:897224763](https://www.openstreetmap.org/way/897224763) dead ends at Node [id:8339439952](https://www.openstreetmap.org/node/8339439952).


Please see the source code for ValenceOneImportantRoadCheck here: [ValenceOnceImportantRoadCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/ValenceOneImportantRoadCheck.java)