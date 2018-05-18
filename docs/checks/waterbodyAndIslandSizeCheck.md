# Waterbody and Island Size Check

#### Description
This check identifies waterbodies that are either to small, or too large in size. Each waterbody and islands' surface area is calculated and compared to minimum and maximum values set in the configuration. Meters squared is the unit of measurement for minimum values, while kilometers squared used when calculating and comparing maximum values.

#### Live Example
1) This [MultiPolygon Water Relation](https://www.openstreetmap.org/relation/2622285#map=14/59.2859/14.6538) is tagged `natural=WATER`. In this case, the inner members are islands, while the outer member is the waterbody. This particular inner members' ([osm id:372647498](https://www.openstreetmap.org/way/372647498)) surface area is less 10 squared meters.
 
2) This islet ([osm id: 23240011](https://www.openstreetmap.org/way/23240011)) is larger than the configured maximum surface area. According to OSM, any islet greater than 1 kilometer squared should be tagged as `place=ISLAND`.

#### Code Review
In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re are looking at [Areas](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java) and [Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java).

Our first goal is to validate the incoming Atlas object. Valid features for this check will satisfy
the following conditions:
* Must be an Area with one of the following tags:
    * `natural=WATER`
    * `place=ISLET`
    * `place=ISLAND`
    * `place=ARCHIPELAGO`
* If Object is a Relation, the following must be ture:
    * Has `type=MULTIPOLYGON` & `natural=WATER` tags
    * Has at least 2 members 

```java
    @Override
        public boolean validCheckForObject(final AtlasObject object)
        {
        // Object must be either a MultiPolygon Relation with at least one member, or an Area with
        // a place=islet, place=island, place=archipelago, or natural=water tag
        return (object instanceof Relation && IS_MULTIPOLYGON_WATER_RELATION.test(object)
                && ((Relation) object).members().size() >= 2)
                || (object instanceof Area
                        && (TagPredicates.IS_WATER_BODY.test(object) || IS_ISLET.test(object)
                                || IS_ISLAND.test(object))
                        && !this.isFlagged(object.getIdentifier()));
```

After our preliminary filtering, we categorize each object as either an Area or Relation, and go from there. If the object is an Area, we simply calculate the surface area in meters and kilometers, and check whether the island, waterbody, or islet is larger or smaller than the configured maximums and minimums. 

```java
        if (object instanceof Area)
        {
            final Area area = (Area) object;
            final Surface surfaceArea = area.asPolygon().surface();
            final double surfaceAreaMeters = surfaceArea.asMeterSquared();
            final double surfaceAreaKilometers = surfaceArea.asKilometerSquared();
...
        }
```

If the object is a Relation, we must calculate the surface area of each individual member. Similar to above, upon completion of surface area calculation, we can compare the members value to the configured maximum and minimum waterbody and island sizes.

```java
    private Optional<CheckFlag> getMultiPolygonRelationFlags(final Relation relation,
            final Set<RelationMember> relationMembers)
    {
        final CheckFlag flag = new CheckFlag(this.getTaskIdentifier(relation));

        for (final RelationMember member : relationMembers)
        {
            final Surface surfaceArea = ((Area) member.getEntity()).asPolygon().surface();
            final double surfaceAreaMeters = surfaceArea.asMeterSquared();
            final double surfaceAreaKilometers = surfaceArea.asKilometerSquared();
            final long memberOsmId = member.getEntity().getOsmIdentifier();
            
            // Multipolygon Island Relations
            if (member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_INNER))
            {
                if (surfaceAreaMeters < this.islandMinimumArea 
                        || surfaceAreaKilometers > this.islandMaximumArea)
...
            }

            // Multipolygon water body Relations
            if (member.getRole().equals(RelationTypeTag.MULTIPOLYGON_ROLE_OUTER))
            {
                if (surfaceAreaMeters < this.waterbodyMinimumArea
                        || surfaceAreaKilometers > this.waterbodyMaximumArea)
...
            }
        }

    }
```

To learn more about the code, please look at the comments in the source code for the check.
[WaterbodyAndIslandSizeCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/areas/WaterbodyAndIslandSizeCheck.java)