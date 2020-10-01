# Water Area Check

#### Description
This check identifies waterbodies that cross each other or are missing a waterway (if the water area requires a water way).

#### Live Example
1) This [riverbank](https://www.openstreetmap.org/way/661564606) is missing a river waterway (on 2020-09-09).

2) This [pond](https://www.openstreetmap.org/way/448755487) is overlapping another [pond](https://www.openstreetmap.org/way/455609665).

#### Code Review
In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines,
Nodes, Areas & Relations; in our case, we’re are looking at [Areas](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java).

Our first goal is to validate the incoming Atlas object. Valid features for this check will satisfy
the following conditions:
* Must be an Area with one of the following tags:
    * `natural=WATER` AND `water=*`
    * `wterway=RIVERBANK`

After the preliminary filtering, each object goes through a series of checks, the first of which checks to see if there should be a waterway inside the object, the second checks to ensure that water has a place to go (waterway exists the water body), and the third checks for overlapping waterways.


### Sample configuration (defaults)
```
{
    "WaterAreaCheck": {
        "intersect.minimum.limit": 0.01,
        "water.tags.crossing.ignore": ["waterway->dam"],
        "water.tags.filters": ["waterway->*"],
        "water.tags.filtersrequireswaterway": ["natural->water&water->river,stream_pool,canal,lock|waterway->riverbank"],
        "waterway.tags.filters": ["natural->water&water->*|waterway->riverbank"]
    }
}
```

To learn more about the code, please look at the comments in the source code for the check.
[WaterAreaCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/areas/WaterbodyAreaCheck.java)
