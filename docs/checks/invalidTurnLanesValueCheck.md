# Invalid Turn Lanes Value Check 

This check flags roads that have invalid `turn:lanes` tag values.

Valid values contain keywords found in 
https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/TurnTag.java
i.e. 
```java
    enum TurnType
    {
        LEFT,
        SHARP_LEFT,
        SLIGHT_LEFT,
        THROUGH,
        RIGHT,
        SHARP_RIGHT,
        SLIGHT_RIGHT,
        REVERSE,
        MERGE_TO_LEFT,
        MERGE_TO_RIGHT,
        NONE;
    }
    String TURN_LANE_DELIMITER = "\\|";
    String TURN_TYPE_DELIMITER = ";";
```
e.g. "turn:lanes":"through|through|right" is valid
e.g. "turn:lanes":"through|through|righ" is not valid


#### Live Examples

1. The way [id:730457851](https://www.openstreetmap.org/way/730457851) has an invalid `turn:lanes` 
2. The way [id:836279618](https://www.openstreetmap.org/way/836279618) has an invalid `turn:lanes:forward`

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes & Relations; in our case, we’re are looking at [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).

Our first goal is to validate the incoming Atlas object. Valid features for this check will satisfy the following conditions:

* Is an Edge
* Has a `highway` tag that is car navigable
* Has a `turn:lanes` or `turn:lanes:forward` or `turn:lanes:backward` tag
* Is not an OSM way that has already been flagged

```java
@Override
public boolean validCheckForObject(final AtlasObject object)
{
    return TurnLanesTag.hasTurnLane(object) && HighwayTag.isCarNavigableHighway(object)
            && object instanceof Edge && ((Edge) object).isMainEdge()
            && !this.isFlagged(object.getOsmIdentifier());
}
```

The valid objects (i.e. `turn:lanes` or `turn:lanes:forward` or `turn:lanes:backward`) 
are then trimmed to remove all the keywords found in
https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/tags/TurnTag.java
i.e. 

```java
    enum TurnType
    {
        LEFT,
        SHARP_LEFT,
        SLIGHT_LEFT,
        THROUGH,
        RIGHT,
        SHARP_RIGHT,
        SLIGHT_RIGHT,
        REVERSE,
        MERGE_TO_LEFT,
        MERGE_TO_RIGHT,
        NONE;
    }
    String TURN_LANE_DELIMITER = "\\|";
    String TURN_TYPE_DELIMITER = ";";
```
if the resulting trimmed string is non-empty, that means `turn:lanes` is malformed

```java
    @Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final String turnLanesTag = object.getTag(TurnLanesTag.KEY).orElse("");
        final String turnLanesForwardTag = object.getTag(TurnLanesForwardTag.KEY).orElse("");
        final String turnLanesBackwardTag = object.getTag(TurnLanesBackwardTag.KEY).orElse("");

        if (!this.trimKeywords(turnLanesTag).isEmpty()
                || !this.trimKeywords(turnLanesForwardTag).isEmpty()
                || !this.trimKeywords(turnLanesBackwardTag).isEmpty())
        {
            this.markAsFlagged(object.getOsmIdentifier());

            return Optional.of(this.createFlag(new OsmWayWalker((Edge) object).collectEdges(),
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }
```

Please note that the keywords "LEFT" and "RIGHT" are trimmed towards the end so that the phrases 
"MERGE_TO_LEFT" AND "MERGE_TO_RIGHT" are trimmed first.
```java
    public final String trimKeywords(final String input)
    {
        String result = input.toLowerCase();
        for (final TurnType turnType : TurnTag.TurnType.values())
        {
            if (turnType != TurnTag.TurnType.LEFT && turnType != TurnTag.TurnType.RIGHT)
            {
                result = result.replaceAll(turnType.name().toLowerCase(), "");
            }
        }
        result = result.replaceAll(TurnTag.TurnType.LEFT.name().toLowerCase(), "");
        result = result.replaceAll(TurnTag.TurnType.RIGHT.name().toLowerCase(), "");
        result = result.replaceAll(TurnTag.TURN_LANE_DELIMITER.toLowerCase(), "");
        result = result.replaceAll(TurnTag.TURN_TYPE_DELIMITER.toLowerCase(), "");
        return result.trim();
    }
```

To learn more about the code, please look at the comments in the source code for the check.  
[InvalidTurnLanesValueCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/InvalidTurnLanesValueCheck.java)