# AreasWithHighwayTagCheck

#### Description

The purpose of this check is to identify Areas attributed with invalid _highway_ Tags. Areas are defined as the Atlas Area Object, not the explicit OSM area definition. An Area will be flagged if it has a _highway_ Tag attached to it, unless the _highway_ Tag is part of the `VALID_HIGHWAY_TAGS` set. Any Area that is flagged an invalid _highway_ Tag will have the invalid _highway_ Tag removed.

The set of `VALID_HIGHWAY_TAGS` is defined in the source code as:
```
private static final EnumSet<HighwayTag> VALID_HIGHWAY_TAGS = EnumSet.of(HighwayTag.SERVICES,
               HighwayTag.SERVICE, HighwayTag.REST_AREA, HighwayTag.PEDESTRIAN, HighwayTag.PLATFORM);
```
Therefore, Areas with _highway=SERVICES_, _highway=SERVICE_, _highway=REST\_AREA_, _highway=PEDESTRIAN_ and _highway=PLATFORM_ will not be flagged by this check.

#### Live Examples

1. Line [id:227275283](https://www.openstreetmap.org/way/227275283) is an area with an invalid highway Tag.
2. Line [id:231865304](https://www.openstreetmap.org/way/231865304) is an area with an invalid highway Tag.

#### Code Review

The check ensures that the Atlas object being evaluated is an Area. The tags associated with that Area are then verified to not be _highway_ Tags, unless they are part of the `VALID_HIGHWAY_TAGS` set. If the Area contains a _highway_ Tag that is not part of the `VALID_HIGHWAY_TAGS` set, then the _highway_ Tag will be removed.

To learn more about the code, please look at the comments in the source code for the check.
[AreasWithHighwayTagCheck.java﻿](../../src/main/java/org/openstreetmap/atlas/checks/validation/areas/AreasWithHighwayTagCheck.java)