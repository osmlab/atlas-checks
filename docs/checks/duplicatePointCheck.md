# Duplicate Point Check

#### Description
The Duplicate Point check flags Points in OSM that share the same location. Whether it's a street
light, viewpoint, or tree, a Point represents something that exists at that location, therefore, we
do not want points on top of each other. Also, as per the [One feature, one OSM element principle](https://wiki.openstreetmap.org/wiki/One_feature,_one_OSM_element), 
we don't want instances of duplicate Points that represent the same feature. 

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
* Location of the Point object must not have been seen before

```java
@Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Point && !this.isFlagged(((Point) object).getLocation());
    }

```

Next, we get all Points at that Point's location. If there is more than 1 Point object found to be
at that location, we mark the Points as flagged, get all the duplicate Point identifiers at
that location, and flag them. Otherwise, we do not flag the Point.

```java
 @Override
     protected Optional<CheckFlag> flag(final AtlasObject object)
     {
         final Point point = (Point) object;
 
         final List<Point> duplicates = Iterables
                 .asList(object.getAtlas().pointsAt(point.getLocation()));
         if (duplicates.size() > 1)
         {
             duplicates.forEach(duplicate -> this.markAsFlagged(point.getLocation()));
             final List<Long> duplicateIdentifiers = duplicates.stream()
                     .map(AtlasEntity::getOsmIdentifier).collect(Collectors.toList());
             return Optional.of(this.createFlag(object,
                     this.getLocalizedInstruction(0, duplicateIdentifiers, point.getLocation())));
         }
 
         return Optional.empty();
     }
```
To learn more about the code, please look at the comments in the source code for the check.
[DuplicatePointCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/DuplicatePointCheck.java)
