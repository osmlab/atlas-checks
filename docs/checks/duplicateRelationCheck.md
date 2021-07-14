# Duplicate Relation Check

#### Description
The Duplicate Relation check flags Relations in OSM that have the same OSM tags and the same members with same roles.
we don't want instances of duplicate Relation that represent the same feature. 

#### Live Example
The following examples illustrate two relations have the same tags and member.
1) This Relation [id:10392649](https://www.openstreetmap.org/relation/10392649) 
2) This Relation [id:10392650](https://www.openstreetmap.org/relation/10392650) 


#### Code Review

To learn more about the code, please look at the comments in the source code for the check.
[DuplicateRelationCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/relation/DuplicateRelationCheck.java)