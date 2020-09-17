# Invalid Access Tag Check

#### Description

This check flags roads with misused `access=no` tags. 

Misuse is measured by a lack of supporting tags. Tags, such as `public_transport=yes`, can be paired with `access=no` to indicate what does or does not have access. If one or more of these supporting tags are present, `access=no` is valid.

#### Live Example

The way [id:440449063](https://www.openstreetmap.org/way/440449063) has an invalid `access=no` tag, as there are no supporting tags.  

#### Code Review

The first step is to filter out objects that contain tags that could be used to support an `access=no` tag.

The following is a filter that is defined in the configuration to do so. 

```json
"tags.filter":"public_transport->!yes&psv->!yes&bus->!yes&emergency->!yes&motor_vehicle->!no&vehicle->!no&motorcar->!no"
```

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Edges]((https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java)) and [Lines]((https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Line.java)).

The next step is to filter in roads, and make sure they have the tag `access=no`.

The following does so by looking for Edges and Lines with a `highway` tag containing a value greater than a minimum, and the `access=no` tag.
In [Atlas](https://github.com/osmlab/atlas) roads can only be Edges and Lines, and the minimum value ensures that things like trails and bicycle paths are not included.

```java
@Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return AccessTag.isNo(object) && ((object instanceof Edge) || (object instanceof Line))
                && Edge.isMainEdgeIdentifier(object.getIdentifier())
                && !this.isFlagged(object.getOsmIdentifier()) && isMinimumHighway(object);
    }
```

The test for a minimum `highway` value is handled by the following. It uses built in Atlas methods and `highway` value priorities.

```java
private boolean isMinimumHighway(final AtlasObject object)
    {
        final Optional<HighwayTag> result = HighwayTag.highwayTag(object);
        return result.isPresent()
                && result.get().isMoreImportantThanOrEqualTo(this.minimumHighwayType);
    }
```

Finally, there is a test made to see if the road is inside a military zone. Roads in military zones do not require any supporting tags to be validly tagged with `access=no`.

The following performs this test by checking for the road being checked inside all [Areas]((https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java)) and [Relations]((https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java)) that have a `landuse=military` or `military` tag.

```java
private boolean isInMilitaryArea(final LineItem object)
    {
        for (final Area area : object.getAtlas()
                .areas(area -> Validators.isOfType(area, LandUseTag.class, LandUseTag.MILITARY)
                        || Validators.hasValuesFor(area, MilitaryTag.class)))
        {
            final Polygon areaPolygon = area.asPolygon();
            if (object.intersects(areaPolygon)
                    || areaPolygon.fullyGeometricallyEncloses(object.asPolyLine()))
            {
                return true;
            }
        }
        for (final Relation relation : object.getAtlas().relationsWithEntitiesIntersecting(
                        object.bounds(),
                        relation -> (Validators.isOfType(relation, LandUseTag.class, LandUseTag.MILITARY)
                                || Validators.hasValuesFor(relation, MilitaryTag.class))
                                && relation.isMultiPolygon()))
        {
            try
            {
                final MultiPolygon relationPolygon = new RelationOrAreaToMultiPolygonConverter()
                        .convert(relation);
                if (object.intersects(relationPolygon)
                        || relationPolygon.fullyGeometricallyEncloses(object.asPolyLine()))
                {
                    return true;
                }
            }
            catch (final MultiplePolyLineToPolygonsConverter.OpenPolygonException e)
            {
                continue;
            }
        }
        return false;
    }
```

To learn more about the code, please look at the comments in the source code for the check.
[InvalidAccessTagCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/InvalidAccessTagCheck.java)