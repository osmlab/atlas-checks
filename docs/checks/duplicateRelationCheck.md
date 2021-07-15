# Duplicate Relation Check

#### Description
The Duplicate Relation check flags multiple members Relations in OSM that have the same OSM tags and the same members with same roles.
We don't want instances of duplicate Relation that represent the same feature. 

#### Live Example
The following examples illustrate two relations have the same tags and member.
1) This Relation [id:2096616](https://www.openstreetmap.org/relation/2096616) 
2) This Relation [id:2845741](https://www.openstreetmap.org/relation/2845741) 


#### Code Review

To learn more about the code, please look at the comments in the source code for the check.
[DuplicateRelationCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/relation/DuplicateRelationCheck.java)