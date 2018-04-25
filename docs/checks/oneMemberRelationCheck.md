# One Member Relation Check

#### Description

This check identifies relations that only contain one member. In OSM, a [Relation](https://wiki.openstreetmap.org/wiki/Elements#Relation)
is a multi-purpose data structure that documents a relationship between two or more data elements 
(nodes, ways, and/or other relations).

#### Live Example
The following examples illustrate two cases where a Relation contains only one member.
1) This relation [id:4646212](https://www.openstreetmap.org/relation/4646212) has only one member.
2) This relation [id:7691824](https://www.openstreetmap.org/relation/7691824) has only one member.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, 
Nodes & Relations; in our case, we’re working with [Relations](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Relation.java).

Our first goal is to validate the incoming Atlas Object. We use some preliminary filtering to target
Relation objects. Therefore, we use:
* Must be a Relation
* Must not have been flagged before

```java
    @Override
        public boolean validCheckForObject(final AtlasObject object)
        {
            return object instanceof Relation && !this.isFlagged(object.getOsmIdentifier());
        }
```

After the preliminary filtering of features, we get the members of that relation.
If the relation is a multi-polygon relation, then we need to check if any of the members are an inner
ring member. If there is an inner member and it is the only member of the relation, then we opt to flag
the relation as problematic. Multi-polygon relations with 1 to n outer members, 1 to n inner members, or
a combination of both outer and inner members are valid and should not be flagged. If the relation is
not a multi-polygon relation, we simply check if there is one member in the relation and flag.


```java
     @Override
         protected Optional<CheckFlag> flag(final AtlasObject object)
         {
             final Relation relation = (Relation) object;
             // Initialize a flag that will be flipped if the relation is found to be problematic
             boolean flag = false;
     
             // If the relation is a multipolygon
             if (relation.isMultiPolygon())
             {
                 // Determine if there are members with the role:inner
                 final boolean isInner = relation.members().stream().anyMatch(member -> member.getRole()
                         .equalsIgnoreCase(RelationTypeTag.MULTIPOLYGON_ROLE_INNER));
     
                 // If the only member of the relation has the role:inner then we want to flag the
                 // relation
                 if (isInner && relation.members().size() == 1)
                 {
                     flag = true;
                 }
                 // If the relation has only one member and is not a multi-polygon
             }
             else if (relation.members().size() == 1)
             {
                 flag = true;
             }
     
             if (flag)
             {
                 this.markAsFlagged(relation.getOsmIdentifier());
                 return Optional.of(createFlag(
                         relation.members().stream().map(member -> member.getEntity())
                                 .collect(Collectors.toSet()),
                         this.getLocalizedInstruction(0, relation.getOsmIdentifier())));
             }
             return Optional.empty();
         }
```


To learn more about the code, please look at the comments in the source code for the check.
[OneMemberRelationCheck.java](../../src/main/java/org/openstreetmap/atlas/checks/validation/relations/OneMemberRelationCheck.java)