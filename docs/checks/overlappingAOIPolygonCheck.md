# Overlapping AOI Polygon Check 

This check flags polygons that overlap and represent the same Area of Interest (AOI).

AOIs are defined through the configurable value `aoi.tags.filters`. This is a list of tag filters. If a polygon has any of the tags in any of the filters, it is considered an AOI.

The defaults AOI tag filters are:  

* `amenity->FESTIVAL_GROUNDS`
* `amenity->GRAVE_YARD|landuse->CEMETERY`
* `boundary->NATIONAL_PARK,PROTECTED_AREA|leisure->NATURE_RESERVE,PARK`
* `historic->BATTLEFIELD`
* `landuse->FOREST|natural->WOOD`
* `landuse->RECREATION_GROUND|leisure->RECREATION_GROUND`
* `landuse->VILLAGE_GREEN|leisure->PARK`
* `leisure->GARDEN`
* `leisure->GOLF_COURSE|sport->GOLF`
* `leisure->PARK&name->*`
* `natural->BEACH`
* `tourism->ZOO`

#### Live Examples

1. The way [id:99881325](https://www.openstreetmap.org/way/99881325) overlaps way [id:173830769](https://www.openstreetmap.org/way/173830769) and they are both tagged with `leisure=PARK`.
2. The way [id:54177792](https://www.openstreetmap.org/way/54177792), with tag `landuse=FOREST`, overlaps way [id:338963545](https://www.openstreetmap.org/way/338963545), with similar tag `natural=WOOD`.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes, Areas & Relations; in our case, we’re are looking at [Areas](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java).

First we check if the object is an Area and it has a tag that identifies it as an AOI.

```java
@Override
public boolean validCheckForObject(final AtlasObject object)
{
    // Checks for areas, that are not flagged, and pass any of the TaggableFilters in
    // aoiFilters. aoiFiltersTest() and aoiFilters is used in place of a single TaggableFilter
    // so that each filter may be tested separately later.
    return object instanceof Area && !this.isFlagged(object.getIdentifier())
            && aoiFiltersTest(object);
}
```

The AOI tag test is performed by looping through all the tag filters in the configurable value.

```java
private boolean aoiFiltersTest(final AtlasObject object)
    {
        return this.aoiFilters.stream().anyMatch(filter -> filter.test(object));
    }
```

Then the Areas that overlap the initial Area are found, and tested to see if the minimum overlap is met (configurable) and if they have the same or similar AOI tag.

```java
@Override
protected Optional<CheckFlag> flag(final AtlasObject object)
{
    final Area aoi = (Area) object;
    final Polygon aoiPolygon = aoi.asPolygon();
    final Rectangle aoiBounds = aoiPolygon.bounds();
    boolean hasOverlap = false;

    // Set of overlapping area AOIs
    final Set<Area> overlappingAreas = Iterables
            .stream(object.getAtlas().areasIntersecting(aoiBounds,
                    area -> area.getIdentifier() != aoi.getIdentifier()
                            && !this.isFlagged(area.getIdentifier())
                            && area.intersects(aoiPolygon) && aoiFiltersTest(area)))
            .collectToSet();

    final CheckFlag flag = new CheckFlag(this.getTaskIdentifier(object));
    flag.addObject(object);

    // Test each overlapping AOI to see if it overlaps enough and passes the same AOI filter as
    // the object
    for (final Area area : overlappingAreas)
    {
        if (this.hasMinimumOverlapProportion(aoiPolygon, area.asPolygon())
                && aoiFiltersTest(object, area))
        {
            flag.addObject(area);
            flag.addInstruction(this.getLocalizedInstruction(0, object.getOsmIdentifier(),
                    area.getOsmIdentifier()));
            this.markAsFlagged(area.getIdentifier());
            hasOverlap = true;
        }
    }

    if (hasOverlap)
    {
        this.markAsFlagged(object.getIdentifier());
        return Optional.of(flag);
    }

    return Optional.empty();
}
```

The minimum overlap test uses the configurable double value `intersect.minimum.limit` and compares the areas by clipping one against the other.

```java
private boolean hasMinimumOverlapProportion(final Polygon polygon, final Polygon otherPolygon)
    {

        Clip clip = null;
        try
        {
            clip = polygon.clip(otherPolygon, Clip.ClipType.AND);
        }
        catch (final TopologyException e)
        {
            System.out
                    .println(String.format("Error clipping [%s] and [%s].", polygon, otherPolygon));
        }

        // Skip if nothing is returned
        if (clip == null)
        {
            return false;
        }

        // Sum intersection area
        long intersectionArea = 0;
        for (final PolyLine polyline : clip.getClip())
        {
            if (polyline != null && polyline instanceof Polygon)
            {
                final Polygon clippedPolygon = (Polygon) polyline;
                intersectionArea += clippedPolygon.surface().asDm7Squared();
            }
        }

        // Avoid division by zero
        if (intersectionArea == 0)
        {
            return false;
        }

        // Pick the smaller building's area as baseline
        final long baselineArea = Math.min(polygon.surface().asDm7Squared(),
                otherPolygon.surface().asDm7Squared());
        final double proportion = (double) intersectionArea / baselineArea;

        return proportion >= this.minimumIntersect;
    }
```

The similar AOI tag test loops through the AOI filters and finds the one that the initial Area matches and tests it against the overlapping Area.

```java
private boolean aoiFiltersTest(final AtlasObject object, final Area area)
    {
        return this.aoiFilters.stream()
                .anyMatch(filter -> filter.test(object) && filter.test(area));
    }
```

To learn more about the code, please look at the comments in the source code for the check.
[OverlappingAOIPolygonCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/areas/OverlappingAOIPolygonCheck.java)