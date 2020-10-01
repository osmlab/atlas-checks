# Road name spelling consistency check

This is a new check that's designed to flag road segments whose Name tags have slightly different spellings. "Slightly different spelling" is understood to mean an edit distance of 1 between two Name tags. The goal is to bring attention to areas of a road with spelling mixups so that editors can create a uniform spelling for the entire road.

This check should flag road segments whose Name tags contain:
1. Accented character inconsistencies
2. Possessive apostrophe misplacement/absence
3. Other character additions/deletions

The check provides the user with a collection of flagged road segments and their names. The user should use their best judgement to decide which spelling to apply to all flagged segments.

### Live Examples

1. Edge [id:653186608](https://www.openstreetmap.org/way/653186608) is spelled Whidbourne, and other parts of the road are spelled Whidborne.
2. Edge [id:26304375](https://www.openstreetmap.org/way/26304375) is spelled Kent Stret, and other nearby roads are called Kent Street.
3. Edge [id:28509866](https://www.openstreetmap.org/way/28509866) is spelled Godwin Court, and a service road just to the east is called Goodwin Court.

### Code Review

The check starts by ensuring a given AtlasObject is a named Main Edge that has not been flagged and is of a high enough HighwayTag priority. The check then collects all Edges that have a common connection to the starting Edge and are also within a configurable search area. The collected Edges are filtered for those that have names that are at an edit distance of 1 from the starting Edge's name. However, there are exceptions in this process. If two Edges' names contain different numbers or space-delimited characters, they are not considered to have inconsistent spelling with one another and are not flagged.
Any Edges whose names are within 1 edit distance of each other are flagged.

For the source code for this check, please refer to [RoadNameSpellingConsistencyCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/RoadNameSpellingConsistencyCheck.java)
