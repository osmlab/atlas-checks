# WaterWay Checks

#### Description
This check identifies waterways that are closed (i.e., would have a circular water flow), waterways that do not have a place for the water to go (a "sink"), and waterways crossing waterways. It also looks for ways that may be going uphill (requires elevation data, see [ElevationUtilities](../elevationUtilities.md))

#### Live Example
1) This canal ([osm id: 46115760 version 5](https://www.openstreetmap.org/way/46115760)) is a closed waterway, which makes no semantic sense without a pump somewhere in the waterway. In this case, it should be tagged `natural=water` + `water=canal`.

2) The waterways at ([osm id: 500672157](https://www.openstreetmap.org/node/500672157) on 2020-08-20) cross each other, when they should be connected.

#### Useful configuration variables:
```json
{
    "WaterWayCheck": {
        "waterway.sink.tags.filters": "natural->sinkhole|waterway->tidal_channel,drain|manhole->drain",
        "waterway.tags.filters": "waterway->river,stream,tidal_channel,canal,drain,ditch,pressurised",
        "ocean.valid": "natural->strait,channel,fjord,sound,bay|harbour->*&harbour->!no|estuary->*&estuary->!no|bay->*&bay->!no|place->sea|seamark:type->harbour,harbour_basin,sea_area|water->bay,cove,harbour|waterway->artificial,dock",
        "ocean.boundary": "natural->coastline",
        "waterway.elevation.resolution.min.uphill": "1" (meter),
        "waterway.elevation.resolution.min.start.end": "457.2" (meters)
    }
}
```
You may also want to look at [ElevationUtilities](../utilities/elevationUtilities.md).

#### Code Review
In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines,
Nodes & Relations; in our case, we’re are looking at [LineItems](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/LineItem.java), which Lines and Edges are specific subtypes of. This is due to the fact that a waterway may either be a _navigable_ way or a _non-navigable_ way (rivers are generally considered _navigable_, while streams may or may not be _navigable_).

Our first goal is to validate the incoming Atlas object. Valid features for this check will satisfy
the following conditions:
* Must be an LineItem with one of the following tags:
    * `waterway=RIVER`
    * `waterway=STREAM`
    * `waterway=TIDAL_CHANNEL`
    * `waterway=CANAL`
    * `waterway=DRAIN`
    * `waterway=DITCH`
    * `waterway=PRESSURISED`

```java
    @Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return !isFlagged(object.getOsmIdentifier()) && object instanceof LineItem
                && this.waterwayTagFilter.test(object);
    }
```

After the preliminary filtering, each object goes through a series of `if` statements. The first checks if the line is closed. The second checks if the waterway is going uphill (requires elevation data), and if the resolution of the elevation data is good enough to determine that the waterway goes uphill, the object is flagged. At this point, we check to see if the waterway ends in a sink, for this check, we attempt ensure that the waterway ends inside the boundaries, and not in a neighboring area. Furthermore, we reuse the check for uphill ways to help improve the error message. Once all of those checks have finished, we check for waterway intersections. If more than one check flags the object, instructions and offending objects are appended to the CheckFlag.


To learn more about the code, please look at the comments in the source code for the check.
[WaterWayCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/lines/WaterWayCheck.java)
