### Description:

This check handles the five classes in [Osmose#1260](https://wiki.openstreetmap.org/wiki/Osmose/issues#1260):

Class 1 "The track of this route contains gaps" 
Class 2 "The stop or platform is too far from the track of this route" 
Class 3 "Non route relation member in route_master relation" 
Class 4 "Public transport relation route not in route_master relation" 
Class 5 "network, operator, ref, colour tag should be the same on route and route_master relations"

#### Live Example
An example for route track containing gaps. 
- Relation [id:1966335](https://www.openstreetmap.org/relation/1966335)

An example for public transport route track not contained in a route master relation.
- Relation [id:967674](https://www.openstreetmap.org/relation/967674)

#### Code Review

Please see the source code for the check at: [RouteRelationCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/relations/RouteRelationCheck.java)