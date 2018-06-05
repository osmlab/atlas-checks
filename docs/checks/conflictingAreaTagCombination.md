# Conflicting Area Tag Check

#### Description
This check flags Atlas Area Objects that have conflicting tag combinations. An Atlas Area Object is an enclosed Polygon that (by default) uses this [tag filter](https://github.com/osmlab/atlas/blob/dev/src/main/resources/org/openstreetmap/atlas/geography/atlas/pbf/atlas-area.json) to differentiate itself from other Atlas Objects.

#### Live Example
1) An OSM Editor has mapped this feature ([osm id: 372444940](https://www.openstreetmap.org/way/372444940#map=19/-6.18454/35.74750)) as a building and a tree, using the `nautual=TREE` and `building=YES` tags. These two tags should not logically co-exist. An editor should review the feature and make the appropriate changes.
 
2) This closed Way ([osm id: 452182969](https://www.openstreetmap.org/way/452182969#map=19/7.77603/81.21694)) is tagged as `natural=WATER` and `man_made=WATER_TOWER`. The `natural=WATER` tag should be used to tag areas of water (e.g. lakes, multipolygon water relations), or water bodies. Water towers are man made structures that contain water, therefore, these two should not co-exist.

#### Code Review
In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes & Relations; in our case, we’re are looking at [Areas](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java).

Our first goal is to validate the incoming Atlas object. Valid features for this check will satisfy the following conditions:
* Must be an Atlas Area Object
* Cannot have an `area=NO` tag.

```java
@Override
public boolean validCheckForObject(final AtlasObject object)
{
    return object instanceof Area
        && Validators.isNotOfType(object, BuildingTag.class, BuildingTag.NO);
}
```

Using the Validators class, we store each conflicting combination into a [Predicate](https://docs.oracle.com/javase/8/docs/api/java/util/function/Predicate.html) variable that can be used to test its truthiness.
```java
private static final Predicate<Taggable> NATURAL_WATER_MANMANDE = object -> 
        Validators.isOfType(object, NaturalTag.class, NaturalTag.WATER)
        && Validators.isNotOfType(object, ManMadeTag.class, ManMadeTag.RESERVOIR_COVERED, ManMadeTag.WASTEWATER_PLANT);;
```

For the variable above to be truthy, the following conditions must be true:
* Area has `natural=WATER` tag
* Area has a `man_made` tag AND its' value must not equal `RESERVOIR_COVERED` OR `WASTEWATER_PLANT`

Then, we can easily test each combination using Predicate's `test()` function.

```java
if (NATURAL_WATER_MANMANDE.test(object))
{
    flag.addInstruction(this.getLocalizedInstruction(FOUR));
    hasConflictingCombinations = true;
}
```

To learn more about the code, please look at the comments in the source code for the check.
[ConflictingAreaTagCombination.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/ConflictingAreaTagCombination.java)
