# ApproximateWayCheck

#### Description

The purpose of this check is to identify ways that are crudely drawn, there is a discrepancy between the drawing 
and the real way, especially for curves.

#### Live Examples

Crudely drawn ways
1. The way [id:168490775](https://www.openstreetmap.org/way/168490775) is crudely drawn and could use more nodes 
and some rearrangement of current nodes.

#### Code Review

This check evaluates [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)
, it attempts to find crudely drawn edges especially those that are curves.

We first validate that the incoming object is: 
* An Edge
* A Master edge
* The Edge is Car Navigable
* The Edge is of a specified minimum highway type
```java
    public boolean validCheckForObject(final AtlasObject object)
    {
        return TypePredicates.IS_EDGE.test(object) && ((Edge) object).isMasterEdge()
                && HighwayTag.isCarNavigableHighway(object) && isMinimumHighwayType(object);
    }
```

Next we split the edge into segments. If there is less than two segments we don't flag the edge. 
Otherwise, we will go through adjacent pairs of segments, first checking that the angle between 
them are within a configurable angle. We get the minimum distance that the middle point is from a bezier curve based 
points from the segments. We then check if the distance is greater than the minDeviationLength and if the ratio between 
the distance and the length of both segments is greater than the configured maxDeviationRatio.

If the angle, and the distance/deviation are both found as valid, the Edge will be flagged.  
```java
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final List<Segment> segments = ((Edge) object).asPolyLine().segments();

        if (segments.size() < 2)
        {
            return Optional.empty();
        }

        final boolean isCrude = IntStream.range(0, segments.size() - 1).anyMatch(index ->
        {
            final Segment seg1 = segments.get(index);
            final Segment seg2 = segments.get(index + 1);
            final double angle = findAngle(seg1, seg2);
            // ignore sharp turns and almost straightaways
            if (angle < minAngle || angle > maxAngle)
            {
                return false;
            }
            final double distance = quadraticBezier(seg1.first(), seg2.first(), seg2.end());
            final double legsLength = seg1.length().asMeters() + seg2.length().asMeters();
            return distance > this.minDeviationLength.asMeters()
                    && distance / legsLength > maxDeviationRatio;
        });

        if (isCrude)
        {
            return Optional.of(
                    createFlag(object, this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }

        return Optional.empty();
    }
```

We build a quadratic bezier curve using the first node of the first segment, the node that joins both segments (anchor), 
and the last node of the second segment. During the creation of the curve we calculate at each step how close the 
curve gets to the anchor, the closest value is returned as the distance (or deviation).
```java
private double quadraticBezier(final Location start, final Location anchor, final Location end)
    {
        final double startX = start.getLongitude().onEarth().asMeters();
        final double startY = start.getLatitude().onEarth().asMeters();
        final double anchorX = anchor.getLongitude().onEarth().asMeters();
        final double anchorY = anchor.getLatitude().onEarth().asMeters();
        final double endX = end.getLongitude().onEarth().asMeters();
        final double endY = end.getLatitude().onEarth().asMeters();

        double min = Double.POSITIVE_INFINITY;
        for (double step = 0; step <= 1; step += bezierStep)
        {
            // https://stackoverflow.com/questions/5634460/quadratic-b%C3%A9zier-curve-calculate-points
            final double pointX = (pow(1 - step, 2) * startX)
                    + (2 * step * (1 - step) * anchorX + pow(step, 2) * endX);
            final double pointY = (pow(1 - step, 2) * startY)
                    + (2 * step * (1 - step) * anchorY + pow(step, 2) * endY);
            // distance from point on bezier curve to anchor
            final double distance = distance(pointX, pointY, anchorX, anchorY);
            if (distance < min)
            {
                min = distance;
            }
        }
        return min;
    }
```

To learn more about the code, please look at the comments in the source code for the check.  
[ApproximateWayCheck.java﻿](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/edges/ApproximateWayCheck.java)
