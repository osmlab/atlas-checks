# OpenBoundaryCheck

#### Description
This check attempts to check for Admin Boundary Relations that should be closed polygons but are not closed.

#### Configurables

#### Live Examples
Yakintas neighborhood is not a vclosed boundary but should be.
1. The relation [id:8284136](https://www.openstreetmap.org/relation/8284136) is not a closed boundary.

Seattle is a false positive since it is a closed boundary.
1. The relation [id:237385](https://www.openstreetmap.org/relation/237385) is a closed boundary and would not be flagged.

#### Code Review
This check evaluates [Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java)
, finding admin boundaries that are not closed.

##### Validating the Object
We first validate that the incoming object is:
* Is a Relation
* Is of type Boundary
* Has admin_level tags

##### Flagging the Relation
Catch the [OpenPolygonException](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/converters/MultiplePolyLineToPolygonsConverter.java) while using the [RelationOrAreaToMultiPolygonConverter](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/complex/RelationOrAreaToMultiPolygonConverter.java)
to inform that the polygon is not closed.

##### Miscellaneous
To learn more about the code, please look at the comments in the source code for the check.  
[OpenBoundaryCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/relations/OpenBoundaryCheck.java)