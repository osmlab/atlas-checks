# Invalid Geometry Check

#### Description

This check flags invalid polyline and polygon geometries. 

#### Live Example

The Way [id:803496316](https://www.openstreetmap.org/way/803496316) is an invalid self intersecting polygon. 

#### Code Review

This check looks at three types of Atlas Items: [Areas](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Area.java), [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java), and [Lines](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Line.java).
A feature is considered valid for the check if it is one of those types and has not been country sliced.

Geometries are validated using the [Java Topology Suite](https://github.com/locationtech/jts) (JTS).
The Atlas geometries are converted to JTS geometries. 
If a features fails to pass the JTS geometry `.isSimple()` or `.isValid()` methods then it is flagged.

To learn more about the code, please look at the comments in the source code for the check.
[InvalidGeometryCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/geometry/InvalidGeometryCheck.java)
