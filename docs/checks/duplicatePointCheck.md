# Duplicate Point Check

#### Description
This check flags Points when there are two or more in the exact same location.

#### Live Example
The following examples illustrate two cases where there are two or more Points in the exact same location.
1) This Point [id:538464430](https://www.openstreetmap.org/node/538464430) has two Points in the same
location.
2) This Point [id:768590227](https://www.openstreetmap.org/node/768590227) has two Points in the same
location.


#### Code Review
In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Points](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java).
In OpenStreetMap, the concept of Points does not exist, and are referred to as [Ways](https://wiki.openstreetmap.org/wiki/Node).

Our first goal in the Duplicate Point check is to validate the incoming Atlas Object. The object:
* Must be a valid Point

```java
@Override
public boolean validCheckForObject(final AtlasObject object)
{
    return object instanceof Point;
}
```

Next, we check that a given Point object has not already been flagged. If this is the case, then
we draw a bounds around the point and get all the points within the bounds. For each point found, we
check that the Point of interest and the Point found in the box do not have the same identifier and
that they have the same location. If this is true, we want to flag the Point of interest.

```java
@Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Point point = (Point) object;
        if (!this.isFlagged(point.getLocation()))
        {
            final Rectangle box = point.getLocation().boxAround(Distance.meters(0));
            for (final Point dupe : object.getAtlas().pointsWithin(box))
            {
                if (object.getIdentifier() != dupe.getIdentifier()
                        && dupe.getLocation().equals(point.getLocation()))
                {
                    this.markAsFlagged(point.getLocation());
                    return Optional.of(createFlag(object, this.getLocalizedInstruction(0,
                            object.getOsmIdentifier(), point.getLocation())));
                }
            }
        }
        return Optional.empty();
    }
```
To learn more about the code, please look at the comments in the source code for the check.
[DuplicatePointCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/DuplicatePointCheck.java)
