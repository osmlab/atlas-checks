# Duplicate Map Feature Check

#### Description
The DuplicateMapFeaturecheck flags node, way or relation which have duplicate map features in areas or connected locations.
[Osmose#4080](https://wiki.openstreetmap.org/wiki/Osmose/issues#4080):
    Details
    Object tagged twice as node, way or relation:
    Class 1 "Object tagged twice as node and way" 
    Class 2 "Object tagged twice as way and relation" 
    Class 3 "Object tagged twice as node and relation" 

This check attempts to identify duplicate map features in areas or connected locations for Class#1/#2/#3 when they have the same OSM tags or have the same following features on node/way/relation:
    1)  amenity
    2)  leisure
    3)  building 
    4)  shop  


#### Live Example
The following examples illustrate map feature tagged as node and way.
1) Way [id:945675753](https://www.openstreetmap.org/way/945675753)
2) Node [id:8738551735](https://www.openstreetmap.org/node/8738551735)

The following examples illustrate map feature tagged as way and relation.
1) Relation [id:3916231](https://www.openstreetmap.org/relation/3916231)
2) Way [id:167449892](https://www.openstreetmap.org/way/167449892)

The following examples illustrate map feature tagged as node and relation.
1) Relation [id:12331039](https://www.openstreetmap.org/relation/12331039)
2) Node [id:2499265898](https://www.openstreetmap.org/node/2499265898)

#### Code Review

To learn more about the code, please look at the comments in the source code for the check.
[DuplicateMapFeatureCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/DuplicateMapFeatureCheck.java)