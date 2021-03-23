# Malformed PolyLine Check

#### Description

This check flags polylines (Edges and Lines) that are not coastlines or rivers, too long/short, or have too many/few points. Polylines that are too long or have too many points are hard to edit; those that are too short or have too few points can be eliminated and replaced by other points or polylines. 

#### Configuration

There are no configurables for this check.

#### Live Examples

- Line [id:763206400](https://www.openstreetmap.org/way/763206400) has too many nodes.
- Edge [id:768275271](https://www.openstreetmap.org/way/768275271) has too many nodes. 
- Edge [id:791952333](https://www.openstreetmap.org/way/791952333) has too many nodes.

Please see the source code for MalformedPolyLineCheck here: [MalformedPolyLineCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/linear/MalformedPolyLineCheck.java)