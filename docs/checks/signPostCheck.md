# SignPostCheck

#### Description

The purpose of this check is to identify On-/Off-Ramps in motorways and trunk highways that are not relaying information from their respective sign posts.

#### Live Examples

1. Line [id:124741413](https://www.openstreetmap.org/way/124741413) represents the Edge on the Off-Ramp that should have a destination tag, which is currently missing.
2. Line [id:16613326](https://www.openstreetmap.org/way/16613326) shows the Edge on the Off-Ramp should have a destination tag, which is currently missing.  Also, the Node that branched off the regular motorway and entered the Off-Ramp should have a highway=MOTORWAY_JUNCTION tag, which is currently missing.

#### Code Review

Sign post information should be tagged at the 1st Edge of the On-/Off-Ramps for the highway.
This check flags:

- The 1st Edge of an On-/Off-Ramp that does not have a destination tag
- The starting Node of the 1st Edge of an On-/Off-Ramp which does not have tag highway=MOTOWAY_JUNCTION

In OSM, there is no specific tag to represent On-/Off-Ramps. There are three steps to search the 1st Edge of the On-/Off-Ramps: 1) define a “possible ramp edge”; 2) search On-/Off-Ramps; and 3) find the 1st  Edge of the On Ramp. More detail of detection is mentioned in the reference section.

To learn more about the code, please look at the comments in the source code for the check.
[SignPostCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/SignPostCheck.java)