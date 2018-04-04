# Address Point Match Check

#### Description
This check identifies Point objects in OSM that have a specified street number (addr:housenumber) 
but no specified street name (addr:street). No specified street name refers to either having a null
value for the street name key, or no street name key present at all.

#### Live Example
The following examples illustrate two cases where a Point has a street number but no street name
in its tags.
1) This Point [id:977597085](https://www.openstreetmap.org/node/977597085) has a street number but 
no street name specified in the address.
2) This Point [id:4552330391](https://www.openstreetmap.org/node/4552330391) has a street number but
no street name specified in the address.

#### Code Review
In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re are looking at [Points](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java)
with specified street numbers but no street name.

Our first goal is to validate the incoming Atlas object. Valid features for this check will satisfy
the following conditions:
* Must be a valid Point object
* Must not be part of an associated street Relation
* Must not have the addr:street tag, have the tag but with a null value, or have the tag but with no value

```java
 @Override
     public boolean validCheckForObject(final AtlasObject object)
     {
         // Object is an instance of Point
         return object instanceof Point
                 // And does not have an Associated Street Relation
                 && !hasAssociatedStreetRelation(object)
                 // And either doesn't have the addr:street tag, has the tag but has a null value,
                 // or has the tag but has no value
                 && Strings.isNullOrEmpty(object.tag(AddressStreetTag.KEY));
     }
```

After the preliminary filtering of features, we need to find candidates for populating each feature's
missing street name. We do this by drawing a bounding box, leveraging the [`boxAround()`](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/Location.java), 
around the Point of interest using a configurable bounds size parameter. Using these bounds, we can consider other Points that fall inside the bounds 
that have a specified street number and street name using [`pointsWithin()`](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/Atlas.java).
If there are no other Points found within this bounding box, we next look at edges within or which 
intersect with our bounding box using [`edgesIntersecting()`](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/Atlas.java).
If there are no Points or Edges within or intersecting our bounds, we still flag the problematic
Point feature but with a specific instruction message indicating that no candidate street names were found.
Otherwise, we flag the Point and list a Set of unique candidate street names in the instructions.

```java
   @Override
      protected Optional<CheckFlag> flag(final AtlasObject object)
      {
          final Point point = (Point) object;
  
          // Get a bounding box around the Point of interest
          final Rectangle box = point.getLocation().boxAround(boundsSize);
  
          // Get all Points in the bounding box, remove Points that have null as their
          // street name or do not have the street name key tag, and get a set of candidate street
          // names
          final Set<String> points = Iterables.stream(point.getAtlas().pointsWithin(box))
                  .map(nearbyPoint -> nearbyPoint.tag(AddressStreetTag.KEY)).filter(Objects::nonNull)
                  .collectToSet();
  
          // Get all Edges intersecting the bounding box, remove Edges that have null as their
          // street name or do not have the street name key tag, and get a set of candidate street
          // names
          final Set<String> edges = Iterables.stream(point.getAtlas().edgesIntersecting(box))
                  .map(nearbyEdge -> nearbyEdge.tag(NameTag.KEY)).filter(Objects::nonNull)
                  .collectToSet();
  
          // If there are no Points or Edges in the bounding box
          if (points.isEmpty() && edges.isEmpty())
          {
              // Flag Point with instruction indicating that there are are no suggestions
              return Optional.of(this.createFlag(point,
                      this.getLocalizedInstruction(2, point.getOsmIdentifier())));
          }
          // If there are Points in the bounding box
          else if (!points.isEmpty())
          {
              // Add all interior Point street names to the list of candidate street names
              return Optional.of(this.createFlag(point,
                      this.getLocalizedInstruction(0, point.getOsmIdentifier(), points)));
          }
          // If there are Edges intersecting or contained by the bounding box
          else
          {
              return Optional.of(this.createFlag(point,
                      this.getLocalizedInstruction(1, point.getOsmIdentifier(), edges)));
          }
      }
```

To learn more about the code, please look at the comments in the source code for the check.
[AddressPointMatchCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/points/AddressPointMatchCheck.java)