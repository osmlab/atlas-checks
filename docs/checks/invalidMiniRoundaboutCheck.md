# Invalid Mini Roundabout Check 

In OSM, a mini-roundabout is recorded using the tag `highway=MINI_ROUNDABOUT`. However, many 
intersections are erroneously tagged as mini-roundabouts when they are not truly mini-roundabouts.
This check filters out some of those cases. More specifically, this check flags all Atlas objects that 
meet the following conditions:
 - Is a `Node` object
 - Has the tag `highway=MINI_ROUNDABOUT`
 - Has less than `valence.minimum` number of car-navigable connecting edges AND
     - Has exactly two connecting edges and exactly one of those edges is a main edge OR
     - The node has neither `direction=CLOCKWISE` nor `direction=ANTICLOCKWISE` values

After performing sensitivity analysis, the best value for `valence.minimum` was determined to be 6, 
but this number is configurable in `config/configuration.json`.

#### Live Examples

The node [1092811451](https://www.openstreetmap.org/node/1092811451) is incorrectly flagged as a 
mini-roundabout and should probably be flagged as a `highway=TURNING_LOOP` or 
`highway=TURNING_CIRCLE` instead. 

The node [2367787523](https://www.openstreetmap.org/node/2367787523) has only 5 connecting car-navigable
edges. Satellite imagery confirms that this is not a mini-roundabout and should be reviewed by an editor.

#### Code Review

For more information, see the source code in 
[InvalidMiniRoundaboutCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/InvalidMiniRoundaboutCheck.java).
