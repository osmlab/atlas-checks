# Missing Relation Type Check

#### Description
This check identifies relations without relation type. In OSM, a [Relation](https://wiki.openstreetmap.org/wiki/Elements#Relation)
suppose to have one of proposed relation [Type](https://wiki.openstreetmap.org/wiki/Types_of_relation). 

#### Live Example
The following examples illustrate two cases where a Relation is missing relation type.
1) This relation [id:7508794](https://www.openstreetmap.org/relation/7508794) is missing "multipolygon" relation type.
2) This relation [id:9769731](https://www.openstreetmap.org/relation/9769731) is missing "boundary" relation type.

#### Code Review
In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java).

Our first goal is to validate the incoming Atlas Object. We use some preliminary filtering to target
Relation objects. Therefore, we use:
* Must be a Relation
* Must not have a Relation Type tag
* Must not be a "One Member Relation"
* Must not have disused:type or disabled:type tags [reference](https://wiki.openstreetmap.org/wiki/Key:disused:)

After the preliminary filtering, Relation will be flagged.

To learn more about the code, please look at the comments in the source code for the check.
[MissingRelationTypeCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/relations/MissingRelationTypeCheck.java)